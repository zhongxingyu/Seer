 package battlechallenge;
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * The Class Ship. A data holder representing ships from the original game
  * battleship.
  */
 public class Ship implements Serializable {
 
 	/** The Constant serialVersionUID. */
 	private static final long serialVersionUID = 0L;
 	
 	/**
 	 * The Enum Direction. Which direction the ship will be extended.
 	 */
 	public static enum Direction {
 		/** The NORTH. */
 		NORTH,
 		/** The EAST. */
 		EAST,
 		/** The SOUTH. */
 		SOUTH,
 		/** The WEST. */
 		WEST
 	}
 
 	/**
 	 * The length. How many spaces the ship extends from the start position in
 	 * the specified direction.
 	 */
 	private int length;
 
 	/** The health. Number of hits left before the ship is sunk. */
 	private int health;
 
 	/** The start position. */
 	private Coordinate startPosition;
 
 	/**
 	 * The direction. Which direction from the starting position the ship will
 	 * be extended.
 	 */
 	private Direction direction;
 	
 	/** The hits. */
 	private Set<String> hits;
 	
 	/** The coordinates */
 	private Set<String> coords;
 
 	/**
 	 * Instantiates a new ship.
 	 * 
 	 * @param length
 	 *            of the ship
 	 * @param startPosition
 	 *            the start position
 	 * @param direction
 	 *            the direction
 	 */
 	public Ship(int length, Coordinate startPosition, Direction direction) {
 		this.length = length;
 		this.health = length;
 		this.startPosition = startPosition;
 		this.direction = direction;
 		this.hits = new HashSet<String>();
 		this.coords = getCoordinateStrings();
 	}
 
 	/**
 	 * Gets the length.
 	 * 
 	 * @return the length
 	 */
 	public int getLength() {
 		return length;
 	}
 
 	/**
 	 * Sets the length.
 	 * 
 	 * @param length
 	 *            the new length
 	 */
 	public void setLength(int length) {
 		this.length = length;
 	}
 
 	/**
 	 * Gets the starting coordinate of the ship.
 	 *
 	 * @return the start position
 	 */
 	public Coordinate getStartPosition() {
 		return startPosition;
 	}
 
 	/**
 	 * Sets the starting coordinate of the ship.
 	 *
 	 * @param startPosition the new start position
 	 */
 	public void setStartPosition(Coordinate startPosition) {
 		this.startPosition = startPosition;
 	}
 
 	/**
 	 * Gets the direction in which the ship extends outward.
 	 *
 	 * @return the direction
 	 */
 	public Direction getDirection() {
 		return direction;
 	}
 
 	/**
 	 * Sets the direction in which the ship extends outward.
 	 *
 	 * @param direction the new direction
 	 */
 	public void setDirection(Direction direction) {
 		this.direction = direction;
 	}
 
 	/**
 	 * Gets the end position based on the direction the ship extends outward
 	 * from its starting position.
 	 *
 	 * @return the end position
 	 */
 	public Coordinate getEndPosition() {
 		switch (direction) {
 		case NORTH:
 			return new Coordinate(startPosition.getRow(),
 					startPosition.getCol() - length);
 		case SOUTH:
 			return new Coordinate(startPosition.getRow(),
 					startPosition.getCol() + length);
 		case EAST:
 			return new Coordinate(startPosition.getRow() + length,
 					startPosition.getCol());
 		case WEST:
 			return new Coordinate(startPosition.getRow() - length,
 					startPosition.getCol());
 		}
 		return null; // Should not reach here, will only be true if the ship
 						// direction is invalid
 	}
 
 	/**
 	 * Gets the coordinate strings.
 	 *
 	 * @return the coordinate strings
 	 */
	public Set<String> getCoordinateStrings() {
 		if (coords == null) {
 			coords = new HashSet<String>();
 			for (int i = 0; i < length; i++) {
 	
 				switch (direction) {
 				case NORTH: {
 					coords.add((this.startPosition.row + i) + ","
 							+ this.startPosition.col);
 				} break;
 				case SOUTH: {
 					coords.add((this.startPosition.row - i) + ","
 							+ this.startPosition.col);
 				} break;
 				case EAST: {
 					coords.add(this.startPosition.row + ","
 							+ (this.startPosition.col + i));
 				} break;
 				case WEST: {
 					coords.add(this.startPosition.row + ","
 							+ (this.startPosition.col - i));
 				} break;
 				}
 			}
 		}
 		return coords;
 	}
 	
 	/**
 	 * In bounds inclusive.
 	 *
 	 * @param rowMin the row min
 	 * @param rowMax the row max
 	 * @param colMin the col min
 	 * @param colMax the col max
 	 * @return true, if successful
 	 */
 	public boolean inBoundsInclusive(int rowMin, int rowMax, int colMin, int colMax) {
 		// TODO: double check "in bound" logic
 		switch (direction) {
 		case NORTH:
 			return this.startPosition.col >= colMin && this.startPosition.col <= colMax && this.startPosition.row <= rowMax && this.startPosition.row >= (rowMin-length);
 		case SOUTH:
 			return this.startPosition.col >= colMin && this.startPosition.col <= colMax && this.startPosition.row <= (rowMax+length) && this.startPosition.row >= rowMin;
 		case EAST:
 			return this.startPosition.col >= colMin && this.startPosition.col <= (colMax-length) && this.startPosition.row <= rowMax && this.startPosition.row >= rowMin;
 		case WEST:
 			return this.startPosition.col >= (colMin+length) && this.startPosition.col <= colMax && this.startPosition.row <= rowMax && this.startPosition.row >= rowMin;
 		}
 		return false;
 	}
 
 	/**
 	 * Gets the health. The number of hits left before sinking.
 	 * 
 	 * @return the health
 	 */
 	public int getHealth() {
 		return health;
 	}
 
 	/**
 	 * Checks if this ship is sunken.
 	 * 
 	 * @return true, if is sunken
 	 */
 	public boolean isSunken() {
 		return health <= 0;
 	}
 
 	/**
 	 * Checks if the supplied coordinate is a hit. Health is automatically
 	 * updated.
 	 *
 	 * @param c the coordinate
 	 * @param damage the damage
 	 * @return true, if is hit
 	 */
 	public boolean isHit(Coordinate c, int damage) {
 		// TODO: handle case where player hits same spot twice (force unique
 		// shot locations)
 		
 		if (isSunken()) {
 			return false;
 		}
 		
 		if (coords.contains(c.toString())) {
 			if (!hits.contains(c.toString())) {
 				health -= damage; // reduce health on ship by damage
 				hits.add(c.toString());
 			}
 			return true; // Is a hit
 		}
 		
 		return false;
 	}
 
 	/**
 	 * Deep copy of the shipObject so that the player can use the ship object as
 	 * necessary while the server has keeps its own copy.
 	 * 
 	 * @return the ship
 	 */
 	public Ship deepCopy() {
 		return new Ship(length, startPosition, direction);
 	}
 }
