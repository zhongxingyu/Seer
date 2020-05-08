 import java.util.LinkedList;
 import java.util.PriorityQueue;
 
 public class ElevatorManager {
 
    private Elevator[] elevators;
    private Building building;
    private boolean[] upRequestsServed;
    private boolean[] downRequestsServed;
    PriorityQueue<Elevator> upElevators;
    PriorityQueue<Elevator> downElevators;
    // INTELI ELEVATOR
    private int[][] oldBuildingState;
    private int priorityFieldDistance = 2;
    /*
     * Mode available: d - Dumb mode (An elevator cannot switch states until it
     * hits the top floor
     */
    private boolean dumbMode; // set with a d
    private boolean smartMode; // set with a s
 
    // Different Modes for the smart elevator
    // NOTE knownDestinations overwrites known people per floor
    private boolean knownPeoplePerFloor; // set with a t
    private boolean knownDestinations; // set with a g
 
    private static final int DOWN = 1;
    private static final int UP = 0;
    private int[][] curBuildingState;
 
    // TODO: Be able to output the proper statistics
    /***
     * Given the elevator and building the elevator will manage the elevators in
     * constraint to the modes set
     * 
     * @param e
     *           An array of all the elevators
     * @param b
     *           The building
     * @param mode
     *           A string of modes that are set
     */
    public ElevatorManager(Elevator[] e, Building b, String mode) {
       setMode(mode);
       this.elevators = e;
       this.building = b;
       ElevatorComparatorAscending up = new ElevatorComparatorAscending();
       ElevatorComparatorDescending down = new ElevatorComparatorDescending();
       upElevators = new PriorityQueue<Elevator>(e.length, up);
       downElevators = new PriorityQueue<Elevator>(e.length, down);
       upRequestsServed = new boolean[b.floors.length];
       downRequestsServed = new boolean[b.floors.length];
       oldBuildingState = new int[b.numbFloors][2]; // Its 2 instead of 3
                                                    // because we do not care
                                                    // about people that do not
                                                    // want to use the elevator
       // init(); // TODO recode this
    }
 
    private void init() {
       // TODO Documentation
       // Sets all elevators on the bottom floor to an up state
       for (int i = 0; i < elevators.length; ++i) {
          if (elevators[i].getCurrentFloor() == 0) {
             elevators[i].changeState(Elevator.UP);
          }
       }
    }
 
    // TODO JOE TEST THIS
    private void setMode(String mode) {
       /***
        * Extracts the string for valid modes
        */
       // TODO Complete setMode for other mode that we will add
       mode = mode.toLowerCase();
       for (int i = 0; i < mode.length(); ++i) {
          if ('d' == mode.charAt(i)) {
             dumbMode = true;
          } else if ('s' == mode.charAt(i)) {
             smartMode = true;
          }
          if (smartMode) {
             // TODO Implement expandable options
             if ('t' == mode.charAt(i)) {
                knownPeoplePerFloor = true;
             }
             if ('g' == mode.charAt(i)) {
                knownDestinations = true;
             }
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
       } else if (smartMode == true) {
          smartManage();
       }
    }
 
    private void generateUpQueue() {
       // TODO Write documentation
       for (int i = 0; i < elevators.length; ++i) {
          if (elevators[i].getState() == Elevator.UP
                || elevators[i].getState() == Elevator.STATIC) { // TODO
                                                                 // THIS
                                                                 // BEHAVIOUR
                                                                 // NEEDS
                                                                 // TO BE
                                                                 // NOTED
             if (!elevators[i].isFull()) { // Ignores full elevators
                upElevators.add(elevators[i]);
             }
          }
       }
    }
 
    private void generateDownQueue() {
       for (int i = 0; i < elevators.length; ++i) {
          if (elevators[i].getState() == Elevator.DOWN
                || elevators[i].getState() == Elevator.STATIC) { // TODO
                                                                 // THIS
                                                                 // BEHAVIOUR
                                                                 // NEEDS
                                                                 // TO BE
                                                                 // NOTED
             if (!elevators[i].isFull()) { // Ignores full elevators
                downElevators.add(elevators[i]);
             }
          }
       }
    }
 
    // TODO JOE TEST THIS
    /***
     * Finds the floor of every elevator going in the desired location
     * 
     * @param state
     *           Please use either Elevator.UP or Elevator.DOWN for polling of
     *           the desired elevators
     * 
     * @returns The location of all the elevators going in the appropriate
     *          location
     */
    private int[] getElevatorFloors(int state) {
       int totalElevators = 0;
       for (int i = 0; i < elevators.length; ++i) {
          if (elevators[i].getState() == state
                || elevators[i].getState() == Elevator.STATIC) {
             ++totalElevators;
          }
       }
       int elevatorFloors[] = new int[totalElevators];
       int index = 0;
       for (int i = 0; i < elevators.length; ++i) {
          if (elevators[i].getState() == state
                || elevators[i].getState() == Elevator.STATIC) {
             elevatorFloors[index] = elevators[i].getCurrentFloor();
             index++;
          }
       }
 
       if (elevatorFloors.length > 0) {
          return elevatorFloors;
       } else {
          System.err.println("No elevators needed.");
          return null;
       }
    }
 
    // TODO JOE TEST THIS
    /***
     * @returns the number of floors that have had new people call the elevators
     */
    private int getNumberOfActiveFloors(int buildingState) {
       int total = 0;
       for (int i = 0; i < curBuildingState.length; ++i) {
          if (curBuildingState[i][buildingState] > 0) {
             ++total;
          }
       }
       return total;
    }
 
    /***
     * Creates a copy of the building after manage has completed most of its
     * function
     */
    private void updateOldBuildingState(int[][] moreGoals) {
       for (int i = 0; i < building.floors.length; ++i) {
          oldBuildingState[i][Building.UP] = building.getPeople(i, Building.UP)
                - moreGoals[i][Building.UP];
          oldBuildingState[i][Building.DOWN] = building.getPeople(i,
                Building.DOWN) - moreGoals[i][Building.DOWN];
          ;
       }
    }
 
    /***
     * Runs all elevators a single unit time
     */
    private void runAllElevators() {
       for (int i = 0; i < elevators.length; ++i) {
          LinkedList<Person> people = elevators[i].update();
          if (people != null) {
             building.insertInFloor(elevators[i].getCurrentFloor(), people);
          }//if (smartMode && people == null ) {
           //   if (elevators[i].getDesiredState () > 0) {
           //       if ()
           //   }
          //}
       }
    }
 
    /***
     * The dumb elevator follows a very strict and poorly optimized: 1. The
     * elevator must go completely down or up before it picks up people 2. The
     * elevator will not pick up people going down until it switches to down
     * state NOTES Dumb Elevator does not have a static mode
     */
    private void dumbManage() {
       // updates the current floors of all the up elevators
       generateUpQueue();
       // Generate goals for elevators going up
       Elevator curElevator = null;
       while (!upElevators.isEmpty()) {
          curElevator = upElevators.remove();
          for (int i = 0; i < building.floors.length; ++i) {
             if (curElevator.getCurrentFloor() <= i) {
                int people = building.getPeople(i, Building.UP);
                if (people > 0) {
                   // We only do work for floors that have people on them
                   if (curElevator.getCurrentFloor() == i) {
                      // Take as many people from this floor
                      // as the elevator allows
                      upRequestsServed[i] = false; // An elevator has
                                                   // serviced
                                                   // the
                                                   // floor
                      while (!curElevator.isFull() && people > 0) {
                         curElevator.enter(building.remove(i, Building.UP));
                         --people;
                      }
                   } else {// The elevator has yet to reach this floor
                      // generate goals for elevator
                      if (people > 0 && !upRequestsServed[i]) {
                         upRequestsServed[i] = true;
                         curElevator.setGoal(i);
                      }
                   }
                }
             }
          }
       }
 
       // updates the current floors of all the up elevators
       generateDownQueue();
       // generate goals for elevators going down
       while (!downElevators.isEmpty()) {
          curElevator = downElevators.remove();
          for (int i = building.floors.length - 1; i >= 0; --i) {
             if (curElevator.getCurrentFloor() >= i) {
                int people = building.getPeople(i, Building.DOWN);
                if (people > 0) {
                   // We only do work for floors that have people on them
                   if (curElevator.getCurrentFloor() == i) {
                      // Take as many people from this floor
                      // as the elevator allows
                      downRequestsServed[i] = false; // An elevator has
                                                     // serviced
                                                     // the
                                                     // floor
                      while (!curElevator.isFull() && people > 0) {
                         curElevator.enter(building.remove(i, Building.DOWN));
                         --people;
                      }
                   } else {// The elevator has yet to reach this floor
                      // generate goals for elevator
                      if (people > 0 && !downRequestsServed[i]) {
                         downRequestsServed[i] = true;
                         curElevator.setGoal(i);
                      }
                   }
                }
             }
          }
       }
       runAllElevators();
    }
 
    private void smartManage() {
       smartElevator();
       int moreGoals[][] = new int[building.floors.length][2]; // Intermediate
                                                               // Scheduler needs
                                                               // more
                                                               // information
       for (int i = 0; i < elevators.length; ++i) {
          int eFloor = elevators[i].getCurrentFloor();
          Integer goal = elevators[i].peekGoal();
          if (goal != null) {
             if (eFloor == goal) {
                // Someone may be entering
 
                if (elevators[i].getState() > 0) {
                   int peopleWaiting = building.getPeople(eFloor, Building.UP);
                   if (peopleWaiting > 0) {
                      while (!elevators[i].isFull() && peopleWaiting > 0) {
                         elevators[i]
                               .enter(building.remove(eFloor, Building.UP));
                         --peopleWaiting;
                      }
                      if (peopleWaiting > 0 && !knownDestinations
                            && !knownPeoplePerFloor) {
                         moreGoals[i][Building.UP] = building.getPeople(i,
                               Building.UP);
                      }
                   }
                   elevators[i].goals.remove ();
                } else if (elevators[i].getState() < 0) {
                   int peopleWaiting = building.getPeople(eFloor, Building.DOWN);
                   if (peopleWaiting > 0) {
                      while (!elevators[i].isFull() && peopleWaiting > 0) {
                         elevators[i].enter(building.remove(eFloor,
                               Building.DOWN));
                         --peopleWaiting;
                      }
                      if (peopleWaiting > 0 && !knownDestinations
                            && !knownPeoplePerFloor) {
                         moreGoals[i][Building.DOWN] = building.getPeople(i,
                               Building.DOWN);
                      }
                   }
                }
                elevators[i].goals.remove ();
             }
          }
       }
       moveAllElevators();
       // This must be done last so that we can get an accurate building state
       updateOldBuildingState(moreGoals);
    }
 
    private void moveAllElevators () {
        for (int i = 0; i < elevators.length; ++i) {
           elevators[i].move();
            //check if anyonre wants to get off
            LinkedList<Person> people = elevators[i].contains.get(elevators[i].getCurrentFloor ());
            if (people != null) {
               int floorGoal = elevators[i].goals.remove ();
               int tempGoal = floorGoal;
               while (tempGoal == floorGoal) {
                   elevators[i].goals.remove ();
               }
               building.insertInFloor(elevators[i].getCurrentFloor(), people);
               elevators[i].contains.remove(elevators[i].getCurrentFloor ());
            }
        }
     
 }
 
 // TODO Exponential recharge time for elevator chaining
    // It will need to poll the building to see which floor has the most people
    private void smartElevator() {
       if (checkBuildingState()) {
          // Manager only does work for tasks that have not been computed and
          // assigned yet
          // calculate floor priority
          int[] buildingOrderUp = intelliScheduler(Building.UP);
          if (buildingOrderUp != null) {
             for (int i = 0; i < buildingOrderUp.length; ++i) {
                LinkedList<Elevator> availableElevators = generatePriorityFields(
                      Elevator.UP, buildingOrderUp[i]);
                if (knownDestinations || knownPeoplePerFloor) {
                   // Check if the elevator we are scheduling to go here can pick
                   // up all of the people
                } else {
                   // TODO We will need ensure that we don't ignore people
                   Elevator e = availableElevators.remove();
                   e.setGoal(buildingOrderUp[i]);
                   e.setDesiredState (Elevator.UP);
                }
             }
          }
          int[] buildingOrderDown = intelliScheduler(Building.DOWN);
          if (buildingOrderDown != null) {
             for (int i = 0; i < buildingOrderDown.length; ++i) {
                LinkedList<Elevator> availableElevators = generatePriorityFields(
                      Elevator.DOWN, buildingOrderDown[i]);
                if (knownDestinations || knownPeoplePerFloor) {
                   // Check if the elevator we are scheduling to go here can pick
                   // up all of the people
                } else {
                   // TODO We will need ensure that we don't ignore people
                   Elevator e = availableElevators.remove();
                   e.setGoal(buildingOrderDown[i]);
                   e.setDesiredState (Elevator.DOWN);
                }
             }
          }
       }
    }
 
    private boolean checkBuildingState() {
       boolean changeOccured = false; // Assume no change
       curBuildingState = new int[oldBuildingState.length][2];
       for (int i = 0; i < building.numbFloors; ++i) {
          // Check the current building state by comparing new state to old
          // state
          int upState = building.getPeople(i, Building.UP)
                - oldBuildingState[i][ElevatorManager.UP];
          int downState = building.getPeople(i, Building.DOWN)
                - oldBuildingState[i][ElevatorManager.DOWN];
          if (upState > 0 || downState > 0) {
             changeOccured = true; // change occurred
          }
          curBuildingState[i][ElevatorManager.UP] = upState;
          curBuildingState[i][ElevatorManager.DOWN] = downState;
       }
       return changeOccured;
    }
 
    /***
     * 
     * @param buildingState
     *           State of the building which allows us to find how large our
     *           array will be
     * @param priorityFloors
     *           An array representing floors in the building filled with
     *           priorities of said floors
     * @return An array representing priority of floors filled with the floors
     */
    private int[] priorityFloors(int buildingState, int[] priorityFloors) {
       int floorSchedule[] = new int[getNumberOfActiveFloors(buildingState)];
       for (int i = 0; i < floorSchedule.length; ++i) { // Populate the
                                                        // priorities
          int min = Integer.MAX_VALUE;
          int index = -1;
          for (int j = 0; j < priorityFloors.length; ++j) { // Find the smallest priority
             if (priorityFloors[j] != 0) {  // We ignore 0s because there was no net change in people
                if (min > priorityFloors[j]) {
                   min = priorityFloors[j];
                   index = j;
                }
             }
          }
          priorityFloors[index] = 0; // We do now want to find the same priority
                                     // again so we remove it
          floorSchedule[i] = index; // The current floor with largest priority
       }
       return floorSchedule;
    }
 
    private int[] intelliScheduler(int buildingState) {
       int[] floorSchedule = null;
       // What information can we know? and how can we use it
       if (knownDestinations) {
 
       } else if (knownPeoplePerFloor) {
 
       } else {
          floorSchedule = primitiveScheduler(buildingState);
       }
       // assert floorSchedule != null;
       return floorSchedule;
    }
 
    /***
     * 
     * Primitive Schedule finds the highest probability floor based on this
     * formula: (Sum of all elevator distances to this floor) / number Of
     * Available elevators
     * 
     * @param buildingState
     *           Whether elevators are going up or down
     * @return An array containing the order in which we assign floors
     */
    private int[] primitiveScheduler(int buildingState) {
       int wantedElevators; // Gets the appropriate elevator state
       if (buildingState == Building.UP) {
          wantedElevators = Elevator.UP;
       } else {
          wantedElevators = Elevator.DOWN;
       }
       // Gets the position of all the appropriate elevators
       int[] elevatorFloors = getElevatorFloors(wantedElevators);
       if (elevatorFloors == null) {
          return null; // No elevators were found
       }
       // Create a new array of floors
       int[] priorityFloors = new int[curBuildingState.length];
       for (int i = 0; i < curBuildingState.length; ++i) {
          int sum = 0;
          if (curBuildingState[i][buildingState] > 0) {
             // calculate the average distance of all elevators from this floor
             for (int j = 0; j < elevatorFloors.length; ++j) {
                sum += Math.abs(i - elevatorFloors[j]);
             }
             sum = sum / elevators.length;
          }
          priorityFloors[i] = sum;
       }
       return priorityFloors(buildingState, priorityFloors);
    }
 
    /*
     * private int[] intermediateScheduler (int buildingState) { int
     * wantedElevators; // Gets the appropriate elevator state if (buildingState
     * == Building.UP) { wantedElevators = Elevator.UP; } else { wantedElevators
     * = Elevator.DOWN; } // Gets the position of all the appropriate elevators
     * int[] priorityFloors = new int[curBuildingState.length]; for (int i = 0; i
     * < curBuildingState.length; ++i) { int sum = 0; if
     * (curBuildingState[i][buildingState] > 0) { // calculate the average
     * distance of all elevators from this floor for (int j = 0; j <
     * elevatorFloors.length; ++j) { sum += Math.abs(i - elevatorFloors[j]); }
     * sum = sum / elevatorFloors.length; } priorityFloors[i] = sum; } return
     * priorityFloors(buildingState, priorityFloors); }
     */
    private LinkedList<Elevator> generatePriorityFields(int direction, int floor) {
 
       // create LinkList that will hold all the elevators in the building
       // and it will be gradually decreased until it contains all the
       // desirable elevators
       LinkedList<Elevator> elevatorList = new LinkedList<Elevator>();
 
       // add all the elevators initially
       for (int i = 0; i < elevators.length; i++) {
          elevatorList.add(elevators[i]);
       }
 
       // remove all elevators that we don't need
       // which at this point, are all the ones that are full
       for (Elevator elevator : elevatorList) {
          if (elevator.isFull()) {
             elevatorList.remove(elevator);
          }
       }
 
       // remove elevators moving in the opposite direction that are NOT empty
       for (int i = 0; i < elevatorList.size(); i++) {
          if (!elevatorList.get(i).isEmpty()
                && elevatorList.get(i).getState() != direction) {
             elevatorList.remove(elevatorList.get(i));
          }
       }
       
       // remove elevators whose desired state does not match the direction
       for (int i = 0; i < elevatorList.size(); i++) {
           int state = elevatorList.get(i).getDesiredState ();
           if (state == 0) {
               // ignore 
           } else if (state != direction) {
               elevatorList.remove(elevatorList.get(i));
           }
       }
       
       // if going one way, desired state is the other way, throw it out
       for (int i = 0; i < elevatorList.size (); i++) {
           int desiredState = elevatorList.get(i).getDesiredState ();
           int actualState = elevatorList.get(i).getState ();
           if (desiredState != actualState) {
               elevatorList.remove(elevatorList.get(i));
           }
       }
 
       for (int i = 0; i < elevatorList.size(); i++) {
          int elevatorFloor = elevatorList.get(i).getCurrentFloor();
          int k = elevatorFloor - floor;
 
          if (Math.abs(k) <= priorityFieldDistance) {
             // we keep the elevator
          } else {
             if ((k < 0) && elevatorList.get(i).getState() < 0) {
                elevatorList.remove(elevatorList.get(i));
             }
             if ((k > 0) && elevatorList.get(i).getState() > 0) {
                elevatorList.remove(elevatorList.get(i));
             }
          }
       }
 
       elevatorList = atomicSort(elevatorList, floor);
 
       return elevatorList;
    }
 
    
    private static LinkedList<Elevator> atomicSort(
          LinkedList<Elevator> localElevatorList, int proximityFloor) {
       // sort based on proximity
       // if proximities are equal, sort by the elevator with the most people
 
       // Loop once for each element in the array.
       for (int counter = 0; counter < localElevatorList.size() - 1; counter++) {
 
          // Once for each element, minus the counter.
          for (int index = 0; index < localElevatorList.size() - 1 - counter; index++) {
 
             // Test if need a swap or not.
             if (atomicCompare(localElevatorList.get(index),
                   localElevatorList.get(index + 1), proximityFloor) == -1) {
                // These three lines just swap the two elements:
                Elevator temp = localElevatorList.get(index);
                localElevatorList.set(index, localElevatorList.get(index + 1));
                localElevatorList.set(index + 1, temp);
             }
          }
       }
 
       return localElevatorList;
    }
 
    private static int atomicCompare(Elevator elevator1, Elevator elevator2,
          int floor) {
 
       // if the elevator 1 is further away from the target floor
       // if elevator 2 is closer
       int elevator1Distance = Math.abs(elevator1.getCurrentFloor() - floor);
       int elevator2Distance = Math.abs(elevator2.getCurrentFloor() - floor);
       if (elevator1Distance > elevator2Distance) {
          return -1; // elevator 2 is closer
       } else if (elevator1Distance < elevator2Distance) {
          return 1; // elevator 1 is closer
       } else {
          // check to see if any of the elevators are empty
          if (elevator1.isEmpty()) {
             return 1;
          }
          if (elevator2.isEmpty()) {
             return -1;
          }
          // check to see which has the most people
          if (elevator1.getCurCap() < elevator2.getCurCap()) {
             return 1; // elevator 1 is more preferred
          } else if (elevator2.getCurCap() > elevator2.getCurCap()) {
             return -1; // elevator 2 is more preferred
          } else {
             return 0; // equal
          }
       }
    }
 
 }
