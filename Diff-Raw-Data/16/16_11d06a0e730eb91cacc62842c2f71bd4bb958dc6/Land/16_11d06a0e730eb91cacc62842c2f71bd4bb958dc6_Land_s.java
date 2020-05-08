 package yuuki.world;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import yuuki.util.ElementGrid;
 import yuuki.util.Grid;
 
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
 	 * The bumps that happened on the last advancement.
 	 */
 	private Map<Movable, Movable> bumps;
 	
 	/**
 	 * The transfers that are waiting to come in.
 	 */
 	private List<Movable> incomingResidents;
 	
 	/**
 	 * The name of this Land.
 	 */
 	private String name;
 	
 	/**
 	 * The residents that were transfered out in the last advancement.
 	 */
 	private List<Movable> outgoingResidents;
 	
 	/**
 	 * The position that the player starts at.
 	 */
 	private Point playerStart;
 	
 	/**
 	 * The Portals that link this Land to different areas.
 	 */
 	private Map<Point, Portal> portals;
 	
 	/**
 	 * The Movable objects in this Land.
 	 */
 	private List<Movable> residents;
 	
 	/**
 	 * The tiles that make up this Land.
 	 */
 	private Grid<Tile> tiles;
 	
 	/**
 	 * Creates a new Land.
 	 * 
 	 * @param name The name of this Land.
 	 * @param size The size of this Land, in tiles.
 	 * @param start The player start for this land.
 	 * @param tileData The tiles that makes up this Land.
 	 */
 	public Land(String name, Dimension size, Point start, Tile[] tileData) {
 		this.name = name;
 		playerStart = start;
 		tiles = new ElementGrid<Tile>(size, tileData);
 		residents = new ArrayList<Movable>();
 		portals = new HashMap<Point, Portal>();
 		incomingResidents = new ArrayList<Movable>();
 		outgoingResidents = new ArrayList<Movable>();
 		bumps = new HashMap<Movable, Movable>();
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
 		if (!hasOccupantAt(pos)) {
 			residents.add(r);
 			tiles.itemAt(pos).setOccupied(true);
 		}
 	}
 	
 	/**
 	 * Advances this Land by one tick. All residents are queried for where they
 	 * wish to move, and if they make a valid request, they are moved to where
 	 * they requested.
 	 */
 	public void advance() {
 		processIncomingResidents();
 		moveResidents();
 		processOutgoingResidents();
 	}
 	
 	/**
 	 * Gets the resident that another resident bumped into during the last
 	 * advancement.
 	 * 
 	 * @param resident The resident who bumped into the other.
 	 * 
 	 * @return The resident that the given resident bumped into, or null if
 	 * there wasn't one.
 	 */
 	public Movable getLastBump(Movable resident) {
 		return bumps.get(resident);
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
 	 * Gets the player start.
 	 */
 	public Point getPlayerStart() {
 		return playerStart;
 	}
 	
 	/**
 	 * Gets the portals in this Land.
 	 * 
 	 * @return The portals.
 	 */
 	public ArrayList<Portal> getPortals() {
 		return new ArrayList<Portal>(portals.values());
 	}
 	
 	/**
 	 * Gets the residents in this Land.
 	 * 
 	 * @return The residents.
 	 */
 	public ArrayList<Movable> getResidents() {
 		return new ArrayList<Movable>(residents);
 	}
 	
 	/**
 	 * Gets the size of this Land in tiles.
 	 * 
 	 * @return The size.
 	 */
 	public Dimension getSize() {
 		return tiles.getSize();
 	}
 	
 	/**
 	 * Gets the tiles that make up this Land.
 	 * 
 	 * @return The tiles.
 	 */
 	public Grid<Tile> getTiles() {
 		return tiles;
 	}
 	
 	/**
 	 * Gets the list of residents who transfered out during the last
 	 * advancement.
 	 * 
 	 * @return The list of residents.
 	 */
 	public List<Movable> getTransfers() {
 		return outgoingResidents;
 	}
 	
 	/**
 	 * Gets the WalkGraph for a given point.
 	 * 
 	 * @param center The coordinates of the center of the WalkGraph.
 	 * @param occupied Whether to include occupied tiles.
 	 * 
 	 * @return The WalkGraph for the given point.
 	 */
 	public WalkGraph getWalkGraph(Point center, boolean occupied) {
 		Point graphCenter = new Point(center.x - 1, center.y - 1);
 		Dimension size = new Dimension(3, 3);
 		Rectangle box = new Rectangle(graphCenter, size);
 		Grid<Tile> grid = tiles.getSubGrid(box);
 		WalkGraph graph = new WalkGraph(center, grid, occupied);
 		return graph;
 	}
 	
 	/**
 	 * Checks whether a tile is occupied by a resident.
 	 * 
 	 * @param location The point to check.
 	 * 
 	 * @return True if the tile at the given point has a resident standing on
 	 * it; otherwise, false.
 	 */
 	public boolean hasOccupantAt(Point location) {
 		Tile toCheck = tiles.itemAt(location);
 		return toCheck.isOccupied();
 	}
 	
 	/**
 	 * Gets the portal at a specific location.
 	 * 
 	 * @param p The point that the portal is at.
 	 * 
 	 * @return The portal at the given location, or null if there is no portal.
 	 */
 	public Portal portalAt(Point p) {
 		return portals.get(p);
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
 		residents.remove(r);
 		tiles.itemAt(r.getLocation()).setOccupied(false);
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
 		Point p = new Point();
 		Dimension d = tiles.getSize();
 		for (p.y = 0; p.y < d.height; p.y++) {
 			for (p.x = 0; p.x < d.width; p.x++) {
 				strVersion.append(tiles.itemAt(p).getDisplayChar());
 			}
 			if (p.x < d.height - 1) {
 				strVersion.append('\n');
 			}
 		}
 		return strVersion.toString();
 	}
 	
 	/**
 	 * Transfers in a resident to a specific point.
 	 * 
 	 * @param r The resident to transfer in.
 	 * @param p The point to transfer to.
 	 */
 	public void transferInResident(Movable r, Point p) {
 		r.setLocation(p);
 		if (!hasOccupantAt(p)) {
 			addResident(r);
 		} else {
 			incomingResidents.add(r);
 		}
 	}
 	
 	/**
 	 * Gets the occupant at a location.
 	 * 
 	 * @param p The point to get the occupant from.
 	 * 
 	 * @return The resident occupying the given location, or null if there
 	 * isn't one.
 	 */
 	private Movable getOccupantAt(Point p) {
 		Movable occupant = null;
 		for (Movable r : residents) {
 			if (r.getLocation().equals(p)) {
 				occupant = r;
 				break;
 			}
 		}
 		return occupant;
 	}
 	
 	/**
 	 * Moves all residents. Each resident is asked where it wishes to move, and
 	 * is then moved there.
 	 */
 	private void moveResidents() {
 		bumps.clear();
 		for (Movable r : residents) {
 			Point current = r.getLocation();
 			Point destination = r.getNextMove(this);
			if (!hasOccupantAt(destination)) {
				tiles.itemAt(current).setOccupied(false);
				tiles.itemAt(destination).setOccupied(true);
				r.setLocation(destination);
			} else {
				Movable bumpee = getOccupantAt(destination);
				bumps.put(r, bumpee);
 			}
 		}
 	}
 	
 	/**
 	 * Adds waiting incoming transfers if they can be added.
 	 */
 	private void processIncomingResidents() {
 		Movable[] incoming = incomingResidents.toArray(new Movable[0]);
 		incomingResidents.clear();
 		for (Movable m : incoming) {
 			transferInResident(m, m.getLocation());
 		}
 	}
 	
 	/**
 	 * Moves transferable residents that have stepped on a portal.
 	 */
 	private void processOutgoingResidents() {
 		outgoingResidents.clear();
 		Iterator<Movable> it = residents.iterator();
 		Movable resident;
 		while (it.hasNext()) {
 			resident = it.next();
 			Point p = resident.getLocation();
 			if (portals.containsKey(p) && resident.isTransferrable()) {
 				tiles.itemAt(p).setOccupied(false);
 				outgoingResidents.add(resident);
 				it.remove();
 			}
 		}
 	}
 	
 }
