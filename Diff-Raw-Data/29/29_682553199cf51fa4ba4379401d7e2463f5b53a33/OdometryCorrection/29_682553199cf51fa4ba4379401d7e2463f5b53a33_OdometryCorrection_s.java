 package odometry;
 
 import lejos.nxt.LightSensor;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.Sound;
 import lejos.util.Delay;
 
 /**
  * Thread to correct the position
  * @author Team 13
  *
  */
 public class OdometryCorrection extends Thread {
 	private static final long CORRECTION_PERIOD = 1000;
<<<<<<< HEAD
	private static final int FORWARD_SPEED = 120;
=======
 	private static final int FORWARD_SPEED = 100;
>>>>>>> 4fe5a82b2d7a09c9de15e09a9cc4bc93deab7b25
 	private Odometer myOdometer;
 	private LightSensor middleSensor;
 	private NXTRegulatedMotor leftMotor, rightMotor;
 	
 	// the midpoint of the tile is set as well as the its width
 	private final double tileWidth = 30.00;
 	
 	// distance measured between the light sensor's position and the the center between the wheels
 	private double sensorCenter = 11.50;
 	
 	// array containing all the horizontal lines
 	private double[] yLines = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
 	
 	// array containing all the vertical lines
 	private double[] xLines = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
 	
 	/**
 	 * Constructor
 	 * @param odometer
 	 * @param middleSensor
 	 * @param myLeftMotor
 	 * @param myRightMotor
 	 */
 	public OdometryCorrection(Odometer odometer, LightSensor middleSensor, NXTRegulatedMotor myLeftMotor, NXTRegulatedMotor myRightMotor) {
 		this.myOdometer = odometer;
 		this.middleSensor = middleSensor;
 		this.leftMotor = myLeftMotor;
 		this.rightMotor = myRightMotor;
 	}
 
 	/**
 	 * Executes the thread
 	 */
 	public void run() {
 		try {Thread.sleep(500);} catch (InterruptedException e) {}
 		int val;
 		int preVal = middleSensor.readValue();
 		
 		while(true) {
 			
 			// important to limit resources from the CPU
 			try {Thread.sleep(50);} catch (InterruptedException e) {}
 			
 			// activate correction only when the robot is moving in a straight line and no line is detected
 			while (	leftMotor.getSpeed() == FORWARD_SPEED && rightMotor.getSpeed() == FORWARD_SPEED ) {
 				
 				val = middleSensor.readValue();
 					
 				if ((val-preVal) > 7) {
 					
 					Sound.beep();
 								
 					double theta = myOdometer.getAng();
 								
 					if (theta < 100 && theta > 80) { // if robot is moving NORTH, correct Y position
 						double currentY = myOdometer.getY();
 						double closestLine = (currentY - sensorCenter)/tileWidth;
 						myOdometer.setY(getClosest(yLines, closestLine)*tileWidth + sensorCenter);
 					}
 								
 					if (theta < 280 && theta > 260){ // if robot is moving SOUTH, correct Y position
 						double currentY = myOdometer.getY();
 						double closestLine = (currentY + sensorCenter)/tileWidth;
 						myOdometer.setY(getClosest(yLines, closestLine)*tileWidth - sensorCenter);
 					}
 								
 					if (theta > 350 && theta < 10) { // if robot is moving EAST, correct X position
 						double currentX = myOdometer.getX();
 						double closestLine = (currentX - sensorCenter)/tileWidth;
 						myOdometer.setX(getClosest(xLines, closestLine)*tileWidth + sensorCenter);
 					}
 								
 					if (theta < 190 && theta > 170){ // if robot is moving WEST, correct X position
 						double currentX = myOdometer.getX();
 						double closestLine = (currentX + sensorCenter)/tileWidth;
 						myOdometer.setX(getClosest(xLines, closestLine)*tileWidth - sensorCenter);
 					}
 							
 					try {Thread.sleep(CORRECTION_PERIOD);} catch (InterruptedException e) {} // correction occurs only once every period
 					val = middleSensor.readValue();
 					preVal = middleSensor.readValue();
 				}	
 				preVal = val;
 				Delay.msDelay(42);
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param array
 	 * @param position
 	 * @return - the closets point to a point in the array
 	 */
 	public double getClosest(double[] array, double position) {
 	    double lowestDiff = Double.MAX_VALUE;
 	    double result = 0.0;
 	    for (double i : array) {
 	        double diff = Math.abs(position - i);
 	        if (diff < lowestDiff) {
 	            lowestDiff = diff;
 	            result = i;
 	        }
 	    }
 	    return result;
 	}
 	
 }
