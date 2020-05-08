 package madesy.model;
 
 import java.util.Date;
 import java.util.UUID;
 
 import madesy.model.types.EventType;
 
 /**
  * Contains information describing each event
  * 
  */
 public class Event {
 	private String eventId = UUID.randomUUID().toString();
 	private Date date = new Date();
 	private String metaData = "";
 	private EventType eventType;
 	
 	public Event(EventType eventType) {
 		this.eventType = eventType;
 	}
 	
 	public Event(EventType eventType, String metaData) {
 		this(eventType);
 		this.metaData = metaData;
 	}
 
 	public String getEventId() {
 		return eventId;
 	}
 
 	public EventType getEventType() {
 		return eventType;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public String getMetaData() {
 		return metaData;
 	}
 	
 	@Override
 	public int hashCode() {
 		return eventId.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Event other = (Event) obj;
		if (eventId != other.eventId)
 			return false;
 		return true;
 	}
 	
 	@Override
 	public String toString() {
 		return "Event [eventId=" + eventId + ", eventType=" + eventType + ", date="
 				+ date + ", metaData=" + metaData + "]";
 	}
 }
