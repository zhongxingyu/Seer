 /**
  * 
  */
 package pl.scriptease.octo.hipster;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  * @author Michał Gardeła
  * 
  */
 public class OctoHipster {
 
     /**
      * @param args
      */
     public static void main(String[] args) {
 	// TODO Auto-generated method stub
 
	JFrame frame = new JFrame("Octo-Hipster");
 	frame.setSize(320, 240);
 
 	JPanel content = (JPanel) frame.getContentPane();
 	content.setLayout(new BorderLayout());
	JLabel message = new JLabel("<html><center>Hello");
 	message.setForeground(Color.white);
 	content.add(message, BorderLayout.CENTER);
 	content.setBackground(Color.black);
 
 	frame.setLocationRelativeTo(null);
 	frame.setVisible(true);

	System.out.println("Octo-hipster is my first shared Git repository!");
     }
 }
