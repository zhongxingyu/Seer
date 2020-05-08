 package bzb.gwt.planner.client.data;
 
import com.google.appengine.api.datastore.Key;

 public class CTrip {
 	
    private Key tripId;
     private CUser creator;
 	private String name;
 	
 	public CTrip () {
 		
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 
	public void setTripId(Key tripId) {
 		this.tripId = tripId;
 	}
 
	public Key getTripId() {
 		return tripId;
 	}
 
 	public void setCreator(CUser creator) {
 		this.creator = creator;
 	}
 
 	public CUser getCreator() {
 		return creator;
 	}
 
 }
