 package jcue.ui;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileFilter;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import jcue.domain.AudioCue;
 import jcue.domain.DeviceManager;
 import jcue.domain.SoundDevice;
 
 /**
  *
  * @author Jaakko
  */
 public class AudioCueUI implements ActionListener, PropertyChangeListener, ChangeListener {
 
     public static JPanel lastPanel;
     
     private JLabel fileLabel;
     private JTextField fileField;
     private JButton fileButton;
     
     private JLabel lengthLabel;
     private JFormattedTextField lengthField;
     
     private JLabel inLabel, outLabel, fadeInLabel, fadeOutLabel;
     private JFormattedTextField inField, outField, fadeInField, fadeOutField;
     
     private JLabel volumeLabel, panLabel;
     private JSlider volumeSlider, panSlider;
     private JFormattedTextField volumeField, panField;
     
     private JLabel loopStartLabel, loopEndLabel, loopCountLabel;
     private JTextField loopStartField, loopEndField, loopCountField;
     private JCheckBox loopCheck;
     
     private WaveformPanel waveform;
     
     private JButton playButton, pauseButton, stopButton;
     
     private JLabel deviceLabel;
     private JComboBox deviceSelect;
     private ComboBoxModel deviceSelectModel;
     private JButton addDeviceButton;
     
     private AudioCue cue;
 
     public AudioCueUI() {
         //File field
         this.fileLabel = new JLabel("File:");
         this.fileField = new JTextField();
         this.fileField.setEditable(false);
 
         this.fileButton = new JButton("...");
         this.fileButton.setActionCommand("loadAudio");
         this.fileButton.addActionListener(this);
         //********
 
         //Length field
         this.lengthLabel = new JLabel("Length:");
         
         this.lengthField = new JFormattedTextField(new TimeFormatter());
         this.lengthField.setColumns(8);
         this.lengthField.setEditable(false);
         //*******
 
         //In and out fields
         this.inLabel = new JLabel("Start:");
         this.outLabel = new JLabel("End:");
         
         this.inField = new JFormattedTextField(new TimeFormatter());
         this.inField.setColumns(8);
         this.inField.addPropertyChangeListener(this);
         
         this.outField = new JFormattedTextField(new TimeFormatter());
         this.outField.setColumns(8);
         this.outField.addPropertyChangeListener(this);
         //**********
 
         //Volume control
         this.volumeLabel = new JLabel("Volume:");
         
         this.volumeSlider = new JSlider(0, 1000);
         this.volumeSlider.addChangeListener(this);
         
         NumberFormat volumeFormat = NumberFormat.getNumberInstance();
         volumeFormat.setMaximumFractionDigits(2);
         volumeFormat.setMinimumFractionDigits(2);
         
         this.volumeField = new JFormattedTextField(volumeFormat);
         this.volumeField.setColumns(5);
         //********
 
         //Fade in and fade out
         NumberFormat fadeInFormat = NumberFormat.getNumberInstance();
         fadeInFormat.setMaximumFractionDigits(2);
         fadeInFormat.setMinimumFractionDigits(2);
         
         this.fadeInLabel = new JLabel("Fade in:");
         this.fadeInField = new JFormattedTextField(fadeInFormat);
         this.fadeInField.setColumns(5);
         
         NumberFormat fadeOutFormat = (NumberFormat) fadeInFormat.clone();
         
         this.fadeOutLabel = new JLabel("Fade out:");
         this.fadeOutField = new JFormattedTextField(fadeOutFormat);
         this.fadeOutField.setColumns(5);
         //********
 
         //Pan control
         this.panLabel = new JLabel("Panning:");
         this.panSlider = new JSlider(-1000, 1000);
         
         NumberFormat panFormat = (NumberFormat) volumeFormat.clone();
         this.panField = new JFormattedTextField(panFormat);
         this.panField.setColumns(5);
         //********
 
         //Loop controls
         this.loopStartLabel = new JLabel("Loop start:");
         this.loopStartField = new JTextField(7);
         this.loopEndLabel = new JLabel("Loop end:");
         this.loopEndField = new JTextField(7);
         this.loopCountLabel = new JLabel("Loop count:");
         this.loopCountField = new JTextField(3);
         this.loopCheck = new JCheckBox("Loop");
         //*********
 
         //Waveform
         this.waveform = new WaveformPanel();
         //***********
 
         //Transport controls
         ImageIcon playIcon = new ImageIcon("images/editor_play.png");
         ImageIcon pauseIcon = new ImageIcon("images/editor_pause.png");
         ImageIcon stopIcon = new ImageIcon("images/editor_stop.png");
         
         this.playButton = new JButton(playIcon);
         this.playButton.setPreferredSize(new Dimension(40, 40));
         this.playButton.setActionCommand("play");
         this.playButton.addActionListener(this);
         
         this.pauseButton = new JButton(pauseIcon);
         this.pauseButton.setPreferredSize(new Dimension(40, 40));
         this.pauseButton.setActionCommand("pause");
         this.pauseButton.addActionListener(this);
         
         this.stopButton = new JButton(stopIcon);
         this.stopButton.setPreferredSize(new Dimension(40, 40));
         this.stopButton.setActionCommand("stop");
         this.stopButton.addActionListener(this);
         //**************
         
         //Adding outputs
         this.deviceLabel = new JLabel("Device:");
         this.deviceSelect = new JComboBox();
         
         ImageIcon addIcon = new ImageIcon("images/add_small.png");
         this.addDeviceButton = new JButton(addIcon);
         this.addDeviceButton.setActionCommand("addDevice");
         this.addDeviceButton.addActionListener(this);
         //*************
                 
     }
 
     public void showUI(JPanel panel) {
         panel.add(this.fileLabel);
         panel.add(this.fileField, "span 4, growx, split 2");
         panel.add(this.fileButton, "wrap");
         
         panel.add(this.lengthLabel);
         panel.add(this.lengthField, "wrap");
         
         panel.add(this.inLabel);
         panel.add(this.inField);
         panel.add(this.fadeInLabel);
         panel.add(this.fadeInField, "wrap");
         
         panel.add(this.outLabel);
         panel.add(this.outField);
         panel.add(this.fadeOutLabel);
         panel.add(this.fadeOutField, "wrap");
         
         panel.add(this.waveform, "span, grow, hmin 170, wrap");
         this.waveform.repaint();
         
         //Create a panel for laying out buttons
         JPanel transportPanel = new JPanel();
         transportPanel.setLayout(new BoxLayout(transportPanel, BoxLayout.X_AXIS));
         transportPanel.add(this.playButton);
         transportPanel.add(this.pauseButton);
         transportPanel.add(this.stopButton);
         
         panel.add(transportPanel, "span 3, wrap");
         
         panel.add(this.volumeLabel);
         panel.add(this.volumeSlider, "span 3, growx");
         panel.add(this.volumeField, "wrap");
         
         panel.add(this.panLabel);
         panel.add(this.panSlider, "span 3, growx");
         panel.add(this.panField, "wrap");
         
        ArrayList<SoundDevice> outputs = this.cue.getOutputs();
        for (SoundDevice sd : outputs) {
            panel.add(new DeviceControlPanel(this.cue, sd), "span, growx, wrap");
         }
         
        
         panel.add(this.deviceLabel);
         
         //Update device selection combo box
         DeviceManager dm = DeviceManager.getInstance();
         ArrayList<SoundDevice> enabledDevices = dm.getEnabledDevices();
         SoundDevice[] tmpArray = new SoundDevice[enabledDevices.size()];
         SoundDevice[] deviceArray = enabledDevices.toArray(tmpArray);
         
         ComboBoxModel cbm = new DefaultComboBoxModel(deviceArray);
         this.deviceSelect.setModel(cbm);
         
         panel.add(this.deviceSelect);
         panel.add(this.addDeviceButton, "wrap");
                 
         
         panel.revalidate();
     }
     
     public void showUI2(JPanel container) {
         GridBagConstraints c = new GridBagConstraints();
         c.insets = new Insets(5, 3, 5, 3);
 
         //File field
         UtilsUI.setGBC(c, 0, 4, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL);
         container.add(this.fileLabel, c);
 
         UtilsUI.setGBC(c, 1, 4, 0.5, 0, 3, 1, GridBagConstraints.HORIZONTAL);
         container.add(this.fileField, c);
 
         UtilsUI.setGBC(c, 4, 4, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL);
         container.add(this.fileButton, c);
         //************
 
         //Length field
         UtilsUI.setGBC(c, 0, 5, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL);
         container.add(this.lengthLabel, c);
 
         UtilsUI.setGBC(c, 1, 5, 0, 0, 1, 1, GridBagConstraints.NONE);
         c.anchor = GridBagConstraints.WEST;
         container.add(this.lengthField, c);
         //*********
 
         //In, out and volume
         UtilsUI.setGBC(c, 0, 6, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL);
         container.add(this.inLabel, c);
 
         UtilsUI.setGBC(c, 1, 6, 0, 0, 1, 1, GridBagConstraints.NONE);
         container.add(this.inField, c);
 
         UtilsUI.setGBC(c, 2, 6, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL);
         container.add(this.outLabel, c);
 
         UtilsUI.setGBC(c, 3, 6, 0.5, 0, 1, 1, GridBagConstraints.NONE);
         container.add(this.outField, c);
 
         UtilsUI.setGBC(c, 4, 6, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL);
         container.add(this.volumeLabel, c);
 
         UtilsUI.setGBC(c, 5, 6, 0.5, 0, 2, 1, GridBagConstraints.HORIZONTAL);
         container.add(this.volumeSlider, c);
 
         UtilsUI.setGBC(c, 7, 6, 0.5, 0, 1, 1, GridBagConstraints.NONE);
         container.add(this.volumeField, c);
         //***********
 
         //Fade in, fade out and panning
         UtilsUI.setGBC(c, 0, 7, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL);
         container.add(this.fadeInLabel, c);
 
         UtilsUI.setGBC(c, 1, 7, 0, 0, 1, 1, GridBagConstraints.NONE);
         container.add(this.fadeInField, c);
 
         UtilsUI.setGBC(c, 2, 7, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL);
         container.add(this.fadeOutLabel, c);
 
         UtilsUI.setGBC(c, 3, 7, 0, 0, 1, 1, GridBagConstraints.NONE);
         container.add(this.fadeOutField, c);
 
         UtilsUI.setGBC(c, 4, 7, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL);
         container.add(this.panLabel, c);
 
         UtilsUI.setGBC(c, 5, 7, 0.5, 0, 2, 1, GridBagConstraints.HORIZONTAL);
         container.add(this.panSlider, c);
 
         UtilsUI.setGBC(c, 7, 7, 0.5, 0, 1, 1, GridBagConstraints.NONE);
         container.add(this.panField, c);
         //*************
 
         //Looping controls
         UtilsUI.setGBC(c, 0, 8, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL);
         container.add(this.loopStartLabel, c);
 
         UtilsUI.setGBC(c, 1, 8, 0, 0, 1, 1, GridBagConstraints.NONE);
         container.add(this.loopStartField, c);
 
         UtilsUI.setGBC(c, 2, 8, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL);
         container.add(this.loopEndLabel, c);
 
         UtilsUI.setGBC(c, 3, 8, 0, 0, 1, 1, GridBagConstraints.NONE);
         container.add(this.loopEndField, c);
 
         UtilsUI.setGBC(c, 4, 8, 0, 0, 1, 1, GridBagConstraints.HORIZONTAL);
         container.add(this.loopCountLabel, c);
 
         UtilsUI.setGBC(c, 5, 8, 0, 0, 1, 1, GridBagConstraints.NONE);
         container.add(this.loopCountField, c);
 
         UtilsUI.setGBC(c, 6, 8, 0, 0, 1, 1, GridBagConstraints.NONE);
         container.add(this.loopCheck, c);
         //**********
 
         //Waveform panel and transport controls
         UtilsUI.setGBC(c, 0, 9, 1, 0.5, 8, 1, GridBagConstraints.BOTH);
         container.add(this.waveform, c);
         this.waveform.repaint();
         
         //Create a panel for laying out buttons
         JPanel transportPanel = new JPanel(new FlowLayout());
         transportPanel.add(this.playButton);
         transportPanel.add(this.pauseButton);
         transportPanel.add(this.stopButton);
         
         UtilsUI.setGBC(c, 0, 10, 0, 0, 2, 1, GridBagConstraints.NONE);
         container.add(transportPanel, c);
         //**********
     }
 
 
     public void setVolumeControlValue(double value) {
         this.volumeField.setValue((1000 * value) / 10);
         this.volumeSlider.setValue((int) (1000 * value));
     }
 
     public void setPanControlValue(double value) {
         this.panField.setValue((1000 * value) / 10);
         this.panSlider.setValue((int) (1000 * value));
     }
 
     public void setFadeInFieldValue(double value) {
         this.fadeInField.setValue(value);
     }
 
     public void setFadeOutFieldValue(double value) {
         this.fadeOutField.setValue(value);
     }
 
     public void setInFieldValue(double value) {
         this.inField.setValue(value);
     }
 
     public void setOutFieldValue(double value) {
         this.outField.setValue(value);
     }
 
     public void setWaveformData(AudioCue cue) {
         this.waveform.setCue(cue);
     }
 
     public void setFileFieldText(String text) {
         this.fileField.setText(text);
     }
 
     public void setLengthFieldValue(double value) {
         this.lengthField.setValue(value);
     }
 
     public void setCurrentCue(AudioCue cue) {
         this.cue = cue;
     }
 
     @Override
     public void actionPerformed(ActionEvent ae) {
         String command = ae.getActionCommand();
         
         if (command.equals("loadAudio")) {  //File choose button was pressed
             
             JFileChooser chooser = new JFileChooser();
             chooser.setFileFilter(new AudioFileFilter());
             int result = chooser.showOpenDialog(null);
             
             if (result == JFileChooser.APPROVE_OPTION) {
                 File file = chooser.getSelectedFile();
                 this.cue.loadAudio(file.getAbsolutePath());
                 this.cue.updateUI();
             }
         } else if (command.equals("play")) {
             this.cue.start(false);
             
             this.playButton.setSelected(true);
             this.pauseButton.setSelected(false);
         } else if (command.equals("pause")) {
             this.cue.pause();
             
             this.playButton.setSelected(false);
             this.pauseButton.setSelected(true);
         } else if (command.equals("stop")) {
             this.cue.stop();
             
             this.playButton.setSelected(false);
             this.pauseButton.setSelected(false);
         } else if (command.equals("addDevice")) {
             SoundDevice sd = (SoundDevice) this.deviceSelect.getSelectedItem();
             
             this.cue.addOutput(sd);
             this.cue.updateUI();
         }
     }
 
     @Override
     public void propertyChange(PropertyChangeEvent pce) {
         Object source = pce.getSource();
         
         if (source == this.inField) {
             if (this.inField.getValue() != null) {
                 this.cue.setInPos((Double) this.inField.getValue());
             }
         } else if (source == this.outField) {
             if (this.outField.getValue() != null) {
                 this.cue.setOutPos((Double) this.outField.getValue());
             }
         }
         
         this.waveform.repaint();
     }
 
     @Override
     public void stateChanged(ChangeEvent ce) {
         Object source = ce.getSource();
         
         if (source == this.volumeSlider) {
             int value = this.volumeSlider.getValue();
             double newVolume = value / 1000.0;
             
             this.cue.setVolume(newVolume);
             
             this.volumeField.setValue(value / 10.0);
         }
     }
 }
