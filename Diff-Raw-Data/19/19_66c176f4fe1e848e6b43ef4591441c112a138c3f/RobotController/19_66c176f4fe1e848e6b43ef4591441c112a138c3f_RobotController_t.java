 package bs;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import comm.Message;
 import comm.messages.BSHeartbeat;
 import comm.messages.BSMotorSpeed;
 import comm.messages.RHeartbeat;
 
 /**
  * Encapsulates a robot controlled over a remote interface
  */
 public class RobotController implements MessageReceiver,
 		ConnectionStateListener {
 	private static final int HEARTBEAT_PUBLISH_PERIOD_MS = 250;
 
 	/**
 	 * The maximum forward motor speed
 	 */
 	public static final int MOTOR_SPEED_MAX_FWD = 127;
 
 	/**
 	 * The motor speed associated with a stationary robot
 	 */
 	public static final int MOTOR_SPEED_STOP = 0;
 
 	/**
 	 * The maximum reverse motor speed
 	 */
 	public static final int MOTOR_SPEED_MAX_REV = -127;
 
 	/**
 	 * A list of recent telemetry data received from the robot. They are ordered
 	 * by increasing received time
 	 */
	private final List<Telemetry> telemetry = new LinkedList<Telemetry>();
 
 	/**
 	 * A list of recent messages that have been sent to the robot. They are
 	 * ordered by increasing message ID
 	 */
	private final List<Message> sentMessages = new LinkedList<Message>();
 
 	/**
 	 * A list of recent messages received from the robot. They are ordered by
 	 * increasing message ID
 	 */
	private final List<Message> receivedMessages = new LinkedList<Message>();
 
 	/**
 	 * A connection with the robot
 	 */
 	private Connection connection;
 
 	/**
 	 * The Current Mode (SAFE or UNSAFE) of the robot being controlled
 	 */
 	private RobotMode currentMode;
 
 	/**
 	 * A list of callbacks to invoke when robot state changes
 	 */
 	private final List<RobotStateListener> robotStateListeners = new LinkedList<RobotStateListener>();
 
 	/**
 	 * This task repeatedly sends a heartbeat message to the robot every 250 ms
 	 */
 	private TimerTask heartbeatTask;
 
 	/**
 	 * This timer repeatedly kicks off the heartbeat task
 	 */
 	private Timer heartbeatTimer;
 
 	/**
 	 * A counter for the current message ID to send. It is incremented whenever
 	 * a new message is created
 	 */
 	private int nextMessageId;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param Connection
 	 *            The connection to use to communicate with the robot
 	 */
 	public RobotController(Connection connection) {
 		this.connection = connection;
		connection.addConnectionStateListener(this);
		connection.addMessageReceiver(this);
 
 		nextMessageId = 0;
 		heartbeatTask = new TimerTask() {
 			@Override
 			public void run() {
 				sendHeartbeat();
 			}
 		};
 
 		heartbeatTimer = new Timer();
 	}
 
 	private void sendMessage(Message m) {
 		connection.sendMessage(m);
 		nextMessageId++;
 	}
 
 	/**
 	 * Sends a message to the robot to set the wheel motor speed
 	 * 
 	 * @param leftSpeed
 	 *            the speed to set the left motor to
 	 * @param rightSpeed
 	 *            the speed to set the right motor to
 	 */
 	public void sendMove(int leftSpeed, int rightSpeed) {
 		Log.v(this, "sendMove(" + leftSpeed + ", " + rightSpeed + ")");
 		Message moveMessage = new BSMotorSpeed(nextMessageId, leftSpeed,
 				rightSpeed);
 		sendMessage(moveMessage);
 	}
 
 	/**
 	 * Sends a message to the robot to set the arm angle
 	 * 
 	 * @param armAngle
 	 *            the angle to set the arm to
 	 */
 	public void setArmAngle(int armAngle) {
 
 	}
 
 	/**
 	 * Sends a heartbeat message to the robot
 	 */
 	public void sendHeartbeat() {
 		Date currentTime = new Date();
 		Message heartbeat = new BSHeartbeat(nextMessageId,
 				currentTime.getTime());
 		sendMessage(heartbeat);
 	}
 
 	/**
 	 * Sends a message to the robot to turn safe mode on or off
 	 * 
 	 * @param safe
 	 *            true if turning on safe mode, false otherwise
 	 */
 	public void setSafeMode(boolean safe) {
 		// TODO implement this
 	}
 
 	/**
 	 * Get the latest telemetry object received from the robot.
 	 * 
 	 * Note that this returns null if no telemetry has been received yet!
 	 * 
 	 * @return a Telemetry object representing the latest telemetry data
 	 *         received from the robot, or null if no telemetry has been
 	 *         received
 	 */
 	public Telemetry getLatestTelemetry() {
 		if (telemetry.size() > 0) {
 			return telemetry.get(telemetry.size() - 1);
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public void receiveMessage(Message message) {
 		String name = message.getName();
 		if (name.equals(RHeartbeat.NAME)) {
 			processHeartbeatMessage(new RHeartbeat(message));
 		}
 	}
 
 	/**
 	 * Process a received heartbeat message, extrating the telemetry data and
 	 * adding it to the list {@code telemetry}. It then notifies robot state
 	 * listeners that the robot's state has changed.
 	 * 
 	 * @param message
 	 *            the heartbeat message that is to be processed
 	 */
 	private void processHeartbeatMessage(RHeartbeat message) {
 		Telemetry newTelemetry = message.getTelemetry();
 		telemetry.add(newTelemetry);
 
 		for (RobotStateListener listener : robotStateListeners) {
 			listener.stateChanged();
 		}
 	}
 
 	@Override
 	public void connectionEstablished() {
 		heartbeatTimer.schedule(heartbeatTask, 0, HEARTBEAT_PUBLISH_PERIOD_MS);
 	}
 
 	@Override
 	public void connectionLost() {
 		heartbeatTimer.cancel();
 	}
 
 	@Override
 	public void connectionAttemptFailed() {
 		// do nothing
 	}
 
 	/**
 	 * Registers a callback to listen for changes to robot state
 	 * 
 	 * @param listener
 	 *            The robot state listener to use
 	 */
 	public void addRobotStateListener(RobotStateListener listener) {
 		robotStateListeners.add(listener);
 	}
 
 	/**
 	 * @return The connection being used to communicate with the robot
 	 */
 	public Connection getConnection() {
 		return connection;
 	}
 
 	@Override
 	public void connecting() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
