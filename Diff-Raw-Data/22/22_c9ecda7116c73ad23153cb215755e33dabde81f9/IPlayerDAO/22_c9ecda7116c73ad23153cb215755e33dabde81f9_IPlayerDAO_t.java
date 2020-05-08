 package edu.wm.werewolf.dao;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import edu.wm.werewolf.domain.GPSLocation;
 import edu.wm.werewolf.domain.Player;
 import edu.wm.werewolf.exceptions.NoRemainingPlayersException;
 import edu.wm.werewolf.exceptions.PlayerNotFoundException;
 
 public interface IPlayerDAO {
 
 	Player getPlayerByID(String ID) throws PlayerNotFoundException;
 	List<Player> getAllPlayers();
 	List<Player> getAllAlive();
 	//List<Player> getAllWerewolves();
 	//List<Player> getAllTownspeople();
	List<Player> getAllNear(String username, GPSLocation loc, int distance);
 	List<String> getAllIDs();
 	boolean insertPlayer(Player p);
 	boolean deletePlayerByID(String ID);
 	boolean deleteAllPlayers();
 	boolean move(Player p, GPSLocation loc);
 	boolean moveByUsername(String username, GPSLocation loc);
 	boolean setDead(Player victim);
 	boolean restartGame();
 	boolean setWerewolfByID(String ID);
 	Player getPlayerByUsername(String username) throws PlayerNotFoundException;
 	double getDistanceBetween(Player first, Player second) throws PlayerNotFoundException, SQLException;
 	boolean vote(Player voter, Player votingFor);
 	boolean logVotes() throws SQLException, NoRemainingPlayersException;
 	List<Player> getAliveWerewolves() throws SQLException;
 	List<Player> getAliveTownspeople() throws SQLException;
 	boolean removeInactivePlayers(int interval);
 
 }
