 package strategies.wall_follower.without_sweeping;
 
 import static robot.Platform.ENGINE;
 import lejos.nxt.Motor;
 import lejos.nxt.NXTRegulatedMotor;
 import strategies.util.ChildStrategy;
 
 public class DetectCollisionStrategy extends ChildStrategy {
 	private final int LAST_VALUES_COUNT;
 	private final int COLLISION_PERCENTAGE;
 	private int[][] lastValues;
 
 	private long endAccelerationTime;
 	private final long ACCELERATION_TIME;
 
 	private boolean foundWall;
 
 	private enum Side {
 		LEFT(0), RIGHT(1);
 
 		private int value;
 
 		private Side(int value) {
 			this.value = value;
 		}
 
 		public int getValue() {
 			return value;
 		}
 
 		public static Side valueOf(int i) {
 			switch (i) {
 			case 0:
 				return LEFT;
 			case 1:
 				return RIGHT;
 			default:
 				throw new IllegalStateException(
 						"Side only has to values. 'i' was: " + i);
 			}
 		}
 	}
 
 	/**
 	 * Detect collisions via tacho count difference (motor speed).
 	 * 
 	 * @param valueCount
 	 *            The number of values used to calculate the average motor
 	 *            speed. If this value is big, there will be less false
 	 *            positives. Should be around 5.
 	 * @param sensitivity
 	 *            The percentage of the tacho count, which will trigger as a
 	 *            collision. Should be around 95.
 	 * @param accelerationTime
 	 *            Time in which no collision is detected (while accelerating)
 	 *            (in ms)
 	 */
 	public DetectCollisionStrategy(int valueCount, int sensitivity,
 			int accelerationTime) {
 		LAST_VALUES_COUNT = valueCount;
 		COLLISION_PERCENTAGE = sensitivity;
 		ACCELERATION_TIME = accelerationTime;
 		lastValues = new int[2][LAST_VALUES_COUNT];
 	}
 
 	private enum State {
 		START, ACCELERATING, DRIVING, WALL_FOUND, STOPPING
 	}
 
 	private static State currentState;
 
 	private static final NXTRegulatedMotor LEFT_MOTOR = Motor.A;
 	private static final NXTRegulatedMotor RIGHT_MOTOR = Motor.B;
 
 	private static int[] oldTachoCount = new int[2];
 
 	private int averageSpeed(Side side) {
 		int average = 0;
 		for (int i : lastValues[side.getValue()])
 			average += i;
 		return average / LAST_VALUES_COUNT;
 	}
 
 	private int[] tachoDiff = new int[2];
 
 	private static final int INITIAL_VALUE = 0;
 
 	@Override
 	protected void childInit() {
 		currentState = State.START;
 		for (int i = 0; i < LAST_VALUES_COUNT; ++i) {
 			for (int j = 0; j < 2; ++j) {
 				lastValues[j][i] = INITIAL_VALUE;
 			}
 		}
 		foundWall = false;
 	}
 
 	private State checkState() {
 		// Syso
 //		for (int side = 0; side < 2; ++side)
 //			System.out.print(tachoDiff[side] + " < " + averageSpeed(Side.valueOf(side)) + ", ");
 //		System.out.println();
 		State newState = currentState;
 		switch (currentState) {
 		case START:
 			newState = State.ACCELERATING;
 			break;
 		case ACCELERATING:
 			if(System.currentTimeMillis() > endAccelerationTime)
 				newState = State.DRIVING;
 			break;
 		case DRIVING:
 			for (int side = 0; side < 2; ++side)
 				if (tachoDiff[side] < (averageSpeed(Side.valueOf(side)) * COLLISION_PERCENTAGE) / 100) {
 					System.out.println("Wall found: " + averageSpeed(Side.LEFT)
 							+ " / " + averageSpeed(Side.RIGHT));
 					newState = State.WALL_FOUND;
 				}
 			break;
 		case WALL_FOUND:
 			newState = State.STOPPING;
 			break;
 		case STOPPING:
 			break;
 		}
 //		System.out.println("State: " + newState);
 		return newState;
 	}
 
 	@Override
 	public void check() {
 		int[] newTachoCount = new int[2];
 		newTachoCount[0] = LEFT_MOTOR.getTachoCount();
 		newTachoCount[1] = RIGHT_MOTOR.getTachoCount();
 
 		// TODO SB move one line down?
 		currentState = checkState();
 		for (int j = 0; j < 2; ++j) {
 			tachoDiff[j] = (newTachoCount[j] - oldTachoCount[j]);
 		}
 
 		switch (currentState) {
 		case START:
			endAccelerationTime = System.currentTimeMillis() + ACCELERATION_TIME;
 			break;
 		case ACCELERATING:
 			// don't detect collisions
 			System.out.println("Accelerating  : " + tachoDiff[Side.LEFT.getValue()]
 					+ " , " + tachoDiff[Side.RIGHT.getValue()]);
 			break;
 		case DRIVING:
 			System.out.println("Moving :        " + tachoDiff[Side.LEFT.getValue()]
 					+ " , " + tachoDiff[Side.RIGHT.getValue()]);
 			break;
 		case WALL_FOUND:
 			System.out.println("Stopping:       " + tachoDiff[Side.LEFT.getValue()]
 					+ " , " + tachoDiff[Side.RIGHT.getValue()]);
 			foundWall = true;
 			break;
 		case STOPPING:
 			break;
 		}
 
 		for (int j = 0; j < 2; ++j) {
 			for (int i = 0; i < LAST_VALUES_COUNT - 1; ++i) {
 				lastValues[j][i] = lastValues[j][i + 1];
 			}
 			lastValues[j][LAST_VALUES_COUNT - 1] = tachoDiff[j];
 			oldTachoCount[j] = newTachoCount[j];
 		}
 	}
 
 	@Override
 	public boolean willStart() {
 		return foundWall;
 	}
 
 	@Override
 	public boolean isStopped() {
 		return true;
 	}
 
 	@Override
 	public void work() {
 		System.out.println("stopping engine");
 		ENGINE.stop();
 	}
 
 }
