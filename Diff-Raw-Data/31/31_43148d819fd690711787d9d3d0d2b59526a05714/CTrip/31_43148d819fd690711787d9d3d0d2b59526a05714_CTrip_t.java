 package bzb.gwt.planner.client.data;
 
 public class CTrip {
 	
    private long tripId;
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
 
	public void setTripId(long tripId) {
 		this.tripId = tripId;
 	}
 
	public long getTripId() {
 		return tripId;
 	}
 
 	public void setCreator(CUser creator) {
 		this.creator = creator;
 	}
 
 	public CUser getCreator() {
 		return creator;
 	}
 
 }
