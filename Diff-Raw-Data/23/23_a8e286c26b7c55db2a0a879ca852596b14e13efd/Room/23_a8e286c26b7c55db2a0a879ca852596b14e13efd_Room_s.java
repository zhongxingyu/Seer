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
 package de.uniluebeck.imis.casi.simulation.model;
 
 import java.awt.Polygon;
 import java.awt.Shape;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 /**
  * This class is a presentation of rooms in the simulation. Furthermore it
  * provides additional functionalities for dealing with rooms.
  * 
  * @author Tobias Mende
  * 
  */
 public class Room implements IPosition {
 	/** the development logger */
 	private static final Logger log = Logger.getLogger(Room.class.getName());
 	/** serialization identifier */
 	private static final long serialVersionUID = 112593179870431369L;
 	/** the collection of walls this room consists of */
 	private List<Wall> walls = new LinkedList<Wall>();
 	/** A polygon that represents the shape of this room */
	private Polygon polygonRepresentation;
 	/** all doors in this room */
 	private Set<Door> doorsInRoom;
 	/** the unique identifier */
 	private String identifier;
 	/** the instance counter */
 	private static int idCounter;
 	
 	/**
 	 * Default constructor
 	 */
 	public Room() {
 		super();
 		identifier = "room-"+idCounter;
 		idCounter++;
 	}
 
 	@Override
 	public Point2D getCoordinates() {
 		/**
 		 * returns the coordinates of the first wall
 		 */
 		return walls.get(0).getCoordinates();
 	}
 
 	@Override
 	public Point2D getCentralPoint() {
 		Rectangle2D bounds = getShapeRepresentation().getBounds2D();
 		Point2D middleCandidate = new Point2D.Double(bounds.getCenterX(),
 				bounds.getCenterY());
 		if (getShapeRepresentation().contains(middleCandidate)) {
 			return middleCandidate;
 		}
 		// If middle candidate is wrong, give central point of first door
 		Door firstDoor = getFirstDoor();
 		if (firstDoor != null) {
 			return firstDoor.getCentralPoint();
 		}
 		return null;
 	}
 
 	/**
 	 * Method for getting the first door in this room
 	 * 
 	 * @return the first door or {@code null} if the room has no doors.
 	 */
 	private Door getFirstDoor() {
 		for (Wall w : walls) {
 			if (w.hasDoors()) {
 				return w.getDoors().get(0);
 			}
 		}
 		log.warning("This room has no doors");
 		return null;
 	}
 
 	/**
 	 * Getter for all doors in this room
 	 * 
 	 * @return a set of doors
 	 */
 	public Set<Door> getDoors() {
 		if(doorsInRoom == null) {
 			doorsInRoom = new HashSet<Door>();
 			for (Wall w : walls) {
 				doorsInRoom.addAll(w.getDoors());
 			}
 		}
 		return doorsInRoom;
 	}
 
 	@Override
 	public Shape getShapeRepresentation() {
 		if (polygonRepresentation == null) {
 			List<Point2D> points = getWallPoints();
 			// Adding startpoint again for providing a closed path
 			points.add(walls.get(0).getStartPoint());
 			int[] x = new int[points.size()];
 			int[] y = new int[points.size()];
 			for (int i = 0; i < x.length; i++) {
 				x[i] = (int) points.get(i).getX();
 				y[i] = (int) points.get(i).getY();
 			}
 			polygonRepresentation = new Polygon(x, y, x.length);
 		}
 		return polygonRepresentation;
 	}
 
 	/**
 	 * Adds a wall to the room
 	 * 
 	 * @param w
 	 *            the wall to add
 	 */
 	public void addWall(Wall w) {
 		if (!walls.contains(w)) {
 			walls.add(w);
 			invalidatePolygonRepresentation();
 			doorsInRoom = null;
 		}
 
 	}
 
 	/**
 	 * Getter for all walls of this room
 	 * @return a list of walls
 	 */
 	public List<Wall> getWalls() {
 		return walls;
 	}
 
 	/**
 	 * Calculates list of points which can be used for generating a polygon
 	 * representation This method expects that the walls are added as they are
 	 * connected.
 	 * 
 	 * @return an ordered list of node points
 	 */
 	private List<Point2D> getWallPoints() {
 		List<Point2D> points = new LinkedList<Point2D>();
 		for (Wall w : walls) {
 			if (!points.contains(w.getStartPoint())) {
 				points.add(w.getStartPoint());
 			}
 			if (!points.contains(w.getEndPoint())) {
 				points.add(w.getEndPoint());
 			}
 		}
 		return points;
 	}
 
 	/**
 	 * Deletes the polygon representation
 	 */
 	private void invalidatePolygonRepresentation() {
 		polygonRepresentation = null;
 	}
 
 	@Override
 	public boolean contains(IPosition position) {
 		return contains(position.getCoordinates());
 	}
 
 	@Override
 	public boolean contains(Point2D point) {
 		if(getShapeRepresentation().contains(point)) {
 			return true;
 		}
 		for(Door d : getDoors()) {
 			if(d.contains(point)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	
 	/**
 	 * @param identifier the identifier to set
 	 */
 	public void setIdentifier(String identifier) {
 		this.identifier = identifier;
 	}
 
 	@Override
 	public String toString() {
 		return identifier;
 	}
 	
 	/**
 	 * Getter for the room identifier
 	 */
 	public String getIdentifier() {
 		return identifier;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((doorsInRoom == null) ? 0 : doorsInRoom.hashCode());
 		result = prime * result
 				+ ((identifier == null) ? 0 : identifier.hashCode());
 		result = prime * result + ((walls == null) ? 0 : walls.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Room other = (Room) obj;
 		if (doorsInRoom == null) {
 			if (other.doorsInRoom != null)
 				return false;
 		} else if (!doorsInRoom.equals(other.doorsInRoom))
 			return false;
 		if (identifier == null) {
 			if (other.identifier != null)
 				return false;
 		} else if (!identifier.equals(other.identifier))
 			return false;
 		if (walls == null) {
 			if (other.walls != null)
 				return false;
 		} else if (!walls.equals(other.walls))
 			return false;
 		return true;
 	}
 	
 	
 
 }
