 package org.usfirst.frc1923;
 
 import java.util.Vector;
 
 import org.usfirst.frc1923.event.Event;
 
 /**
  * An event bus for managing incoming events.
  * 
  * @author Aaron Weiss
  * @version 2.0
  * @since 2/9/13
  */
 public class EventBus {
 	private static EventBus instance;
 	private final Vector events = new Vector();
 	
 	/**
 	 * Creates an <code>EventBus</code>.
 	 */
 	private EventBus() {
 		this.clear();
 	}
 	
 	/**
 	 * Pushes the event onto the bus.
 	 * @param e
 	 * 			the event to add
 	 */
 	public void push(Event e) {
 		this.events.addElement(e);
 	}
 	
 	/**
 	 * Runs the next event from the event bus.
 	 */
 	public void next() {
		if (this.hasNext()) {
			((Event) this.events.elementAt(0)).start();
			this.events.removeElementAt(0);
 		}
 	}
 	
 	/**
 	 * Gets whether or not the event bus has any events.
 	 * @return whether or not the event bus has events
 	 */
 	public boolean hasNext() {
 		return (this.events.capacity() > 0);
 	}
 	
 	/**
 	 * Clears the event bus completely.
 	 */
 	public void clear() {
 		this.events.removeAllElements();
 	}
 
 	/**
 	 * Gets the robot's instance of the event bus.
 	 * @return the robot's event bus
 	 */
 	public static EventBus getInstance() {
 		if (instance == null) {
 			instance = new EventBus();
 		}
 		return instance;
 	}
 }
