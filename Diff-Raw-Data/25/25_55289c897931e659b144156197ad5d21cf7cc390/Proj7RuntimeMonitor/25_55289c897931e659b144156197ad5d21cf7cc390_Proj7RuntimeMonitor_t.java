 package simulator.elevatorcontrol;
 
 import jSimPack.SimTime;
 import simulator.framework.Direction;
 import simulator.framework.DoorCommand;
 import simulator.framework.Elevator;
 import simulator.framework.Hallway;
 import simulator.framework.Harness;
 import simulator.framework.RuntimeMonitor;
 import simulator.framework.Side;
 import simulator.payloads.AtFloorPayload.ReadableAtFloorPayload;
 import simulator.payloads.CarCallPayload.ReadableCarCallPayload;
 import simulator.payloads.CarLanternPayload.ReadableCarLanternPayload;
 import simulator.payloads.CarWeightPayload.ReadableCarWeightPayload;
 import simulator.payloads.DoorClosedPayload.ReadableDoorClosedPayload;
 import simulator.payloads.DoorMotorPayload.ReadableDoorMotorPayload;
 import simulator.payloads.DoorOpenPayload.ReadableDoorOpenPayload;
 import simulator.payloads.DriveSpeedPayload.ReadableDriveSpeedPayload;
 import simulator.payloads.HallCallPayload.ReadableHallCallPayload;
 
 public class Proj7RuntimeMonitor extends RuntimeMonitor{
 
 	protected int currentFloor = MessageDictionary.NONE;
 	Stopwatch watch = new Stopwatch();
     DoorStateMachine doorState = new DoorStateMachine();
     WeightStateMachine weightState = new WeightStateMachine();
     boolean hasMoved = false;
     boolean wasOverweight = false;
     boolean wasCarCall[][] = new boolean[Elevator.numFloors][2];
     boolean wasHallCall[][][] = new boolean[Elevator.numFloors][2][2];
     boolean litLantern[] = new boolean[2];
     boolean wasCall = false;
     boolean bothLit = false;
     int wastedOpeningCount = 0;
     int overWeightCount = 0;
    Hallway hallway = Hallway.NONE;
 
     public Proj7RuntimeMonitor() {
     }
 
     @Override
     protected String[] summarize() {
         String[] arr = new String[4];
         arr[0] = "Overweight Count = " + overWeightCount;
        arr[1] = "Wasted Openings Count = " + wastedOpeningCount;
         arr[2] = "Wasted Time Dealing with Reversal = " + watch.getAccumulatedTime();
         arr[3] = "Both lanterns lit up? " + bothLit;
         return arr;
     }
 
     public void timerExpired(Object callbackData) {

//        System.out.println("CF: "+ currentFloor);
         //do nothing
     }
 
     /**************************************************************************
      * high level event methods
      *
      * these are called by the logic in the message receiving methods and the
      * state machines
      **************************************************************************/
     /**
      * Called once when the door starts opening
      * @param hallway which door the event pertains to
      */
     private void doorOpening(Hallway hallway) {
     	CheckHallorCarCall(hallway);
         if (!wasCall) {
         	message("Wasted Opening");
         	wastedOpeningCount++;
         }
         wasCall = false;
     }
 
     /**
      * Called once when the door starts closing
      * @param hallway which door the event pertains to
      */
     private void doorClosing(Hallway hallway) {
         //System.out.println(hallway.toString() + " Door Closing");
     }
 
     /**
      * Called once if the door starts opening after it started closing but before
      * it was fully closed.
      * @param hallway which door the event pertains to
      */
     private void doorReopening(Hallway hallway) {
     	message("Logging wasted time for reversal");
     	watch.start();
         //System.out.println(hallway.toString() + " Door Reopening");
     }
 
     /**
      * Called once when the doors close completely
      * @param hallway which door the event pertains to
      */
     private void doorClosed(Hallway hallway) {
         //System.out.println(hallway.toString() + " Door Closed");
         //once all doors are closed, check to see if the car was overweight
         if (!doorState.anyDoorOpen()) {
         	watch.stop();
             if (wasOverweight) {
                 message("Overweight");
                 overWeightCount++;
                 wasOverweight = false;
             }
         }
     }
 
     /**
      * Called once when the doors are fully open
      * @param hallway which door the event pertains to
      */
     private void doorOpened(Hallway hallway) {
         //System.out.println(hallway.toString() + " Door Opened");
     }
 
     /**
      * Called when the car weight changes
      * @param hallway which door the event pertains to
      */
     private void weightChanged(int newWeight) {
         if (newWeight > Elevator.MaxCarCapacity) {
             wasOverweight = true;
         }
     }
 
     /**************************************************************************
      * low level message receiving methods
      * 
      * These mostly forward messages to the appropriate state machines
      **************************************************************************/
     @Override
     public void receive(ReadableDoorClosedPayload msg) {
         doorState.receive(msg);
     }
 
     @Override
     public void receive(ReadableDoorOpenPayload msg) {
         doorState.receive(msg);
     }
 
     @Override
     public void receive(ReadableDoorMotorPayload msg) {
         doorState.receive(msg);
     }
 
     @Override
     public void receive(ReadableCarWeightPayload msg) {
         weightState.receive(msg);
     }
 
     @Override
     public void receive(ReadableDriveSpeedPayload msg) {
         if (msg.speed() > 0) {
             hasMoved = true;
         }
     }
 
     @Override
     public void receive(ReadableAtFloorPayload msg) {
     	updateCurrentFloor(msg);
     }
     
     @Override
     public void receive(ReadableHallCallPayload msg) {
     	if(msg.pressed())
    	    wasHallCall[msg.getFloor()-1][msg.getHallway().ordinal()][msg.getDirection().ordinal()] = true;	
     }
     
     @Override
     public void receive(ReadableCarCallPayload msg) {
     	if(msg.pressed())
     	wasCarCall[msg.getFloor()-1][msg.getHallway().ordinal()] = true;
     }
     
     @Override
     public void receive(ReadableCarLanternPayload msg) {
    	if(msg.lighted()) {
     		litLantern[msg.getDirection().ordinal()] = true;
        	wasHallCall[currentFloor-1][hallway.ordinal()][msg.getDirection().ordinal()] = false;
        }
     	else
     		litLantern[msg.getDirection().ordinal()] = false;
     }
 
     private static enum DoorState {
 
         CLOSED,
         OPENING,
         OPEN,
         CLOSING
     }
     
     private void checkLantern() {
     	if (litLantern[0] && litLantern[1])
     		bothLit = true;
     }
     private void updateCurrentFloor(ReadableAtFloorPayload lastAtFloor) {
         if (lastAtFloor.getFloor() == currentFloor) {
             //the atFloor message is for the currentfloor, so check both sides to see if they are both false
             if (!atFloors[lastAtFloor.getFloor()-1][Hallway.BACK.ordinal()].value() && !atFloors[lastAtFloor.getFloor()-1][Hallway.FRONT.ordinal()].value()) {
                 //both sides are false, so set to NONE
                 currentFloor = MessageDictionary.NONE;
             }
             //otherwise at least one side is true, so leave the current floor as is
         } else {
             if (lastAtFloor.value()) {
                 currentFloor = lastAtFloor.getFloor();
             }
         }
     }
     
     /**
      * Utility class to detect weight changes
      */
     private class WeightStateMachine {
 
         int oldWeight = 0;
 
         public void receive(ReadableCarWeightPayload msg) {
             if (oldWeight != msg.weight()) {
                 weightChanged(msg.weight());
             }
             oldWeight = msg.weight();
         }
     }
     
 
     public void CheckHallorCarCall(Hallway h){
     	for(Direction d : Direction.replicationValues){
//    		System.out.println("currentFloor: " + currentFloor);
                if(wasCarCall[currentFloor-1][h.ordinal()] || 
     			wasHallCall[currentFloor-1][h.ordinal()][d.ordinal()]){
     			wasCall = true;
                        hallway = h;
     			wasCarCall[currentFloor-1][h.ordinal()] = false;
//    			wasHallCall[currentFloor-1][h.ordinal()][d.ordinal()] = false;
     		}
     	}	
     }
     
     /**
      * Utility class for keeping track of the door state.
      * 
      * Also provides external methods that can be queried to determine the
      * current door state.
      */
     private class DoorStateMachine {
 
         DoorState state[] = new DoorState[2];
 
         public DoorStateMachine() {
             state[Hallway.FRONT.ordinal()] = DoorState.CLOSED;
             state[Hallway.BACK.ordinal()] = DoorState.CLOSED;
         }
 
         public void receive(ReadableDoorClosedPayload msg) {
             updateState(msg.getHallway());
         }
 
         public void receive(ReadableDoorOpenPayload msg) {
             updateState(msg.getHallway());
         }
 
         public void receive(ReadableDoorMotorPayload msg) {
             updateState(msg.getHallway());
         }
 
         private void updateState(Hallway h) {
             DoorState previousState = state[h.ordinal()];
 
             DoorState newState = previousState;
 
             if (allDoorsClosed(h) && allDoorMotorsStopped(h)) {
                 newState = DoorState.CLOSED;
             } else if (allDoorsCompletelyOpen(h) && allDoorMotorsStopped(h)) {
                 newState = DoorState.OPEN;
                 //} else if (anyDoorMotorClosing(h) && anyDoorOpen(h)) {
             } else if (anyDoorMotorClosing(h)) {
                 newState = DoorState.CLOSING;
             } else if (anyDoorMotorOpening(h)) {
                 newState = DoorState.OPENING;
             }
             
             if (newState != previousState) {
                 switch (newState) {
                     case CLOSED:
                         doorClosed(h);
                         break;
                     case OPEN:
                         doorOpened(h);
                         break;
                     case OPENING:
                         if (previousState == DoorState.CLOSING) {
                             doorReopening(h);
                         } else {
                             doorOpening(h);         
                         }
                         break;
                     case CLOSING:
                         doorClosing(h);
                         break;
 
                 }
             }
 
             //set the newState
             state[h.ordinal()] = newState;
         }
 
         //door utility methods
         public boolean allDoorsCompletelyOpen(Hallway h) {
             return doorOpeneds[h.ordinal()][Side.LEFT.ordinal()].isOpen()
                     && doorOpeneds[h.ordinal()][Side.RIGHT.ordinal()].isOpen();
         }
 
         public boolean anyDoorOpen() {
             return anyDoorOpen(Hallway.FRONT) || anyDoorOpen(Hallway.BACK);
 
         }
 
         public boolean anyDoorOpen(Hallway h) {
             return !doorCloseds[h.ordinal()][Side.LEFT.ordinal()].isClosed()
                     || !doorCloseds[h.ordinal()][Side.RIGHT.ordinal()].isClosed();
         }
 
         public boolean allDoorsClosed(Hallway h) {
             return (doorCloseds[h.ordinal()][Side.LEFT.ordinal()].isClosed()
                     && doorCloseds[h.ordinal()][Side.RIGHT.ordinal()].isClosed());
         }
 
         public boolean allDoorMotorsStopped(Hallway h) {
             return doorMotors[h.ordinal()][Side.LEFT.ordinal()].command() == DoorCommand.STOP && doorMotors[h.ordinal()][Side.RIGHT.ordinal()].command() == DoorCommand.STOP;
         }
 
         public boolean anyDoorMotorOpening(Hallway h) {
             return doorMotors[h.ordinal()][Side.LEFT.ordinal()].command() == DoorCommand.OPEN || doorMotors[h.ordinal()][Side.RIGHT.ordinal()].command() == DoorCommand.OPEN;
         }
 
         public boolean anyDoorMotorClosing(Hallway h) {
             return doorMotors[h.ordinal()][Side.LEFT.ordinal()].command() == DoorCommand.CLOSE || doorMotors[h.ordinal()][Side.RIGHT.ordinal()].command() == DoorCommand.CLOSE;
         }
     }
 
     /**
      * Keep track of time and decide whether to or not to include the last interval
      */
     private class ConditionalStopwatch {
 
         private boolean isRunning = false;
         private SimTime startTime = null;
         private SimTime accumulatedTime = SimTime.ZERO;
 
         /**
          * Call to start the stopwatch
          */
         public void start() {
             if (!isRunning) {
                 startTime = Harness.getTime();
                 isRunning = true;
             }
         }
 
         /**
          * stop the stopwatch and add the last interval to the accumulated total
          */
         public void commit() {
             if (isRunning) {
                 SimTime offset = SimTime.subtract(Harness.getTime(), startTime);
                 accumulatedTime = SimTime.add(accumulatedTime, offset);
                 startTime = null;
                 isRunning = false;
             }
         }
 
         /**
          * stop the stopwatch and discard the last interval
          */
         public void reset() {
             if (isRunning) {
                 startTime = null;
                 isRunning = false;
             }
         }
 
         public SimTime getAccumulatedTime() {
             return accumulatedTime;
         }
 
         public boolean isRunning() {
             return isRunning;
         }
     }
 
     /**
      * Keep track of the accumulated time for an event
      */
     private class Stopwatch {
 
         private boolean isRunning = false;
         private SimTime startTime = null;
         private SimTime accumulatedTime = SimTime.ZERO;
 
         /**
          * Start the stopwatch
          */
         public void start() {
             if (!isRunning) {
                 startTime = Harness.getTime();
                 isRunning = true;
             }
         }
 
         /**
          * Stop the stopwatch and add the interval to the accumulated total
          */
         public void stop() {
             if (isRunning) {
                 SimTime offset = SimTime.subtract(Harness.getTime(), startTime);
                 accumulatedTime = SimTime.add(accumulatedTime, offset);
                 startTime = null;
                 isRunning = false;
             }
         }
 
         public SimTime getAccumulatedTime() {
             return accumulatedTime;
         }
 
         public boolean isRunning() {
             return isRunning;
         }
     }
 
     /**
      * Utility class to implement an event detector
      */
     private abstract class EventDetector {
 
         boolean previousState;
 
         public EventDetector(boolean initialValue) {
             previousState = initialValue;
         }
 
         public void updateState(boolean currentState) {
             if (currentState != previousState) {
                 previousState = currentState;
                 eventOccurred(currentState);
             }
         }
 
         /**
          * subclasses should overload this to make something happen when the event
          * occurs.
          * @param newState
          */
         public abstract void eventOccurred(boolean newState);
     }
 }
