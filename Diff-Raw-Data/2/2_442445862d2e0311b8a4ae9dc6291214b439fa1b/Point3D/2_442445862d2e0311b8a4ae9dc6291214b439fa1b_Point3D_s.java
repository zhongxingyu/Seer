 package org.jtrace.primitives;
 
 /**
  * Basic class to represent a point in a three-dimensional space.
  * 
  * @author raphaelpaiva
  * @author brunocosta
  * @author flaviocdc
  *
  */
 public class Point3D {
     private double x, y, z;
 
     /**
      * Create a point from its coordinates
      *  
      * @param x axis value
      * @param y axis value
      * @param z axis value
      */
     public Point3D(final double x, final double y, final double z) {
         super();
         this.x = x;
         this.y = y;
         this.z = z;
     }
     
     /**
      * Subtract operation
      * 
      * Performs the basic point subtraction.<br>
      * Suppose the points A(x, y, z) and B(u, v, w).<br>
      * 
      * A - B = C(x - u, y - v, z - w)
      * 
      * @param otherVector the point to subtract.
      * @return a new {@link Point3D} equivalent to <code>(this - otherVector)</code>
      */
     public Point3D subtract(Point3D otherVector) {
         return new Point3D(x - otherVector.getX(), y - otherVector.getY(), z - otherVector.getZ());
     }
 
     public double getX() {
         return x;
     }
 
     public void setX(final double x) {
         this.x = x;
     }
 
     public double getY() {
         return y;
     }
 
     public void setY(final double y) {
         this.y = y;
     }
 
     public double getZ() {
         return z;
     }
 
     public void setZ(final double z) {
         this.z = z;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         long temp;
         temp = Double.doubleToLongBits(x);
         result = prime * result + (int) (temp ^ (temp >>> 32));
         temp = Double.doubleToLongBits(y);
         result = prime * result + (int) (temp ^ (temp >>> 32));
         temp = Double.doubleToLongBits(z);
         result = prime * result + (int) (temp ^ (temp >>> 32));
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         Point3D other = (Point3D) obj;
         if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
             return false;
         if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
             return false;
         if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
             return false;
         return true;
     }
 
     @Override
     public String toString() {
    	return "(" + x + ", " + y + ", " + z + ", " + ")";
     }
     
 }
