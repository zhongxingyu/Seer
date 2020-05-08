 package balle.world;
 
 public class Line {
 
     private final Coord a, b;
 
     public Coord getA() {
         return a;
     }
 
     public Coord getB() {
         return b;
     }
 
     public Line(double x1, double y1, double x2, double y2) {
         this.a = new Coord(x1, y1);
         this.b = new Coord(x2, y2);
     }
 
     public Line(Coord a, Coord b) {
         this.a = a;
         this.b = b;
     }
 
     public boolean contains(Coord a) {
         // TODO: fix fix fix!
         return false;
     }
 
     /**
      * rotate the line around the origin
      * 
      * @param orientation
      * @return
      */
     public Line rotate(Orientation orientation) {
         return new Line(a.rotate(orientation), b.rotate(orientation));
     }
 
     public Line add(Coord position) {
         return new Line(a.add(position), b.add(position));
     }
 
     /**
      * Returns the midpoint of the line
      * 
      * @return
      */
     public Coord midpoint() {
        return new Coord((getA().getX() + getB().getX()) / 2.0,
                 (getA().getY() + getB().getY()) / 2.0);
     }
 }
