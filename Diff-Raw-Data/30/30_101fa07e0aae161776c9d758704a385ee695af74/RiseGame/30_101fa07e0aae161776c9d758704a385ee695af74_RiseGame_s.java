 package com.matthewtole.androidrise.lib;
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 import android.util.Log;
 
 public class RiseGame {
 
 	public static final int BLUE = 0;
 	public static final int RED = 1;
 
 	private static final int TURN_STATE_NOTHING = 1;
 	private static final int TURN_STATE_WORKER_SELECTED = 2;
 	private static final int TURN_STATE_SACRIFICING = 3;
 
 	public static final int VICTORY_ELIMINATION = 1;
 	public static final int VICTORY_TOWERS = 1;
 
 	public static final int GAME_STATE_PLAYING = 1;
 	public static final int GAME_STATE_DONE = 2;
 
 	private static final int TILE_COUNT = 60;
 	private static final int WORKER_COUNT = 30;
 
 	private Stack<RiseTile[][]> oldBoards;
 
 	private RiseTile[][] board;
 	private int turn;
 	private int turnState;
 	private int moveCounter;
 	private int[] availableWorkers;
 	private int availableTiles;
 	private int[] towerCounts;
 	private RiseTile selectedTile = null;
 	private RiseTile[] sacrifices;
 	private ArrayList<RiseTile> towersProcessed;
 	private int gameState = 0;
 	private int gameWinner = 0;
 	private int victoryType = 0;
 
 	public RiseGame() {
 
 		this.oldBoards = new Stack<RiseTile[][]>();
 
 		this.board = new RiseTile[TILE_COUNT][TILE_COUNT];
 		for (int x = 0; x < TILE_COUNT; x += 1) {
 			for (int y = 0; y < TILE_COUNT; y += 1) {
 				this.board[x][y] = new RiseTile(x, y);
 			}
 		}
 
 		this.availableWorkers = new int[2];
 		this.towerCounts = new int[2];
 	}
 
 	public void setup(char[][] layout) {
 
 		this.turn = RiseGame.RED;
 		this.availableTiles = TILE_COUNT;
 		this.availableWorkers[RiseGame.RED] = WORKER_COUNT - 1;
 		this.availableWorkers[RiseGame.BLUE] = WORKER_COUNT - 1;
 		this.towerCounts[RiseGame.RED] = 0;
 		this.towerCounts[RiseGame.BLUE] = 0;
 		this.sacrifices = new RiseTile[2];
 		this.towersProcessed = new ArrayList<RiseTile>();
 		this.moveCounter = 1;
 		this.turnState = TURN_STATE_NOTHING;
 
 		for (int x = 0; x < TILE_COUNT; x += 1) {
 			for (int y = 0; y < TILE_COUNT; y += 1) {
 				this.board[x][y].clear();
 			}
 		}
 
 		this.buildLayout(layout);
 	}
 
 	private void buildLayout(char[][] layout) {
 		int layoutOffsetX = (TILE_COUNT / 2) - layout.length / 2;
 		int layoutOffsetY = (TILE_COUNT / 2) - layout[0].length / 2;
 		if (layoutOffsetX % 2 == 1) {
 			layoutOffsetX -= 1;
 		}
 		if (layoutOffsetY % 2 == 1) {
 			layoutOffsetY -= 1;
 		}
 
 		for (int x = 0; x < layout.length; x += 1) {
 			for (int y = 0; y < layout[x].length; y += 1) {
 				RiseTile tile = this.board[layoutOffsetX + x][layoutOffsetY + y];
 				switch (layout[x][y]) {
 				case 'B':
 					tile.setWorker(RiseGame.BLUE);
 					break;
 				case 'R':
 					tile.setWorker(RiseGame.RED);
 					break;
 				case 'O':
 					tile.setTile();
 					break;
 				}
 			}
 		}
 	}
 
 	public boolean hasPiece(int x, int y) {
 		if (!validLocation(x, y)) {
 			return false;
 		}
 		return this.getTile(x, y).isPiece();
 	}
 
 	private boolean validLocation(int x, int y) {
 		if (x < 0 || y < 0) {
 			return false;
 		}
 		if (x >= TILE_COUNT || y >= TILE_COUNT) {
 			return false;
 		}
 		return true;
 	}
 
 	public int pieceColour(int x, int y) {
 		if (!validLocation(x, y)) {
 			return -1;
 		}
 		if (!hasPiece(x, y)) {
 			return -1;
 		}
 		return this.getTile(x, y).pieceColour();
 	}
 
 	public int towerHeight(int x, int y) {
 		if (!validLocation(x, y) || !hasPiece(x, y) || !hasTower(x, y)) {
 			return 0;
 		}
 		return this.getTile(x, y).towerHeight();
 	}
 
 	private RiseTile getTile(int x, int y) {
 		if (!validLocation(x, y)) {
 			return null;
 		}
 		return this.board[x][y];
 	}
 
 	public int towerColour(int x, int y) {
 		if (!validLocation(x, y) || !hasPiece(x, y) || !hasTower(x, y)) {
 			return 0;
 		}
 		return this.pieceColour(x, y);
 	}
 
 	public boolean hasTower(int x, int y) {
 		if (!validLocation(x, y) || !hasPiece(x, y)) {
 			return false;
 		}
 		return this.getTile(x, y).isTower();
 	}
 
 	public boolean hasTile(int x, int y) {
 		if (!validLocation(x, y)) {
 			return false;
 		}
 		return !this.getTile(x, y).isBlank();
 	}
 
 	public static String colourName(int pieceColour) {
 		return pieceColour == BLUE ? "blue" : "red";
 	}
 
 	public boolean undoLastAction() {
 		if (this.oldBoards.empty()) {
 			Log.d("RiseGame", "There is nothing to undo!");
 			return false;
 		}
 		Log.d("RiseGame", "An undo has just occurred.");
 		this.board = this.oldBoards.pop();
 		return true;
 	}
 
 	public RiseTile[][] copyBoard() {
 		RiseTile[][] boardCopy = new RiseTile[this.board.length][this.board.length];
 		for (int x = 0; x < TILE_COUNT; x += 1) {
 			for (int y = 0; y < TILE_COUNT; y += 1) {
 				boardCopy[x][y] = this.board[x][y].clone();
 			}
 		}
 		return boardCopy;
 	}
 
 	public boolean doAction(int x, int y, int player) {
 
 		this.oldBoards.push(copyBoard());
 
 		if (this.turn != player || !this.validLocation(x, y)) {
 			return false;
 		}
 
 		switch (this.turnState) {
 		case RiseGame.TURN_STATE_NOTHING:
			return doNormalAction(x, y, player);
 
 		case RiseGame.TURN_STATE_WORKER_SELECTED:
 			return doActionSelected(x, y, player);
 
 		case TURN_STATE_SACRIFICING:
 			return doActionSacrifice(x, y, player);
 		}
 
 		return false;
 	}
 
 	private boolean doActionSacrifice(int x, int y, int player) {
 		RiseTile theTile = this.getTile(x, y);
 
 		// SACRIFICE TO PLACE ANYWHERE
		if (theTile.isTile()) {
 			this.sacrifices[0].setTile();
 			this.sacrifices[1].setTile();
 			this.availableWorkers[player] += 2;
 			theTile.setWorker(player);
			this.availableWorkers[player] -= 1;
 			this.moveMade(player);
 			return true;
 		}
 		// SACRIFICE TO REMOVE OTHER PLAYER
		if (theTile.isWorker(RiseGame.otherPlayer(player))) {
 			this.sacrifices[0].setTile();
 			this.sacrifices[1].setTile();
 			this.availableWorkers[player] += 2;
 			theTile.setTile();
 			this.availableWorkers[RiseGame.otherPlayer(player)] += 1;
 			this.moveMade(player);
 			return true;
 		}
 		return false;
 	}
 
 	private boolean doActionSelected(int x, int y, int player) {
 		RiseTile theTile = this.getTile(x, y);
 		// UNSELECT WORKER
 		if (theTile == this.selectedTile) {
 			this.turnState = RiseGame.TURN_STATE_NOTHING;
 			this.selectedTile.unselect();
 			this.selectedTile = null;
 			return true;
 		}
 		// GO INTO SACRIFICE
		if (theTile.isWorker(player)
				&& (WORKER_COUNT - this.availableWorkers[player] >= 2)) {
 			this.turnState = RiseGame.TURN_STATE_SACRIFICING;
 			theTile.select();
 			this.sacrifices[0] = this.selectedTile;
 			this.sacrifices[1] = theTile;
 			this.selectedTile = null;
 		}
 		// MOVE WORKER
 		if (theTile.isTile() && this.areNeighbours(theTile, this.selectedTile)) {
 			theTile.setWorker(player);
 			this.selectedTile.setTile();
 			this.selectedTile.unselect();
 			this.selectedTile = null;
 			this.moveMade(player);
 			return true;
 		}
 		// JUMP WORKER
 		if (theTile.isTile()) {
 			RiseTile[] neighbours = this.getNeighbours(theTile);
 			for (int n = 0; n < neighbours.length; n += 1) {
 				RiseTile[] neighbours2 = this.getNeighbours(neighbours[n]);
 				if (neighbours[n].isWorker(RiseGame.otherPlayer(player))
 						&& neighbours2[n] == this.selectedTile) {
 					theTile.setWorker(player);
 					neighbours[n].setTile();
 					this.availableWorkers[RiseGame.otherPlayer(player)] += 1;
 					this.selectedTile.setTile();
 					this.selectedTile.unselect();
 					this.selectedTile = null;
 					this.moveMade(player);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
	private boolean doNormalAction(int x, int y, int player) {
 		RiseTile theTile = this.getTile(x, y);
 
 		// ADD TILE
 		if (theTile.isBlank() && this.availableTiles > 0) {
 			if (this.hasNeighbour(x, y)) {
 				theTile.setTile();
 				this.availableTiles -= 1;
 				this.moveMade(player);
 				return true;
 			} else {
 				return false;
 			}
 		}
 		// ADD WORKER
 		if (theTile.isTile() & this.availableWorkers[player] > 0) {
 			if (this.hasNeighbourWorker(x, y, player)) {
 				theTile.setWorker(player);
 				this.availableWorkers[player] -= 1;
 				this.moveMade(player);
 				return true;
 			} else {
 				return false;
 			}
 		}
 		// REMOVE TOWER
 		if (theTile.isTower(player)) {
 			if (theTile.demolishTower()) {
 				this.towerCounts[player] -= 1;
 				this.moveMade(player);
 				return true;
 			} else {
 				return false;
 			}
 		}
 		// SELECT WORKER
 		if (theTile.isWorker(player)) {
 			theTile.select();
 			this.turnState = RiseGame.TURN_STATE_WORKER_SELECTED;
 			this.selectedTile = theTile;
 			return true;
 		}
 
 		return false;
 	}
 
 	private static int otherPlayer(int player) {
 		return player == RiseGame.BLUE ? RiseGame.RED : RiseGame.BLUE;
 	}
 
 	private boolean areNeighbours(RiseTile tile1, RiseTile tile2) {
 		RiseTile[] neighbours = this.getNeighbours(tile1);
 		for (int n = 0; n < neighbours.length; n += 1) {
 			if (neighbours[n] == tile2) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private RiseTile[] getNeighbours(RiseTile tile) {
 		return getNeighbours(tile.getX(), tile.getY());
 	}
 
 	private void moveMade(int player) {
 
 		for (int x = 0; x < 60; x += 1) {
 			for (int y = 0; y < 60; y += 1) {
 				RiseTile thisTile = this.getTile(x, y);
 				if (this.towersProcessed.contains(thisTile)) {
 					continue;
 				}
 				if (thisTile.isTower(RiseGame.otherPlayer(player))
 						&& this.tileSurrounded(thisTile, player)) {
 					thisTile.demolishTower();
 					this.towersProcessed.add(thisTile);
 					this.towerCounts[RiseGame.otherPlayer(player)] -= 1;
 				}
 				if (thisTile.isTile()) {
 					if (this.tileSurrounded(thisTile, player)) {
 						thisTile.setTower(player, 0);
 					}
 				}
 				if (thisTile.isTower(player)
 						&& this.tileSurrounded(thisTile, player)) {
 					thisTile.buildTower();
 					this.towersProcessed.add(thisTile);
 					this.towerCounts[player] += 1;
 					continue;
 				}
 
 			}
 		}
 
 		this.turnState = RiseGame.TURN_STATE_NOTHING;
 		this.moveCounter -= 1;
 		if (this.moveCounter <= 0) {
 			this.endTurn();
 		}
 		else if (this.checkVictory()) {
 			return;
 		}
 	}
 
 	private void endTurn() {
 
 		this.turn = (this.turn == RiseGame.BLUE) ? RiseGame.RED : RiseGame.BLUE;
 		this.moveCounter = 2;
 		this.oldBoards.clear();
 
 		this.towersProcessed = new ArrayList<RiseTile>();
 		for (int x = 0; x < 60; x += 1) {
 			for (int y = 0; y < 60; y += 1) {
 				RiseTile thisTile = this.getTile(x, y);
 				if (this.towersProcessed.contains(thisTile)) {
 					continue;
 				}
 				if (thisTile.isTower(RiseGame.otherPlayer(turn))
 						&& this.tileSurrounded(thisTile, turn)) {
 					thisTile.demolishTower();
 					this.towersProcessed.add(thisTile);
 					this.towerCounts[RiseGame.otherPlayer(turn)] -= 1;
 				}
 				if (thisTile.isTile()) {
 					if (this.tileSurrounded(thisTile, turn)) {
 						thisTile.setTower(turn, 0);
 					}
 				}
 				if (thisTile.isTower(this.turn)
 						&& this.tileSurrounded(thisTile, this.turn)) {
 					thisTile.buildTower();
 					this.towersProcessed.add(thisTile);
 					this.towerCounts[this.turn] += 1;
 					continue;
 				}
 			}
 		}
 
 		if (this.checkVictory()) {
 			return;
 		}
 	}
 
 	private boolean checkVictory() {
 		
 		if (this.availableWorkers[RiseGame.otherPlayer(turn)] == WORKER_COUNT) {
 			this.gameWon(this.turn, RiseGame.VICTORY_ELIMINATION);
 			return true;			
 		}
 		if (this.availableWorkers[this.turn] == WORKER_COUNT) {
 			this.gameWon(RiseGame.otherPlayer(this.turn), RiseGame.VICTORY_ELIMINATION);
 			return true;			
 		}
 		return false;
 	}
 
 	private void gameWon(int winner, int victoryType) {
 		this.gameState = RiseGame.GAME_STATE_DONE;
 		this.gameWinner = winner;
 		this.victoryType = victoryType;
 	}
 
 	private boolean tileSurrounded(RiseTile tile, int colour) {
 		RiseTile[] neighbours = getNeighbours(tile);
 		for (int n = 0; n < neighbours.length; n += 1) {
 			if (!neighbours[n].isWorker(colour)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private RiseTile[] getNeighbours(int x, int y) {
 		RiseTile[] neighbours = new RiseTile[6];
 
 		if (y % 2 == 1) {
 			neighbours[0] = this.getTile(x - 1, y);
 			neighbours[1] = this.getTile(x, y - 1);
 			neighbours[2] = this.getTile(x + 1, y - 1);
 			neighbours[3] = this.getTile(x + 1, y);
 			neighbours[4] = this.getTile(x + 1, y + 1);
 			neighbours[5] = this.getTile(x, y + 1);
 		} else {
 			neighbours[0] = this.getTile(x - 1, y);
 			neighbours[1] = this.getTile(x - 1, y - 1);
 			neighbours[2] = this.getTile(x, y - 1);
 			neighbours[3] = this.getTile(x + 1, y);
 			neighbours[4] = this.getTile(x, y + 1);
 			neighbours[5] = this.getTile(x - 1, y + 1);
 		}
 
 		return neighbours;
 
 	}
 
 	private boolean hasNeighbourWorker(int x, int y, int player) {
 		RiseTile[] neighbours = this.getNeighbours(x, y);
 		for (int n = 0; n < neighbours.length; n += 1) {
 			if (neighbours[n] != null && neighbours[n].isWorker(player)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean hasNeighbour(int x, int y) {
 		RiseTile[] neighbours = this.getNeighbours(x, y);
 		for (int n = 0; n < neighbours.length; n += 1) {
 			if (neighbours[n] != null && neighbours[n].isNotBlank()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public int getCurrentPlayer() {
 		return this.turn;
 	}
 
 	public boolean isSelectedWorker(int x, int y) {
 		return this.getTile(x, y).isSelected();
 	}
 
 	public int getMovesLeft() {
 		return this.moveCounter;
 	}
 
 	public int getState() {
 		return this.gameState;
 	}
 
 }
