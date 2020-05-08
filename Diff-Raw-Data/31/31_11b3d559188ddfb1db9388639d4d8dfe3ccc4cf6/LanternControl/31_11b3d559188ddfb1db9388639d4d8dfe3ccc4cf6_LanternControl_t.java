 /*
  * 18-649 Fall 2013
  * Group 22
  * David Chow davidcho
  * Brody Anderson bcanders
  * Yang Liu yangliu2
  * Jeffrey Lau jalau
  * LanternControl.java
  * @author: Yang Liu yangliu2
  */
 
 package simulator.elevatorcontrol;
 
 import jSimPack.SimTime;
 import simulator.elevatorcontrol.Utility.AtFloorArray;
 import simulator.elevatormodules.DoorClosedCanPayloadTranslator;
 import simulator.elevatormodules.AtFloorCanPayloadTranslator;
 import simulator.framework.Controller;
 import simulator.framework.Direction;
 import simulator.framework.Hallway;
 import simulator.framework.ReplicationComputer;
 import simulator.framework.Side;
 import simulator.payloads.CanMailbox;
 import simulator.payloads.CanMailbox.ReadableCanMailbox;
 import simulator.payloads.CarLanternPayload;
 import simulator.payloads.CarLanternPayload.WriteableCarLanternPayload;
 
 
 public class LanternControl extends Controller {
 
 	private ReadableCanMailbox networkDoorClosedFrontLeft;
     private DoorClosedCanPayloadTranslator mDoorClosedFrontLeft;
 
     private ReadableCanMailbox networkDoorClosedFrontRight;
     private DoorClosedCanPayloadTranslator mDoorClosedFrontRight;
 
     private ReadableCanMailbox networkDoorClosedBackLeft;
     private DoorClosedCanPayloadTranslator mDoorClosedBackLeft;
 
     private ReadableCanMailbox networkDoorClosedBackRight;
     private DoorClosedCanPayloadTranslator mDoorClosedBackRight;
 
     private ReadableCanMailbox networkDesiredFloor;
     private DesiredFloorCanPayloadTranslator mDesiredFloor;
 
     private WriteableCarLanternPayload carLantern;
 
     private int CurrentFloor;
     private Direction DesiredDirection;
     private AtFloorArray floorArray;
 
     private final Direction direction;
     private SimTime period;
 
     private enum State {
         STATE_LANTERN_OFF,
         STATE_LANTERN_ON,
     }
     //state variable initialized to the initial state LANTERN_OFF
     private State state = State.STATE_LANTERN_OFF;
 
     public LanternControl(Direction direction, SimTime period, boolean verbose) {
     	super("LanternControl" + ReplicationComputer.makeReplicationString(direction), verbose);
 
     	this.period = period;
         this.direction = direction;
 
         log("Created LanternControl with period = ", period);
 
         carLantern = CarLanternPayload.getWriteablePayload(direction);
         physicalInterface.sendTimeTriggered(carLantern, period);
 
         networkDoorClosedFrontLeft = CanMailbox.getReadableCanMailbox(MessageDictionary.DOOR_CLOSED_SENSOR_BASE_CAN_ID + ReplicationComputer.computeReplicationId(Hallway.FRONT, Side.LEFT));
         mDoorClosedFrontLeft = new DoorClosedCanPayloadTranslator(networkDoorClosedFrontLeft, Hallway.FRONT, Side.LEFT);
         canInterface.registerTimeTriggered(networkDoorClosedFrontLeft);
 
         networkDoorClosedFrontRight = CanMailbox.getReadableCanMailbox(MessageDictionary.DOOR_CLOSED_SENSOR_BASE_CAN_ID + ReplicationComputer.computeReplicationId(Hallway.FRONT, Side.RIGHT));
         mDoorClosedFrontRight = new DoorClosedCanPayloadTranslator(networkDoorClosedFrontRight, Hallway.FRONT, Side.RIGHT);
         canInterface.registerTimeTriggered(networkDoorClosedFrontRight);
 
         networkDoorClosedBackLeft = CanMailbox.getReadableCanMailbox(MessageDictionary.DOOR_CLOSED_SENSOR_BASE_CAN_ID + ReplicationComputer.computeReplicationId(Hallway.BACK, Side.LEFT));
         mDoorClosedBackLeft = new DoorClosedCanPayloadTranslator(networkDoorClosedBackLeft, Hallway.BACK, Side.LEFT);
         canInterface.registerTimeTriggered(networkDoorClosedBackLeft);
 
         networkDoorClosedBackRight = CanMailbox.getReadableCanMailbox(MessageDictionary.DOOR_CLOSED_SENSOR_BASE_CAN_ID + ReplicationComputer.computeReplicationId(Hallway.BACK, Side.RIGHT));
         mDoorClosedBackRight = new DoorClosedCanPayloadTranslator(networkDoorClosedBackRight, Hallway.BACK, Side.RIGHT);
         canInterface.registerTimeTriggered(networkDoorClosedBackRight);
 
         networkDesiredFloor = CanMailbox.getReadableCanMailbox(MessageDictionary.DESIRED_FLOOR_CAN_ID);
         mDesiredFloor = new DesiredFloorCanPayloadTranslator(networkDesiredFloor);
         canInterface.registerTimeTriggered(networkDesiredFloor);
 
         floorArray = new AtFloorArray(canInterface);
         DesiredDirection = Direction.STOP;
 
         timer.start(period);
 
     }
 
     public void timerExpired(Object callbackData) {
     	State newState = state;
         switch (state) {
             case STATE_LANTERN_OFF:
             	carLantern.set(false);
             	CurrentFloor = floorArray.getCurrentFloor();
 
             	if(CurrentFloor != MessageDictionary.NONE) {
             		if(mDesiredFloor.getFloor() > CurrentFloor) {
             			DesiredDirection = Direction.UP;
             		}else if(mDesiredFloor.getFloor() < CurrentFloor) {
             			DesiredDirection = Direction.DOWN;
             		}else{
             			DesiredDirection = Direction.STOP;
             		}
             		
             	}
                 //#transition '7.T.1'
            	if(mDesiredFloor.getDirection() == direction && 
            		(mDoorClosedFrontLeft.getValue() == false || 
            		mDoorClosedFrontRight.getValue() == false || 
            		mDoorClosedBackLeft.getValue() == false || 
            		mDoorClosedBackRight.getValue() == false)) {
             		newState = State.STATE_LANTERN_ON;
                 } 
                 //#transition '7.T.2'
                 else {
                     newState = state;
                 }
                 break;
             case STATE_LANTERN_ON:	
             	carLantern.set(true);
                 //#transition '7.T.3'
            	if(mDoorClosedFrontLeft.getValue() == true && 
            		mDoorClosedFrontRight.getValue() == true &&
            		mDoorClosedBackLeft.getValue() == true &&
            		mDoorClosedBackRight.getValue() == true) {
             		newState = State.STATE_LANTERN_OFF;
             	} else {
             		newState = state;
             	}
             	break;
             default:
                 throw new RuntimeException("State " + state + " was not recognized.");
         }

         //update the state variable
         state = newState;
 
         //report the current state
         setState(STATE_KEY,newState.toString());
 
         timer.start(period);
     }
 
 }
