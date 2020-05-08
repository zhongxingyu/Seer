 package ch.sfdr.fractals;
 
import javax.swing.UIManager;

 import ch.sfdr.fractals.gui.MainFrame;
 
 /**
  * Fractals main class
  * @author S.Freihofer
  */
 public class Main
 {
 	public static void main(String[] args)
 	{
		try {
			// the java LAF is just ugly...
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Error setting system LookAndFeel: " + e);
		}
 		MainFrame m = new MainFrame();
 		m.setVisible(true);
 	}
 }
