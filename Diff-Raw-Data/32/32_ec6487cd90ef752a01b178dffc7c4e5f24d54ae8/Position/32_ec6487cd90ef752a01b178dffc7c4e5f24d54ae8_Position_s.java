 package projectrts.model.core;
 
 import javax.vecmath.Vector2d;
 
 /**
  * A 2D position.
  * @author Bjorn Persson Mattsson
  *
  */
 public class Position {
 
 	private Vector2d coord;
 	
 	/**
 	 * Creates a new position with the given components.
 	 * @param x X component.
 	 * @param y Y component.
 	 */
 	public Position(double x, double y)
 	{
 		this.coord = new Vector2d(x, y);
 	}
 	
 	/**
 	 * Creates a new position from another position.
 	 * @param otherPos Other position
 	 */
 	public Position(Position otherPos)
 	{
 		this(otherPos.getX(), otherPos.getY());
 	}
 	
 	/**
 	 * @return Returns a clone of this position.
 	 */
 	@Override
 	public Position clone()
 	{
 		return new Position(this.getX(), this.getY());
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 29; // Default generated hashcode
 		long result = 1;
 		result = prime * result + Double.doubleToLongBits(coord.x);
 		result = prime * result + Double.doubleToLongBits(coord.y);
 		return (int)result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Position other = (Position) obj;
		if (Double.doubleToLongBits(coord.x) != Double.doubleToLongBits(other.coord.x))
 			return false;
		if (Double.doubleToLongBits(coord.y) != Double.doubleToLongBits(other.coord.y))
 			return false;
 		return true;
 	}
 
 	/**
 	 * @return The x component.
 	 */
 	public double getX()
 	{
 		return coord.x;
 	}
 	
 	/**
 	 * @return The y component.
 	 */
 	public double getY()
 	{
 		return coord.y;
 	}
 	
 	@Override
 	public String toString() {
 		return coord.x + ";" + coord.y;
 	}
 
 	/**
 	 * Returns the position at the distance and direction from this position.
 	 * @param distance Distance from this position to the returned one.
 	 * @param direction Direction vector from this position.
 	 * @return Position at the distance and direction from this position.
 	 */
 	public Position add(double distance, Vector2d direction) {
 		if (direction.length() == 0) // null vector, no direction
 		{
 			return this;
 		}
 		Vector2d delta = new Vector2d();
 		delta.scale(distance/direction.length(), direction);
 		
 		// this position plus delta
 		Position result = new Position(this.getX() + delta.x, this.getY() + delta.y);
 		return result;
 	}
 
 	/**
 	 * Returns a vector that goes from startPos to endPos.
 	 * @param startPos Start position.
 	 * @param endPos End position.
 	 * @return Vector2d between the two positions.
 	 */
 	public static Vector2d getVectorBetween(Position startPos,
 			Position endPos) {
 		Vector2d result = new Vector2d(endPos.getX()-startPos.getX(),
 				endPos.getY()-startPos.getY());
 		return result;
 	}
 }
