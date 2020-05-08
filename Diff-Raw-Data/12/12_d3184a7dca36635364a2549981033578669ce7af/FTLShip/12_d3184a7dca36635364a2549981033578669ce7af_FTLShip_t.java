 package com.kartoflane.superluminal.elements;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 
 import com.kartoflane.superluminal.core.Main;
 
 /**
  * Class representing the ship.
  */
 public class FTLShip implements Serializable {
 	private static final long serialVersionUID = 344200154077072015L;
 	/** Size of the anchor handle */
 	final static public int ANCHOR = 12;
 
 	/** The most recent designation */
 	final public static int VERSION = 14;
 	/** Ship's own designation */
 	public int version = 0;
 
 	public boolean isPlayer;
 	public boolean weaponsBySet;
 	public boolean dronesBySet;
 
 	public Set<FTLRoom> rooms;
 	public Set<FTLDoor> doors;
 	public List<FTLMount> mounts;
 	public List<FTLGib> gibs;
 
 	// === Images
 	public String imagePath;
 	public String shieldPath;
 	public String floorPath;
 	public String cloakPath;
 	public String miniPath;
 
 	/** Use Main.hullBox.isPinned() instead */
 	@Deprecated
 	public boolean hullPinned;
 	/** Use Main.shieldBox.isPinned() instead */
 	@Deprecated
 	public boolean shieldPinned;
 
 	/** Posiition of the ship on canvas (anchor's x and y) */
 	public Point anchor;
 
 	// === FTL in-game ship information
 	public Point offset;
 	public Rectangle ellipse;
 	public Rectangle imageRect;
 	public String blueprintName;
 	public String shipClass;
 	public String shipName;
 	public String layout;
 	public String imageName;
 	public String descr;
 
 	public int vertical = 0;
 	public int horizontal = 0;
 
 	public int hullHealth = 0;
 	public int reactorPower = 0;
 	public int weaponSlots = 0;
 	public int droneSlots = 0;
 
 	// === player specific
 	public int weaponCount = 0;
 	public int droneCount = 0;
 	/** Override functionality was removed completely */
 	@Deprecated
 	public String cloakOverride;
 	/** Override functionality was removed completely */
 	@Deprecated
 	public String shieldOverride;
 
 	// === enemy specific
 	/** Kept for compatibility with earlier projects */
 	@Deprecated
 	public int crewMax = 8;
 	public int minSec = 0;
 	public int maxSec = 0;
 
 	// ===
 	public int missiles = 0;
 	public int drones = 0;
 
 	public LinkedList<String> weaponSet;
 	public LinkedList<String> droneSet;
 	public LinkedList<String> augmentSet;
 	public HashMap<String, Integer> crewMap;
 	/** Only used in enemy ships */
 	public HashMap<String, Integer> crewMaxMap;
 	public HashMap<Systems, Integer> powerMap;
 	public HashMap<Systems, Integer> levelMap;
 	public HashMap<Systems, Boolean> startMap;
 	public HashMap<Systems, Integer> slotMap;
 	public HashMap<Systems, Slide> slotDirMap;
 
 	// public HashMap<Systems, FTLInterior> interiorMap;
 
 	public FTLShip() {
 		rooms = new HashSet<FTLRoom>();
 		doors = new HashSet<FTLDoor>();
 		mounts = new LinkedList<FTLMount>();
 		gibs = new LinkedList<FTLGib>();
 
 		weaponSet = new LinkedList<String>();
 		droneSet = new LinkedList<String>();
 		augmentSet = new LinkedList<String>();
 		crewMap = new HashMap<String, Integer>();
 		crewMaxMap = new HashMap<String, Integer>();
 		powerMap = new HashMap<Systems, Integer>();
 		levelMap = new HashMap<Systems, Integer>();
 		startMap = new HashMap<Systems, Boolean>();
 		slotMap = new HashMap<Systems, Integer>();
 		slotDirMap = new HashMap<Systems, Slide>();
 		// interiorMap = new HashMap<Systems, FTLInterior>();
 
 		minSec = 0;
 		maxSec = 0;
 
 		ellipse = new Rectangle(0, 0, 0, 0);
 		imageRect = new Rectangle(0, 0, 0, 0);
 		offset = new Point(0, 0);
 		anchor = new Point(0, 0);
 		vertical = 0;
 		hullHealth = 0;
 		reactorPower = 0;
 		weaponSlots = 0;
 		droneSlots = 0;
 		weaponCount = 0;
 		droneCount = 0;
 		crewMax = 8;
 		missiles = 0;
 		drones = 0;
 
 		hullPinned = false;
 		shieldPinned = false;
 		isPlayer = false;
 		weaponsBySet = false;
 		dronesBySet = false;
 		imageName = new String();
 
 		crewMap.put("human", 0);
 		crewMap.put("engi", 0);
 		crewMap.put("energy", 0);
 		crewMap.put("mantis", 0);
 		crewMap.put("slug", 0);
 		crewMap.put("rock", 0);
 		crewMap.put("crystal", 0);
 		crewMap.put("ghost", 0);
		crewMap.put("random", 0);
 
 		crewMaxMap.put("human", 0);
 		crewMaxMap.put("engi", 0);
 		crewMaxMap.put("energy", 0);
 		crewMaxMap.put("mantis", 0);
 		crewMaxMap.put("slug", 0);
 		crewMaxMap.put("rock", 0);
 		crewMaxMap.put("crystal", 0);
 		crewMaxMap.put("ghost", 0);
		crewMaxMap.put("random", 0);
 
 		for (Systems key : Systems.values()) {
 			levelMap.put(key, 1);
 			powerMap.put(key, 0);
 			startMap.put(key, true);
 
 			slotMap.put(key, -2);
 			slotDirMap.put(key, Slide.UP);
 		}
 	}
 
 	public FTLShip(int x, int y) {
 		this();
 		offset.x = x;
 		offset.y = y;
 	}
 
 	/**
 	 * Override the anchor and automatically snap it to the ship.
 	 * Generally not used.
 	 */
 	public void computeNewAnchor() {
 		Point p = null;
 
 		p = findLowBounds();
 		p.x = (p.x - offset.x * 35 >= 0) ? (p.x - offset.x * 35) : (offset.x * 35);
 		p.y = (p.y - offset.y * 35 >= 0) ? (p.y - offset.y * 35) : (offset.y * 35);
 
 		updateElements(p, AxisFlag.BOTH);
 		anchor = p;
 	}
 
 	/**
 	 * Update positions of elements of the ship (that's rooms, doors, etc) relative to the anchor.
 	 * Should be used in the following way:<br>
 	 * 
 	 * var1 = (new anchor for the ship);<br>
 	 * ship.updateElements(var1, AxisFlag);<br>
 	 * ship.anchor = var1;<br>
 	 * 
 	 * @param newAnchor
 	 *            the new anchor that the ship is going to use.
 	 * @param flag
 	 *            used to state which component of position is be updated.
 	 */
 	public void updateElements(Point newAnchor, AxisFlag flag) {
 		Point p;
 
 		for (FTLRoom r : rooms) {
 			p = r.getLocation();
 			r.setLocationAbsolute(p.x + newAnchor.x - anchor.x, p.y + newAnchor.y - anchor.y);
 		}
 		for (FTLDoor d : doors) {
 			p = d.getLocation();
 			d.setLocationAbsolute(p.x + newAnchor.x - anchor.x, p.y + newAnchor.y - anchor.y + 15); // (+15 <- fix for calculateOptimalOffset while anchor is in top left corner moving doors 1 grid up)
 		}
 		if (Main.ship != null) {
 			p = Main.hullBox.getLocation();
 			Main.hullBox.setLocation(p.x + newAnchor.x - anchor.x, p.y + newAnchor.y - anchor.y);
 			p = Main.shieldBox.getLocation();
 			Main.shieldBox.setLocation(p.x + newAnchor.x - anchor.x, p.y + newAnchor.y - anchor.y);
 		}
 		for (FTLMount m : mounts) {
 			p = m.getLocation();
 			m.setLocation(p.x + newAnchor.x - anchor.x, p.y + newAnchor.y - anchor.y);
 		}
 
 		anchor.x = newAnchor.x;
 		anchor.y = newAnchor.y;
 	}
 
 	/**
 	 * Computes the size of the ship (smallest rectangle in which the entire ship will fit), not the end point of the ship.
 	 * 
 	 * @return Point consisting of the ship's width and height.
 	 */
 	public Point computeShipSize() {
 		Point start = findLowBounds();
 		Point end = findHighBounds();
 
 		start.x = end.x - start.x;
 		start.y = end.y - start.y;
 		return start;
 	}
 
 	/**
 	 * Finds the low bounds of the ship (closest to top left corner).
 	 * 
 	 * @return Point consisting of lowest x and y values at which the ship's rooms start.
 	 */
 	public Point findLowBounds() {
 		int x = 2000, y = 2000;
 		for (FTLRoom r : rooms) {
 			if (r != null && r.getBounds().x <= x) {
 				x = r.getBounds().x;
 			}
 			if (r != null && r.getBounds().y <= y) {
 				y = r.getBounds().y;
 			}
 		}
 
 		x = (y == 10000) ? 0 : x;
 		y = (y == 10000) ? 0 : y;
 
 		return new Point(x, y);
 	}
 
 	/**
 	 * Finds the high bounds of the ship (closest to bottom right corner).
 	 * 
 	 * @return Point consisting of highest x and y values at which the ship's rooms end.
 	 */
 	public Point findHighBounds() {
 		int x = 0, y = 0;
 		for (FTLRoom r : rooms) {
 			if (r != null && r.getBounds().x + r.getBounds().width >= x) {
 				x = r.getBounds().x + r.getBounds().width;
 			}
 			if (r != null && r.getBounds().y + r.getBounds().height >= y) {
 				y = r.getBounds().y + r.getBounds().height;
 			}
 		}
 		return new Point(x, y);
 	}
 
 	public int findLeftRoom(FTLDoor d) {
 		for (FTLRoom r : rooms) {
 			if (r.getBounds().intersects(d.getBounds()) && ((d.horizontal && r.getBounds().y < d.getBounds().y) || (!d.horizontal && r.getBounds().x < d.getBounds().x))) {
 				return r.id;
 			}
 		}
 		return -1;
 	}
 
 	public int findRightRoom(FTLDoor d) {
 		for (FTLRoom r : rooms) {
 			if (r.getBounds().intersects(d.getBounds()) && ((d.horizontal && r.getBounds().y > d.getBounds().y) || (!d.horizontal && r.getBounds().x > d.getBounds().x))) {
 				return r.id;
 			}
 		}
 		return -1;
 	}
 
 	public FTLRoom getRoomWithId(int id) {
 		for (FTLRoom r : rooms) {
 			if (r != null && id != -1 && r.id == id) {
 				return r;
 			}
 		}
 		return null;
 	}
 
 	public FTLMount getMountWithIndex(int index) {
 		for (FTLMount m : mounts)
 			if (m.index == index)
 				return m;
 
 		return null;
 	}
 
 	public void reassignID() {
 		Main.idList.clear();
 		int i = 0;
 		for (FTLRoom r : rooms) {
 			r.id = i;
 			Main.idList.add(i);
 			i++;
 		}
 	}
 }
