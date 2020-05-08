 package game;
 
 import java.util.*;
 
 import exception.*;
 import grid.*;
 
 /**
  * This class represents a game.
  * 
  * @author 	Groep 8
  * @version	February 2013
  */
 public class Game {
 	
 	/**
 	 * The grid of the game.
 	 */
 	private Grid grid;
 	
 	/**
 	 * Players that take part in the game.
 	 */
 	private Player[] players = new Player[PlayerColour.values().length];
 	
 	/**
 	 * The turn queue of players in the game.
 	 */
 	private LinkedList<Player> playerQueue = new LinkedList<Player>() ;
 	
 	/**
 	 * Indicates who's turn it is.
 	 */
 	private Player currentPlayer;
 	
 	/**
 	 * Indicate if the game is ended.
 	 */
 	private boolean gameEnded; 
 	
 	/**
 	 * Represents the winner of the game.
 	 */
 	private Player winner;
 	
 	/**
 	 * Represents the gridCreator, which is responsible for creating the grid.
 	 */
 	private GridBuilder gridBuilder;
 	
 	/**
 	 * Constructor for a game.
 	 * 
 	 * @param 	width
 	 * 			The width of the grid/game.
 	 * @param 	height
 	 * 			The height of the grid/game.
 	 * @effect	The grid is set.
 	 * 			| getGrid() == new GridCreator(width, height).getGrid()
 	 * @effect 	The players are initialized.
 	 * 			| initializePlayers();
 	 * @effect	The game is set not ended.
 	 * 			| getGameEnded() == false;
 	 * @throws	InvalidDimensionException  [must]
 	 * 			Dimension of the width and the height are invalid.
 	 */
 	public Game(int width, int height) throws InvalidDimensionException
 	{
 		setGridBuilder(new RandomGridBuilder(width, height));
 		setGrid(gridBuilder.getGrid());
 		initializePlayers();
 		setGameEnded(false);
 	}
 	
 	/**
 	 * Initialize a new game with a manually defined grid
 	 * @effect	The grid is set.
 	 * 			| getGrid() == new GridCreator(width, height).getGrid()
 	 * @effect 	The players are initialized.
 	 * 			| initializePlayers();
 	 * @effect	The game is set not ended.
 	 * 			| getGameEnded() == false;
 	 * @throws InvalidDimensionException  [must]
 	 * 			Dimension of the width and the height are invalid.
 	 */
 	public Game() throws InvalidDimensionException{
 		setGridBuilder(new TestCaseTwoGridBuilder());
 		setGrid(gridBuilder.getGrid());
 		initializePlayers();
 		setGameEnded(false);
 	}
 	
 	/********************
 	 * GETTERS & SETTERS
 	 ********************/
 
 	/**
 	 * Set the grid in this game (More information inside Grid).
 	 * 
 	 * @param	grid
 	 * 			The given grid to be set.
 	 */
 	public void setGrid(Grid grid)
 	{
 		this.grid = grid;
 	}
 	
 	/**
 	 * Return the grid in this game.
 	 * 
 	 * @return 	The grid in this game.
 	 */
 	public Grid getGrid()
 	{
 		return grid;
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
 	 * Set the player who's turn it is of this game to the given player
 	 * 
 	 * @param 	player 
 	 * 			The player that will get the turn.
 	 * @Post	The given player is the current player in the game.
 	 * 			| getCurrentPlayer == player
 	 */
 	private void setCurrentPlayer(Player player) {
 		this.currentPlayer = player;
 	}
 
 	/**
 	 * Initialize the players in this game.
 	 * Starting from the fact their is for the moment only a red and a blue player.
 	 * 
 	 * @Effect	Player Red is constructed and placed on his starting position.  
 	 * @Effect 	Player Blue is constructed and placed on his starting position.
 	 * @Effect	Both players are added to the list of players.
 	 * 			| players.add(players)
 	 * @Effect	The current player is set (which is player Red).
 	 * 			| getCurrentPlayer() == playerRed
 	 */
 	public void initializePlayers() {
 		Square redSquare = null;
 		Square blueSquare = null;
 		try {
 			redSquare = getGrid().getSquareAtCoordinate(getGrid().getStartingPositions().get(0));
 			blueSquare = getGrid().getSquareAtCoordinate(getGrid().getStartingPositions().get(1));
 		} catch (OutsideTheGridException e) {
 			// Can not occur.
 		}	
 		// Add the players to the game
 		players[0] = new Player(redSquare,PlayerColour.RED);		
 		players[1] = new Player(blueSquare,PlayerColour.BLUE);
 		// Add to the turn queue
		playerQueue.add(players[1]);
 		playerQueue.add(players[0]);
 		// Red always starts.
 		setCurrentPlayer(players[0]);
 		
 		getCurrentPlayer().startTurn();
 	}
 	
 	/**
 	 * Return an array of the players in this game.
 	 * 
 	 * @return 	An array of the players in this game.
 	 * 			| result == players
 	 */
 	public Player[] getPlayers(){
 		return players;
 	}
 	
 	/**
 	 * Return an array of active players in this game
 	 */
 	public Player[] getActivePlayers() {
 		return playerQueue.toArray(new Player[getNbActivePlayers()]);
 	}
 	
 	/**
 	 * Return the winner of the game.
 	 * 
 	 * @return	The winner of the game.
 	 */
 	public Player getWinner() {
 		return winner;
 	}
 
 	/**
 	 * Set the winner of the game.
 	 * 
 	 * @param 	winner
 	 * 			The winner of the game.
 	 * @Post	The given winner is the winner of the game.
 	 * 			| getWinner() == winner
 	 */
 	private void setWinner(Player winner) {
 		this.winner = winner;
 	}
 	
 	public GridBuilder getGridBuilder() {
 		return gridBuilder;
 	}
 	
 	private void setGridBuilder(GridBuilder gridBuilder) {
 		this.gridBuilder = gridBuilder;
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
 
 	/**
 	 * Return whether the game is ended.
 	 * 
 	 * @return	True if the game is ended. False if not.
 	 */
 	public boolean isGameEnded() {
 		return gameEnded;
 	}
 
 	/**
 	 * Set the game to be ended.
 	 * 
 	 * @param 	gameEnded
 	 * 			True or false.
 	 * @Post	The game is ended (or the game is not ended).
 	 * 			| getGameEnded == true (or == false)
 	 */
 	private void setGameEnded(boolean gameEnded) {
 		this.gameEnded = gameEnded;
 	}
 
 	/*********************
 	 * CORE FUNCTIONALITY
 	 *********************/
 	
 	/**
 	 * Check if the player who's turn it currently is, has more than 1 action left.
 	 * If he doesn't have any actions left, the turn goes to another player.
 	 */
 	public void checkTurn() {
 		Player player = getCurrentPlayer();
 		if (player.getActionsLeft() <= 0)
 			switchTurn();
 	}
 
 	/**
 	 * Switch turns.
 	 * 
 	 * @effect 	If the current player hasn't moved, he loses the game.
 	 * 			| if !getCurrentPlayer().hasMoved()
 	 * 			|	loseGame(getCurrentPlayer())
 	 * @effect 	The first player in the list of players is removed and added to the end of the list.
 	 * 			| if getCurrentPlayer().hasMoved()
 	 * 			| 	players.addLast(players.removeFirst())
 	 * @effect 	The turn is set to the player that was in front of the list of players.
 	 * 			| if getCurrentPlayer().hasMoved()
 	 * 			| 	setCurrentPlayer(players.removeFirst())
 	 * @effect 	The actions of the new player are renewed.
 	 * 			| if getCurrentPlayer().hasMoved()
 	 * 			| 	getCurrentPlayer().renewActions()
 	 */
 	private void switchTurn() {
 		if(!getCurrentPlayer().hasMoved() && !getCurrentPlayer().hasReceivedDamage()){
 			loseGame(getCurrentPlayer());
 		}
 		else{
 			Player nextPlayer = playerQueue.removeFirst();
 			playerQueue.addLast(nextPlayer);
 			setCurrentPlayer(nextPlayer);
 			getGridBuilder().setGrid(grid);
 			getGridBuilder().winPowerOnSquares();
 			getGridBuilder().spreadPowerFailures();
 			setGrid(getGridBuilder().getGrid());
 			
 			currentPlayer.startTurn();
 			checkTurn();
 		}
 	}
 	
 	/**
 	 * Check if a player now stands on the starting position of another player.
 	 * If that is the case: end the game and select the given player as the winner of objectron!
 	 * 
 	 * @param 	player
 	 * 			The player to be checked.
 	 */
 	public void checkWin(Player player) {		
 		for(Player other: getPlayers()) {
 			if(other != player){
 				Square finish = other.getStartingPosition();
 				if(player.getLocation() == finish){
 					winGame(getCurrentPlayer());
 					break;
 				}
 			}
 		}
 	}
 	
 	public void checkLose(Player player) {
 		if(isTrapped(player))
 			loseGame(player);
 	}
 
 	/**
 	 * Let a player win the game.
 	 * 
 	 * @param 	winner
 	 * 			The player who wins the game.
 	 * @effect	The game is ended.
 	 * 			| getGameEnded() == true
 	 * @effect	The player who wins is the winner.
 	 * 			| getWinner() == winner
 	 */
 	private void winGame(Player winner) {
 		setGameEnded(true);
 		setWinner(winner);
 	}
 	
 	/**
 	 * Let a player lose the game.
 	 * 
 	 * @param 	loser
 	 * 			The player who loses the game.
 	 * @effect	The loser is removed from the game.
 	 * 			| players.remove(loser)
 	 * @effect	If the number of players left is 1, this player wins the game.
 	 * 			| if(getNbPlayers() == 1){
 				| 	winGame(players.getFirst())
 	 */
 	private void loseGame(Player loser) {
 		playerQueue.remove(loser);
 		if(getNbActivePlayers() == 1){
 			winGame(playerQueue.getFirst());
 		}
 	}
 	
 	/**
 	 * Returns whether a player is trapped in its current position.
 	 * 
 	 * @return	True if there is any direction in which a player can perform a valid move.
 	 * 			False if not.
 	 */
 	private boolean isTrapped(Player currentPlayer) {
 		boolean isTrapped = true;
 		for(int i=0; i<Direction.values().length; i++) {
 			if(getGrid().isValidMove(currentPlayer.getLocation(), Direction.values()[i]))
 				isTrapped = false;
 		}
 		return isTrapped;
 	}
 	
 }
