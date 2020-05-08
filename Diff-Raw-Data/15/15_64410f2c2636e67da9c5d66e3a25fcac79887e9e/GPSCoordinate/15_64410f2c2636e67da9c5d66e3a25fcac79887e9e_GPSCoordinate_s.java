 package projectswop20102011.domain;
 
 /**
  * A class that represents a GPSCoordinate.
  *
  * @author Willem Van Onsem, Jonas Vanthornhout & Pieter-Jan Vuylsteke
  */
 public class GPSCoordinate {
 
 	/**
 	 * A variable that registers the x coordinate of the GPS coordinate.
 	 */
 	private final long x;
 	/**
 	 * A variable that registers the y coordinate of the GPS coordinate.
 	 */
 	private final long y;
 
 	/**
 	 * Creates a new GPS coordinate.
 	 * @param x
 	 *		The x coordinate of this GPS coordinate.
 	 * @param y
 	 *		The y coordinate of this GPS coordinate.
 	 * @post The x-coordinate of this GPSCoordinate is set according to the given x-coordinate.
 	 *		|new.getX() == x
 	 * @post The y-coordinate of this GPSCoordinate is set according to the given y-coordinate.
 	 *		|new.getY() == y
 	 */
 	public GPSCoordinate(long x, long y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * Returns the x coordinate of this GPS coordinate.
 	 * @return The x coordinate of this GPS coordinate.
 	 */
 	public long getX() {
 		return x;
 	}
 
 	/**
 	 * Returns the y coordinate of this GPS coordinate.
 	 * @return The y coordinate of this GPS coordinate.
 	 */
 	public long getY() {
 		return y;
 	}
 
 	/**
 	 * Returns a textual representation of this GPSCoordinate.
 	 * @return A textual representation of this GPSCoordinate.
 	 */
 	@Override
 	public String toString() {
 		return String.format("(%s,%s)", this.getX(), this.getY());
 	}
 
 	/**
 	 * Calculates the distance to a given gpsCoordinate.
 	 * @param gpsCoordinate
 	 *		The gpsCoordinate to calculate te distance to.
 	 * @return The distance to the given gpsCoordinate.
 	 */
 	public double getDistanceTo(GPSCoordinate gpsCoordinate) {
		double distance;
		distance = Math.sqrt(Math.pow(getX() - gpsCoordinate.getX(), 2) + Math.pow(getY() - gpsCoordinate.getY(), 2));
		return distance;
 	}
 
 	/**
 	 * Checks whether the given object is equal to this GPSCoordinate.
 	 * @param o
 	 *		The object to check whether this GPSCoordinate is equal to it.
 	 * @return True if the given object is equal to this GPSCoordinate; false otherwise.
 	 */
 	@Override
 	public boolean equals(Object o) {
 		if (o instanceof GPSCoordinate) {
 			return equals((GPSCoordinate) o);
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Checks whether the given GPSCoordinate is equal to this GPSCoordinate.
 	 * @param gps2
 	 *		The GPSCoordinate to check whether this GPSCoordinate is equal to it.
 	 * @return True if the given GPSCoordinate is equal to this GPSCoordinate; false otherwise.
 	 */
 	public boolean equals(GPSCoordinate gps2) {
 		return (getX() == gps2.getX() && getY() == gps2.getY());
 	}
 
 	/**
 	 * Returns the hascode of the current GPSCoordinate. An integer representing the coordinate.
 	 * @return the hascode of the current GPSCoordinate.
 	 */
 	@Override
 	public int hashCode() {
 		return ((Long) getX()).hashCode() ^ ((Long) getY()).hashCode();
 	}
 }
