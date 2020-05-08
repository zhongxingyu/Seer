 package jcue.ui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.text.NumberFormat;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import jcue.domain.SoundDevice;
 import jcue.domain.audiocue.AudioCue;
 import net.miginfocom.swing.MigLayout;
 
 /**
  *
  * @author Jaakko
  */
 public class DeviceControlPanel extends JPanel implements ChangeListener, 
         ItemListener, ActionListener {
     
     private JLabel volumeLabel, panLabel;
     
     private JSlider volumeSlider, panSlider;
     private JFormattedTextField volumeField, panField;
     
     private JCheckBox muteCheck;
     
     private JButton removeButton;
     
     private SoundDevice targetDevice;
     private AudioCue targetCue;
     
     private static final ImageIcon removeIcon = new ImageIcon("Images/remove_small.png");
 
     public DeviceControlPanel(AudioCue targetCue, SoundDevice targetDevice) {
         super(new MigLayout("fillx"));
         super.setBorder(BorderFactory.createTitledBorder(targetDevice.getName()));
         
         this.targetCue = targetCue;
         this.targetDevice = targetDevice;
         
         this.volumeLabel = new JLabel("Volume:");
         this.volumeSlider = new JSlider(0, 1000);
         this.volumeSlider.setValue((int) (targetCue.getDeviceVolume(targetDevice) * 1000));
         this.volumeSlider.addChangeListener(this);
         
         NumberFormat volFormat = NumberFormat.getNumberInstance();
         volFormat.setMinimumFractionDigits(2);
         volFormat.setMaximumFractionDigits(2);
         
         this.volumeField = new JFormattedTextField(volFormat);
         this.volumeField.setColumns(5);
         this.volumeField.setValue(targetCue.getDeviceVolume(targetDevice) * 100.0);
         
         this.panLabel = new JLabel("Panning:");
         this.panSlider = new JSlider(-1000, 1000);
         this.panSlider.setValue((int) (targetCue.getAudio().getDevicePan(targetDevice) * 1000.0));
         this.panSlider.addChangeListener(this);
         
         NumberFormat panFormat = (NumberFormat) volFormat.clone();
         
         this.panField = new JFormattedTextField(panFormat);
         this.panField.setColumns(5);
         this.panField.setValue(targetCue.getAudio().getDevicePan(targetDevice) * 100.0);
         
         this.muteCheck = new JCheckBox("Mute");
         this.muteCheck.addItemListener(this);
        this.muteCheck.setSelected(targetCue.getAudio().isMuted(targetDevice));
         
         this.removeButton = new JButton(removeIcon);
         this.removeButton.addActionListener(this);
         
         addComponents();
     }
     
     private void addComponents() {
         //this.add(this.deviceLabel);
         
         this.add(this.muteCheck);
         
         this.add(this.volumeLabel);
         this.add(this.volumeSlider, "span, growx, split 2");
         this.add(this.volumeField, "wrap");
         
         this.add(this.removeButton);
         
         this.add(this.panLabel);
         this.add(this.panSlider, "span, growx, split 2");
         this.add(this.panField);
     }
     
     @Override
     public void stateChanged(ChangeEvent ce) {
         Object source = ce.getSource();
         
         if (source == this.volumeSlider) {
             int value = this.volumeSlider.getValue();
             double newVolume = value / 1000.0;
             
             this.targetCue.setDeviceVolume(this.targetDevice, newVolume);
             
             this.volumeField.setValue(value / 10.0);
         } else if (source == this.panSlider) {
             int value = this.panSlider.getValue();
             double newPan = value / 1000.0;
             
             this.targetCue.getAudio().setDevicePan(newPan, this.targetDevice);
             
             this.panField.setValue(value / 10.0);
         }
     }
 
     @Override
     public void itemStateChanged(ItemEvent ie) {
         Object source = ie.getSource();
         boolean muted = false;
         
         if (source == this.muteCheck) {
             if (ie.getStateChange() == ItemEvent.SELECTED) {
                 muted = true;
                 this.targetCue.getAudio().muteOutput(this.targetDevice);
             } else if (ie.getStateChange() == ItemEvent.DESELECTED) {
                 muted = false;
                 this.targetCue.getAudio().unmuteOutput(this.targetDevice);
             }
         }
         
         this.volumeLabel.setEnabled(!muted);
         this.volumeSlider.setEnabled(!muted);
         this.volumeField.setEnabled(!muted);
         
         this.panLabel.setEnabled(!muted);
         this.panSlider.setEnabled(!muted);
         this.panField.setEnabled(!muted);
     }
 
     @Override
     public void actionPerformed(ActionEvent ae) {
         Object source = ae.getSource();
         
         if (source == this.removeButton) {
             boolean removeOutput = this.targetCue.removeOutput(this.targetDevice);
             
             //Last output cannot be removed
             if (!removeOutput) {
                JOptionPane.showMessageDialog(this, "Cannot delete output.\nAudio cues must have at least one output.", "Error", JOptionPane.ERROR_MESSAGE);
             }
         }
     }
     
 }
