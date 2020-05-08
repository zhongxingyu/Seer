 package de.xftl.model.ships;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import de.xftl.spec.model.Point;
 import de.xftl.spec.model.crew.CrewMember;
 import de.xftl.spec.model.ships.OxygenLevel;
 import de.xftl.spec.model.ships.Room;
 import de.xftl.spec.model.ships.RoomConnector;
 import de.xftl.spec.model.ships.Tile;
 import de.xftl.spec.model.ships.TileUnit;
 import de.xftl.spec.model.ships.TileUnitPositioned;
 
 public class BasicRoom implements Room {
 
 	private Point<TileUnit> _leftUpperCornerPos;
 	private System _system;
 	private List<Tile> _tiles;
 	private OxygenLevel _oxygenLevel;
 	
 	public BasicRoom(List<Tile> tiles) {
 	    super();
 	    
 	    _tiles = tiles;
 	    determineLeftUpperCornerPos();
 	}
 
 	private void determineLeftUpperCornerPos() {
         List<Tile> tiles = new ArrayList<>(_tiles);
         Collections.sort(tiles);
         
         _leftUpperCornerPos = tiles.get(0).getLeftUpperCornerPos();
     }
 
     @Override
 	public void update(float elapsedTime) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Point<TileUnit> getLeftUpperCornerPos() {
 		return _leftUpperCornerPos;
 	}
 
 	@Override
 	public System getSystem() {
 		return _system;
 	}
 
 	@Override
 	public List<Tile> getTiles() {
 		return _tiles;
 	}
 
 	@Override
 	public List<CrewMember> getCrewMembers() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<RoomConnector> getRoomConnectors() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<Room> getAdjacentRooms() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<Room> getAdjacentRooms(Room origin) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public OxygenLevel getOxygenLevel() {
 		return _oxygenLevel;
 	}
 
 	@Override
     public int compareTo(TileUnitPositioned o) {
         return _leftUpperCornerPos.compareTo(o.getLeftUpperCornerPos());
     }
 }
