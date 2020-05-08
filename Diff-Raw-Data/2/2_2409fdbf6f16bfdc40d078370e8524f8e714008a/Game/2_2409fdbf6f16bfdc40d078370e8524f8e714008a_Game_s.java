 package com.reddit.worddit.api.response;
 
 /**
  * A class to represent a game that a player can join.
  * @author OEP
  *
  */
 public class Game {
 	/** ID for this game */
 	public String id;
 	
 	/** Status for this game. Can be: {invited|accepted|active|waiting}. */
 	public String status;
 	
 	/** Current player for this game. */
 	public String current_player;
 	
 	/** Players for this game in order of rotation. */
 	public Player players[];
 	
 	/** Date of last move in UTC. */
 	public String last_move_utc;
 	
 	/**
 	 * Checks to see if the player has been invited, but has not accepted this game.
 	 * @return true if the game is waiting to have the player respond to the invitation
 	 */
 	public boolean isInvited() {
 		return status.equalsIgnoreCase(STATUS_INVITED);
 	}
 	
 	/**
 	 * Checks to see if the game has been accepted, but has not begun.
 	 * @return true if the game has been accepted but has not begun.
 	 */
 	public boolean isAccepted() {
 		return status.equalsIgnoreCase(STATUS_ACCEPTED);
 	}
 	
 	/**
 	 * Checks to see if this game is active.
 	 * @return true if game is active, but not waiting on the player to move
 	 */
 	public boolean isActive() {
 		return status.equalsIgnoreCase(STATUS_ACTIVE);
 	}
 	
 	/**
 	 * Checks to see if this game wants the player to move
 	 * @return true if this game is waiting for the player to move
 	 */
 	public boolean isWaiting() {
 		return status.equalsIgnoreCase(STATUS_WAITING);
 	}
 	
 	/**
 	 * A holder class to represent a player in a game.
 	 * @author pkilgo
 	 *
 	 */
	public class Player {
 		public Player() { }
 		
 		/** Player ID */
 		public String id;
 		
 		/** Player's score */
 		public String score;
 	}
 	
 	/** Constant values for the 'status' field. */
 	public static final String
 		STATUS_INVITED = "invited",
 		STATUS_ACCEPTED = "accepted",
 		STATUS_ACTIVE = "active",
 		STATUS_WAITING = "waiting";
 	
 	public static void main(String [] args) {
 		
 	}
 }
