 /**
  * 
  */
 package no.whg.mini;
 
 import java.awt.BorderLayout;
 
 import javax.swing.*;
 
 /**
  * The Main class, connects all the "dots" together to become a full program, that
  * 	lets the users create a GUI and save that to file
  * @author Fredrik, Peer
  *
  */
 public class Main extends JFrame {
 	public Main() {
 		
 		// override JFrame constructor
 		super(Messages.getString("Main.title")); //$NON-NLS-1$
 		// tell application to close on exit, instead of hide on exit
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		// set the default size
 		setSize(400, 400);
 		// set the initial position of window to center
 		setLocationRelativeTo(null);
 		// show the frame
 		setVisible(true);
 		
		setJMenuBar(new Menubar());
		add(new Toolbar(), BorderLayout.NORTH);
 		// set the look and feel of the program to resemble users OS, if possible
 		try {
 			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
 		} catch (Exception e) {
 			System.out.println(Messages.getString("Main.catchError") + e); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * @param args The variables that are sent with the "java" command
 	 */
 	public static void main(String[] args) {
 		// set the locale
 		//Messages.setLocale(args[0], args[1]);
 		new Main();
 		
 	}
 
 }
