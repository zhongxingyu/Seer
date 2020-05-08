 package robot;
 
 import lejos.nxt.LCD;
 import lejos.nxt.Button;
 import lejos.robotics.navigation.DifferentialPilot;
 
 /**
  * Racer class that implements the logic for robot going toward the light
  * 
  * @author Khoa Tran, Phuoc Nguyen, Corey Short
  */
 public class Racer {
 
 	/**
 	 * Static variables
 	 */
 	public static final double THRESHOLD = 55;
 
 	/**
 	 * Instance variables
 	 */
 	private Scanner scanner;
 	private DifferentialPilot pilot;
 	private int distanceLimit = 25;
 	private static boolean isDetected = false;
 
 	/**
 	 * Constructor for Racer, which takes in a Scanner and a DifferentialPilot
 	 * 
 	 * @param s
 	 *            - Scanner equips with lights and obstacles detection
 	 * @param dp
 	 *            - Pilot to control the motor
 	 */
 	public Racer(Scanner s, DifferentialPilot dp) {
 		scanner = s;
 		pilot = dp;
 	}
 
 	/**
 	 * Completes two round trips for Milestone 1 by going to the lights back and forth
 	 */
 	public void toLight() {
 		for (int i = 0; i < 2; i++) {
 			while (scanner.getLight() < THRESHOLD) {
 				scanner.scanTo(60);
 				toAngle(scanner.getAngle());
 				LCD.drawInt((int) scanner.getLight(), 0, 0);
 
 				if (scanner.getLight() > THRESHOLD) {
 					stopRobot();
 					sleepRobot(500);
 					break;
 				}
 				scanner.scanTo(-60);
 				toAngle(scanner.getAngle());
 				LCD.drawInt((int) scanner.getLight(), 0, 0);
 				if (scanner.getLight() > THRESHOLD) {
 					stopRobot();
 					sleepRobot(500);
 				}
 			}
 			turnAround();
 			scanner.setLight(0);
 
 			// Move back
 			while (scanner.getLight() < THRESHOLD) {
 				scanner.scanTo(60);
 				toAngle(scanner.getAngle());
 				LCD.drawInt((int) scanner.getLight(), 0, 0);
 
 				if (scanner.getLight() > THRESHOLD) {
 					stopRobot();
 					sleepRobot(500);
 					break;
 				}
 				scanner.scanTo(-60);
 				toAngle(scanner.getAngle());
 				LCD.drawInt((int) scanner.getLight(), 0, 0);
 				if (scanner.getLight() > THRESHOLD) {
 					stopRobot();
 					sleepRobot(500);
 				}
 			}
 			turnAround();
 			scanner.setLight(0);
 		}
 
 		scanner.scanTo(0);
 	}
 
 	/**
 	 * Find light for Milestone 2, since we only want to find the light while 
 	 * detecting for obstacles this time
 	 */
 	public void findLight() {
 		scanner.rotateTo(0, true);
 		Detector detector = new Detector();
 		detector.start();
 
 		while (true) {
 			if (detector.isDetected) {
 				whenDetected();
 				Button.ENTER.waitForPressAndRelease();
 				detector = new Detector();
 				detector.start();
 			}
 			scanner.scanTo(60);
 			toAngle(scanner.getAngle());
 			LCD.drawInt((int) scanner.getLight(), 0, 0);
 			
 			if (detector.isDetected) {
 				whenDetected();
 				Button.ENTER.waitForPressAndRelease();
 				detector = new Detector();
 				detector.start();
 			}
 			scanner.scanTo(-60);
 			toAngle(scanner.getAngle());
 			LCD.drawInt((int) scanner.getLight(), 0, 0);
 			
 			if (detector.isDetected) {
 				whenDetected();
 				Button.ENTER.waitForPressAndRelease();
 				detector = new Detector();
 				detector.start();
 			}
 		}
 	}
 
 	/**
 	 * Tasks to perform when object is detected
 	 */
 	public void whenDetected() {
 		pilot.travel(-30);
 		scanner.rotateTo(0, true);
 		isDetected = false;
 	}
 
 	/**
 	 * Stops the robot
 	 */
 	public void stopRobot() {
 		pilot.stop();
 	}
 
 	/**
 	 * Sleeps the robot
 	 * 
 	 * @param ms
 	 *            - how long to sleep
 	 */
 	public void sleepRobot(int ms) {
 		try {
 			Thread.sleep(ms);
 		} catch (InterruptedException e) {
 			System.out.println(e);
 		}
 	}
 
 	/**
 	 * Steers to a specific angle
 	 * @param angle - the angle to steer to
 	 */
 	private void toAngle(double angle) {
 		pilot.steer(angle);
 	}
 
 	/**
 	 * Turns the robot around at the end of every half
 	 */
 	private void turnAround() {
 		pilot.rotate(200);
 		scanner.rotateTo(0, false);
 		pilot.steer(0); // travel straight
 	}
 
 	/**
 	 * Inner-class detector to make use of Thread
 	 */
 	class Detector extends Thread {
 
 		/**
 		 * Instance variables
 		 */
 		boolean isDetected = false;
 
 		/**
 		 * Runs the detector
 		 */
 		public void run() {
 			while (!isDetected) {
				LCD.drawInt((int) getDistance(), 5, 5);
 
 				if (getDistance() < distanceLimit) {
 					isDetected = true;
 					stopRobot();
 				}
 				if (isLeftTouched() || isRightTouched()) {
 					isDetected = true;
 					stopRobot();
 				}
 				Thread.yield();
 			}
 		}
 
 		/**
 		 * Returns distance from the reading from Ultrasonic Sensor
 		 */
 		public int getDistance() {
 			return scanner.getUltrasonicSensor().getDistance();
 		}
 
 		/**
 		 * Returns True/False when the Left Touch Sensor is touched
 		 */
 		public boolean isLeftTouched() {
 			return scanner.getLeftTouchSensor().isPressed();
 		}
 
 		/**
 		 * Returns True/False when the Right Touch Sensor is touched
 		 */
 		public boolean isRightTouched() {
 			return scanner.getRightTouchSensor().isPressed();
 		}
 	}
 }
