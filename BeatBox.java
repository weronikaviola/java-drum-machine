import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;

public class BeatBox {
  JPanel mainPanel;
  ArrayList<JCheckBox> checkboxList;
  Sequencer sequencer;
  Sequence sequence;
  Track track;
  JFrame theFrame;
  JLabel tempoLabel;
  String[] instrumentNames = {
    "Bass Drum",
    "Closed Hi-Hat",
    "Open Hi-Hat",
    "Acoustic Snare",
    "Crash Cymbal",
    "Hand Clap",
    "Hight Tom",
    "High Bongo",
    "Maracas",
    "Whistle",
    "Low Conga",
    "Cowbell",
    "Vibraslap",
    "Low-mid Tom",
    "High Agogo",
    "Open Hi Conga",
  };
  int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};
  JButton startStop;

  public static void main (String[] args) {
    new BeatBox().buildGUI();
  }

  public void buildGUI() {
    theFrame = new JFrame("Cyber BeatBox");
    theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    BorderLayout layout = new BorderLayout();
    JPanel background = new JPanel(layout);
    background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    checkboxList = new ArrayList<JCheckBox>();
    Box buttonBox = new Box(BoxLayout.Y_AXIS);
    buttonBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    buttonBox.setOpaque(true);
    buttonBox.setBackground(new Color(200, 19, 100));

    startStop = new JButton("Start");
    startStop.addActionListener(new MyStartListener());
    buttonBox.add(startStop);

    Box tempoBox = new Box(BoxLayout.X_AXIS);
    ImageIcon leftAngle = new ImageIcon("left.png");
    ImageIcon rightAngle = new ImageIcon("right.png");
    ImageIcon metronome = new ImageIcon("metronome.png");

    JButton downTempo = new JButton("");
    downTempo.setIcon(leftAngle);
    downTempo.addActionListener(new MyDownTempoListener());
    tempoBox.add(downTempo);

    JButton metronomeBtn = new JButton("");
    metronomeBtn.setIcon(metronome);
    metronomeBtn.addActionListener(new MyResetTempoListener());
    tempoBox.add(metronomeBtn);
    
    JButton upTempo = new JButton("");
    upTempo.setIcon(rightAngle);
    upTempo.addActionListener(new MyUpTempoListener());
    tempoBox.add(upTempo);

    JButton saveBtn = new JButton("save");
    saveBtn.addActionListener(new MySendListener());
    buttonBox.add(saveBtn);

    JButton openBtn = new JButton("open");
    openBtn.addActionListener(new MyReadInListener());
    buttonBox.add(openBtn);

    JButton resetBtn = new JButton("clear all");
    resetBtn.addActionListener(new MyClearListener());
    buttonBox.add(resetBtn);

    buttonBox.add(tempoBox);

    Box nameBox = new Box(BoxLayout.Y_AXIS);
    nameBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    for (int i = 0; i < 16; i++) {
      nameBox.add(new Label(instrumentNames[i]));
    }
    nameBox.setOpaque(true);
    nameBox.setBackground(new Color(66, 170, 245));

    background.add(BorderLayout.EAST, buttonBox);
    background.add(BorderLayout.WEST, nameBox);

    theFrame.getContentPane().add(background);

    GridLayout grid = new GridLayout(16, 16);
    grid.setVgap(1);
    grid.setHgap(2);
    mainPanel = new JPanel(grid);
    background.add(BorderLayout.CENTER, mainPanel);

    for (int i = 0; i < 256; i++) {
      JCheckBox c = new JCheckBox();
      c.setSelected(false);
      checkboxList.add(c);
      mainPanel.add(c);
    }

    setUpMidi();

    theFrame.setBounds(50, 50, 300, 300);
    theFrame.pack();
    theFrame.setVisible(true);
  }

  public void setUpMidi() {
    try {
      sequencer = MidiSystem.getSequencer();
      sequencer.open();
      sequence = new Sequence(Sequence.PPQ, 4);
      track = sequence.createTrack();
      sequencer.setTempoInBPM(120);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void buildTrackAndStart() {
    int[] trackList = null;

    sequence.deleteTrack(track);
    track = sequence.createTrack();

    for (int i = 0; i < 16; i++) {
      trackList = new int[16];

      int key = instruments[i];

      for (int j = 0; j < 16; j++) {
        JCheckBox jc = checkboxList.get(j + 16 * i);
        if (jc.isSelected()) {
          trackList[j] = key;
        } else {
          trackList[j] = 0;
        }
      }

      makeTracks(trackList);
      track.add(makeEvent(176, 1, 127, 0, 16));
    }

    track.add(makeEvent(192, 9, 1, 0, 15));

    try {
      sequencer.setSequence(sequence);
      sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
      sequencer.start();
      sequencer.setTempoInBPM(120);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public class MyStartListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      buildTrackAndStart();
      startStop.removeActionListener(this);
      startStop.setText("Stop");
      startStop.addActionListener(new MyStopListener());
    }
  }

  public class MyStopListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      sequencer.stop();
      startStop.removeActionListener(this);
      startStop.setText("Start");
      startStop.addActionListener(new MyStartListener());
    }
  }

  public class MyUpTempoListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      float tempoFactor = (float) (sequencer.getTempoFactor() * 1.05);
      sequencer.setTempoFactor(tempoFactor);
    }
  }

  public class MyDownTempoListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      float tempoFactor = (float) (sequencer.getTempoFactor() * 0.95);
      sequencer.setTempoFactor(tempoFactor);
    }
  }

  public class MyResetTempoListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      sequencer.setTempoFactor(1);
    }
  }

  public class MySendListener implements ActionListener {
    public void actionPerformed(ActionEvent evt) {
      boolean[] checkboxState = new boolean[256];
      for (int i = 0; i < 256; i++) {
        JCheckBox check = (JCheckBox) checkboxList.get(i);
        if (check.isSelected()) {
          checkboxState[i] = true;
        }
      }

      JFileChooser fileSave = new JFileChooser();
      fileSave.showSaveDialog(theFrame);
      
      try {
        FileOutputStream fileStream = new FileOutputStream(fileSave.getSelectedFile());
        ObjectOutputStream os = new ObjectOutputStream(fileStream);
        os.writeObject(checkboxState);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  public class MyReadInListener implements ActionListener {
    public void actionPerformed(ActionEvent evt) {
      boolean[] checkboxState = null;

      JFileChooser fileOpen = new JFileChooser();
      fileOpen.showOpenDialog(theFrame);

      try {
        FileInputStream fileIn = new FileInputStream(fileOpen.getSelectedFile());
        ObjectInputStream is = new ObjectInputStream(fileIn);
        checkboxState = (boolean[]) is.readObject();
      } catch(Exception ex) {
        ex.printStackTrace();
      }

      for (int i = 0; i < 256; i++) {
        JCheckBox check = (JCheckBox) checkboxList.get(i);
        if (checkboxState[i]) {
          check.setSelected(true);
        } else {
          check.setSelected(false);
        }
      }
      sequencer.stop();
    }
  }

  public class MyClearListener implements ActionListener {
    public void actionPerformed(ActionEvent evt) {
      for (int i = 0; i < 256; i++) {
        JCheckBox check = checkboxList.get(i);
        check.setSelected(false);
      }
    }
  }

  public void makeTracks(int[] list) {
    for (int i = 0; i < 16; i++) {
      int key = list[i];
      if (key != 0) {
        track.add(makeEvent(144, 9, key, 100, i));
        track.add(makeEvent(128, 9, key, 100, i+1));
      }
    }
  }

  public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
    MidiEvent event = null;
    try {
      ShortMessage a = new ShortMessage();
      a.setMessage(comd, chan, one, two);
      event = new MidiEvent(a, tick);
    } catch(Exception e) {
      e.printStackTrace();
    }
    return event;
  }
}