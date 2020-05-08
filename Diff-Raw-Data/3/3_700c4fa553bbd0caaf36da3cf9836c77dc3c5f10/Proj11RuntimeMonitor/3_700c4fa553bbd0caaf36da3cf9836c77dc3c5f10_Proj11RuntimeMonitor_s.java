 /** 18649 Fall 2013
  *  Project 7 Runtime Monitor
  *  Jeffrey Lau jalau
  *  David Chow davidcho
  *  Yang Liu yangliu2
  *  Brody Anderson bcanders
  *  Project11RuntimeMonitor.java
  *  @author Jeff Lau (jalau)
  */
 package simulator.elevatorcontrol;
 
 import jSimPack.SimTime;
 import simulator.elevatorcontrol.Utility.AtFloorArray;
 import simulator.elevatormodules.AtFloorCanPayloadTranslator;
 import simulator.framework.Direction;
 import simulator.framework.DoorCommand;
 import simulator.framework.Elevator;
 import simulator.framework.Hallway;
 import simulator.framework.Harness;
 import simulator.framework.ReplicationComputer;
 import simulator.framework.RuntimeMonitor;
 import simulator.framework.Side;
 import simulator.payloads.CarPositionPayload.ReadableCarPositionPayload;
 import simulator.payloads.CarWeightPayload.ReadableCarWeightPayload;
 import simulator.payloads.DoorClosedPayload.ReadableDoorClosedPayload;
 import simulator.payloads.DoorMotorPayload.ReadableDoorMotorPayload;
 import simulator.payloads.DoorOpenPayload.ReadableDoorOpenPayload;
 import simulator.payloads.DriveSpeedPayload.ReadableDriveSpeedPayload;
 
 public class Proj11RuntimeMonitor extends RuntimeMonitor{
 	
     DoorStateMachine doorState = new DoorStateMachine();
     DriveStateMachine driveState = new DriveStateMachine();
     WeightStateMachine weightState = new WeightStateMachine();
     Stopwatch reversalTimer = new Stopwatch();
     boolean hasMoved = false;
     boolean wasOverweight = false;
     boolean weightChange = false;
     boolean hasReversed = false;
     int overWeightCount = 0;
     int wastedOpening = 0;
     int current_floor = 0;
     int oldWeight = 0;
     double timeReversal = 0.0;
     private AtFloorArray floorArray;
     //keep track of the next direction
     Direction nextDirection;
 
     public Proj11RuntimeMonitor() {
         floorArray = new AtFloorArray(canInterface);
     }
  
     @Override
     protected String[] summarize() {
         String[] arr = new String[4];
         timeReversal = reversalTimer.getAccumulatedTime().getFracSeconds();
         arr[0] = "Overweight Count = " + overWeightCount;
         arr[1] = "Wasted Opening Count = " + wastedOpening; 
         arr[2] = "Time Spent Reversing = " + timeReversal + "s";
         arr[3] = getWarningStats();
         return arr;
     }
 
     public void timerExpired(Object callbackData) {
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
     	//reset weightChange counter
     	weightChange = false;
     	//Check next desired direction
     	nextDirection = mDesiredFloor.getDirection();
     	current_floor = floorArray.getCurrentFloor();
     	if(current_floor != mDesiredFloor.getFloor()){
     		warning("R-T7 Violated: Opening doors at floor" + 
     				current_floor + " with no pending calls.");
     	}
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
         //System.out.println(hallway.toString() + " Door Reopening");
    	reversalTimer.start();
     	hasReversed = true;
     }
 
     /**
      * Called once when the doors close completely
      * @param hallway which door the event pertains to
      */
     private void doorClosed(Hallway hallway) {
         //System.out.println(hallway.toString() + " Door Closed");
         //once all doors are closed, check to see if the car was overweight
         if (!doorState.anyDoorOpen()) {
             if (wasOverweight) {
                 message("Overweight");
                 overWeightCount++;
                 wasOverweight = false;
             }
             if(reversalTimer.isRunning)
             	reversalTimer.stop();
           //If no weight has changed, then no one called that floor.
             if(!weightChange) {
             	message("Wasted Opening");
             	wastedOpening++;    
             	weightChange = false;
             }
         }
         //Reset reversal flag
         hasReversed = false;
         
     }
 
     /**
      * Called once when the doors are fully open
      * @param hallway which door the event pertains to
      */
     private void doorOpened(Hallway hallway) {
     	//System.out.println(hallway.toString() + " Door Opened");
     	//if the lantern flickers, violation of 8.2
     	if(nextDirection != Direction.STOP){
     		if(!carLanterns[nextDirection.ordinal()].lighted())
     			warning("R-T8.2 Violated: Car Lantern is flickering!");
     		//Check if car lantern is in compliance with requirements.
     		if(!carLanterns[nextDirection.ordinal()].lighted())
     			warning("R-T8.1 Violated: Lantern not on with other pending"
     					+ "calls on other floors");
     	}
     }
     
     /**
      * Called if the doors are nudging
      * @param hallway
      */
     private void doorNudging(Hallway hallway){
     	if (hasReversed == false){
     		warning("R-T10 Violated: Car doors began to nudge "
     				+ "without a previous door reversal.");
     	}
     }
     
     /**
      * Called when the drive is in the Fast State
      */
     private void driveFast(){
     	
     }
     
     /**
      * Called when the drive is in the Slow State
      */
     private void driveSlow(){
     	//Check if there are any calls in the specified direction.
     	//If so, then violation. Otherwise, no violation.
     	if(nextDirection != Direction.STOP){
         	int currentFloor = floorArray.getCurrentFloor() - 1;
         	if(nextDirection == Direction.UP){
         		for(int f_u = currentFloor; f_u < Elevator.numFloors; f_u++){
         			for (Hallway h : Hallway.replicationValues) {
 						for(Direction d : Direction.replicationValues){
 						if(carLights[f_u][h.ordinal()].lighted() || hallLights[f_u][h.ordinal()][d.ordinal()].lighted()){
 							if(nextDirection != driveActualSpeed.direction())
 								warning("R-T8.3 Violated: Elevator is sevicing direction " +
 				    				driveActualSpeed.direction() + " instead of " + f_u + " " + h +
 				    				" in direction " + nextDirection);
 							}
 						}
         			}
         		}
         	}
         	else if(nextDirection == Direction.DOWN){
         		for(int f_d = currentFloor;f_d >= 1; f_d--){
         			for (Hallway h : Hallway.replicationValues) {
 						for(Direction d : Direction.replicationValues){
 						if(carLights[f_d][h.ordinal()].lighted() || hallLights[f_d][h.ordinal()][d.ordinal()].lighted()){
 							if(nextDirection != driveActualSpeed.direction())
 								warning("R-T8.3 Violated: Elevator is sevicing direction " +
 				    				driveActualSpeed.direction() + " instead of " + f_d + " " + h + 
 				    				" in direction " + nextDirection);
 							}
 						}
         			}
         		}
         	}		
     	}
     }
 
     /**
      * Called when the drive is stopped
      */
     private void driveStopped(){
     	
     }
     /**
      * Called when the drive is transitioning to the Stop state
      */
     private void driveStopping(){
     	int currentFloor = floorArray.getCurrentFloor();
     	Hallway h;
     	if (Elevator.hasLanding(currentFloor, Hallway.FRONT))	{
 			if (Elevator.hasLanding(currentFloor, Hallway.BACK))
 				h = Hallway.BOTH;
 			else
 				h = Hallway.FRONT;
 		}
 		else 
 			h = Hallway.BACK;
     	currentFloor--;
     	if(h == Hallway.BOTH){
     		if(!carLights[currentFloor][Hallway.FRONT.ordinal()].lighted() &&
     		!carLights[currentFloor][Hallway.BACK.ordinal()].lighted() &&
     		!hallLights[currentFloor][Hallway.FRONT.ordinal()][Direction.UP.ordinal()].lighted() &&
     		!hallLights[currentFloor][Hallway.FRONT.ordinal()][Direction.DOWN.ordinal()].lighted() &&
     		!hallLights[currentFloor][Hallway.BACK.ordinal()][Direction.UP.ordinal()].lighted() &&
     		!hallLights[currentFloor][Hallway.BACK.ordinal()][Direction.DOWN.ordinal()].lighted()){
     			warning("R-T6 Violated: Stopped at floor" + 
         				currentFloor + " with no pending calls.");
     		}
     	}else if(!carLights[currentFloor][h.ordinal()].lighted() &&
     	    	!hallLights[currentFloor][h.ordinal()][Direction.UP.ordinal()].lighted() &&
     	    	!hallLights[currentFloor][h.ordinal()][Direction.DOWN.ordinal()].lighted()){
     	    		warning("R-T6 Violated: Stopped at floor" + 
     	        			currentFloor + " with no pending calls.");
     		}
     	}
     
     /**
      * Called when the car weight changes
      * @param hallway which door the event pertains to
      */
     private void weightChanged(int newWeight) {
         if (newWeight > Elevator.MaxCarCapacity) {
             wasOverweight = true;
         }
         weightChange = true;
     }
     
     /**
      * Determines the point when the commit point is reached.
      * @param position
      */
     private void updateCommitPoint(int position){
     	int floor = mDesiredFloor.getFloor();
     	Direction d = driveActualSpeed.direction();
     	double speed = driveActualSpeed.speed();
     	//If the commit point is reached
     	if(!commitPoint(floor,d,position,speed)){
     		if(speed == 0.25)
     			warning("R-T9 Violated: Car was traveling at"
     					+ speed + "instead of max speed.");
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
         driveState.receive(msg);
     }
     
     @Override
     public void receive(ReadableCarPositionPayload msg){
     	updateCommitPoint((int)msg.position()*1000);
     }
     
     private static enum DriveState{
     	
     	STOPPED,
     	SLOW,
     	FAST
     }
     
     private static enum DoorState {
 
         CLOSED,
         OPENING,
         OPEN,
         CLOSING,
         NUDGING
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
     
     /**
      * Utility class for keeping track of the drive state.
      * 
      * Also provides external methods that can be queried to determine the
      * current drive state.
      */
     private class DriveStateMachine {
 
         DriveState state;
 
         public DriveStateMachine() {
             state = DriveState.STOPPED;
         }
 
 		public void receive(ReadableDriveSpeedPayload msg) {
 			updateState(msg.speed());
 		}
 	   
         private void updateState(Double speed) {
             DriveState previousState = state;
 
             DriveState newState = previousState;
 
             if (speed <= 0.05) {
                 newState = DriveState.STOPPED;
             } else if (speed >= 0.25) {
                 newState = DriveState.FAST;
             } else if (speed < 0.25) {
                 newState = DriveState.SLOW;
             } 
 
             if (newState != previousState) {
                 switch (newState) {
                     case STOPPED:
                     	if(previousState == DriveState.SLOW)
                     		driveStopping();
                     	else
                     		driveStopped();
                         break;
                     case FAST:
                         driveFast();
                         break;
                     case SLOW:
                         driveSlow();
                         break;
                 }
             }
             state = newState;
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
             } else if (anyDoorMotorNudging(h)) {
             	newState = DoorState.NUDGING;
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
                     case NUDGING:
                     	doorNudging(h);
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
         
         public boolean anyDoorMotorNudging(Hallway h) {
             return doorMotors[h.ordinal()][Side.LEFT.ordinal()].command() == DoorCommand.NUDGE || doorMotors[h.ordinal()][Side.RIGHT.ordinal()].command() == DoorCommand.NUDGE;
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
 	 * Performs a check to see whether a requested floor is viable to stop at or not.
 	 * 
 	 * @param f The requested floor
 	 * @param d The current direction the car is traveling in
 	 * @param car_position  The current car position
 	 * @param speed 
 	 * @return true if commit point not reached.
 	 * 		   false if commit point past.
 	 */
 	private boolean commitPoint(int f, Direction d, int car_position, double speed) {
 		if(d == Direction.UP){
 			return (((double)f - 1.0) * 5.0 - ((speed * speed) / 2.0 + 2.0))*1000 > car_position;
 		}
 		else if(d == Direction.DOWN){
 			return (((double)f - 1.0) * 5.0 + ((speed * speed) / 2.0 + 2.0))*1000 < car_position;
 		}
 		//If stopped, then we've definitely not reached the commit point.
 		else
 			return true;
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
