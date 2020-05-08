 /**
  * Represents a 2D point coordinate.
  *
  * @author Ory Band
  * @version 1.0
  */
 public class Point {
     /** X,Y coordinates. */
     private double x,y;
 
     /**
      * @param x X coordinate.
      * @param y Y coordinate.
      *
      * @return new initialized Point object.
      */
     public Point(double x, double y) {
         this.x = x;
         this.y = y;
     }
 
     /**
      * @param p Point object to deep copy.
      *
      * @return new initialized Point object.
      */
     public Point(Point p) {
         this.x = p.getX(); 
         this.y = p.getY(); 
     }
 
     /** @return Point's X coordinate. */
     public double getX() {
         return this.x;
     }
 
     /** @return Point's Y coordinate. */
     public double getY() {
         return this.y;
     }
 
     /**
      * @param x X coordinate to add to Point's X coordinate.
      * @param y Y coordinate to add to Point's Y coordinate.
      */
     public void move(double x, double y) {
         this.x += x;
         this.y += y;
     }
 
     /**
      * @param p Another Point object to add its coordinate's to the Point object in hand.
      */
     public void move(Point p) {
         this.x += p.getX();
         this.y += p.getY();
     }
 
    /** @return True if object is of type Point, and if coordinates are equal to the Point object in hand. */
     public boolean equals(Object o) {
         return o instanceof Point &&
                this.x == ((Point) o).getX() &&
                this.y == ((Point) o).getY();
     }
 
     /**
      * @param p Point object to calculate distnace against.
      *
      * @return distance between points.
      */
     public double distance(Point p) {
         return Math.sqrt(
                 Math.pow(p.getX() - this.x, 2) +
                 Math.pow(p.getY() - this.y, 2) );
     }
 }
