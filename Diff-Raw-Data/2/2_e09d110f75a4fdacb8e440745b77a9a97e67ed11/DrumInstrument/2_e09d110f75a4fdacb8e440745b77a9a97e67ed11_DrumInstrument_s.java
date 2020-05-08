 package GUI;
 
 import messaging.SongFragment;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Tae Kim
  * Date: 5/2/13
  * Time: 12:21 AM
  * To change this template use File | Settings | File Templates.
  */
 public class DrumInstrument extends AbstractInstrument {
 
 
    private instruments.DrumMachine drums;
 
 
     private String[] keys = {"Crash", "Hi-Tom", "Kick", "Low-Tom", "Snare"};
 
     public DrumInstrument(final CardLayout cl, final JPanel mainPanel) {
         // Call Abstract Instrument's constructor
         super(cl, mainPanel);
 
         // Instantiate the instrument
         this.drums = new instruments.DrumMachine();
 
         // Add image to the image panel
         JLabel instrumentsLabel = new JLabel();
         instrumentsLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
         instrumentsLabel.setIcon(new ImageIcon("Resources/images/drumtitle.jpg"));
         titlePanel.add(instrumentsLabel);
 
 
         // Create buttons for the piano
         for (int i = 0; i < keys.length; i++) {
             final JButton key = new JButton(keys[i]);
             key.setPreferredSize(new Dimension(75, 200));
             key.setBackground(Color.WHITE);
             key.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent arg0) {
                     SongFragment fragment = new SongFragment(drums.getNoteByName(key.getText()));
                     Session.songController.play(fragment, true);
                 }
             });
             buttonPanel.add(key);
         }
 
         // add components
         this.addComponents();
     }
 }
