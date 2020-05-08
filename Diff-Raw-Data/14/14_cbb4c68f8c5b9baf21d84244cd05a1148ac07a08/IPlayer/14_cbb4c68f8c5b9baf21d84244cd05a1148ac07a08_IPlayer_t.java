 package de.htwg.battleship.model;
 
 /**
  * @author Sandro, Julian
  * The Player Interface, for different Bots or Players;
  */
 public interface IPlayer {
 	
 	/**
 	 * Initializer for a new playboard
 	 * 
 	 * @param size
 	 *            the size of the new playboard
 	 */
	abstract void initPlayboard(int size);
 
 	/**
 	 * Getter for the player's number of remaining ships.
 	 * 
 	 * @return the number of ships
 	 */
	int getNumberShips();
 
 	/**
 	 * Setter for the player's remaining ships.
 	 * 
 	 * @param numberShips
 	 *            the new value for the remaining ships
 	 */
	void setNumberShips(int numberShips);
 	
 	/**
 	 * Getter for the player's playboard.
 	 * 
 	 * @return the players playboard
 	 */
	Playboard getPlayboard();
 
 	/**
 	 * Setter for the player's playboard
 	 * 
 	 * @param playboard
 	 *            the new playboard
 	 */
	void setPlayboard(Playboard playboard);
 }
