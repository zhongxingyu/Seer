 package ProcessingKinect;
 /**
  * Top level module for processing version of the ActiveSpace applet
  */
 
 import ActiveSpace.Actor;
 import ActiveSpace.Coord;
 import ActiveSpace.Space;
 import processing.core.*;
 
 @SuppressWarnings("serial")
 public class ProcessingKinect extends PApplet {
 	private KinectSensor k;		// kinect session
 	private Space s;			// space in which we are running
 	private Actor actors[];		// the known actors
 	private int maxActors;		// upper limit on number of actors
 	private	int debugLevel;		// how noisy we want to be
 
 	private int testsRequested;	// number of test passes requested
 	private int testsRun;		// number of test passes actually run
 
 	// pseudo-tunable constants
 	private static final int MAX_ACTORS = 3;	// maximum number of actors we support
 
 
 	/**
 	 * get configuration information and establish session
 	 */
 	public void setup() {
 
 		background(0);	// FIXME - default display window
 		
 		// process our parameters
 		String regions = getParameter("regions");
 		if (regions == null)
 			regions = "Regions.xml";
 		String rules = getParameter("rules");
 		if (rules == null) 
 			rules = "Rules.xml";
 		
 		// FIXME figure out where images, sound and prose come from
 		String images = getParameter("images");
 		if (images == null)
 			images = "images";
 		String sounds = getParameter("sounds");
 		if (sounds == null)
 			sounds = "images";
 		String prose = getParameter("prose");
 		if (prose == null)
 			prose = "images";
 
 		// if no base is specified all files are packaged with the applet
 		String prefix = getParameter("base");
 		if (prefix != null) {
 			regions = prefix + "/" + regions;
 			rules = prefix + "/" + rules;
 			images = prefix + "/" + images;	// FIXME figure out image files
 			sounds = prefix + "/" + sounds;	// FIXME figure out sound files
 			prose = prefix + "/" + prose;	// FIXME figure out text files
 		}
 
 		maxActors = MAX_ACTORS;
 		// see if this is real, or just region/rule testing
 		String p = getParameter("test");
 		if (p != null) {
 			testsRequested = Integer.parseInt(p);
 			testsRun = 0;
 			if (testsRequested > maxActors)
 				maxActors = testsRequested;
 			k = null;		// we won't be using the kinect
 		} else {
 			// establish a Kinect session
 			k = new KinectSensor((PApplet)this);
 		}
 
 		// create and initialize the space
 		s = new Space();
 		p = getParameter("debug");
		debugLevel = (p == null) ? 0 : Integer.parseInt(p);
		s.debug(debugLevel);
 		try {
 			s.readRegions(regions);
 			s.readRules(rules);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// instantiate some actors (with no current positions)
 		actors = new Actor[maxActors];
 		for (int i = 0; i < maxActors; i++ ) {
 			actors[i] = new Actor("actor-" + i, null);
 		}
 	}
 
 	/**
 	 * regularly called update method
 	 */
 	public void draw() {
 		
 		/*
 		 * In test mode we progressively move a sequence of actors
 		 * through all of the regions in the space, trying to hit
 		 * all of the (stateless) rules.   We work our way through
 		 * the test Actors, calling the space.test method on each 
 		 * until it reports that all have done the whole walk. 
 		 */
 		if (testsRequested > testsRun) {
 			Actor a = actors[testsRun];
 			if (s.test(a) == false) {
 				testsRun++;
 				a.lastPosition( null );
 			}
 			return;
 		}	
 
 		// how many players is the kinect tracking?
 		int n = (k == null) ? 0 : k.getNumberOfPlayers();
		if (debugLevel > 2)
			System.out.println("Kinect returns " + n + " players");
 		for (int i = 0; i < n; i++) {	// for each
 			Coord c = k.getPlayerLocation();	// get current recorded location
			if (c == null) {
				if (debugLevel > 2)
					System.out.println("Player " + n + " has no position");
			} else if (i < maxActors) {
 				Actor a = actors[i];			// FIXME binding of players to actors
 				if (a.lastPosition() == null && debugLevel > 1)
 					System.out.println("Actor " + a + " entered at " + c);
				else if (debugLevel > 1)
					System.out.println("    Actor " + a + " at " + c);
 				
 				s.processPosition(a, c);	// process the new position
 				a.lastPosition(c);			// update the known position
			}
 		}
 		
 		// reset any actors no longer known to the kinect
 		for (int i = n; i < maxActors; i++ ) {
 			Actor a = actors[i];
 			if (a.lastPosition() != null) {
 				a.lastPosition(null);
 				if (debugLevel > 1)
 					System.out.println("Actor " + a + " has disappeared");
 			}
 		}
 		
 		// FIXME - this goes completely away as soon as we have real actions
 		stroke(255);
 		if (mousePressed) {
 			line(mouseX, mouseY, pmouseX, pmouseY);
 		}
 	}
 }
