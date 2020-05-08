 package yuuki.world;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Holds all data for a particular land in the world. In addition to their tile
  * data, Lands hold entities that have a specific location in the Land. All of
  * these entities implement the interface yuuki.world.Locatable, and there are
  * several types of entities.
  * 
  * A resident is a Locatable that moves from tile to tile around the Land on
  * every turn. Residents do not have a specific type, but all residents
  * implement the yuuki.world.Movable interface.
  * 
  * A portal is a Locatable that occupies a tile and serves to link the Land it
  * is in with another. All portals are of the type Portal.
  */
 public class Land {
 	
 	/**
 	 * The name of this Land.
 	 */
 	private String name;
 	
 	/**
 	 * The Portals that link this Land to different areas.
 	 */
 	private Map<Point, Portal> portals;
 	
 	/**
 	 * The Movable objects in this Land.
 	 */
 	private Map<Point, Movable> residents;
 	
 	/**
 	 * The tiles that make up this Land.
 	 */
 	private TileGrid tiles;
 	
 	/**
 	 * Creates a new Land.
 	 * 
 	 * @param name The name of this Land.
 	 * @param width The width, in tiles, of this Land.
 	 * @param height The height, in tiles, of this Land.
 	 * @param tileData The tiles that makes up this Land.
 	 */
 	public Land(String name, int width, int height, Tile[] tileData) {
 		this.name = name;
 		tiles = new TileGrid(width, height, tileData);
 		residents = new HashMap<Point, Movable>();
 		portals = new HashMap<Point, Portal>();
 	}
 	
 	/**
 	 * Adds a Portal to this Land if it has not already been added.
 	 * 
 	 * @param p The Portal to add.
 	 */
 	public void addPortal(Portal p) {
 		Point pos = p.getLocation();
 		if (portals.get(pos) == null) {
 			portals.put(pos, p);
 		}
 	}
 	
 	/**
 	 * Adds a resident to this Land if it has not already been added.
 	 * 
 	 * @param r The resident to add.
 	 */
 	public void addResident(Movable r) {
 		Point pos = r.getLocation();
 		if (residents.get(pos) == null) {
 			residents.put(pos, r);
 		}
 	}
 	
 	/**
 	 * Advances this Land by one tick. All residents are queried for where they
 	 * wish to move, and if they make a valid request, they are moved to where
 	 * they requested.
 	 */
 	public void advance() {
 		moveResidents();
 	}
 	
 	/**
 	 * Gets the height of this Land in tiles.
 	 * 
 	 * @return The height.
 	 */
 	public int getHeight() {
 		return tiles.getHeight();
 	}
 	
 	/**
 	 * Gets all the Locatables that occupy this Land.
 	 * 
 	 * @return The Locatables.
 	 */
 	public ArrayList<Locatable> getLocatables() {
 		ArrayList<Locatable> ls = new ArrayList<Locatable>();
 		ls.addAll(portals.values());
 		ls.addAll(residents.values());
 		return ls;
 	}
 	
 	/**
 	 * Gets the name of this Land.
 	 * 
 	 * @return The name of this Land.
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * Gets the tiles that make up this Land.
 	 * 
 	 * @return The tiles.
 	 */
 	public TileGrid getTiles() {
 		return tiles;
 	}
 	
 	/**
 	 * Gets the WalkGraph for a given point.
 	 * 
 	 * @param center The coordinates of the center of the WalkGraph.
 	 * 
 	 * @return The WalkGraph for the given point.
 	 */
 	public WalkGraph getWalkGraph(Point center) {
 		Point p = center;
 		TileGrid grid = tiles.getSubGrid(p.x - 1, p.y - 1, 3, 3);
 		WalkGraph graph = new WalkGraph(p, grid);
 		return graph;
 	}
 	
 	/**
 	 * Gets the height of this Land in tiles.
 	 * 
 	 * @return The height.
 	 */
 	public int getWidth() {
 		return tiles.getWidth();
 	}
 	
 	/**
 	 * Checks whether a tile is occupied by a resident.
 	 * 
 	 * @param location The point to check.
 	 * 
 	 * @return True if the tile at the given point has a resident standing on
 	 * it; otherwise, false.
 	 */
 	public boolean isOccupied(Point location) {
 		Tile toCheck = tiles.tileAt(location.x, location.y);
 		return toCheck.isOccupied();
 	}
 	
 	/**
 	 * Removes a Portal from this Land.
 	 * 
 	 * @param p The Portal to remove.
 	 */
 	public void removePortal(Portal p) {
 		portals.remove(p.getLocation());
 	}
 	
 	/**
 	 * Removes a resident from this Land.
 	 * 
 	 * @param r The resident to remove.
 	 */
 	public void removeResident(Movable r) {
 		residents.remove(r.getLocation());
 	}
 	
 	/**
 	 * Gets a String representation of this Land. This will be a .lnd format
 	 * String containing all meta data and tiles.
 	 * 
 	 * @return The String representation of this Land.
 	 */
 	@Override
 	public String toString() {
 		StringBuilder strVersion = new StringBuilder();
 		for (int i = 0; i < tiles.getHeight(); i++) {
 			for (int j = 0; j < tiles.getWidth(); j++) {
 				strVersion.append(tiles.tileAt(j, i).getDisplayChar());
 			}
			if (i < tiles.getHeight() - 1) {
				strVersion.append('\n');
			}
 		}
 		return strVersion.toString();
 	}
 	
 	/**
 	 * Clears the resident map and moves all residents to their new positions.
 	 * 
 	 * @param moveList An array containing the updated residents.
 	 */
 	private void applyMove(Movable[] moveList) {
 		residents.clear();
 		for (Movable r: moveList) {
 			residents.put(r.getLocation(), r);
 		}
 	}
 	
 	/**
 	 * Moves all residents. Each resident is asked where it wishes to move, and
 	 * is then moved there.
 	 */
 	private void moveResidents() {
 		Movable[] moveList = residents.values().toArray(new Movable[0]);
 		for (Movable r: moveList) {
 			Point destination = r.getNextMove(this);
 			if (destination != null) {
 				r.setLocation(destination);
 			}
 		}
 		applyMove(moveList);
 	}
 	
 }
