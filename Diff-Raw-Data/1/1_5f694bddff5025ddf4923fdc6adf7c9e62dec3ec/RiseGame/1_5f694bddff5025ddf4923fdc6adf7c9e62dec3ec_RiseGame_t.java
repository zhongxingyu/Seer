 package com.matthewtole.androidrise.lib;
 
 import java.util.ArrayList;
 import java.util.EnumMap;
 
 import android.util.Log;
 
 import com.matthewtole.androidrise.lib.enums.GamePlayer;
 import com.matthewtole.androidrise.lib.enums.TurnState;
 import com.matthewtole.androidrise.lib.enums.UpdateType;
 
 public class RiseGame {
 
 	private static final String TAG = RiseGame.class.getSimpleName();
 
 	private static final int TILE_COUNT = 60;
 	private static final int WORKER_COUNT = 30;
 
 	private RiseTile[][] board;
 	private GamePlayer turn = GamePlayer.UNKNOWN;
 	private TurnState turnState;
 	private int moveCounter;
 
 	private EnumMap<GamePlayer, Integer> availableWorkers;
 	private EnumMap<GamePlayer, Integer> towerCounts;
 
 	private int availableTiles;
 	private RiseTile selectedTile = null;
 	private RiseTile[] sacrifices;
 
 	private ArrayList<RiseTile> towersProcessed;
 
 	private SimpleQueue<GameUpdate> updateQueue;
 	private String updateMessage = "";
 
 	private boolean updateQueueLockout = false;
 
 	public RiseGame() {
 
 		this.updateQueue = new SimpleQueue<GameUpdate>();
 
 		this.board = new RiseTile[TILE_COUNT][TILE_COUNT];
 		for (int x = 0; x < TILE_COUNT; x += 1) {
 			for (int y = 0; y < TILE_COUNT; y += 1) {
 				this.board[x][y] = new RiseTile(x, y);
 			}
 		}
 
 		this.availableWorkers = new EnumMap<GamePlayer, Integer>(
 				GamePlayer.class);
 		this.towerCounts = new EnumMap<GamePlayer, Integer>(GamePlayer.class);
 	}
 
 	public void setup(char[][] layout) {
 
 		this.turn = GamePlayer.RED;
 		this.availableTiles = TILE_COUNT;
 		this.availableWorkers.put(GamePlayer.RED, WORKER_COUNT - 1);
 		this.availableWorkers.put(GamePlayer.BLUE, WORKER_COUNT - 1);
 		this.towerCounts.put(GamePlayer.RED, 0);
 		this.towerCounts.put(GamePlayer.BLUE, 0);
 		this.sacrifices = new RiseTile[2];
 		this.towersProcessed = new ArrayList<RiseTile>();
 		this.moveCounter = 1;
 		this.turnState = TurnState.NOTHING;
 
 		for (int x = 0; x < TILE_COUNT; x += 1) {
 			for (int y = 0; y < TILE_COUNT; y += 1) {
 				this.board[x][y].clear();
 			}
 		}
 
 		this.buildLayout(layout);
 	}
 
 	public boolean doAction(int x, int y, GamePlayer player) {
 
 		if (this.turn != player) {
 			this.setMessage("Not your turn!");
 			return false;
 		}
 
 		if (!this.validLocation(x, y)) {
 			this.setMessage("Invalid location");
 			return false;
 		}
 
 		switch (this.turnState) {
 		case NOTHING:
 			return doActionNothing(x, y, player);
 
 		case SELECTED:
 			return doActionSelected(x, y, player);
 
 		case SACRIFICING:
 			return doActionSacrifice(x, y, player);
 		}
 
 		this.setMessage("Nothing to do here");
 		return false;
 	}
 
 	private void setMessage(String message) {
 		this.updateMessage = message;
 	}
 	
 	public String getMessage() {
 		return this.updateMessage;
 	}
 
 	public GamePlayer getCurrentPlayer() {
 		return this.turn;
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
 					tile.setWorker(GamePlayer.BLUE);
 					break;
 				case 'R':
 					tile.setWorker(GamePlayer.RED);
 					break;
 				case 'O':
 					tile.setTile();
 					break;
 				}
 			}
 		}
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
 
 	private RiseTile getTile(int x, int y) {
 		if (!validLocation(x, y)) {
 			return null;
 		}
 		return this.board[x][y];
 	}
 
 	private boolean doActionSacrifice(int x, int y, GamePlayer player) {
 		RiseTile theTile = this.getTile(x, y);
 
 		// SACRIFICE TO PLACE ANYWHERE
 		if (theTile.isTile()
 				&& (WORKER_COUNT - this.availableWorkers.get(player) > 2)) {
 			this.sacrifices[0].setTile();
 			this.sacrifices[1].setTile();
 			this.availableWorkers.put(player,
 					this.availableWorkers.get(player) + 2);
 			theTile.setWorker(player);
 			this.availableWorkers.put(player,
 					this.availableWorkers.get(player) - 1);
 			this.moveMade(player);
 			this.addUpdate(new GameUpdate(UpdateType.SACRIFICE_ADD,
 					new GridLocation(x, y), new GridLocation(this.sacrifices[0]
 							.getX(), this.sacrifices[0].getY()),
 					new GridLocation(this.sacrifices[1].getX(),
 							this.sacrifices[1].getY())));
 			return true;
 		}
 		// SACRIFICE TO REMOVE OTHER PLAYER
 		if (theTile.isWorker(RiseGame.otherPlayer(player))
 				&& (WORKER_COUNT - this.availableWorkers.get(player) > 2)) {
 			this.sacrifices[0].setTile();
 			this.sacrifices[1].setTile();
 			this.availableWorkers.put(player,
 					this.availableWorkers.get(player) + 2);
 			theTile.setTile();
 			this.availableWorkers
 					.put(RiseGame.otherPlayer(player), this.availableWorkers
 							.get(RiseGame.otherPlayer(player)) + 1);
 			this.moveMade(player);
 			this.addUpdate(new GameUpdate(UpdateType.SACRIFICE_REMOVE,
 					new GridLocation(x, y), new GridLocation(this.sacrifices[0]
 							.getX(), this.sacrifices[0].getY()),
 					new GridLocation(this.sacrifices[1].getX(),
 							this.sacrifices[1].getY())));
 			return true;
 		}
 		// UNSELECT THIS TILE
 		if (theTile == this.sacrifices[0] || theTile == this.sacrifices[1]) {
 			this.turnState = TurnState.SELECTED;
 			theTile.unselect();
 			if (theTile == this.sacrifices[0]) {
 				this.selectedTile = this.sacrifices[1];
 			} else {
 				this.selectedTile = this.sacrifices[0];
 			}
 			this.addUpdate(new GameUpdate(UpdateType.WORKER_UNSELECTED,
 					new GridLocation(x, y)));
 			return true;
 
 		}
 
 		this.setMessage("You are in sacrifice mode, you cannot do that!");
 		return false;
 	}
 
 	private boolean doActionSelected(int x, int y, GamePlayer player) {
 		RiseTile theTile = this.getTile(x, y);
 
 		// UNSELECT WORKER
 		if (theTile == this.selectedTile) {
 			this.turnState = TurnState.NOTHING;
 			this.selectedTile.unselect();
 			this.selectedTile = null;
 			this.addUpdate(new GameUpdate(UpdateType.WORKER_UNSELECTED,
 					new GridLocation(x, y)));
 			return true;
 		}
 		// GO INTO SACRIFICE
 		if (theTile.isWorker(player)) {
 			this.turnState = TurnState.SACRIFICING;
 			theTile.select();
 			this.sacrifices[0] = this.selectedTile;
 			this.sacrifices[1] = theTile;
 			this.selectedTile = null;
 			this.addUpdate(new GameUpdate(UpdateType.WORKER_SELECTED,
 					new GridLocation(x, y)));
 			return true;
 		}
 		// MOVE WORKER
 		if (theTile.isTile() && this.areNeighbours(theTile, this.selectedTile)) {
 			theTile.setWorker(player);
 			this.selectedTile.setTile();
 			this.selectedTile.unselect();
 			GridLocation tmpLocation = new GridLocation(
 					this.selectedTile.getX(), this.selectedTile.getY());
 			this.selectedTile = null;
 			this.moveMade(player);
 			this.addUpdate(new GameUpdate(UpdateType.WORKER_MOVED, tmpLocation,
 					new GridLocation(x, y)));
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
 
 					this.availableWorkers.put(RiseGame.otherPlayer(player),
 							this.availableWorkers.get(RiseGame
 									.otherPlayer(player)) + 1);
 					this.selectedTile.setTile();
 					this.selectedTile.unselect();
 					GridLocation tmp = new GridLocation(
 							this.selectedTile.getX(), this.selectedTile.getY());
 					this.selectedTile = null;
 					this.moveMade(player);
 					this.addUpdate(new GameUpdate(UpdateType.WORKER_JUMP, tmp,
 							new GridLocation(x, y), new GridLocation(
 									neighbours[n].getX(), neighbours[n].getY())));
 					return true;
 				}
 			}
 		}
 
 		this.setMessage("You are in selected mode, you cannot do that!");
 		return false;
 	}
 
 	private boolean doActionNothing(int x, int y, GamePlayer player) {
 		RiseTile theTile = this.getTile(x, y);
 
 		// ADD TILE
 		if (theTile.isBlank() && this.availableTiles > 0) {
 			if (this.hasNeighbour(x, y)) {
 				theTile.setTile();
 				this.availableTiles -= 1;
 				this.moveMade(player);
 				this.addUpdate(new GameUpdate(UpdateType.TILE_ADDED,
 						new GridLocation(x, y)));
 				return true;
 			} else {
 				this.setMessage("Cannot add a tile here.");
 				return false;
 			}
 		}
 		// ADD WORKER
 		if (theTile.isTile() & this.availableWorkers.get(player) > 0) {
 			if (this.hasNeighbourWorker(x, y, player)) {
 				theTile.setWorker(player);
 				this.availableWorkers.put(player,
 						this.availableWorkers.get(player) - 1);
 				this.moveMade(player);
 				this.addUpdate(new GameUpdate(UpdateType.WORKER_ADDED,
 						new GridLocation(x, y), player));
 				return false;
 			} else {
 				this.setMessage("Cannot add a worker here.");
 				return false;
 			}
 		}
 		// REMOVE TOWER
 		if (theTile.isTower(player)) {
 			if (theTile.demolishTower()) {
 				this.towerCounts.put(player, this.towerCounts.get(player) - 1);
 				this.moveMade(player);
 				if (theTile.isTower()) {
 					this.addUpdate(new GameUpdate(UpdateType.TOWER_REDUCED,
 							new GridLocation(x, y)));
 					return true;
 				}
 				this.addUpdate(new GameUpdate(UpdateType.TOWER_DEMOLISHED,
 						new GridLocation(x, y)));
 				return true;
 			} else {
 				this.setMessage("Cannot demolish this tower");
 				return false;
 			}
 		}
 		// SELECT WORKER
 		if (theTile.isWorker(player)) {
 			theTile.select();
 			this.turnState = TurnState.SELECTED;
 			this.selectedTile = theTile;
 			this.addUpdate(new GameUpdate(UpdateType.WORKER_SELECTED,
 					new GridLocation(x, y)));
 			return true;
 		}
 
 		this.setMessage("You are in regular mode, yet you cannot do that!");
 		return false;
 	}
 
 	private static GamePlayer otherPlayer(GamePlayer player) {
 		return player == GamePlayer.BLUE ? GamePlayer.RED : GamePlayer.BLUE;
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
 
 	private void moveMade(GamePlayer player) {
 
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
 
 					this.towerCounts
 							.put(RiseGame.otherPlayer(player), this.towerCounts
 									.get(RiseGame.otherPlayer(player)) - 1);
 					if (this.towerCounts.get(RiseGame.otherPlayer(player)) == 0) {
 						this.addUpdate(new GameUpdate(
 								UpdateType.TOWER_DEMOLISHED, new GridLocation(
 										x, y)));
 					} else {
 						this.addUpdate(new GameUpdate(UpdateType.TOWER_REDUCED,
 								new GridLocation(x, y)));
 					}
 				}
 				if (thisTile.isTile()) {
 					if (this.tileSurrounded(thisTile, player)) {
 						thisTile.setTower(player, 0);
 						this.addUpdate(new GameUpdate(UpdateType.TOWER_CREATED,
 								new GridLocation(x, y), player));
 					}
 				}
 				if (thisTile.isTower(player)
 						&& this.tileSurrounded(thisTile, player)) {
 					this.towersProcessed.add(thisTile);
 
 					if (thisTile.buildTower()) {
 						this.towerCounts.put(player,
 								this.towerCounts.get(player) + 1);
 						this.addUpdate(new GameUpdate(UpdateType.TOWER_BUILT,
 								new GridLocation(x, y)));
 					}
 					continue;
 				}
 			}
 		}
 
 		this.addUpdate(new GameUpdate(UpdateType.MOVE_MADE, player));
 
 		this.turnState = TurnState.NOTHING;
 		this.moveCounter -= 1;
 		if (this.moveCounter <= 0) {
 			this.endTurn();
 		} else if (this.checkVictory()) {
 			return;
 		}
 	}
 
 	private void endTurn() {
 
 		this.turn = RiseGame.otherPlayer(this.turn);
 		this.moveCounter = 2;
 
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
 					this.towerCounts.put(RiseGame.otherPlayer(this.turn),
 							this.towerCounts.get(RiseGame
 									.otherPlayer(this.turn)) - 1);
 					this.addUpdate(new GameUpdate(UpdateType.TOWER_REDUCED,
 							new GridLocation(x, y)));
 				}
 				if (thisTile.isTile()) {
 					if (this.tileSurrounded(thisTile, turn)) {
 						thisTile.setTower(turn, 0);
 					}
 				}
 				if (thisTile.isTower(this.turn)
 						&& this.tileSurrounded(thisTile, this.turn)) {
 					this.towersProcessed.add(thisTile);
 					if (thisTile.buildTower()) {
 						this.towerCounts.put(this.turn,
 								this.towerCounts.get(this.turn) + 1);
 						this.addUpdate(new GameUpdate(UpdateType.TOWER_BUILT,
 								new GridLocation(x, y)));
 					}
 					continue;
 				}
 			}
 		}
 
 		this.addUpdate(new GameUpdate(UpdateType.TURN_FINISHED, this.turn));
 
 		if (this.checkVictory()) {
 			return;
 		}
 	}
 
 	private boolean checkVictory() {
 
 		if (this.availableWorkers.get(RiseGame.otherPlayer(this.turn)) == WORKER_COUNT) {
 			Log.e(TAG, "VICTORY NOT IMPLEMENTED!");
 			return true;
 		}
 		if (this.availableWorkers.get(this.turn) == WORKER_COUNT) {
 			Log.e(TAG, "VICTORY NOT IMPLEMENTED!");
 			return true;
 		}
 		return false;
 	}
 
 	private boolean tileSurrounded(RiseTile tile, GamePlayer player) {
 		RiseTile[] neighbours = getNeighbours(tile);
 		for (int n = 0; n < neighbours.length; n += 1) {
 			if (!neighbours[n].isWorker(player)) {
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
 
 	private boolean hasNeighbourWorker(int x, int y, GamePlayer player) {
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
 
 	public GameUpdate getUpdate() {
 		while (updateQueueLockout) {
 		}
 		updateQueueLockout = true;
 		if (!this.updateQueue.isEmpty()) {
			updateQueueLockout = false;
 			return this.updateQueue.get();
 		}
 		updateQueueLockout = false;
 		return null;
 	}
 
 	private void addUpdate(GameUpdate update) {
 		while (updateQueueLockout) {
 		}
 		updateQueueLockout = true;
 		this.updateQueue.put(update);
 		updateQueueLockout = false;
 	}
 
 	public boolean hasUpdate() {		
 		while (updateQueueLockout) {
 		}
 		return (!this.updateQueue.isEmpty());
 	}
 
 }
