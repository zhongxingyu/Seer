 /*
  * 18-649 Fall 2013
  * Group 22
  * David Chow davidcho
  * Brody Anderson bcanders
  * Yang Liu yangliu2
  * Jeffrey Lau jalau
  * DoorControl.java
  * @author: Brody Anderson (bcanders)
  */
 
 
 package simulator.elevatorcontrol;
 
 import java.util.ArrayList;
 
 import jSimPack.SimTime;
 import jSimPack.SimTime.SimTimeUnit;
 import simulator.elevatormodules.*;
 import simulator.framework.Controller;
 import simulator.framework.Direction;
 import simulator.framework.DoorCommand;
 import simulator.framework.Elevator;
 import simulator.framework.Hallway;
 import simulator.framework.ReplicationComputer;
 import simulator.framework.Side;
 import simulator.payloads.CanMailbox;
 import simulator.payloads.CanMailbox.ReadableCanMailbox;
 import simulator.payloads.DoorMotorPayload;
 import simulator.payloads.DoorMotorPayload.WriteableDoorMotorPayload;
 import simulator.payloads.translators.BooleanCanPayloadTranslator;
 
 
 public class DoorControl extends Controller {
 	
 	
 	//enumerate states
     private enum State {
         CAR_MOVING,
         OPENING_DOOR,
         WAIT_FOR_DWELL,
         CLOSING_DOOR,
         NUDGING_DOOR,
     }
     
     // Initialize state to CAR_MOVING
     private State state = State.CAR_MOVING;
     
     // State Variables:
     // long integer with number of msec desired for door 
     // dwell during current cycle.
     private final SimTime Dwell = new SimTime("2s");
     // a count-down timer for Door Dwell[b] (implemented in simulation by 
     // scheduling a future task execution at time of expiration)
     private SimTime CountDown;
     // Which hallway and side is this DoorControl for?
     private Hallway h;
     private Side s;
     private int maxReversals = 2;
     private int reversalCount;
     private boolean open_flag = true;
     private Direction p_dir = Direction.STOP;
     private int p_floor = 1;
     private Hallway p_hall = Hallway.BOTH;
     
     //store the period for the controller
     private SimTime period;
     
     // initialize local variables
     private int currentFloor;
     private int lastValidFloor;
     // 0: front,left, 1: front,right, 2: back,left, 3: back,right
     ReadableCanMailbox networkDoorClosed;
     DoorClosedCanPayloadTranslator mDoorClosed;
     
     ReadableCanMailbox networkDoorOpen;
     DoorOpenedCanPayloadTranslator mDoorOpen;
     
     ReadableCanMailbox networkDoorReversal1;
     DoorReversalCanPayloadTranslator mDoorReversal1;
     
     ReadableCanMailbox networkDoorReversal2;
     DoorReversalCanPayloadTranslator mDoorReversal2;
     
     ReadableCanMailbox networkCarWeight;
     CarWeightCanPayloadTranslator mCarWeight;
     
     int length = 2*Elevator.numFloors;
     ReadableCanMailbox[] networkAtFloor = new ReadableCanMailbox[length];
     AtFloorCanPayloadTranslator[] mAtFloor = new AtFloorCanPayloadTranslator[length];
     
     ReadableCanMailbox networkDesiredFloor;
     DesiredFloorCanPayloadTranslator mDesiredFloor;
     
     ReadableCanMailbox[] networkCarCall;
     CarCallCanPayloadTranslator[] mCarCall;
     
     ReadableCanMailbox[] networkHallCall;
     HallCallCanPayloadTranslator[] mHallCall;
     
     // Outputs
     WriteableDoorMotorPayload DoorMotor;
     
     ReadableCanMailbox networkDriveSpeed;
     DriveSpeedCanPayloadTranslator mDriveSpeed;
     
 	
     /**
      * The arguments listed in the .cf configuration file should match the order and
      * type given here.
      *
      * Make sure that the constructor matches
      * the method signatures in ControllerBuilder.makeAll().
      */
 	public DoorControl(Hallway h, Side s, SimTime period, boolean verbose) {	
 		super("DoorControl" + ReplicationComputer.makeReplicationString(h,s), verbose);
 		
 		this.period = period;
 		this.h = h;
 		this.s = s;
 		this.currentFloor = 1;
 		this.lastValidFloor = 1;
 		
 		log("Created DoorControl with period = ", period);
 		
 		// make the mailboxes and translators for all network messages
 		networkDoorClosed = CanMailbox.getReadableCanMailbox(MessageDictionary.DOOR_CLOSED_SENSOR_BASE_CAN_ID + ReplicationComputer.computeReplicationId(h, s));
 		mDoorClosed = new DoorClosedCanPayloadTranslator(networkDoorClosed, h,s);
 		canInterface.registerTimeTriggered(networkDoorClosed);
 
 		networkDoorOpen = CanMailbox.getReadableCanMailbox(MessageDictionary.DOOR_OPEN_SENSOR_BASE_CAN_ID + ReplicationComputer.computeReplicationId(h, s));
 		mDoorOpen = new DoorOpenedCanPayloadTranslator(networkDoorOpen, h, s);
 		canInterface.registerTimeTriggered(networkDoorOpen);
 		
 		networkDoorReversal1 = CanMailbox.getReadableCanMailbox(MessageDictionary.DOOR_REVERSAL_SENSOR_BASE_CAN_ID + ReplicationComputer.computeReplicationId(h, Side.LEFT));
 		mDoorReversal1 = new DoorReversalCanPayloadTranslator(networkDoorReversal1, h, Side.LEFT);
 		canInterface.registerTimeTriggered(networkDoorReversal1);
 		
 		networkDoorReversal2 = CanMailbox.getReadableCanMailbox(MessageDictionary.DOOR_REVERSAL_SENSOR_BASE_CAN_ID + ReplicationComputer.computeReplicationId(h, Side.RIGHT));
 		mDoorReversal2 = new DoorReversalCanPayloadTranslator(networkDoorReversal2, h, Side.RIGHT);
 		canInterface.registerTimeTriggered(networkDoorReversal2);		
 		
 		networkCarWeight = CanMailbox.getReadableCanMailbox(MessageDictionary.CAR_WEIGHT_CAN_ID);
 		mCarWeight = new CarWeightCanPayloadTranslator(networkCarWeight);
 		canInterface.registerTimeTriggered(networkCarWeight);
 		
 		networkDesiredFloor = CanMailbox.getReadableCanMailbox(MessageDictionary.DESIRED_FLOOR_CAN_ID);
 	    mDesiredFloor = new DesiredFloorCanPayloadTranslator(networkDesiredFloor);
 	    canInterface.registerTimeTriggered(networkDesiredFloor);
 		
         for (int floor = 1; floor <= Elevator.numFloors; floor++) {
             for (Hallway hall : Hallway.replicationValues) {
                 int index = ReplicationComputer.computeReplicationId(floor, hall);
                 networkAtFloor[index] = CanMailbox.getReadableCanMailbox(MessageDictionary.AT_FLOOR_BASE_CAN_ID + 
                 											ReplicationComputer.computeReplicationId(floor, hall));
                 mAtFloor[index] = new AtFloorCanPayloadTranslator(networkAtFloor[index], floor, hall);
                 canInterface.registerTimeTriggered(networkAtFloor[index]);
             }
         }
 	    	    
         networkDriveSpeed = CanMailbox.getReadableCanMailbox(MessageDictionary.DRIVE_SPEED_CAN_ID);
 		mDriveSpeed = new DriveSpeedCanPayloadTranslator(networkDriveSpeed); // leave this as is
 		canInterface.registerTimeTriggered(networkDriveSpeed);
 		
 		networkCarCall = new CanMailbox.ReadableCanMailbox[Elevator.numFloors*2];
         mCarCall = new CarCallCanPayloadTranslator[Elevator.numFloors*2];        
         for(int floors = 1; floors <= Elevator.numFloors; floors++) {
                 for (Hallway H : Hallway.replicationValues) {
                         int index = ReplicationComputer.computeReplicationId(floors,H);
                         networkCarCall[index] = CanMailbox.getReadableCanMailbox(
                                         MessageDictionary.CAR_CALL_BASE_CAN_ID + 
                                         ReplicationComputer.computeReplicationId(floors,H));
                         mCarCall[index] = new CarCallCanPayloadTranslator(networkCarCall[index], floors, H);
                         canInterface.registerTimeTriggered(networkCarCall[index]);
                 }
         }
         
         //HallCall
   		networkHallCall = new CanMailbox.ReadableCanMailbox[Elevator.numFloors*4];
   		mHallCall = new HallCallCanPayloadTranslator[Elevator.numFloors*4];
   		for(int floors = 1; floors <= Elevator.numFloors; floors++) {
   			for (Hallway h1 : Hallway.replicationValues) {
   				for (Direction d : Direction.replicationValues) {
   					int index = ReplicationComputer.computeReplicationId(floors,h1,d);
   					networkHallCall[index] = CanMailbox.getReadableCanMailbox(
   							MessageDictionary.HALL_CALL_BASE_CAN_ID + 
   							ReplicationComputer.computeReplicationId(floors,h1,d));
   					mHallCall[index] = new HallCallCanPayloadTranslator(networkHallCall[index],floors,h1,d);
   					canInterface.registerTimeTriggered(networkHallCall[index]);
   				}
   			}
   		}
 
 		DoorMotor = DoorMotorPayload.getWriteablePayload(h,s);
 		physicalInterface.sendTimeTriggered(DoorMotor, period);
 		
 		/* issuing the timer start method with no callback data means a NULL value 
          * will be passed to the callback later.  Use the callback data to distinguish
          * callbacks from multiple calls to timer.start() (e.g. if you have multiple
          * timers.
          */
         timer.start(period);
 	}
 
 	/** 
 	 * CurrentFloor is a simple helper function that returns an int
 	 * representing the floor at which mAtFloor[f,d] is true
 	 * @return - the floor at which mAtFloor[f,d] is true
 	 */
 	private int CurrentMAtFloor() {
 		int f;
 		int cFloor = -1;
 		for (int floor = 1; floor <= Elevator.numFloors; floor++) {
             for (Hallway hall : Hallway.replicationValues) {
                 f = ReplicationComputer.computeReplicationId(floor,hall);
                 if (mAtFloor[f].getValue()){
                 	if (cFloor != -1) { // This should never happen!
                 		// Errormessage
                 	}
                 	else {
                     	cFloor = floor;
                 	}
                 }
             }
         }
 		return cFloor;
 	}
 
 	@Override
 	public void timerExpired(Object callbackData) {
 		if (CurrentMAtFloor() != -1) {
     		this.lastValidFloor = CurrentMAtFloor();
     	}
 		this.currentFloor = this.lastValidFloor;
 		State newState = state;
         switch (state) {
             case CAR_MOVING: // S1
             	// State 1 actions:
             	DoorMotor.set(DoorCommand.STOP);
             	this.CountDown = this.Dwell;
             	this.reversalCount = 0;
             	set_open_flag();
             	// State 1 transitions
             	// #transition 5.T.1
             	//If desired hallway is BOTH, open all doors.
             	if(mDesiredFloor.getHallway() == Hallway.BOTH){
             		if (mAtFloor[ReplicationComputer.computeReplicationId(
             				mDesiredFloor.getFloor(),Hallway.FRONT)].getValue() == true 
             				&& mDriveSpeed.getSpeed() == 0.0
             				&& this.open_flag) {
             			newState = State.OPENING_DOOR; // T1
             		}
             	}
             	else if (mAtFloor[ReplicationComputer.computeReplicationId(
             			mDesiredFloor.getFloor(),mDesiredFloor.getHallway())].getValue() == true 
             			&& mDriveSpeed.getSpeed() == 0.0 && mDesiredFloor.getHallway() == this.h
             			&& this.open_flag) {
             		newState = State.OPENING_DOOR; // T1
             	}
                 break;
             case OPENING_DOOR: // S2
             	// State 2 actions:
             	DoorMotor.set(DoorCommand.OPEN);
             	this.CountDown = this.Dwell;
 
             	// State 2 transitions
 				// #transition 5.T.2
            		if (this.mDoorOpen.getValue() == true) {
             			newState = State.WAIT_FOR_DWELL; // T2
             	}
                 break;
             case WAIT_FOR_DWELL: // S3
             	// State 3 actions:
             	DoorMotor.set(DoorCommand.STOP);
             	
             	// decrement Countdown by the period
             	this.CountDown = SimTime.subtract(this.CountDown, this.period);
             	
             	// Increase the countdown whenver the elevator is over weight
             	if (mCarWeight.getValue() >= Elevator.MaxCarCapacity) {
             		this.CountDown = this.Dwell;
             	}
             	
             	// State 3 transitions
                 if (this.CountDown.isPositive()) { // Countdown has reached 0
                 	newState = state; 
                 }
 				// #transition 5.T.3
                 else { // Countdown still greater than 0
                	newState = State.CLOSING_DOOR; // T3
                 }
                 break;
             case CLOSING_DOOR: // S4
             	// State 4 actions:
             	DoorMotor.set(DoorCommand.CLOSE);
             	this.CountDown = this.Dwell;
             	
             	// State 4 transitions
 				// #transition 5.T.5 and 5.T.6
                 if ((mCarWeight.getValue() >= Elevator.MaxCarCapacity) ||
                 (mCarCall[ReplicationComputer.computeReplicationId(
                  this.currentFloor, this.h)].getValue() == true)) {
                   		newState = State.OPENING_DOOR; // T5
             	}
                 else if (mDoorReversal1.getValue() || mDoorReversal2.getValue()) { // Check for door reversal
                	if (this.reversalCount < this.maxReversals){
	                	newState = State.OPENING_DOOR; // T5
	                	reversalCount++;
                	}
                	else { // Too many reversals have been done
                		newState = State.NUDGING_DOOR; // T6
                	}
                 }
 				// #transition 5.T.4
             	else if (mDoorClosed.getValue()) {
             		newState = State.CAR_MOVING; //T4
             		p_dir = mDesiredFloor.getDirection();
         			p_floor = mDesiredFloor.getFloor();
         			p_hall = mDesiredFloor.getHallway();
                 }
             	else {
             		newState = state;
             	}
                 
                 break;
             case NUDGING_DOOR: // S5
             	DoorMotor.set(DoorCommand.NUDGE);
             	this.CountDown = this.Dwell;
             	
             	// State 5 transitions
 				// #transition 5.T.8
                 if ((mCarWeight.getValue() >= Elevator.MaxCarCapacity) ||
                 (mCarCall[ReplicationComputer.computeReplicationId(
                  this.currentFloor, this.h)].getValue() == true)) {
                   		newState = State.OPENING_DOOR; // T8
             	}
 				// #transition 5.T.7
             	else if (mDoorClosed.getValue()) {
             		newState = State.CAR_MOVING; //T7
             		p_dir = mDesiredFloor.getDirection();
         			p_floor = mDesiredFloor.getFloor();
         			p_hall = mDesiredFloor.getHallway();
                 }
             	else {
             		newState = state;
             	}
                 break;
             default:
                 throw new RuntimeException("State " + state + " was not recognized.");
         }
         
         //log the results of this iteration
        /* if (state == newState) {
             log("DoorControl remains in state: ",state);
         } else {
             log("Transition:",state,"->",newState);
         }*/
 
         //update the state variable
         state = newState;
 
         //report the current state
         setState(STATE_KEY,newState.toString());
 
         //schedule the next iteration of the controller
         //you must do this at the end of the timer callback in order to restart
         //the timer
         timer.start(period);
 	}
 	
 	private void set_open_flag() {
 		if (mCarCall[ReplicationComputer.computeReplicationId(this.currentFloor, this.h)].getValue()) {
 			this.open_flag = true;
 		}
 		else if (mDesiredFloor.getDirection() != Direction.STOP &&
 				mHallCall[ReplicationComputer.computeReplicationId(this.currentFloor, this.h, mDesiredFloor.getDirection())].getValue()) {
 			this.open_flag = true;
 		}
 		else if (p_dir == mDesiredFloor.getDirection() 
 				&& p_floor == mDesiredFloor.getFloor()
 				&& p_hall == mDesiredFloor.getHallway()) {
 			this.open_flag = false;
 		}
 		else {
 			this.open_flag = true;
 		}
 		return;
 	}
 
 }
