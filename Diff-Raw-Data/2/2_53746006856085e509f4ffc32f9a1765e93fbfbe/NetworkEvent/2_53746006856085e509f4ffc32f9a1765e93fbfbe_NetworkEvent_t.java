 package events;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import chronos.Person;
 
 /**
  * Wrapper class for all events sent to server.
  */
 abstract public class NetworkEvent implements Serializable {
 	protected static final long serialVersionUID = 4077361285383168257L;
 
 	public enum EventType {
 		LOGIN, //
 		CALENDAR, //
 		ROOM_BOOK, //
 		USER_SEARCH, //
 		TEST, //
		BATCH_CALENDAR, //
 		QUERY, //
 	}
 
 	ArrayList<?> results;
 	private EventType type;
 	protected Person person;
 
 	public NetworkEvent(EventType type) {
 		this.type = type;
 	}
 
 	public Person getSender() {
 		return person;
 	}
 
 	public void setPerson(Person person) {
 		this.person = person;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("Network Event with type: %s, from person: %s", type.name(), person.toString());
 	}
 
 	public EventType getType() {
 		return type;
 	}
 }
