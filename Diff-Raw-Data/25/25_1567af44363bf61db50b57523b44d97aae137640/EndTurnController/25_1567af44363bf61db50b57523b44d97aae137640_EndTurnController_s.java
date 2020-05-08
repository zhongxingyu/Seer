 package controller;
 
 import game.*;
 
 /**
  * Represents the controller for the END TURN usecase. 
  * This controller receives and coordinates the end turn operation to the game.
  * 
  * @author 	Groep 8
  * @version	February 2013
  */
 public class EndTurnController {
 
 	/**
 	 * Represents the game to which this controller passes the system operation.
 	 */
 	private Game game;
 	
 	/**
 	 * Initializes the end turn controller with a given existing game.
 	 * 
 	 * @param 	game
 	 * 			The given game.
 	 * @post	The game is set.
 	 * 			| this.game = game
 	 */
 	public EndTurnController(Game game) {
 		this.game = game;
 	}
 	
 	/**
 	 * End the turn of the current player.
 	 * 
 	 * @Pre		It is the player's turn, and the player has performed less than 3 actions in the current turn.
 	 * @effect	The actions of the player are set to 0.
 	 * 			| getCurrentPlayer().emptyActions()
 	 * @Effect	Check if the turn needs to be changed. This will of course be true now.
 	 * 			| game.checkTurn()
 	 */
 	public void endTurn()
 	{
 		// The player has confirmed.
 		Player player = game.getTurnHandler().getCurrentPlayer();
		// The system lets the character of the player perform an empty action 
		// for each remaining action in this turn.
		player.emptyActions();
 	}
 }
