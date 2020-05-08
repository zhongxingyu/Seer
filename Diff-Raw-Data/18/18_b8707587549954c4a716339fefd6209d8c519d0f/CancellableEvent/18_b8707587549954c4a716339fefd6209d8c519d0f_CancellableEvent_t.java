 package com.edinarobotics.scouting.definitions.event;
 
 /**
  * This class represents events that can be cancelled throughout the scouting system.
  * It makes implementing classes extending {@link Event} and implementing
  * {@link Cancellable} simpler.
  */
 public abstract class CancellableEvent extends Event implements Cancellable {
 	private boolean cancelled = false;
 	
	/**
	 * Constructs a new CancellableEvent. This constructor assigns
	 * a random, unique ID value to this event.
	 * @see Event#Event()
	 */
	public CancellableEvent(){
		super();
	}
	
	/**
	 * Constructs a new CancellableEvent with a specified ID value.
	 * This constructor assigns the specified ID value to this event.
	 * @param id The String ID value to be assigned to this event.
	 */
	public CancellableEvent(String id){
		super(id);
	}
	
 	public boolean isCancelled(){
 		return cancelled;
 	}
 	
 	public void setCancelled(boolean cancel){
 		cancelled = cancel;
 	}
 }
