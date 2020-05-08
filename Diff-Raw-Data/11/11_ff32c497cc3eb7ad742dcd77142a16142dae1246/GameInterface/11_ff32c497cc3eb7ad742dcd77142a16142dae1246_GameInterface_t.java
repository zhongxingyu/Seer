 package game.ui;
 
 import game.logic.Game;
 
 abstract class GameInterface {
 	
 	protected Game game;
 	
 	public abstract void startGame();
	
	public Game getGame() {
		return game;
	}
 
 }
