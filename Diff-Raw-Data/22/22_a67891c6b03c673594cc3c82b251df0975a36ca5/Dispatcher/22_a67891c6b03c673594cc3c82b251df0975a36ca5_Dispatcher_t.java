 /* 18649 Fall 2012
  * (Group  17)
  * Jesse Salazar (jessesal) - Author
  * Rajeev Sharma (rdsharma) 
  * Collin Buchan (cbuchan) - Editor
  * Jessica Tiu   (jtiu)
  */
 
 
 package simulator.elevatorcontrol;
 
 import jSimPack.SimTime;
 import simulator.elevatorcontrol.Utility.*;
 import simulator.elevatormodules.CarLevelPositionCanPayloadTranslator;
 import simulator.elevatormodules.CarWeightCanPayloadTranslator;
 import simulator.framework.Controller;
 import simulator.framework.Direction;
 import simulator.framework.Hallway;
 import simulator.framework.ReplicationComputer;
 import simulator.payloads.CanMailbox;
 import simulator.payloads.CanMailbox.ReadableCanMailbox;
 import simulator.payloads.CanMailbox.WriteableCanMailbox;
 
 
 /**
  * There is one DriveControl, which controls the elevator Drive
  * (the main motor moving Car Up and Down). For simplicity we will assume
  * this node never fails, although the system could be implemented with
  * two such nodes, one per each of the Drive windings.
  *
  * @author Collin Buchan, Jesse Salazar
  */
 public class Dispatcher extends Controller {
 
     public static final byte FRONT_LAND = 0;
     public static final byte BACK_LAND = 1;
 
     /**
      * ************************************************************************
      * Declarations
      * ************************************************************************
      */
     //note that inputs are Readable objects, while outputs are Writeable objects
 
     //output network messages
     private WriteableCanMailbox networkDesiredFloor;
     private WriteableCanMailbox networkDesiredDwellFront;
     private WriteableCanMailbox networkDesiredDwellBack;
 
 
     //translators for output network messages
     private DesiredFloorCanPayloadTranslator mDesiredFloor;
     private DesiredDwellCanPayloadTranslator mDesiredDwellFront;
     private DesiredDwellCanPayloadTranslator mDesiredDwellBack;
 
 
     //input network messages
     private AtFloorArray networkAtFloorArray;
     private DoorClosedArray networkDoorClosed;
     private HallCallArray networkHallCallArray;
     private CarCallArray networkCarCallArrayFront;
     private CarCallArray networkCarCallArrayBack;
     private ReadableCanMailbox networkCarWeight;
     private ReadableCanMailbox networkDriveSpeed;
     private ReadableCanMailbox networkCarLevelPosition;
 
     //translators for input network messages
     private CarWeightCanPayloadTranslator mCarWeight;
     private DriveSpeedCanPayloadTranslator mDriveSpeed;
     private CarLevelPositionCanPayloadTranslator mCarLevelPosition;
 
     // CommitPoint interface
     private CommitPointCalculator commitPointCalculator;
 
     //these variables keep track of which instance this is.
     private final int numFloors;
 
     //store the period for the controller
     private SimTime period;
 
     //enumerate states
     private enum State {
         STATE_RESET,
         STATE_IDLE,
         STATE_SERVICE_CALL,
         STATE_COMPUTE_NEXT,
     }
 
     //state variable initialized to the initial state STATE_INIT
     private int CONST_DWELL = 10;
     private State state = State.STATE_RESET;
     private int targetFloor;
     private Hallway targetHallway;
     private Direction direction;
     private int commitPoint;
 
 
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
     public Dispatcher(int numFloors, SimTime period, boolean verbose) {
         //call to the Controller superclass constructor is required
         super("Dispatcher", verbose);
         this.period = period;
         this.numFloors = numFloors;
 
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
         log("Created Dispatcher with period = ", period);
 
         //create CAN mailbox for output network messages
         networkDesiredFloor = CanMailbox.getWriteableCanMailbox(MessageDictionary.DESIRED_FLOOR_CAN_ID);
         networkDesiredDwellFront = CanMailbox.getWriteableCanMailbox(
                 MessageDictionary.DESIRED_DWELL_BASE_CAN_ID + ReplicationComputer.computeReplicationId(Hallway.FRONT));
         networkDesiredDwellBack = CanMailbox.getWriteableCanMailbox(
                 MessageDictionary.DESIRED_DWELL_BASE_CAN_ID + ReplicationComputer.computeReplicationId(Hallway.BACK));
 
         /*
         * Create a translator with a reference to the CanMailbox.  Use the
         * translator to read and write values to the mailbox
         */
         mDesiredFloor = new DesiredFloorCanPayloadTranslator(networkDesiredFloor);
         mDesiredDwellFront = new DesiredDwellCanPayloadTranslator(networkDesiredDwellFront, Hallway.FRONT);
         mDesiredDwellBack = new DesiredDwellCanPayloadTranslator(networkDesiredDwellBack, Hallway.BACK);
 
 
         //register the mailbox to have its value broadcast on the network periodically
         //with a period specified by the period parameter.
         canInterface.sendTimeTriggered(networkDesiredFloor, period);
         canInterface.sendTimeTriggered(networkDesiredDwellFront, period);
         canInterface.sendTimeTriggered(networkDesiredDwellBack, period);
 
         /*
         * To register for network messages from the smart sensors or other objects
         * defined in elevator modules, use the translators already defined in
         * elevatormodules package.  These translators are specific to one type
         * of message.
         */
         networkAtFloorArray = new AtFloorArray(canInterface);
         networkDoorClosed = new DoorClosedArray(canInterface);
         networkHallCallArray = new HallCallArray(canInterface);
         networkCarCallArrayFront = new CarCallArray(Hallway.FRONT, canInterface);
         networkCarCallArrayBack = new CarCallArray(Hallway.BACK, canInterface);
         networkCarWeight = CanMailbox.getReadableCanMailbox(MessageDictionary.CAR_WEIGHT_CAN_ID);
         networkDriveSpeed = CanMailbox.getReadableCanMailbox(MessageDictionary.DRIVE_SPEED_CAN_ID);
         networkCarLevelPosition = CanMailbox.getReadableCanMailbox(MessageDictionary.CAR_LEVEL_POSITION_CAN_ID);
 
         // translators
         mCarWeight = new CarWeightCanPayloadTranslator(networkCarWeight);
         mDriveSpeed = new DriveSpeedCanPayloadTranslator(networkDriveSpeed);
         mCarLevelPosition = new CarLevelPositionCanPayloadTranslator(networkCarLevelPosition);
 
         //register to receive periodic updates to the mailbox via the CAN network
         //the period of updates will be determined by the sender of the message
         canInterface.registerTimeTriggered(networkCarWeight);
         canInterface.registerTimeTriggered(networkDriveSpeed);
         canInterface.registerTimeTriggered(networkCarLevelPosition);
 
         commitPointCalculator = new CommitPointCalculator(canInterface);
 
         /* issuing the timer start method with no callback data means a NULL value 
         * will be passed to the callback later.  Use the callback data to distinguish
         * callbacks from multiple calls to timer.start() (e.g. if you have multiple
         * timers.
         */
         timer.start(period);
     }
 
     /*
     * The timer callback is where the main controller code is executed.  For time
     * triggered design, this consists mainly of a switch block with a case blcok for
     * each state.  Each case block executes actions for that state, then executes
     * a transition to the next state if the transition conditions are met.
     */
     public void timerExpired(Object callbackData) {
         State newState = state;
 
         switch (state) {
 
             case STATE_RESET:
 
                 //state actions for STATE_RESET
                 targetFloor = 1;
                 targetHallway = Hallway.NONE;
 
                 mDesiredFloor.setFloor(targetFloor);
                 mDesiredFloor.setHallway(targetHallway);
                 mDesiredFloor.setDirection(Direction.STOP);
 
                 mDesiredDwellBack.set(CONST_DWELL);
                 mDesiredDwellFront.set(CONST_DWELL);
 
                 //#transition 'T11.1'
                 if (networkAtFloorArray.getCurrentFloor() == 1 && networkDoorClosed.getAllClosed()) {
                     newState = State.STATE_IDLE;
                 } else {
                     newState = state;
                 }
                 break;
 
             case STATE_IDLE:
 
                 //state actions for STATE_IDLE
                 direction = Direction.STOP;
                 commitPoint = networkAtFloorArray.getCurrentFloor();
 
                 mDesiredFloor.setFloor(networkAtFloorArray.getCurrentFloor());
                 mDesiredFloor.setHallway(Hallway.NONE);
                 mDesiredFloor.setDirection(direction);
 
                 mDesiredDwellBack.set(CONST_DWELL);
                 mDesiredDwellFront.set(CONST_DWELL);
 
                 //#transition 'T11.2'
                 if (!allCallsOff()) {
                     newState = State.STATE_COMPUTE_NEXT;
                 }
 
                 break;
 
             case STATE_COMPUTE_NEXT:
 
                commitPoint = nextReachableFloor(commitPoint);
 
 
                 //state actions for STATE_COMPUTE_NEXT
                 targetFloor = computeNextFloor(commitPoint, direction);
 
                 //set the target Hallway to be as many floors as are called for
 //                targetHallway = getHallways(targetFloor, Direction.STOP);
 
                 targetHallway = getHallways(targetFloor, direction);
 
                 mDesiredFloor.setFloor(targetFloor);
                 mDesiredFloor.setHallway(targetHallway);
                 mDesiredFloor.setDirection(direction);
 
                 mDesiredDwellBack.set(CONST_DWELL);
                 mDesiredDwellFront.set(CONST_DWELL);
 
 
                 //#transition 'T11.3'
                 if (commitPoint == targetFloor) {
                     newState = State.STATE_SERVICE_CALL;
                 }
 
                 //#transition 'T11.6'
                 else if (networkAtFloorArray.getCurrentFloor() == MessageDictionary.NONE && !networkDoorClosed.getAllClosed()) {
                     newState = State.STATE_RESET;
                 } else {
                     newState = state;
                 }
 
                 break;
 
             case STATE_SERVICE_CALL:
 
                 commitPoint = computeCommitPoint(commitPoint);
                 direction = computeDirection(direction, commitPoint);
 
                 //state actions for STATE_SERVICE_CALL
                 mDesiredFloor.setFloor(targetFloor);
                 mDesiredFloor.setHallway(targetHallway);
                 mDesiredFloor.setDirection(direction);
 
                 mDesiredDwellBack.set(CONST_DWELL);
                 mDesiredDwellFront.set(CONST_DWELL);
 
                 //#transition 'T11.4
                 if (allCallsOff() && !networkDoorClosed.getAllClosed()) {
                     newState = State.STATE_IDLE;
                 }
                 //#transition 'T11.5
                 else if (!allCallsOff() && !networkDoorClosed.getAllClosed()) {
                     newState = State.STATE_COMPUTE_NEXT;
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
 
 //        System.out.println(
 //                "State: " + state + " " + mDesiredFloor.getFloor() + " " + mDesiredFloor.getHallway() + " " + mDesiredFloor.getDirection());
 
         //update the state variable
         state = newState;
 
         //report the current state
         setState(STATE_KEY, newState.toString());
 
         //schedule the next iteration of the controller
         //you must do this at the end of the timer callback in order to restart
         //the timer
         timer.start(period);
     }
 
     /**
      * ************************************************************************
      * Macros
      * ************************************************************************
      */
 
     /*
     * returns True if any calls are found at or above the commitPoint.
     * returns False otherwise.
     */
     private boolean allCallsOff() {
         return networkHallCallArray.getAllOff() && networkCarCallArrayBack.getAllOff() && networkCarCallArrayFront.getAllOff();
     }
 
     /*
     * returns True if any calls are found at or above the commitPoint.
     * Only UP hall calls are checked.
     * returns False otherwise.
     */
     private int nextUpCall(int commitPoint) {
         for (int floor = commitPoint; floor <= numFloors; floor++) {
             if ((carUnderCapacity() && anyHallCall(floor, Direction.UP))
                     || anyCarCall(floor)) {
                 return floor;
             }
         }
         return MessageDictionary.NONE;
     }
 
     private int nextCallAbove(int commitPoint) {
         for (int floor = commitPoint + 1; floor <= numFloors; floor++) {
             if (anyHallCall(floor, Direction.UP) || anyHallCall(floor, Direction.DOWN) || anyCarCall(floor)) {
                 return floor;
             }
         }
         return MessageDictionary.NONE;
     }
 
     /*
     * returns True if any calls are found at or above the commitPoint.
     * Only UP hall calls are checked.
     * returns False otherwise.
     */
     private boolean anyUpCall(int commitPoint) {
         if (nextUpCall(commitPoint) != MessageDictionary.NONE) {
             return true;
         } else {
             return false;
         }
     }
 
     private int nextDownCall(int commitPoint) {
         for (int floor = commitPoint; floor >= 1; floor--) {
             if ((carUnderCapacity() && anyHallCall(floor, Direction.DOWN)) ||
                     anyCarCall(floor)) {
                 return floor;
             }
         }
         return MessageDictionary.NONE;
     }
 
     private int nextCallBelow(int commitPoint) {
         for (int floor = commitPoint - 1; floor >= 1; floor--) {
             if (anyHallCall(floor, Direction.UP) || anyHallCall(floor, Direction.DOWN) || anyCarCall(floor)) {
                 return floor;
             }
         }
         return MessageDictionary.NONE;
     }
 
     /*
     * returns True if any calls are found at or below the commitPoint.
     * Only DOWN hall calls are checked
     * returns False otherwise.
     */
     private boolean anyDownCall(int commitPoint) {
         if (nextDownCall(commitPoint) != MessageDictionary.NONE) {
             return true;
         } else {
             return false;
         }
     }
 
     private boolean anyCarCall(int floor) {
         return networkCarCallArrayFront.getValueForFloor(floor) || networkCarCallArrayBack.getValueForFloor(floor);
     }
 
     private boolean anyHallCall(int floor, Direction dir) {
         return networkHallCallArray.getValue(floor, Hallway.FRONT, dir) || networkHallCallArray.getValue(floor, Hallway.BACK, dir);
     }
 
     private boolean carUnderCapacity() {
         return mCarWeight.getWeight() < 13500;
     }
 
 
     private int computeNextFloor(int commitPoint, Direction dir) {
         //Car moving, DON'T CHANGE DIRECTION
         if (mDriveSpeed.getDirection() == Direction.UP) {
             if (nextUpCall(commitPoint) != MessageDictionary.NONE) {
                 return nextUpCall(commitPoint);
             } else if (nextCallAbove(commitPoint) != MessageDictionary.NONE) {
                 return nextCallAbove(commitPoint);
             } else {
                 return commitPoint;
             }
         } else if (mDriveSpeed.getDirection() == Direction.DOWN) {
             if (nextDownCall(commitPoint) != MessageDictionary.NONE) {
                 return nextDownCall(commitPoint);
             } else if (nextCallBelow(commitPoint) != MessageDictionary.NONE) {
                 return nextCallBelow(commitPoint);
             } else {
                 return commitPoint;
             }
         }
         //Car stopped, use desired direction
         else {
             // Traveling UP
             if (dir == Direction.UP) {
                 if (nextUpCall(commitPoint) != MessageDictionary.NONE) {
                     return nextUpCall(commitPoint);
                 } else if (nextCallAbove(commitPoint) != MessageDictionary.NONE) {
                     return nextCallAbove(commitPoint);
                 } else {
                     return MessageDictionary.NONE;
                 }
             }
             // Traveling DOWN
             else if (dir == Direction.DOWN) {
                 if (nextDownCall(commitPoint) != MessageDictionary.NONE) {
                     return nextDownCall(commitPoint);
                 } else if (nextCallBelow(commitPoint) != MessageDictionary.NONE) {
                     return nextCallBelow(commitPoint);
                 } else {
                     return MessageDictionary.NONE;
                 }
             }
             // Stopped
             else if (closestCall(commitPoint, numFloors) != MessageDictionary.NONE) {
                 return closestCall(commitPoint, numFloors);
             } else {
                 return MessageDictionary.NONE;
             }
         }
     }
 
     /*
     * Computes a new direction based on the current floor and previous direction.
     * Will try to keep going in the same direction if any calls are yet to be serviced in that direction.
     * Otherwise, it will switch directions or STOP (if no calls are found).
     */
     private Direction computeDirection(Direction oldDir, int currentFloor) {
         // Previously traveling UP
         if (oldDir == Direction.UP) {
             if (anyHallCall(currentFloor, Direction.UP) || (nextCallAbove(currentFloor) != MessageDictionary.NONE)) {
                 return Direction.UP;
             } else if (nextCallBelow(currentFloor) != MessageDictionary.NONE) {
                 return Direction.DOWN;
             } else {
                 return Direction.STOP;
             }
         }
         // Previously traveling DOWN
         else if (oldDir == Direction.DOWN) {
             if (anyHallCall(currentFloor, Direction.DOWN) || (nextCallBelow(currentFloor) != MessageDictionary.NONE)) {
                 return Direction.DOWN;
             } else if (nextCallAbove(currentFloor) != MessageDictionary.NONE) {
                 return Direction.UP;
             } else {
                 return Direction.STOP;
             }
         }
         // Previous direction STOP
         else {
             if (!allCallsOff()) {
                 return directionOfClosestCall(currentFloor, numFloors);
             } else {
                 return Direction.STOP;
             }
         }
     }
 
     /*
     * Gives direction of closest lit call button (hall or car), with bias towards UP in a tie.
     * Returns STOP if no call is found.
     */
     private Direction directionOfClosestCall(int floor, int maxFloor) {
 
         int closestCall = closestCall(floor, maxFloor);
         if (closestCall == MessageDictionary.NONE || closestCall == floor) {
             return Direction.STOP;
         } else if (floor <= closestCall) {
             return Direction.UP;
         } else if (floor >= closestCall) {
             return Direction.DOWN;
         } else {
             return Direction.STOP;
         }
     }
 
     /*
     * Returns the number of the floor of the closest lit call button (hall or car), with bias towards UP in a tie.
     * Returns NONE if no call is found.
     */
     private int closestCall(int commitPoint, int maxFloor) {
         for (int i = 0; i <= Math.max(commitPoint, maxFloor - commitPoint); i++) {
             //Check above
             int tempFloor = commitPoint + i;
             if (validFloor(tempFloor, maxFloor) && (getLitHallways(tempFloor, Direction.UP) != Hallway.NONE ||
                     getLitHallways(tempFloor, Direction.DOWN) != Hallway.NONE) ||
                     getLitHallways(tempFloor, Direction.STOP) != Hallway.NONE) {
                 return tempFloor;
             }
             //Check below
             tempFloor = commitPoint - i;
             if (validFloor(tempFloor, maxFloor) && (getLitHallways(tempFloor, Direction.UP) != Hallway.NONE ||
                     getLitHallways(tempFloor, Direction.DOWN) != Hallway.NONE) ||
                     getLitHallways(tempFloor, Direction.STOP) != Hallway.NONE) {
                 return tempFloor;
             }
         }
         return MessageDictionary.NONE;
     }
 
 
     /*
     * Returns True if the given floor is between 1 and maxFloor
     * Returns False otherwise.
     */
     private boolean validFloor(int floor, int maxFloor) {
         return floor <= maxFloor && floor >= 1;
     }
 
     private Hallway getHallways(int targetFloor, Direction direction) {
 
         Hallway litHallway;
 
         if (getLitHallways(targetFloor, direction) == Hallway.NONE) {
             litHallway = getLitHallways(targetFloor, Direction.STOP);
         } else {
             litHallway = getLitHallways(targetFloor, direction);
         }
 
         return litHallway;
 
     }
 
     /*
     * For a given floor, returns all hallways for which a valid call has been lit.
     */
     private Hallway getLitHallways(int floor, Direction dir) {
         Hallway desiredHallway;
 
         boolean frontCall;
         boolean backCall;
 
         if (dir == Direction.STOP) {
             frontCall =
                     networkHallCallArray.getValue(floor, Hallway.FRONT, Direction.UP) ||
                             networkHallCallArray.getValue(floor, Hallway.FRONT, Direction.DOWN) ||
                             networkCarCallArrayFront.getValueForFloor(floor);
             backCall = networkHallCallArray.getValue(floor, Hallway.BACK, Direction.UP) ||
                     networkHallCallArray.getValue(floor, Hallway.BACK, Direction.DOWN) || networkCarCallArrayBack.getValueForFloor(floor);
         } else {
             frontCall =
                     networkHallCallArray.getValue(floor, Hallway.FRONT, dir) || networkCarCallArrayFront.getValueForFloor(floor);
             backCall = networkHallCallArray.getValue(floor, Hallway.BACK, dir) || networkCarCallArrayBack.getValueForFloor(floor);
         }
 
 
         if (frontCall && !backCall) {
             desiredHallway = Hallway.FRONT;
         } else if (backCall && !frontCall) {
             desiredHallway = Hallway.BACK;
         } else if (backCall && frontCall) {
             desiredHallway = Hallway.BOTH;
         } else {
             desiredHallway = Hallway.NONE;
         }
         return desiredHallway;
     }
 
 
     private int computeCommitPoint(int oldFloor) {
         if (networkAtFloorArray.getCurrentFloor() != MessageDictionary.NONE && mDriveSpeed.getSpeed() <= 0.05) {
             return networkAtFloorArray.getCurrentFloor();
         } else {
 
             int newFloor = commitPointCalculator.nextReachableFloor(mDriveSpeed.getDirection(), mDriveSpeed.getSpeed());
             if (mDriveSpeed.getDirection() == Direction.UP && newFloor > oldFloor) {
                 return newFloor;
             } else if (mDriveSpeed.getDirection() == Direction.DOWN && newFloor < oldFloor) {
                 return newFloor;
             }
             return oldFloor;
         }
     }
 
    private int nextReachableFloor(int oldFloor) {

        int nextReachable = commitPointCalculator.nextReachableFloor(mDriveSpeed.getDirection(), mDriveSpeed.getSpeed());
        int computePoint = commitPointCalculator.getCommitedFloor(mDriveSpeed.getDirection(), mDriveSpeed.getSpeed());

        if (!(mDriveSpeed.getDirection() == Direction.DOWN && computePoint < oldFloor) &&
                !(mDriveSpeed.getDirection() == Direction.UP && computePoint > oldFloor)) {
            computePoint = oldFloor;
        }

        if (mDriveSpeed.getDirection() == Direction.UP) {
            return Math.max(nextReachable, computePoint);
        } else if (mDriveSpeed.getDirection() == Direction.DOWN) {
            return Math.min(nextReachable, computePoint);
        } else {
            return nextReachable;
        }
     }
 }
