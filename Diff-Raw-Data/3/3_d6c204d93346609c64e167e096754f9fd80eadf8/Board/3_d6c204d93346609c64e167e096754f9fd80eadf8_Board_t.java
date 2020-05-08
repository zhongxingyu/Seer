 package model;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 /**
  * @author Andrew Wylie <andrew.dale.wylie@gmail.com>
  * @version 1.0
  * @since 2012-07-02
  */
 public class Board {
 
 	private Tile[][] gameBoard;
 
 	// The board will keep track of the relationship between meeples and their
 	// position on it.
 	private HashMap<Meeple, BoardPosition> meeplePlacement;
 
 	/**
 	 * Initialize the game board. All tile positions are set to null to start.
 	 */
 	public Board() {
 
 		meeplePlacement = new HashMap<Meeple, BoardPosition>();
 
 		// We have 72 tiles to place, with the starting tile in the center
 		// of the board. This guarantees that the board can't extend outside
 		// of it's container. Use an odd number for an easier 'center' to
 		// place the beginning piece.
 		// The tile at 72, 72 is at the center of the board.
 
 		gameBoard = new Tile[145][145];
 
 		for (int i = 0; i < gameBoard.length; i++) {
 			Arrays.fill(gameBoard[i], null);
 		}
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
 
 		Tile tile = player.getCurrentTile();
 
 		if (tile != null && isTilePosValid(tile, xBoard, yBoard)) {
 
 			gameBoard[yBoard][xBoard] = tile;
 			player.setCurrentTile(null);
 			player.setLastTilePlacedPosition(xBoard, yBoard);
 
 			return 0;
 		}
 
 		return 1;
 	}
 
 	/**
 	 * Verifies whether a tile can be placed at a specified board position.
 	 * 
 	 * There are three separate conditions which have to be met. The first is
 	 * that the position for tile placement must not already be occupied by a
 	 * tile. Secondly, there must be at least one adjacent tile on either the
 	 * top, bottom, left, or right of the placement position. Lastly, the sides
 	 * of the tile to be placed must match with any adjacent tile sides.
 	 * 
 	 * Alternatively, the tile must be the first to be placed on the board.
 	 * 
 	 * @param tile
 	 *            The tile which is to be placed.
 	 * @param xBoard
 	 *            The x position the tile is to be placed on the board.
 	 * @param yBoard
 	 *            The y position the tile is to be placed on the board.
 	 * 
 	 * @return True if the move is valid, false otherwise.
 	 */
 	private boolean isTilePosValid(Tile tile, int xBoard, int yBoard) {
 
 		// Check that there is no tile in the specified position.
 		boolean free = (gameBoard[yBoard][xBoard] == null);
 
 		// Check that there is an adjacent tile wrt/ the specified position.
 		Tile top = gameBoard[yBoard - 1][xBoard];
 		Tile bottom = gameBoard[yBoard + 1][xBoard];
 		Tile right = gameBoard[yBoard][xBoard + 1];
 		Tile left = gameBoard[yBoard][xBoard - 1];
 
 		boolean adjacent = (top != null) || (bottom != null) || (right != null)
 				|| (left != null);
 
 		// Check that the adjacent tiles sides' match.
 		boolean topMatches = (top == null);
 		boolean bottomMatches = (bottom == null);
 		boolean rightMatches = (right == null);
 		boolean leftMatches = (left == null);
 
 		if (top != null && Arrays.deepEquals(top.getBottom(), tile.getTop())) {
 			topMatches = true;
 		}
 
 		if (bottom != null
 				&& Arrays.deepEquals(bottom.getTop(), tile.getBottom())) {
 			bottomMatches = true;
 		}
 
 		if (right != null
 				&& Arrays.deepEquals(right.getLeft(), tile.getRight())) {
 			rightMatches = true;
 		}
 
 		if (left != null && Arrays.deepEquals(left.getRight(), tile.getLeft())) {
 			leftMatches = true;
 		}
 
 		boolean sidesMatch = topMatches && bottomMatches && rightMatches
 				&& leftMatches;
 
 		return (free && adjacent && sidesMatch) || !hasGameStarted();
 	}
 
 	/**
 	 * Answer whether there has been a tile placed yet or not.
 	 * 
 	 * @return True if any board positions are not null, false otherwise.
 	 */
 	public boolean hasGameStarted() {
 
 		boolean gameStarted = false;
 
 		// Check if there has already been a tile placed on the board.
 		for (int i = 0; i < gameBoard.length; i++) {
 			for (int j = 0; j < gameBoard[i].length; j++) {
 				if (gameBoard[j][i] != null) {
 					gameStarted = true;
 				}
 			}
 		}
 
 		return gameStarted;
 	}
 
 	/**
 	 * Allow a player to place a meeple on the game board. The meeple must be
 	 * placed on the most recently played tile of the player. Also, the tile
 	 * type it is placed on (field, road, city, or cloister) must not be claimed
 	 * by another player's meeple.
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
 	 * @return An integer, with any non-zero value indicating an error.
 	 */
 	public int placeMeeple(Player player, int xBoard, int yBoard, int xTile,
 			int yTile) {
 
 		// Don't allow a player to play on a 'null' tiletype.
		if (gameBoard[yBoard][xBoard] == null
				|| gameBoard[yBoard][xBoard].getTileType(xTile, yTile) == null) {
 			return 1;
 		}
 
 		// First we need to check the correctness of the input x & y.
 		boolean xPlacedCorrectly = (xBoard == player.getLastTilePlacedXPos());
 		boolean yPlacedCorrectly = (yBoard == player.getLastTilePlacedYPos());
 
 		boolean correctTile = (xPlacedCorrectly && yPlacedCorrectly);
 
 		// Next we check that the feature is not already taken.
 		boolean newFeature = isNewFeature(xBoard, yBoard, xTile, yTile);
 
 		// If so, we place the meeple.
 		if (correctTile && newFeature) {
 
 			BoardPosition meeplePosition;
 			ArrayList<Meeple> meeples = player.getMeeples();
 
 			for (int i = 0; i < meeples.size(); i++) {
 
 				Meeple meeple = meeples.get(i);
 
 				if (meeplePlacement.get(meeple) == null) {
 
 					meeplePosition = new BoardPosition(xBoard, yBoard, xTile,
 							yTile);
 					meeplePlacement.put(meeple, meeplePosition);
 
 					return 0;
 				}
 			}
 		}
 
 		return 1;
 	}
 
 	/**
 	 * Find the number of meeples which are in play for a player.
 	 * 
 	 * @param player
 	 *            The player to check how many meeples are in play for.
 	 * 
 	 * @return The number of meeples which are in play.
 	 */
 	public int getNumMeeplesPlaced(Player player) {
 
 		int numMeeplesPlaced = 0;
 		ArrayList<Meeple> meeples = player.getMeeples();
 
 		for (int i = 0; i < meeples.size(); i++) {
 			if (meeplePlacement.get(meeples.get(i)) != null) {
 				numMeeplesPlaced++;
 			}
 		}
 
 		return numMeeplesPlaced;
 	}
 
 	/**
 	 * This function returns whether a meeple may be placed on the game board.
 	 * This function uses the method
 	 * {@link #isNewFeatureRecursive(HashSet, HashSet)} to search the game board
 	 * & contained tiles for any meeples which may have already claimed the
 	 * terrain/feature.
 	 * 
 	 * @param xBoard
 	 *            The x board position to start the search at.
 	 * @param yBoard
 	 *            The y board position to start the search at.
 	 * @param xTile
 	 *            The x tile position to start the search at.
 	 * @param yTile
 	 *            The y tile position to start the search at.
 	 * 
 	 * @return A boolean indicating whether the terrain is free to be claimed.
 	 */
 	private boolean isNewFeature(int xBoard, int yBoard, int xTile, int yTile) {
 
 		// Hold searched tile positions so we don't search any positions twice.
 		HashSet<BoardPosition> searched = new HashSet<BoardPosition>();
 		HashSet<BoardPosition> toSearch = new HashSet<BoardPosition>();
 
 		// Create the starting position.
 		BoardPosition boardPosition;
 		boardPosition = new BoardPosition(xBoard, yBoard, xTile, yTile);
 
 		toSearch.add(boardPosition);
 
 		// Do a recursive search on all adjacent cells for a meeple.
 		return isNewFeatureRecursive(searched, toSearch);
 	}
 
 	/**
 	 * This function returns whether a meeple may be placed on the game board.
 	 * 
 	 * @param searched
 	 *            A HashSet containing any searched tiles.
 	 * @param toSearch
 	 *            A HashSet containing any tiles to be searched.
 	 * 
 	 * @return A boolean indicating whether the terrain is free to be claimed.
 	 */
 	private boolean isNewFeatureRecursive(HashSet<BoardPosition> searched,
 			HashSet<BoardPosition> toSearch) {
 
 		// Take a position from the toSearch map.
 		Iterator<BoardPosition> toSearchIterator = toSearch.iterator();
 		BoardPosition boardPosition = null;
 
 		if (toSearchIterator.hasNext()) {
 			boardPosition = toSearchIterator.next();
 		} else {
 			return false;
 		}
 
 		int xBoard = boardPosition.xBoard;
 		int yBoard = boardPosition.yBoard;
 		int xTile = boardPosition.xTile;
 		int yTile = boardPosition.yTile;
 
 		Tile currentTile = gameBoard[yBoard][xBoard];
 
 		// Can't place a meeple on a tile which doesn't exist!
 		if (currentTile == null) {
 			return false;
 		}
 
 		// Search the position & add it to searched map.
 		// If there is a meeple on the feature then it isn't a new feature.
 		if (getMeeple(xBoard, yBoard, xTile, yTile) == null) {
 			toSearch.remove(boardPosition);
 			searched.add(boardPosition);
 		} else {
 			return false;
 		}
 
 		// Add valid neighbors of position to toSearch map.
 		// A neighbor is valid if it is in neither map,
 		// and is of the same tile type.
 		TileType currentTileType = currentTile.getTileType(xTile, yTile);
 
 		// Add to an array. And run through adding each to toSearch.
 		BoardPosition[] neighborTiles = getTileNeighbors(xBoard, yBoard, xTile,
 				yTile);
 
 		for (int i = 0; i < neighborTiles.length; i++) {
 
 			// Check the tile is not null.
 			Tile tile = gameBoard[neighborTiles[i].yBoard][neighborTiles[i].xBoard];
 
 			if (tile != null) {
 
 				// Check that the tile has the same tile type.
 				TileType tileType = tile.getTileType(neighborTiles[i].xTile,
 						neighborTiles[i].yTile);
 
 				if (tileType == currentTileType) {
 
 					BoardPosition toAdd = new BoardPosition(
 							neighborTiles[i].xBoard, neighborTiles[i].yBoard,
 							neighborTiles[i].xTile, neighborTiles[i].yTile);
 
 					// Check the tile is not already in searched or toSearch.
 					if (!toSearch.contains(toAdd) && !searched.contains(toAdd)) {
 						toSearch.add(toAdd);
 					}
 				}
 			}
 		}
 
 		// If toSearch is empty then we are done. We have found that this is
 		// indeed a new feature.
 		if (toSearch.isEmpty()) {
 			return true;
 		}
 
 		return isNewFeatureRecursive(searched, toSearch);
 	}
 
 	/**
 	 * Return the meeple on a tile given the its position.
 	 * 
 	 * This function is currently also used in the ui, as a helper function
 	 * which allows the game to get a meeple so that it can be placed on the
 	 * canvas component.
 	 * 
 	 * @param xBoard
 	 *            The x position on the board to check for the meeple.
 	 * @param yBoard
 	 *            The y position on the board to check for the meeple.
 	 * @param xTile
 	 *            The x position on the tile to check for the meeple.
 	 * @param yTile
 	 *            The y position on the tile to check for the meeple.
 	 * 
 	 * @return The meeple if one has claimed the tile, null otherwise.
 	 */
 	public Meeple getMeeple(int xBoard, int yBoard, int xTile, int yTile) {
 
 		Iterator<Meeple> iter = meeplePlacement.keySet().iterator();
 
 		while (iter.hasNext()) {
 
 			Meeple meeple = iter.next();
 			BoardPosition meeplePosition = meeplePlacement.get(meeple);
 
 			if (meeplePosition != null && meeplePosition.xBoard == xBoard
 					&& meeplePosition.yBoard == yBoard
 					&& meeplePosition.xTile == xTile
 					&& meeplePosition.yTile == yTile) {
 
 				return meeple;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Get the x & y position of a tile on the game board.
 	 * 
 	 * @param tile
 	 *            The tile to search for on the game board.
 	 * 
 	 * @return The position of the tile, or null if it was not found.
 	 */
 	private Point getTilePosition(Tile tile) {
 
 		for (int i = 0; i < gameBoard.length; i++) {
 			for (int j = 0; j < gameBoard[i].length; j++) {
 				if (gameBoard[i][j] != null && gameBoard[i][j] == tile) {
 					return new Point(j, i);
 				}
 			}
 		}
 
 		// The tile is not placed on the board.
 		return null;
 	}
 
 	/**
 	 * Score each player for any cloisters they may have. This function can be
 	 * called either during the game to score completed cloisters, or after the
 	 * game to score incomplete cloisters. The passed in boolean parameter
 	 * determines if incomplete cloisters are scored.
 	 * 
 	 * @param players
 	 *            An array of the players. Used with the meeple to figure out
 	 *            which player has scored.
 	 * @param hasGameEnded
 	 *            True if scoring at the end of the game, false if scoring
 	 *            during the game.
 	 * 
 	 * @return an ArrayList of BoardPosition which represent meeples that have
 	 *         been removed from the board.
 	 */
 	public ArrayList<BoardPosition> scoreCloisters(Player[] players,
 			boolean hasGameEnded) {
 
 		ArrayList<BoardPosition> removedMeeples = new ArrayList<BoardPosition>();
 		Iterator<Meeple> iter = meeplePlacement.keySet().iterator();
 
 		// For each meeple which is placed we check to see if it is placed on
 		// a cloister. Then depending on this and the game state we score.
 		while (iter.hasNext()) {
 
 			Meeple meeple = iter.next();
 			BoardPosition meeplePosition = meeplePlacement.get(meeple);
 
 			// Check to see if it is attached to a cloister.
 			Tile tile = gameBoard[meeplePosition.yBoard][meeplePosition.xBoard];
 			TileType tileType = tile.getTileType(meeplePosition.xTile,
 					meeplePosition.yTile);
 
 			// If it is attached to a cloister.
 			if (tileType == TileType.CLOISTER) {
 				// If all 8 neighbor tiles are not null.
 				Point tilePosition = getTilePosition(tile);
 				int xIile = tilePosition.x;
 				int yTile = tilePosition.y;
 
 				Tile nTile = gameBoard[xIile][yTile - 1];
 				Tile neTile = gameBoard[xIile + 1][yTile - 1];
 				Tile eTile = gameBoard[xIile + 1][yTile];
 				Tile seTile = gameBoard[xIile + 1][yTile + 1];
 				Tile sTile = gameBoard[xIile][yTile + 1];
 				Tile swTile = gameBoard[xIile - 1][yTile + 1];
 				Tile wTile = gameBoard[xIile - 1][yTile];
 				Tile nwTile = gameBoard[xIile - 1][yTile - 1];
 
 				Tile[] neighborTiles = { nTile, neTile, eTile, seTile, sTile,
 						swTile, wTile, nwTile };
 
 				int numNeighborTiles = 0;
 
 				// Count the number of placed (non-null) neighbor tiles.
 				for (int i = 0; i < neighborTiles.length; i++) {
 					if (neighborTiles[i] != null) {
 						numNeighborTiles++;
 					}
 				}
 
 				// Score if the game has ended, or score if the game
 				// is still being played and the cloister is complete.
 				if (hasGameEnded || (!hasGameEnded && numNeighborTiles == 8)) {
 
 					// TODO something is wrong;
 					// after all 8 neighbor tiles were placed the meeple wasn't
 					// removed. however end-game scoring removed it.
 
 					// Find out which player owns the meeple.
 					Player scorer = null;
 
 					for (int i = 0; i < players.length; i++) {
 						if (players[i].getMeeples().contains(meeple)) {
 							scorer = players[i];
 						}
 					}
 
 					// Then add to the player's score.
 					int playerScore = scorer.getScore();
 					scorer.setScore(playerScore + numNeighborTiles + 1);
 
 					// And remove the meeple from the tile.
 					removedMeeples.add(meeplePlacement.get(meeple));
 					meeplePlacement.remove(meeple);
 
 					// Since we are iterating through meeplePlacement
 					// we need to refresh it when it's altered.
 					iter = meeplePlacement.keySet().iterator();
 				}
 			}
 		}
 
 		return removedMeeples;
 	}
 
 	/**
 	 * Score the cities, using the helper function
 	 * {@link #genericScore(Player[], TileType, boolean)}.
 	 * 
 	 * @param hasGameEnded
 	 *            A boolean indicating whether the game has ended.
 	 * 
 	 * @return an ArrayList of BoardPosition which represent meeples that have
 	 *         been removed from the board.
 	 */
 	public ArrayList<BoardPosition> scoreCities(Player[] players,
 			boolean hasGameEnded) {
 		return genericScore(players, TileType.CITY, hasGameEnded);
 	}
 
 	/**
 	 * Score the roads, using the helper function
 	 * {@link #genericScore(Player[], TileType, boolean)}.
 	 * 
 	 * @param hasGameEnded
 	 *            A boolean indicating whether the game has ended.
 	 * 
 	 * @return an ArrayList of BoardPosition which represent meeples that have
 	 *         been removed from the board.
 	 */
 	public ArrayList<BoardPosition> scoreRoads(Player[] players,
 			boolean hasGameEnded) {
 		return genericScore(players, TileType.ROAD, hasGameEnded);
 	}
 
 	/**
 	 * Find out which players have the majority of meeples on a given feature.
 	 * The player(s) with the most meeples on a feature receive the points for
 	 * the feature.
 	 * 
 	 * @param players
 	 *            An array of players which are playing the game.
 	 * @param meeplesOnFeature
 	 *            An ArrayList of meeples which are on the feature.
 	 * 
 	 * @return An ArrayList of players that will receive the points for the
 	 *         feature.
 	 */
 	private ArrayList<Player> getFeatureScorers(Player[] players,
 			ArrayList<Meeple> meeplesOnFeature) {
 
 		// For each player check if they own each meeple on the feature; if they
 		// do then add to the count of meeples which they have on the feature.
 
 		// At the same time keep track of the maximum number of meeples found
 		// on the feature, as the player with the most meeples on the feature
 		// will get the points for it.
 
 		// As there can be mulitple players which score (same # of meeples on
 		// the feature) we'll also keep track via an array of players (who get
 		// to score for the feature!).
 
 		HashMap<Player, Integer> nMeeples = new HashMap<Player, Integer>();
 		ArrayList<Player> scoringPlayers = new ArrayList<Player>();
 		int max = 0;
 
 		for (int i = 0; i < players.length; i++) {
 
 			Player player = players[i];
 
 			for (int j = 0; j < meeplesOnFeature.size(); j++) {
 
 				if (player.getMeeples().contains(meeplesOnFeature.get(j))) {
 
 					if (nMeeples.get(player) == null) {
 
 						nMeeples.put(player, 1);
 
 					} else {
 
 						int newScore = nMeeples.get(player) + 1;
 						nMeeples.put(player, newScore);
 					}
 
 					int score = nMeeples.get(player);
 
 					// Keep track of our scoringPlayers array, &
 					// update max variable if neccessary.
 					if (score == max) {
 
 						scoringPlayers.add(player);
 
 					} else if (score > max) {
 
 						max = score;
 						scoringPlayers.clear();
 						scoringPlayers.add(player);
 					}
 
 				}
 			}
 		}
 
 		return scoringPlayers;
 	}
 
 	/**
 	 * Generic scoring function used to score roads & cities. The function runs
 	 * through each meeple, determining if each is on a tile of the specified
 	 * type. If so, another helper function
 	 * {@link #genericScoreRecursive(HashSet, HashSet, Object[])} is called to
 	 * assist by finding all the meeples on that feature, whether the feature is
 	 * complete, and the number of tiles taken by the feature. This information
 	 * is then used to complete scoring. Scoring is done by finding the
 	 * player(s) which has the maximum number of meeples on the feature and then
 	 * giving them the points. Meeples are also returned to their owners, and
 	 * different scoring types are accounted for through a score multiplier
 	 * (cities during the game).
 	 * 
 	 * @param players
 	 *            An array of the players. This is used for scoring purposes.
 	 * @param scoreTileType
 	 *            The tile type to score for.
 	 * @param hasGameEnded
 	 *            A boolean indicating whether the game has ended.
 	 * 
 	 * @return an ArrayList of BoardPosition which represent meeples that have
 	 *         been removed from the board.
 	 */
 	private ArrayList<BoardPosition> genericScore(Player[] players,
 			TileType scoreTileType, boolean hasGameEnded) {
 
 		// Initialize the variables which will hold information about the
 		// specific feature. These are reset for each feature.
 		int nTiles;
 		boolean nullTileFound;
 		HashSet<BoardPosition> searched;
 		HashSet<BoardPosition> toSearch;
 		ArrayList<Meeple> meeplesOnFeature;
 
 		// And other variables.
 		ArrayList<BoardPosition> removedMeeples = new ArrayList<BoardPosition>();
 
 		// Run through all the placed meeples.
 		Iterator<Meeple> iter = meeplePlacement.keySet().iterator();
 
 		while (iter.hasNext()) {
 
 			// Reset the variables which hold info about specific features.
 			nTiles = 0;
 			nullTileFound = false;
 			searched = new HashSet<BoardPosition>();
 			toSearch = new HashSet<BoardPosition>();
 			meeplesOnFeature = new ArrayList<Meeple>();
 			Object[] featureProperties = { nTiles, nullTileFound };
 
 			// And now the real work begins.
 			BoardPosition meeplePosition = meeplePlacement.get(iter.next());
 
 			// Check to see if it is attached to the correct tile type.
 			Tile tile = gameBoard[meeplePosition.yBoard][meeplePosition.xBoard];
 			TileType tileType = tile.getTileType(meeplePosition.xTile,
 					meeplePosition.yTile);
 
 			// If it is attached to the correct tile type.
 			if (tileType == scoreTileType) {
 
 				// Init search.
 				Point tilePosition = getTilePosition(tile);
 				BoardPosition boardPosition = new BoardPosition(tilePosition.x,
 						tilePosition.y, meeplePosition.xTile,
 						meeplePosition.yTile);
 
 				toSearch.add(boardPosition);
 
 				// Call our search.
 				genericScoreRecursive(searched, toSearch, meeplesOnFeature,
 						featureProperties);
 
 				// Recover tracker variables from object array.
 				nTiles = (Integer) featureProperties[0];
 				nullTileFound = (Boolean) featureProperties[1];
 
 				// Score multiplier depends on the feature.
 				// The base multiplier is 1.
 				int multiplier = 1;
 
 				// Each city tile is worth 2 points during the game.
 				if (!hasGameEnded && scoreTileType == TileType.CITY) {
 					multiplier = 2;
 				}
 
 				// We are scoring if the feature is complete and
 				// the game is not over, or if the game is over.
 				if (hasGameEnded || (!nullTileFound && !hasGameEnded)) {
 
 					ArrayList<Player> scoringPlayers = getFeatureScorers(
 							players, meeplesOnFeature);
 
 					// Remove the meeples from the board.
 					for (int i = 0; i < meeplesOnFeature.size(); i++) {
 
 						Meeple meeple = meeplesOnFeature.get(i);
 						removedMeeples.add(meeplePlacement.get(meeple));
 						meeplePlacement.remove(meeple);
 					}
 
 					// Since we are iterating through meeplePlacement
 					// we need to refresh it when it's altered.
 					iter = meeplePlacement.keySet().iterator();
 
 					// Recalculate scores.
 					for (int i = 0; i < scoringPlayers.size(); i++) {
 
 						int score = scoringPlayers.get(i).getScore();
 						int newScore = (nTiles * multiplier) + score;
 						scoringPlayers.get(i).setScore(newScore);
 					}
 				}
 			}
 		}
 
 		return removedMeeples;
 	}
 
 	/**
 	 * This method calculates the number of tiles held by a meeple. This method
 	 * is used for the score calculation for both roads and cities in game and
 	 * at the end of a game. It is to be called by one of the main score
 	 * calculation methods which will pass in the proper arguments. A similar
 	 * technique to what is used in
 	 * {@link #isNewFeatureRecursive(HashSet, HashSet)} is utilized for the main
 	 * tile search. While the search is occurring there are several separate
 	 * data items kept track of. These include: <li>A list of meeples which are
 	 * located on the same feature. <li>The total number of tiles which comprise
 	 * the feature. <li>Whether we have found any adjacent tiles which are null;
 	 * this determines if the feature is incomplete.
 	 * 
 	 * @param searched
 	 *            A HashSet of Strings of board & tile coordinates which have
 	 *            been used.
 	 * @param toSearch
 	 *            A HashSet of Strings of board & tile coordinates which are to
 	 *            be searched.
 	 * @param meeplesOnFeature
 	 *            An ArrayList of Meeple to be filled by the meeples which are
 	 *            found on the feature.
 	 * @param featureProperties
 	 *            An array of objects containing the number of tiles which
 	 *            comprise the feature, and a boolean indicating the presence of
 	 *            a null tile.
 	 */
 	private void genericScoreRecursive(HashSet<BoardPosition> searched,
 			HashSet<BoardPosition> toSearch,
 			ArrayList<Meeple> meeplesOnFeature, Object[] featureProperties) {
 
 		// Take a position from the toSearch map to search.
 		Iterator<BoardPosition> toSearchIterator = toSearch.iterator();
 		BoardPosition boardPosition = null;
 
 		if (toSearchIterator.hasNext()) {
 			boardPosition = toSearchIterator.next();
 		} else {
 			return;
 		}
 
 		int xBoard = boardPosition.xBoard;
 		int yBoard = boardPosition.yBoard;
 		int xTile = boardPosition.xTile;
 		int yTile = boardPosition.yTile;
 
 		Tile currentTile = gameBoard[yBoard][xBoard];
 
 		// Search the position & add it to searched map.
 		Meeple meeple = getMeeple(xBoard, yBoard, xTile, yTile);
 
 		if (meeple != null) {
 			// Add the meeple on the tile to our list.
 			meeplesOnFeature.add(meeple);
 		}
 
 		toSearch.remove(boardPosition);
 		searched.add(boardPosition);
 
 		// Add valid neighbors of position to toSearch map.
 		// A neighbor is valid if it is in neither map,
 		// and is of the same tile type.
 		TileType currentTileType = currentTile.getTileType(xTile, yTile);
 
 		// Add to an array. And run through adding each to toSearch.
 		BoardPosition[] neighborTiles = getTileNeighbors(xBoard, yBoard, xTile,
 				yTile);
 
 		for (int i = 0; i < neighborTiles.length; i++) {
 
 			// Check the tile is not null.
 			Tile tile = gameBoard[neighborTiles[i].yBoard][neighborTiles[i].xBoard];
 
 			if (tile != null) {
 
 				// Check that the tile has the same tile type.
 				TileType tileType = tile.getTileType(neighborTiles[i].xTile,
 						neighborTiles[i].yTile);
 
 				if (tileType == currentTileType) {
 
 					BoardPosition toAdd = new BoardPosition(
 							neighborTiles[i].xBoard, neighborTiles[i].yBoard,
 							neighborTiles[i].xTile, neighborTiles[i].yTile);
 
 					// Check the tile is not already in searched or toSearch.
 					if (!toSearch.contains(toAdd) && !searched.contains(toAdd)) {
 						toSearch.add(toAdd);
 					}
 				}
 
 			} else {
 				// If we have a neighboring tile which is null then the
 				// feature is not complete.
 				featureProperties[1] = true;
 			}
 		}
 
 		// If there are still tiles in toSearch then continue searching.
 		if (!toSearch.isEmpty()) {
 			genericScoreRecursive(searched, toSearch, meeplesOnFeature,
 					featureProperties);
 		} else {
 			// Get the number of tiles searched.
 			HashSet<BoardPosition> searchedTiles = new HashSet<BoardPosition>();
 			Iterator<BoardPosition> searchedIterator = searched.iterator();
 
 			while (searchedIterator.hasNext()) {
 
 				boardPosition = searchedIterator.next();
 
 				BoardPosition tilePosition = new BoardPosition(
 						boardPosition.xBoard, boardPosition.yBoard, 0, 0);
 
 				searchedTiles.add(tilePosition);
 			}
 
 			// Set the number of tiles searched.
 			featureProperties[0] = searchedTiles.size();
 		}
 	}
 
 	/**
 	 * This function scores the fields at the end of the game. It uses
 	 * {@link #fieldScoreRecursive(HashSet, HashSet, ArrayList, ArrayList, ArrayList)}
 	 * and {@link #getCompletedCities()}. This function relies heavily on the
 	 * aforementioned helper functions and does not do much else besides set up
 	 * the variables to be passed to the helpers, recalculate the player scores,
 	 * and meeple removal after scoring.
 	 * 
 	 * @param players
 	 *            An array of the players. Used for scoring calculations.
 	 * 
 	 * @return an ArrayList of BoardPosition which represent meeples that have
 	 *         been removed from the board.
 	 */
 	// Basic overview:
 	//
 	// Check all meeples for claim to a field,
 	// Once one is found do a search of the whole field to record both
 	// any other meeples on it, and which completed cities are adjacent.
 	// Use the gathered info to recalculate scores & return meeples to players.
 	//
 	// To do this:
 	//
 	// 1. Use the flooding algorithm altered to keep track of completed cities.
 	// It will return a list of 'cities', each of which will be a set of
 	// BoardPosition objects which will hold the xBoard, yBoard, xTile, yTile.
 	//
 	// 2. Use another flooding algorithm to detect if there is an adjacent
 	// castle to the field being explored. If there is, then check if it is a
 	// completed castle and add points, & the castle for future checking.
 	//
 	public ArrayList<BoardPosition> scoreFields(Player[] players) {
 
 		// Initialize the variables which will hold information about the
 		// specific feature. These are reset for each feature.
 		HashSet<BoardPosition> searched;
 		HashSet<BoardPosition> toSearch;
 		ArrayList<Meeple> meeplesOnFeature;
 		ArrayList<HashSet<BoardPosition>> adjacentCities;
 
 		// And other variables.
 		ArrayList<BoardPosition> removedMeeples = new ArrayList<BoardPosition>();
 		ArrayList<HashSet<BoardPosition>> completedCities = getCompletedCities();
 
 		// Run through all the placed meeples.
 		Iterator<Meeple> iter = meeplePlacement.keySet().iterator();
 
 		while (iter.hasNext()) {
 
 			// Reset the variables which hold info about specific features.
 			searched = new HashSet<BoardPosition>();
 			toSearch = new HashSet<BoardPosition>();
 			meeplesOnFeature = new ArrayList<Meeple>();
 			adjacentCities = new ArrayList<HashSet<BoardPosition>>();
 
 			// And now the real work begins.
 			BoardPosition meeplePosition = meeplePlacement.get(iter.next());
 
 			Tile tile = gameBoard[meeplePosition.yBoard][meeplePosition.xBoard];
 			TileType tileType = tile.getTileType(meeplePosition.xTile,
 					meeplePosition.yTile);
 
 			if (tileType == TileType.FIELD) {
 
 				// Init search.
 				Point tilePosition = getTilePosition(tile);
 				BoardPosition boardPosition = new BoardPosition(tilePosition.x,
 						tilePosition.y, meeplePosition.xTile,
 						meeplePosition.yTile);
 
 				toSearch.add(boardPosition);
 
 				// Call the search.
 				fieldScoreRecursive(searched, toSearch, meeplesOnFeature,
 						completedCities, adjacentCities);
 
 				// Recover the tracker variables.
 				// Only primitives need to be recovered.
 				int nCities = adjacentCities.size();
 
 				// Score multiplier.
 				int multiplier = 3;
 
 				ArrayList<Player> scoringPlayers = getFeatureScorers(players,
 						meeplesOnFeature);
 
 				// Remove the meeples from the board.
 				for (int i = 0; i < meeplesOnFeature.size(); i++) {
 
 					Meeple meepleOnFeature = meeplesOnFeature.get(i);
 					removedMeeples.add(meeplePlacement.get(meepleOnFeature));
 					meeplePlacement.remove(meepleOnFeature);
 				}
 
 				// Since we are iterating through meeplePlacement
 				// we need to refresh it when it's altered.
 				iter = meeplePlacement.keySet().iterator();
 
 				// Recalculate scores.
 				for (int i = 0; i < scoringPlayers.size(); i++) {
 
 					int score = scoringPlayers.get(i).getScore();
 					int newScore = (nCities * multiplier) + score;
 					scoringPlayers.get(i).setScore(newScore);
 				}
 			}
 		}
 
 		return removedMeeples;
 	}
 
 	/**
 	 * This function returns a list of the completed cities which exist. This is
 	 * the first of two helper functions which are used by
 	 * {@link #scoreFields()}. The other is the {@link #fieldScoreRecursive()}.
 	 * 
 	 * It searches through the game board position by position testing if each
 	 * is a city tile. If the position is a city tile and is not already part of
 	 * a completed city, then a new city is recorded using the
 	 * {@link #getCompletedCitiesRecursive(HashSet, HashSet, ArrayList, ArrayList)}
 	 * function. As a result of the method of board search, incomplete cities
 	 * must be recorded during the method and are removed at the end, before the
 	 * final list is returned.
 	 * 
 	 * @return A list of sets of board positions belonging to separate completed
 	 *         cities.
 	 */
 	private ArrayList<HashSet<BoardPosition>> getCompletedCities() {
 
 		// Each HashSet in the ArrayList denotes a city; a collection of
 		// BoardPosition objects which indicate the tile locations which
 		// comprise the city.
 		ArrayList<HashSet<BoardPosition>> cities;
 		cities = new ArrayList<HashSet<BoardPosition>>();
 
 		// Each city which is found to be incomplete is recorded by having an
 		// entry put in incompleteCities specifying the index of the incomplete
 		// city in the cities array.
 		ArrayList<Integer> incompleteCities = new ArrayList<Integer>();
 
 		boolean newCity = true;
 		HashSet<BoardPosition> searched;
 		HashSet<BoardPosition> toSearch;
 
 		// Run through the whole map; all board tiles and all tile positions.
 		// When we find a city tile type we check if the city it belongs to is
 		// already recorded. If not then we call the recursive search to record
 		// the city.
 		for (int i = 0; i < gameBoard.length; i++) {
 			for (int j = 0; j < gameBoard[i].length; j++) {
 
 				Tile tile = gameBoard[i][j];
 
 				if (tile == null) {
 					continue;
 				}
 
 				for (int k = 0; k < Tile.tileSize; k++) {
 					for (int l = 0; l < Tile.tileSize; l++) {
 						if (tile.getTileType(l, k) == TileType.CITY) {
 
 							// Reset flags for testing the next tile position.
 							newCity = true;
 
 							// Get the current tile board position.
 							BoardPosition currentTile;
 							currentTile = new BoardPosition(j, i, l, k);
 
 							for (int m = 0; m < cities.size(); m++) {
 								if (cities.get(m).contains(currentTile)) {
 									newCity = false;
 								}
 							}
 
 							if (newCity) {
 								searched = new HashSet<BoardPosition>();
 								toSearch = new HashSet<BoardPosition>();
 
 								toSearch.add(currentTile);
 
 								getCompletedCitiesRecursive(searched, toSearch,
 										cities, incompleteCities);
 							}
 						}
 					}
 				}
 			}
 		}
 
 		// After the search is completed we will remove the incomplete cities
 		// from the list as they are not used in field scoring.
 		for (int i = incompleteCities.size() - 1; i >= 0; i--) {
 			int cityToRemove = incompleteCities.get(i);
 			cities.remove(cityToRemove);
 		}
 
 		return cities;
 	}
 
 	/**
 	 * This function uses recursion to find all board positions of a city. This
 	 * function is similar to
 	 * {@link #genericScoreRecursive(HashSet, HashSet, Object[])} and
 	 * {@link #isNewFeatureRecursive(HashSet, HashSet)} in the way that it
 	 * searches the game board feature. It recursively records a city by finding
 	 * the neighbor tile positions of an initial position (passed in in
 	 * toSearch). If a neighboring tile is found to be null during the search,
 	 * this is also recorded and used by the calling function (which indicates
 	 * an incomplete city). When the search has completed the set of coordinates
 	 * is added to the list of cities.
 	 * 
 	 * @param searched
 	 *            A set of previously searched game board coordinates.
 	 * @param toSearch
 	 *            A set of game board coordinates to be searched.
 	 * @param cities
 	 *            A list of sets of coordinates which comprise a city.
 	 * @param incompleteCities
 	 *            A list of integers corresponding to any cities in the 'cities'
 	 *            parameter which are incomplete.
 	 */
 	private void getCompletedCitiesRecursive(HashSet<BoardPosition> searched,
 			HashSet<BoardPosition> toSearch,
 			ArrayList<HashSet<BoardPosition>> cities,
 			ArrayList<Integer> incompleteCities) {
 
 		// Take a position from the toSearch map to search.
 		Iterator<BoardPosition> toSearchIterator = toSearch.iterator();
 		BoardPosition boardPosition = null;
 
 		if (toSearchIterator.hasNext()) {
 			boardPosition = toSearchIterator.next();
 		} else {
 			return;
 		}
 
 		int xBoard = boardPosition.xBoard;
 		int yBoard = boardPosition.yBoard;
 		int xTile = boardPosition.xTile;
 		int yTile = boardPosition.yTile;
 
 		Tile currentTile = gameBoard[yBoard][xBoard];
 
 		toSearch.remove(boardPosition);
 		searched.add(boardPosition);
 
 		TileType currentTileType = currentTile.getTileType(xTile, yTile);
 
 		// Add neighbors to an array. And run through adding each to toSearch.
 		BoardPosition[] neighborTiles = getTileNeighbors(xBoard, yBoard, xTile,
 				yTile);
 
 		// Add valid neighbors of position to toSearch map.
 		// A neighbor is valid if it is in neither map,
 		// and is of the same tile type.
 		for (int i = 0; i < neighborTiles.length; i++) {
 			// Check the tile is not null.
 			Tile tile = gameBoard[neighborTiles[i].yBoard][neighborTiles[i].xBoard];
 
 			if (tile != null) {
 
 				// Check that the tile has the same tile type.
 				TileType tileType = tile.getTileType(neighborTiles[i].xTile,
 						neighborTiles[i].yTile);
 
 				if (tileType == currentTileType) {
 
 					BoardPosition toAdd = new BoardPosition(
 							neighborTiles[i].xBoard, neighborTiles[i].yBoard,
 							neighborTiles[i].xTile, neighborTiles[i].yTile);
 
 					// Check the tile is not already in searched or toSearch.
 					if (!toSearch.contains(toAdd) && !searched.contains(toAdd)) {
 						toSearch.add(toAdd);
 					}
 				}
 
 			} else {
 				// If we find a null tile, add the city's index to
 				// incompleteCities.
 				// Do not subtract 1, as the city has yet to be added;
 				// Once the city is added to cities, the index will be correct.
 				if (!incompleteCities.contains(cities.size())) {
 					incompleteCities.add(cities.size());
 				}
 			}
 		}
 
 		// If toSearch is empty, the add searched to cities. Otherwise recurse.
 		if (!toSearch.isEmpty()) {
 			getCompletedCitiesRecursive(searched, toSearch, cities,
 					incompleteCities);
 		} else {
 			cities.add(searched);
 		}
 	}
 
 	/**
 	 * This function searches through the fields to find all adjacent cities.
 	 * Given board coordinates to start with, this function will search out from
 	 * the position recording any adjacent cities along the way. It also tracks
 	 * any meeples which are on the same feature for scoring by the calling
 	 * function.
 	 * 
 	 * @param searched
 	 *            A set of previously searched game board coordinates.
 	 * @param toSearch
 	 *            A set of game board coordinates to be searched.
 	 * @param meeplesOnFeature
 	 *            A list of meeples which are found on the feature.
 	 * @param allCities
 	 *            A list of all completed cities in the game (sets of
 	 *            coordinates).
 	 * @param adjacentCities
 	 *            A list of the adjacent completed cities to the feature being
 	 *            searched (sets of coordinates).
 	 * @param featureProperties
 	 *            An object array used to hold the number of cities adjacent to
 	 *            the searched feature.
 	 */
 	private void fieldScoreRecursive(HashSet<BoardPosition> searched,
 			HashSet<BoardPosition> toSearch,
 			ArrayList<Meeple> meeplesOnFeature,
 			ArrayList<HashSet<BoardPosition>> allCities,
 			ArrayList<HashSet<BoardPosition>> adjacentCities) {
 
 		// Take a position from the toSearch map to search.
 		Iterator<BoardPosition> toSearchIterator = toSearch.iterator();
 		BoardPosition boardPosition = null;
 
 		if (toSearchIterator.hasNext()) {
 			boardPosition = toSearchIterator.next();
 		} else {
 			return;
 		}
 
 		int xBoard = boardPosition.xBoard;
 		int yBoard = boardPosition.yBoard;
 		int xTile = boardPosition.xTile;
 		int yTile = boardPosition.yTile;
 
 		// Search the position & add it to searched map.
 		Meeple meeple = getMeeple(xBoard, yBoard, xTile, yTile);
 
 		if (meeple != null) {
 			// Add the meeple on the tile to our list.
 			meeplesOnFeature.add(meeple);
 		}
 
 		toSearch.remove(boardPosition);
 		searched.add(boardPosition);
 
 		// Get the neighbor tile positions.
 		BoardPosition[] neighborTiles = getTileNeighbors(xBoard, yBoard, xTile,
 				yTile);
 
 		// Add valid neighbors of position to toSearch map.
 		// A neighbor is valid if it is in neither map, and is a field.
 		for (int i = 0; i < neighborTiles.length; i++) {
 
 			// Check the tile is not null.
 			Tile tile = gameBoard[neighborTiles[i].yBoard][neighborTiles[i].xBoard];
 
 			if (tile != null) {
 
 				// Add the tile to the toSearch set.
 				BoardPosition toAdd = new BoardPosition(
 						neighborTiles[i].xBoard, neighborTiles[i].yBoard,
 						neighborTiles[i].xTile, neighborTiles[i].yTile);
 
 				// Check that the tile has the same tile type.
 				TileType tileType = tile.getTileType(neighborTiles[i].xTile,
 						neighborTiles[i].yTile);
 
 				// Add the tile to be searched if it is also a field.
 				if (tileType == TileType.FIELD) {
 
 					// Check the tile is not already in searched or toSearch.
 					if (!toSearch.contains(toAdd) && !searched.contains(toAdd)) {
 						toSearch.add(toAdd);
 					}
 
 				} else if (tileType == TileType.CITY) {
 
 					// If the neighboring tile is a city tile, find the city
 					// that it belongs to and add that city to our adjacent
 					// cities (only if not already in the adjacent cities).
 					boolean newCity = true;
 
 					for (int j = 0; j < adjacentCities.size(); j++) {
 						if (adjacentCities.get(j).contains(toAdd)) {
 							newCity = false;
 						}
 					}
 
 					if (newCity) {
 						for (int j = 0; j < allCities.size(); j++) {
 							if (allCities.get(j).contains(toAdd)) {
 								adjacentCities.add(allCities.get(j));
 							}
 						}
 					}
 				}
 			}
 		}
 
 		// If there are still tiles in toSearch then continue searching.
 		if (!toSearch.isEmpty()) {
 			fieldScoreRecursive(searched, toSearch, meeplesOnFeature,
 					allCities, adjacentCities);
 		}
 	}
 
 	/**
 	 * This function gets neighbor tile positions from a given position. It
 	 * compensates for tile positions which are on the edge of a given tile by
 	 * testing for these cases and will overflow to positions on the neighboring
 	 * tiles if needed.
 	 * 
 	 * @param xBoard
 	 *            An x position on the board.
 	 * @param yBoard
 	 *            An y position on the board.
 	 * @param xTile
 	 *            An x tile position on a board tile.
 	 * @param yTile
 	 *            An y tile position on a board tile.
 	 * 
 	 * @return An array of BoardPosition objects containing the coordinates of
 	 *         all neighboring tile positions (not tiles).
 	 */
 	private BoardPosition[] getTileNeighbors(int xBoard, int yBoard, int xTile,
 			int yTile) {
 
 		int[] nStr = { xBoard, yBoard, xTile, (yTile - 1) };
 		int[] eStr = { xBoard, yBoard, (xTile + 1), yTile };
 		int[] sStr = { xBoard, yBoard, xTile, (yTile + 1) };
 		int[] wStr = { xBoard, yBoard, (xTile - 1), yTile };
 
 		// Catch any across-tiles conditions.
 		// If yTile == 6 then we set it to 0 and increment yBoard.
 		// If yTile == 0 then we set it to 6 and decrement yBoard.
 		// If xTile == 6 then we set it to 0 and increment xBoard.
 		// If xTile == 0 then we set it to 6 and decrement xBoard.
 		// Only for those which we are adding or subtracting from.
 		if (yTile == 6) {
 			sStr[3] = 0;
 			sStr[1]++;
 		}
 
 		if (yTile == 0) {
 			nStr[3] = 6;
 			nStr[1]--;
 		}
 
 		if (xTile == 6) {
 			eStr[2] = 0;
 			eStr[0]++;
 		}
 
 		if (xTile == 0) {
 			wStr[2] = 6;
 			wStr[0]--;
 		}
 
 		BoardPosition nBoardPosition;
 		BoardPosition eBoardPosition;
 		BoardPosition sBoardPosition;
 		BoardPosition wBoardPosition;
 
 		nBoardPosition = new BoardPosition(nStr[0], nStr[1], nStr[2], nStr[3]);
 		eBoardPosition = new BoardPosition(eStr[0], eStr[1], eStr[2], eStr[3]);
 		sBoardPosition = new BoardPosition(sStr[0], sStr[1], sStr[2], sStr[3]);
 		wBoardPosition = new BoardPosition(wStr[0], wStr[1], wStr[2], wStr[3]);
 
 		BoardPosition[] neighborTiles = { nBoardPosition, eBoardPosition,
 				sBoardPosition, wBoardPosition };
 
 		return neighborTiles;
 	}
 
 	// Used for the ui; return the size of the board array.
 	public int getWidth() {
 		return gameBoard[0].length;
 	}
 
 	public int getHeight() {
 		return gameBoard.length;
 	}
 }
