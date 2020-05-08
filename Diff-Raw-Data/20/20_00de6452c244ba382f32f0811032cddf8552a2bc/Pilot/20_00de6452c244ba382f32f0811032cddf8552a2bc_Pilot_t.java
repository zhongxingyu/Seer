 package navigation;
 
 import odometry.Odometer;
 
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.util.Delay;
 
 public class Pilot {
	final static int FAST = 175, SLOW = 85, ACCELERATION = 1500; // default 4000, trying lower for smooth transitions
 	final static double DEG_ERR = 1.0, CM_ERR = 1.0;
 	private Odometer myOdometer;
 	private NXTRegulatedMotor leftMotor, rightMotor;
 
 	public Pilot(Odometer odo) {
 		this.myOdometer = odo;
 
 		NXTRegulatedMotor[] motors = this.myOdometer.getMotors();
 		this.leftMotor = motors[0];
 		this.rightMotor = motors[1];
 
 		// set acceleration
 		this.leftMotor.setAcceleration(ACCELERATION);
 		this.rightMotor.setAcceleration(ACCELERATION);
 	}
 
 	/*
 	 * Functions to set the motor speeds jointly
 	 */
 	public void setSpeeds(float lSpd, float rSpd) {
 		this.leftMotor.setSpeed(lSpd);
 		this.rightMotor.setSpeed(rSpd);
 		if (lSpd < 0)
 			this.leftMotor.backward();
 		else
 			this.leftMotor.forward();
 		if (rSpd < 0)
 			this.rightMotor.backward();
 		else
 			this.rightMotor.forward();
 	}
 
 	public void setSpeeds(int lSpd, int rSpd) {
 		this.leftMotor.setSpeed(lSpd);
 		this.rightMotor.setSpeed(rSpd);
 		if (lSpd < 0)
 			this.leftMotor.backward();
 		else
 			this.leftMotor.forward();
 		if (rSpd < 0)
 			this.rightMotor.backward();
 		else
 			this.rightMotor.forward();
 	}
 
 	/*
 	 * Float the two motors jointly
 	 */
 	public void setFloat() {
 		this.leftMotor.stop();
 		this.rightMotor.stop();
 		this.leftMotor.flt(true);
 		this.rightMotor.flt(true);
 	}
 
 	/*
 	 * TravelTo function which takes as arguments the x and y position in cm Will travel to designated position, while
 	 * constantly updating it's heading
 	 */
 	public void travelTo(double x, double y) {
 		
 		// determine the vectors needed to travel
 		double vectorX = x - myOdometer.getX();
 		double vectorY = y - myOdometer.getY();
 		
 		// calculate the distance needed to travel
 		double distance = Math.sqrt(Math.pow(vectorX,2) + Math.pow(vectorY,2));
 		
 		double minAng;
 		minAng = (Math.atan2(y - myOdometer.getY(), x - myOdometer.getX())) * (180.0 / Math.PI);
 		if (minAng < 0)
 			minAng += 360.0;
		this.turnTo(minAng, false);
		//this.setSpeeds(FAST, FAST);
 		leftMotor.setSpeed(FAST);
 		rightMotor.setSpeed(FAST);
 		leftMotor.rotate(convertDistance(myOdometer.leftRadius, distance), true);
 		rightMotor.rotate(convertDistance(myOdometer.rightRadius, distance), false);
 	
 		leftMotor.stop();
 		rightMotor.stop();
 
 	}
 	
 	/*
 	 * TravelTo function which takes as arguments the x and y position in cm Will travel to designated position, while
 	 * constantly updating it's heading
 	 */
 	public void travelTo2(double x, double y) {
 		double minAng;
		minAng = (Math.atan2(y - myOdometer.getY(), x - myOdometer.getX())) * (180.0 / Math.PI);
		if (minAng < 0)
			minAng += 360.0;
		this.turnTo(minAng, true);
		Delay.msDelay(500); 
 		while (Math.abs(x - myOdometer.getX()) > CM_ERR || Math.abs(y - myOdometer.getY()) > CM_ERR) {
 			this.setSpeeds(FAST, FAST);
 		}
 		this.setSpeeds(0, 0);
 	}
 
 	/*
 	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
 	 * motors when the turn is completed
 	 */
 	public void turnTo(double angle, boolean stop) {
 		
 		double error = angle - this.myOdometer.getAng();
 
 		while (Math.abs(error) > DEG_ERR) {
 
 			error = angle - this.myOdometer.getAng();
 
 			if (error < -180.0) {
 				this.setSpeeds(-SLOW, SLOW);
 			} else if (error < 0.0) {
 				this.setSpeeds(SLOW, -SLOW);
 			} else if (error > 180.0) {
 				this.setSpeeds(SLOW, -SLOW);
 			} else {
 				this.setSpeeds(-SLOW, SLOW);
 			}
 		}
 
 		if (stop) {
 			this.setSpeeds(0, 0);
 		}
 	}
 	
 	/*
 	 * Go foward a set distance in cm
 	 */
 	public void goForward(double distance) {
 		this.travelTo(Math.cos(Math.toRadians(this.myOdometer.getAng())) * distance, Math.cos(Math.toRadians(this.myOdometer.getAng())) * distance);
 	}
 	
 	/*
 	 * Useful conversions from lab 2 squaredriver.java, converts distance to rotation degrees according to wheel radius
 	 */ 
 	private static int convertDistance(double radius, double distance) {
 		return (int) ((180.0 * distance) / (Math.PI * radius));
 	}
 	
 }
