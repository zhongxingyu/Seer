 package server.model;
 
 import java.util.ArrayList;
 
 public class Game {
 	private Board gameBoard = new Board();
 	private DrawPile drawPile = new DrawPile();
 	private ArrayList<Player> players = new ArrayList<Player>();
 
 	public Game(int numPlayers) {
 		// TODO input checking on number of players
 		// Initialize the players.
 		initPlayers(numPlayers);
 	}
 
 	/**
 	 * Initialize the players.
 	 * 
 	 * @param numPlayers
 	 *            The number of players which will be playing the game.
 	 */
 	private void initPlayers(int numPlayers) {
 
 		for (int i = 0; i < numPlayers; i++) {
 			players.add(new Player());
 		}
 	}
 
 	/**
 	 * Allow a player to take a tile from the draw pile.
 	 * 
 	 * @param player
 	 *            The player which receives the tile.
 	 */
 	public void drawTile(Player player) {
 		drawPile.draw(player);
 
 		// Re-draw if we get a tile which is not able to be placed.
 		while (!gameBoard.canPlaceTile(player)) {
 			drawPile.draw(player);
 		}
 	}
 
 	/**
 	 * Return whether there are tiles left in the draw pile.
 	 * 
 	 * @return A boolean indicating if the draw pile is empty.
 	 */
 	public boolean isDrawPileEmpty() {
 		return drawPile.isEmpty();
 	}
 
 	/**
 	 * Allow a player to place a tile on the game board.
 	 * 
 	 * @param player
 	 *            The player which is placing the tile.
 	 * @param xBoard
 	 *            The x position on the game board to place the tile.
 	 * @param yBoard
 	 *            The y position on the game board to place the tile.
 	 * 
 	 * @return a non-zero integer if the tile could not be placed, zero
 	 *         otherwise.
 	 */
 	public int placeTile(Player player, int xBoard, int yBoard) {
 		return gameBoard.placeTile(player, xBoard, yBoard);
 	}
 
 	/**
 	 * Allow a player to place a meeple on the game board.
 	 * 
 	 * @param player
 	 *            The player which is placing the meeple.
 	 * @param xBoard
 	 *            The x position the meeple is to be placed on the board.
 	 * @param yBoard
 	 *            The y position the meeple is to be placed on the board.
 	 * @param xTile
 	 *            The x position on the tile to place the meeple.
 	 * @param yTile
 	 *            The y position on the tile to place the meeple.
 	 * 
 	 * @return a non-zero integer if the meeple could not be placed, zero
 	 *         otherwise.
 	 */
 	public int placeMeeple(Player player, int xBoard, int yBoard, int xTile,
 			int yTile) {
 		return gameBoard.placeMeeple(player, xBoard, yBoard, xTile, yTile);
 	}
 
 	/**
 	 * Return the number of meeples a player has placed.
 	 * 
 	 * @param player
 	 *            The player to check for how many meeples they have placed.
 	 * 
 	 * @return the number of meeples which are in gameplay.
 	 */
 	public int getNumMeeplesPlaced(Player player) {
 		return gameBoard.getNumMeeplesPlaced(player);
 	}
 
 	/**
 	 * Score each player for all game features.
 	 * 
 	 * @param hasGameEnded
 	 *            True if scoring at the end of the game, false if scoring
 	 *            during the game.
 	 * 
 	 * @return an ArrayList of BoardPosition which represent meeples that have
 	 *         been removed from the board.
 	 */
 	public ArrayList<BoardPosition> score(boolean hasGameEnded) {
 
 		ArrayList<BoardPosition> meeples = new ArrayList<BoardPosition>();
 
 		meeples.addAll(gameBoard.scoreCloisters(players, hasGameEnded));
 		meeples.addAll(gameBoard.scoreRoads(players, hasGameEnded));
 		meeples.addAll(gameBoard.scoreCities(players, hasGameEnded));
 
 		if (hasGameEnded) {
 			meeples.addAll(gameBoard.scoreFields(players));
 		}
 
 		return meeples;
 	}
 
 	/**
 	 * Allow a player to leave the game before it has ended.
 	 * 
 	 * The player which is leaving the game will have their score set to zero,
 	 * their meeples removed from the board, and they will be removed from turn
 	 * rotation.
 	 * 
 	 * @param player
 	 *            The player that is leaving the game.
 	 */
 	public void exitGame(Player player) {
 
 		gameBoard.removeMeeples(player);
 
 		// This is going to be interesting; the game protocol uses the player's
 		// position in the array to identify them. Perhaps this should be
 		// converted to a hash map. (this was already done in gameui for ui
 		// elements bound to a specific user).
 	}
 
 	public ArrayList<Player> getPlayers() {
 		return players;
 	}
 
 	public int getNumPlayers() {
 		return players.size();
 	}
 	
	public int getMaxPlayers() {
 		return 5;
 	}
 
 	// Pass this off to the gameboard as it keeps track of positioning.
 	public Meeple getMeeple(int xBoard, int yBoard, int xTile, int yTile) {
 		return gameBoard.getMeeple(xBoard, yBoard, xTile, yTile);
 	}
 
 	// Pass off info about the board, used for ui to calculate the canvas size.
 	public int getBoardWidth() {
 		return gameBoard.getWidth();
 	}
 
 	public int getBoardHeight() {
 		return gameBoard.getHeight();
 	}
 }
