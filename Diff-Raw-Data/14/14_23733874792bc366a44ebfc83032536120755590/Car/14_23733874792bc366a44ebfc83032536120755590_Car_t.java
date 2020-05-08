 package rental.g3;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import rental.sim.RGid;
 
 class Car {
 	int cid;
 	int location;
 	int source;
 	int destination;
 	int driver = -1;
 	boolean inuse = false; 
 	boolean isDeposit = false;
 	List<RGid> passengers = new ArrayList<RGid>();
 	
 	// flag for next movement
 	int nextLoc = -1;
 	
 	public Car(int id, int src, int dst) {
 		this.cid = id;
 		this.source = src;
 		this.destination = dst;
 		this.location = this.source;
 	}
 	
 	public void move() {
 		if (nextLoc >= 0) {
 			location = nextLoc;
 			nextLoc = -1;
 		}
 	}
 	
 	public int getDriver() {
 		return driver;
 	}
 	public void setDriver(int driver) {
 		this.driver = driver;
 	}
 	public int getDestination() {
 		return destination;
 	}
 	public int getSource() {
 		return source;
 	}
 	public int getLocation() {
 		return location;
 	}
 	public void setLocation(int location) {
 		this.location = location;
 	}
 	public int getCid() {
 		return cid;
 	}
 	public boolean isDeposit() {
 		return isDeposit;
 	}
 	public void setDeposit(boolean isDeposit) {
		if(this.isDeposit) {
			// Don't ever regress deposit state.
			return;
		}
		
 		this.isDeposit = isDeposit;
 	}	
 	public void setNext(int nextLoc, boolean toDeposit) {
 		this.nextLoc = nextLoc;
		setDeposit(toDeposit);
 	}
 
 	public boolean isInuse() {
 		return inuse;
 	}
 
 	public void setInuse(boolean inuse) {
 		this.inuse = inuse;
 	}
 
 	public List<RGid> getPassengers() {
 		return passengers;
 	}
 
 	public void setPassengers(List<RGid> passengers) {
 		this.passengers = passengers;
 	}
 }
 	
