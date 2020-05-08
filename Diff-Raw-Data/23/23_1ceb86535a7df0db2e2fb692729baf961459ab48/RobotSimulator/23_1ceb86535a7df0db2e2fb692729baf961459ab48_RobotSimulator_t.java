 package raisa.simulator;
 
 import static java.lang.Math.round;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import raisa.comms.Communicator;
 import raisa.comms.ControlMessage;
 import raisa.comms.SensorListener;
 import raisa.domain.Robot;
 import raisa.domain.WorldModel;
 import raisa.util.Vector2D;
 import raisa.util.Vector3D;
 
 /**
  * Simulated Rover.
  * 
  */
 public class RobotSimulator implements RobotState, ServoScanListener, Communicator {
 	private static final Logger log = LoggerFactory.getLogger(RobotSimulator.class);
 	/** degrees */
 	private float heading;
 	private Vector2D position;
 	private RotatingServo rotatingServo = new RotatingServo(this);
 	private DistanceScanner irScanner = new IRDistanceScanner();
 	private DistanceScanner sonarScanner = new SonarDistanceScanner();
 	private DriveSystem driveSystem;
 	private WorldModel worldModel;
 	private List<SensorListener> sensorListeners = new ArrayList<SensorListener>();
 	private final long startTime = System.currentTimeMillis();
 	private int messageNumber = 1;
 	public RobotSimulator(Vector2D position, float heading, DriveSystem driveSystem, WorldModel worldModel) {
 		this.position = position;
 		this.heading = heading;
 		this.driveSystem = driveSystem;
 		this.worldModel = worldModel;
 	}
 
 	public static RobotSimulator createRaisaInstance(Vector2D position, float heading, WorldModel worldModel) {
 		DifferentialDrive driveSystem = new DifferentialDrive(Robot.ROBOT_WIDTH, Robot.WHEEL_DIAMETER);
 		return new RobotSimulator(position, heading, driveSystem, worldModel);
 	}
 	
 	public RobotSimulator tick(float timestep) {
 		driveSystem.move(this, timestep);
 		rotateScanners(timestep);
 		return this;
 	}
 	
 	private void rotateScanners(float timestep) {
 		rotatingServo.rotate(timestep);
 	}
 
 	public float getIRDistance() {
 		return irScanner.scanDistance(worldModel, this, rotatingServo.getHeading());
 	}
 
 	public float getSonarDistance() {
 		return sonarScanner.scanDistance(worldModel, this, rotatingServo.getHeading());
 	}
 
 	@Override
 	public float getHeading() {
 		return heading;
 	}
 
 	@Override
 	public void setHeading(float heading) {
 		this.heading = heading % 360.0f;
 		while(this.heading < 0) {
 			this.heading += 360f;
 		}
 		this.heading = this.heading % 360.0f;
 	}
 
 	@Override
 	public Vector2D getPosition() {
 		return position;
 	}
 
 	@Override
 	public void setPosition(Vector2D position) {
 		this.position = position;
 	}
 
 	@Override
 	public void scan(float servoHeading) {
 		SensorReading reading = new SensorReading();
 		float irDistance = irScanner.scanDistance(worldModel, this, servoHeading);
 		float sonarDistance = sonarScanner.scanDistance(worldModel, this, servoHeading);
 		if(irDistance > 0 ) {
 			reading.setIrDistance(round(irDistance));
 			reading.setIrDirection(round(servoHeading) + 90);
 		}
 		if(sonarDistance > 0 ) {
 			reading.setSonarDistance(round(sonarDistance));
 			reading.setSonarDirection(round(servoHeading) + 90);
 		}
		reading.setCompassHeading(360-round(heading));
 		reading.setTimestamp(System.currentTimeMillis() - startTime).setMessageNumber(messageNumber++);
 		
 		Random random = new Random();
 		Vector3D gyro = new Vector3D();
 		reading.setGyro(gyro);
 		gyro.setX((float) random.nextGaussian());
 		gyro.setY((float) random.nextGaussian());
 		gyro.setZ((float) random.nextGaussian());
 
 		Vector3D acceleration = new Vector3D();
 		reading.setAcceleration(acceleration);
 		acceleration.setX((float) random.nextGaussian());
 		acceleration.setY((float)(-9.81 + random.nextGaussian()));
 		acceleration.setZ((float) random.nextGaussian());
 
 		sendSendorReading(reading);
 	}
 	
 	private void sendSendorReading(SensorReading sensorReading) {
 		log.trace("Sendor reading: {}", sensorReading.toString());
 
 		for (SensorListener sensorListener : sensorListeners) {
 			sensorListener.sampleReceived(sensorReading.toString());
 		}
 	}
 
 	/**
 	 * Called when simulator receives a control message
 	 */
 	@Override
 	public void sendPackage(ControlMessage message) {
 		driveSystem.setLeftWheelSpeed(convertControlSpeed(message.getLeftSpeed()));
 		driveSystem.setRightWheelSpeed(convertControlSpeed(message.getRightSpeed()));
 	}
 
 	private float convertControlSpeed(int controlSpeed) {
 		// TODO 255 is full speed, convert it to about 3/s
 		return controlSpeed/255.0f * 200f;
 	}
 	
 	
 	public void reset() {
 		this.position = new Vector2D(-100,0);
 		this.heading = 0;
 		this.driveSystem.setLeftWheelSpeed(0);
 		this.driveSystem.setRightWheelSpeed(0);
 	}
 	
 	@Override
 	public boolean connect() {
 		return true;
 	}
 
 	@Override
 	public Communicator addSensorListener(SensorListener ... sensorListeners) {
 		for(SensorListener sensorListener: sensorListeners) {
 			this.sensorListeners.add(sensorListener);
 		}
 		return this;
 	}
 
 	@Override
 	public Communicator removeSensorListener(SensorListener ... sensorListeners) {
 		for(SensorListener sensorListener: sensorListeners) {
 			this.sensorListeners.remove(sensorListener);
 		}
 		return this;
 	}
 }
