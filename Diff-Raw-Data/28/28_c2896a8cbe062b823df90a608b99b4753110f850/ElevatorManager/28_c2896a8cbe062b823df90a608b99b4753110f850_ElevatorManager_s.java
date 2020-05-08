 import java.util.PriorityQueue;
 
 public class ElevatorManager {
 
    private Elevator[] elevators;
    private Building building;
    private boolean[] upRequestsServed;
    private boolean[] downRequestsServed;
    PriorityQueue<Elevator> upElevators;
    PriorityQueue<Elevator> downElevators; // TODO Implement this
    /*
     * Mode available: d - Dumb mode (An elevator cannot switch states until it
     * hits the top floor
     */
    private boolean dumbMode;
 
    // TODO: Be able to output the proper statistics
 
    public ElevatorManager(Elevator[] e, Building b, String mode) {
       /***
        * Given the elevator and building the elevator will manage the elevators
        * in constraint to the modes set
        */
       setMode(mode);
       this.elevators = e;
       this.building = b;
       ElevatorComparatorAscending up = new ElevatorComparatorAscending();
       ElevatorComparatorDescending down = new ElevatorComparatorDescending();
       upElevators = new PriorityQueue<Elevator>(e.length, up);
       downElevators = new PriorityQueue<Elevator>(e.length, down);
       upRequestsServed = new boolean[b.floors.length];
       downRequestsServed = new boolean[b.floors.length];
    }
 
    private void setMode(String mode) {
       /***
        * Extracts the string for valid modes
        */
       // TODO Complete setMode for other mode that we will add
       for (int i = 0; i < mode.length(); ++i) {
          if ('d' == mode.charAt(i)) {
             dumbMode = true;
          }
       }
    }
 
    public void manage() {
 
       // this should not happen
       assert elevators == null;
       assert building == null;
       // TODO Create other modes
       if (dumbMode == true) {
          dumbManage();
       }
 
    }
 
    private void generateUpQueue() {
       // TODO Write documentation
       for (int i = 0; i < elevators.length; ++i) {
          if (elevators[i].getState() == Elevator.UP
                || elevators[i].getState() == Elevator.STATIC) { // TODO THIS
                                                                 // BEHAVIOUR
                                                                 // NEEDS TO BE
                                                                 // NOTED
             if (!elevators[i].isFull()) { // Ignores full elevators
                upElevators.add(elevators[i]);
             }
          }
       }
    }
 
    private void generateDownQueue() {

    }
 
    private Elevator getElevator(int elevatorFloor, int state) {
 
       for (int i = 0; i < elevators.length; ++i) {
          if (elevators[i].getCurrentFloor() == elevatorFloor
                && elevators[i].getState() == state) {
             return elevators[i];
          }
       }
       System.err.println("Error has occurred: No elevator found");
       return null;
    }
 
    private void dumbManage() {
       /***
        * The dumb elevator follows a very strict and poorly optimized: 1. The
        * elevator must go completely down or up before it picks up people 2. The
        * elevator will not pick up people going down until it switches to down
        * state NOTES Dumb Elevator does not have a static mode
        */
 
       // updates the current floors of all the up elevators
       generateUpQueue();
       // Generate goals for elevators going up
       for (int i = 0; i < building.floors.length; ++i) {
          Elevator curElevator = upElevators.remove();
 
          if (curElevator.getCurrentFloor() <= i) {
             int people = building.getPeople(i, Building.UP);
             if (people > 0) {
                // We only do work for floors that have people on them
                if (curElevator.getCurrentFloor() == i) {
                   // Take as many people from this floor
                   // as the elevator allows
                   upRequestsServed[i] = false; // An elevator has serviced the
                                                // floor
                   while (!curElevator.isFull() && people > 0) {
                      curElevator.enter(building.remove(i, Building.UP));
                      --people;
                   }
                } else {// The elevator has yet to reach this floor
                   // generate goals for elevator
                   if (people > 0 && !upRequestsServed[i]) {
                      upRequestsServed[i] = true;
                      curElevator.setGoal(i); // TODO For the more intelligent
                                              // elevator we will need a
                                              // scheduler
                   }
                }
             }
          }
       }
       // updates the current floors of all the up elevators
       generateDownQueue();
       // generate goals for elevators going down
       for (int i = building.floors.length - 1; i >= 0; --i) {
          Elevator curElevator = downElevators.remove();
 
          if (curElevator.getCurrentFloor() >= i) {
             int people = building.getPeople(i, Building.DOWN);
             if (people > 0) {
                // We only do work for floors that have people on them
                if (curElevator.getCurrentFloor() == i) {
                   // Take as many people from this floor
                   // as the elevator allows
                   downRequestsServed[i] = false; // An elevator has serviced the
                                                  // floor
                   while (!curElevator.isFull() && people > 0) {
                      curElevator.enter(building.remove(i, Building.DOWN));
                      --people;
                   }
                } else {// The elevator has yet to reach this floor
                   // generate goals for elevator
                   if (people > 0 && !downRequestsServed[i]) {
                      downRequestsServed[i] = true;
                      curElevator.setGoal(i); // TODO For the more intelligent
                                              // elevator we will need a
                                              // scheduler
                   }
                }
             }
          }
       }
    }
    // It will need to poll the building to see which floor has the most people
 
 }
