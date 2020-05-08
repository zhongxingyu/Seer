 package com.edinarobotics.scouting.definitions.event;
 
 /**
  * This class represents events that can be cancelled throughout the scouting system.
  * It makes implementing classes extending {@link Event} and implementing
  * {@link Cancellable} simpler.
  */
 public abstract class CancellableEvent extends Event implements Cancellable {
 	private boolean cancelled = false;
 	
 	public boolean isCancelled(){
 		return cancelled;
 	}
 	
 	public void setCancelled(boolean cancel){
 		cancelled = cancel;
 	}
 }
