 /* 18649 Fall 2012
  * (Group  17)
  * Jesse Salazar (jessesal)
  * Rajeev Sharma (rdsharma) - Author
  * Collin Buchan (cbuchan)
  * Jessica Tiu   (jtiu)
  */
 
 package simulator.elevatorcontrol;
 
 import jSimPack.SimTime;
 import simulator.elevatormodules.CarWeightCanPayloadTranslator;
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
  * DoorControl controls DoorMotor objects based on current call and safety states.
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
 
     private Utility.HallCallArray networkHallCallArray;
 
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
         STATE_DOOR_OPEN_E,
     }
 
     //state variable initialized to the initial state DOOR_CLOSING
     private State state = State.STATE_DOOR_CLOSING;
 
     /**
      * The arguments listed in the .cf configuration file should match the order and
      * type given here.
      * <p/>
      * For your elevator controllers, you should make sure that the constructor matches
      * the method signatures in ControllerBuilder.makeAll().
      */
     public DoorControl(Hallway hallway, Side side, SimTime period, boolean verbose) {
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
 
         networkHallCallArray = new Utility.HallCallArray(canInterface);
 
         networkCarWeight = CanMailbox.getReadableCanMailbox(
                 MessageDictionary.CAR_WEIGHT_CAN_ID);
         mCarWeight = new CarWeightCanPayloadTranslator(networkCarWeight);
         canInterface.registerTimeTriggered(networkCarWeight);
 
         timer.start(period);
     }
 
     /*
      * The timer callback is where the main controller code is executed.  For time
      * triggered design, this consists mainly of a switch block with a case block for
      * each state.  Each case block executes actions for that state, then executes
      * a transition to the next state if the transition conditions are met.
      */
     @Override
     public void timerExpired(Object callbackData) {
         State newState = state;
         switch (state) {
             case STATE_DOOR_CLOSING:
                 //state actions
                 localDoorMotor.set(DoorCommand.NUDGE);
                 mDoorMotorCommand.set(DoorCommand.NUDGE);
 
                 dwell = mDesiredDwell.getDwell();
                 countDown = SimTime.ZERO;
 
                 //transitions -- note that transition conditions are mutually exclusive
                 //#transition 'T5.5'
                 if (isValidHallway() && isStopped()
                         && ((isDesiredFloor() && isDesiredHallway())
                         || (isOverweight() && !doorOpened())
                         || (isDoorReversal() && !doorOpened()))) {
                     newState = State.STATE_DOOR_OPENING;
 
                 }
                 //#transition 'T5.1'
                 else if (doorClosed()) {
                     newState = State.STATE_DOOR_CLOSED;
                 } else {
                     newState = state;
                 }
                 break;
             case STATE_DOOR_CLOSED:
                 //state actions
                 localDoorMotor.set(DoorCommand.STOP);
                 mDoorMotorCommand.set(DoorCommand.STOP);
 
                 dwell = mDesiredDwell.getDwell();
                 countDown = SimTime.ZERO;
 
                 //transitions
                 //#transition 'T5.2'
                 if (isValidHallway() && isStopped()
                         && ((isDesiredFloor() && isDesiredHallway())
                         || (isOverweight() && !doorOpened())
                         || (isDoorReversal() && !doorOpened()))) {
                     newState = State.STATE_DOOR_OPENING;
                 } else {
                     newState = state;
                 }
                 break;
             case STATE_DOOR_OPENING:
                 //state actions
                 localDoorMotor.set(DoorCommand.OPEN);
                 mDoorMotorCommand.set(DoorCommand.OPEN);
 
                 dwell = mDesiredDwell.getDwell();
                 countDown = new SimTime(dwell, SimTime.SimTimeUnit.SECOND);
 
                 //transitions
                 //#transition 'T5.3'
                 if (doorOpened() && !isOverweight() && !isDoorReversal()) {
                     newState = State.STATE_DOOR_OPEN;
                 }
                 //#transition 'T5.6'
                else if (doorOpened() && (isOverweight() || isDoorReversal())) {
                     newState = State.STATE_DOOR_OPEN_E;
                 } else {
                     newState = state;
                 }
                 break;
             case STATE_DOOR_OPEN:
                 //state actions
                 localDoorMotor.set(DoorCommand.STOP);
                 mDoorMotorCommand.set(DoorCommand.STOP);
 
                 dwell = mDesiredDwell.getDwell();
                 countDown = SimTime.subtract(countDown, period);
 
                 //transitions
                 //#transition 'T5.4'
                 if (countDown.isLessThanOrEqual(SimTime.ZERO)) {
                     newState = State.STATE_DOOR_CLOSING;
 
                 }
                 //#transition 'T5.7'
                 else if (isOverweight() || isDoorReversal()) {
                     newState = State.STATE_DOOR_OPEN_E;
                 } else {
                     newState = state;
                 }
                 break;
             case STATE_DOOR_OPEN_E:
                 //state actions
                 localDoorMotor.set(DoorCommand.STOP);
                 mDoorMotorCommand.set(DoorCommand.STOP);
 
                 dwell = mDesiredDwell.getDwell();
                 countDown = new SimTime(dwell, SimTime.SimTimeUnit.SECOND);
 
                 //transitions
                 //#transition 'T5.8'
                 if (!isOverweight() && !isDoorReversal()) {
                     newState = State.STATE_DOOR_OPEN;
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
 
     private Boolean isValidHallway() {
         if (networkAtFloorArray.getCurrentFloor() == MessageDictionary.NONE) {
             return false;
         } else {
             return Elevator.hasLanding(networkAtFloorArray.getCurrentFloor(), hallway);
         }
     }
 
     private Boolean isStopped() {
         return mDriveSpeed.getSpeed() == Speed.STOP;
     }
 
     private Boolean isOverweight() {
         return mCarWeight.getWeight() >= Elevator.MaxCarCapacity;
     }
 
     private Boolean doorOpened() {
         return mDoorOpened.getValue() == true;
     }
 
     private Boolean doorClosed() {
         return mDoorClosed.getValue() == true;
     }
 
     private Boolean isDoorReversal() {
         return mDoorReversal.getValue() == true;
     }
 
     private Boolean isDesiredFloor() {
         return networkAtFloorArray.getCurrentFloor() == mDesiredFloor.getFloor();
     }
 
     private Boolean isDesiredHallway() {
         return (hallway == mDesiredFloor.getHallway() || mDesiredFloor.getHallway() == Hallway.BOTH);
     }
 }
