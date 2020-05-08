 package balle.world;
 
 public class Coord {
 
     private final double  x;
     private final double  y;
 
     private final boolean estimated;
 
     public Coord(double x, double y) {
         super();
         this.x = x;
         this.y = y;
         this.estimated = false;
     }
 
     public Coord(double x, double y, boolean estimated) {
         super();
         this.x = x;
         this.y = y;
         this.estimated = estimated;
     }
 
     public Coord(Coord coordinate, boolean estimated) {
         this(coordinate.getX(), coordinate.getY(), estimated);
     }
 
     public double getX() {
         return x;
     }
 
     public double getY() {
         return y;
     }
 
     public double abs() {
         return Math.sqrt(x * x + y * y);
     }
 
     public Coord sub(Coord c) {
         return new Coord(x - c.getX(), y - c.getY(), estimated);
     }
 
     public Coord add(Coord c) {
         return new Coord(x + c.getX(), y + c.getY(), estimated);
     }
 
     public Coord mult(double scalar) {
         return new Coord(x * scalar, y * scalar, estimated);
     }
 
     public double dist(Coord c) {
         return c.sub(this).abs();
     }
 
     public Orientation orientation() {
         double orientation_atan2 = Math.atan2(this.getY(), this.getX());
         if (orientation_atan2 < 0)
            orientation_atan2 = 2 * Math.PI + orientation_atan2;
         return new Orientation(orientation_atan2, true);
     }
 
     /**
      * Returns whether the coordinates have been estimated or not. By convention
      * the coordinates from the vision are not estimated, whereas the
      * coordinates that are updated from velocities e.g. when the vision returns
      * -1 are.
      * 
      * @return true or false depending whether the coordinates were estimated or
      *         not
      */
     public boolean isEstimated() {
         return estimated;
     }
 
     @Override
     public boolean equals(Object other) {
         if (other == null)
             return false;
         if (other == this)
             return true;
         if (this.getClass() != other.getClass())
             return false;
         Coord otherCoord = (Coord) other;
         return (otherCoord.getX() == this.getX())
                 && (otherCoord.getY() == this.getY())
                 && (otherCoord.isEstimated() == this.isEstimated());
     }
 }
