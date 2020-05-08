 package com.planningpoker.model.observer;
 
 import com.planningpoker.model.Game;
 import com.planningpoker.model.Play;
 import com.planningpoker.model.Player;
 
 public interface Observer {
 	void newGame(Game game);
 	
 	void newPlayerHasEnteredInTheGame(Player player);
 	void newPlayHasBeenInitiated(Play play);
 	void gameHasBeenFinished();
 	
 	void playerHasVoteInCurrentPlay(Player player, int points);
 	void showResult(Play play);
 	
 	void playerIsOffline(Player player);
 }
