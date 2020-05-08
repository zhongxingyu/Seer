 package cscie160.hw1;
 	
 /**
  * Simulator a single elevator.
  */
 public class Elevator {
 
 	public final int MAX_CAPACITY = 10;
 	public final int NUM_FLOORS = 7;
 	
 	private int current_floor;
 	private boolean going_up;
 	
 	private boolean[] targets;
 	private int[] passenger_targets;
 	
 	/**
 	 * Class constructor.
 	 */
 	public Elevator() {
 		current_floor = 0;
 		going_up = true;
 	
 		targets = new boolean[NUM_FLOORS];
 		passenger_targets = new int[NUM_FLOORS];
 	}
 	
 	/**
 	 * Moves the elevator up or down. 
 	 * Stops if the new floor is one of its target floors.
 	 */
 	public void move() {
 		if (going_up) {
 			current_floor++;
 		}
 		else {
 			current_floor--;
 		}
 		
 		if (current_floor == 0) {
 			going_up = true;
 		}
		else if (current_floor == NUM_FLOORS-1) {
 			going_up = false;
 		}
 		
 		if (targets[current_floor]) {
 			stop();
 		}
 	}
 	
 	/**
 	 * Disembarks all passengers heading for this floor.
 	 * Clears the target for this floor.
 	 * Prints out status information.
 	 */
 	public void stop() {
 		passenger_targets[current_floor] = 0;
 		targets[current_floor] = false;
 		
		System.out.println("\r\nStopping on floor "+Integer.toString(current_floor+1));
 		System.out.println(this);
 	}
 	
 	/**
 	 * Adds a passenger to the elevator. 
 	 * Sets the passenger's destination as a target.
 	
 	@param	floor	The passenger's target floor.
 	*/
 	public void boardPassenger(int floor) {
		floor--;
 		targets[floor] = true;
 		passenger_targets[floor]++;
 	}
 	
 	/**
 	 * Override of toString
 	 */
 	public String toString() {
 		int passengers = 0;
 		for (int i=0; i<passenger_targets.length; i++) {
 			passengers += passenger_targets[i];
 		}
		return "Current Passengers: "+Integer.toString(passengers)+"\r\nCurrent Floor: "+Integer.toString(current_floor+1)+"\r\nDirection: "+(going_up?"Up":"Down");
	}
	
	/**
	 * For testing purposes
	 */
	static public void main(String argv[]) {
		Elevator elev = new Elevator();
		
		elev.boardPassenger(2);
		elev.boardPassenger(2);
		elev.boardPassenger(3);
		
		for (int i=0; i<10; i++) {
			elev.move();
		}
 	}
 }
