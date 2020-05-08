 package master;
 
 import lejos.nxt.Motor;
 import lejos.nxt.NXTRegulatedMotor;
 
 /**
  * @author alex
  *
  */
 public class YARRobot implements IRobot {
 	
 	private NXTRegulatedMotor leftMotor = Motor.A;
 	private NXTRegulatedMotor rightMotor = Motor.B;
 	private double forwardSpeed, rotationSpeed;
 	
 	public YARRobot () {
 		this.forwardSpeed = 0;
 		this.rotationSpeed = 0;
 	}
 
 	public void setForwardSpeed(double speed) {
 		forwardSpeed = speed;
 		setSpeeds(forwardSpeed, rotationSpeed);
 	}
 
 	public void setRotationSpeed(double speed) {
 		rotationSpeed = speed;
 		setSpeeds(forwardSpeed, rotationSpeed);
 	}
 
 	public void setSpeeds(double forwardSpeed, double rotationalSpeed) {
 		double leftSpeed, rightSpeed;
 
 		this.forwardSpeed = forwardSpeed;
 		this.rotationSpeed = rotationalSpeed; 
 
 		leftSpeed = (forwardSpeed + rotationalSpeed * WHEEL_WIDTH * Math.PI / 360.0) *
 				180.0 / (LEFT_WHEEL_RADIUS * Math.PI);
 		rightSpeed = (forwardSpeed - rotationalSpeed * WHEEL_WIDTH * Math.PI / 360.0) *
 				180.0 / (RIGHT_WHEEL_RADIUS * Math.PI);
 
 		// set motor directions
 		if (leftSpeed > 0.0)
 			leftMotor.forward();
 		else {
 			leftMotor.backward();
 			leftSpeed = -leftSpeed;
 		}
 		
 		if (rightSpeed > 0.0)
 			rightMotor.forward();
 		else {
 			rightMotor.backward();
 			rightSpeed = -rightSpeed;
 		}
 		
 		// set motor speeds
 		if (leftSpeed > 900.0)
 			leftMotor.setSpeed(900);
 		else
 			leftMotor.setSpeed((int)leftSpeed);
 		
 		if (rightSpeed > 900.0)
 			rightMotor.setSpeed(900);
 		else
 			rightMotor.setSpeed((int)rightSpeed);
 	}
 
 	@Override
 	public void getDisplacementAndHeading(double [] data) {
 		int leftTacho, rightTacho;
 		leftTacho = leftMotor.getTachoCount();
 		rightTacho = rightMotor.getTachoCount();
 		
 		data[0] = (leftTacho * LEFT_WHEEL_RADIUS + rightTacho * RIGHT_WHEEL_RADIUS) *	Math.PI / 360.0;
		data[1] = (-leftTacho * LEFT_WHEEL_RADIUS + rightTacho * RIGHT_WHEEL_RADIUS) / WHEEL_WIDTH;
 	}
 	
 	public double getDisplacement() {
 		return (leftMotor.getTachoCount() * LEFT_WHEEL_RADIUS +
 				rightMotor.getTachoCount() * RIGHT_WHEEL_RADIUS) *
 				Math.PI / 360.0;
 	}
 	
 	public double getHeading() {
 		return (leftMotor.getTachoCount() * LEFT_WHEEL_RADIUS -
 				rightMotor.getTachoCount() * RIGHT_WHEEL_RADIUS) / WHEEL_WIDTH;
 	}
 
 	@Override
 	public NXTRegulatedMotor getLeftMotor() {
 		return leftMotor;
 	}
 
 	@Override
 	public NXTRegulatedMotor getRightMotor() {
 		return rightMotor;
 	}
 
 }
