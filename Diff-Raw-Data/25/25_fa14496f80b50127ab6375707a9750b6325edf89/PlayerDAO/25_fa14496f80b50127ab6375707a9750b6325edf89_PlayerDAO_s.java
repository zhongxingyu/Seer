 /* memory
  * de.thm.ateam.memory.engine.interfaces
  * PlayerDAO.java
  * 04.06.2012
  *
  * by Frank Kevin Zey
  */
 package de.thm.ateam.memory.engine.interfaces;
 
 import java.util.ArrayList;
 
 import de.thm.ateam.memory.engine.type.Player;
 
 /**
  * @author Frank Kevin Zey
  *
  */
 public interface PlayerDAO {
 	
 	static final String LOG_TAG = "MemoryPlayerDAO";
 	
 	/**
 	 * getAllPlayers returns all stored players
 	 * 
 	 * @return sequence(Player) All players, otherwise null
 	 */
 	ArrayList<Player> getAllPlayers();
 	
 	/**
 	 * getPlayer gives a specified player with given ID
 	 * 
 	 * @param id Specified ID of a player
 	 * @return Player Returns player with specified ID, if not exists null
 	 */
 	Player getPlayer(int id);
 	
 	/**
 	 * getPlayer returns player with specified nickname
 	 * 
 	 * @param nick Nickname of player
 	 * @return Player Returns player of specified nickname 'nick', if not exists null
 	 */
 	Player getPlayer(String nick);
 	
 	/**
 	 * storePlayer stores a new player object to database
 	 * 
 	 * @param p Player to be stored in database
 	 * @return boolean Returns true, if Player is stored, otherwise false
 	 */
 	boolean storePlayer(Player p);
 	
 	/**
 	 * updatePlayer updates informations of a player in the database
 	 * 
 	 * @param p Player, this information have to updated
 	 * @return boolean returns true, if update success, otherwise false
 	 */
 	boolean updatePlayer(Player p);
 	
 	/**
 	 * removePlayer deletes player from database, by given player
 	 * 
 	 * @param p Player, which be deleted
 	 * @return boolean Returns true, if player deleted, otherwise false
 	 */
 	boolean removePlayer(Player p);
 	
 	/**
 	 * removePlayer deletes Player by given ID of player
 	 * 
 	 * @param id ID of the player to be deleted
 	 * @return boolean Returns true, if player with id ID deleted, otherwise false
 	 */
	boolean removePlayer(int id);
 
 }
