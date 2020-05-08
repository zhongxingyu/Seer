 
 
 import java.awt.GraphicsEnvironment;
 import java.awt.GraphicsDevice;
 import java.util.ArrayList;
 /**
  * Main
  * This class will create the application frame.
  * @author quincy
  */
 public class Main {
 
 	/**
 	 * Creates an instance of  PopUpQuiz. Puts the game in full-screen
 	 * mode.
 	 * @param args Command line arguments, which are disregarded.
 	 */
 	public static void main(String[] args) {
 		PopUpQuiz frame = new PopUpQuiz();
		
 
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		GraphicsDevice gd = ge.getDefaultScreenDevice();
 		gd.setFullScreenWindow(frame);
 		
		frame.setUndecorated(false);
		frame.setResizable(false);
 		
 		frame.setVisible(true);
 
 
 	}
 
 
 }
