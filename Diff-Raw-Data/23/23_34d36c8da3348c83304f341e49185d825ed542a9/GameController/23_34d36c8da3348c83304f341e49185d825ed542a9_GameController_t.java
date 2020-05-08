 /**
  * Copyright 2012 Jami Couch
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * This project uses:
  * 
  * LibGDX
  * Copyright 2011 see LibGDX AUTHORS file
  * Licensed under Apache License, Version 2.0 (see above).
  * 
  */
 package com.ahsgaming.valleyofbones;
 
 import com.ahsgaming.valleyofbones.map.HexMap;
 import com.ahsgaming.valleyofbones.map.MapData;
 import com.ahsgaming.valleyofbones.network.*;
 import com.ahsgaming.valleyofbones.screens.GameSetupConfig;
 import com.ahsgaming.valleyofbones.units.Prototypes;
 import com.ahsgaming.valleyofbones.units.Prototypes.JsonProto;
 import com.ahsgaming.valleyofbones.units.Unit;
 import com.ahsgaming.valleyofbones.units.UnitManager;
 import com.ahsgaming.valleyofbones.units.UnitView;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 
 /**
  * @author jami
  *
  */
 public class GameController {
 	public static final String DEFAULT_MAP = "blank.tmx";
 	public static final String MAP_DIRECTORY = "maps";
 	
 	public String LOG = "GameController";
 	
 	UnitManager unitManager;
 	Unit selectedObject;
 
 	Array<Player> players;
 	
 	String mapName;
 	HexMap map;
 	Vector2[] spawnPoints;
 	
 	GameStates state;
 	
 	Array<Command> commandHistory;
 	Array<Command> commandQueue;
 	Array<Command> cmdsToAdd;
 	
 	Player currentPlayer;
     int gameTurn = 0;
 	float turnLength = 60;
     float turnAfterMoveReset = 0;
     int maxPauses = 0;
 
 	float turnTimer = 0;
 	boolean nextTurn = false;
 	
 	int nextObjectId = 0;
 	
 	GameResult gameResult = null;
 
 	/**
 	 * Constructors
 	 */
 	
 	public GameController(GameSetupConfig config, Array<Player> players) {
 		// TODO load map
 		this.mapName = config.mapName;
         this.maxPauses = config.maxPauses;
 
         unitManager = new UnitManager(this);
 
 		commandHistory = new Array<Command>();
 		commandQueue = new Array<Command>();
 		cmdsToAdd = new Array<Command>();
 		
 		this.players = players;
 		
 		this.loadMap();
 		this.loadMapObjects();
 
 		state = GameStates.PAUSED;
 	}
 	
 	/**
 	 * Methods
 	 */
 	
 	private HexMap loadMap() {
 		// loads the map based on the value in mapName
 		if (mapName == null || mapName.length() == 0) mapName = DEFAULT_MAP;
 		// TODO implement loading of maps
 		//map = new HexMap(this, 19, 13, 2, 4);
 		map = new HexMap(this, Gdx.files.internal("maps/" + mapName));
 		return map;
 	}
 	
 	private void loadMapObjects() {
 		for (MapData.MapObject spawn : map.getPlayerSpawns()) {
 			Unit unit;
 			if (spawn.player >= 0 && spawn.player < players.size) {
                 unit = Unit.createUnit(getNextObjectId(), "castle-base", players.get(spawn.player));
 				players.get(spawn.player).setBaseUnit(unit);
 			} else {
                 unit = Unit.createUnit(getNextObjectId(), "castle-base", null);
                 Gdx.app.log(VOBGame.LOG, "Map Error: player spawn index out of range");
 			}
 			Vector2 pos = map.boardToMapCoords(spawn.x, spawn.y);
 			unit.getView().setPosition(pos.x, pos.y);
 			unit.getView().setBoardPosition(spawn.x, spawn.y);
 			unitManager.addUnit(unit);
 			
 			if (spawn.player >= 0 && spawn.player < players.size) {
 				addSpawnPoint(players.get(spawn.player).getPlayerId(), new Vector2(unit.getView().getX() + unit.getView().getWidth() * 0.5f, unit.getView().getY() + unit.getView().getHeight() * 0.5f));
 			}
 		}
 		
 		// TODO load capture points
         for (MapData.MapObject obj : map.getControlPoints()) {
             Unit unit;
             unit = Unit.createUnit(getNextObjectId(), obj.proto, obj.player == -1 ? null : players.get(obj.player));
             Vector2 pos = map.boardToMapCoords(obj.x, obj.y);
             unit.getView().setPosition(pos.x, pos.y);
             unit.getView().setBoardPosition(new Vector2(obj.x, obj.y));
             unitManager.addUnit(unit);
         }
 
         if (VOBGame.DEBUG_ATTACK) {
             Unit unit = Unit.createUnit(getNextObjectId(), "marine-base", players.get(0));
             unit.getView().setBoardPosition(9, 0);
             unit.getView().setPosition(getMap().boardToMapCoords(9, 0));
             unitManager.addUnit(unit);
 
             unit = Unit.createUnit(getNextObjectId(), "marine-base", players.get(1));
             unit.getView().setBoardPosition(10, 0);
             unit.getView().setPosition(getMap().boardToMapCoords(10, 0));
             unitManager.addUnit(unit);
         }
 	}
 	
 	public void update(float delta) {
 		
 		switch(state) {
             case RUNNING:
                 doCommands();
 
                 unitManager.update(delta);
                 map.update();
 
                 for (int p = 0; p < players.size; p++) {
                     players.get(p).update(this, delta);
                 }
 
                 turnTimer -= delta;
 
                 checkResult();
 
                 if (gameResult != null) {
                     this.state = GameStates.GAMEOVER;
                 }
 
                 break;
             case PAUSED:
                 for (Command c: cmdsToAdd) {
                     if (c instanceof Unpause) {
                         executeCommand(c);
                         map.invalidateViews();
                     } else {
                         commandQueue.add(c);
                     }
                 }
                 break;
         }
 
 	}
 	
 	public void doTurn() {
 //		Gdx.app.log(LOG, "doTurn");
 
         if (nextTurn) {
             unitManager.endTurn(currentPlayer);
         }
 
 		gameTurn += 1;
         turnTimer = turnLength;
         nextTurn = false;
 
         if (players.size > 0 && (gameTurn > 1 || currentPlayer == null)) {    // this way when the NetController sets currentPlayer, that player goes first (doTurn is called at the beginning of the game)
             int i = players.indexOf(currentPlayer, true);
             i = (i + 1) % players.size;
             currentPlayer = players.get(i);
         }
 
         currentPlayer.startTurn(this);
 
         unitManager.startTurn(currentPlayer);
 
         map.invalidateViews();
 	}
 	
 	public void checkResult() {
 		int alive = 0;
 		Player playerAlive = null;
 		for (Player p: players) {
 			if (p.isAlive()) {
 				alive += 1;
 				playerAlive = p;
 			}
 		}
 		
 		if (alive <= 1) {
 			GameResult result = new GameResult();
 			if (playerAlive != null) {
 				result.winner = playerAlive.getPlayerId();
 			}
 			Array<Integer> losingPlayers = new Array<Integer>();
 			for (Player p : players) {
 				if (!p.isAlive()) {
 					losingPlayers.add(p.getPlayerId());
 				}
 			}
 			result.losers = new int[losingPlayers.size];
 			for (int i = 0; i < losingPlayers.size; i++) {
 				result.losers[i] = losingPlayers.get(i);
 			}
 			
 			//Gdx.app.log(LOG, String.format("Game Over // Winner: %d (%d); Losers: (%d)", result.winningTeam, result.winners.length, result.losers.length));
 			this.gameResult = result;
 		}
 	}
 
     public void declareWinner(Player p) {
         GameResult result = new GameResult();
         result.winner = p.getPlayerId();
         result.losers = new int[players.size - 1];
         int i = 0;
         for (Player player: players) {
             if (player != p) {
                 result.losers[i] = player.getPlayerId();
                 i++;
             }
         }
         this.gameResult = result;
     }
 
 	public void doCommands() {
 		// process commands
 
         commandQueue.addAll(cmdsToAdd);
         cmdsToAdd.clear();
 
 		Array<Command> toKeep = new Array<Command>();
 		commandQueue.reverse();
 		Command command;
 		while (commandQueue.size > 0) {
             command = commandQueue.pop();
 //			Gdx.app.log(LOG, "Command: " + Integer.toString(command.turn));
 //            if (command instanceof Unpause) Gdx.app.log(LOG, "UNPAUSE");
 //            if (command instanceof Pause) Gdx.app.log(LOG, "PAUSE");
 //            Gdx.app.log(LOG, "Current turn: " + gameTurn);
 			if (command.turn < gameTurn) {
 				// remove commands in the past without executing
 
 			} else if (command.turn == gameTurn) {
 				// execute current commands and remove
 				if (state == GameStates.RUNNING || command instanceof Unpause) { 
 					executeCommand(command);
 				}
 
 			} else {
 				// future commands are left alone
 
 				toKeep.add(command);
 			}
 		}
 		
 		
 	}
 	
 	public boolean validate(Command cmd) {
 		if (cmd.owner != currentPlayer.getPlayerId() && !(cmd instanceof Surrender) && !(cmd instanceof Pause) && !(cmd instanceof Unpause)) return false;
 
 		if (cmd instanceof Attack) {
             Unit u = unitManager.getUnit(((Attack)cmd).unit);
             return u != null && u.getData().getAttacksLeft() >= 1;
 		} else if (cmd instanceof Build) {
 			Build b = (Build)cmd;
 			return (getPlayerById(b.owner).canBuild(b.building, this) && isBoardPosEmpty(b.location));
 		} else if (cmd instanceof Move) {
 			return validateMove((Move)cmd);
 		} else if (cmd instanceof Pause) {
 			return cmd.owner == -1 || maxPauses == 0 || ((Pause) cmd).isAuto || getPlayerById(cmd.owner).getPauses() < maxPauses;
 		} else if (cmd instanceof Unpause) {
 			return true;
 		} else if (cmd instanceof EndTurn) {
             return (cmd.owner == currentPlayer.getPlayerId());
         } else if (cmd instanceof Refund) {
             Refund r = (Refund)cmd;
             Player p = getPlayerById(r.owner);
             Unit u = unitManager.getUnit(r.unit);
             return canPlayerRefundUnit(p, u);
         } else if (cmd instanceof ActivateAbility) {
             ActivateAbility ab = (ActivateAbility)cmd;
             Unit u = unitManager.getUnit(ab.unit);
             if (u.getOwner() != null && u.getOwner().getPlayerId() != ab.owner) return false;
             return true;
         } else if (cmd instanceof Surrender) {
             return true;
         }
 		return false;
 	}
 	
 	public void executeCommand(Command cmd) {
 		if (!validate(cmd)) return;
 		if (cmd instanceof Attack) {
 			executeAttack((Attack)cmd);
             if (turnTimer < turnAfterMoveReset)
                 turnTimer = turnAfterMoveReset;
 		} else if (cmd instanceof Build) {
 //			Gdx.app.log(LOG, "Building: " + Integer.toString(cmd.turn));
 			executeBuild((Build)cmd);
             if (turnTimer < turnAfterMoveReset)
                 turnTimer = turnAfterMoveReset;
 		} else if (cmd instanceof Move) {
 			executeMove((Move)cmd);
             if (turnTimer < turnAfterMoveReset)
                 turnTimer = turnAfterMoveReset;
 		} else if (cmd instanceof Pause) {
 			executePause((Pause)cmd);
 		} else if (cmd instanceof Unpause) {
 			executeUnpause((Unpause)cmd);
 		} else if (cmd instanceof EndTurn) {
             setNextTurn(true);
         } else if (cmd instanceof Refund) {
             Refund r = (Refund)cmd;
             refundUnit(getPlayerById(r.owner), unitManager.getUnit(r.unit));
             if (turnTimer < turnAfterMoveReset)
                 turnTimer = turnAfterMoveReset;
         } else if (cmd instanceof ActivateAbility) {
             ActivateAbility ab = (ActivateAbility)cmd;
             Unit u = unitManager.getUnit(ab.unit);
             unitManager.activateAbility(u);
             if (turnTimer < turnAfterMoveReset)
                 turnTimer = turnAfterMoveReset;
         } else if (cmd instanceof Surrender) {
             Player p = getPlayerById(cmd.owner);
             p.getBaseUnit().getData().setCurHP(0);
         } else {
 //			Gdx.app.log(LOG, "Unknown command");
 		}
 
         commandHistory.add(cmd);
         map.invalidateViews();
 	}
 	
 	public void executeAttack(Attack cmd) {
 		//Gdx.app.log(LOG, String.format("Attack: unit(%d) --> unit(%d)", cmd.unit, cmd.target));
         Unit obj = unitManager.getUnit(cmd.unit);
 		Unit tar = unitManager.getUnit(cmd.target);
 		if (obj == null || tar == null) {
 			if (obj == null) Gdx.app.log(LOG, String.format("Attack: cannot find unit(%d)", cmd.unit));
 			if (tar == null) Gdx.app.log(LOG, String.format("Attack: cannot find target(%d)", cmd.target));
 		} else if (obj.getOwner() == null || obj.getOwner().getPlayerId() != cmd.owner) {
 			Gdx.app.log(LOG,  "Error: object owner does not match command owner");
 		} else {
 			if ((!tar.getData().isInvisible() || obj.getData().isDetector()) && playerCanSee(obj.getOwner(), tar)) {
                 if (unitManager.attack(obj, tar) && obj.getData().getSplashDamage() > 0) {
                     for (Vector2 pos: HexMap.getAdjacent((int) tar.getView().getBoardPosition().x, (int) tar.getView().getBoardPosition().y)) {
                         Unit o = unitManager.getUnit(pos);
                         if (o != null) {
                             unitManager.applyDamage(o, obj.getData().getAttackDamage() * obj.getData().getSplashDamage() * obj.getData().getBonus(o.getData().getSubtype()));
                         }
                     }
                 }
             }
 		}
 	}
 
     public boolean playerCanSee(Player player, Unit target) {
         return unitManager.canPlayerSee(player, target);
     }
 	
 	public void executeBuild(Build cmd) {
 		// check that this can be built
 		JsonProto junit = Prototypes.getProto(cmd.building);
 		Vector2 levelPos = map.boardToMapCoords(cmd.location.x, cmd.location.y);
 		Player owner = getPlayerById(cmd.owner);
 			
 		// TODO place builder
 		// for now, just add the unit
         Unit unit = Unit.createUnit(getNextObjectId(), cmd.building, this.getPlayerById(cmd.owner));
 		unit.getView().setPosition(levelPos.x - 300 * VOBGame.SCALE, levelPos.y + 600 * VOBGame.SCALE);
 		unit.getView().setBoardPosition((int) cmd.location.x, (int) cmd.location.y);
         unit.getView().addAction(UnitView.Actions.moveTo(levelPos.x, levelPos.y, 0.5f));
 		
 		unitManager.addUnit(unit);
 		owner.setBankMoney(owner.getBankMoney() - unit.getData().getCost());
 		owner.updateFood(this);
         cmd.unitId = unit.getId();
 	}
 	
 	public void executeMove(Move cmd) {
 		Unit unit = unitManager.getUnit(cmd.unit);
 		unitManager.moveUnit(unit, cmd.toLocation);
 	}
 
     public boolean validateMove(Move m) {
         if (m.toLocation.x < 0 || m.toLocation.x >= map.getWidth() || m.toLocation.y < 0 || m.toLocation.y >= map.getHeight()) return false;
 
         Unit u = unitManager.getUnit(m.unit);
 
         if (u.getOwner() == null || u.getOwner().getPlayerId() != m.owner) return false;
 
         boolean[] notavailable = new boolean[map.getWidth() * map.getHeight()];
         for (Unit unit: getUnits()) {
             if (unit.getId() != u.getId())
                 notavailable[(int)(unit.getView().getBoardPosition().y * map.getWidth() + unit.getView().getBoardPosition().x)] = true;
         }
 
         int[] start = {(int)(u.getView().getBoardPosition().y * map.getWidth() + u.getView().getBoardPosition().x)};
         int[] radii = {(int)u.getData().getMovesLeft()};
 
         boolean[] available = map.getAvailablePositions(start, radii, notavailable, true);
 
         int to = (int)(m.toLocation.y * map.getWidth() + m.toLocation.x);
         return (available[to] && !notavailable[to]);
     }
 	
 	public void executePause(Pause cmd) {
 		state = GameStates.PAUSED;
         if (cmd.owner != -1 && !cmd.isAuto) {
             getPlayerById(cmd.owner).addPause();
         }
 	}
 	
 	public void executeUnpause(Unpause cmd) {
 		state = GameStates.RUNNING;
 	}
 	
 	/**
 	 * Getters/Setters
 	 */
 
 	public Array<Player> getPlayers() {
 		return players;
 	}
 	
 	public Player getPlayerById(int id) {
 		for (Player player: players) {
 			if (player.getPlayerId() == id) return player;
 		}
 		return null;
 	}
 
     public Array<Unit> getUnits() {
         return unitManager.getUnits();
     }
 	
 	public Array<Unit> getUnitsByPlayerId(int id) {
 		return unitManager.getUnits(id);
 	}
 
     public boolean canPlayerRefundUnit(Player player, Unit unit) {
         return false; // TODO re-implement refund
 //        return player != null && unit != null
 //                && player == currentPlayer && unit.getOwner() == player
 //                && !unit.getData().getType().equals("building")
 //                && units.contains(unit, true)
 //                && !unit.isRemove();
     }
 
     public void refundUnit(Player player, Unit unit) {
 //        unit.setRemove(true);
 //        player.setBankMoney(player.getBankMoney() + unit.getRefund());
 //        removeGameUnit(unit);
     }
 
     public Array<Unit> getUnitsInArea(Vector2 boardPos, int radius) {
         return unitManager.getUnitsInArea(boardPos, radius);
     }
 
 	public Unit getUnitAtBoardPos(int x, int y) {
         return unitManager.getUnit(new Vector2(x, y));
 	}
 	
 	public Unit getUnitAtBoardPos(float x, float y) {
 		return getUnitAtBoardPos((int) x, (int) y);
 	}
 	
 	public Unit getUnitAtBoardPos(Vector2 boardPos) {
 		return getUnitAtBoardPos((int) boardPos.x, (int) boardPos.y);
 	}
 	
 	public boolean isBoardPosEmpty(int x, int y) {
         if (!map.getMapData().isBoardPositionTraversible(x, y)) return false;
         return (unitManager.getUnit(new Vector2(x, y)) == null);
 	}
 	
 	public boolean isBoardPosEmpty(float x, float y) {
 		return isBoardPosEmpty((int)x, (int)y);
 	}
 	
 	public boolean isBoardPosEmpty(Vector2 boardPos) {
 		return isBoardPosEmpty((int)boardPos.x, (int)boardPos.y);
 	}
 
     public UnitManager getUnitManager() {
         return unitManager;
     }
 	
 	public HexMap getMap() {
 		return map;
 	}
 	
 	public int getGameTurn() {
 		return gameTurn;
 	}
 	
 	public float getTurnTimer() {
 		return turnTimer;
 	}
 
    public float getTurnLength() {
        return turnLength;
    }

     public void setTurnTimer(float timer) {
         turnTimer = timer;
     }
 
     public Player getCurrentPlayer() {
         return currentPlayer;
     }
 
     public void setCurrentPlayer(Player p) {
         currentPlayer = p;
     }
 
     public void setCurrentPlayer(int pid) {
         currentPlayer = getPlayerById(pid);
     }
 	
 	public Array<Command> getCommandHistory() {
 		return this.commandHistory;
 	}
 	
 	public Array<Command> getCommandQueue() {
 		return this.commandQueue;
 	}
 
     public void setCommandQueue(Command[] commands) {
         this.commandQueue.clear();
         if (commands != null)
             commandQueue.addAll(commands);
     }
 	
 	public void queueCommand(Command cmd) {
 //		Gdx.app.log(LOG, state.toString());
 		if (state == GameStates.RUNNING || cmd instanceof Unpause) {
 			//if (!(cmdsToAdd.contains(cmd, false) || commandQueue.contains(cmd, false))) {
 				cmdsToAdd.add(cmd);
 //				Gdx.app.log(LOG, String.format("queued command (turn: %d)", cmd.turn));
 			//}
 		}
 	}
 	
 	public int getNextObjectId() {
 		int id = this.nextObjectId;
 		this.nextObjectId += 1;
 		return id;
 	}
 	
 	public GameStates getState() {
 		return state;
 	}
 	
 	public void setState(GameStates state) {
 		this.state = state;
 	}
 	
 	public void addSpawnPoint(int id, Vector2 point) {
 		if (id < 0) return;
 		
 		if (spawnPoints == null) {
 			spawnPoints = new Vector2[id + 1];
 		} else if (spawnPoints.length <= id) {
 			Vector2[] tmp = new Vector2[id + 1];
 			for (int i=0;i<spawnPoints.length;i++) tmp[i] = spawnPoints[i];
 			spawnPoints = tmp;
 		}
 		spawnPoints[id] = point;
 	}
 	
 	public Vector2 getSpawnPoint(int id) {
 		if (id >= 0 && id < spawnPoints.length){
 			return spawnPoints[id];
 		}
 		return null;
 	}
 	
 	public GameResult getGameResult() {
 		return gameResult;
 	}
 
     public boolean isNextTurn() {
         return nextTurn;
     }
 
     public void setNextTurn(boolean nextTurn) {
         this.nextTurn = nextTurn;
     }
 
     public String getMapName() {
         return mapName;
     }
 
     /**
 	 * static methods
 	 */
 
 }
