 package raisa.simulator;
 
 import static java.lang.Math.round;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.math3.distribution.NormalDistribution;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import raisa.comms.Communicator;
 import raisa.comms.ControlMessage;
 import raisa.comms.SensorListener;
 import raisa.config.VisualizerConfig;
 import raisa.config.VisualizerConfigListener;
 import raisa.domain.WorldModel;
 import raisa.domain.robot.Robot;
 import raisa.domain.robot.RobotState;
 import raisa.util.RandomUtil;
 import raisa.util.Vector2D;
 import raisa.util.Vector3D;
 
 /**
  * Simulated Rover.
  * 
  */
 public class RobotSimulator implements SimulatorState, ServoScanListener, Communicator, VisualizerConfigListener, Runnable {
 	
 	private static final Logger log = LoggerFactory.getLogger(RobotSimulator.class);
 	private static final float SPEED_PER_GEAR = 0.04f;
 
 	/** degrees */
 	private float heading;
 	private Vector2D position;
 	private RotatingServo rotatingServo = new RotatingServo(this);
 	private DistanceScanner irScanner1 = new IRDistanceScanner();
 	private DistanceScanner sonarScanner1 = new SonarDistanceScanner();
 	private DistanceScanner irScanner2 = new IRDistanceScanner();
 	private DistanceScanner sonarScanner2 = new SonarDistanceScanner();
 	private DriveSystem driveSystem;
 	private WorldModel worldModel;
 	private List<SensorListener> sensorListeners = new ArrayList<SensorListener>();
 	private final long startTime = System.currentTimeMillis();
 	private int messageNumber = 1;
 
 	private NormalDistribution sensorDirectionNoise = RandomUtil.normalDistribution(0.0d, 0.5d);
 	private NormalDistribution headingNoise = RandomUtil.normalDistribution(0.0d, 0.5d);
 
 	private Thread simulatorThread;
 	private boolean simulatorActive = false;
 	
 	public RobotSimulator(Vector2D position, float heading, DriveSystem driveSystem, WorldModel worldModel) {
 		VisualizerConfig config = VisualizerConfig.getInstance();
 		
 		this.position = position;
 		this.heading = heading;
 		this.driveSystem = driveSystem;
 		this.worldModel = worldModel;
 		config.addVisualizerConfigListener(this);
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
 
 	public float getIRDistance1() {
 		return irScanner1.scanDistance(worldModel, this, rotatingServo.getHeading());
 	}
 
 	public float getSonarDistance1() {
 		return sonarScanner1.scanDistance(worldModel, this, rotatingServo.getHeading());
 	}
 
 	/**
 	 * Like compass direction but counter clockwise.
 	 */
 	@Override
 	public float getHeading() {
 		return heading;
 	}
 
 	@Override
 	public void setHeading(float heading) {
 		this.heading = heading;
 		while(this.heading < 0) {
 			this.heading += 360.0f;
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
 		float irDistance1 = irScanner1.scanDistance(worldModel, this, servoHeading);
 		float sonarDistance1 = sonarScanner1.scanDistance(worldModel, this, servoHeading);
 		float irDistance2 = irScanner2.scanDistance(worldModel, this, servoHeading + 180);
 		float sonarDistance2 = sonarScanner2.scanDistance(worldModel, this, servoHeading + 180);
 		int directionNoiseSample = (int)sensorDirectionNoise.sample();
 		if(irDistance1 > 0 ) {
 			reading.setIrDistance1(round(irDistance1));
 			reading.setIrDirection1(round(servoHeading) + 90 + directionNoiseSample);
 		}
 		if(sonarDistance1 > 0 ) {
 			reading.setSonarDistance1(round(sonarDistance1));
 			reading.setSonarDirection1(round(servoHeading) + 90 + directionNoiseSample);
 		}
 		if(irDistance2 > 0 ) {
 			reading.setIrDistance2(round(irDistance2));
 			reading.setIrDirection2(round(servoHeading) + 90 + 180 + directionNoiseSample);
 		}
 		if(sonarDistance2 > 0 ) {
 			reading.setSonarDistance2(round(sonarDistance2));
 			reading.setSonarDirection2(round(servoHeading) + 90 + 180 + directionNoiseSample);
 		}
 		
 		reading.setCompassHeading(360-round(heading - (int)headingNoise.sample()));
 		reading.setTimestamp(System.currentTimeMillis() - startTime).setMessageNumber(messageNumber++);
 		
 		Vector3D gyro = new Vector3D();
 		reading.setGyro(gyro);
 		gyro.setX((float) RandomUtil.nextGaussian());
 		gyro.setY((float) RandomUtil.nextGaussian());
 		gyro.setZ((float) RandomUtil.nextGaussian());
 
 		Vector3D acceleration = new Vector3D();
 		reading.setAcceleration(acceleration);
 		acceleration.setX((float) RandomUtil.nextGaussian());
 		acceleration.setY((float)(-9.81 + RandomUtil.nextGaussian()));
 		acceleration.setZ((float) RandomUtil.nextGaussian());
 		
		reading.setRightEncoder(driveSystem.readRightWheelEncoderTicks());
		reading.setLeftEncoder(driveSystem.readLeftWheelEncoderTicks());
 
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
 		if (simulatorActive) {
 			driveSystem.setLeftWheelSpeed(convertControlSpeed(message.getLeftSpeed(), message.isRawValues()));
 			driveSystem.setRightWheelSpeed(convertControlSpeed(message.getRightSpeed(), message.isRawValues()));
 		}
 	}
 
 	private float convertControlSpeed(int controlSpeed, boolean rawValue) {
 		if (rawValue) {
 			int[] speedPowerMap = ControlMessage.getSpeedPowerMap();
 			int absoluteSpeed = Math.abs(controlSpeed);			
 			for (int i=1; i < speedPowerMap.length; i++) {
 				if ((int)speedPowerMap[i] > absoluteSpeed) {
 					return ( controlSpeed > 0 ? 1 : -1 ) * SPEED_PER_GEAR * ( (i-1) + ( ((float)( absoluteSpeed - speedPowerMap[i-1] )) / ((float)( speedPowerMap[i] - speedPowerMap[i-1] )) ) );
 				}
 			}
 			return ( controlSpeed > 0 ? 1 : -1 ) * SPEED_PER_GEAR * ( speedPowerMap.length - 1);
 		} else {
 			return SPEED_PER_GEAR * controlSpeed;
 		}
 	}
 	
 	
 	public void reset() {
 		this.position = new Vector2D(0,0);
 		this.heading = 0;
 		this.driveSystem.setLeftWheelSpeed(0);
 		this.driveSystem.setRightWheelSpeed(0);
 	}
 	
 	@Override
 	public boolean connect() {
 		return true;
 	}
 
 	@Override
 	public void close() {
 		;
 	}	
 	
 	@Override
 	public void setActive(boolean active) {
 		;
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
 	
 	@Override
 	public void visualizerConfigChanged(VisualizerConfig config) {
 		
 		switch(config.getInputOutputTarget()) {
 		case REALTIME_SIMULATOR:
 			if (!simulatorActive) {
 				simulatorActive = true;
 				RobotState state = worldModel.getLatestState().getMeasuredState();
 				setPosition(new Vector2D(state.getPosition().x, state.getPosition().y));
 				setHeading(360-(float)Math.toDegrees(state.getHeading()));
 				simulatorThread = new Thread(this);				
 				simulatorThread.start();
 			}
 			break;
 		default:
 			if (simulatorActive) {
 				simulatorActive = false;
 				try {
 					Thread.sleep((int)(getTimeStepLengthInSeconds()*1000));
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 	}
 	
 	@Override	
 	public void run() {
 		log.info("Simulator thread starting");
 		long prevTickDuration = -1;
 		float timeStepLength = getTimeStepLengthInSeconds();
 		while(this.simulatorActive) {
 			try {
 				long maxTime = (long)(timeStepLength * 1000 / 4);
 				long timeToSleep;
 				if(prevTickDuration < 0) {
 					timeToSleep = maxTime;
 				} else {
 					timeToSleep = Math.max(0, maxTime - prevTickDuration);
 				}
 				Thread.sleep(timeToSleep);
 			} catch (InterruptedException e) {
 			}
 			long start = System.currentTimeMillis();
 			tick(timeStepLength);
 			prevTickDuration = System.currentTimeMillis() - start;
 		}
 		log.info("Simulator thread stopping");
 	}
 
 	private float getTimeStepLengthInSeconds() {
 		return 0.120f;
 	}
 	
 }
