 /* 18649 Fall 2012
  * (Group  17)
  * Jesse Salazar (jessesal)
  * Rajeev Sharma (rdsharma) 
  * Collin Buchan (cbuchan)
  * Jessica Tiu   (jtiu) - Author
  * Last modified: 2012-Nov-03
  */
 
 
 package simulator.elevatorcontrol;
 
 import jSimPack.SimTime;
 import simulator.elevatorcontrol.Utility.AtFloorArray;
 import simulator.elevatorcontrol.Utility.DoorClosedHallwayArray;
 import simulator.elevatorcontrol.Utility.CommitPointCalculator;
 import simulator.elevatormodules.CarLevelPositionCanPayloadTranslator;
 import simulator.elevatormodules.CarWeightCanPayloadTranslator;
 import simulator.elevatormodules.DriveObject;
 import simulator.elevatormodules.LevelingCanPayloadTranslator;
 import simulator.framework.Controller;
 import simulator.framework.Direction;
 import simulator.framework.Elevator;
 import simulator.framework.Hallway;
 import simulator.framework.ReplicationComputer;
 import simulator.framework.Speed;
 import simulator.payloads.CanMailbox;
 import simulator.payloads.CanMailbox.ReadableCanMailbox;
 import simulator.payloads.CanMailbox.WriteableCanMailbox;
 import simulator.payloads.DrivePayload;
 import simulator.payloads.DrivePayload.WriteableDrivePayload;
 import simulator.payloads.DriveSpeedPayload;
 import simulator.payloads.DriveSpeedPayload.ReadableDriveSpeedPayload;
 import simulator.payloads.translators.BooleanCanPayloadTranslator;
 
 /**
  * There is one DriveControl, which controls the elevator Drive
  * (the main motor moving Car Up and Down). For simplicity we will assume
  * this node never fails, although the system could be implemented with
  * two such nodes, one per each of the Drive windings.
  *
  * @author Jessica Tiu
  */
 public class DriveControl extends Controller {
 
     /**
      * ************************************************************************
      * Declarations
      * ************************************************************************
      */
     //note that inputs are Readable objects, while outputs are Writeable objects
 
     //local physical state
     private WriteableDrivePayload localDrive;
     private ReadableDriveSpeedPayload localDriveSpeed;
 
     //output network messages
     private WriteableCanMailbox networkDriveOut;
     private WriteableCanMailbox networkDriveSpeedOut;
 
     //translators for output network messages
     private DriveCommandCanPayloadTranslator mDrive;
     private DriveSpeedCanPayloadTranslator mDriveSpeed;
 
     //input network messages
     private ReadableCanMailbox networkLevelUp;
     private ReadableCanMailbox networkLevelDown;
     private ReadableCanMailbox networkEmergencyBrake;
     private ReadableCanMailbox networkCarWeight;
     private ReadableCanMailbox networkDesiredFloor;
     //private ReadableCanMailbox networkCarLevelPosition;
     private DoorClosedHallwayArray networkDoorClosedFront;
     private DoorClosedHallwayArray networkDoorClosedBack;
     private AtFloorArray networkAtFloorArray;
     private CommitPointCalculator networkCommitPoint;
 
     //translators for input network messages
     private LevelingCanPayloadTranslator mLevelUp;
     private LevelingCanPayloadTranslator mLevelDown;
     private BooleanCanPayloadTranslator mEmergencyBrake;
     private CarWeightCanPayloadTranslator mCarWeight;
     private DesiredFloorCanPayloadTranslator mDesiredFloor;
     //private CarLevelPositionCanPayloadTranslator mCarLevelPosition;
 
     //store the period for the controller
     private SimTime period;
 
     //enumerate states
     private enum State {
         STATE_DRIVE_STOPPED,
         STATE_DRIVE_LEVEL,
         STATE_DRIVE_SLOW,
         STATE_DRIVE_FAST,
     }
 
     //state variable initialized to the initial state DRIVE_STOPPED
     private State state = State.STATE_DRIVE_STOPPED;
 
     //macros
     private Direction driveDir = Direction.UP;
 
     //returns the level direction based on mLevel sensors
     private Direction getLevelDir(){
         if (!mLevelUp.getValue() && mLevelDown.getValue()) return Direction.UP;
         else if (mLevelUp.getValue() && !mLevelDown.getValue()) return Direction.DOWN;
         else return Direction.STOP;
     }
 
     //returns the desired direction based on current floor and desired floor by dispatcher
     private Direction getDriveDir(Direction curDirection) {
         Direction driveDir = curDirection;
         int currentFloor = networkAtFloorArray.getCurrentFloor();
         int desiredFloor = mDesiredFloor.getFloor();
 
         //check car is not between floors
         if (currentFloor != MessageDictionary.NONE) {
             //current floor below desired floor
             if (currentFloor < desiredFloor) {
                 driveDir = Direction.UP;
             }
             //current floor above desired floor
             else if (currentFloor > desiredFloor) {
                 driveDir = Direction.DOWN;
             }
             //current floor is desired floor
             else {
                 driveDir = Direction.STOP;
             }
         }
        return driveDir;
     }
 
     /**
      * The arguments listed in the .cf configuration file should match the order and
      * type given here.
      * <p/>
      * For your elevator controllers, you should make sure that the constructor matches
      * the method signatures in ControllerBuilder.makeAll().
      * <p/>
      * controllers.add(createControllerObject("DriveControl",
      * MessageDictionary.DRIVE_CONTROL_PERIOD, verbose));
      */
     public DriveControl(SimTime period, boolean verbose) {
         //call to the Controller superclass constructor is required
         super("DriveControl", verbose);
         this.period = period;
 
         /* 
         * The log() method is inherited from the Controller class.  It takes an
         * array of objects which will be converted to strings and concatenated
         * only if the log message is actually written.
         *
         * For performance reasons, call with comma-separated lists, e.g.:
         *   log("object=",object);
         * Do NOT call with concatenated objects like:
         *   log("object=" + object);
         */
         log("Created DriveControl with period = ", period);
 
         //payloads
         localDrive = DrivePayload.getWriteablePayload();            //output
         localDriveSpeed = DriveSpeedPayload.getReadablePayload();   //input
         physicalInterface.sendTimeTriggered(localDrive, period);
         physicalInterface.registerTimeTriggered(localDriveSpeed);
 
         //output network messages
         networkDriveOut = CanMailbox.getWriteableCanMailbox(MessageDictionary.DRIVE_COMMAND_CAN_ID);
         networkDriveSpeedOut = CanMailbox.getWriteableCanMailbox(MessageDictionary.DRIVE_SPEED_CAN_ID);
         mDrive = new DriveCommandCanPayloadTranslator(networkDriveOut);
         mDriveSpeed = new DriveSpeedCanPayloadTranslator(networkDriveSpeedOut);
         canInterface.sendTimeTriggered(networkDriveOut, period);
         canInterface.sendTimeTriggered(networkDriveSpeedOut, period);
 
         //input network messages
         networkLevelUp =
                 CanMailbox.getReadableCanMailbox(MessageDictionary.LEVELING_BASE_CAN_ID +
                         ReplicationComputer.computeReplicationId(Direction.UP));
         networkLevelDown =
                 CanMailbox.getReadableCanMailbox(MessageDictionary.LEVELING_BASE_CAN_ID +
                         ReplicationComputer.computeReplicationId(Direction.DOWN));
         networkEmergencyBrake =
                 CanMailbox.getReadableCanMailbox(MessageDictionary.EMERGENCY_BRAKE_CAN_ID);
         networkCarWeight =
                 CanMailbox.getReadableCanMailbox(MessageDictionary.CAR_WEIGHT_CAN_ID);
         networkDesiredFloor =
                 CanMailbox.getReadableCanMailbox(MessageDictionary.DESIRED_FLOOR_CAN_ID);
         //networkCarLevelPosition =
         //        CanMailbox.getReadableCanMailbox(MessageDictionary.CAR_POSITION_CAN_ID);
         networkDoorClosedFront = new Utility.DoorClosedHallwayArray(Hallway.FRONT, canInterface);
         networkDoorClosedBack = new Utility.DoorClosedHallwayArray(Hallway.BACK, canInterface);
         networkAtFloorArray = new Utility.AtFloorArray(canInterface);
         networkCommitPoint = new Utility.CommitPointCalculator(canInterface);
 
         mLevelUp =
                 new LevelingCanPayloadTranslator(networkLevelUp, Direction.UP);
         mLevelDown =
                 new LevelingCanPayloadTranslator(networkLevelDown, Direction.DOWN);
         mEmergencyBrake =
                 new BooleanCanPayloadTranslator(networkEmergencyBrake);
         mCarWeight =
                 new CarWeightCanPayloadTranslator(networkCarWeight);
         // used to calculate desiredDir
         mDesiredFloor =
                 new DesiredFloorCanPayloadTranslator(networkDesiredFloor);
         //mCarLevelPosition =
         //        new CarLevelPositionCanPayloadTranslator(networkCarLevelPosition);
 
         canInterface.registerTimeTriggered(networkLevelUp);
         canInterface.registerTimeTriggered(networkLevelDown);
         canInterface.registerTimeTriggered(networkEmergencyBrake);
         canInterface.registerTimeTriggered(networkCarWeight);
         canInterface.registerTimeTriggered(networkDesiredFloor);
         //canInterface.registerTimeTriggered(networkCarLevelPosition);
 
         /* issuing the timer start method with no callback data means a NULL value 
         * will be passed to the callback later.  Use the callback data to distinguish
         * callbacks from multiple calls to timer.start() (e.g. if you have multiple
         * timers.
         */
         timer.start(period);
     }
 
     public void timerExpired(Object callbackData) {
         State newState = state;
 
         switch (state) {
 
             case STATE_DRIVE_STOPPED:
 
                 driveDir = getDriveDir(driveDir);
 
                 //state actions for DRIVE_STOPPED
                 localDrive.set(Speed.STOP, Direction.STOP);
                 mDrive.set(Speed.STOP, Direction.STOP);
                 mDriveSpeed.set(localDriveSpeed.speed(), localDriveSpeed.direction());
 
                 //transitions
 
                 //#transition 'T6.1'
                 if (driveDir==Direction.STOP && (!mLevelUp.getValue() || !mLevelDown.getValue())) {
                     newState = State.STATE_DRIVE_LEVEL;
                 }
 
                 //#transition 'T6.3'
                 else if (driveDir!=Direction.STOP
                         && networkDoorClosedFront.getAllClosed() && networkDoorClosedBack.getAllClosed()){
                     newState = State.STATE_DRIVE_SLOW;
                 }
 
                 else newState = state;
 
                 break;
 
             case STATE_DRIVE_LEVEL:
 
                 driveDir = getDriveDir(driveDir);
                 Direction levelDir = getLevelDir();
 
                 //state actions for DRIVE_LEVEL
                 localDrive.set(Speed.LEVEL, levelDir);
                 mDrive.set(Speed.LEVEL, levelDir);
                 mDriveSpeed.set(localDriveSpeed.speed(), localDriveSpeed.direction());
 
                 //transitions
 
                 //#transition 'T6.2'
                 if (driveDir==Direction.STOP && mLevelUp.getValue() && mLevelDown.getValue()
                         || mEmergencyBrake.getValue()) {
                     newState = State.STATE_DRIVE_STOPPED;
                 }
 
                 else newState = state;
 
                 break;
 
             case STATE_DRIVE_SLOW:
 
                 driveDir = getDriveDir(driveDir);
 
                 //state actions for DRIVE_SLOW
                 localDrive.set(Speed.SLOW, driveDir);
                 mDrive.set(Speed.SLOW, driveDir);
                 mDriveSpeed.set(localDriveSpeed.speed(), localDriveSpeed.direction());
 
                 //transitions
 
                 //#transition 'T6.4'
                 if (driveDir==Direction.STOP && (!mLevelUp.getValue() || !mLevelDown.getValue())
                         || mCarWeight.getWeight() >= Elevator.MaxCarCapacity) {
                     newState = State.STATE_DRIVE_LEVEL;
                 }
 
                 //#transition 'T6.5'
                 else if (driveDir!=Direction.STOP
                         && !networkCommitPoint.commitPoint(
                             mDesiredFloor.getFloor(),localDriveSpeed.direction(),localDriveSpeed.speed())) {
                     newState = State.STATE_DRIVE_FAST;
                 }
 
                 //#transition 'T6.7'
                 else if (mEmergencyBrake.getValue()) {
                     newState = State.STATE_DRIVE_STOPPED;
                 }
 
                 else newState = state;
 
                 break;
 
             case STATE_DRIVE_FAST:
 
                 driveDir = getDriveDir(driveDir);
 
                 //state actions for DRIVE_FAST
                 localDrive.set(Speed.FAST, driveDir);
                 mDrive.set(Speed.FAST, driveDir);
                 mDriveSpeed.set(localDriveSpeed.speed(), localDriveSpeed.direction());
 
                 //transitions
 
                 //#transition 'T6.6'
                 if (driveDir!=Direction.STOP
                         && networkCommitPoint.commitPoint(
                             mDesiredFloor.getFloor(),localDriveSpeed.direction(),localDriveSpeed.speed())
                         || mCarWeight.getWeight() >= Elevator.MaxCarCapacity) {
                     newState = State.STATE_DRIVE_SLOW;
                 }
 
                 //#transition 'T6.8'
                 else if (mEmergencyBrake.getValue()) {
                     newState = State.STATE_DRIVE_STOPPED;
                 }
 
                 else newState = state;
 
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
 
