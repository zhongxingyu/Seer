 package strategies.sections;
 
 import bluetooth.Gate;
 import robot.Platform;
 import strategies.Strategy;
 
 /**
  * A very simple gate strategy. Right now the robot just stops a few
  * centimeters in front of the gate, sends the required bluetooth commands
  * and moves on. If there is enough time left, a wall follower strategy should
  * be used to align the robot.
  * 
  * Experiments showed that there is no need to wait for the gate to open. It's
  * by far fast enough to just drive through.
  */
 public class GateStrategy extends Strategy {
 	public enum State {
 		APPROACH,
 		WAIT_FOR_CONNECTION,
 		PASS
 	}
 
 	private State currentState;
 	
 	public State checkStateTransition() {
 		switch(currentState) {
 		case APPROACH:
 			if (Platform.HEAD.getDistance() < 20) {
 				Platform.ENGINE.stop();
 				if (Gate.getInstance().isConnected()) {
 					Gate.getInstance().open();
 					Platform.ENGINE.move(1000);
 					return State.PASS;
 				} else {
 					return State.WAIT_FOR_CONNECTION;
 				}
 			}
 			break;
 		case WAIT_FOR_CONNECTION:
 			if (Gate.getInstance().isConnected()) {
 				Gate.getInstance().open();
 				Platform.ENGINE.move(1000);
 				Gate.getInstance().disconnect();
 				return State.PASS;
 			}
 			break;
 		case PASS:
 			break;
 		default:
 			break;
 		}
 		
 		return currentState;
 	}
 
 	@Override
 	protected void doInit() {
 		currentState = State.APPROACH;
 		Gate.getInstance().connect();
 		Platform.ENGINE.move(1000);
		Platform.HEAD.moveTo(0, true);
 	}
 
 	@Override
 	protected void doRun() {
 		State oldState = currentState;
 		currentState = checkStateTransition();
 		if (oldState != currentState) {
 			System.out.println(currentState.toString() + " -> " + currentState.toString());
 		}
 	}
 
 }
