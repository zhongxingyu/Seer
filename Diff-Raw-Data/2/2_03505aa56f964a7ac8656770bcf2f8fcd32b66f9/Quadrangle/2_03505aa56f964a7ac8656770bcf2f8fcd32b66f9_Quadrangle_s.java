 /**
  * Represends a Quadrangle polygon.
  *
  * @author Ory Band
  * @version 1.0
  */
 public class Quadrangle extends Polygon {
     /**
      * @param p1 Quadrangle coordinate.
      * @param p2 Quadrangle coordinate.
      * @param p3 Quadrangle coordinate.
      * @param p4 Quadrangle coordinate.
      *
      * @return an initialized Quadrangle object.
      */
     public Quadrangle(Point p1, Point p2, Point p3, Point p4) { super( new Point[] {p1, p2, p3, p4} ); }
 
     /**
      * @param q Quadrangle to deep copy.
      *
      * @return an initialized Quadrangle object.
      */
     public Quadrangle(Quadrangle q) { super(q.getPoints()); }
 
     /** @return a new copy of Quadrangle's 1st point */
     public Point getP1() { return new Point(this.points[0]); }
 
     /** @return a new copy of Quadrangle's 2nd point */
     public Point getP2() { return new Point(this.points[1]); }
 
     /** @return a new copy of Quadrangle's 3rd point */
     public Point getP3() { return new Point(this.points[2]); }
 
     /** @return a new copy of Quadrangle's 4th point */
    public Point getP4() { return new Point(this.points[4]); }
 
     /**
      * @param o Object to compare against.
      *
      * @return true if object is of type Quadrangle, and if all coordinates are equal appropriately.
      */
     public boolean equals(Object o) {
         // Validity test.
         if ( ! (o instanceof Quadrangle) ) {
             return false;
         } else {
             Point[] other_ps = ((Quadrangle) o).getPoints(),
                     ps       = this.getPoints();
 
             // Compare each point with respective point in other Quadrangle.
             boolean found;
             for (int i=0; i<4; i++) {
                 found = false;
 
                 for (int j=0; j<4 && ! found; j++) {
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
         // Splitting Quadrangle to 2 Triangles and calculating their area.
         return new Triangle( this.getP1(), this.getP2(), this.getP3() ) .getArea() +
                new Triangle( this.getP1(), this.getP3(), this.getP4() ) .getArea();
 
     }
 
     public boolean contains(Point p) {
         if (p == null) {
             throw new RuntimeException("Point argument is null.");
         }
 
         // Splitting Quadrangle to 2 Triangles and testing if the point is in one of the triangles.
         return new Triangle( this.getP1(), this.getP2(), this.getP3() ) .contains(p) ||
                new Triangle( this.getP1(), this.getP3(), this.getP4() ) .contains(p);
     }
 }
