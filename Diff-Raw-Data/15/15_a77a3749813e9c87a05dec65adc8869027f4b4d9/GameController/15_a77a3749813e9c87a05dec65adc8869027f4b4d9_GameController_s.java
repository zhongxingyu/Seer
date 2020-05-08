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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.ahsgaming.valleyofbones.map.HexMap;
 import com.ahsgaming.valleyofbones.network.Attack;
 import com.ahsgaming.valleyofbones.network.Build;
 import com.ahsgaming.valleyofbones.network.Command;
 import com.ahsgaming.valleyofbones.network.Move;
 import com.ahsgaming.valleyofbones.network.Pause;
 import com.ahsgaming.valleyofbones.network.Unpause;
 import com.ahsgaming.valleyofbones.network.Upgrade;
 import com.ahsgaming.valleyofbones.units.Prototypes;
 import com.ahsgaming.valleyofbones.units.Prototypes.JsonProto;
 import com.ahsgaming.valleyofbones.units.Unit;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Group;
 
 /**
  * @author jami
  *
  */
 public class GameController {
 	public static final String DEFAULT_MAP = "blank.tmx";
 	public static final String MAP_DIRECTORY = "maps";
 	
 	public String LOG = "GameController";
 	
 	ArrayList<GameObject> gameObjects, objsToAdd, objsToRemove;
 	GameObject selectedObject;
 	Group grpRoot, grpUnits;
 	
 	ArrayList<Player> players;
 	
 	String mapName;
 	HexMap map;
 	Vector2[] spawnPoints;
 	
 	GameStates state;
 	
 	ArrayList<Command> commandHistory;
 	ArrayList<Command> commandQueue;
 	ArrayList<Command> cmdsToAdd;
 	
 	int gameTurn = 0;
 	float turnLength = 5;
 	float turnTimer = 0;
 	
 	int nextObjectId = 0;
 	
 	GameResult gameResult = null;
 	
 	/**
 	 * Constructors
 	 */
 	
 	public GameController(String mapName, ArrayList<Player> players) {
 		// TODO load map
 		this.mapName = mapName;
 		grpRoot = new Group();
 		grpUnits = new Group();
 		
 		
 		gameObjects = new ArrayList<GameObject>();
 		selectedObject = null;
 		objsToAdd = new ArrayList<GameObject>();
 		objsToRemove = new ArrayList<GameObject>();
 		
 		commandHistory = new ArrayList<Command>();
 		commandQueue = new ArrayList<Command>();
 		cmdsToAdd = new ArrayList<Command>();
 		
 		this.players = players;
 		
 		this.loadMap();
 		this.loadMapObjects();
 		
 		grpRoot.addActor(map.getMapGroup());
 		grpRoot.addActor(grpUnits);
 		grpRoot.setSize(map.getMapWidth(), map.getMapHeight());
 		
 		// TODO start paused
 		state = GameStates.RUNNING;
 	}
 	
 	/**
 	 * Methods
 	 */
 	
 	private HexMap loadMap() {
 		// loads the map based on the value in mapName
 		if (mapName == null || mapName.length() == 0) mapName = DEFAULT_MAP;
 		// TODO implement loading of maps
 		map = new HexMap(20, 10, 2, 3);
 		
 		return map;
 	}
 	
 	private Group loadMapObjects() {
 		int player = 0;
 		for (Vector2 spawn : map.getPlayerSpawns()) {
 			//Vector2 objPos = mapToLevelCoords(spawn);
 			Unit unit;
 			if (player >= 0 && player < players.size()) {
				unit = new Unit(getNextObjectId(), players.get(player), (JsonProto)Prototypes.getProto("space-station-base"));
 				players.get(player).setBaseUnit(unit);
 			} else {
				unit = new Unit(getNextObjectId(), null, (JsonProto)Prototypes.getProto("space-station-base"));
 				Gdx.app.log(VOBGame.LOG, "Map Error: player spawn index out of range");
 			}
 			Gdx.app.log(LOG, spawn.toString());
 			Vector2 pos = map.boardToMapCoords((int)spawn.x, (int)spawn.y);
 			unit.setPosition(pos.x, pos.y);
 			unit.setBoardPosition((int)spawn.x, (int)spawn.y);
 			addGameUnit(unit);
 			
 			if (player >= 0 && player < players.size()) {
 				addSpawnPoint(players.get(player).getPlayerId(), new Vector2(unit.getX() + unit.getWidth() * 0.5f, unit.getY() + unit.getHeight() * 0.5f));
 			}
 			player ++;
 		}
 		
 		// TODO load capture points
 		
 		return grpUnits;
 	}
 	
 	public void update(float delta) {
 		if (state == GameStates.RUNNING) {
 			turnTimer -= delta;
 			if (turnTimer <= 0) {
 				doTurn();
 				turnTimer = turnLength;
 			}
 		}
 		doCommands(delta);
 	}
 	
 	public void doTurn() {
 		Gdx.app.log(LOG, "doTurn");
 		gameTurn += 1;
 		float delta = 1;
 		
 		for (Player p: players) {
 			p.update(this, delta);
 		}
 		
 		// update collection (do simulation for turn)
 		gameObjects.removeAll(objsToRemove);
 		for (GameObject obj: objsToRemove) {
 			grpUnits.removeActor(obj);
 		}
 		objsToRemove.clear();
 		
 		for (GameObject obj: objsToAdd) {
 			addGameUnitNow(obj);
 		}
 		objsToAdd.clear();
 		
 		for (GameObject obj : gameObjects) {
 			// TODO update object positions
 			
 			obj.update(this, delta);
 			
 			if (obj.isRemove()) objsToRemove.add(obj);
 		}
 		
 		
 		checkResult();
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
 			ArrayList<Integer> losingPlayers = new ArrayList<Integer>();
 			for (Player p : players) {
 				if (!p.isAlive()) {
 					losingPlayers.add(p.getPlayerId());
 				}
 			}
 			result.losers = new int[losingPlayers.size()];
 			for (int i = 0; i < losingPlayers.size(); i++) {
 				result.losers[i] = losingPlayers.get(i);
 			}
 			
 			//Gdx.app.log(LOG, String.format("Game Over // Winner: %d (%d); Losers: (%d)", result.winningTeam, result.winners.length, result.losers.length));
 			this.gameResult = result;
 		}
 	}
 
 	public void doCommands(float delta) {
 		// process commands
 		
 		List<Command> toAdd = cmdsToAdd;
 		cmdsToAdd = new ArrayList<Command>();
 	
 		commandQueue.addAll(toAdd);
 	
 	
 		List<Command> toRemove = new ArrayList<Command>();
 		for (Command command: commandQueue) {
 			Gdx.app.log(LOG, "Command: " + Integer.toString(command.turn));
 			if (command.turn < gameTurn) {
 				// remove commands in the past without executing
 				toRemove.add(command);
 			} else if (command.turn == gameTurn) {
 				// execute current commands and remove
 				// TODO execute the command
 				Gdx.app.log(VOBGame.LOG, "Executing command on tick " + Integer.toString(command.turn) + "==" + Integer.toString(gameTurn));
 				if (state == GameStates.RUNNING || command instanceof Unpause) { 
 					executeCommand(command);
 				}
 				toRemove.add(command);
 				commandHistory.add(command);
 			} // future commands are left alone
 		}
 		commandQueue.removeAll(toRemove);
 		
 		
 	}
 	
 	public boolean validate(Command cmd) {
 		
 		if (cmd instanceof Attack) {
 			return true;
 		} else if (cmd instanceof Build) {
 			Build b = (Build)cmd;
 			return (getPlayerById(b.owner).canBuild(b.building, this) && isBoardPosEmpty(b.location));
 		} else if (cmd instanceof Move) {
 			return true;
 		} else if (cmd instanceof Pause) {
 			return true;
 		} else if (cmd instanceof Unpause) {
 			return true;
 		} else if (cmd instanceof Upgrade) {
 			Upgrade u = (Upgrade)cmd;
 			// TODO check dependencies here
 			Player player = this.getPlayerById(u.owner);
 			GameObject obj = this.getObjById(u.unit);
 			if (obj instanceof Unit) {
 				return player.canUpgrade((Unit)obj, u.upgrade, this);
 			}
 			
 			return false;
 		}
 		return false;
 	}
 	
 	public void executeCommand(Command cmd) {
 		if (!validate(cmd)) return;
 		if (cmd instanceof Attack) {
 			executeAttack((Attack)cmd);
 		} else if (cmd instanceof Build) {
 			Gdx.app.log(LOG, "Building: " + Integer.toString(cmd.turn));
 			executeBuild((Build)cmd);
 		} else if (cmd instanceof Move) {
 			executeMove((Move)cmd);
 		} else if (cmd instanceof Pause) {
 			executePause((Pause)cmd);
 		} else if (cmd instanceof Unpause) {
 			executeUnpause((Unpause)cmd);
 		} else if (cmd instanceof Upgrade) {
 			executeUpgrade((Upgrade)cmd);
 		} else {
 			Gdx.app.log(LOG, "Unknown command");
 		}
 	}
 	
 	public void executeAttack(Attack cmd) {
 		//Gdx.app.log(LOG, String.format("Attack: unit(%d) --> unit(%d)", cmd.unit, cmd.target));
 		GameObject obj = getObjById(cmd.unit);
 		GameObject tar = getObjById(cmd.target);
 		if (obj == null || tar == null) {
 			if (obj == null) Gdx.app.log(LOG, String.format("Attack: cannot find unit(%d)", cmd.unit));
 			if (tar == null) Gdx.app.log(LOG, String.format("Attack: cannot find target(%d)", cmd.target));
 		} else if (obj.owner == null || obj.owner.getPlayerId() != cmd.owner) {
 			Gdx.app.log(LOG,  "Error: object owner does not match command owner");
 		} else {
 			if (obj instanceof Unit && tar instanceof Unit) {
 				((Unit)obj).doCommand(cmd, cmd.isAdd);// TODO implement shift-click to add to queue
 			} else {
 				if (!(obj instanceof Unit)) {
 					Gdx.app.log(LOG, "Error: unit is not a Unit");
 				}
 				
 				if (!(tar instanceof Unit)) {
 					Gdx.app.log(LOG, "Error: target is not a Unit");
 				}
 			}
 		}
 	}
 	
 	public void executeBuild(Build cmd) {
 		// check that this can be built
 		JsonProto junit = Prototypes.getProto(cmd.building);
 		Vector2 levelPos = map.boardToMapCoords(cmd.location.x, cmd.location.y);
 		Player owner = getPlayerById(cmd.owner);
 		if (owner.canBuild(junit.id, this) && isBoardPosEmpty(cmd.location)) {
 			
 			// TODO place builder
 			// for now, just add the unit
 			Unit unit = new Unit(getNextObjectId(), this.getPlayerById(cmd.owner), (JsonProto)Prototypes.getProto(cmd.building));
 			unit.setPosition(levelPos);
 			unit.setBoardPosition((int)cmd.location.x, (int)cmd.location.y);
 			
 			addGameUnitNow(unit);
 			owner.setBankMoney(owner.getBankMoney() - unit.getCost());
 			owner.update(this, 0);
 		}
 		
 	}
 	
 	public void executeMove(Move cmd) {
 		Gdx.app.log(LOG, "Move unit: " + Integer.toString(cmd.unit)+ " to: " + cmd.toLocation.toString());
 		GameObject obj = getObjById(cmd.unit);
 		if (obj == null) {
 			Gdx.app.log(this.getClass().getSimpleName(), "Error: unknown unit id");
 		} else if (obj.owner == null || obj.owner.getPlayerId() != cmd.owner) {
 			Gdx.app.log(LOG, "Error: object owner does not match command owner");
 		} else {
 			if (obj instanceof Unit) {
 				((Unit)obj).doCommand(cmd, cmd.isAdd);
 			} else {
 				obj.moveTo(cmd.toLocation, cmd.isAdd);
 			}
 		}
 	}
 	
 	public void executePause(Pause cmd) {
 		state = GameStates.PAUSED;
 	}
 	
 	public void executeUnpause(Unpause cmd) {
 		state = GameStates.RUNNING;
 	}
 	
 	public void executeUpgrade(Upgrade cmd) {
 		
 	}
 	
 	/**
 	 * Getters/Setters
 	 */
 	
 	public Group getGroup() {
 		return grpRoot;
 	}
 	
 	public ArrayList<Player> getPlayers() {
 		return players;
 	}
 	
 	public Player getPlayerById(int id) {
 		for (Player player: players) {
 			if (player.getPlayerId() == id) return player;
 		}
 		return null;
 	}
 	
 	private void addGameUnitNow(GameObject obj) {
 		
 		if (!gameObjects.contains(obj)) gameObjects.add(obj);
 		
 		
 		if (!obj.hasParent() || !obj.getParent().equals(grpUnits)) grpUnits.addActor(obj);
 	}
 	
 	public void addGameUnit(GameObject obj) {
 		if (!objsToAdd.contains(obj)) objsToAdd.add(obj);
 	}
 	
 	public void removeGameUnit(GameObject obj) {
 		gameObjects.remove(obj);
 		grpUnits.removeActor(obj);
 	}
 	
 	public ArrayList<GameObject> getGameObjects() {
 		ArrayList<GameObject> ret = new ArrayList<GameObject>();
 		
 		for(GameObject obj: gameObjects) {
 			ret.add(obj);
 		}
 		
 		return ret;
 	}
 	
 	public GameObject getObjById(int id) {
 		
 		for (GameObject obj: gameObjects) {
 			if (obj.getObjId() == id) {
 				return obj;
 			}
 		}
 		
 		return null;
 	}
 	
 	public ArrayList<Unit> getUnitsByPlayerId(int id) {
 		ArrayList<Unit> ret = new ArrayList<Unit>();
 		
 		for (GameObject obj: gameObjects) {
 			if (obj instanceof Unit && obj.getOwner() != null && obj.getOwner().getPlayerId() == id) {
 				ret.add((Unit)obj);
 			}
 		}
 		
 		return ret;
 	}
 	
 	public GameObject getSelectedObject() {
 		return selectedObject;
 	}
 	
 	public void selectObjAtBoardPos(int x, int y) {
 		selectedObject = getObjAtBoardPos(x, y);
 	}
 	
 	public void selectObjAtBoardPos(float x, float y) {
 		selectObjAtBoardPos((int)x, (int)y);
 	}
 	
 	public void selectObjAtBoardPos(Vector2 boardPos) {
 		selectObjAtBoardPos((int)boardPos.x, (int)boardPos.y);
 	}
 	
 	public ArrayList<GameObject> getObjsAtPosition(Vector2 mapCoords) {
 		ArrayList<GameObject> returnVal = new ArrayList<GameObject>();
 		
 		
 		for (GameObject obj: gameObjects) {
 			if (obj.isColliding(mapCoords)) {
 				returnVal.add(obj);
 			}
 		}
 		
 		
 		return returnVal;
 	}
 	
 	public GameObject getObjAtBoardPos(int x, int y) {
 		for (GameObject obj: gameObjects) {
 			if (obj.getBoardPosition().x == x && obj.getBoardPosition().y == y) return obj; 
 		}
 		return null;
 	}
 	
 	public GameObject getObjAtBoardPos(float x, float y) {
 		return getObjAtBoardPos((int)x, (int)y);
 	}
 	
 	public GameObject getObjAtBoardPos(Vector2 boardPos) {
 		return getObjAtBoardPos((int)boardPos.x, (int)boardPos.y);
 	}
 	
 	public boolean isBoardPosEmpty(int x, int y) {
 		for (GameObject obj: this.gameObjects) {
 			if (obj.getBoardPosition().x == x && obj.getBoardPosition().y == y) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public boolean isBoardPosEmpty(float x, float y) {
 		return isBoardPosEmpty((int)x, (int)y);
 	}
 	
 	public boolean isBoardPosEmpty(Vector2 boardPos) {
 		return isBoardPosEmpty((int)boardPos.x, (int)boardPos.y);
 	}
 	
 	public HexMap getMap() {
 		return map;
 	}
 	
 	public int getGameTurn() {
 		return gameTurn;
 	}
 	
 	public ArrayList<Command> getCommandHistory() {
 		return this.commandHistory;
 	}
 	
 	public ArrayList<Command> getCommandQueue() {
 		return this.commandQueue;
 	}
 	
 	public void queueCommand(Command cmd) {
 		Gdx.app.log(LOG, state.toString());
 		if (state == GameStates.RUNNING || cmd instanceof Unpause) {
 			cmdsToAdd.add(cmd);
 			Gdx.app.log(LOG, String.format("queued command (turn: %d)", cmd.turn));
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
 	
 	/**
 	 * static methods
 	 */
 
 }
