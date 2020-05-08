 package org.team751.util;
 
 import com.sun.squawk.util.MathUtils;
 
 /**
  * A vector with two elements of type double.
  * 
  * All modifying methods return a newly allocated instance, rather than modifying
  * the current one.
  * 
  * @author Sam Crow
  */
 public class Vec2 {
     
     /**
      * The X (horizontal) component
      */
     protected double x = 0;
     
     /**
      * The Y (vertical) component
      */
     protected double y = 0;
     
     public Vec2() {
         this(0, 0);
     }
     
     public Vec2(double x, double y) {
         this.x = x;
         this.y = y;
     }
     
     /**
      * Get the X component
      * @return the X component
      */
     public double getX() {
         return x;
     }
     
     /**
      * Get the Y component
      * @return the Y component
      */
     public double getY() {
         return y;
     }
     
     /**
      * Create and return a newly allocated vector with the same values as this one
      * @return the new vector
      */
    public Object clone() {
         return new Vec2(x, y);
     }
     
     /**
      * Create a new vector from the sum of this vector and another
      * @param other The vector to add.
      * @return the new vector
      */
     public Vec2 add(Vec2 other) {
         return new Vec2(x + other.x, y + other.y);
     }
     
     /**
      * Create a new vector from the product of this vector and a scalar
      * @param d The number to multiply by
      * @return the new vector
      */
     public Vec2 multiply(double d) {
         return new Vec2(x * d, y * d);
     }
     
     /**
      * Create a new vector with X and Y values that are the negatives of the
      * values in this vector
      * @return the new vector
      */
     public Vec2 invert() {
         return new Vec2(-x, -y);
     }
     
     /**
      * Get the magnitude (hypotenuse length) of this vector
      * @return the magnitude
      */
     public double getMagnitude() {
         return Math.sqrt((x * x) + (y * y));
     }
     
     /**
      * Get the angle of this vector
      * @return The angle of this vector, in degrees, counterclockwise of the
      * positive X axis
      */
     public double getAngleDegrees() {
         return Math.toDegrees(MathUtils.atan2(y, x));
     }
     
     /**
      * Create a new vector with a given angle (counterclockwise from the positive
      * X axis) and magnitude
      * @param angleDegrees The angle in degrees, counterclockwise from the
      * positive X axis, that the vector should point
      * @param magnitude The magnitude of the vector
      * @return The new vector
      */
     public static Vec2 fromAngle(double angleDegrees, double magnitude) {
         
         double radians = Math.toRadians(angleDegrees);
         double newX = magnitude * Math.cos(radians);
         double newY = magnitude * Math.sin(radians);
         
         return new Vec2(newX, newY);
     }
 }
