 /**
  * 
  */
import java.swing.*;
 /**
  * @author Colin Mackey and David Justis 
  * As of right now, this is a shell of the
  *         stuff to come
  * 
  */
 public class barkerMain {
 
 	/**
 	 * Constructor
 	 */
 	public barkerMain() {
 
 		super();
 	}
 
 	/**
 	 * GUI initialization method
 	 */
 	public void initGui() {
 
 	}
 
 	/**
 	 * method for the logic
 	 */
 	public void logic() {
 		// name can change
 		// there's a loop in here for getting pings and storing them in a queue
 	}
 
 	/**
 	 * @param args
 	 */
	public static void main(String[] args) {
 		try {
 			// Set System L&F
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (UnsupportedLookAndFeelException e) {
 			// handle exception
 		}
 
		new SwingApplication(); // Create and show the GUI
 
 	}
 
 }
