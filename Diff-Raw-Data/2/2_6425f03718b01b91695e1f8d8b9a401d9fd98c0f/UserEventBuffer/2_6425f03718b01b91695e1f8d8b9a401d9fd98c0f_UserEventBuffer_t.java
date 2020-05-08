 package models;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 import play.Logger;
 import play.db.jpa.*;
 import play.libs.F.*;
 
 public class UserEventBuffer {
	final ArchivedEventStream<Event> stream = new ArchivedEventStream<Event>(100);
 
 	public Promise<List<IndexedEvent<Event>>> nextEvents(long lastReceived) {
 		return stream.nextEvents(lastReceived);
 	}
 
 	public void publish(Event e) {
 		stream.publish(e);
 	}
 
 	/**
 	 * GETTERS AND SETTERS
 	 */
 
 	public ArchivedEventStream getArchivedEventStream() {
 		return stream;
 	}
 
 	public EventStream getEventStream() {
 		return stream.eventStream();
 	}
 }
