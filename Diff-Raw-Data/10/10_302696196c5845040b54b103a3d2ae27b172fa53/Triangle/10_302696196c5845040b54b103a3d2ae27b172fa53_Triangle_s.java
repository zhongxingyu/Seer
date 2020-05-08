 /**
  * Represents a Triangle Polygon.
  *
  * @author Ory Band
  * @version 1.0
  */
 public class Triangle extends Polygon {
     /**
      * @param p1 Triangle coordinate.
      * @param p2 Triangle coordinate.
      * @param p3 Triangle coordinate.
      *
      * @return an initialized Triangle object.
      */
     public Triangle(Point p1, Point p2, Point p3) { super( new Point[] {p1, p2, p3} ); }
 
     /**
      * @param t Triangle to deep copy.
      *
      * @return an initialized Triangle object.
      */
     public Triangle(Triangle t) { super(t.getPoints()); }
 
     /** @return a new copy of Triangle's 1st point */
     public Point getP1() { return new Point(this.points[0]); }
 
     /** @return a new copy of Triangle's 2nd point */
     public Point getP2() { return new Point(this.points[1]); }
 
     /** @return a new copy of Triangle's 3rd point */
     public Point getP3() { return new Point(this.points[2]); }
 
     /**
      * @param o Object to compare against.
      *
      * @return true if object is of type Triangle, and if all coordinates are equal appropriately.
      */
     public boolean equals(Object o) {
         // Validity test.
         if ( ! (o instanceof Triangle) ) {
             return false;
         } else {
             Point[] other_ps = ((Triangle) o).getPoints(),
                     ps       = this.getPoints();
 
             // Compare each point with all points in other Triangle.
             boolean found;
             for (int i=0; i<3; i++) {
                 found = false;
 
                 for (int j=0; j<3 && ! found; j++) {
                     if ( ps[i].equals(other_ps[j]) ) {
                         found = true;
                     }
                 }
 
                 if ( ! found ) {
                     return false;
                 }
             }
 
             return true;
         }
     }
 
     public double getArea() {
         // Using Heron's formulae.
		return ( this.getP1().distance(this.getP2()) +
                 this.getP1().distance(this.getP3()) ) / 2;
     }
 
     public boolean contains(Point p) {
         if (p == null) {
             throw new RuntimeException("Point argument is null.");
         }
 
         // Point is in triangle if sum of 3 triangles' area, built using Point argument
         // is very close to original triangles area.
         double diff = ( (new Triangle(p, this.points[0], this.points[1])).getArea() +
                         (new Triangle(p, this.points[0], this.points[2])).getArea() +
                         (new Triangle(p, this.points[1], this.points[2])).getArea() ) - this.getArea();
 
         return diff <= 0.001 && diff >= -0.001;
     }
 }
 
