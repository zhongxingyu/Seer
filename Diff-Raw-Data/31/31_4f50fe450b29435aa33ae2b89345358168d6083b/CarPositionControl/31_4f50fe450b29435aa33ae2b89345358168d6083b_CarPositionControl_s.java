 /* 18649 Fall 2012
  * (Group  17)
  * Jesse Salazar (jessesal) - Editor
  * Rajeev Sharma (rdsharma) - Author 
  * Collin Buchan (cbuchan)
  * Jessica Tiu   (jtiu)
  */
 
 package simulator.elevatorcontrol;
 
 import jSimPack.SimTime;
 import simulator.elevatormodules.AtFloorCanPayloadTranslator;
 import simulator.elevatormodules.CarLevelPositionCanPayloadTranslator;
 import simulator.elevatormodules.CarPositionIndicator;
 import simulator.elevatormodules.DoorClosedCanPayloadTranslator;
 import simulator.framework.Controller;
 import simulator.framework.Hallway;
 import simulator.framework.ReplicationComputer;
 import simulator.framework.Side;
 import simulator.payloads.*;
 import simulator.payloads.CanMailbox.ReadableCanMailbox;
 import simulator.payloads.CanMailbox.WriteableCanMailbox;
 import simulator.elevatorcontrol.Utility.AtFloorArray;
 import simulator.payloads.CarPositionIndicatorPayload.WriteableCarPositionIndicatorPayload;
 import simulator.payloads.translators.BooleanCanPayloadTranslator;
 import simulator.payloads.translators.IntegerCanPayloadTranslator;
 
 
 /**
  * CarPositionControl displays the current floor of the elevator by actuating CarPositionIndicator
  *
  * @author Collin Buchan, Rajeev Sharma
  */
 public class CarPositionControl extends Controller {
 
     /**
      * ************************************************************************
      * Declarations
      * ************************************************************************
      */
     //note that inputs are Readable objects, while outputs are Writeable objects
 
     //local physical state (local output)
     private WriteableCarPositionIndicatorPayload localCarPositionIndicator;
 
     //output network messages
     private WriteableCanMailbox networkCarPositionIndicator;
     private IntegerCanPayloadTranslator mCarPositionIndicator;
 
     //input network messages
     private AtFloorArray networkAtFloorArray;
     private Utility.CommitPointCalculator networkCommitPointCalculator;
     private ReadableCanMailbox networkDesiredFloor;
     private ReadableCanMailbox networkDriveSpeed;
 
     private DesiredFloorCanPayloadTranslator mDesiredFloor;
     private DriveSpeedCanPayloadTranslator mDriveSpeed;
 
     //store the period for the controller
     private SimTime period;
 
     //enumerate states
     private enum State {
         STATE_DISPLAY_FLOOR,
         STATE_DISPLAY_COMMIT_POINT,
     }
 
     //state variable initialized to the initial state STATE_DISPLAY_FLOOR
     private State state = State.STATE_DISPLAY_FLOOR;
     private int currentFloor;
     private int commitedFloor;
 
     private Boolean[] commitPoint;
 
     /**
      * The arguments listed in the .cf configuration file should match the order and
      * type given here.
      * <p/>
      * For your elevator controllers, you should make sure that the constructor
      * matches the method signatures in the following file:
      * simulator.framework.ControllerBuilder.makeAll()
      * <p/>
      * controllers.add(createControllerObject("CarPositionControl",floor, hallway,
      * MessageDictionary.CAR_BUTTON_CONTROL_PERIOD, verbose));
      */
     public CarPositionControl(SimTime period, boolean verbose) {
         //call to the Controller superclass constructor is required
         super("CarPositionControl", verbose);
 
         //stored the constructor arguments in internal state
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
         log("Created CarPositionControl");
 
         //initialize physical state
         localCarPositionIndicator = CarPositionIndicatorPayload.getWriteablePayload();
         physicalInterface.sendTimeTriggered(localCarPositionIndicator, period);
 
         //initialize input network interface
         networkAtFloorArray = new Utility.AtFloorArray(canInterface);
 
         networkCommitPointCalculator = new Utility.CommitPointCalculator(canInterface);
 
         networkDesiredFloor = CanMailbox.getReadableCanMailbox(MessageDictionary.DESIRED_FLOOR_CAN_ID);
         mDesiredFloor = new DesiredFloorCanPayloadTranslator(networkDesiredFloor);
         canInterface.registerTimeTriggered(networkDesiredFloor);
 
         networkDriveSpeed = CanMailbox.getReadableCanMailbox(MessageDictionary.DRIVE_SPEED_CAN_ID);
         mDriveSpeed = new DriveSpeedCanPayloadTranslator(networkDriveSpeed);
         canInterface.registerTimeTriggered(networkDriveSpeed);
 
         //initialize output network interface
         networkCarPositionIndicator = CanMailbox.getWriteableCanMailbox(MessageDictionary.CAR_POSITION_CAN_ID);
         mCarPositionIndicator = new IntegerCanPayloadTranslator(networkCarPositionIndicator);
         canInterface.sendTimeTriggered(networkCarPositionIndicator, period);
 
         /* issuing the timer start method with no callback data means a NULL value
          * will be passed to the callback later.  Use the callback data to distinguish
          * callbacks from multiple calls to timer.start() (e.g. if you have multiple
          * timers.
          */
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
         switch (state) {
             case STATE_DISPLAY_FLOOR:
                 //state actions for 'DISPLAY_FLOOR'
                 currentFloor = networkAtFloorArray.getCurrentFloor();
                 commitedFloor =
                        networkCommitPointCalculator.nextReachableFloor(
                                 mDriveSpeed.getDirection(),
                                 mDriveSpeed.getSpeed());
 
                 // Make sure we don't set an illegal mCarPositionIndicator
                 if (currentFloor != MessageDictionary.NONE) {
                     mCarPositionIndicator.set(currentFloor);
                     localCarPositionIndicator.set(currentFloor);
                 }
 
                 //transitions -- note that transition conditions are mutually exclusive
                 //#transition 'T10.1'
                 if (currentFloor == MessageDictionary.NONE) {
                     newState = State.STATE_DISPLAY_COMMIT_POINT;
                 } else {
                     newState = state;
                 }
                 break;
             case STATE_DISPLAY_COMMIT_POINT:
                 //state actions for 'DISPLAY_COMMIT_POINT'
                 currentFloor = networkAtFloorArray.getCurrentFloor();
                 commitedFloor =
                        networkCommitPointCalculator.nextReachableFloor(
                                 mDriveSpeed.getDirection(),
                                 mDriveSpeed.getSpeed());
 
                 mCarPositionIndicator.set(commitedFloor);
                 localCarPositionIndicator.set(commitedFloor);
 
                 //transitions -- note that transition conditions are mutually exclusive
                 //#transition 'T10.2'
                 if (currentFloor != MessageDictionary.NONE) {
                     newState = State.STATE_DISPLAY_FLOOR;
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
