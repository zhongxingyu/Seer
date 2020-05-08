 package RegionBuilder;
 
 import ActiveSpace.Coord;
import ProcessingKinect.KinectSensor;
 
 /**
  * main class
  * 		process command line arguments
  * 		instantiate the regionbuilder GUI
  * 
  * @author markk
  */
 public class RegionBuilder {
 	
 	private MainScreen gui;		// reference to the gui widgetry
 	private KinectViewer view;	// reference to the Kinect viewer window
 
 	private static String usage[] = {
 		"RegionBuilder [switches] [regions-file]",
 		"    switches:",
 		"        --regions=regions-file",
 		"        --debug=#"
 	};
 	
 	public static void main(String args[]) {
 		String regionFile = null;
 		int debugLevel = 1;		// major file events only
 		
 		// process the command line arguments (why isn't there a standard for this?)
 		for( int i = 0; i < args.length; i++ ) {
 			if (args[i].contains("regions=")) {
 				regionFile = args[i].substring(args[i].indexOf('=') + 1);
 			} else if (args[i].contains("debug=")) {
 				debugLevel = Integer.parseInt(args[i].substring(args[i].indexOf('=') + 1));
 			} else if (args[i].contains("help") || args[i].contains("?")) {
 				usage();
 				return;
 			} else if (regionFile == null) {
 				regionFile = args[i];
 			} else {
 				usage();
 				return;
 			}
 		}
 		
 		// instantiate an app and GUI and kick off the main loop
 		RegionBuilder app = new RegionBuilder();
 		app.view = new KinectViewer();
 		app.view.debug(debugLevel);
 		app.gui = new MainScreen( regionFile, debugLevel );
 		app.run();
 	}
 	
 	/**
 	 * main loop
 	 */
 	public void run() {
 		while( !gui.finished) {
 			view.update();	// re-read the kinect
 			
 			// check for position selections 
 			Coord c = view.lastClick();
 			if (c != null)
 				gui.setCoord(c);
 			
 			view.repaint();	// update the display
 		}
 		System.exit(0);
 	}
 
 	/**
 	 * print out a usage message
 	 */
 	private static void usage() {
 		for( int i = 0; i < usage.length; i++ )
 			System.out.println(usage[i]);
 	}
 }
