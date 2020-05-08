 package dab.gui.auxpanels;
 
 import java.awt.Color;
 import java.awt.Component;
import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 
 public abstract class ControlButton extends JPanel {
 
     JToggleButton underlyingButton;
     JLabel label;
     private Icon activePressed, brokenIcon, disabledIcon;
     protected boolean softFailed = false;
 
     public ControlButton(String labelText) {
         try {
             activePressed = new ImageIcon(ImageIO.read(ControlButton.class.getResourceAsStream("active.png")));
             disabledIcon = new ImageIcon(ImageIO.read(ControlButton.class.getResourceAsStream("disabled.png")));
             brokenIcon = new ImageIcon(ImageIO.read(ControlButton.class.getResourceAsStream("broken.png")));
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         
         
         setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         underlyingButton = new JToggleButton();
         underlyingButton.setAlignmentX(Component.CENTER_ALIGNMENT);
         underlyingButton.setContentAreaFilled(false);
         underlyingButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 if (!softFailed)
                     onClick();
             }
         });
         
         label = new JLabel(labelText);
        label.setFont(new Font("Bookman Old Style", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
         label.setAlignmentX(Component.CENTER_ALIGNMENT);
         
 
         setFocusable(false);
         add(label);
         add(underlyingButton);
         setOpaque(false);
     }
 
     private void changeState(Icon icon, boolean enabled, boolean selected) {
         underlyingButton.setIcon(icon);
         underlyingButton.setEnabled(enabled);
 
         underlyingButton.setSelected(selected);
     }
 
     protected void setFailed() {
         changeState(brokenIcon, false, false);
     }
 
     protected void setStatus(boolean status) {
         if (status == true) {
             changeState(activePressed, true, true);
         } else {
             changeState(disabledIcon, true, false);
         }
     }
     
     public void setSoftFailed() {
         softFailed = true;
         setFailed();
     }
 
     protected abstract void onClick();
 
     public void update() {
         softFailed = false;
     }
 }
