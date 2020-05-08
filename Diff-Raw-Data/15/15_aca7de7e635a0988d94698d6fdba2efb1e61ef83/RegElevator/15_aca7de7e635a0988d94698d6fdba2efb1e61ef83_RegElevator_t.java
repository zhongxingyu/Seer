 package elevator.quarter.project;
 
 import static elevator.quarter.project.Lift.DEFAULT_FLOOR;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * 
  * @author Craig
  */
 public class RegElevator implements Elevator, Lift, Runnable
 {
     private final int ELEVATOR_CAPACITY = 20;
     
     //current floor & queue information
     private Floor currentFloor;
     private ArrayList<Floor> destinations;
     private static int elevatorIDCounter = 1;
     private int elevatorID;
     
     //people currently on the elevator
     private ArrayList<Movable> movablesOnElevator;
     
     //other elevator components
     private FloorPanel elevatorFloorPanel;
     
     //state variables
     private Door doorState;
     private ElevatorState elevatorState;
     //private boolean running;
     
     //time values (ms)
     private final int timeBetweenFloors = 1000;
     private final int doorOpenCloseTime = 250;
     private final int doorStaysOpenTime = 1000;
     
     public RegElevator()
     {
         doorState = Door.CLOSED;
         elevatorState = ElevatorState.IDLE;
         movablesOnElevator = new ArrayList<Movable>();
         destinations = new ArrayList<Floor>();
         elevatorID = elevatorIDCounter;
         elevatorIDCounter++;
     }
     
     //accessors
     /**
      * accessor for elevatorID
      * @return 
      */
     @Override
     public int getElevatorID()
     {
         return elevatorID;
     }
     
     /**
      * accessor for currentFloor
      * @return 
      */
     @Override
     public Floor getCurrentFloor()
     {
         return currentFloor;
     }
     
     /**
      * The elevator's initial floor can be set only once with this method.
      * @param floorIn 
      */
     @Override
     public void initiallySetCurrentFloor()
     {
         if(currentFloor == null)
         {
             currentFloor = RegBuilding.getInstance().getFloorWithIndex(0);
         }
     }
     
     /**
      * This method adds a new floor request to either the upward-bound or downward-bound destination list.
      * @param floorIn
      */
     @Override
     public void addFloorToDestList(Floor floorIn)
     {
         /*
         //if the elevator is on the same floor as the floor in request, open the doors and remain idle. otherwise, add it to the proper up/down destination list.
         if(currentFloor.getFloorID() == floorIn.getFloorID())
         {
             System.out.println("The requested elevator is already on the requested floor. Doors opening.");
             doorOpen();
         }
         else if(currentFloor.getFloorID() < floorIn.getFloorID())
         {
             upDestinations.add(floorIn);
             System.out.println("Floor " + floorIn.getFloorID() + " added to the upDestinations list of Elevator " + elevatorID);
         }
         else if(currentFloor.getFloorID() > floorIn.getFloorID())
         {
             downDestinations.add(floorIn);
             System.out.println("Floor " + floorIn.getFloorID() + " added to the downDestinations list of Elevator" + elevatorID);
         }
         * */
     }
     
     /**
      * Continuously running thread method.
      */
     @Override
     public void run()
     {
         System.out.println("Elevator " + elevatorID + " running.");
         
 	boolean running = true;
 	boolean waitEnded = false;
 	boolean waitException = false;
 	
 	while(running)
 	{
             //this block waits for an elevator timeout
             synchronized(this)
             {
                 if(destinations.isEmpty())
 		{
                     try
                     {
 			wait(IDLE_WAIT_TIME/SCALE_FACTOR);
 			waitEnded = true;
                     }
                     catch(InterruptedException ex)
                     {
                         waitException = true;
                         ex.printStackTrace();
                     }
                 }
             }
             
             synchronized(this)
             {
                 //this means you timed out, so return to default floor
                 if(destinations.isEmpty() && waitEnded)
                 {
                     try
                     {
                         addDestination(DEFAULT_FLOOR);
                         
                         //reset waitEnded flag
                     }
                     catch (InvalidFloorRequestException ex)
                     {
                         ex.printStackTrace();
                     }
                 }
                 //this means there are still floors left in the destination list to visit.
                 else if(!destinations.isEmpty())
                 {
                     try
                     {
                         Thread.sleep(TIME_PER_FLOOR/SCALE_FACTOR);
                     }
                     catch(InterruptedException e)
                     {
                         e.printStackTrace();
                     }
                     
                     if(elevatorState == ElevatorState.GOING_DOWN)
                     {
                         //needs a try-catch
                         currentFloor = RegBuilding.getInstance().getNextLowerFloor(currentFloor);
                         System.out.println("Passing floor " + currentFloor.getFloorID() + ".");
                     }
                     else if(elevatorState == ElevatorState.GOING_UP)
                     {
                         //needs a try-catch
                         currentFloor = RegBuilding.getInstance().getNextHigherFloor(currentFloor);
 
                         System.out.println("Elevator " + elevatorID + " arrived at floor " + currentFloor.getFloorID() + ".");
                     }
                     else
                     {
                         //System.out.println("Elevator " + elevatorID + " idle.");
                     }
                     
                     //if the elevator is on the first floor in its destination list, it is at its only destination and it has arrived.
                     if(currentFloor == destinations.get(0))
                     {
                         elevatorArrived();
                     }
                 }
             }
         }
     }
     
     /**
      * 
      * @param floorIn 
      */
     @Override
     public synchronized void addDestination(int floorIn) throws InvalidFloorRequestException
     {
 	///////////////////////////////
 	//error checking
 	//ensure that requested floor is within an acceptable min/max range
         if(floorIn > RegBuilding.getInstance().getFloorCount() - 1)
         {
             throw new InvalidFloorRequestException("Requested floor too large.");
         }
         else if(floorIn < 1)
         {
             throw new InvalidFloorRequestException("Requested floor too small.");
         }
	//check if requested floor is not in the current direction when going up
        else if(elevatorState == ElevatorState.GOING_UP && currentFloor.getFloorID() > floorIn)
        {
            throw new InvalidFloorRequestException("Requested floor not in current direction.");
        }
        //check if requested floor is not in the current direction when going down
        else if(elevatorState == ElevatorState.GOING_DOWN && currentFloor.getFloorID() < floorIn)
        {
            throw new InvalidFloorRequestException("Requested floor not in current direction.");
        }
 	//check to see if the requested floor is already in the destinations list
         else if(destinations.indexOf(floorIn) > -1)
         {
             throw new InvalidFloorRequestException("Requested floor already in destinations list.");
         }
 	//check if you're already at destination >> msg + return
         else if(currentFloor.equals(floorIn))
         {
             throw new InvalidFloorRequestException("Already on requested floor.");
         }
         else
         {
             ///////////////////////////////
             //add destination if it passes all the error checking
             destinations.add(getFloorInstanceOf(floorIn));
             
             System.out.println("Added floor " + floorIn + " to destinations list of Elevator " + elevatorID + ".");
             
             //Collections.sort(destinations);
 
             if(elevatorState == ElevatorState.GOING_DOWN)
             {
                 Collections.reverse(destinations);
             }
 
             notifyAll(); //notify must be in a synchronized context (block or method)
         }
     }
     
     /**
      * Takes care of clearing out the destination list, opening the doors, and other arrival tasks.
      */
     private void elevatorArrived()
     {
         System.out.println("Elevator " + elevatorID + " arrived at floor " + currentFloor.getFloorID() + ".");
         destinations.clear();
         doorOpen();
     }
     
     /**
      * Opens the elevator doors and prints a message.
      */
     @Override
     public void doorOpen()
     {
         if(doorState != Door.OPEN)
         {
             doorState = Door.OPEN;
             System.out.println("Elevator " + elevatorID + " doors open.");
             
             //open doors for 1 second
             try
             {
                 Thread.sleep(DOOR_OPEN_TIME/SCALE_FACTOR);
             }
             catch (InterruptedException ex)
             {
                 ex.printStackTrace();
             }
             
             //close the doors after 1 second
             doorClose();
         }
     }
 
     /**
      * Closes the elevator doors and prints a message.
      */
     @Override
     public void doorClose()
     {
         if(doorState != Door.CLOSED)
         {
             doorState = Door.CLOSED;
             System.out.println("Elevator " + elevatorID + " doors closed.");
         }
     }
     
     /**
      * Takes an integer floor number and returns its corresponding floor object in the building's arraylist of floors.
      * @param floorNumIn
      * @return 
      */
     private Floor getFloorInstanceOf(int floorNumIn)
     {
         return RegBuilding.getInstance().getFloorWithIndex(floorNumIn - 1);
     }
     
     
     
     
     
     
     
     
     
     ///////////////////////////////////////////////////////////////////////////
     //everything below this line is not part of submission 2
     
     /**
      * Checks that an entry request for the elevator is valid. First checks that the elevator is not at capacity, then checks that the Movable that made the request is currently on the same floor as the elevator.
      * @param movableIn
      * @throws OverCapacityException 
      */
     @Override
     public void entryRequest(Movable movableIn) throws OverCapacityException
     {
         /*
         if(movablesOnElevator.size() < ELEVATOR_CAPACITY)
         {
             if(((RegPerson)movableIn).getCurrentFloor().getFloorID() == this.currentFloor)
             {
                 
             }
             else
             {
                 //throw new MovableNotAvailableException("The requested movable is not on the same floor as the elevator.");
             }
         }
         else
         {
             throw new OverCapacityException("This elevator cannot accept any more movables.");
         }
         * 
         *         if(doorState == Door.OPEN)
         {
             
         }
         else
         {
             throw new ElevatorNotReadyException("This elevator's doors are closed and cannot accept movables.");
         }
         * */
         
     }
 
     @Override
     public void removalRequest(Movable movableIn)
     {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
     /**
      * After confirming that the entry request is valid, this method will delete the movable that requested entry from its current floor and add it to the elevator. This method can potentially be delegated using the strategy pattern.
      * @param movableIn 
      */
     private void addMovable(Movable movableIn)
     {
         //error involves converting between int/ floor types. ALSO: why do i have to cast everything???
         
         //movableIn.getCurrentFloor().getMovablesOnFloor().delete(movableIn);
         movablesOnElevator.add(movableIn);
     }
     
     /**
      * After confirming that the exit request is valid, this method will delete the movable that requested to exit from the elevator and add it to the elevator's current floor. This method can potentially be delegated using the strategy pattern.
      * @param movableIn 
      */
     private void removeMovable(Movable movableIn)
     {
         movablesOnElevator.remove(movableIn);
         //currentFloor.getMovablesOnFloor().add(movableIn);
     }
 }
