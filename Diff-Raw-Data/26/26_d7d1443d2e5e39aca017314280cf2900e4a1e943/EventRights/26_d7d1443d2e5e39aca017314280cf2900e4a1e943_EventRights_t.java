 package model.userRights;
 
 public class EventRights extends ContentRights {
 	private int eventID;
 
 	public int getEventID() {
 		return eventID;
 	}
 
 	public void setEventID(int eventID) {
 		this.eventID = eventID;
 	}
 	
 	public String toValueNames() {
 		String valueNames = super.toValueNames() + ", eventID";
 		return valueNames;
 	}
 	
	public String toValues() {
 		String values = super.toValues() + ", "+eventID;
 		return values;
 	}
 	
 	public String[] toValueNamesArray() {
 		String[] valueNames = super.toValueNamesArray();
 		valueNames[4] = "eventID";
 		return valueNames;
 	}
 	
 	public String[] toValuesArray() {
 		String[] values = super.toValuesArray();
 		values[4] = "" + eventID;
 		return values;
 	}
 }
