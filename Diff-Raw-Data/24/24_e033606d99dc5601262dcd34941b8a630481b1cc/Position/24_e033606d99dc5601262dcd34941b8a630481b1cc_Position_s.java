 package projectrts.model.world;
 
 import javax.vecmath.Vector2d;
 
 
 
 /**
  * A 2D position.
  * @author Bjorn Persson Mattsson
  *
  */
 public class Position {
 
 	private Vector2d coord;
	// TODO Plankton: Fix the javadoc here.
 	
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
 	 * Creates a new position with the same components.
 	 * @return A copy of the position such that oldPos.equals(returnedPos)==true.
 	 */
 	public Position copy()
 	{
 		return new Position(this.getX(), this.getY());
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 29; // Default generated hashcode
 		int result = 1;
 		long temp;
 		temp = Double.doubleToLongBits(coord.x);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		temp = Double.doubleToLongBits(coord.y);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
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
 		Position other = (Position) obj;
 		if (coord.x != other.coord.x)
 			return false;
 		if (coord.y != other.coord.y)
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
 	 * Returns a new position that, from this position, is distance away in the direction.
 	 * @param distance The distance from this position.
 	 * @param direction The direction from this position.
 	 * @return The resulting position.
 	 */
 	public Position add(double distance, Vector2d direction) {
 		if (direction.length() == 0) // null vector, no direction
 		{
 			return this;
 		}
 		Vector2d delta = new Vector2d();
 		delta.scale(distance/direction.length(), direction);
 		
 		// this position plus delta
 		return new Position(this.getX() + delta.x, this.getY() + delta.y);
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
 	
 	/**
 	 * Gets the distance between two positions
 	 * @param p1 the first point
 	 * @param p2 the second point
 	 * @return the distance between the points
 	 */
 	public static double getDistance(Position p1, Position p2){
 		double dx = p1.getX() - p2.getX();
 		double dy = p1.getY() - p2.getY();
 		
 		return Math.sqrt(dx*dx+dy*dy);
 	}
 }
