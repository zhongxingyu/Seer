 /**
  * 
  */
 package pl.scriptease.octo.hipster;
 
import static pl.scriptease.octo.hipster.Globals.APP_NAME;

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
 
	JFrame frame = new JFrame(APP_NAME);
 	frame.setSize(320, 240);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 	JPanel content = (JPanel) frame.getContentPane();
 	content.setLayout(new BorderLayout());

	JLabel message = new JLabel("<html><center>Octo-hipster is my first shared Git repository!");
	message.setSize(content.getWidth(), content.getHeight());
 	message.setForeground(Color.white);

 	content.add(message, BorderLayout.CENTER);
 	content.setBackground(Color.black);
 
 	frame.setLocationRelativeTo(null);
 	frame.setVisible(true);
     }
 }
