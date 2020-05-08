 package util;
 
 /**
  * A utility class to represent a 2D position.
  */
 public class Position2D {
 	
     /**
     * The line.
      */
    public final int x;

    /**
     * The column.
     */
    public final int y;

 	public boolean equals(Object o) {
 		if (! (o instanceof Position2D) ) {
 			return false;
 		}
 		Position2D p = (Position2D) o;
 		return p.x == x && p.y == y;
 	}
 	
 	public int hashCode() {
 		return x + 31*y;
 	}
     
     /**
      * Trivial constructor/
      *
      * @param x_ the line
      * @param y_ the column
      */
 	public Position2D(int x_,  int y_) {
 		x = x_;
 		y = y_;
 	}
 }
