 import java.util.Vector;
 
 public class PointSET {
     private SET<Point2D> pointSet;
     
     // construct an empty set of points
     public PointSET() {
         pointSet = new SET<Point2D>();
     }
     
     // is the set empty?
     public boolean isEmpty() {
         return pointSet.isEmpty();
     }
     
     // number of points in the set
     public int size() {
         return pointSet.size();
     }
     
     // add the point p to the set (if it is not already in the set)
     public void insert(Point2D p) {
         pointSet.add(p);
     }
     
     // does the set contain the point p?
     public boolean contains(Point2D p) {
         return pointSet.contains(p);
     }
     
     // draw all of the points to standard draw
     public void draw() {
         for (Point2D x : pointSet) {
             x.draw();
         }
         
     }
     
     // all points in the set that are inside the rectangle
     public Iterable<Point2D> range(RectHV rect) {
         Vector<Point2D> out = new Vector<Point2D>();
         for (Point2D x : pointSet) {
             if (rect.contains(x))
                 out.add(x);
         }
         return out;
         
     }
     
     // a nearest neighbor in the set to p; null if set is empty
     public Point2D nearest(Point2D p) {
        double distance = Double.POSITIVE_INFINITY;
         Point2D out = null;
         for (Point2D x : pointSet) {
             double d = p.distanceTo(x);
             if (d < distance) {
                 out = x;
                 distance = d;
             }
         }
         return out;
     }
 }
