 package game;
 
import exception.InvalidMoveException;

 /**
  * Interface that represents the possible events that can happen due to
  * moves of players.
  * 
  * @author 	Group 8
  * @version	April 2013
  */
 public interface Event {
 
 	/**
 	 * The event that happens when an actor starts on this.
 	 * 
 	 *  @param	Actor
 	 * 			The Actor starting on this event.
 	 */
 	public void onStart(Actor actor);
 	
 	/**
 	 * The event that happens when a player steps on this.
 	 * 
 	 * @param	Actor
 	 * 			The Actor stepping on this event.
 	 */
 	public void onStep(Actor actor);
 	
 	/**
 	 * The event that happens when an actor lands on this (due to teleport).
 	 * 
 	 *  @param	Actor
 	 * 			The Actor landing on this event.
 	 */
 	public void onLand(Actor actor);
 	
 	/**
 	 * The event that happens when an actor leaves this.
 	 * 
 	 * @param	Actor
 	 * 			The Actor leaving this event.
	 * 
	 * @throws  InvalidMoveException 
 	 */
	public void onLeave(Actor actor) throws InvalidMoveException;
 	
 }
