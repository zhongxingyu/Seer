 package game;
 
 import item.*;
 
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
 	 * The players of the game.
 	 */
 	private LinkedList<Player> players = new LinkedList<Player>() ;
 	
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
 	private GridCreator gridCreator;
 	
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
		setGridCreator(new RandomGridCreator(width, height));
 		setGrid(gridCreator.getGrid());
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
		setGridCreator(new TestCaseOneGridCreator());
 		setGrid(gridCreator.getGrid());
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
 		Player playerRed = new Player(redSquare,PlayerColour.RED);		
 		Player playerBlue = new Player(blueSquare,PlayerColour.BLUE);	
 		players.add(playerBlue);
 		players.add(playerRed);	
 		// Red always starts.
 		setCurrentPlayer(playerRed);
 	}
 	
 	/**
 	 * Return an array of the players in this game.
 	 * 
 	 * @return 	An array of the players in this game.
 	 * 			| result == players.toArray(new Player[getNbPlayers()])
 	 */
 	public Player[] getPlayers(){
 		return players.toArray(new Player[getNbPlayers()]);
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
 	
 	public GridCreator getGridCreator() {
 		return gridCreator;
 	}
 	
 	private void setGridCreator(GridCreator gridCreator) {
 		this.gridCreator = gridCreator;
 	}
 	
 	/**
 	 * Return the number of players in this game.
 	 * 
 	 * @return 	The number of players in this game.
 	 * 			| result == players.size();
 	 */
 	public int getNbPlayers(){
 		return players.size();
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
 	 * Let the player make a move in the given direction.
 	 * 
 	 * @param 	direction
 	 * @Pre		It is the player's turn, and the player has performed less than 3 actions in the current turn.
 	 * @Effect	The player makes a move.
 	 * 			| player.move(direction)
 	 * 			The direction in which the player wants to move.
 	 * @throws 	InvalidMoveException [must] 
 	 * 			When a move in the given direction is invalid (see inside Grid for more information).
 	 */
 	public void move(Direction direction) throws InvalidMoveException {
 		
 		Player player = getCurrentPlayer();
 		Square location = player.getLocation();
 		try {
 			if(getGrid().isValidMove(location, direction)) {
 				// The system moves the character of the player 1 square in the selected direction.
 				player.move(direction);
 				checkWin(player);
 				checkTurn();
 			} else throw new InvalidMoveException();
 		}
 		// No more actions left.
 		catch (NoMoreActionsException e) 
 		{	
 			// Extra safety.
 			switchTurn();
 		} catch (OutsideTheGridException e) {
 			throw new InvalidMoveException();
 		}
 	}
 
 	/**
 	 * Let the current player pick up an item from the location square.
 	 * 
 	 * @Pre		It is the player's turn, and the player has performed less than 3 actions in the current turn.
 	 * @Pre		If the player has not yet moved during this turn, he must have more than one action left.
 	 * @Effect	The user picked up the given item on the location square.
 	 * 			| player.pickUpItem(item)
 	 * @throws 	OverCapacityException [must]
 	 * 			The inventory of the player if already full.
 	 * @throws 	NoItemException [must]
 	 * 			When the given item is not one of the items on the location square.
 	 */
 	public void pickUpItem(Square location, Item item) throws NoItemException, OverCapacityException {
 		Player player = getCurrentPlayer();
 		try	{
 			// The system removes the item from the gameboard at the position of the player and
 			// puts it in the inventory of the player.
 			player.pickUpItem(item);
 			checkTurn();	
 		}
 		// No more actions left.
 		catch (NoMoreActionsException e) {
 			// Extra safety.
 			switchTurn();
 		}
 	}
 	
 	/**
 	 * Let the current player use a given item.
 	 * 
 	 * @Pre		It is the player's turn, and the player has performed less than 3 actions in the current turn.
 	 * @Pre		If the player has not yet moved during this turn, he must have more than one action left.
 	 * @Effect	The user has used an item.
 	 * 			| player.useItem(item)
 	 * @throws 	NoItemException [must]
 	 * 			When the given item is not contained in the inventory of the player.
 	 */
 	public void useItem(Item item) throws NoItemException {
 		// The player has selected an item from his inventory and indicated that he wants to use that item.
 		Player player = getCurrentPlayer();
 		try	{
 			player.useItem(item);
 			// The system adds 1 to the number of actions that the player has performed during this turn.
 			checkTurn();
 		}
 		// No more actions left.
 		catch (NoMoreActionsException e) {	
 			// Extra safety.
 			switchTurn();
 		}
 	}
 	
 	/**
 	 * End the turn of the current player.
 	 * 
 	 * @Pre		It is the player's turn, and the player has performed less than 3 actions in the current turn.
 	 * @effect	The actions of the player are set to 0.
 	 * 			| getCurrentPlayer().emptyActions()
 	 * @effect	The turn of the player ends.
 	 * 			| switchTurn()
 	 */
 	public void endTurn()
 	{
 		// The player has confirmed.
 		Player player = getCurrentPlayer();
 		// The system lets the character of the player perform an empty action 
 		// for each remaining action in this turn.
 		player.emptyActions();
 		switchTurn();
 	}
 	
 	/**
 	 * Check if the player who's turn it currently is, has more than 1 action left.
 	 * If he doesn't have any actions left, the turn goes to another player.
 	 */
 	protected void checkTurn() {
 		Player player = getCurrentPlayer();
 		if (player.getActionsLeft() == 0)
 			switchTurn();
 	}
 
 	/**
 	 * Switch turns.
 	 * 
 	 * @effect 	If the current player hasn't move, he loses the game.
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
 		if(!getCurrentPlayer().hasMoved()){
 			loseGame(getCurrentPlayer());
 		}
 		else{
 			Player nextPlayer = players.removeFirst();
 			players.addLast(nextPlayer);
 			setCurrentPlayer(nextPlayer);
 			
 			currentPlayer.startTurn();
 			
 			// At the start of each turn, each square has a 5% chance of losing power.
 			getGridCreator().setGrid(grid);
 			getGridCreator().spreadPowerFailures();
 			setGrid(getGridCreator().getGrid());
 		}
 	}
 	
 	/**
 	 * Check if a player now stands on the starting position of another player.
 	 * If that is the case: end the game and select the given player as the winner of objectron!
 	 * 
 	 * @param 	player
 	 * 			The player to be checked.
 	 */
 	private void checkWin(Player player) {		
 		for(Player other: getPlayers()) {
 			if(other != player){
 				Square finish = null;
 				finish = other.getStartingPosition();
 				if(player.getLocation() == finish){
 					winGame(getCurrentPlayer());
 					break;
 				}
 			}
 		}
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
 		players.remove(loser);
 		if(getNbPlayers() == 1){
 			winGame(players.getFirst());
 		}
 	}
 
 	
 }
