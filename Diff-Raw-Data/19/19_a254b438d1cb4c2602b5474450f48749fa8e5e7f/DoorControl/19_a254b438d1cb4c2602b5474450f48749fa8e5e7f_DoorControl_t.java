 package simulator.elevatorcontrol;
 
 import jSimPack.SimTime;
 import simulator.elevatormodules.CarWeightCanPayloadTranslator;
 import simulator.elevatormodules.DesiredDwellCanPayloadTranslator;
 import simulator.elevatormodules.DoorClosedCanPayloadTranslator;
 import simulator.elevatormodules.DoorOpenedCanPayloadTranslator;
 import simulator.elevatormodules.DoorReversalCanPayloadTranslator;
 import simulator.framework.*;
 import simulator.payloads.CanMailbox;
 import simulator.payloads.CanMailbox.ReadableCanMailbox;
 import simulator.payloads.CanMailbox.WriteableCanMailbox;
 import simulator.payloads.DoorMotorPayload;
 import simulator.payloads.DoorMotorPayload.WriteableDoorMotorPayload;
 
 /**
  * HallButtonControl controls responds to passenger input on the hall buttons
  * and eventually will notify the dispatcher when a passenger is requesting pickup
  *
  * @author Rajeev Sharma
  */
 public class DoorControl extends Controller {
 
     /**
      * ************************************************************************
      * Declarations
      * ************************************************************************
      */
     //note that inputs are Readable objects, while outputs are Writeable objects
 
     //local physical state
     private WriteableDoorMotorPayload localDoorMotor;
 
     private WriteableCanMailbox networkDoorMotorCommandOut;
     private DoorMotorCommandCanPayloadTranslator mDoorMotorCommand;
 
     private Utility.AtFloorArray networkAtFloorArray;
 
     private ReadableCanMailbox networkDriveSpeed;
     private DriveSpeedCanPayloadTranslator mDriveSpeed;
 
     private ReadableCanMailbox networkDesiredFloor;
     private DesiredFloorCanPayloadTranslator mDesiredFloor;
 
     private ReadableCanMailbox networkDesiredDwell;
     private DesiredDwellCanPayloadTranslator mDesiredDwell;
 
     private ReadableCanMailbox networkDoorClosed;
     private DoorClosedCanPayloadTranslator mDoorClosed;
 
     private ReadableCanMailbox networkDoorOpened;
     private DoorOpenedCanPayloadTranslator mDoorOpened;
 
     private ReadableCanMailbox networkDoorReversal;
     private DoorReversalCanPayloadTranslator mDoorReversal;
 
     private Utility.CarCallArray networkCarCallArray;
 
     // TODO:  add back in, not currently used
     //private ReadableCanMailbox networkHallCall;
     //private HallCallCanPayloadTranslator mHallCall;
 
     private ReadableCanMailbox networkCarWeight;
     private CarWeightCanPayloadTranslator mCarWeight;
 
     //these variables keep track of which instance this is.
     private final Hallway hallway;
     private final Side side;
 
     // local state variables
    private int dwell = 0;
    private SimTime countDown = SimTime.ZERO;
 
     //store the period for the controller
     private SimTime period;
 
     //internal constant declarations
 
     //enumerate states
     private enum State {
         STATE_DOOR_CLOSING,
         STATE_DOOR_CLOSED,
         STATE_DOOR_OPENING,
         STATE_DOOR_OPEN,
     }
 
     //state variable initialized to the initial state FLASH_OFF
     private State state = State.STATE_DOOR_CLOSING;
 
     /**
      * The arguments listed in the .cf configuration file should match the order and
      * type given here.
      * <p/>
      * For your elevator controllers, you should make sure that the constructor matches
      * the method signatures in ControllerBuilder.makeAll().
      */
     public DoorControl(SimTime period, Hallway hallway, Side side, boolean verbose) {
         //call to the Controller superclass constructor is required
         super("DoorControl" + ReplicationComputer.makeReplicationString(hallway, side), verbose);
 
         //stored the constructor arguments in internal state
         this.period = period;
         this.hallway = hallway;
         this.side = side;
 
         log("Created DoorControl[", this.hallway, "][", this.side, "]");
 
         localDoorMotor = DoorMotorPayload.getWriteablePayload(hallway, side);
         physicalInterface.sendTimeTriggered(localDoorMotor, period);
 
         //initialize network interface
         //create a can mailbox - this object has the binary representation of the message data
         //the CAN message ids are declared in the MessageDictionary class.  The ReplicationComputer
         //class provides utility methods for computing offsets for replicated controllers
         networkDoorMotorCommandOut = CanMailbox.getWriteableCanMailbox(
                 MessageDictionary.DOOR_MOTOR_COMMAND_BASE_CAN_ID +
                         ReplicationComputer.computeReplicationId(hallway, side));
         mDoorMotorCommand = new DoorMotorCommandCanPayloadTranslator(
                 networkDoorMotorCommandOut, hallway, side);
         canInterface.sendTimeTriggered(networkDoorMotorCommandOut, period);
 
         networkAtFloorArray = new Utility.AtFloorArray(canInterface);
 
         networkDriveSpeed = CanMailbox.getReadableCanMailbox(
                 MessageDictionary.DRIVE_SPEED_CAN_ID);
         mDriveSpeed = new DriveSpeedCanPayloadTranslator(networkDriveSpeed);
         canInterface.registerTimeTriggered(networkDriveSpeed);
 
         networkDesiredFloor = CanMailbox.getReadableCanMailbox(
                 MessageDictionary.DESIRED_FLOOR_CAN_ID);
         mDesiredFloor = new DesiredFloorCanPayloadTranslator(networkDesiredFloor);
         canInterface.registerTimeTriggered(networkDesiredFloor);
 
         networkDesiredDwell = CanMailbox.getReadableCanMailbox(
                 MessageDictionary.DESIRED_DWELL_BASE_CAN_ID +
                         ReplicationComputer.computeReplicationId(hallway));
         mDesiredDwell = new DesiredDwellCanPayloadTranslator(
                 networkDesiredDwell, hallway);
         canInterface.registerTimeTriggered(networkDesiredDwell);
 
         networkDoorClosed = CanMailbox.getReadableCanMailbox(
                 MessageDictionary.DOOR_CLOSED_SENSOR_BASE_CAN_ID +
                         ReplicationComputer.computeReplicationId(hallway, side));
         mDoorClosed = new DoorClosedCanPayloadTranslator(
                 networkDoorClosed, hallway, side);
         canInterface.registerTimeTriggered(networkDoorClosed);
 
         networkDoorOpened = CanMailbox.getReadableCanMailbox(
                 MessageDictionary.DOOR_OPEN_SENSOR_BASE_CAN_ID +
                         ReplicationComputer.computeReplicationId(hallway, side));
         mDoorOpened = new DoorOpenedCanPayloadTranslator(
                 networkDoorOpened, hallway, side);
         canInterface.registerTimeTriggered(networkDoorOpened);
 
         networkDoorReversal = CanMailbox.getReadableCanMailbox(
                 MessageDictionary.DOOR_REVERSAL_SENSOR_BASE_CAN_ID +
                         ReplicationComputer.computeReplicationId(hallway, side));
         mDoorReversal = new DoorReversalCanPayloadTranslator(
                 networkDoorReversal, hallway, side);
         canInterface.registerTimeTriggered(networkDoorReversal);
 
         networkCarCallArray = new Utility.CarCallArray(hallway, canInterface);
     }
 
     /*
      * The timer callback is where the main controller code is executed.  For time
      * triggered design, this consists mainly of a switch block with a case block for
      * each state.  Each case block executes actions for that state, then executes
      * a transition to the next state if the transition conditions are met.
      */
     public void timerExpired(Object callbackData) {
         State newState = state;
         switch (state) {
             case STATE_DOOR_CLOSING:
                 //state actions
                 localDoorMotor.set(DoorCommand.NUDGE);
                 mDoorMotorCommand.setCommand(DoorCommand.NUDGE);
 
                 dwell = mDesiredDwell.getDwell();
                countDown = SimTime.ZERO;
 
                 //transitions -- note that transition conditions are mutually exclusive
                 //#transition 'T5.1'
                 //if (mDoorClosed[b,r]==True)
                 if (mDoorClosed.getValue() == true) {
                     newState = State.STATE_DOOR_CLOSED;
                     //#transition 'T5.2'
                     //if ( mAtFloor[f,b]==True && mDesiredFloor.f==f && ( mDriveSpeed==(0,d) || mDriveSpeed==(s, Stop) ) )
                     //      || ( mCarWeight(g) >= MaxCarCapacity && mDoorOpened[b,r]==False )
                     //      || ( mDoorReversal==True && mDoorOpened[b,r]==False )
                     //      || ( mCarCall[f,b]==True && mAtFloor[f,b]==True ) {
                 } else if (
                         ((networkAtFloorArray.getCurrentFloor() == mDesiredFloor.getFloor())
                                 && (Speed.isStopOrLevel(mDriveSpeed.getSpeed()) || (mDriveSpeed.getDirection() == Direction.STOP)))
                                 || ((mCarWeight.getWeight() > Elevator.MaxCarCapacity)
                                 && (mDoorOpened.getValue() == false))
                                 || ((mDoorReversal.getValue() == true)
                                 && (mDoorOpened.getValue() == false))
                                 || (networkCarCallArray.getValueForFloor(networkAtFloorArray.getCurrentFloor()) == true)
                         ) {
                     newState = State.STATE_DOOR_OPENING;
                 } else {
                     newState = state;
                 }
                 break;
             case STATE_DOOR_CLOSED:
                 //state actions
                 localDoorMotor.set(DoorCommand.STOP);
                 mDoorMotorCommand.setCommand(DoorCommand.STOP);
 
                 dwell = mDesiredDwell.getDwell();
                countDown = SimTime.ZERO;
 
                 //transitions
                 //#transition 'T5.3'
                 //if ( mAtFloor[f,b]==True && mDesiredFloor.f==f && ( mDriveSpeed==(0,d) || mDriveSpeed==(s, Stop) ) )
                 //      || ( mCarWeight(g) >= MaxCarCapacity && mDoorOpened[b,r]==False )
                 //      || ( mDoorReversal==True && mDoorOpened[b,r]==False )
                 //      || ( mCarCall[f,b]==True && mAtFloor[f,b]==True ) {
                 if (((networkAtFloorArray.getCurrentFloor() == mDesiredFloor.getFloor())
                         && (Speed.isStopOrLevel(mDriveSpeed.getSpeed()) || (mDriveSpeed.getDirection() == Direction.STOP)))
                         || ((mCarWeight.getWeight() > Elevator.MaxCarCapacity)
                         && (mDoorOpened.getValue() == false))
                         || ((mDoorReversal.getValue() == true)
                         && (mDoorOpened.getValue() == false))
                         || (networkCarCallArray.getValueForFloor(networkAtFloorArray.getCurrentFloor()) == true)
                         ) {
                     newState = State.STATE_DOOR_OPENING;
                 } else {
                     newState = state;
                 }
                 break;
             case STATE_DOOR_OPENING:
                 //state actions
                 localDoorMotor.set(DoorCommand.OPEN);
                 mDoorMotorCommand.setCommand(DoorCommand.OPEN);
 
                 dwell = mDesiredDwell.getDwell();
                countDown = new SimTime(dwell, SimTime.SimTimeUnit.SECOND);
 
                 //transitions
                 //#transition 'T5.4'
                 //if (mDoorOpened[b,r] = true
                 if (mDoorOpened.getValue() == true) {
                     newState = State.STATE_DOOR_OPEN;
                 } else {
                     newState = state;
                 }
                 break;
             case STATE_DOOR_OPEN:
                 //state actions
                 localDoorMotor.set(DoorCommand.STOP);
                 mDoorMotorCommand.setCommand(DoorCommand.STOP);
 
                 dwell = mDesiredDwell.getDwell();
                countDown = SimTime.subtract(countDown, period);
 
                 //transitions
                 //#transition 'T5.5'
                 //if (countDown <= 0)
                if (countDown.isLessThanOrEqual(SimTime.ZERO)) {
                     newState = State.STATE_DOOR_CLOSING;
                 } else {
                     newState = state;
                 }
                 break;
             default:
                 throw new RuntimeException("State " + state + " was not recognized.");
         }
 
         //log the results of this iteration
         if (state == newState) {
             log("remains in state: ", state);
         } else {
             log("Transition:", state, "->", newState);
         }
 
         //update the state variable
         state = newState;
 
         //report the current state
         setState(STATE_KEY, newState.toString());
 
         //schedule the next iteration of the controller
         //you must do this at the end of the timer callback in order to restart
         //the timer
         timer.start(period);
     }
 }
