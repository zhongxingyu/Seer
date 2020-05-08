 package fr.frozen.iron.common;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 
 import fr.frozen.game.GameObject;
 import fr.frozen.game.GameState;
 import fr.frozen.iron.common.entities.IronUnit;
 import fr.frozen.iron.util.IronConst;
 import fr.frozen.iron.util.IronGL;
 import fr.frozen.iron.util.IronUtil;
 import fr.frozen.util.pathfinding.AStar;
 import fr.frozen.util.pathfinding.Path;
 import fr.frozen.util.pathfinding.PathFinder;
 
 public class IronWorld extends GameState {
 	
 	//protected IronServer server;
 	protected GameContext context;
 	protected IronMap map;
 	protected Hashtable<Integer,IronUnit> units;
 	protected List<GameObject> gameObjects;
 	protected PathFinder pathFinder;
 	//hashmap ?
 
 	public IronWorld(GameContext context) {
 		super(null, "world");
 		this.context = context;
 		units = new Hashtable<Integer, IronUnit>();
 		gameObjects = new ArrayList<GameObject>();
 	}
 	
 	public synchronized void reInit() {
 		map = null;
		units.clear();
 		gameObjects.clear();
 	}
 	
 	public void endTurn(int playerId) {
 		for (IronUnit unit : getPlayerUnits(playerId)) {
 			unit.onEndTurn();
 		}
 	}
 	
 	public void initTurn(int playerId, boolean addParticles) {
 		for (IronUnit unit : getPlayerUnits(playerId)) {
 			unit.onStartTurn(addParticles);
 		}
 	}
 	
 	public boolean areAllUnitsDead(int playerId) {
 		boolean res = true;
 		for (IronUnit unit : getPlayerUnits(playerId)) {
 			res &= unit.isDead();
 		}
 		return res;
 	}
 	
 	public boolean haveAllUnitsPlayed(int playerId) {
 		boolean res = true;
 		for (IronUnit unit : getPlayerUnits(playerId)) {
 			res &= unit.hasPlayed();
 		}
 		return res;
 	}
 	
 	public GameContext getContext() {
 		return context;
 	}
 	
 	public void setContext(GameContext context) {
 		this.context = context;
 	}
 	
 	public IronMap getMap() {
 		return map;
 	}
 	
 	public void setMap(IronMap map) {
 		this.map = map;
 		pathFinder = new AStar(map, false);
 	}
 	
 	public synchronized Collection<IronUnit> getUnits() {
 		return units.values();
 	}
 	
 	public synchronized Collection<IronUnit> getPlayerUnits(int playerId) {
 		List<IronUnit> list = new ArrayList<IronUnit>();
 		
 		for (IronUnit unit : getUnits()) {
 			if (unit.getOwnerId() == playerId) {
 				list.add(unit);
 			}
 		}
 		return list;
 	}
 	
 	public synchronized IronUnit getUnitAtXY(int x, int y) {
 		//returns null if no one at XY
 		if (map == null) return null;
 		if (map.getTile(x, y) == null) return null;
 		return map.getTile(x, y).getUnitOnTile();
 	}
 
 	public synchronized boolean isInMelee(int unitId) {
 		IronUnit unitSrc = getUnitFromId(unitId);
 		if (unitSrc == null) return false;
 		
 		for (IronUnit unit : getUnits()) {
 			if (unit.getOwnerId() != unitSrc.getOwnerId() && !unit.isDead()) {
 				if (IronUtil.distance(unit.getX(), unit.getY(),
 									  unitSrc.getX(), unitSrc.getY()) <= 1) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public synchronized IronUnit getUnitFromId(int id) {
 		return units.get(id);
 	}
 	
 	public synchronized void addUnit(IronUnit unit) {
 		units.put(unit.getId(), unit);
 		getMap().getTile((int)unit.getPos().getX(), (int)unit.getPos().getY()).setUnitOnTile(unit);
 	}
 	
 	
 	public synchronized void removeUnit(int id) {
 		IronUnit unit = units.get(id);
 		getMap().getTile((int)unit.getPos().getX(), (int)unit.getPos().getY()).setUnitOnTile(null);
 		units.remove(id);
 	}
 	
 	public synchronized void setUnits(List<IronUnit> units) {
 		this.units.clear();
 		for (IronUnit unit : units) {
 			addUnit(unit);
 		}
 	}
 	
 	public synchronized void update(float deltaTime) {
 		super.update(deltaTime);
 		for (IronUnit unit : getUnits()) {
 			unit.update(deltaTime);
 		}
 	}
 	
 	public synchronized void render(float deltaTime, IronUnit selectedUnit) {
 		if (map != null) {
 			map.renderTiles(deltaTime);
 			
 			List<GameObject> corpses = getGameObjectCollection("corpse");
 			if (corpses != null) {
 				for (GameObject corpse : corpses){
 					corpse.render(deltaTime);
 				}
 			}
 			
 			
 			if (selectedUnit != null) {
 				
 				selectedUnit.renderMoveableTiles();
 				selectedUnit.renderActionTiles();
 				
 				int x = Mouse.getX() / IronConst.TILE_WIDTH;
 				int y = (Display.getDisplayMode().getHeight() - Mouse.getY()) / IronConst.TILE_HEIGHT;
 				//out of bounds
 				if (x >= 0 && x < IronConst.MAP_WIDTH && y >= 0 && y < IronConst.MAP_HEIGHT) {
 					if (getPath(selectedUnit.getId(), x, y) != null) {
 						IronGL.drawRect(x * IronConst.TILE_WIDTH,
 								y * IronConst.TILE_HEIGHT,
 								IronConst.TILE_WIDTH,
 								IronConst.TILE_HEIGHT,
 								0.3f, 1f, 0f, 0.4f);
 					}
 				}
 			}
 			
 			map.renderUnitsTileGfx(deltaTime);
 			getMap().renderGrid();
 			map.renderUnitsAndObjects(deltaTime);
 			//map.renderUnits(deltaTime);
 			
 			
 			List<GameObject> gfxList = getGameObjectCollection("gfx");
 			if (gfxList != null) {
 				for (GameObject gfx : gfxList){
 					gfx.render(deltaTime);
 				}
 			}
 		}
 	}
 	
 	
 	public Path getPath(int unitId, int x, int y) {
 		IronUnit unit = getUnitFromId(unitId);
 		if (unit == null) return null;
 		
 		Path path = pathFinder.findPath(unit,
 										(int)unit.getPos().getX(), (int)unit.getPos().getY(),
 										x, y,
 										unit.getMovement());
 		return path;
 	}
 }
