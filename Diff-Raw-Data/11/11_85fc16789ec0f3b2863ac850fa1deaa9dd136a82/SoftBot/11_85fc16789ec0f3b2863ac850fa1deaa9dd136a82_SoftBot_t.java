 package balle.simulator;
 
 import org.jbox2d.dynamics.Body;
 
 import balle.controller.Controller;
 import balle.misc.Globals;
 
 public class SoftBot implements Controller {
 
 	private boolean kick = false;
 
 	private float leftWheelSpeed = 0;
 	private float rightWheelSpeed = 0;
 
 	private Body body;
 
 	private float desiredAngle;
 
 	private boolean turningLeft;
 
 	private boolean rotating;
 
 	public float getLeftWheelSpeed() {
 		return leftWheelSpeed;
 	}
 
 	public float getRightWheelSpeed() {
 		return rightWheelSpeed;
 	}
 
 	@Override
 	public void backward(int speed) {
 		rotating = false;
 		leftWheelSpeed = -speed;
 		rightWheelSpeed = -speed;
 	}
 
 	@Override
 	public void forward(int speed) {
 		rotating = false;
 		leftWheelSpeed = speed;
 		rightWheelSpeed = speed;
 	}
 
 	@Override
 	public void floatWheels() {
 		rotating = false;
 		// TODO Changes behaviour of the robot.
 		// Not exactly sure how will behave?
 	}
 
 	@Override
 	public void stop() {
 		rotating = false;
 		leftWheelSpeed = 0;
 		rightWheelSpeed = 0;
 	}
 
 	/**
 	 * @param deg
 	 *            signed angle relative to the robots current angle, that the
 	 *            robot will rotate to (in place).
 	 * @param speed
 	 *            POSITIVE angular velocity in degrees/seconds. If a negative
 	 *            value is given, the robot will rotate in the opposite
 	 *            direction but end up in the same position as if speed was
 	 *            positive.
 	 */
 	@Override
 	public void rotate(int deg, int speed) {
 		float wheelVelocity = Globals.velocityToPower((speed
 				* Globals.ROBOT_TRACK_WIDTH * (float) Math.PI) / 360f);
 		// turning right
 		if (deg < 0) {
 			leftWheelSpeed = wheelVelocity;
 			rightWheelSpeed = -wheelVelocity;
 			// turning left
 		} else {
 			leftWheelSpeed = -wheelVelocity;
 			rightWheelSpeed = wheelVelocity;
 
 		}
 
 		desiredAngle = (body.getAngle() + ((float) Math.PI * deg / 180))
 				% (2 * (float) Math.PI);
 		turningLeft = deg < 0;
 
 		rotating = true;
 	}
 
 	@Override
 	public void setWheelSpeeds(int leftWheelSpeed, int rightWheelSpeed) {
 		rotating = false;
 		this.leftWheelSpeed = leftWheelSpeed;
 		this.rightWheelSpeed = rightWheelSpeed;
 		// System.out.println("setWheelSpeeds(): "+leftWheelSpeed+" "+rightWheelSpeed);
 
 	}
 
 	@Override
 	public int getMaximumWheelSpeed() {
 		return 720;
 	}
 
 	@Override
 	public void kick() {
 		kick = true;
 	}
 
 	@Override
 	public void penaltyKick() {
 		rotating = false;
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean isReady() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	public void setBody(Body body) {
 		this.body = body;
 	}
 
 	public float getDesiredAngle() {
 		return desiredAngle;
 	}
 
 	public boolean isTurningLeft() {
 		return turningLeft;
 	}
 
 	/**
 	 * return the angles to the desired angle. mod 180
 	 * 
 	 * @return
 	 */
 	public float deltaDesiredAngle() {
		float d = (float) ((body.getAngle()% (2 * (float)Math.PI)) - getDesiredAngle());
		// -2PI <= d <= 2PI
		// if |d| > PI then change direction of the angle
		if(Math.abs(d) > Math.PI) {
			if(d < 0) d += 2*Math.PI;
			else      d -= 2*Math.PI;
		}
 		if (d == Math.PI) {
 			return d;
 		}
		return d;
 	}
 
 	public boolean isRotating() {
 		return rotating;
 	}
 
 	public boolean isKicking() {
 		return kick;
 	}
 	
 	public void stopKicking() {
 		kick = false;
 	}
 
 }
