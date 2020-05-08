 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2012  Moritz Bürger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi.simulation.factory;
 
 import java.awt.geom.Point2D;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import de.uniluebeck.imis.casi.simulation.engine.SimulationEngine;
 import de.uniluebeck.imis.casi.simulation.model.AbstractInteractionComponent;
 import de.uniluebeck.imis.casi.simulation.model.Door;
 import de.uniluebeck.imis.casi.simulation.model.Room;
 import de.uniluebeck.imis.casi.simulation.model.Wall;
 
 /**
  * The WorldFactory holds world objects and is able generate new objects
  * 
  * @author Tobias Mende
  * 
  */
 public class WorldFactory {
 	/** The development logger */
 	private static final Logger log = Logger.getLogger(WorldFactory.class
 			.getName());
 	/** A set of doors, that are used in the simulation */
 	private static Map<Integer, Door> doors = new HashMap<Integer, Door>();
 
 	/**
 	 * Method for searching a door with a given identifier
 	 * 
 	 * @param identifier
 	 *            the identifier
 	 * @return the door, if it was found, <code>null</code> otherwise.
 	 */
 	public static Door findDoorForIdentifier(String identifier) {
 		for (Door door : doors.values()) {
 			if (door.getIdentifier().equals(identifier)) {
 				return door;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Method for searching a door with a given int identifier
 	 * 
 	 * @param identifier
 	 *            the identifier
 	 * @return the door or <code>null</code> if no door was found
 	 */
 	public static Door findDoorForIdentifier(int identifier) {
 		return doors.get(identifier);
 	}
 
 	/**
 	 * Method for adding a door
 	 * 
 	 * @param door
 	 *            the door
 	 */
 	public static void addDoor(Door door) {
 		doors.put(door.getIntIdentifier(), door);
 	}
 
 	/**
 	 * Method for getting a room which have exactly the provided doors
 	 * 
 	 * @param doors
 	 *            a set of doors
 	 * @return the room or <code>null</code> if no room was found
 	 */
 	public static Room getRoomWithDoors(Set<Door> doors) {
 		try {
 			Set<Room> rooms = SimulationEngine.getInstance().getWorld()
 					.getRooms();
 			for (Room room : rooms) {
 				if (room.getWalls().equals(doors)) {
 					return room;
 				}
 			}
 		} catch (IllegalAccessException e) {
 			log.severe("World isn't sealed. This should not happen! "
 					+ e.fillInStackTrace());
 		}
 		return null;
 	}
 
 	/**
 	 * Method for getting a set of rooms which contain the provided doors.
 	 * 
 	 * @param doors
 	 *            the doors to search rooms for
 	 * @return a set of rooms containing the doors or an empty set, if no room
 	 *         was found.
 	 */
 	public static Set<Room> getRoomsWithDoors(Set<Door> doors) {
 		Set<Room> results = new HashSet<Room>();
 		try {
 			Set<Room> rooms = SimulationEngine.getInstance().getWorld()
 					.getRooms();
 			for (Room room : rooms) {
 				if (room.getDoors().containsAll(doors)) {
 					results.add(room);
 				}
 			}
 		} catch (IllegalAccessException e) {
 			log.severe("World isn't sealed. This should not happen! "
 					+ e.fillInStackTrace());
 		}
 		return results;
 	}
 
 	/**
 	 * Method for getting a room that contains a specific point
 	 * 
 	 * @param point
 	 *            the point to get a room for
 	 * @return a room containing the provided point or <code>null</code> if no
 	 *         room was found.
 	 */
 	public static LinkedList<Room> getRoomsWithPoint(final Point2D point) {
 		Collection<Room> rooms;
 		LinkedList<Room> result = new LinkedList<Room>();
 		try {
 			rooms = SimulationEngine.getInstance().getWorld().getRooms();
 			for (Room r : rooms) {
 				if (r.contains(point)) {
 					result.add(r);
 				}
 			}
 		} catch (IllegalAccessException e) {
 			log.severe("Method should not be called before sealing the world. "
 					+ e.fillInStackTrace());
 		}
 		return result;
 	}
 
 	/**
 	 * Method for getting a room with a provided collection of walls. Searches
 	 * for a room that contains these walls.
 	 * 
 	 * @param walls
 	 *            the walls to search the room for
 	 * @return the room, if one was found or <code>null</code> otherwise.
 	 */
 	public static Room getRoomWithWalls(Collection<Wall> walls) {
 		Set<Room> allRooms;
 		try {
 			allRooms = SimulationEngine.getInstance().getWorld().getRooms();
 			for (Room room : allRooms) {
 				if (room.getWalls().containsAll(walls)) {
 					return room;
 				}
 			}
 		} catch (IllegalAccessException e) {
 			log.severe("Method should not be called before sealing the world. "
 					+ e.fillInStackTrace());
 		}
 		return null;
 	}
 
 	public static Room getRoomWithPoints(Point2D first, Point2D second) {
 		Set<Room> allRooms;
 		try {
 			allRooms = SimulationEngine.getInstance().getWorld().getRooms();
 			for (Room room : allRooms) {
 				if (room.contains(first) && room.contains(second)) {
 					return room;
 				}
 			}
 		} catch (IllegalAccessException e) {
 			log.severe("Method should not be called before sealing the world. "
 					+ e.fillInStackTrace());
 		}
 		return null;
 	}
 
 	/**
 	 * Gets a list of actuators and sensors that may be interested in actions
 	 * occurring at a given position
 	 * 
 	 * @param coordinates
 	 *            the position
 	 * @return a list of actuators and sensors which monitor this point.
 	 */
 	public static LinkedList<AbstractInteractionComponent> getInteractionComponentsForPosition(
 			Point2D coordinates) {
 		LinkedList<AbstractInteractionComponent> components = new LinkedList<AbstractInteractionComponent>();
 		Set<AbstractInteractionComponent> allComponents;
 		try {
 			allComponents = SimulationEngine.getInstance().getWorld()
 					.getInteractionComponents();
 			for (AbstractInteractionComponent comp : allComponents) {
 				if (comp.contains(coordinates)) {
 					components.add(comp);
 				}
 			}
 		} catch (IllegalAccessException e) {
 			log.severe("Don't call this method before sealing the world!");
 		}
 		return components;
 	}
 
 	/**
 	 * Searches a door with a provided point
 	 * 
 	 * @param point
 	 *            the point to search a door for
 	 * @return the door which's shape contains this point or {@code null} if no
 	 *         door was found.
 	 */
 	public static Door getDoorWithPoint(Point2D point) {
 		for (Door d : doors.values()) {
			if (d.getShapeRepresentation().ptSegDist(point) == 0) {
 				return d;
 			}
 		}
 		return null;
 	}
 
 }
