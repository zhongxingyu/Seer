 package game;
 
 import grid.Square;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.Observable;
 
 /**
  * This class handles everything involving the turns in the game
  * 
  * @invar	The turnhandler has a game to handle the turns for.
  * 			| game != null
  * 
  * @author 	Group 8
  * 
  * @version	April 2013
  */
 public class TurnHandler extends Observable {
 	
 	/**
 	 * The turn queue of players in the game.
 	 */
 	private LinkedList<Player> playerQueue = new LinkedList<Player>() ;
 	
 	/**
 	 * Indicates who's turn it is.
 	 */
 	private Player currentPlayer;
 	
 	/**
 	 * The game which the TurnHandler handles the turns of
 	 */
 	private Game game;
 	
 	/**
 	 * Initializes the TurnHandler
 	 */
 	
 	private Square turnStartLocation; 
 	
 	public TurnHandler() {
 		
 	}
 	
 	/**
 	 * Returns the game currently being played.
 	 * 
 	 * @return	The game.
 	 */
 	public Game getGame() {
 		return game;
 	}
 
 	/**
 	 * This method sets the game.
 	 * 
 	 * @param 	game
 	 * 			The game being played.
 	 * 
 	 * @effect	The game has been set.
 	 */
 	public void setGame(Game game) {
 		this.game = game;
 	}
 
 	/**
 	 * Return who's turn it is.
 	 * 
 	 * @return	Who's turn it is.
 	 */
 	public Player getCurrentPlayer() {
 		return currentPlayer;
 	}
 
 	/**
 	 * Set the player who's turn it is of this game to the given player.
 	 * 
 	 * @param 	player 
 	 * 			The player that will get the turn.
 	 * 
 	 * @Post	The given player is the current player in the game.
 	 * 			| getCurrentPlayer == player
 	 */
 	private void setCurrentPlayer(Player player) {
 		this.currentPlayer = player;
 	}
 	
 	/**
 	 * Return an array of active players in this game.
 	 * 
 	 * @return 	The array of active players.
 	 */
 	public Player[] getActivePlayers() {
 		return playerQueue.toArray(new Player[getNbActivePlayers()]);
 	}
 	
 	/**
 	 * Return the number of active players in this game.
 	 * 
 	 * @return 	The number of players in this game.
 	 * 			| result == players.size();
 	 */
 	public int getNbActivePlayers(){
 		return playerQueue.size();
 	}
 	
 	/*********************
 	 * CORE FUNCTIONALITY
 	 *********************/
 	
 	/**
 	 * This method initializes the players in the turnhandler.
 	 * 
 	 * @param 	players
 	 * 			The players in the game.
 	 * 
 	 * @post	The players are added to the playerQueue.
 	 * 			| playerQueue.addAll(players)
 	 * 
 	 * @effect	The players have a turnhandler
 	 * 			| for each player in players do
 	 * 			| 	 player.setTurnhandler(this)
 	 * @effect	The first player may begin.
 	 * 			| goToNextPlayer()
 	 * @effect	The current player starts his turn
 	 * 			| getCurrentPlayer().startTurn()
 	 */
 	public void initializeTurnQueue(Player... players) {
 		// Add to the turn queue
 		playerQueue.addAll(Arrays.asList(players));
 		
 		// Set this turn handler
 		for(Player p: players)
 			p.setTurnHandler(this);
 		
 		// First one in the queue starts.
 		goToNextPlayer();
 		getCurrentPlayer().startTurn();
 	}
 	
 	/**
 	 * Go to the next player in the queue.
 	 * 
 	 * @effect 	The first player in the list of players is removed and added to the end of the list.
 	 * 			| players.addLast(players.removeFirst())
 	 * @effect 	The turn is set to the player that was in front of the list of players.
 	 * 			| setCurrentPlayer(players.removeFirst())
 	 */
 	private void goToNextPlayer() {
 		Player nextPlayer = playerQueue.removeFirst();
 		playerQueue.addLast(nextPlayer);
 		setCurrentPlayer(nextPlayer);
 		turnStartLocation = currentPlayer.getLocation();
 	}
 
 	/**
 	 * Check if the player who's turn it currently is, has more than 1 action left.
 	 * If he doesn't have any actions left, the turn goes to another player.
 	 * 
 	 * @effect	Check if the win or lose conditions are fulfilled.
 	 * 			|	turnHandler.checkWin()
 	 *  		|	turnHandler.checkLose()
 	 * @effect	The turns get switched when the current player has no actions left.
 	 * 			| if (getCurrentPlayer().getActionsLeft() <= 0
 	 * 			|	switchTurn();
 	 */
 	public void checkTurn() {
 		checkWin();
 		checkLose();
 		if(getCurrentPlayer().getActionsLeft() <= 0)
 			switchTurn();
 	}
 
 	/**
 	 * Switch turns.
 	 * 
 	 * @effect 	If the current player hasn't moved, does not skip his turn and has not received damage, he loses the game.
 	 * 			| if !getCurrentPlayer().hasMoved() && !getCurrentPlayer().skipsTurn() && !getCurrentPlayer().hasReceivedDamage() then
 	 * 			|	loseGame(getCurrentPlayer())
 	 * @effect	Go to the next player in the queue.
 	 * 			| if getCurrentPlayer().hasMoved() || getCurrentPlayer().skipsTurn() || getCurrentPlayer().hasReceivedDamage() then
 	 * 			| 	 goToNextPlayer()
 	 * @effect 	The turn of the new player is started.
 	 * 			| if getCurrentPlayer().hasMoved() || getCurrentPlayer().skipsTurn() || getCurrentPlayer().hasReceivedDamage() then
 	 * 			| 	 getCurrentPlayer().startTurn()
 	 * @effect	Check the turn again, to be sure.
 	 * 			| checkTurn();
 	 */
 	private void switchTurn() {
 		if(!getCurrentPlayer().hasMoved() && !getCurrentPlayer().skipsTurn() && !getCurrentPlayer().hasReceivedDamage()){
 			loseGame(getCurrentPlayer());
 		}
 		else{
 			goToNextPlayer();
 			currentPlayer.startTurn();
			checkTurn();
 		}
 		setChanged();
 		notifyObservers();
 	}
 	
 	/**
 	 * Check if a the current player now stands on the starting position of another player.
 	 * If that is the case: end the game and select the given player as the winner of objectron!
 	 * 
 	 * @effect	If a player is on the starting position of another player,
 	 * 			let him win the game.
 	 * 			| foreach otherPlayer do
 	 * 			|	 if player.getLocation() == other.getStartingPosition() then
 	 * 			|		 game.winGame(getCurrentPlayer())
 	 */
 	protected void checkWin() {	
 		for(Player other: game.getPlayers()) {
 			if(other != getCurrentPlayer()){
 				Square finish = other.getStartingPosition();
 				if(getCurrentPlayer().getLocation() == finish){
 					game.winGame(getCurrentPlayer());
 					break;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Check if a player has lost the game.
 	 * 
 	 * @effect	Lose the game, if the player is trapped.
 	 * 			| if isTrapped(currentPlayer) then
 	 * 			| 	 loseGame(currentPlayer)
 	 */
 	protected void checkLose() {
 		if(currentPlayer.isTrapped() || (currentPlayer.getLocation().equals(turnStartLocation) && !currentPlayer.hasReceivedDamage()))
 			loseGame(currentPlayer);
 	}
 	
 	/**
 	 * Let a player lose the game.
 	 * 
 	 * @param 	loser
 	 * 			The player who loses the game.
 	 * 
 	 * @effect	The loser is removed from the game.
 	 * 			| players.remove(loser)
 	 * @effect	If the number of players left is 1, this player wins the game.
 	 * 			| if(getNbPlayers() == 1){
 				| 	winGame(players.getFirst())
 	 */
 	private void loseGame(Player loser) {
 		playerQueue.remove(loser);
 		if(getNbActivePlayers() <= 1) 
 			game.winGame(playerQueue.get(0));
 	}
 	
 }
