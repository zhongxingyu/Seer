 package com.github.zinfidel.jarvis_march.geometry;
 
 import static java.lang.Math.sqrt;
 
 /**
  * Represents a 2D point as on a Cartesian plane.
  * 
  * @author Zach Friedland
  */
 public class Point {
     
     public static final Point Origin = new Point(0, 0);
     
     public final int x;
     public final int y;
     
     public Point(int x, int y) {
 	this.x = x;
 	this.y = y;
     }
     
     /**
      * Calculates the difference between this point and <code>point</code>.
      * 
      * @param point The point to subtract from this point.
      * @return A new point representing the difference between the points.
      */
     public Point Minus(Point point) {
 	int x = this.x - point.x;
 	int y = this.y - point.y;
 	
 	return new Point(x, y);
     }
     
     /**
      * Calculate the distance from this point to <code>point</code>.
      * 
      * @param point The point to calculate distance to.
      * @return The distance between this point and <code>point</code>.
      */
     public double DistanceTo(Point point) {
	double x2 = (double) (x * x);
	double y2 = (double) (y * y);
 	
 	return sqrt(x2 + y2);
     }
 
     /** {@InheritDoc} */
     @Override
     public boolean equals(Object obj) {
 	// Ensure we are comparing points.
         if (!(obj instanceof Point)) return false;
         
         // Ensure x and y values are the same.
         Point point = (Point) obj;
         boolean xEquals = (this.x == point.x);
         boolean yEquals = (this.y == point.y);
         
         return xEquals && yEquals;
     }
 }
