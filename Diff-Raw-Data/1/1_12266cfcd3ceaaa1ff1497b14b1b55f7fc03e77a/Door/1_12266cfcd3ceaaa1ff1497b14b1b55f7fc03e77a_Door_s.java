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
 
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import de.uniluebeck.imis.casi.CASi;
 import de.uniluebeck.imis.casi.simulation.factory.WorldFactory;
 
 /**
  * The representation for a door that can be added to a wall
  * 
  * @author Tobias Mende
  * 
  */
 public class Door extends AbstractComponent {
 	/** Possible door states which are sensed by attached {@link DoorSensor}s */
 	public enum State {
 		/** The door is open */
 		OPEN,
 		/** The door is closed */
 		CLOSED,
 		/**
 		 * The door is locked. In this case agents have to perform a special
 		 * action to enter the room behind this door
 		 */
 		LOCKED
 	}
 
 	/** the development logger */
 	private static final Logger log = Logger.getLogger(Door.class.getName());
 	/** A prefix for the identifier of this door */
 	public static final String ID_PREFIX = "door-";
 	/** the id for serialization */
 	private static final long serialVersionUID = 8551792658587147027L;
 	/** The default size for doors */
 	public static final int DEFAULT_DOOR_SIZE = 5;
 	/** The default door offset, if <code>-1</code> the door will be centered */
 	public static final int DEFAULT_DOOR_OFFSET = -1;
 	/** The id counter */
 	private static int id;
 	/** The offset from the startpoint of the containing wall */
 	private int offset;
 	/** The size of the door */
 	private int size;
 	/** A list of listeners */
 	private ArrayList<IDoorListener> listeners = new ArrayList<IDoorListener>();
 	/**
 	 * The wall that contains this door and is used for calculating the position
 	 */
 	private Wall firstWall;
 	/** The wall, that also contains this door but is not used for calculations */
 	private Wall secondWall;
 	/** The identifier */
 	private int identifier;
 	/** The current state of this door */
 	private State currentState = State.CLOSED;
 
 	/**
 	 * Creates a door with a given identifier
 	 * 
 	 * @param identifier
 	 *            the identifier
 	 */
 	private Door(int identifier) throws InvalidParameterException {
 		super(ID_PREFIX + identifier);
 		this.identifier = identifier;
 		id++;
 		if (WorldFactory.findDoorForIdentifier(ID_PREFIX + identifier) != null) {
 			throw new InvalidParameterException(
 					"There is a door with this identifier yet.");
 		}
 		WorldFactory.addDoor(this);
 	}
 
 	/**
 	 * Constructor for a door with a given offset of the walls start point
 	 * 
 	 * @param offset
 	 *            the offset from the start point of the containing wall
 	 * @param size
 	 *            the size of the wall (must be positive)
 	 */
 	public Door(int offset, int size) {
 		this(id);
 
 		this.offset = offset;
 		this.size = Math.abs(size);
 	}
 
 	/**
 	 * Constructor for a default door that is automatically centered on the wall
 	 * and has a default size of DEFAULT_DOOR_SIZE
 	 */
 	public Door() {
 		this(DEFAULT_DOOR_OFFSET, DEFAULT_DOOR_SIZE);
 	}
 
 	/**
 	 * Getter for the offset of the wall
 	 * 
 	 * @return the offset or <code>-1</code> if the door should be automatically
 	 *         positioned
 	 */
 	public int getOffset() {
 		return offset;
 	}
 
 	/**
 	 * Getter for the size of the door
 	 * 
 	 * @return the size
 	 */
 	public int getSize() {
 		return size;
 	}
 
 	/**
 	 * Setter for the wall that contains this door
 	 * 
 	 * @param wall
 	 */
 	public void setWall(Wall wall) {
 		if (firstWall == null) {
 			this.firstWall = wall;
 		} else if (secondWall == null) {
 			this.secondWall = wall;
 		} else {
 			CASi.SIM_LOG
 					.warning("Adding door to more than two walls isn't allowed. Ignored this command! First wall = "
 							+ firstWall + ", second wall = " + secondWall);
 		}
 	}
 
 	/**
 	 * Getter for the integer value of the identifier for this door
 	 * 
 	 * @return the identifier as integer
 	 */
 	public int getIntIdentifier() {
 		return identifier;
 	}
 
 	@Override
 	public boolean contains(IPosition position) {
 		return contains(position.getCoordinates());
 	}
 
 	@Override
 	public boolean contains(Point2D point) {
 		return getShapeRepresentation().ptSegDist(point) <= 1;
 	}
 
 	@Override
 	public Line2D getShapeRepresentation() {
 		Point2D wallVector = firstWall.getNormalizedWallVector();
 		Point2D centralPoint = getCentralPoint();
 		double startEndOffset = ((double) size) / 2;
 		// Calculating offset vectors
 		Point2D startOffsetVector = new Point2D.Double(wallVector.getX()
 				* (-startEndOffset), wallVector.getY() * (-startEndOffset));
 		Point2D endOffsetVector = new Point2D.Double(wallVector.getX()
 				* startEndOffset, wallVector.getY() * startEndOffset);
 		// Calculating start end end points
 		Point2D startPoint = new Point2D.Double(centralPoint.getX()
 				+ startOffsetVector.getX(), centralPoint.getY()
 				+ startOffsetVector.getY());
 		Point2D endPoint = new Point2D.Double(centralPoint.getX()
 				+ endOffsetVector.getX(), centralPoint.getY()
 				+ endOffsetVector.getY());
 		Line2D line = new Line2D.Double(startPoint, endPoint);
 		return line;
 	}
 
 	@Override
 	public Point2D getCentralPoint() {
 		if (offset < 0) {
 			return firstWall.getCentralPoint();
 		}
 		Point2D wallVector = firstWall.getNormalizedWallVector();
 		// The offset of the central point:
 		double centralOffset = ((double) offset) + ((double) size) / 2;
 		// A vector with the wall direction and the length of the offset
 		Point2D doorStartOffset = new Point2D.Double(wallVector.getX()
 				* centralOffset, wallVector.getY() * centralOffset);
 		// Add start point as begin for the vector
 		return new Point2D.Double(firstWall.getStartPoint().getX()
 				+ doorStartOffset.getX(), firstWall.getStartPoint().getY()
 				+ doorStartOffset.getY());
 	}
 
 	@Override
 	public Point2D getCoordinates() {
 		return getCentralPoint();
 	}
 
 	/**
 	 * Method for getting the number of doors in the simulation
 	 * 
 	 * @return The static identifier counter
 	 */
 	public static int getNumberOfDoors() {
 		return id;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + identifier;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Door other = (Door) obj;
 		if (identifier != other.identifier)
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return super.toString();// +" ("+getIntIdentifier()+")";
 	}
 
 	/**
 	 * Informs listeners about state changes of this door
 	 * 
 	 * @param oldState
 	 *            the old state
 	 * @param newState
 	 *            the new current state
 	 */
 	private void informListenersAboutStateChange(State oldState, State newState) {
 		for (IDoorListener l : listeners) {
 			l.stateChanged(oldState, newState);
 		}
 	}
 
 	/**
 	 * Sets the state
 	 * 
 	 * @param state
 	 *            the state to set
 	 */
 	public void setState(State state) {
 		if (state.equals(currentState)) {
 			return;
 		}
 		State oldState = currentState;
 		currentState = state;
 		log.info(this.getIdentifier() + " changed state to " + state);
 		informListenersAboutStateChange(oldState, state);
 	}
 
 	/**
 	 * Gets the state
 	 * 
 	 * @return the current state
 	 */
 	public State getState() {
 		return currentState;
 	}
 
 	/**
 	 * Adds a door listener
 	 * 
 	 * @param listener
 	 *            the listener
 	 */
 	public void addListener(IDoorListener listener) {
 		if (!listeners.contains(listener)) {
 			listeners.add(listener);
 		}
 	}
 
 	/**
 	 * Removes a door listener
 	 * 
 	 * @param listener
 	 *            the listener to remove.
 	 */
 	public void removeListener(IDoorListener listener) {
 		listeners.remove(listener);
 	}
 
 }
