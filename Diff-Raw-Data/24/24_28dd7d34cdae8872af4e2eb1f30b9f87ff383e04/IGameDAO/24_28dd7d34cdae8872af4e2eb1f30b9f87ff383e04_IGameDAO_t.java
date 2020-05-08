 package edu.wm.werewolf.dao;
 
 import edu.wm.werewolf.domain.Game;
 
 public interface IGameDAO {
 
 	boolean getIsRunning();
 
 	boolean isNight();
 
 	void createGame(Game game);
 
 	void endGame();
 
	void reset();

 }
