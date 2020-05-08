 // * 
 //*/
 package master;
 
 import lejos.nxt.LCD;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.SensorPort;
 import lejos.nxt.UltrasonicSensor;
 import lejos.util.Delay;
 
 /**
  * This is a singleton implementation of a Navigator where the robot will
  * attempt to travel as though in a grid. For example the navigator will break
  * down the path to the destination in terms of number of vertical and
  * horizontal blocks.
  * 
  * @author alex
  * @author
  * 
  */
 public class BlockNavigator extends Navigator {
 
 	private static final double TOL = 0.05;
 	private final int US_RANGE = 35;
 	private LineDetector detector;
 	private static INavigator nav;
 	private static Odometer odo;
 	private IRobot robot;
 	private USPoller us;
 
 	double ROTATE_SPEED = 110;
 	public final int FORWARD_SPEED = 200;
 	private static SensorPort usPort = SensorPort.S4;
 
 	private BlockNavigator(IRobot robot, Odometer odo, USPoller uspoller) {
 		super(robot, odo);
 		this.odo = odo;
 		this.robot = robot;
 		this.us = uspoller;
 		this.setDetector(new LineDetector(odo, robot));
 
 	}
 
 	/**
 	 * Get the instance of the BlockNavigator.
 	 * 
 	 * @param robot
 	 *            Robot containing the corresponding motors for the wheels.
 	 * @param odo
 	 *            Odometer that is used to guide the navigation and that will be
 	 *            corrected.
 	 * @return
 	 */
 	public static INavigator getInstance(IRobot robot, Odometer odo,
 			USPoller uspoller) {
 		if (nav == null) {
 			nav = new BlockNavigator(robot, odo, uspoller);
 		}
 		return nav;
 	}
 
 	private double calculateDestination(double coord) {
 		double destCoord;
 		if ((coord % 30) >= 15) {
 			destCoord = coord + (30 - (coord % 30));
 		} else {
 			destCoord = coord - (coord % 30);
 		}
 
 		if (destCoord == 0) {
 			destCoord = 30;
 		} else if (destCoord == 360) {
 			destCoord = 360 - 30;
 		}
 		return destCoord;
 	}
 
 	public void travelToNearestIntersection() {
 		double closeIntersectionX = calculateDestination(odo.getX());
 		double closeIntersectionY = calculateDestination(odo.getY());
 		LCD.drawString("X: " + closeIntersectionX + ", Y: "
 				+ closeIntersectionY, 0, 7);
 		simpleTravelTo(closeIntersectionX, closeIntersectionY);
 
 	}
 
 	@Override
 	public void travelTo(double x, double y) {
 		travelToNearestIntersection();
 		Delay.msDelay(2000);
 
 		double destinationRow = calculateDestination(y);
 		if (odo.getY() < destinationRow) {
 			// Go Up
 			turnTo(90);
 		} else {
 			// Go Down
 			turnTo(270);
 		}
 
 		// Travel vertically loop (while not at destination row)
 		while (!isAt(destinationRow, odo.getY())) {
 			// If there is an obstacle within the next tile
 			if (us.isObjectInRange(US_RANGE)) {
 				avoidObstacle(x, y);
 				return;
 			}
 			// move forward one tile
 			advanceATile();
 		}// end Travel vertically loop
 		
 		double destinationColumn = calculateDestination(x);
 		if (odo.getX() < destinationColumn) {
 			// Go Right
 			turnTo(0);
 		} else {
 			// Go Left
 			turnTo(180);
 		}
 
 		// Travel horizontally loop (while not at destination column)
 		while (!isAt(destinationColumn, odo.getX())) {
 			// If there is an obstacle within the next tile
 			if (us.isObjectInRange(US_RANGE)) {
 				avoidObstacle(x, y);
 				return;
 			}
 			// move forward one tile
 			advanceATile();
 		}// end Travel horizontally loop
 
 		simpleTravelTo(x, y);
 	}// end travelTo
 	
 	private void avoidObstacle(double x, double y) {
 		// Initialize to left
 		double dir = 90;
 		// While there is an obstacle
 		while (us.isObjectInRange(US_RANGE)) {
 
			if (withinATile(odo.getX(), x)) {
 				break;
 			}
 
 			// Turn dir
 			turnTo(odo.getTheta() + dir);
 			if (us.isObjectInRange(US_RANGE)) {
 				dir = -dir;
 				turnTo(odo.getTheta() + dir);
 			} else {
 				// Advance one tile
 				advanceATile();
 				turnTo(odo.getTheta() - dir);
 			}
 		}// end while there is an obstacle
 		advanceATile();
 		this.travelTo(x, y);		
 	}
 
 	private boolean withinATile(double curr, double dest) {
 		return Math.abs(curr - dest) < 45;
 	}
 
 	private void advanceATile() {
 		this.getDetector().advanceToIntersection();
 	}
 
 	private boolean isAt(double destCoord, double coord) {
 		int tol = 8;
 		return ((coord > (destCoord - tol)) && (coord < (destCoord + tol)));
 	}
 
 	/**
 	 * This navigation method does not make use of blocks.
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	public void simpleTravelTo(double x, double y) {
 		super.travelTo(x, y);
 	}
 
 	@Override
 	public void turnTo(double angle) {
 		super.turnTo(angle);
 	}
 
 	public LineDetector getDetector() {
 		return detector;
 	}
 
 	private void setDetector(LineDetector detector) {
 		this.detector = detector;
 	}
 
 	@Override
 	public void goToLoader(int loaderX, int loaderY) {
 		// how many cm the punch is from the center of the robot
		double offset = 10;
 		// how far the center of the robot is from the fron of the robot in cm
 		double dist = 15;
 		double xAAA = 30;
 		double yAAA = 30;
 		double xBBB = 30;
 		double yBBB = 30;
 
 		double maxX = 360;
 		double maxY = 360;
 
 		NXTRegulatedMotor leftMotor = robot.getLeftMotor();
 		NXTRegulatedMotor rightMotor = robot.getRightMotor();
 
 		// first determine which wall the loader is on
 		if (loaderX == 0) {
 			xAAA = 45;
 			yAAA = loaderY + offset;
 
 			xBBB = loaderX + dist;
 			yBBB = yAAA;
 
 		}
 		if (loaderY == 0) {
 			xAAA = loaderX - offset;
 			yAAA = 45;
 
 			xBBB = xAAA;
 			yBBB = loaderY + dist;
 		}
 		if (loaderX == maxX) {
 			xAAA = maxX - 45;
 			yAAA = loaderY - offset;
 
 			xBBB = loaderX - dist;
 			yBBB = yAAA;
 		}
 		if (loaderY == maxY) {
 			xAAA = loaderX + offset;
 			yAAA = maxY - 45;
 
 			xBBB = xAAA;
 			yBBB = loaderY - dist;
 		}
 		travelTo(xAAA, yAAA);
 		simpleTravelTo(xBBB, yBBB);
 
 		// Wait for balls to load
 		try {
 			Thread.sleep(10000);
 		} catch (InterruptedException ex) {
 			Thread.currentThread().interrupt();
 		}
 
 		rightMotor.setSpeed(FORWARD_SPEED);
 		leftMotor.setSpeed(FORWARD_SPEED);
 		rightMotor.rotate(-convertDistance(robot.LEFT_WHEEL_RADIUS, 45 - dist),
 				true);
 		leftMotor.rotate(-convertDistance(robot.LEFT_WHEEL_RADIUS, 45 - dist),
 				false);
 		return;
 	}
 
 }
