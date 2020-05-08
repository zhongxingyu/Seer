 package de.xftl.model.ships;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.xftl.spec.model.Direction;
 import de.xftl.spec.model.Point;
 import de.xftl.spec.model.crew.CrewMember;
 import de.xftl.spec.model.ships.Positioned;
 import de.xftl.spec.model.ships.Room;
 import de.xftl.spec.model.ships.RoomConnector;
 import de.xftl.spec.model.ships.Tile;
 import de.xftl.spec.model.ships.TileOrRoomConnector;
 import de.xftl.spec.model.ships.TileUnit;
 
 public class BasicTile implements Tile {
 
 	private static final float HULL_BREACH_AIR_VENT_FACTOR = 0.1f;
 	private static final float FIRE_OXYGEN_CONSUMPTION = 0.2f;
 	private static final float FIRE_SPREAD_PROBABILITY = 0.2f;
 	private static final float FIRE_SPREAD_OXYGEN_REQUIREMENT = 0.5f;
	private static final float FIRE_DIE_THRESHOLD = 0.2f;
 	
 	private Room _room;
 	private Point<TileUnit> _leftUpperCornerPos;
 	private CrewMember _crewMember;
 	private CrewMember _enemyCrewMember;
 	
 	private TileOrRoomConnector _north;
 	private TileOrRoomConnector _east;
 	private TileOrRoomConnector _south;
 	private TileOrRoomConnector _west;
 	private List<Tile> _neightboringTiles = new ArrayList<>(4);
 	
 	private float _hullBreachLevel;
 	private boolean _onFire;
 	
 	public BasicTile(Room room, Point<TileUnit> leftUpperCornerPos) {
 		super();
 		
 		_room = room;
 		_leftUpperCornerPos = leftUpperCornerPos;
 	}
 	
 	@Override
 	public void update(float elapsedTime) {
 		updateHullBreach(elapsedTime);
 		updateFire(elapsedTime);
 	}
 
 	@Override
 	public Point<TileUnit> getLeftUpperCornerPos() {
 		return _leftUpperCornerPos;
 	}
 
 	@Override
 	public CrewMember getCrewMember() {
 		return _crewMember;
 	}
 
 	@Override
 	public CrewMember getEnemyCrewMember() {
 		return _enemyCrewMember;
 	}
 
 	@Override
 	public int compareTo(Positioned<TileUnit> o) {
 	    return _leftUpperCornerPos.compareTo(o.getLeftUpperCornerPos());
 	}
 	
 	public void addNeighbor(Direction dir, TileOrRoomConnector neighbor) {
 		switch (dir) {
 		    case NORTH:
 		        _north = neighbor;
 		        break;
 		    case EAST:
 		        _east = neighbor;
 		        break;
 		    case SOUTH:
 		        _south = neighbor;
 		        break;
 		    case WEST:
 		        _west = neighbor;
 		        break;
 		    default:
 		        throw new IllegalArgumentException("Unknown enum value " + dir);
 		}
 		
 		if (neighbor instanceof RoomConnector)
 		    _room.addRoomConnector((RoomConnector) neighbor);
 		else if (neighbor instanceof Tile)
 		    _neightboringTiles.add((Tile) neighbor);
 	}
 	
 	@Override
 	public String toString() {
 	    return String.format("BasicTile %s", _leftUpperCornerPos);
 	}
 
 	public Room getRoom() {
 		return _room;
 	}
 
     @Override
     public TileOrRoomConnector getNorthNeighbor() {
         return _north;
     }
 
     @Override
     public TileOrRoomConnector getEastNeighbor() {
         return _east;
     }
 
     @Override
     public TileOrRoomConnector getSouthNeighbor() {
         return _south;
     }
 
     @Override
     public TileOrRoomConnector getWestNeighbor() {
         return _west;
     }
 
 	@Override
 	public float getHullBreachLevel() {
 		return _hullBreachLevel;
 	}
 
 	@Override
 	public boolean hasHullBreach() {
 		return _hullBreachLevel > NO_HULL_BREACH;
 	}
 
 	@Override
 	public void createHullBreach(float initialBreachValue) {
 		_hullBreachLevel = Math.min(MAX_HULL_BREACH, _hullBreachLevel + initialBreachValue);
 		_onFire = false;
 	}
 
 	@Override
 	public void repairHullBreach(float repairValue) {
 		_hullBreachLevel = Math.min(NO_HULL_BREACH, _hullBreachLevel - repairValue);
 	}
 	
 	private void updateHullBreach(float elapsedTime) {
         _room.consumeOxygen((_hullBreachLevel * HULL_BREACH_AIR_VENT_FACTOR * elapsedTime) / _room.getTiles().size());
     }
 
 	@Override
 	public boolean isOnFire() {
 		return _onFire;
 	}
 
 	@Override
 	public void ignite() {
 		if (!hasHullBreach()) {
 			_onFire = true;
 		}
 	}
 	
 	private void updateFire(float elapsedTime) {
	    float consumedOxygen = FIRE_OXYGEN_CONSUMPTION * elapsedTime;
 	    
 	    _room.consumeOxygen(consumedOxygen / _room.getTiles().size());
 	    
	    if (_room.getOxygenLevel() < FIRE_DIE_THRESHOLD) {
	    	_onFire = false;
	    }
	    else if (_room.getOxygenLevel() >= FIRE_SPREAD_OXYGEN_REQUIREMENT) {
 	        float spreadProbability = FIRE_SPREAD_PROBABILITY * elapsedTime;
 	        if (spreadProbability >= Math.random()) {
 	            Tile tile = getNeighboringTileNotOnFire();
 	            if (tile != null) {
 	                tile.ignite();
 	            }
 	        }
 	    }
 	}
 	
 	private Tile getNeighboringTileNotOnFire() {
 	    for (Tile tile : _neightboringTiles)
 	        if (!tile.isOnFire())
 	            return tile;
 	    return null;
 	}
 }
