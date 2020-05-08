 package kinect;
 
 import java.util.ArrayList;
 
 import simulation.Boid;
 import simulation.Set;
 import simulation.Sim;
 import SimpleOpenNI.SimpleOpenNI;
 
 
 public class Kinect {
 
 	public static final int MOTION_DETECTION = 1;
 	
 	public static final int DEPTH_MIN = 0;
 	public static final int DEPTH_MAX = 3000;
 
 	public static int COLOR_OFFSET;
 	public static int NUM_COLORS;
 
 	public static final int[][] SPECTRUM = { { 255, 255, 255 },
 			{ 150, 253, 253 }, { 50, 50, 253 }, { 253, 50, 253 }, { 253, 50, 50 },
 			{ 253, 253, 50 }, { 50, 253, 50 } };
 
 	public SimpleOpenNI context; // SimpleOpenNI handle
 
 	public int[] depthMap; // last frame
 	public int[] diffMap; // difference btw last frame and scene
 	public int[] staticScene; // avgs of each pixel from sample. picture of the
 								// empty scene
 	public boolean[] pointCloud; // if KINECT_AffectsSim, pixels are true if object present
 
 	
 	public KinectConfig config; // handle for KinectConfig, if in Setup mode
 	public int[] goodPixels; // pixels to use in simulation (with or without
 								// filters applied)
 	public boolean filter = true; // filter out pixels w/ high stddev
 	public float filterThreshold = Set.KINECT_DefaultFilter; // filters out pixels w/ a stddev > threshold;
 	public int range = -4400;
 	public int rangeSize = 4000;
 	public float pointCloudRepulsionMulti = 0.2f;//900*(Set.KINECT_SampleInterval*Set.KINECT_SampleInterval)/(640*480);
 
 	public int[] mapKinectToSim_Col = new int[640*480]; // Each PIXEL of Kinect depth image corresponds to a col of Sim screen
 	public int[] mapKinectToSim_Row = new int[640*480]; // Each PIXEL        "         "          "          row      "
 	
 	public int[] mapDepthToColor;
 
 	public RunningStat[] stats;
 	
 	public int alphaChannel = 200;
 
 	
 	public Kinect(Sim simul, int mode) {
 		// If we'll need a config, create one
 		if (Set.KINECT_SetupMode) {
 			config = new KinectConfig(this);
 		}
 
 		// If we have to render, register our colors
 		if (Set.KINECT_INIT_Render || Set.KINECT_SetupMode ) {
 			NUM_COLORS = (SPECTRUM.length - 1) * 256 + 1;
 			COLOR_OFFSET = simul.registerColors(createColors());
 			mapDepthToColor = createDepthToColorMap();
 		}
 
 		// generate lookup arrays mapKinectToSim_Col and mapKinectToSim_Row
 		createKinectToSimMap();
 		
 		// generate lookup arrays 
 
 		// SimpleOpenNI handle
 		context = new SimpleOpenNI(simul, SimpleOpenNI.RUN_MODE_MULTI_THREADED);
 
 		// enable depthMap generation
 		context.enableDepth();
 
 		// initialize the static scene
 		staticScene = new int[context.depthMap().length];
 	}
 
 	public void init() {
 
 		System.out.println("calibrating kinect...");
 
 		// Fetch data once
 		context.update();
 		depthMap = context.depthMap();
 		diffMap = new int[depthMap.length];
 		if( Set.KINECT_INIT_AffectsSim ) {
 			pointCloud = new boolean[depthMap.length];
 		}
 
 		// Get ready for the rest
 		int perc10 = Set.KINECT_CalibrationLevel / 10;
 		RunningStat[] stats = new RunningStat[depthMap.length];
 		for (int j = 0; j < depthMap.length; j++) {
 			stats[j] = new RunningStat();
 		}
 
 		// Take the samples and process
 		for (int i = 0; i < Set.KINECT_CalibrationLevel; i++) {
 			if (i % perc10 == 0) {
 				System.out.printf("%d", i / perc10);
 			}
 
 			// Fetch data
 			context.update();
 			depthMap = context.depthMap();
 
 			// Add it up
 			for (int j = 0; j < depthMap.length; j++) {
 				stats[j].addSample(depthMap[j]);
 			}
 
 		}
 
 		// If we're in config mode, save the data
 		if (config != null) {
 			config.stats = stats;
 		}
 
 		// Save the scene
 		for (int j = 0; j < depthMap.length; j++) {
 			staticScene[j] = (int) stats[j].getMean();
 		}
 
 		refreshGoodPixels(stats);
 
 		System.out.println("\nCalibration DONE!");
 
 	}
 	
 	public void initFancy(int i) {
 		
 		int perc10 = 0;
 		
 		// Setup
 		if( i==0 ) {
 			System.out.println("calibrating kinect...");
 
 			// Fetch data once
 			context.update();
 			depthMap = context.depthMap();
 			diffMap = new int[depthMap.length];
 			if( Set.KINECT_INIT_AffectsSim ) {
 				pointCloud = new boolean[depthMap.length];
 			}
 
 			// Get ready for the rest
 			perc10 = Set.KINECT_CalibrationLevel / 10;
 			stats = new RunningStat[depthMap.length];
 			for (int j = 0; j < depthMap.length; j++) {
 				stats[j] = new RunningStat();
 			}
 		}
 		
 		// Take the samples and process
 		if (i % perc10 == 0) {
 			System.out.printf("%d", i / perc10);
 		}
 
 		// Fetch data
 		context.update();
 		depthMap = context.depthMap();
 
 		// Add it up
 		for (int j = 0; j < depthMap.length; j++) {
 			stats[j].addSample(depthMap[j]);
 		}
 
 
 		if(i == Set.KINECT_CalibrationLevel) {
 			// If we're in config mode, save the data
 			if (config != null) {
 				config.stats = stats;
 			}
 
 			// Save the scene
 			for (int j = 0; j < depthMap.length; j++) {
 				staticScene[j] = (int) stats[j].getMean();
 			}
 			
 			refreshGoodPixels(stats);
 			stats = null; // release the stats
 			
 			System.out.println("\nCalibration DONE!");
 		}
 	
 	}
 
 	
 	public void update() {
 		// update the cam;
 		context.update();
 		depthMap = context.depthMap();
 		for (int i = 0; i < goodPixels.length; i++) {
 			diffMap[goodPixels[i]] = depthMap[goodPixels[i]]
 					- staticScene[goodPixels[i]];
 			if( Set.kinect_AffectsSim ) {
 				if( diffMap[goodPixels[i]] > range &&
 					diffMap[goodPixels[i]] < range+rangeSize ) {
 					
 					pointCloud[goodPixels[i]] = true;
 				
 				} else {
 					pointCloud[goodPixels[i]] = false;
 				}
 			}
 		}
 
 	}
 
 	protected void refreshGoodPixels(RunningStat[] stats) {
 		// protected so KinectConfig can call
 
 		ArrayList<Integer> stack = new ArrayList<Integer>();
 
 		for( int i=0; i<depthMap.length; i++ ) {
 			
 			// If we are sampling every pixel, count it. OR if the row%interval and col%inteval ==0, count it
 			if( Set.KINECT_SampleInterval == 1 ||
 				((i/640)%Set.KINECT_SampleInterval == 0 && (i%640)%Set.KINECT_SampleInterval == 0) ) {
 			// if we're filtering, filter
 			if( !filter || stats[i].getStdDev() <= filterThreshold ) {
 			// always take out offscreen pixels
 			if( mapKinectToSim_Col[i] >= 0 && mapKinectToSim_Col[i] <= Set.SCREEN_Width &&
 				mapKinectToSim_Row[i] >= 0 && mapKinectToSim_Row[i] <= Set.SCREEN_Height ) {
 				stack.add(i);
 			}
 			}
 			}
 		}
 
 		// Transfer stack into new array
 		goodPixels = new int[stack.size()];
 		Object[] Ostack = stack.toArray();
 		for (int i = 0; i < goodPixels.length; i++) {
 			goodPixels[i] = (Integer) Ostack[i];
 		}
 		
 	}
 
 	/*
 	 * Creates the map of the Kinect's depthMap pixels to the Simulation's
 	 * pixels and populates mapToSimR (with the row indices) and mapToSimC (with
 	 * the column indices)
 	 */
 	private void createKinectToSimMap() {
 
 		if (Set.KINECT_Coord[1][0] - Set.KINECT_Coord[0][0] < 640
 				|| Set.KINECT_Coord[1][1] - Set.KINECT_Coord[0][1] < 480) {
 			System.out.printf("warning: unsupported size. compressing depth image to %dx%d"
 					+ " (orig. 640x480)",
 					Set.KINECT_Coord[1][0] - Set.KINECT_Coord[0][0],
 					Set.KINECT_Coord[1][1] - Set.KINECT_Coord[0][1]);
 		}
 
 		for (int i = 0; i < 640*480; i++) {
 			mapKinectToSim_Col[i] = (int) Boid.redoRange(i%640, Set.KINECT_Coord[0][0],
 					Set.KINECT_Coord[1][0], 0, 640);
 		}
 
		for (int i = 0; i < 480; i++) {
 			mapKinectToSim_Row[i] = (int) Boid.redoRange(i/640, Set.KINECT_Coord[0][1],
 					Set.KINECT_Coord[1][1], 0, 480);
 		}
 	}
 	
 	
 	private int[] createDepthToColorMap() {
 		
 		int[] map = new int[DEPTH_MAX-DEPTH_MIN+1];
 		
 		for( int i=DEPTH_MIN; i<=DEPTH_MAX; i++) {
 			map[i] = (int) Boid.redoRange( i, 0, NUM_COLORS, DEPTH_MIN, DEPTH_MAX+2 );
 		}
 		
 		return map;
 		
 	}
 
 	private int[][] createColors() {
 
 		int[][] colors = new int[NUM_COLORS][4];
 
 		int i, j, k = 0;
 		for (i = 0; i < SPECTRUM.length - 1; i++) {
 			// colors[i] = new int[4];
 			for (j = 0; j < 256; j++) {
 				colors[k][0] = (int) Boid.redoRange(j, SPECTRUM[i][0],
 						SPECTRUM[i + 1][0], 0, 255);
 				colors[k][1] = (int) Boid.redoRange(j, SPECTRUM[i][1],
 						SPECTRUM[i + 1][1], 0, 255);
 				colors[k][2] = (int) Boid.redoRange(j, SPECTRUM[i][2],
 						SPECTRUM[i + 1][2], 0, 255);
 				colors[k][3] = alphaChannel;
 
 				k++;
 			}
 		}
 		return colors;
 	}
 }
