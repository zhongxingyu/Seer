 package master;
 
 import lejos.nxt.LCD;
 import lejos.nxt.Sound;
 
 /**
  * This is an abstract navigator. This is where we want to have the default
  * navigator code.
  * 
  * @author alex
  * 
  */
 public abstract class Navigator extends Thread implements INavigator {
 
	final static double DEG_ERR = 4.0, CM_ERR = 5.0;
 	final static int FORWARD_SPEED = 40, ROTATION_SPEED = 110;
 
 	protected Odometer odo;
 	protected boolean isNavigating;
 	protected IRobot robot;
 
 	protected Navigator(IRobot robot, Odometer odo) {
 		this.odo = odo;
 		this.robot = robot;
 		this.isNavigating = false;
 	}
 
 	@Override
 	public void travelTo(double x, double y) {
 		double minAng = 0;
 		double[] pos = new double[3];
 		this.odo.getPosition(pos);
 
 		double dy = y - pos[1];
 		double dx = x - pos[0];
 		// LCD.drawString("Turning !!    ", 0, 6);
 
 		if (dx > 0) {
 			minAng = Math.atan(dy / dx) * (180.0 / Math.PI);
 		} else if (dx < 0 && dy > 0) {
 			minAng = (Math.atan(dy / dx) + Math.PI) * (180.0 / Math.PI);
 		} else if (dx < 0 && dy < 0) {
 			minAng = (Math.atan(dy / dx) - Math.PI) * (180.0 / Math.PI);
 		}
 
 		// Rotate
 		turnTo(-minAng);
 
 		// LCD.drawString("Going Forward!!", 0, 6);
 		this.robot.setForwardSpeed(FORWARD_SPEED);
 
 		while (Math.abs(x - pos[0]) > CM_ERR || Math.abs(y - pos[1]) > CM_ERR) {
 			this.odo.getPosition(pos);
 		}
 		this.robot.setSpeeds(0, 0);
 	}
 
 	@Override
 	public void turnTo(double angle) {
 		isNavigating = true;
 		Sound.beep();
 
 		double[] pos = new double[3];
 		this.odo.getPosition(pos);
 		double error = minimizeAngle(angle - pos[2]);
 
 		// LCD.drawString("Angle:" + angle + "  ", 0, 4);
 		// LCD.drawString("Error:" + error + "   ", 0, 5);
 		this.odo.getPosition(pos);
 		error = minimizeAngle(angle - pos[2]);
 		
 		if (error > DEG_ERR && error <= 180) {
 			this.robot.getLeftMotor().setSpeed(ROTATION_SPEED);
 			this.robot.getLeftMotor().backward();
 			this.robot.getRightMotor().setSpeed(ROTATION_SPEED);
 			this.robot.getRightMotor().forward();			
 		}
 		if (error >= -180 && error < DEG_ERR) {
 			this.robot.getLeftMotor().setSpeed(ROTATION_SPEED);
 			this.robot.getLeftMotor().forward();
 			this.robot.getRightMotor().setSpeed(ROTATION_SPEED);
 			this.robot.getRightMotor().backward();			
 		}
 
 		while (Math.abs(error) > DEG_ERR) {
 			this.odo.getPosition(pos);
 			error = minimizeAngle(angle - pos[2]);
 		}
 		
 		this.robot.getLeftMotor().setSpeed(0);
 		this.robot.getRightMotor().setSpeed(0);
 
 		this.robot.setSpeeds(0, 0);
 	}
 
 	private double minimizeAngle(double error) {
 		error = error % (360);
 		if (error > 180) {
 			// Gives us small negative angle instead of large positive angle
 			error = error - 360;
 		} else if (error < -180) {
 			// Gives us small positive angle instead of large negative angle
 			error = error + 360;
 		}
 		return error;
 	}
 
 	@Override
 	public boolean isNavigating() {
 		return this.isNavigating;
 	}
 
 	protected static int convertAngle(double radius, double width, double angle) {
 		return convertDistance(radius, width * angle / 2);
 	}
 
 	protected static int convertDistance(double radius, double distance) {
 		return (int) ((180.0 * distance) / (Math.PI * radius));
 	}
 }
