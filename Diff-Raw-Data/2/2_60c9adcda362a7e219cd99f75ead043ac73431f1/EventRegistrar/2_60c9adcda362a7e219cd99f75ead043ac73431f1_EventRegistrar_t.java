 package com.edinarobotics.scouting.definitions.event;
 
 /**
  * Interface for classes that can register event listeners to
  * receive events.
  */
 public interface EventRegistrar {
 	
 	/**
 	 * Registers all {@link EventListener} methods that can
 	 * receive all known {@link Event} types.
 	 * <br/><br/>
 	 * <em><b>Caution:</b></em> be careful when registering
 	 * a single {@link Listener} class with multiple
 	 * EventRegistrars that can register for the same Event.
 	 * For example registering your Listener with two separate
 	 * Database implementations. Dual registration of EventListener
 	 * methods can cause bugs.
	 * @param listener The Listener object whose EventListener
 	 * methods are to be registered.
 	 */
 	public void registerEvents(Listener listener);
 	
 	/**
 	 * Unregisters the given {@link Listener} from all {@link Event}
 	 * channels.
 	 * The given {@code listener} (if it is registered) will no longer
 	 * receive events from this EventRegistrar. If the given Listener
 	 * is not registered for this EventRegistrar, no changes will be made.
 	 * @param listener The Listener object to be unregistered.
 	 */
 	public void unregisterListener(Listener listener);
 }
