 package no.ntnu.fp.common.model;
 
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.Calendar;
 
 public class Notification implements Model{
     private int ID;
     private final Calendar cal;
     private boolean is_invitation;
     private Event event;
     private PropertyChangeSupport pcs;
     private NotificationType type;
     private String timestamp;
 
     public Notification(int id, Event event, String timestamp, boolean is_invitation, NotificationType type){
     	this.ID = id;
         this.event = event;
         this.timestamp = timestamp;
         cal = Calendar.getInstance();
         this.is_invitation = is_invitation;
         this.type = type;
     }
 
     public Notification(JSONObject object) throws JSONException {
        this(object.getInt("id"), new Event(object.getJSONObject("event")), object.getString("timestamp"), object.getBoolean("is_invitation"), NotificationType.valueOf(object.getString("type")));
     }
 
 
 
     /**
      * returns the description string
      * @return description
      */
     public String getDescription() {
         String description = "";
         if (event == null) return description;
         switch(type) {
             case INVITATION:
                 description = /*getFirstName()*/"Person" + " invited you to " + event.getTitle(); break;
             case DELETION:
                 description = /*getFirstName()*/"Person" + " deleted " + event.getTitle(); break;
             case ACCEPTED:
                 description = /*getFirstName()*/"Person" + " accepted your invitation to " + event.getTitle(); break;
             case DECLINED:
                 description = /*getFirstName()*/"Person" + " declined your invitation to " + event.getTitle(); break;
         }
         return description;
     }
     
     /**
      * toString method, prints out the description.
      */
     @Override
     public String toString() {
         return getDescription();
     }
 
     public int getID() {
         return ID;
     }
 
     public Event getEvent() {
         return event;
     }
 
     public void setEvent(Event event) {
         this.event = event;
     }
 
     public String getTimestampString() {
         return timestamp;
     }
     
     public Calendar getTimestamp() {
     	return Calendar.getInstance();
     }
     
     public NotificationType getType() {
     	return type;
     }
 
     public int isInvitationAsInt() {
         if (this.is_invitation) return 1;
         else return 0;
 
     }
 
     @Override
     public boolean save() {
         return false;
     }
 
     @Override
     public void addPropertyChangeListener(PropertyChangeListener listener) {
         pcs.addPropertyChangeListener(listener);
     }
     
     public String getFirstName(Employee employee) {
     	return employee.getName().split(" ")[0];
     }
 
     public JSONObject toJson() throws JSONException {
         JSONObject object = new JSONObject();
         object.put("id", ID);
         object.put("is_invitation", is_invitation);
         object.put("event", event.toJson());
         object.put("type", type.toString());
         object.put("timestamp", timestamp);
         return object;
     }
 
     public enum NotificationType {
     	INVITATION, DELETION, CHANGE, ACCEPTED, DECLINED;
     }
 
 }
