 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.*;
 
 public class PController implements UltrasonicController {
 
 	private final int bandCenter, bandWidth;
 	private final int motorStraight = 250, FILTER_OUT = 60;				// code changed
 	private final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.C;	
 	private int distance;
 	private int currentLeftSpeed;
 	private int filterControl;
 	private double turnCoefficient; 		// code changed
 	private int leftSpeed, rightSpeed;
 
 	public PController(int bandCenter, int bandWidth) {
 		//Default Constructor
 		this.bandCenter = bandCenter;
 		this.bandWidth = bandWidth;
 		leftMotor.setSpeed(motorStraight);
 		rightMotor.setSpeed(motorStraight);
 		leftMotor.forward();
 		rightMotor.forward();
 		currentLeftSpeed = 0;
 		filterControl = 0;
 		turnCoefficient = 5;	// code changed
 	}
 
 	/**
 	* This method process a movement based on the us distance passed in (P style)
 	*/
 	@Override
 	public void processUSData(int distance) {
 
 		// rudimentary filter
 		if (distance == 255 && filterControl < FILTER_OUT) {
 			// bad value, do not set the distance var, however do increment the filter value
 			filterControl ++;
 		} else if (distance == 255){
 			// true 255, therefore set distance to 255
 			this.distance = distance;
 		} else {
 			// distance went below 255, therefore reset everything.
 			filterControl = 0;
 			this.distance = distance;
 		}
 		
 		
 		// Wall on the left
 		int error = distance - this.bandCenter;
 		
 		/* adjust turn coefficient according to the value of the error */
 		
 		if (Math.abs(error) <= bandWidth){ 	// Within acceptable range
 			this.turnCoefficient = 0;
 			LCD.drawString("Within Bandwith", 0, 6);
 		}	
 		else if(error > 20){			// too far
 			this.turnCoefficient = (error > 150) ?  0.2 : 5;  // if way too far (i.e,large error), turnCoefficient=0.2
 			LCD.drawString("Too far", 0, 6);
 		}
 		else if(error < -8){			// too close
 			this.turnCoefficient = 35;	
 			LCD.drawString("Too close", 0, 6);
 		}
 		
 		/* adjust Wheels Speed  */
 		leftSpeed = this.adjustSpeed(error);
 		leftMotor.setSpeed(leftSpeed);
 		rightSpeed = this.adjustSpeed(-error);
 		rightMotor.setSpeed(rightSpeed);
 		
 		
 		
 	}
 
 
 	public int getLeftSpeed() {
 		return leftSpeed;
 	}
 
 
 	public int getRightSpeed() {
 		return rightSpeed;
 	}
 
 
 	@Override
 	public int readUSDistance() {
 		return this.distance;
 	}
 	/*
 	* Compute Adjusted speed: for positive error:  newSpeed < motorStraight
 	* 						  for negative error:  newSpeed > motorStraight
 	* make sure  Lab1.getMotorlow() <= newSpeed <= Lab1.getMotorhigh()
 	*/
 	private int adjustSpeed(int error){
 		int newSpeed = Math.max(  Lab1.getMotorlow() ,  motorStraight - (int)(turnCoefficient*error));
		newSpeed = Math.min(val,  Lab1.getMotorhigh());
 		return newSpeed;
 	}
 
 }
