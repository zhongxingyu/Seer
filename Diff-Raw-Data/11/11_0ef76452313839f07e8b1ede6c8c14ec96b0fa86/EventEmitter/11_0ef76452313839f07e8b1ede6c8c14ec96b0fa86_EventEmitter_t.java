 package sat.events;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public abstract class EventEmitter implements EventEmitterInterface {
 	protected Set<EventListener> listeners = new HashSet<EventListener>();
 	protected EventEmitterInterface emitter;
 
 	public EventEmitter() {
 		this(null);
 	}
 
 	public EventEmitter(EventEmitterInterface emitter) {
 		this.emitter = emitter != null ? emitter : this;
 	}
 
 	public void addListener(EventListener listener) {
		if(listener != null) {
 			listeners.add(listener);
		}
 	}
 
 	public void removeListener(EventListener listener) {
 		listeners.remove(listener);
 	}
 
 	public void emit(Event event) {
 		for(EventListener listener : listeners) {
 			try {
 				event.notify(listener, emitter);
 			}
 			catch(Exception e) {
 				// Ignore exceptions when emitting events
 				//System.out.println(e);
 			}
 		}
 	}
 }
