 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package math;
 
 /**
  * Immutable mathematical 2D vector.
  */
 public class Vector2 {
   public static final Vector2 ZERO = new Vector2(0, 0);
 
   /**
    * X and Y components of the vector.
    */
   public final float x, y;
 
   /**
    * The magnitude of the vector (also as it's square).
    */
   private float magnitude, magnitudeSquared;
 
   /**
    * Normalized version of the vector.
    */
   private Vector2 normalized;
 
   /**
    * Construct a new vector from two floats, X and Y.
    * @param x the X component
    * @param y the Y component
    */
   public Vector2(float x, float y) {
     this.x = x;
     this.y = y;
 
     this.magnitudeSquared = -1;
     this.magnitude        = -1;
 
     this.normalized = null;
   }
 
   /**
    * Return the magnitude of the vector.
    * @return the magnitude, greater than or equal to zero
    */
   public float magnitude() {
     if (magnitude == -1)
       magnitude = (float) Math.sqrt(magnitudeSquared());
 
     return magnitude;
   }
 
   /**
    * Return the squared magnitude of the vector.
    * @return the squared magnitude, greater than or equal to zero
    */
   public float magnitudeSquared() {
     if (magnitudeSquared == -1)
       magnitudeSquared = sq(x) + sq(y);
 
     return magnitudeSquared;
   }
 
   /**
    * Return a normalization of this vector.
    * If the magnitude is zero
    * @return a normalized vector
    * @throws ArithmeticException when the magnitude of the vector is zero
    */
   public Vector2 normalize() {
     if (normalized == null) {
      float m = magnitude();
      normalized = new Vector2(x / m, y / m);
     }
 
     return normalized;
   }
 
   /**
    * Return a string representation of the vector.
    * Format: "(x, y)"
    * @return a string
    */
   @Override
   public String toString() {
     return "(" + x + ", " + y + ")";
   }
 
   /**
    * Square a float.
    * @param a the float
    * @return the square (a * a)
    */
   private static float sq(float a) {
     return a * a;
   }
 
   /**
    * Calculate the dot product between two vectors.
    * @param a the first vector
    * @param b the second vector
    * @return the dot product as a float
    */
   public static float dot(Vector2 a, Vector2 b) {
     return (a.x * b.x) + (a.y * b.y);
   }
 
   /**
    * Calculates the "distance" between two vectors. The vectors are regarded
    * using a shared start point. The distance is then the magnitude of the
    * vector (a - b).
    * @param a the first vector
    * @param b the second vector
    * @return the distance as a float, greater than or equal to zero
    */
   public static float distance(Vector2 a, Vector2 b) {
     return (float) Math.sqrt(Vector2.distanceSquared(a, b));
   }
 
   /**
    * Calculates the squared distance between two vectors.
    * @see #Vector2.distance(Vector2 a, Vector2 b)
    * @param a the first vector
    * @param b the second vector
    * @return the squared distance as a float, greater than or equal to zero
    */
   public static float distanceSquared(Vector2 a, Vector2 b) {
     return sq(a.x - b.x) + sq(a.y - b.y);
   }
 
   /**
    * Add two vectors.
    * @param a the first vector
    * @param b the second vector
    * @return a new vector
    */
   public static Vector2 add(Vector2 a, Vector2 b) {
     return new Vector2(a.x + b.x, a.y + b.y);
   }
 
   /**
    * Add two vectors. Where the second is just X and Y components.
    * @param a the first vector
    * @param bx the X component of the second vector
    * @param by the Y component of the second vector
    * @return a new vector
    */
   public static Vector2 add(Vector2 a, float bx, float by) {
     return new Vector2(a.x + bx, a.y + by);
   }
 
   /**
    * Subtract a vector from another vector.
    * The resulting vector is a vector "from b to a".
    * Note that this operation is not commutative.
    * @param a the first vector
    * @param b the second vector
    * @return a new vector
    */
   public static Vector2 subtract(Vector2 a, Vector2 b) {
     return new Vector2(a.x - b.x, a.y - b.y);
   }
 
   /**
    * Multiply a vector with a scalar.
    * @param a the vector
    * @param scalar the scalar
    * @return a new vector
    */
   public static Vector2 multiply(Vector2 a, float scalar) {
     return new Vector2(a.x * scalar, a.y * scalar);
   }
 
   /**
    * Divide a vector with a scalar.
    * @param a the vector
    * @param scalar the scalar
    * @return a new vector
    */
   public static Vector2 divide(Vector2 a, float scalar) {
     return new Vector2(a.x / scalar, a.y / scalar);
   }
 }
