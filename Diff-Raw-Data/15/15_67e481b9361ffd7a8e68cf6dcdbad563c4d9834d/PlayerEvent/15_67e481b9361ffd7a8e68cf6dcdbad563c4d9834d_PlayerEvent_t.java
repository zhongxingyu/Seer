 package com.herocraftonline.dev.heroes.api;
 
 import org.bukkit.event.Event;
 
 @SuppressWarnings("serial")
 public class PlayerEvent extends Event{
 	protected boolean cancelled;
 	
 	protected PlayerEvent(final Type type) {
 		super(type);
 		this.cancelled = false;
 	}
 	
 	/**
 	 * Returns if the event is canceled or not
 	 * @return
 	 */
	public boolean isCancelled(){
 		return cancelled;
 	}
 
 	/**
 	 * Sets if the event is canceled or not.
 	 * @param cancelled
 	 */
 	public void setCancelled(Boolean cancelled){
 		this.cancelled = cancelled;
 	}
 }
