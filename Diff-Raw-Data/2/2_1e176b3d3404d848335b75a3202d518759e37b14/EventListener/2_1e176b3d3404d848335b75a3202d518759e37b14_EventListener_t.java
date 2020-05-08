 package com.edinarobotics.scouting.definitions.event;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.annotation.Retention;
 
 /**
  * This annotation is used to mark class methods
  * that act as {@link Event} listeners.
  * The type of event to which this method is bound is determined by reflection.
  * It is the responsibility of the event firing class to properly bind
  * these listeners.
  */
 @Target(ElementType.METHOD)
 @Retention(RetentionPolicy.RUNTIME)
 public @interface EventListener {
 	
 	/**
 	 * The priority with which the bound event listener should be called.
 	 * @return The priority of the event listener.
 	 */
 	ListenerPriority priority() default ListenerPriority.NORMAL;
 	
 	/**
	 * If {@code true} this event listener will not receive cancelled
 	 * events. This can be useful if your listener will only cancel an
 	 * event, but never uncancel one.
 	 * @return The state of the {@code ignoreCancelled} flag.
 	 */
 	boolean ignoreCancelled() default false;
 }
