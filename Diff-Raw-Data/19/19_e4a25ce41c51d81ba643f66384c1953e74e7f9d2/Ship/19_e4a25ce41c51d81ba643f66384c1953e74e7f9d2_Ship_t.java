 package battlechallenge.ship;
 
 import java.io.Serializable;
 import java.util.List;
 
 import battlechallenge.Coordinate;
 import battlechallenge.ShipAction;
 import battlechallenge.ShipIdentifier;
 
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
		WEST,
		/** Don't move. */
		STOP
 	}
 
 	/** The damage. */
 	private int damage;
 
 	/** The health. Number of hits left before the ship is sunk. */
 	private int health;
 
 	/** The location. */
 	private Coordinate location;
 
 	/** The range. */
 	private int range;
 
 	/** The movement. */
 	private int movement;
 
 	private int numShots;
 
 	/** The player id. */
 	private int playerId;
 
 	/** The ship id. */
 	private int shipId;
 
 	/**
 	 * Gets the damage.
 	 * 
 	 * @return the damage
 	 */
 	public int getDamage() {
 		return damage;
 	}
 
 	/**
 	 * Sets the damage.
 	 * 
 	 * @param damage
 	 *            the new damage
 	 */
 	public void setDamage(int damage) {
 		this.damage = damage;
 	}
 
 	/**
 	 * Sets the health.
 	 * 
 	 * @param health
 	 *            the new health
 	 */
 	public void setHealth(int health) {
 		this.health = health;
 	}
 
 	/**
 	 * Gets the range.
 	 * 
 	 * @return the range
 	 */
 	public int getRange() {
 		return range;
 	}
 
 	/**
 	 * Sets the range.
 	 * 
 	 * @param range
 	 *            the new range
 	 */
 	public void setRange(int range) {
 		this.range = range;
 	}
 
 	/**
 	 * Gets the movement.
 	 * 
 	 * @return the movement
 	 */
 	public int getMovement() {
 		return movement;
 	}
 
 	/**
 	 * Sets the movement.
 	 * 
 	 * @param movement
 	 *            the new movement
 	 */
 	public void setMovement(int movement) {
 		this.movement = movement;
 	}
 
 	/**
 	 * Gets the player id.
 	 * 
 	 * @return the player id
 	 */
 	public int getPlayerId() {
 		return playerId;
 	}
 
 	/**
 	 * Sets the player id.
 	 * 
 	 * @param playerId
 	 *            the new player id
 	 */
 	public void setPlayerId(int playerId) {
 		this.playerId = playerId;
 	}
 
 	/**
 	 * Gets the ship id.
 	 * 
 	 * @return the ship id
 	 */
 	public int getShipId() {
 		return shipId;
 	}
 
 	/**
 	 * Sets the ship id.
 	 * 
 	 * @param shipId
 	 *            the new ship id
 	 */
 	public void setShipId(int shipId) {
 		this.shipId = shipId;
 	}
 
 	public ShipIdentifier getIdentifier() {
 		return new ShipIdentifier(shipId, playerId);
 	}
 
 	public int getNumShots() {
 		return numShots;
 	}
 
 	/**
 	 * Instantiates a new ship.
 	 */
 	public Ship() {
 		// FIXME: remove magic numbers
 		this(1, new Coordinate(-1, -1));
 	}
 
 	/**
 	 * Instantiates a new ship.
 	 * 
 	 * @param startPosition
 	 *            the start position
 	 */
 	public Ship(int health, Coordinate startPosition) {
 		// FIXME: remove magic numbers
 		this(1, 1, 10, 1, 1, startPosition);
 	}
 
 	/**
 	 * Instantiates a new ship.
 	 * 
 	 * @param damage
 	 *            the damage
 	 * @param health
 	 *            the health
 	 * @param range
 	 *            the range
 	 * @param movement
 	 *            the movement
 	 * @param numShots
 	 *            the number of shots
 	 * @param startPosition
 	 *            the start position
 	 */
 	public Ship(int damage, int health, int range, int movement, int numShots,
 			Coordinate startPosition) {
 		this.damage = damage;
 		this.health = health;
 		this.range = range;
 		this.movement = movement;
 		this.location = startPosition;
 		this.numShots = numShots;
 	}
 
 	public Ship(Coordinate location) {
 		this(1, 1, 10, 1, 1, location);
 	}
 
 	/**
 	 * Gets the starting coordinate of the ship.
 	 * 
 	 * @return the start position
 	 */
 	public Coordinate getLocation() {
 		return location;
 	}
 
 	/**
 	 * Sets the starting coordinate of the ship.
 	 * 
 	 * @param startPosition
 	 *            the new start position
 	 */
 	public void setLocation(Coordinate location) {
 		this.location = location;
 	}
 
 	/**
 	 * Checks to see if any part of the ship is within the specified bounds.
 	 * 
 	 * @param rowMin
 	 *            the row min
 	 * @param rowMax
 	 *            the row max
 	 * @param colMin
 	 *            the col min
 	 * @param colMax
 	 *            the col max
 	 * @return true, if successful
 	 */
 	public boolean inBoundsInclusive(int rowMin, int rowMax, int colMin,
 			int colMax) {
 		return location.getRow() >= rowMin && location.getRow() <= rowMax
 				&& location.getCol() >= colMin && location.getCol() <= colMax;
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
 	 * @param c
 	 *            the coordinate
 	 * @param damage
 	 *            the damage
 	 * @return true, if is hit
 	 */
 	public boolean isHit(Coordinate c, int damage) {
 		if (!isSunken() && location.equals(c)) {
 			health -= damage;
 			return true;
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder("Ship[ Location: ");
 		sb.append(location).append(" Idenifier: ").append(getIdentifier())
 				.append(" Health: ").append(health).append(" ]");
 		return sb.toString();
 	}
 
 	/**
 	 * Gets the ship action.
 	 * 
 	 * @param move
 	 *            the move
 	 * @param shot
 	 *            the shot
 	 * @return the ship action
 	 */
 	public ShipAction getShipAction(Direction move, Coordinate shot) {
 		return new ShipAction(new ShipIdentifier(shipId, playerId), shot, move);
 	}
 
 	/**
 	 * Gets the ship action.
 	 * 
 	 * @param moves
 	 *            the moves
 	 * @param shots
 	 *            the shots
 	 * @return the ship action
 	 */
 	public ShipAction getShipAction(List<Direction> moves,
 			List<Coordinate> shots) {
 		return new ShipAction(new ShipIdentifier(shipId, playerId), shots,
 				moves);
 	}
 
 	@Override
 	public int hashCode() {
 		return this.getIdentifier().toString().hashCode();
 	}
 
 	/**
 	 * Used to determine the distance from the center of the ship to a
 	 * coordinate
 	 * 
 	 * @param coord
 	 *            The coordinate to get the distance to
 	 * @return The distance between a ship and a coordinate
 	 */
 	public double distanceFromCoord(Coordinate coord) {
 		return location.distanceTo(coord);
 	}
 
 	public boolean inRange(Coordinate c) {
 		return ((int) location.distanceTo(c) <= range);
 	}
 
 	public Ship deepCopy() {
 		Ship temp = new Ship(damage, health, range, movement, numShots,
 				location);
 		return temp;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (o instanceof Ship) {
 			Ship s = (Ship) o;
 			return s.getPlayerId() == playerId && s.getShipId() == shipId;
 		}
 		return false;
 	}
 }
