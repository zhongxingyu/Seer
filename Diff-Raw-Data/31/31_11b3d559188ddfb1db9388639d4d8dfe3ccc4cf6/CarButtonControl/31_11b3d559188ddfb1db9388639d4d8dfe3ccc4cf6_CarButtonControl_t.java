 /*
  * 18-649 Fall 2013
  * Group 22
  * David Chow davidcho
  * Brody Anderson bcanders
  * Yang Liu yangliu2
  * Jeffrey Lau jalau
  * CarButtonControl.java
  * @author: David Chow (davidcho)
  */
 /**
  * This CarButtonControl uses car calls and car lights, so it can be instantiated for 
  * any [floor,hallway] combination.
  */
 
 package simulator.elevatorcontrol;
 
 import jSimPack.SimTime;
 import simulator.elevatorcontrol.Utility.AtFloorArray;
 import simulator.elevatormodules.AtFloorCanPayloadTranslator;
 import simulator.elevatormodules.DoorClosedCanPayloadTranslator;
 import simulator.framework.Controller;
 import simulator.framework.Direction;
 import simulator.framework.Hallway;
 import simulator.framework.ReplicationComputer;
 import simulator.framework.Side;
 import simulator.payloads.CanMailbox;
 import simulator.payloads.CanMailbox.ReadableCanMailbox;
 import simulator.payloads.CanMailbox.WriteableCanMailbox;
 import simulator.payloads.CarCallPayload;
 import simulator.payloads.CarCallPayload.ReadableCarCallPayload;
 import simulator.payloads.CarLightPayload;
 import simulator.payloads.CarLightPayload.WriteableCarLightPayload;
 import simulator.payloads.translators.BooleanCanPayloadTranslator;
 
 public class CarButtonControl extends Controller {
 
     /***************************************************************************
      * Declarations
      **************************************************************************/
     //local physical state
 	// Inputs:
     private ReadableCarCallPayload localCarCall;
     // Outputs:
     private WriteableCarLightPayload localCarLight;
     
     //network interface
     // Input Mailbox, then Translator:
 		// receive mAtFloor[f,b]
     private ReadableCanMailbox networkAtFloor;
     private AtFloorCanPayloadTranslator mAtFloor;
     	// receive mDesiredFloor
     private ReadableCanMailbox networkDesiredFloor;
     private DesiredFloorCanPayloadTranslator mDesiredFloor;
     	// receive mDoorClosed[b,r]
     private ReadableCanMailbox networkDoorClosedLeft;
     private DoorClosedCanPayloadTranslator mDoorClosedLeft;
     private ReadableCanMailbox networkDoorClosedRight;
     private DoorClosedCanPayloadTranslator mDoorClosedRight;
     
     
     // Output Mailbox and Translators:
     	// Send mCarLight[f,b]
     private WriteableCanMailbox networkCarLight;
     private BooleanCanPayloadTranslator mCarLight;
     	// Send mCarCall[f,b]
     private WriteableCanMailbox networkCarCall;
     private BooleanCanPayloadTranslator mCarCall;
     
     
     //these variables keep track of which instance this is.
     private final Hallway hallway;
     private final int floor;
     
     //store the period for the controller
     private SimTime period;
     
     //additional internal state variables
     private int CurrentFloor;
     private AtFloorArray floorArray;
 
     //enumerate states
     private enum State {
         STATE_LIGHT_OFF,
         STATE_LIGHT_ON,
     }
     //state variable initialized to the initial state FLASH_OFF
     private State state = State.STATE_LIGHT_OFF;
 
     /**
      * The arguments listed in the .cf configuration file should match the order and
      * type given here.
      *
      * For your elevator controllers, you should make sure that the constructor matches
      * the method signatures in ControllerBuilder.makeAll().
      */
     public CarButtonControl(int floor, Hallway hallway, SimTime period, boolean verbose) {
         //call to the Controller superclass constructor is required
         super("CarButtonControl" + ReplicationComputer.makeReplicationString(floor, hallway), verbose);
         
         //stored the constructor arguments in internal state
         this.hallway = hallway;
         this.floor = floor;
         this.period = period;
         floorArray = new AtFloorArray(canInterface);
         /*
          * Initialize Physical State
          */
         /** Inputs */
         // CarCall
         localCarCall = CarCallPayload.getReadablePayload(floor, hallway);
         physicalInterface.registerTimeTriggered(localCarCall);
         
         /** Outputs */
         // CarLight
         localCarLight = CarLightPayload.getWriteablePayload(floor, hallway);
         physicalInterface.sendTimeTriggered(localCarLight, period);
 
         /*
          * Initialize network interface
          */
         /** Inputs */
         // Registration for all the AtFloor messages
         networkAtFloor = CanMailbox.getReadableCanMailbox(
         				 MessageDictionary.AT_FLOOR_BASE_CAN_ID +
         				 ReplicationComputer.computeReplicationId(floor, hallway));
     	mAtFloor = new AtFloorCanPayloadTranslator(networkAtFloor, floor, hallway);
         canInterface.registerTimeTriggered(networkAtFloor);
 
     	
          // Registration for the DesiredFloor message
         networkDesiredFloor = CanMailbox.getReadableCanMailbox(
         					  MessageDictionary.DESIRED_FLOOR_CAN_ID);
         mDesiredFloor = new DesiredFloorCanPayloadTranslator(networkDesiredFloor);
         canInterface.registerTimeTriggered(networkDesiredFloor);
         
          // Registration for the DoorClosed message is similar to the mCarLight message
         networkDoorClosedLeft = CanMailbox.getReadableCanMailbox(
         						MessageDictionary.DOOR_CLOSED_SENSOR_BASE_CAN_ID +
         						ReplicationComputer.computeReplicationId(hallway, Side.LEFT));
         mDoorClosedLeft = new DoorClosedCanPayloadTranslator(networkDoorClosedLeft, hallway, Side.LEFT);
         canInterface.registerTimeTriggered(networkDoorClosedLeft);
         networkDoorClosedRight= CanMailbox.getReadableCanMailbox(
 								MessageDictionary.DOOR_CLOSED_SENSOR_BASE_CAN_ID +
 								ReplicationComputer.computeReplicationId(hallway, Side.RIGHT));
         mDoorClosedRight = new DoorClosedCanPayloadTranslator(networkDoorClosedRight, hallway, Side.RIGHT);
         canInterface.registerTimeTriggered(networkDoorClosedRight);    
         
         floorArray = new AtFloorArray(canInterface);
         
         /** Outputs */      
         // Registration for all the mCarLight messages
         networkCarLight = CanMailbox.getWriteableCanMailbox(
         				  MessageDictionary.CAR_LIGHT_BASE_CAN_ID+
         				  ReplicationComputer.computeReplicationId(floor, hallway));
         mCarLight = new BooleanCanPayloadTranslator(networkCarLight);
         canInterface.sendTimeTriggered(networkCarLight, period);
         
         // Registration for all the mCarCall messages
         networkCarCall= CanMailbox.getWriteableCanMailbox(
 				  MessageDictionary.CAR_CALL_BASE_CAN_ID +
 				  ReplicationComputer.computeReplicationId(floor, hallway));
         mCarCall= new BooleanCanPayloadTranslator(networkCarCall);
         canInterface.sendTimeTriggered(networkCarCall, period);
         
         timer.start(period);
     }
 
     /*
      * The timer callback is where the main controller code is executed.  For time
      * triggered design, this consists mainly of a switch block with a case block for
      * each state.  Each case block executes actions for that state, then executes
      * a transition to the next state if the transition conditions are met.
      */
     public void timerExpired(Object callbackData) {
         State newState = state;
         /* State Machine */
         switch (state) {
             case STATE_LIGHT_OFF:
                 //state actions for 'LIGHT OFF'
                 localCarLight.set(false);
                 mCarLight.set(false);
                 mCarCall.set(false);
                 CurrentFloor = floorArray.getCurrentFloor();
                 
                 //#transition '9.T.1'
                   if (localCarCall.pressed() == true) {
                     newState = State.STATE_LIGHT_ON;
                 }
                 else {
                 	newState = state;
                 }
                 break;
             case STATE_LIGHT_ON:
                 //state actions for 'LIGHT ON'
                 localCarLight.set(true);
                 mCarLight.set(true);
                 mCarCall.set(true);
                 
                 //#transition '9.T.2'
<<<<<<< HEAD
                 if (((mDoorClosedLeft.getValue() == false && 
                     	mDoorClosedRight.getValue() == false && 
                 		(mAtFloor.getValue() == true)) ||
                 	((mDesiredFloor.getDirection() == Direction.UP) && (floorArray.getCurrentFloor() > floor)) || 
                 	((mDesiredFloor.getDirection() == Direction.DOWN) && (floorArray.getCurrentFloor() < floor))) &&
                 	localCarCall.pressed() == false) {
=======
                if (mAtFloor.getValue() == true) {
>>>>>>> 796420e9e2a75cec5a7747586ed224fbd5efb8b0
                 	// AND BUTTON NOT PRESSED
                     newState = State.STATE_LIGHT_OFF;
                     break;
                 }
                 else {
                 	newState = state;
                 }
                 break;
             default:
                 throw new RuntimeException("State " + state + " was not recognized.");
         }
         
         /*log the results of this iteration
         if (state == newState) {
             log("remains in state: ",state);
         } else {
             log("Transition:",state,"->",newState);
         }
 		*/
         //update the state variable
         state = newState;
 
         //report the current state
         setState(STATE_KEY,newState.toString());
 
         timer.start(period);
     }
 }
 
