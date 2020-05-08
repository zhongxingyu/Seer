 package ee.ttu.kinect.model;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import ee.ttu.kinect.SkeletonParserFile;
 import ee.ttu.kinect.SkeletonParserKinect;
 import ee.ttu.kinect.controller.MainController;
 import ee.ttu.kinect.util.FileWorker;
 
 import kinectsensorproxy.KinectSensorProxy;
 import net.sf.jni4net.Bridge;
 
 public class MainModel {
 
 	private final Logger logger = Logger.getLogger(this.getClass().getName());
 
 	private MainController controller;
 
 	private Body body;
 
 	private KinectSensorProxy kinectSensorProxy;
 
 	private SkeletonParserKinect skeletonParserKinect;
 
 	private SkeletonParserFile skeletonParserFile;
 
 	private FileWorker fileWorker;
 
 	private KinectThread kinectThread;
 
 	private boolean doRun;
 
 	private boolean doReadFromFile;
 
 	private boolean doSaveToFile;
 
 	public MainModel(MainController controller) {
 		this.controller = controller;
 
 		this.body = new Body();
 
 		this.fileWorker = new FileWorker();
 
 		this.skeletonParserKinect = new SkeletonParserKinect();
 
 		this.skeletonParserFile = new SkeletonParserFile();
 	}
 
 	public void init() {
 		try {
 			// Bundle bundle =
 			// org.eclipse.core.runtime.Platform.getBundle("org.jnect.core");
 			// URL locationUrl = FileLocator.find(bundle, new Path("/"), null);
 			// URL fileUrl = FileLocator.toFileURL(locationUrl);
 			// String parentPath = fileUrl.getFile();
 
 			Bridge.init(new File(System.getProperty("user.dir"), "lib"));
 			File path = new File(System.getProperty("user.dir"),
 					"lib/KinectSensorProxy.j4n.dll").getCanonicalFile();
 			Bridge.LoadAndRegisterAssemblyFrom(path);
 
 			this.kinectSensorProxy = new KinectSensorProxy();
 		} catch (IOException ioe) {
 			logger.log(Level.SEVERE, ioe.getLocalizedMessage(), ioe);
 		}
 	}
 
 	public void doStart() {
 		logger.info("Starting skeleton tracking");
 
 		try {
 			this.kinectSensorProxy.initialize(); // Ignoring the return value.
 													// "Setup Done!"
 			this.kinectSensorProxy.start();
 		} catch (Exception e) {
 			controller.showMessagePopup(e.getMessage());
 		}
 
 		this.doRun = true;
 		kinectThread = new KinectThread();
 		kinectThread.start();
 	}
 
 	public void doStop() {
 		logger.info("Shutdown requested");
 		
 		this.doRun = false;
 
 		String msg = this.kinectSensorProxy.stop();
 
 		logger.info(msg);
 	}
 
 	public void readFile(File file) {
 		try {
 			fileWorker.readFile(file);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void startReading() {
 			doReadFromFile = true;
 			doSaveToFile = false;
 
 			body = new Body();
 	}
 
 	public void stopReading() {
 		doReadFromFile = false;
 	}
 
 	public void startSaving() {
 		doSaveToFile = true;
 		doReadFromFile = false;
 
 		body = new Body();
 	}
 
	public void stopSaving() {
 		try {
 			doSaveToFile = false;

			fileWorker.dumpFile();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private String getDataFromKinect() {
 		return kinectSensorProxy.getFrames(); // This seems to be non-blocking
 	}
 
 	private String getDataFromFile() {
 		String input = null;
 		try {
 			input = fileWorker.readNextLine();
 			if (input == null) { // end of file reached
 				stopReading();
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return input;
 	}
 
 	private void parseSkeletonFromKinect(String input) {
 		// Parse skeleton to Body model
 		// The parts are separated with a '*'.
 		String[] inputParts = input.split("\\*");
 		if (input.contains("skeletonData")) {
 			// Found skeleton
 			for (int i = 0; i < inputParts.length; i++) {
 				if (inputParts[i].contains("skeletonData")) {
 					skeletonParserKinect.parseSkeleton(inputParts[i], body);
 					break; // TODO Do we want all skeleton frames
 					// collected
 					// in 50ms or only one frame?
 				}
 			}
 		} else { // Lost skeleton
 			skeletonParserKinect.parseSkeleton(inputParts[0], body);
 		}
 	}
 
 	private void parseSkeletonFromFile(String input) {
 		skeletonParserFile.parseSkeleton(input, body);
 	}
 
 	private class KinectThread extends Thread {
 		@Override
 		public void run() {
 			while (doRun) {
 				String input = null;
 				if (!doReadFromFile) {
 					input = getDataFromKinect();
 					if (input != null) {
 						parseSkeletonFromKinect(input);
 					}
 				} else {
 					input = getDataFromFile();
 					if (input != null) {
 						parseSkeletonFromFile(input);
 					}
 				}
 
 				if (input != null) {
 					// Redraw the skeleton
 					controller.redrawSkeleton(body);
 
 					// Save skeleton
 					if (doSaveToFile) {
 						try {
 							fileWorker.addToSave(body.toString() + getMarkers());
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				}
 
 				// Sleep for X ms - give Kinect some time to collect
 				// skeleton data
 				try {
 					Thread.sleep(30);
 				} catch (InterruptedException e) {
 					logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
 				}
 			}
 
 		}
 		
 		private String getMarkers() {
 			StringBuffer markers = new StringBuffer();
 			boolean[] markersBool = controller.getMarkersState();
 			for (boolean mb : markersBool) {
 				markers = markers.append(booleanToInt(mb)).append("\t");
 			}
 			
 			return markers.toString();
 		}
 		
 		private int booleanToInt(boolean bool) {
 			return bool ? 1 : 0;
 		}
 
 	}
 
 }
