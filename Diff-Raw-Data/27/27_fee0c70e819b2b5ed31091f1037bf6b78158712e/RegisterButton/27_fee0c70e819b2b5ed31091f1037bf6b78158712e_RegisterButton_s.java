 package xeon.cm.gui.search;
 
 import xeon.cm.gui.factory.Factory;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * User: xeon
  * Date: 3/16/13
  * Time: 8:47 AM
  */
 public class RegisterButton extends JButton implements ActionListener {
 
    private Factory factory;
 
     public RegisterButton(Factory factory) {
         super("Register");
         this.factory = factory;
         addActionListener(this);
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         JFrame registerFrame = factory.createRegisterFrame();
         registerFrame.setVisible(true);
     }
 }
