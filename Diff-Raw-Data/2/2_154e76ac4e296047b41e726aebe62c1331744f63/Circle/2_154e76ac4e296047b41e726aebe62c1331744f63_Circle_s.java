 /**
  * Represents a Circle Shape.
  *
  * @author Ory Band
  * @version 1.0
  */
 public class Circle implements Shape {
     private Point c;   // Center coordinate.
     private double r;  // Radius.
 
     /**
      * @param c Center coordinate.
      * @param r Radius.
      *
      * @return an initialized Circle object.
      */
     public Circle(Point c, double r) {
         if (c == null) {
             throw new RuntimeException("Point argument is null.");
         } else if (r <= 0) {
             throw new RuntimeException("Radious can't be 0 or negative!");
         }
 
         this.c = c;
         this.r = r;
     }
 
     /**
      * @param c Circle object to deep copy.
      *
      * @return an initialized Circle object.
      */
     public Circle(Circle c) {
         if (c == null) {
             throw new RuntimeException("Circle argument is null.");
         }
 
         this.c = new Point(c.getCenter());
         this.r = c.getRadius();
     }
 
     /** @return a new copy of the Circle's center. */
     public Point getCenter() { return new Point(this.c); }
 
     /** @return the Circle's radius. */
     public double getRadius() { return this.r; }
 
     /**
      * @param o Object to compare against.
      *
      * @return true if object is of type Circle, and if its center and radius are equal..
      */
     public boolean equals(Object o) {
         return o instanceof Circle &&
               this.c == ((Circle) o) .getCenter() &&
                this.r == ((Circle) o) .getRadius();
     }
 
     public void move(Point p) {
 		if (p == null) {
 			throw new RuntimeException("Point argument is null.");
 		}
 
         this.c.move(p);
     }
 
     public double getPerimeter() {
         return 2 * Math.PI * this.r;
     }
 
     public double getArea() {
         return Math.PI * Math.pow(this.r, 2);
     }
 
     public boolean contains(Point p) {
         // Validity Test.
 		if (p == null) {
 			throw new RuntimeException("Point argument is null.");
 		}
 		
         // Test if point is in radius distance from the center.
 		return (p.distance(this.c) <= this.r);
     }
 }
 
