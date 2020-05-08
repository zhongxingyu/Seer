 package com.edinarobotics.scouting.definitions.event;
 
 /**
  * These values represent all possible outcomes of a call
  * that creates an event.
  */
 public enum Result {
 	
 	/**
 	 * This {@link Result} indicates that the call's event was cancelled
 	 * before the call actually ran. The call was cancelled by a plugin.
 	 */
 	CANCELLED,
 	
 	/**
	 * This {@link Result} indicates that the call was run but
 	 * an error occurred while running it. Expect exceptions when
 	 * calling {@link Future#get()}.
 	 * @see Future#get()
 	 */
 	ERROR,
 	
 	/**
 	 * This {@link Result} indicates that the call went through correctly.
 	 * The results should be available through {@link Future#get()}.
 	 * @see Future#get()
 	 */
 	SUCCESS
 }
