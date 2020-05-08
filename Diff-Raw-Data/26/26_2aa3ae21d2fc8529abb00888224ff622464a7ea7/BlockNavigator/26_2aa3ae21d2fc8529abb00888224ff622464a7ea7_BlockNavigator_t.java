 // * 
 //*/
 package master;
 
 import common.Pos;
 import lejos.nxt.*;
 import lejos.nxt.comm.RConsole;
 
 /**
  * This is a singleton implementation of a Navigator where the robot will
  * attempt to travel as though in a grid. For example the navigator will break
  * down the path to the destination in terms of number of vertical and
  * horizontal blocks.
  * 
  * @author alex
  * 
  */
 public class BlockNavigator extends Navigator {
 
 	private final double US_RANGE = 35;
 	private LineDetector detector;
 	private static INavigator nav;
 	private static Odometer odo;
 	private IRobot robot;
 	private USPoller us;
 	double ROTATE_SPEED = 60;
 	public final double FORWARD_SPEED = 60;
 	private double headNow;
 	private double xNow;
 	private double yNow;
 	private double xHead, yHead;
 	private double destRow;
 	private double destCol;
 
 	private BlockNavigator(IRobot robot, Odometer odo, USPoller uspoller) {
 		super(robot, odo);
 		this.odo = odo;
 		this.robot = robot;
 		this.us = uspoller;
 		this.headNow = odo.getTheta();
 		this.xNow = odo.getX();
 		this.yNow = odo.getY();
 		this.detector = new LineDetector(odo, robot);
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
 		} else if (destCoord == 1080) {
 			destCoord = 1080 - 30;
 		}
 		return destCoord;
 	}
 
 	public void travelToNearestIntersection() {
 		double closeIntersectionX = calculateDestination(odo.getX());
 		double closeIntersectionY = calculateDestination(odo.getY());
 
 		// Find whether closest intersection is left or right
 		if (odo.getX() <= closeIntersectionX) {
 			xHead = 180;
 		} else {
 			xHead = 0;
 		}
 
 		turnTo(xHead);
 		LCD.clear(4);
 		LCD.drawString("About to advance", 0, 4);
 		detector.advanceToIntersection();
 		LCD.clear(4);
 		LCD.drawString("Advanced", 0, 4);
 
 		// Find whether closest intersection is up or down
 		if (odo.getY() <= closeIntersectionY) {
 			yHead = 90;
 		} else {
 			yHead = 270;
 		}
 
 		turnTo(yHead);
 		detector.advanceToIntersection();
 
 	}
 
 	@Override
 	public void travelTo(double x, double y) {
 
 		this.isNavigating = true;
 		int dir;
//		travelToNearestIntersection();
 
 		destRow = calculateDestination(x);
 
 		// check if we need to go up or down
 		if (xNow < destRow) {
 			xHead = 90;
 		} else {
 			xHead = 270;
 		}
 		turnTo(xHead);
 		headNow = odo.getTheta();
 
 		int counter = 0;
 
 		// Travel vertically loop (while not at destination row)
 		while (!isAt(destRow, odo.getX())) {
 			LCD.drawString("Counter " + counter, 0, 4);
 			counter++;
 			// Initialize to left
 			dir = 90;
 
 			// If there is an obstacle within the next tile
 			if (us.isObjectInRange(US_RANGE)) {
 				// While there is an obstacle
 				while (us.isObjectInRange(US_RANGE)) {
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
 				return;
 			} else {
 				// move forward one tile
 				advanceATile();
 			}
 
 			// are we at the row?
 		}// end Travel vertically loop
 
 		destCol = calculateDestination(y);
 		// check if we need to go left or right
 		if (yNow < destCol) {
 			yHead = 0;
 		} else {
 			yHead = 180;
 		}
 		turnTo(yHead);
 		headNow = odo.getTheta();
 
 		// Travel horizontally loop (while not at destination column)
 		while (!isAt(destCol, odo.getY())) {
 			// Initialize to left
 			dir = 90;
 
 			// If there is an obstacle within the next tile
 			if (us.isObjectInRange(US_RANGE)) {
 
 				// While there is an obstacle
 				while (us.isObjectInRange(US_RANGE)) {
 					// Turn dir
 					turnTo(odo.getTheta() + dir);
 					if (us.isObjectInRange(US_RANGE)) {
 						dir = -dir;
 						turnTo(odo.getTheta() + dir);
 					} else {
 						advanceATile();
 						turnTo(odo.getTheta() - dir);
 					}
 				}// end while obstacle
 				advanceATile();
 				this.travelTo(x, y);
 				return;
 			} else {
 				// move forward one tile
 				advanceATile();
 			}
 
 		}// end Travel horizontally loop
 
 		simpleTravelTo(x, y);
 
 		this.isNavigating = false;
 	}// end travelTo
 
 	private void advanceATile() {
 		this.detector.advanceToIntersection();
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
 
 	// Methode for turning 90 degrees counterclockwise
 	private void turnNinety() {
 		double l = IRobot.LEFT_WHEEL_RADIUS, r = IRobot.RIGHT_WHEEL_RADIUS, w = IRobot.WHEEL_WIDTH;
 		robot.setSpeeds(FORWARD_SPEED, ROTATE_SPEED);
 		robot.getLeftMotor().rotate(-convertAngle(l, w, 90.0), true);
 		robot.getRightMotor().rotate(convertAngle(r, w, 90.0), false);
 	}
 
 }
