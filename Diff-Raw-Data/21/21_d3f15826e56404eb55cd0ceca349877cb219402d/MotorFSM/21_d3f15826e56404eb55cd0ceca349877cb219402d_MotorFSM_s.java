 package se.purplescout.purplemow.core.fsm;
 
 import java.io.IOException;
 
 import se.purplescout.purplemow.core.ComStream;
 import se.purplescout.purplemow.core.Constants;
 import se.purplescout.purplemow.core.GuiLogCallback;
 import se.purplescout.purplemow.core.MotorController;
 import se.purplescout.purplemow.core.MotorController.Direction;
 import se.purplescout.purplemow.core.fsm.event.MainFSMEvent;
 import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent;
 import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent.EventType;
 import android.util.Log;
 
 public class MotorFSM extends AbstractFSM<MotorFSMEvent> {
 
 	private enum State {
 		STOPPED, MOVING_FWD, TURNING_LEFT, BACKING_UP, TURNING_RIGHT
 	}
 
 	private State state = State.STOPPED;
 	private MotorController motorController;
 	private final GuiLogCallback logCallback;
 	private AbstractFSM<MainFSMEvent> mainFSM;
 
 	@Override
 	public void shutdown() {
 		try {
 			stopMotors();
 		} catch (IOException e) {
 			Log.e(this.getClass().getCanonicalName(), e.getMessage(), e);
 		} finally {
 			super.shutdown();
 		}
 	}
 
 	public MotorFSM(ComStream comStream, GuiLogCallback logCallback) {
 		this.logCallback = logCallback;
 		this.motorController = new MotorController(comStream);
 	}
 
 	public void setMainFSM(AbstractFSM<MainFSMEvent> fsm) {
 		this.mainFSM = fsm;
 	}
 
 	@Override
 	protected void handleEvent(MotorFSMEvent event) {
 
 		int value = event.getValue();
 
 		logToTextView("Eventtype: " + event.getEventType().name() + " value is: " + value);
 		value = value * 85;
 		if (value > Constants.FULL_SPEED) {
 			value = Constants.FULL_SPEED;
 		} else if (value < 0) {
 			value = 0;
 			logToTextView("Setting value to 0 ");
 		}
 
 		try {
 			switch (event.getEventType()) {
 			case MOVE_FWD:
 				moveForward(value);
 				mainFSM.queueEvent(new MainFSMEvent(MainFSMEvent.EventType.STARTED_MOWING));
 				break;
 			case REVERSE:
 				backUp(value);
 				break;
 			case TURN_LEFT:
 				turnLeft(value);
 				break;
 			case TURN_RIGHT:
 				turnRight(value);
 				break;
 			case STOP:
 				stopMotors();
 				break;
 			case EMERGENCY_STOP:
 				stopMotors();
 				System.exit(1);
 				break;
 			case MOW:
 				cutterEngine(value);
 			default:
 				break;
 			}
 		} catch (IOException e) {
 			logToTextView(e.getMessage());
 		}
 	}
 
 	private void stopMotors() throws IOException {
 		motorController.move(0);
 		changeState(State.STOPPED);
 	}
 
 	private void moveForward(int value) throws IOException {
 		if (state == State.STOPPED || state == State.MOVING_FWD) {
 			motorController.setDirection(Direction.FORWARD);
 			motorController.move(value);
 			changeState(State.MOVING_FWD);
 		} else {
 			queueEvent(new MotorFSMEvent(EventType.STOP));
			queueDelayedEvent(new MotorFSMEvent(EventType.MOVE_FWD), 500);
 		}
 	}
 
 	private void backUp(int value) throws IOException {
 		if (state == State.STOPPED || state == State.BACKING_UP) {
 			motorController.setDirection(Direction.BACKWARD);
 			motorController.move(value);
 			changeState(State.BACKING_UP);
 		} else {
 			queueEvent(new MotorFSMEvent(EventType.STOP));
			queueDelayedEvent(new MotorFSMEvent(EventType.REVERSE), 500);
 		}
 	}
 
 	private void turnLeft(int value) throws IOException {
 		if (state == State.STOPPED || state == State.TURNING_LEFT) {
 			motorController.setDirection(Direction.LEFT);
 			motorController.move(value);
 			changeState(State.TURNING_LEFT);
 		} else {
 			queueEvent(new MotorFSMEvent(EventType.STOP));
			queueDelayedEvent(new MotorFSMEvent(EventType.TURN_LEFT), 500);
 		}
 	}
 
 	private void turnRight(int value) throws IOException {
 		if (state == State.STOPPED || state == State.TURNING_RIGHT) {
 			motorController.setDirection(Direction.RIGHT);
 			motorController.move(value);
 			changeState(State.TURNING_RIGHT);
 		} else {
 			queueEvent(new MotorFSMEvent(EventType.STOP));
			queueDelayedEvent(new MotorFSMEvent(EventType.TURN_RIGHT), 500);
 		}
 	}
 
 	private void cutterEngine(int value) throws IOException {
 		motorController.runCutter(value);
 	}
 
 	private void changeState(State newState) {
 		String aMessage = "Change state from " + state + ", to " + newState;
 		logToTextView(aMessage);
 
 		state = newState;
 	}
 
 	private void logToTextView(final String msg) {
 		Log.d(this.getClass().getName(), msg + " ");
 		logCallback.post(msg);
 	}
 
 }
