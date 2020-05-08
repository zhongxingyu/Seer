 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 
 public class Elevator implements Comparable<Elevator> {
 
    private int floor; // The current floor of the elevator
    private int startingFloor; // The floor the elevator starts on
    private final int maxCap; // Maximum Capacity of the elevator
    private final int maxFloor; // The maximum Floor the elevator can reach
    private final int minFloor; // The minimum Floor the elevator can reach
    private int distanceTrav;
    private int curCap; // Current Capacity of the elevator
    private int state; // -1 going down, 0 not moving, 1 going up
    private PriorityQueue<Integer> goals; // The goals for the elevator
    private HashMap<Integer, LinkedList<Person>> contains; // The people the
    /* STATE */// elevator
    private boolean dumbMode;
    // contains
    private FloorComparatorAscending up = new FloorComparatorAscending();
    private FloorComparatorDescending down = new FloorComparatorDescending();
    /* Variables to be used by the manager as well as the elevator */
    public static final int DOWN = -1;
    public static final int STATIC = 0;
    public static final int UP = 1;
 
    private static final char dMode = 'd';
 
    /***
     * The constructor for an elevator, with an initial starting floor and
     * maximum capacity
     * 
     * @param maxCap
     *           Sets the maximum capacity for the elevator (total people)
     * @param start
     * 
     * @param upperElevatorRange
     * 
     * @param lowerElevatorRange
     * 
     * @param mode
     * 
     */
    public Elevator(int maxCap, int start, int upperElevatorRange, int lowerElevatorRange, String mode) {
 
       this.maxCap = maxCap;
       this.maxFloor = upperElevatorRange;
       this.minFloor = lowerElevatorRange;
       this.distanceTrav = 0;
       this.startingFloor = start;
       this.floor = this.startingFloor;
       this.state = STATIC; // The elevator hasn't moved yet
       goals = new PriorityQueue<Integer>();
       contains = new HashMap<Integer, LinkedList<Person>>(maxCap);
       extractMode(mode);
    }
 
    /***
     * Sets the appropriate modes for the elevator d - for dumbMode
     * 
     * @param mode
     *           A string that contains the modes with no spaces
     */
    private void extractMode(String mode) {
 
       // currently only one state
       for (int i = 0; i < mode.length(); ++i) {
          if (mode.charAt(i) == dMode) {
             dumbMode = true;
          }
       }
    }
 
    /*** @returns the current capacity of the elevator */
    public int getCurCap() {
       return curCap;
    }
 
    /***
     * Changes the current capacity of the elevator
     * 
     * @param people
     *           Total number of people currently in the elevator
     */
    public void setCurCap(int people) {
       this.curCap = people;
    }
 
    /***
     * Changes whether the elevator goes up or down
     */
    public boolean changeState(int newState) {
 
       if (newState != state) {
          if (goals.isEmpty()) {
             state = newState;
             this.setState(newState);
             return true;
          }
       }
       return false;
    }
 
    /***
     * deletes the old goals Queue and changes it to the appropriate one so that
     * it priorities based on the new state of the elevator
     */
    private void setState(int newState) {
       if (newState > 0) {
          goals = new PriorityQueue<Integer>(2 * maxCap, up);
       } else {
          goals = new PriorityQueue<Integer>(2 * maxCap, down);
       }
       this.state = newState;
    }
 
    /***
     * Sets a goal for which floor the elevator will stops on
     * 
     * @param toDo
     *           A floor that the elevator will go to
     */
    public void setGoal(int toDo) {
       goals.add(toDo);
    }
 
    public int peekGoal () {
       return goals.peek();
    }
    
    /*** @Returns the total distance the elevator has traveled */
    public int getDistance() {
       return distanceTrav;
    }
 
    /*** @Returns the direction the elevator will move */
    public int getState() {
 
       return state;
    }
 
    /*** @returns whether the elevator is full or not */
    public boolean isFull() {
       return maxCap == curCap;
    }
 
    /*** Changes the current floor of the elevator */
    // TODO not needed
    /*
     * public void setCurrentFloor(int floor) { this.floor = floor; }
     */
    /***
     * @Returns The floor the elevator is currently on
     */
    public int getCurrentFloor() {
       return this.floor;
    }
 
    /*** @Returns whether the elevator has moved in the last unit time */
    public boolean isActive() {
 
       if (state != 0) {
          return true;
       }
       return false;
    }
 
    /*** @Returns whether the elevator is empty */ 
    public boolean isEmpty () {
       return curCap == 0;
    }
    
    /***
     * Takes the input f (floor) and check if its a valid floor. Example: if f is
     * somewhere below the current floor of the elevator but the elevator is
     * going up it will be rejected
     */
    private boolean checkValid(int f) {
       if (f < floor && state == UP) {
          return false;
       } else if (f > floor && state == DOWN) {
          return false;
       }
       return true;
    }
 
    // TODO Write a more specific method for enter thats takes in a certain
    // person
    /***
     * Adds a person when appropriate inside the elevator keeping track of
     * necessary information and without losing the instance of the person
     */
    public boolean enter(Person p) {
 
       if (curCap == maxCap) {
          assert false; //TODO Fix this code
          return false;
       }
       int floorWanted = p.getDestinationFloor();
       if (!checkValid(floorWanted)) {
          assert false; //TODO Fix this
          return false;
       }
       goals.add(floorWanted);
       LinkedList<Person> group = contains.get(floorWanted);
       if (group != null) {
          // group exists so we add the person to the existing group
          group.add(p);
       } else {
          // group doesn't exist so we create a group and add its first person
          LinkedList<Person> newGroup = new LinkedList<Person>();
          newGroup.add(p);
          contains.put(floorWanted, newGroup);
       }
       assert curCap >= 0; //It should never be negative
       curCap++;
       return true;
    }
 
    /***
     * Updates the elevator's state should the elevator reach the max floor
     */
    private void checkRange() {
       if (floor == maxFloor && state > 0 || floor == minFloor && state < 0) {
          assert (goals.isEmpty()); // If the goals are not empty this is a bug
          setState(state * -1);
       } 
    }
 
    /***
     * Moves the elevator forward one unit of time. The elevator moves up or down
     * a floor depending on its state and if it has a goal. Returns either a
     * group of people or null, should null be returned it means that no person
     * left that floor, but people might be entering.
     */
    public LinkedList<Person> update() {
       // TODO Check what floors the elevator can move to
       if (!goals.isEmpty()) {
          move ();
          int destination = goals.peek();
          int localGoal = goals.peek();
          // We have found a goal set
          if (localGoal == floor) {
             // int key = goals.remove(); //Good idea by Roger
             while (localGoal == floor) {
                goals.remove();
                if(goals.isEmpty ()){
                    break;
                }
                localGoal = goals.peek();
             }
            LinkedList<Person> peopleLeaving = contains.get(destination); // TODO
                                                                           // change
                                                                           // name
                                                                           // of
                                                                           // destination
                                                                           // to
                                                                           // idk
             if (peopleLeaving != null) {
                this.curCap -= peopleLeaving.size();
             }
             checkRange();
             return peopleLeaving; // Returns the group of people departing on
                                   // this floor
          }
       } else if (dumbMode) {
          move ();
          checkRange();
       } else {
          state = STATIC; // we have nothing to do
       }
       return null;
    }
    
    public void move () {
       floor += state;
       distanceTrav += 1;
       //checkRange();
    }
    
    /***
     * Allows elevators to be comparable by priority (which is the current floor
     * they are on)
     */
    @Override
    public int compareTo(Elevator o) {
 
       // TODO Test this function
       if (this.floor > o.floor) {
          return 1;
       } else if (this.floor < o.floor) {
          return -1;
       }
       return 0;
    }
 
    public int[] getInfo () {
       int[] info = {floor, startingFloor, maxCap, maxFloor, distanceTrav, curCap, state};
       return info;
    }
 }
