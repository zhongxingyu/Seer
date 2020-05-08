 package raisa.simulator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import raisa.domain.robot.Robot;
 import raisa.util.RandomUtil;
 
 
 /**
  * 
  * @see http://rossum.sourceforge.net/papers/DiffSteer/
  * 
  */
 public class DifferentialDrive implements DriveSystem {
 	
 	private static final Logger log = LoggerFactory.getLogger(DifferentialDrive.class);
 
 	private float leftSpeed;
 	private float rightSpeed;
 	private final float axisWidth;
 	private final float wheelDiameter;
 	private float leftWheelDistanceSinceLastRead = 0.0f;
 	private float rightWheelDistanceSinceLastRead = 0.0f;	
 	
 	public DifferentialDrive(float axisWidth, float wheelDiameter) {
 		this.axisWidth = axisWidth;
 		this.wheelDiameter = wheelDiameter;
 	}
 
 	@Override
 	public void move(SimulatorState roverState, float timestep) {
 		double theta0 = Math.toRadians(roverState.getHeading());
 		
 		double wheelDistance = Math.PI * wheelDiameter; 
 		double rightTravelDistance = rightSpeed * wheelDistance * timestep;
 		double leftTravelDistance = leftSpeed * wheelDistance * timestep;
 		this.rightWheelDistanceSinceLastRead += rightTravelDistance;
 		this.leftWheelDistanceSinceLastRead += leftTravelDistance;
 
 		double avgTravelDistance = (rightTravelDistance + leftTravelDistance) / 2f;
 		double theta = (rightTravelDistance - leftTravelDistance) / axisWidth + theta0;
 		double newY = -avgTravelDistance * Math.cos(theta) + roverState.getPosition().y;
 		double newX = -avgTravelDistance * Math.sin(theta) + roverState.getPosition().x;
 
 		roverState.setHeading((float)Math.toDegrees(theta));
 		roverState.getPosition().setLocation(newX, newY);
 		log.trace("L: {} R:{}, H:{}, P:{}", new Object[]{leftSpeed, rightSpeed, roverState.getHeading(), roverState.getPosition()});	
 	}
 
 	/**
 	 * Rotations per second.
 	 */
 	@Override
 	public DriveSystem setLeftWheelSpeed(float speed) {
 		this.leftSpeed = speed;
 		return this;
 	}
 
 	/**
 	 * Rotations per second.
 	 */
 	@Override
 	public DriveSystem setRightWheelSpeed(float speed) {
 		this.rightSpeed = speed;
 		return this;
 	}
 
 	@Override
 	public int readLeftWheelEncoderTicks() {
 		float distancePerTick = (wheelDiameter * Robot.TICK_RADIANS / 2.0f);
 		int leftWheelTicks = (int)(leftWheelDistanceSinceLastRead / distancePerTick);
 		if (leftWheelTicks != 0) {
 			leftWheelDistanceSinceLastRead -= leftWheelTicks * distancePerTick;
 		}
 		return leftWheelTicks + getErrorTicks(leftWheelTicks);
 	}
 
 	@Override
 	public int readRightWheelEncoderTicks() {
 		float distancePerTick = (wheelDiameter * Robot.TICK_RADIANS / 2.0f);
 		int rightWheelTicks = (int)(rightWheelDistanceSinceLastRead / distancePerTick);
 		if (rightWheelTicks != 0) {
 			rightWheelDistanceSinceLastRead -= rightWheelTicks * distancePerTick;
 		}
 		return rightWheelTicks + getErrorTicks(rightWheelTicks);
 	}
 
 	private int getErrorTicks(int ticks) {
 		double rnd = RandomUtil.random();
 		if (rnd < 0.01d * ticks) {
 			return -1;
		} else if (rnd < 0.15d * ticks) {
 			return 1;
 		}
 		return 0;
 	}
 	
 }
