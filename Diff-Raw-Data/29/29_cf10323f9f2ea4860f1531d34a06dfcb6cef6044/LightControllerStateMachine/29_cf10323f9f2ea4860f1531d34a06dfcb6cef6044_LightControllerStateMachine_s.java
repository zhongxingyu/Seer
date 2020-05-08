 package SecurityLightController;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This class implements a state machine that can be used to control a security
  * light.
  * 
  * @author schilling
  * 
  */
 public class LightControllerStateMachine implements
 		LightControllerCommandInterface {
 
 	/**
 	 * This variable holds a list of observers. Whenever a state changes, the
 	 * observers are updated with the state change.
 	 */
 	private List<LightControllerStateMachineObserverInterface> observers = new ArrayList<LightControllerStateMachineObserverInterface>();
 
 	/**
 	 * This variable holds the current state for the state machine.
 	 */
 	private int currentState;
 
 	/**
 	 * This variable holds the light which is to be controlled by this state
 	 * machine.
 	 */
 	private LightDeviceInterface light;
 
 	/**
 	 * This variable is the instance of the timer that is to be used for timed
 	 * activities.
 	 */
 	private LightTimerInterface tmr;
 
 	/**
 	 * This value is used for keeping track of the Intrusion detected substates.
 	 */
 	private final static int INTRUSION_DETECTED_LAMP_ON = 1;
 
 	/**
 	 * This value is used for keeping track of the Intrusion detected substates.
 	 */
 	private final static int INTRUSION_DETECTED_LAMP_OFF = 2;
 
 	/**
 	 * This variable holds the substate for the intrusion detected state.
 	 */
 	private int intrusionDetectionStateVariable;
 
 	/**
 	 * This is the default constructor, which will instantiate a new instance of
 	 * this class.
 	 */
 	public LightControllerStateMachine() {
 		currentState = LightControllerStateMachineObserverInterface.LAMP_OFF_DAYLIGHT;
 	}
 
 	/**
 	 * This method will set the light that is to be controlled by this state
 	 * machine.
 	 * 
 	 * @param light
 	 *            This is the instance of the light that is to be directly
 	 *            controlled.
 	 */
 	public void setLight(LightDeviceInterface light) {
 		this.light = light;
 	}
 
 	/**
 	 * This method will set an instance of the timer that is to be used with
 	 * this class.
 	 * 
 	 * @param tmr
 	 *            This is the timer instance.
 	 */
 	public void setTmr(LightTimerInterface tmr) {
 		this.tmr = tmr;
 	}
 
 	/**
 	 * This method will be invoked to update the observers
 	 * 
 	 * @param state
 	 *            This is the new state.
 	 */
 	private void updateState(int state) {
 		for (LightControllerStateMachineObserverInterface obs : this.observers) {
 			obs.updateLightState(state);
			obs.updateLightState(state);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeSecurityLightController.LightControllerCommandInterface#subscribe(
 	 * SecurityLightController.LightControllerStateMachineObserverInterface)
 	 */
 	public void subscribe(LightControllerStateMachineObserverInterface obs) {
 		this.observers.add(obs);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeSecurityLightController.LightControllerCommandInterface#unsubscribe(
 	 * SecurityLightController.LightControllerStateMachineObserverInterface)
 	 */
 	public void unsubscribe(LightControllerStateMachineObserverInterface obs) {
 		this.observers.remove(obs);
 
 	}
 
 	/**
 	 * This method will process exit conditions based upon leaving a state.
 	 * 
 	 * @param presentState
 	 *            This is the present state. It will be used to determine which
 	 *            exit actions to invoke.
 	 */
 	protected void handleExitConditions(int presentState) {
 		// Based on the present state, switch and invoke the exit actions.
 		switch (presentState) {
 		case LightControllerStateMachineObserverInterface.LAMP_OFF_DAYLIGHT:
 			// No exit actions exist.
 			break;
 
 		case LightControllerStateMachineObserverInterface.LAMP_ON_FULL_BRIGHTNESS:
 			// No exit actions exist.
 			break;
 
 		case LightControllerStateMachineObserverInterface.LAMP_OFF_NIGHTIME:
 			// No exit actions exist.
 			break;
 
 		case LightControllerStateMachineObserverInterface.LAMP_ON_NIGHTIME_BRIGHTNESS:
 			// No exit actions exist.
 			break;
 
 		case LightControllerStateMachineObserverInterface.MOTION_DETECTED:
 			// No exit actions exist.
 			break;
 
 		case LightControllerStateMachineObserverInterface.INTRUSION_DETECTED:
 			light.turnLightOff();
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * This method will process entry conditions based upon entering a state.
 	 * 
 	 * @param presentState
 	 *            This is the present state. It will be used to determine which
 	 *            exit actions to invoke.
 	 */
 	protected void handleEntryConditions(int presentState) {
 		switch (presentState) {
 		case LightControllerStateMachineObserverInterface.LAMP_OFF_DAYLIGHT:
 			light.turnLightOff();
 			break;
 
 		case LightControllerStateMachineObserverInterface.LAMP_ON_FULL_BRIGHTNESS:
 			light.turnLightOnFullBrightness();
 			break;
 
 		case LightControllerStateMachineObserverInterface.LAMP_OFF_NIGHTIME:
 			light.turnLightOff();
 			break;
 
 		case LightControllerStateMachineObserverInterface.LAMP_ON_NIGHTIME_BRIGHTNESS:
 			light.turnLightOnNightimeBrightness();
 			break;
 
 		case LightControllerStateMachineObserverInterface.MOTION_DETECTED:
 			tmr.startTimer(5);
 			light.turnLightOnFullBrightness();
 			//break;
 
 		case LightControllerStateMachineObserverInterface.INTRUSION_DETECTED:
 			// Initialize the substate appropriately.
 			intrusionDetectionStateVariable = INTRUSION_DETECTED_LAMP_ON;
 
 			// Start the timer.
 			tmr.startTimer(1);
 
 			// Adjust the light setting.
 			light.turnLightOnFullBrightness();
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * SecurityLightController.LightControllerCommandInterface#signalAction(int)
 	 */
 	public void signalAction(int request) {
 
 		// This is a local variable which has the present state of the system.
 		int presentState = this.currentState;
 
 		// This is the variable which holds the destination state for the
 		// system.
 		// We will determine it through the case statement.
 		int destinationState = presentState;
 		// This variable will indicate if a state change is necessary.
 		boolean stateChange = true;
 
 		// This switch state will determine the destination state that we need
 		// to enter. Based on that, make the correct state changes.
 		switch (presentState) {
 		case LightControllerStateMachineObserverInterface.LAMP_OFF_DAYLIGHT:
 			if (request == LightControllerCommandInterface.MANUAL_SWITCH_ON) {
 				destinationState = LightControllerStateMachineObserverInterface.LAMP_ON_FULL_BRIGHTNESS;
 				stateChange = true;
 			} else if (request == LightControllerCommandInterface.LIGHT_SENSOR_DARKENED) {
 				destinationState = LightControllerStateMachineObserverInterface.LAMP_OFF_NIGHTIME;
 				stateChange = true;
 			} else {
 				// No state change here.
 				
 			}
 			break;
 		case LightControllerStateMachineObserverInterface.LAMP_ON_FULL_BRIGHTNESS:
 			if (request == LightControllerCommandInterface.MANUAL_SWITCH_OFF) {
 				destinationState = LightControllerStateMachineObserverInterface.LAMP_OFF_DAYLIGHT;
 				stateChange = true;
 			} else if (request == LightControllerCommandInterface.LIGHT_SENSOR_DARKENED) {
 				destinationState = LightControllerStateMachineObserverInterface.LAMP_ON_NIGHTIME_BRIGHTNESS;
 				stateChange = true;
 			} else {
 				// No state change here.
 				
 			}
 			break;
 
 		case LightControllerStateMachineObserverInterface.LAMP_OFF_NIGHTIME:
 			if (request == LightControllerCommandInterface.MOTION_DETECTED) {
 				destinationState = LightControllerStateMachineObserverInterface.MOTION_DETECTED;
 				stateChange = true;
 			} else if (request == LightControllerCommandInterface.MANUAL_SWITCH_ON) {
 				destinationState = LightControllerStateMachineObserverInterface.LAMP_ON_NIGHTIME_BRIGHTNESS;
 				stateChange = true;
 			} else if (request == LightControllerCommandInterface.SECURITY_ALARM_TRIPPED) {
 				destinationState = LightControllerStateMachineObserverInterface.INTRUSION_DETECTED;
 				stateChange = true;
 			} else if (request == LightControllerCommandInterface.LIGHT_SENSOR_LIGHTENED) { // FIXED
 																							// -
 																							// this
 																							// case
 																							// was
 																							// missing
 				destinationState = LightControllerStateMachineObserverInterface.LAMP_OFF_DAYLIGHT;
 				stateChange = true;
 			} else {
 				// No state change here.
 				
 			}
 			break;
 
 		case LightControllerStateMachineObserverInterface.LAMP_ON_NIGHTIME_BRIGHTNESS:
 			if (request == LightControllerCommandInterface.MANUAL_SWITCH_OFF) {
 				destinationState = LightControllerStateMachineObserverInterface.LAMP_OFF_NIGHTIME;
 				stateChange = true;
 			} else if (request == LightControllerCommandInterface.LIGHT_SENSOR_LIGHTENED) {
 				destinationState = LightControllerStateMachineObserverInterface.LAMP_ON_FULL_BRIGHTNESS;
 				stateChange = true;
 			} else {
 				// No state change here.
 				
 			}
 			break;
 
 		case LightControllerStateMachineObserverInterface.MOTION_DETECTED:
 			if (request == LightControllerCommandInterface.SECURITY_ALARM_TRIPPED) {
 				destinationState = LightControllerStateMachineObserverInterface.INTRUSION_DETECTED;
 				stateChange = true;
 			} else if (request == LightControllerCommandInterface.LAMP_TIMER_EXPIRED) {
 				destinationState = LightControllerStateMachineObserverInterface.LAMP_OFF_NIGHTIME;
 				stateChange = true;
 			} else {
 				// No state change here.
 				
 			}
 			break;
 
 		case LightControllerStateMachineObserverInterface.INTRUSION_DETECTED:
 			if (request == LightControllerCommandInterface.ALARM_CLEARED) {
 				destinationState = LightControllerStateMachineObserverInterface.LAMP_OFF_NIGHTIME;
 				stateChange = true;
 			} else if (request == LightControllerCommandInterface.LAMP_TIMER_EXPIRED) {
 				if (intrusionDetectionStateVariable == INTRUSION_DETECTED_LAMP_ON) {
 					// Toggle the variable to change the substate.
 					intrusionDetectionStateVariable = INTRUSION_DETECTED_LAMP_OFF;
 
 					// Start the timer.
 					tmr.startTimer(1);
 
 					// Adjust the light setting.
 					light.turnLightOff();
 				} else if (intrusionDetectionStateVariable == INTRUSION_DETECTED_LAMP_OFF) {
 					// Toggle the variable to change the substate.
 					intrusionDetectionStateVariable = INTRUSION_DETECTED_LAMP_ON;
 
 					// Start the timer.
 					tmr.startTimer(1);
 
 					// Adjust the light setting.
 					light.turnLightOnFullBrightness();
 				} else {
 					// Something went wrong. Start back with the initial
 					// conditions.
 					// Toggle the variable to change the substate.
 					intrusionDetectionStateVariable = INTRUSION_DETECTED_LAMP_ON;
 
 					// Start the timer.
 					tmr.startTimer(1);
 
 					// Adjust the light setting.
 					light.turnLightOnFullBrightness();
 
 				}
 				
 			}
 
 		default:
 			break;
 		}
 
 		// Now determine if an actual state change has occured. If so, invoke
 		// the exit action on the given state as well as the entry action for
 		// the new state.
 		if (stateChange = true) {
 			// Invoke exit actions.
 			handleExitConditions(presentState);
 
 			// Invoke entry actions.
 			handleEntryConditions(destinationState);
 
 			// Update the state variable.
 			this.currentState = destinationState;
 
 			// Update the observers.
 			updateState(destinationState);
 		}
 	}
 
 	/**
 	 * This method will return the current state of the Light controller. It is
 	 * only intended to be used for testing. Thus, the protected attribute.
 	 * 
 	 * @return This will return the current state of the light controller.
 	 */
 	protected int getCurrentState() {
 		return currentState;
 	}
 
 	/**
 	 * This method will set the current state of the light controller. It is
 	 * intended to only be used for testing, as it may or may not properly
 	 * invoke any entry or exit conditions.
 	 * 
 	 * @param currentState
 	 *            This is the desired state for the light controller.
 	 */
 	protected void setCurrentState(int currentState) {
 		this.currentState = currentState;
 	}
 
 }
