 package jcue.ui;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListDataEvent;
 import jcue.domain.AbstractCue;
 import jcue.domain.CueState;
 import jcue.domain.DeviceManager;
 import jcue.domain.SoundDevice;
 import jcue.domain.audiocue.AudioCue;
 
 /**
  *
  * @author Jaakko
  */
 public class AudioCueUI extends AbstractCueUI implements ActionListener, 
         PropertyChangeListener, ChangeListener {
 
     private JLabel fileLabel;
     private JTextField fileField;
     private JButton fileButton;
     
     private JLabel lengthLabel;
     private JFormattedTextField lengthField;
     
     private JLabel inLabel, outLabel, fadeInLabel, fadeOutLabel;
     private JFormattedTextField inField, outField, fadeInField, fadeOutField;
     
     private JLabel volumeLabel;
     private JSlider volumeSlider;
     private JFormattedTextField volumeField;
     
     private JLabel loopStartLabel, loopEndLabel, loopCountLabel;
     private JTextField loopStartField, loopEndField, loopCountField;
     private JCheckBox loopCheck;
     
     private WaveformPanel waveform;
     
     private JButton playButton, pauseButton, stopButton;
     
     private JFormattedTextField posField;
     
     private JLabel deviceLabel;
     private JComboBox deviceSelect;
     private ComboBoxModel deviceSelectModel;
     private JButton addDeviceButton;
     
     private JPanel devicesPanel;
     
     private AudioCue cue;
 
     public AudioCueUI() {
         super();
         
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
         this.waveform = new WaveformPanel(this);
         this.waveform.addPropertyChangeListener(this);
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
         
         //Position field
         this.posField = new JFormattedTextField(new TimeFormatter());
         
         this.posField.setColumns(8);
         this.posField.setEditable(false);
         //************
         
         //Adding outputs
         this.deviceLabel = new JLabel("Device:");
         
         //Get array of available devices
         DeviceManager dm = DeviceManager.getInstance();
         ArrayList<SoundDevice> enabledDevices = dm.getEnabledDevices();
         SoundDevice[] tmpArray = new SoundDevice[enabledDevices.size()];
         SoundDevice[] deviceArray = enabledDevices.toArray(tmpArray);
         
         this.deviceSelectModel = new DefaultComboBoxModel(deviceArray);
         
         this.deviceSelect = new JComboBox(this.deviceSelectModel);
         
         ImageIcon addIcon = new ImageIcon("images/add_small.png");
         this.addDeviceButton = new JButton(addIcon);
         this.addDeviceButton.setActionCommand("addDevice");
         this.addDeviceButton.addActionListener(this);
         //*************
         
         this.devicesPanel = new JPanel();
         this.devicesPanel.setLayout(new BoxLayout(devicesPanel, BoxLayout.Y_AXIS));
         
         this.addComponents();
     }
 
     private void addComponents() {
         this.add(this.fileLabel, "align label");
         this.add(this.fileField, "span 4, growx, split 2");
         this.add(this.fileButton, "wrap");
         
         this.add(this.lengthLabel, "align label");
         this.add(this.lengthField, "wrap");
         
         this.add(this.inLabel, "align label");
         this.add(this.inField);
         this.add(this.fadeInLabel, "align label");
         this.add(this.fadeInField, "wrap");
         
         this.add(this.outLabel, "align label");
         this.add(this.outField);
         this.add(this.fadeOutLabel, "align label");
         this.add(this.fadeOutField, "wrap");
         
         this.add(this.waveform, "span, grow,  wrap");
         this.waveform.repaint();
         
         //Create a panel for laying out buttons
         JPanel transportPanel = new JPanel();
         transportPanel.setLayout(new BoxLayout(transportPanel, BoxLayout.X_AXIS));
         transportPanel.add(this.playButton, "split 3");
         transportPanel.add(this.pauseButton);
         transportPanel.add(this.stopButton);
         
         this.add(transportPanel, "span, growx, split 2");
         
         this.add(this.posField, "align right, wrap");
         
         this.add(this.volumeLabel, "align label");
         this.add(this.volumeSlider, "span 3, growx");
         this.add(this.volumeField, "wrap");
         
         this.add(this.devicesPanel, "growx, span, wrap");
         
         this.add(this.deviceLabel, "align label");
         
         this.add(this.deviceSelect, "span, split 2");
         this.add(this.addDeviceButton, "wrap");
     }
     
     @Override
     public void update() {
         super.update();
         
         if (this.cue != null) {
             setVolumeControlValue(this.cue.getAudio().getMasterVolume());
         
             setFadeInFieldValue(this.cue.getFadeIn());
             setFadeOutFieldValue(this.cue.getFadeOut());
 
             setInFieldValue(this.cue.getInPos());
             setOutFieldValue(this.cue.getOutPos());
 
             setWaveformData(this.cue);
 
             setFileFieldText(this.cue.getAudio().getFilePath());
 
             setLengthFieldValue(this.cue.getAudio().getLength());
             
             this.updateDevicesPanel();
         }
         
         this.waveform.repaint();
     }
 
     private void setVolumeControlValue(double value) {
         this.volumeField.setValue((1000 * value) / 10);
         this.volumeSlider.setValue((int) (1000 * value));
     }
 
     private void setFadeInFieldValue(double value) {
         this.fadeInField.setValue(value);
     }
 
     private void setFadeOutFieldValue(double value) {
         this.fadeOutField.setValue(value);
     }
 
     private void setInFieldValue(double value) {
         this.inField.setValue(value);
     }
 
     private void setOutFieldValue(double value) {
         this.outField.setValue(value);
     }
 
     private void setWaveformData(AudioCue cue) {
         this.waveform.setCue(cue);
     }
 
     private void setFileFieldText(String text) {
         this.fileField.setText(text);
     }
 
     private void setLengthFieldValue(double value) {
         this.lengthField.setValue(value);
     }
     
     public void setPositionFieldValue(double value) {
         this.posField.setValue(value);
     }
 
     @Override
     public void setCurrentCue(AbstractCue cue) {
         super.setCurrentCue(cue);
         
         this.cue = (AudioCue) cue;
         this.update();
         this.updateDevicesPanel();
     }
     
     private void updateDevicesPanel() {
         //Show output devices
         this.devicesPanel.removeAll();
         
         ArrayList<SoundDevice> outputs = this.cue.getOutputs();
         for (SoundDevice sd : outputs) {
             DeviceControlPanel dcp = new DeviceControlPanel(this.cue, sd);
             
             this.devicesPanel.add(dcp);
         }
         
         this.revalidate();
         //*****
     }
 
     @Override
     public void actionPerformed(ActionEvent ae) {
         super.actionPerformed(ae);
         
         String command = ae.getActionCommand();
         
         if (command.equals("loadAudio")) {  //File choose button was pressed
             
             JFileChooser chooser = new JFileChooser();
             chooser.setFileFilter(new AudioFileFilter());
             int result = chooser.showOpenDialog(null);
             
             if (result == JFileChooser.APPROVE_OPTION) {
                 File file = chooser.getSelectedFile();
                 this.cue.loadAudio(file.getAbsolutePath());
                 //this.cue.updateUI();
             }
         } else if (command.equals("play")) {
             this.cue.start(false);
             
             this.playButton.setSelected(true);
             this.pauseButton.setSelected(false);
             
             this.waveform.start();
         } else if (command.equals("pause")) {
             this.cue.pause();
             
             this.playButton.setSelected(false);
             this.pauseButton.setSelected(true);
             
             this.waveform.stop();
         } else if (command.equals("stop")) {
             this.cue.stop();
             
             this.playButton.setSelected(false);
             this.pauseButton.setSelected(false);
             
             this.waveform.stop();
             this.waveform.repaint();
            
            this.posField.setValue(this.cue.getInPos());
         } else if (command.equals("addDevice")) {
             SoundDevice sd = (SoundDevice) this.deviceSelect.getSelectedItem();
             
             this.cue.addOutput(sd);
             this.updateDevicesPanel();
         }
     }
 
     @Override
     public void propertyChange(PropertyChangeEvent pce) {
         super.propertyChange(pce);
         
         Object source = pce.getSource();
         
         if (source == this.inField) {
             if (this.inField.getValue() != null) {
                 this.cue.setInPos((Double) this.inField.getValue());
             }
         } else if (source == this.outField) {
             if (this.outField.getValue() != null) {
                 this.cue.setOutPos((Double) this.outField.getValue());
             }
         } else if (source == this.waveform) {
             String propertyName = pce.getPropertyName();
             
             if (propertyName.equals("inPos")) {
                 this.inField.setValue(pce.getNewValue());
             } else if (propertyName.equals("outPos")) {
                 this.outField.setValue(pce.getNewValue());
             } else if (propertyName.equals("ctiPos")) {
                 this.posField.setValue(pce.getNewValue());
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
 
     @Override
     public void contentsChanged(ListDataEvent lde) {
         super.contentsChanged(lde);
         
         this.update();
     }
 }
