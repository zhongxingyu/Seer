 package core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Elevator {
 	private final int capacity;
 	private int currentStorey;
 	private final int lastStorey;
 	private List<Passenger> passengers = new ArrayList<Passenger>();
 	private boolean upward;
 	
 	public Elevator(int capacity, int lastStorey) {
 		this.capacity = capacity;
		this.lastStorey = lastStorey;
 		currentStorey = 0;
 		upward = true;
 	}
 	
 	public int getStorey() {
 		return currentStorey;
 	}
 	
 	public void removePassenger(Passenger passenger) {
 		passengers.remove(passenger);
 	}
 	
 	public void takePassenger(Passenger passenger) {
 		if (!isFull()) {			
 			passengers.add(passenger);
 		}
 	}
 	
 	public boolean isFull() {
 		return capacity == passengers.size();
 	}
 	
 	private void changeDirection() {
 		upward = !upward;
 	}
 	
 	public void gotoNextStorey() {
 		currentStorey += upward ? 1 : -1;
 		
 		if (currentStorey == 0 || currentStorey == lastStorey) {
 			changeDirection();
 		}
 	}
 }
