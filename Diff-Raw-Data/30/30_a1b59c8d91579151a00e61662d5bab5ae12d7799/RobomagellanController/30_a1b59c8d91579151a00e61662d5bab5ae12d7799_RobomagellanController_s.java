 package com.formicite;
 
 import com.google.android.maps.GeoPoint;
 import java.lang.Thread;
 import java.lang.Math;
 
 public abstract class RobomagellanController extends Thread {
 	private Course course;
 	private GeoPoint gps;
 	private float orientation;
 	private boolean terminate;
 	private boolean pictureRequested;
 	private VisionAlgo visionAlgo;
 	private VisionAlgo.Result[] results;
 	private int[] sonar;
 	private int motorSpeed = MOTOR_ZERO;
 	private int steeringPosition = STEERING_ZERO;
 	private boolean powerDraw = false;
 	private String message;
 	
	public static final int SERVO_MIN =      96000;
	public static final int SERVO_MAX =     192000;
 	public static final int MOTOR_ZERO =    144000;
 	public static final int STEERING_ZERO = 175000;
 		
 	public RobomagellanController(Course course) {
 		this.course = course;
 		terminate = false;
 		pictureRequested = false;
 	}
 	
 	protected void sleep(int l) {
 		if (isTerminated()) { return; }
 		try {
 			Thread.sleep(l);
 		} catch (Exception ex) {
 			terminate();
 		}
 	}
 	
 	protected void waitForGPS() {
 		while (getGPS() == null && !isTerminated()) {
 			sleep(10);
 		}
 	}
 	
 	protected double distanceToCoursePoint(CoursePoint pt) {
 		GeoPoint gps = getGPS();
 		int dLat = gps.getLatitudeE6() - pt.getLocation().getLatitudeE6();
 		int dLon = gps.getLongitudeE6() - pt.getLocation().getLongitudeE6();
 		return Math.sqrt(dLat * dLat + dLon * dLon);
 	}
 	
 	protected double getShortestAngleToCoursePoint(CoursePoint pt) {
 		GeoPoint gps = getGPS();
 		int dLat = pt.getLocation().getLatitudeE6() - gps.getLatitudeE6();
 		int dLon = pt.getLocation().getLongitudeE6() - gps.getLongitudeE6();
 		double goalAngle = Math.atan2(dLat, dLon);
 		//N = pi/2
 		//W = +/-pi
 		//E = 0
 		//S = -pi/2
		double currentAngle = Math.PI - getOrientation(); //Deal with compass strangeness.
 		while (currentAngle < -Math.PI) { currentAngle += Math.PI * 2; }
 		while (currentAngle > +Math.PI) { currentAngle -= Math.PI * 2; }
 		//FIXME: Need to add offset via visual inspection/
 		double turn = goalAngle - currentAngle;
 		while (turn < -Math.PI) { turn += Math.PI * 2; }
 		while (turn > +Math.PI) { turn -= Math.PI * 2; }
 		return turn;
 	}
 	
 	public boolean isTerminated() {
 		return terminate;
 	}
 	
 	public void terminate() {
 		terminate = true;
 	}
 	public String getMessage() {
 		return message;
 	}
 	protected void setMessage(String data) {
 		message = data;
 	}
 	
 	public VisionAlgo getVisionAlgorithm() {
 		return visionAlgo;
 	}
 	
 	public abstract void run();
 	
 	protected void setVisionAlgorithm(VisionAlgo v) {
 		visionAlgo = v;
 	}
 	
 	public boolean pictureRequested() {
 		if (pictureRequested) {
 			pictureRequested = false;
 			return true;
 		}
 		return false;
 	}
 	
 	public void setVisionResults(VisionAlgo.Result[] r) {
 		results = r;
 	}
 	
 	protected void requestPicture() {
 		results = null;
 		pictureRequested = true;
 	}
 	
 	protected GeoPoint getGPS() {
 		return gps;
 	}
 	protected float getOrientation() {
 		return orientation;
 	}
 	protected int[] getSonar() {
 		return sonar;
 	}
 		
 	public void setSonar(int[] val) {
 		sonar = val;
 	}
 		
 	public void setGPS(GeoPoint gps, float o) {
 		this.gps = gps;
 		orientation = o;
 	}
 	
 	public int getMotor() {
 		return motorSpeed;
 	}
 	public int getSteering() {
 		return steeringPosition;
 	}
 	public boolean getPowerDraw() {
 		return powerDraw;
 	}
 	
 		
 	protected void setMotor(int i) {
 		motorSpeed = i;
 	}
 	protected void setSteering(int i) {
 		steeringPosition = i;
 	}
 	protected void activatePowerDraw() {
 		powerDraw = true;
 	}
 	protected void deactivatePowerDraw() {
 		powerDraw = false;
 	}
 
 	
 }
