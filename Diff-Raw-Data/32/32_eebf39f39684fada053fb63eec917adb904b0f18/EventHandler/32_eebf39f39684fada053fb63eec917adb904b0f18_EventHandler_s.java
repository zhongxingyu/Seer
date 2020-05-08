 package com.punchline.javalib.utils.events;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.punchline.javalib.entities.Entity;
 
 /**
  * Handles multiple callbacks that subscribe to a certain event, and invokes each of them when the event occurs.
  * EventHandlers should be declared as public final so that other classes they cannot be re-assigned.
  * @author Natman64
  *
  */
 public class EventHandler {
 	
 	private Map<Object, EventCallback> callbacks = new HashMap<Object, EventCallback>();
 	
 	/**
 	 * Subscribes a callback to this handler's event.
 	 * @param key The key that can be used to remove this callback.
 	 * @param callback The callback to add.
 	 */
 	public void addCallback(Object key, EventCallback callback) {
 		callbacks.put(key, callback);
 	}
 	
 	/**
 	 * Unsubscribes a callback from this handler's event.
 	 * @param key The key of the callback to remove.
 	 */
 	public void removeCallback(Object key) {
 		callbacks.remove(key);
 	}
 	
 	/**
 	 * Invokes every {@link EventCallback} that is subscribed to this handler's event.
 	 * @param e The Entity that triggered this handler's event.
 	 * @param args Miscellaneous arguments for the event.
 	 */
 	public void invoke(Entity e, Object... args) {
		for (EventCallback callback : callbacks.values()) {
			callback.invoke(e, args);
		}
 	}
 	
 	/**
 	 * Clears this EventHandler's callbacks.
 	 */
 	public void clear() {
 		callbacks.clear();
 	}
 	
 }
