 package yuuki.world;
 
 import java.awt.Point;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import yuuki.content.Mergeable;
 import yuuki.util.Grid;
 import yuuki.util.InvalidIndexException;
 
 /**
  * Handles overworld navigation and data. The World class is responsible for
  * loading land data, keeping track of entities on the current land, and
  * modifying the current land.
  */
 public class World implements Mergeable<Map<String, Land>> {
 	
 	/**
 	 * The land currently being controlled by this World.
 	 */
 	private Land activeLand;
 	
 	/**
 	 * All lands loaded, indexed by land name.
 	 */
 	private Map<String, Deque<Land>> lands;
 	
 	/**
 	 * Creates a new, empty World.
 	 */
 	public World() {
 		lands = new HashMap<String, Deque<Land>>();
 	}
 	
 	/**
 	 * Adds a new Land to this world.
 	 * 
 	 * @param land The Land to add.
 	 */
 	public void addLand(Land land) {
 		Deque<Land> d = lands.get(land.getName());
 		if (d == null) {
 			d = new ArrayDeque<Land>();
 			lands.put(land.getName(), d);
 		}
 		d.push(land);
 	}
 	
 	@Override
 	public void merge(Map<String, Land> content) {
 		for (Land l : content.values()) {
 			addLand(l);
 		}
 	}
 	
 	@Override
 	public void subtract(Map<String, Land> content) {
 		for (Land land : content.values()) {
 			Deque<Land> d = lands.get(land.getName());
 			if (d != null) {
 				d.remove(land);
 				if (d.isEmpty()) {
					lands.remove(land.getName());
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Adds a resident to the active land.
 	 * 
 	 * @param resident The resident to add.
 	 */
 	public void addResident(Movable resident) {
 		activeLand.addResident(resident);
 	}
 	
 	/**
 	 * Advances the world by one tick. The current Land is instructed to
 	 * advance.
 	 * 
 	 * @throws InterruptedException If the current thread is interrupted while
 	 * waiting for a move from the player.
 	 */
 	public void advance() throws InterruptedException {
 		activeLand.advance();
 		moveTransfers();
 	}
 	
 	/**
 	 * Changes the active land.
 	 * 
 	 * @param landName The name of the land to switch to.
 	 * @throws InvalidIndexException If the given name does not refer to an
 	 * existing Land.
 	 */
 	public void changeLand(String landName) throws InvalidIndexException {
 		activeLand = getLand(landName);
 	}
 	
 	/**
 	 * Gets the names of all lands in this World.
 	 * 
 	 * @return An array containing the names of all loaded Lands.
 	 */
 	public String[] getAllLandNames() {
 		Set<String> nameSet = lands.keySet();
 		String[] names = new String[nameSet.size()];
 		nameSet.toArray(names);
 		return names;
 	}
 	
 	/**
 	 * Gets the name of the current Land.
 	 * 
 	 * @return The name of the current land.
 	 */
 	public String getLandName() {
 		return activeLand.getName();
 	}
 	
 	/**
 	 * Gets the resident that another resident bumped into during the last
 	 * world advancement.
 	 * 
 	 * @param resident The resident who bumped into another.
 	 * 
 	 * @return The resident that the given resident bumped into, or null if the
 	 * given resident did not bump into another.
 	 */
 	public Movable getLastBump(Movable resident) {
 		return activeLand.getLastBump(resident);
 	}
 	
 	/**
 	 * Gets the player start for the current land.
 	 * 
 	 * @return The player start.
 	 */
 	public Point getPlayerStart() {
 		return activeLand.getPlayerStart();
 	}
 	
 	/**
 	 * Gets the portals in the current land.
 	 * 
 	 * @return The portals.
 	 */
 	public ArrayList<Portal> getPortals() {
 		return activeLand.getPortals();
 	}
 	
 	/**
 	 * Gets the residents in the current land.
 	 * 
 	 * @return The residents.
 	 */
 	public ArrayList<Movable> getResidents() {
 		return activeLand.getResidents();
 	}
 	
 	/**
 	 * Gets the tiles in the current Land.
 	 * 
 	 * @return The grid of tiles that makes up the current Land.
 	 */
 	public Grid<Tile> getTiles() {
 		return activeLand.getTiles();
 	}
 	
 	/**
 	 * Removes a resident from the active land.
 	 * 
 	 * @param resident The resident to remove.
 	 */
 	public void removeResident(Movable resident) {
 		activeLand.removeResident(resident);
 	}
 	
 	/**
 	 * Finalizes the transfers out of the active land and moves them to the
 	 * land that they are transferring to.
 	 */
 	private void moveTransfers() {
 		List<Movable> moves = activeLand.getTransfers();
 		for (Movable m : moves) {
 			Portal p = activeLand.portalAt(m.getLocation());
 			Land destination;
 			try {
 				destination = getLand(p.getLinkedLand());
 			} catch (InvalidIndexException e) {
 				throw new InvalidLinkNameException(p.getLinkedLand());
 			}
 			destination.transferInResident(m, p.getLink());
 		}
 	}
 	
 	/**
 	 * Gets a Land object by name.
 	 * 
 	 * @param name The name of the Land object.
 	 * @return The Land referred to by the name.
 	 * @throws InvalidIndexException If the name does not refer to an existing
 	 * Land.
 	 */
 	private Land getLand(String name) throws InvalidIndexException {
 		Deque<Land> landDeque = lands.get(name);
 		if (landDeque == null) {
 			throw new InvalidIndexException(name);
 		}
 		Land l = landDeque.peek();
 		return l;
 	}
 	
 }
