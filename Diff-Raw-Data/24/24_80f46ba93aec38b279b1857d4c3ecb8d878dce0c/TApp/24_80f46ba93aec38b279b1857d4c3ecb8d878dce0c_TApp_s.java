 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 
 /** Our entry point for the MBTA Application */
 class TApp extends JFrame {
 
 	/** Default constructor */
 	TApp() {
    	// Create a new map panel and add it to our content pane
    	TMapPanel mapPanel = new TMapPanel();
    	getContentPane().add(mapPanel);
 
    	// Set up our frame...
 
    	// Stop the application when the window is closed
    	setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
 
    	// Ignore repaint since we will handle that in our map panel ourselves
    	setIgnoreRepaint( true );
 
    	pack();
    	setVisible( true );
 	}
 
 	/** Our main function */
 	public static void main( String strArgs[] ) {
 		// Start our listener
 		TDataListener.start();
 
 		// Simply create our JFrame
 		new TApp();
 	}
 
 }
