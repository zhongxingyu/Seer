 package analyticgeometry;
 
 import SimpleOpenNI.SimpleOpenNI;
 import processing.core.PApplet;
 import processing.core.PVector;
 
 public class MainDrum extends PApplet {
 	private Player p;
 	private KinectData k;
 	private KinectInstrument instrument;
 	private Midi midi;
 
 	private SimpleOpenNI context;
 	private boolean autoCalib = true;
 
 	private boolean kinectReady = false;
 
 	public void setup() {
 		// Processing Stuff
 		size(640, 480);
 		frameRate(60);
 		smooth();
 		stroke(255, 0, 255);
 		strokeWeight(2);
 
 		// New Player
 		p = new Player();
 
 		// Kinect
 		k = new KinectData();
 
 		context = new SimpleOpenNI(this, SimpleOpenNI.RUN_MODE_SINGLE_THREADED);
 		context.openFileRecording("drum3.oni");
 		context.seekPlayer(10, SimpleOpenNI.PLAYER_SEEK_CUR);
 		context.enableDepth();
 		context.enableRGB();
 		context.enableUser(SimpleOpenNI.SKEL_PROFILE_ALL);
 		context.enableScene(640, 480, 60);
 		context.setSmoothingHands(0.5f);
		context.setSmoothingSkeleton(0.5f);
 		context.mirror();
 
 		// Midi
 		midi = new Midi(this);
 
 		// Instrument
 		int InstrumentNumber = 5;
 		int InstrumentMargin = 20;
 		int InstrumentWidth = 100;
 		int InstrumentHeight = 60;
 
 		instrument = new Drum(InstrumentNumber, InstrumentMargin, InstrumentWidth,
 				InstrumentHeight, this, midi);
 	}
 
 	public void draw() {
 		background(0);
 		// Kinect update
 		context.update();
 
 		// Kinectbild
 		image(context.rgbImage(), 0, 0);
 
 		// Userdaten updaten
 		getuserLimbs();
 
 		if (kinectReady) {
 
 			pushMatrix();
 
 			// Ursprung zum COM verschieben
 			translate(p.centerOfMass.x, p.centerOfMass.y);
 
 			instrument.update(p);
 			instrument.draw(p);
 			instrument.checkFredMatch(p);
 			// instrument.checkNeckMatch(p);
 			// instrument.checkHeadFred();
 
 			popMatrix();
 		}
 	}
 
 	// Callbacks SimpleOpenNI
 
 	public void onNewUser(int userId) {
 		println("onNewUser - userId: " + userId);
 		println("  start pose detection");
 
 		if (autoCalib)
 			context.requestCalibrationSkeleton(userId, true);
 		else
 			context.startPoseDetection("Psi", userId);
 	}
 
 	public void onLostUser(int userId) {
 		println("onLostUser - userId: " + userId);
 	}
 
 	public void onExitUser(int userId) {
 		println("onExitUser - userId: " + userId);
 	}
 
 	public void onReEnterUser(int userId) {
 		println("onReEnterUser - userId: " + userId);
 	}
 
 	public void onStartCalibration(int userId) {
 		println("onStartCalibration - userId: " + userId);
 	}
 
 	public void onEndCalibration(int userId, boolean successfull) {
 		println("onEndCalibration - userId: " + userId + ", successfull: " + successfull);
 
 		if (successfull) {
 			println("  User calibrated !!!");
 			context.startTrackingSkeleton(userId);
 
 		} else {
 			println("  Failed to calibrate user !!!");
 			println("  Start pose detection");
 			context.startPoseDetection("Psi", userId);
 		}
 	}
 
 	public void onStartPose(String pose, int userId) {
 		println("onStartPose - userId: " + userId + ", pose: " + pose);
 		println(" stop pose detection");
 
 		context.stopPoseDetection(userId);
 		context.requestCalibrationSkeleton(userId, true);
 	}
 
 	public void onEndPose(String pose, int userId) {
 		println("onEndPose - userId: " + userId + ", pose: " + pose);
 	}
 
 	public void getuserLimbs() {
 		int[] userList = context.getUsers();
 		for (int i = 0; i < userList.length; i++) {
 			if (context.isTrackingSkeleton(userList[i])) {
 
 				PVector handRight = new PVector();
 				PVector handLeft = new PVector();
 
 				PVector centerOfMass = new PVector();
 
 				PVector elbowRight = new PVector();
 				PVector shoulderRight = new PVector();
 				PVector neck = new PVector();
 
 				// Get joints
 				// context.getJointPositionSkeleton(userList[i],
 				// SimpleOpenNI.SKEL_RIGHT_HAND, handRight);
 				// context.getJointPositionSkeleton(userList[i],
 				// SimpleOpenNI.SKEL_LEFT_HAND, handLeft);
 
 				// Switch Hands
 				context.getJointPositionSkeleton(userList[i], SimpleOpenNI.SKEL_LEFT_HAND,
 						handRight);
 				context.getJointPositionSkeleton(userList[i], SimpleOpenNI.SKEL_RIGHT_HAND,
 						handLeft);
 
 				context.getJointPositionSkeleton(userList[i], SimpleOpenNI.SKEL_RIGHT_ELBOW,
 						elbowRight);
 				context.getJointPositionSkeleton(userList[i], SimpleOpenNI.SKEL_RIGHT_SHOULDER,
 						shoulderRight);
 
 				context.getJointPositionSkeleton(userList[i], SimpleOpenNI.SKEL_NECK, neck);
 
 				context.getCoM(userList[i], centerOfMass);
 
 				// Convert joints
 				context.convertRealWorldToProjective(centerOfMass, centerOfMass);
 				context.convertRealWorldToProjective(handLeft, handLeft);
 				context.convertRealWorldToProjective(handRight, handRight);
 				context.convertRealWorldToProjective(elbowRight, elbowRight);
 				context.convertRealWorldToProjective(shoulderRight, shoulderRight);
 				context.convertRealWorldToProjective(neck, neck);
 
 				p.setCOM(centerOfMass);
 				p.setHandRight(handRight);
 				p.setHandLeft(handLeft);
 				p.setNeck(neck);
 
 				p.setElbow(elbowRight);
 				p.setShoulder(shoulderRight);
 
 				kinectReady = true;
 			}
 		}
 	}
 }
