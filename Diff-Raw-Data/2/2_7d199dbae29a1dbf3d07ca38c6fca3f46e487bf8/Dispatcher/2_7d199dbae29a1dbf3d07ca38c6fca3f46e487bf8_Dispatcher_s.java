 package simulator.elevatorcontrol;
 
 import java.util.HashMap;
 
 import jSimPack.SimTime;
 import simulator.elevatormodules.AtFloorCanPayloadTranslator;
 import simulator.elevatormodules.DoorClosedCanPayloadTranslator;
 import simulator.framework.Controller;
 import simulator.framework.Direction;
 import simulator.framework.Elevator;
 import simulator.framework.Hallway;
 import simulator.framework.ReplicationComputer;
 import simulator.framework.Side;
 import simulator.payloads.CanMailbox;
 import simulator.payloads.CanMailbox.ReadableCanMailbox;
 import simulator.payloads.CanMailbox.WriteableCanMailbox;
 
 public class Dispatcher extends Controller {
 	
     //network interface
     // command door motor
     private WriteableCanMailbox networkDesiredFloor;
     // translator for the door motor command message -- this is a generic translator
     private DesiredFloorCanPayloadTranslator mDesiredFloor;
 
     private WriteableCanMailbox networkDesiredDwellFront;
     // translator for the door motor command message -- this is a generic translator
     private DesiredDwellCanPayloadTranslator mDesiredDwellFront;
 
     private WriteableCanMailbox networkDesiredDwellBack;
     // translator for the door motor command message -- this is a generic translator
     private DesiredDwellCanPayloadTranslator mDesiredDwellBack;
     
     //received at floor message
     private HashMap<Integer, AtFloorCanPayloadTranslator> mAtFloor = new HashMap<Integer, AtFloorCanPayloadTranslator>();
     
     //received door closed message
     private ReadableCanMailbox networkDoorClosedFrontLeft;
     //translator for the doorClosed message -- this translator is specific
     private DoorClosedCanPayloadTranslator mDoorClosedFrontLeft;
     
     //received door closed message
     private ReadableCanMailbox networkDoorClosedFrontRight;
     //translator for the doorClosed message -- this translator is specific
     private DoorClosedCanPayloadTranslator mDoorClosedFrontRight;
     
     //received door closed message
     private ReadableCanMailbox networkDoorClosedBackLeft;
     //translator for the doorClosed message -- this translator is specific
     private DoorClosedCanPayloadTranslator mDoorClosedBackLeft;
     
     //received door closed message
     private ReadableCanMailbox networkDoorClosedBackRight;
     //translator for the doorClosed message -- this translator is specific
     private DoorClosedCanPayloadTranslator mDoorClosedBackRight;
     
     private static Hallway hallway = Hallway.NONE;
 	private static int targetFloor = 1;
     private final static SimTime dwell = new SimTime(5,
             SimTime.SimTimeUnit.SECOND);
     
     //store the period for the controller
     private final static SimTime period = 
     		MessageDictionary.DISPATCHER_PERIOD;
 
     //enumerate states
     private enum State {
         STATE_INIT,
         STATE_SET_TARGET,
         STATE_RESET,
     }
     
     //state variable initialized to the initial state FLASH_OFF
     private State state = State.STATE_INIT;
 
 	public Dispatcher(boolean verbose) {
 		super("Dispatcher", verbose);
 	
         log("Created Dispatcher with period = ", period);
     
         networkDesiredFloor = CanMailbox.getWriteableCanMailbox(
                 MessageDictionary.DESIRED_FLOOR_CAN_ID);
         mDesiredFloor = new DesiredFloorCanPayloadTranslator(
         		networkDesiredFloor);
         canInterface.sendTimeTriggered(networkDesiredFloor, period);
         
         networkDesiredDwellFront = CanMailbox.getWriteableCanMailbox(
                 MessageDictionary.DESIRED_DWELL_BASE_CAN_ID + 
                 ReplicationComputer.computeReplicationId(Hallway.FRONT));
         mDesiredDwellFront = new DesiredDwellCanPayloadTranslator(
         		networkDesiredDwellFront, Hallway.FRONT);
         canInterface.sendTimeTriggered(networkDesiredDwellFront, period);
 
         networkDesiredDwellBack = CanMailbox.getWriteableCanMailbox(
                 MessageDictionary.DESIRED_DWELL_BASE_CAN_ID + 
                 ReplicationComputer.computeReplicationId(Hallway.BACK));
         mDesiredDwellBack = new DesiredDwellCanPayloadTranslator(
        		networkDesiredDwellFront, Hallway.BACK);
         canInterface.sendTimeTriggered(networkDesiredDwellBack, period);
         
         for (int i = 0; i < Elevator.numFloors; i++) {
             int floor = i + 1;
             for (Hallway h : Hallway.replicationValues) {
                 int index = ReplicationComputer.computeReplicationId(floor, h);
                 ReadableCanMailbox m = CanMailbox.getReadableCanMailbox(MessageDictionary.AT_FLOOR_BASE_CAN_ID + index);
                 AtFloorCanPayloadTranslator t = new AtFloorCanPayloadTranslator(m, floor, h);
                 canInterface.registerTimeTriggered(m);
                 mAtFloor.put(index, t);
             }
         }
         
         networkDoorClosedFrontLeft = CanMailbox.getReadableCanMailbox(
                 MessageDictionary.DOOR_CLOSED_SENSOR_BASE_CAN_ID + 
                 ReplicationComputer.computeReplicationId(Hallway.FRONT, Side.LEFT));
         mDoorClosedFrontLeft = new DoorClosedCanPayloadTranslator(networkDoorClosedFrontLeft, Hallway.FRONT, Side.LEFT);
         canInterface.registerTimeTriggered(networkDoorClosedFrontLeft);
         
         networkDoorClosedFrontRight = CanMailbox.getReadableCanMailbox(
                 MessageDictionary.DOOR_CLOSED_SENSOR_BASE_CAN_ID + 
                 ReplicationComputer.computeReplicationId(Hallway.FRONT, Side.RIGHT));
         mDoorClosedFrontRight = new DoorClosedCanPayloadTranslator(networkDoorClosedFrontRight, Hallway.FRONT, Side.RIGHT);
         canInterface.registerTimeTriggered(networkDoorClosedFrontRight);
         
         networkDoorClosedBackLeft = CanMailbox.getReadableCanMailbox(
                 MessageDictionary.DOOR_CLOSED_SENSOR_BASE_CAN_ID + 
                 ReplicationComputer.computeReplicationId(Hallway.BACK, Side.LEFT));
         mDoorClosedBackLeft = new DoorClosedCanPayloadTranslator(networkDoorClosedBackLeft, Hallway.BACK, Side.LEFT);
         canInterface.registerTimeTriggered(networkDoorClosedBackLeft);
         
         networkDoorClosedBackRight = CanMailbox.getReadableCanMailbox(
                 MessageDictionary.DOOR_CLOSED_SENSOR_BASE_CAN_ID + 
                 ReplicationComputer.computeReplicationId(Hallway.BACK, Side.RIGHT));
         mDoorClosedBackRight = new DoorClosedCanPayloadTranslator(networkDoorClosedBackRight, Hallway.BACK, Side.RIGHT);
         canInterface.registerTimeTriggered(networkDoorClosedBackRight);
         
         
         timer.start(period);
 	}
 	
     /*
      * The timer callback is where the main controller code is executed.  For
      * time triggered design, this consists mainly of a switch block with a
      * case blcok for each state.  Each case block executes actions for that
      * state, then executes a transition to the next state if the transition
      * conditions are met.
      */
     public void timerExpired(Object callbackData) {
         State newState = state;
         switch (state) {
             case STATE_INIT:
                 // state actions for 'Initialize'
                 mDesiredFloor.set(targetFloor, hallway, Direction.STOP);
                 if (hallway == Hallway.FRONT)
                 	mDesiredDwellFront.setDwell(dwell);
                 
                 if (hallway == Hallway.BACK)
                 	mDesiredDwellBack.setDwell(dwell);
                 
                 // #transition 'T11.1'
                 if (!(mDoorClosedFrontLeft.getValue() && 
                 	mDoorClosedFrontRight.getValue() && 
                 	mDoorClosedBackLeft.getValue() && 
                 	mDoorClosedBackRight.getValue())) {
                 	newState = State.STATE_SET_TARGET;
                 	targetFloor++;
  
                 	int nHallway = 0;
                     for (int i = 0; i < Elevator.numFloors; i++) {
                         int floor = i + 1;
                         for (Hallway h : Hallway.replicationValues) {
                             int index = ReplicationComputer.computeReplicationId(floor, h);
                             if (mAtFloor.get(index).getValue()) {
                             	nHallway++;
                             	if (nHallway >= 2)
                             		hallway = Hallway.BOTH;
                             	else
                             		hallway = h;
                              }
                         }
                     }
                 }
                 break;
             case STATE_SET_TARGET:
                 // state actions for 'SET TARGET'
             	mDesiredFloor.set(targetFloor, hallway, Direction.STOP);
             	if (hallway == Hallway.FRONT)
                 	mDesiredDwellFront.setDwell(dwell);
                 
                 if (hallway == Hallway.BACK)
                 	mDesiredDwellBack.setDwell(dwell);
                 
                 // #transition 'T11.1'
                 if (!(mDoorClosedFrontLeft.getValue() && 
                 	mDoorClosedFrontRight.getValue() && 
                 	mDoorClosedBackLeft.getValue() && 
                 	mDoorClosedBackRight.getValue())) {
                 	targetFloor++;
  
                 	int nHallway = 0;
                 	int index = 0;
                 	boolean isAtFloor = false;
                     for (int i = 0; i < Elevator.numFloors; i++) {
                         int floor = i + 1;
                         for (Hallway h : Hallway.replicationValues) {
                             index = ReplicationComputer.computeReplicationId(floor, h);
                             if (mAtFloor.get(index).getValue()) {
                             	isAtFloor = true;
                             	nHallway++;
                             	if (nHallway >= 2)
                             		hallway = Hallway.BOTH;
                             	else
                             		hallway = h;
                              }
                         }
                     }
                     
                     // make sure that the last is false as well
                     // #transition 'T11.1'
                     if ((!isAtFloor) && (index == ReplicationComputer.computeReplicationId(Elevator.numFloors, Hallway.BACK)))
                     	if (!mAtFloor.get(index).getValue())
                     		state = State.STATE_RESET;
                 }
                 break;
             case STATE_RESET:
                 // state actions for 'RESET'
             	mDesiredFloor.set(1, Hallway.NONE, Direction.STOP);
                 break;
             default:
                 throw new RuntimeException("State " + state + " was not recognized.");
         }
         
         if (state == newState) {
             log("remains in state: ",state);
         } else {
             log("Transition:",state,"->",newState);
         }
 
         state = newState;
         setState(STATE_KEY,newState.toString());
 
         // schedule the next iteration of the controller
         // you must do this at the end of the timer callback in order to
         // restart the timer
         timer.start(period);
     }
 
 }
