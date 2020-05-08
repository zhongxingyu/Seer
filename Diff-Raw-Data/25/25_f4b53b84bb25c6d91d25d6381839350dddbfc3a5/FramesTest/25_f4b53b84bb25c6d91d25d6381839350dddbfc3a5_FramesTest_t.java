 package mikera.gui;
 
 import javax.swing.JButton;
import javax.swing.JLabel;
 
 public class FramesTest {
 	
 	public static void main(String... args) {
 		JButton b=new JButton("Foobar");
		Frames.display(b, "Test Frame");
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		JLabel l=new JLabel("Bazbux");
		Frames.display(l, "Test Frame");

 	}
 
 }
