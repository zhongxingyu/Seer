 package edacc.experiment.plots;
 
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.util.LinkedList;
 
 /**
  * Simple implementation of a generic quad tree.
  * Implements insert and query.
  * @author simon
  */
 public class QuadTree<T> {
     public static final int CAPACITY = 10000; // this is the maximum size of a bucket
     private Point upperLeft, lowerRight;
     /**
      * children[0].. upper left bucket
      * children[1].. upper right bucket
      * children[2].. lower right bucket
      * children[3].. lower left bucket
      */
     private static final int UL = 0, UR = 1, LR = 2, LL = 3;
     private QuadTree<T>[] children;
     private LinkedList<BucketObject<T>> points;
 
     public QuadTree(Point upperLeft, Point lowerRight) {
         this.upperLeft = upperLeft;
         this.lowerRight = lowerRight;
         points = new LinkedList<BucketObject<T>>();
     }
 
     public void insert(Point2D point, T obj) {
         //System.out.println("My coordinates: " + upperLeft + " and " + lowerRight + ". Insert: " + java.util.Arrays.toString(point));
         if (children != null) {
             // check in which bucket this point fits:
             for (int bucket = UL; bucket <= LL; bucket++) {
                 if (point.getX() >= children[bucket].upperLeft.x
                         && point.getX() <= children[bucket].lowerRight.x
                         && point.getY() >= children[bucket].upperLeft.y
                         && point.getY() <= children[bucket].lowerRight.y) {
                     children[bucket].insert(point, obj);
                 }
             }
         } else {
             points.add(new BucketObject(point, obj));
 
             if (points.size() > QuadTree.CAPACITY) {
                 // System.out.println("DEBUG: Capacity reached");
                 if (!(lowerRight.x - upperLeft.x >= 2 && lowerRight.y - upperLeft.y >= 2)) {
                     // cannot split into more buckets
                     //  System.out.println("DEBUG: cannot split into four buckets");
                     return;
                 }
                 // split bucket
                 children = new QuadTree[4];
                 int mid_x = (upperLeft.x + lowerRight.x) / 2;
                 int mid_y = (upperLeft.y + lowerRight.y) / 2;
                 children[UL] = new QuadTree(upperLeft, new Point(mid_x, mid_y));
                 children[UR] = new QuadTree(new Point(mid_x, upperLeft.y), new Point(lowerRight.x, mid_y));
                 children[LR] = new QuadTree(new Point(mid_x, mid_y), lowerRight);
                 children[LL] = new QuadTree(new Point(upperLeft.x, mid_y), new Point(mid_x, lowerRight.y));
 
                 for (BucketObject p : points) {
                     insert(p.point, (T) p.obj);
                 }
             }
         }
     }
 
     private boolean intersects(Point2D p, double radius, Point upperLeft, Point lowerRight) {
         int rectWidth = lowerRight.x - upperLeft.x;
         int rectHeight = lowerRight.y - upperLeft.y;
         double circleDistX = p.getX() - upperLeft.x - rectWidth / 2;
         double circleDistY = p.getY() - upperLeft.y - rectHeight / 2;
 
         if (circleDistX > (rectWidth / 2 + radius)) {
             return false;
         }
         if (circleDistY > (rectHeight / 2 + radius)) {
             return false;
         }
 
         if (circleDistX <= rectWidth / 2) {
             return true;
         }
         if (circleDistY <= rectHeight / 2) {
             return true;
         }
         double cornerDistanceSq = Math.pow(circleDistX - rectWidth / 2, 2)
                 + Math.pow(circleDistY - rectHeight / 2, 2);
         return (cornerDistanceSq <= radius * radius);
     }
 
     public T query(Point2D point, double radius) {
         //System.out.println("QUERY; my coordinates: " + upperLeft + " and " + lowerRight + ". Query was: " + java.util.Arrays.toString(point) + " radius:" + radius);
         if (!intersects(point, radius, upperLeft, lowerRight)) {
             //System.out.println("DEBUG: the point cannot be in this bucket!");
             return null;
         }
         LinkedList<BucketObject<T>> tmp = new LinkedList<BucketObject<T>>();
         if (children != null) {
             T p = children[UL].query(point, radius);
             if (p != null) {
                 tmp.add(new BucketObject(point, p));
             }
             p = children[UR].query(point, radius);
             if (p != null) {
                 tmp.add(new BucketObject(point, p));
             }
             p = children[LR].query(point, radius);
             if (p != null) {
                 tmp.add(new BucketObject(point, p));
             }
             p = children[LL].query(point, radius);
             if (p != null) {
                 tmp.add(new BucketObject(point, p));
             }
         } else {
             tmp.addAll(points);
         }
         T res = null;
         double dist = radius + 1;
         for (BucketObject<T> bObj : tmp) {
             Point2D p = bObj.point;
             double tmpdist = p.distance(point); //Math.sqrt((p[0] - point[0]) * (p[0] - point[0]) + (p[1] - point[1]) * (p[1] - point[1]));
             if (tmpdist < dist) {
                 dist = tmpdist;
                 res = bObj.obj;
             }
         }
         if (res != null && dist <= radius) {
             return res;
         } else {
             return null;
         }
 
     }
 
     class BucketObject<T> {
 
         private Point2D point;
         private T obj;
 
         public BucketObject(Point2D point, T obj) {
             this.point = point;
             this.obj = obj;
         }
     }
 }
