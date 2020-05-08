 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package vikingrobotics.commands.shooter;
 
 import vikingrobotics.misc.Constants;
 import vikingrobotics.misc.Debug;
 import vikingrobotics.commands.CommandBase;
 
 public class ShooterMove extends CommandBase {
 	
 	private boolean hasFinished = false;
 	private boolean hasTimeout = false;
 	private boolean hasSpeed = false;
 	private boolean firstTimeStopped = false;
 	private double timeout;
 	private double speed = 0.0;
	public static class Direction {
		public final int value;
		public static final Direction kStop = new Direction(0);
		public static final Direction kUp = new Direction(1);
		public static final Direction kDown = new Direction(-1);
		private Direction(int value) {
			this.value = value;
		}
	}
	
	public ShooterMove() {
 		super("ShooterMove");
	}
 	
 	public ShooterMove(double speed) {
 		this();
 		this.speed = speed;
 		this.hasSpeed = true;
 	}
 	
 	public ShooterMove(double speed, double timeout) {
 		this(speed);
 		this.hasTimeout = true;
 		this.timeout = timeout;
 	}
 	
 	protected void initialize() {
 		hasFinished = false;
 		shooter.resetGyro();
 		if(hasTimeout)
 			setTimeout(timeout);
 	}
 
 	protected void execute() {
 		if(!hasSpeed) {
 			speed = oi.getGamePad().getAxis(Constants.kGamepadAxisDpadX);
 		}
 		if(speed > 0 && canMove()) {
 			shooter.moveUp();
 			firstTimeStopped = true;
 			Debug.println("[ShooterMove] moving up");
 		}
 		else if(speed < 0 && canMove()) {
 			shooter.moveDown();
 			firstTimeStopped = true;
 			Debug.println("[ShooterMove] moving down");
 		}
 		else {
 			shooter.moveStop();
 			shooter.resetGyro();
 			if (firstTimeStopped) {
 				firstTimeStopped = false;
 				Debug.println("[ShooterMove] changing angle");
 				shooter.changingAngle();
 			}
 		}
 	}
 	
 	public boolean canMove() {
 		return true;
 		//return (shooter.getGyroAngle() < 55 && shooter.getGyroAngle() > 25);
 	}
 	
 	protected boolean isFinished() {
 		return isTimedOut() || hasFinished;
 	}
 
 	protected void end() {
 		shooter.moveStop();
 		shooter.changingAngle();
 	}
 
 	protected void interrupted() {
 		Debug.println("[interrupted] " + getName());
 		shooter.moveStop();
 		shooter.changingAngle();
 	}
 
 }
