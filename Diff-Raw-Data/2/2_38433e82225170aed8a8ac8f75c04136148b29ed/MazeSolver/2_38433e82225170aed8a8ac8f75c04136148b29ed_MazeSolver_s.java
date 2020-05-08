 package nxt;
 
 import lejos.nxt.Button;
 import lejos.nxt.LightSensor;
 import lejos.nxt.Motor;
 import lejos.nxt.SensorPort;
 import lejos.nxt.addon.CompassSensor;
 import lejos.robotics.navigation.DifferentialPilot;
 
 /**
  * Maze Solver
  * 
  * 
  */
 public class MazeSolver {
 
 	/**
 	 * This is responsible for moving the robot
 	 */
 	private DifferentialPilot myPilot;
 	/**
 	 * Our light sensors
 	 */
 	private LightSensor myFrontSensor, myRightSensor;
 	/**
 	 * Our compass sensor
 	 */
 	private CompassSensor myCompass;
 
 	/**
 	 * 12 Inches, what we think is the length of one tile
 	 */
 	private static final double travelDist = 10.48f;
 	/**
 	 * The wheel diameter
 	 */
 	private static final double wheelDiam = 2.5f;
 	/**
 	 * The space between wheels
 	 */
 	private static final double axleLength = 9.6f;
 	/**
 	 * Light Threshold
 	 */
 	private static final int lightThreshold = 33;
 	/**
 	 * End of maze threshold
 	 */
 	private static final int endThreshold = 52;
 	/**
 	 * Speed to rotate
 	 */
 	private static final double rotateSpeed = 30.0f;
 
 	/**
 	 * Default constructor, initializes the motors and sensors
 	 */
 	public MazeSolver() {
 		// Set up motors and sensors
 		myPilot = new DifferentialPilot(wheelDiam, axleLength, Motor.B,
 				Motor.A, true);
 		myFrontSensor = new LightSensor(SensorPort.S1);
 		myRightSensor = new LightSensor(SensorPort.S2);
 		myCompass = new CompassSensor(SensorPort.S3);
 
 		// Set rotate speed
 		myPilot.setRotateSpeed(rotateSpeed);
 		myPilot.setAcceleration(15);
 
 		// Calibrate
 		doCalibration();
 	}
 
 	/**
 	 * Program Entry Point
 	 * 
 	 * @param args
 	 *            Command line arguments
 	 */
 	public static void main(String[] args) {
 		// Create and start the solver
 		MazeSolver mySolver = new MazeSolver();
 		mySolver.solve();
 		// mySolver.testLights();
 	}
 
 	/**
 	 * Runs the maze solving algorithm
 	 */
 	public void solve() {
 
 		// While we haven't solved the maze
 		while (!atTarget()) {
 
 			// Check for a change in the light value on the right
 			if (rightIsClear()) {
 				// Turn right
 				turnRight();
 
 				// Check for result
 				if (atTarget()) {
 					break;
 				}
 
 				// Move forward since the front will be clear if the right was
 				// clear
 				goForward();
 
 			} else {
 				// Check if front is clear
 				if (frontIsClear()) {
 					goForward();
 				} else {
 					// If front and right are blocked, turn left (keep hand
 					// against wall)
 					turnLeft();
 				}
 			}
 
 		}
 
 		// We are done
 		mazeSolved();
 	}
 
 	/**
 	 * Calibration utility method, not sure if we need it
 	 */
 	private void doCalibration() {
 		// Prep sensor
 		System.out.println("Port 1: Front Sensor");
 		System.out.println("Port 2: Right Sensor");
 		System.out.println("Port 3: Compass\n");
 
 		// Calibrate front
 		// System.out.println("Calibrate High Front");
 		// Button.waitForPress();
 		// myFrontSensor.calibrateHigh();
 		System.out.print("Value: ");
 		System.out.println(myFrontSensor.getLightValue());
 
 		// Calibrate right
 		// System.out.println("Calibrate High Right");
 		// Button.waitForPress();
 		// myRightSensor.calibrateHigh();
 		System.out.print("Value: ");
 		System.out.println(myRightSensor.getLightValue());
 		Button.waitForPress();
 	}
 
 	/**
 	 * Right turn method Uses compass
 	 */
 	private void turnRight() {
 		System.out.println("R");
 		/*
 		 * // Stop momentarily myPilot.stop();
 		 * 
 		 * // Get current bearing float x = myCompass.getDegrees(); float y = (x
 		 * - 90f) % 360;
 		 * 
 		 * // Get us within a threshold of the degree that we want while (x < y
 		 * - 3) { myPilot.rotate(-5); x = myCompass.getDegrees();
 		 * System.out.println("Bearing: " + x); }
 		 */
 
 		// Turn Right
 		myPilot.rotate(-90);
 	}
 
 	/**
 	 * Turns the robot left using compass
 	 */
 	private void turnLeft() {
 		System.out.println("L");
 		/*
 		 * // Stop momentarily myPilot.stop();
 		 * 
 		 * // Get current bearing float x = myCompass.getDegrees(); float y = (x
 		 * + 90f) % 360;
 		 * 
 		 * // Get us within a threshold of the degree that we want while (x < y
 		 * - 3) { myPilot.rotate(-5); x = myCompass.getDegrees();
 		 * System.out.println("Bearing: " + x); }
 		 */
 
 		// Turn Right
		myPilot.rotate(-90);
 	}
 
 	/**
 	 * Go forward method
 	 */
 	private void goForward() {
 		System.out.println("F");
 		// Move some more, and return right away
 		myPilot.travel(travelDist);
 	}
 
 	/**
 	 * Right is clear method
 	 * 
 	 * @return True if right is clear
 	 */
 	private boolean rightIsClear() {
 		//
 		// System.out.print("Right Light Value: ");
 		System.out.print("R: ");
 		System.out.println(myRightSensor.getLightValue());
 		return myRightSensor.getLightValue() > lightThreshold;
 	}
 
 	/**
 	 * Front is clear method
 	 * 
 	 * @return True if front is clear
 	 */
 	private boolean frontIsClear() {
 		//
 		// System.out.print("Front Light Value: ");
 		System.out.print("F: ");
 		System.out.println(myFrontSensor.getLightValue());
 		return myFrontSensor.getLightValue() > lightThreshold;
 	}
 
 	/**
 	 * Checks for a bright object in front to indicate that we are at the target
 	 * 
 	 * @return True if we are at target
 	 */
 	private boolean atTarget() {
 		return myFrontSensor.getLightValue() > endThreshold;
 	}
 
 	/**
 	 * Maze solved method
 	 */
 	private void mazeSolved() {
 		// We should have completed by now
 		System.out.println("Done");
 		Button.waitForPress();
 		System.out.println("X");
 		// Play Victorious Sound!
 	}
 
 	public void testLights() {
 		for (int i = 0; i < 10; i++) {
 			// goForward();
 			rightIsClear();
 			frontIsClear();
 			Button.waitForPress();
 		}
 	}
 
 }
