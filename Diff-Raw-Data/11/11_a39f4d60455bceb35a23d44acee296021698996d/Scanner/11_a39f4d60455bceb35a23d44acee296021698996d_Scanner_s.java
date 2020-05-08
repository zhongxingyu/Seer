 package robot;
 
 import lejos.nxt.LightSensor;
 import lejos.nxt.UltrasonicSensor;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.TouchSensor;
 
 /**
  * Scanner class which implements logic for scanning lights and obstacles
  * Adapted from ScanRecorder.java in the skeleton
  * @author Corey Short, Khoa Tran
  */
 public class Scanner {
 
 	/**
 	 * Instance variables
 	 */
 	private NXTRegulatedMotor motor;
 	private LightSensor eye;
 	private UltrasonicSensor ultraSensor;
 	private TouchSensor leftTouchSensor;
 	private TouchSensor rightTouchSensor;
 	private int xAngle = 0;
 	private int xLight = 0;
 	
 	/**
 	 * Constructor for Scanner class that takes in the motor and lightsensor as params
 	 * @param theMotor - motor of the robot
 	 * @param theEye - light sensor to detect the lights
 	 */
 	public Scanner(NXTRegulatedMotor theMotor, LightSensor theEye) {
 		motor = theMotor;
 		motor.setSpeed(500);
 		motor.setAcceleration(4000);
 		eye = theEye;
 		eye.setFloodlight(false);		
 	}
 
 	public Scanner(NXTRegulatedMotor theMotor, LightSensor theEye, UltrasonicSensor ussensor, TouchSensor left, TouchSensor right) {
 		motor = theMotor;
		//motor.setSpeed(500);
		//motor.setAcceleration(4000);
 		motor.setSpeed(300);
		motor.setAcceleration(1000);
 		eye = theEye;
 		eye.setFloodlight(false);
 		ultraSensor = ussensor;
 		leftTouchSensor = left;
 		rightTouchSensor = right;
 		
 	}
 	/**
 	 * Scans to a certain angle
 	 * @param limit - the angle to scan to
 	 */
 	public void scanTo(int limit) {
 		int light;
 		xLight = 0;
 		xAngle = motor.getTachoCount();
 		motor.rotateTo(limit, true);
 		while (motor.isMoving()) {
 			light = eye.getLightValue();
 			if (light > xLight) {
 				xLight = light;
 				xAngle = motor.getTachoCount();
 			}
 		}
 	}
 	
 	/**
 	 * Get the current Angle from TachoCount()
 	 * @return xAngle : angle from TachoCount()
 	 */
 	public int getAngle(){
 		return xAngle;
 	}
 	
 	/**
 	 * Get the max Light Value
 	 * @return max light value
 	 */
 	public int getLight(){
 		return xLight;
 	}
 	
 	/**
 	 * Set the Light value
 	 * @param: light value to set 
 	 */
 	public void setLight(int light){
 		xLight = light;
 	}
 	
 	/**
 	 * Rotates the scanner head to the angle
 	 * @param angle - how much to rotate to
 	 * @param instantReturn - if true, we don't wait until the whole rotate process completes
 	 */
 	public void rotateTo(int angle, boolean instantReturn) {
 		motor.rotateTo(angle, instantReturn);
 	}
 	
 	/**
 	 * @return the ultrasonic sensor
 	 */
 	protected UltrasonicSensor getUltrasonicSensor(){
 		return ultraSensor;
 	}
 	
 	/**
 	 * @return the left touch sensor
 	 */
 	protected TouchSensor getLeftTouchSensor(){
 		return leftTouchSensor;
 	}
 	
 	/**
 	 * @return the right touch sensor
 	 */
 	protected TouchSensor getRightTouchSensor(){
 		return rightTouchSensor;
 	}
 }
