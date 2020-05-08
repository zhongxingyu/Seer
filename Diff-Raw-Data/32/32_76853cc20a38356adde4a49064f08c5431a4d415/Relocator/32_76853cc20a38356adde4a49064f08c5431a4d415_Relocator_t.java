 package rental.g3;
 
 import java.util.List;
 import java.util.Stack;
 
 class Relocator {
 	public enum RelocatorStatus {		
 		ENROUTE, // the driver heading to his destination with no passenger
 		IDLING, // the driver who has a car available at the destination
 		WAITING, // a relocator who is waiting for a pick up		
 		PASSENGER, // a relocator in another driver's car to the next available car
 		PICKUP, // a relocator who is rerouting to pick another relocator
 		DROPOFF, // a relocator who carry other relocators
 		INVALID
 	}
 		
 	int rid;
 	int location;
 	RelocatorStatus status;
 	Car car = null;
 	Relocator pickuper = null;	
 	private boolean scheduled;
 	
 	
 	// Aux Structure:	
 	// for enrouting relocator
 	//private Route route;			
 	Stack<Route> routes = new Stack<Route>();
 	// for pickup relocator
 	//private List<Pickup> pickups;	
 	private int baseDestination;
 	
 	public Relocator(int id, RelocatorStatus s, int loc) {
 		rid = id;
 		status = s;
 		location = loc;
 	}
 	
 	public RelocatorStatus getStatus() {
 		return status;
 	}
 	public void setStatus(RelocatorStatus status) {
 		this.status = status;
 	}
 	public int getLocation() {
 		return location;
 	}
 	public void setLocation(int location) {
 		this.location = location;
 	}
 	public Stack<Route> getRoutes() {
 		return routes;
 	}
 	
 	public void move(RelocatorStatus nextStatus, int nextLoc) {
 		this.status = nextStatus;
 		this.location = nextLoc;
 		this.scheduled = true;
 	}
 	
 	public void pushRoute(Route r) {
 		routes.push(r);
 	}
 	public void popRoute() {
 		routes.pop();
 	}
 	public Route firstRoute() {
 		return routes.peek();
 	}
 
 	public boolean isScheduled() {
 		return scheduled;
 	}
 	
 	public void reset() {
 		this.scheduled = false;
 	}
 	
 	public void assignCar(Car car) {
 		this.car = car;
 		pushRoute(new Route(car.cid, car.destination));
 		this.baseDestination = car.destination;
		
 	}
 	
 	public boolean hasCar() {
 		return car != null;
 	}
 	
 	public boolean isDriving() {
 		return car != null && car.location == location;
 	}
 }
