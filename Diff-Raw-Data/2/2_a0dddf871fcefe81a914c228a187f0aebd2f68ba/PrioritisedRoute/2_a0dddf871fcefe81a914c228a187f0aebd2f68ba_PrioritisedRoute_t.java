 package backend;
 
 import priority.Priority;
 import routes.Firm;
 import routes.Route;
 
 public class PrioritisedRoute {
 	private Route route;
 	private Priority priority;
 	
 	public void setRoute(Route route) {
 		this.route = route;
 	}
 	public Route getRoute() {
 		return route;
 	}
 	public void setPriority(Priority priority) {
 		this.priority = priority;
 	}
 	public Priority getPriority() {
 		return priority;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
		if (!(obj instanceof PrioritisedRoute))
 			return false;
 		PrioritisedRoute other = (PrioritisedRoute) obj;
 		if (this.route.equals(other.getRoute()) && this.priority == other.getPriority()){
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public String toString(){
 		return route.getD1().getName() + " - " + route.getD2().getName() + " (" + priority + ")";
 	}
 }
