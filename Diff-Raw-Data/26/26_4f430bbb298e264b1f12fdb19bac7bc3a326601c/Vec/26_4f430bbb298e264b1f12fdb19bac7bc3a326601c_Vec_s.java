 package math;
 
 
 import java.awt.*;
 
 /**
  * 3D vector class that contains three doubles. Could be used to represent
  * Vectors but also Points and Colors.
  * 
  */
 public class Vec {
 
 	/**
 	 * Vector data. Allowed to be accessed publicly for performance reasons
 	 */
 	public double x, y, z;
 
 	/**
 	 * Initialize vector to (0,0,0)
 	 */
 	public Vec() {
 		x = 0;
         y = 0;
         z = 0;
 	}
 
 	/**
 	 * Initialize vector to given coordinates
 	 * 
 	 * @param x
 	 *            Scalar
 	 * @param y
 	 *            Scalar
 	 * @param z
 	 *            Scalar
 	 */
 	public Vec(double x, double y, double z) {
 		this.x = x;
 		this.y = y;
 		this.z = z;
 	}
 
 	/**
 	 * Initialize vector values to given vector (copy by value)
 	 * 
 	 * @param v
 	 *            Vector
 	 */
 	public Vec(Vec v) {
 		x = v.x;
         y = v.y;
         z = v.z;
 	}
 
	/**
 	 * Calculates the reflection of the vector in relation to a given surface
 	 * normal. The vector points at the surface and the result points away.
 	 * 
 	 * @return The reflected vector
 	 */
 	public Vec reflect(Vec normal) {
         double dotProduct = dotProd(normal);
 
         Vec reflectionNormal = new Vec(normal);
         Vec reflection = new Vec(this);
 
         reflectionNormal.scale(dotProduct);
 
         reflectionNormal.negate();
 
         reflection.add(reflectionNormal);
         reflectionNormal.add(reflection);
         return reflectionNormal;
 	}
     
     public Vec(String values) {
         String[] valueArray = values.split(" ");
 
         if (valueArray.length != 3) {
             throw new IllegalArgumentException("Wrong number of parameters, got " + valueArray.length + " parameters.");
         }
 
         x = Integer.parseInt(valueArray[0]);
         y = Integer.parseInt(valueArray[1]);
         z = Integer.parseInt(valueArray[2]);
 
     }
 
     private float limitColor(double color) {
         return (float)Math.max(Math.min(color, 0), 1);
     }
     
     public Color toColor() {
         return new Color(limitColor(x), limitColor(y), limitColor(z));
     }
     
 	/**
 	 * Adds a to vector
 	 * 
 	 * @param a
 	 *            Vector
 	 */
 	public void add(Vec a) {
 		x += a.x;
         y += a.y;
         z += a.z;
 	}
 
 	/**
 	 * Subtracts from vector
 	 * 
 	 * @param a
 	 *            Vector
 	 */
 	public void sub(Vec a) {
         x -= a.x;
         y -= a.y;
         z -= a.z;
 	}
 	
 	/**
 	 * Multiplies & Accumulates vector with given vector and a. v := v + s*a
 	 * 
 	 * @param s
 	 *            Scalar
 	 * @param a
 	 *            Vector
 	 */
 	public void mac(double s, Vec a) {
 		x += s * a.x;
         y += s * a.y;
         z += s * a.z;
 	}
 
 	/**
 	 * Multiplies vector with scalar. v := s*v
 	 * 
 	 * @param s
 	 *            Scalar
 	 */
 	public void scale(double s) {
 		x *= s;
         y *= s;
         z *= s;
 	}
 
 	/**
 	 * Pairwise multiplies with another vector
 	 * 
 	 * @param a
 	 *            Vector
 	 */
 	public void scale(Vec a) {
 		x *= a.x;
         y *= a.y;
         z *= a.z;
 	}
 
 	/**
 	 * Inverses vector
 	 * 
 	 * @return Vector
 	 */
 	public void negate() {
 		this.x = -1 * this.x;
         this.y = -1 * this.y;
         this.z = -1 * this.z;
 	}
 
 	/**
 	 * Computes the vector's magnitude
 	 * 
 	 * @return Scalar
 	 */
 	public double length() {
 		return Math.sqrt(lengthSquared());
 	}
 
 	/**
 	 * Computes the vector's magnitude squared. Used for performance gain.
 	 * 
 	 * @return Scalar
 	 */
 	public double lengthSquared() {
 		return x * x + y * y + z * z;
 	}
 
 	/**
 	 * Computes the dot product between two vectors
 	 * 
 	 * @param a
 	 *            Vector
 	 * @return Scalar
 	 */
 	public double dotProd(Vec a) {
 		return x * a.x + y * a.y + z * a.z;
 	}
 
 	/**
 	 * Normalizes the vector to have length 1. Throws exception if magnitude is zero.
 	 * 
 	 * @throws ArithmeticException
 	 */
 	public void normalize() throws ArithmeticException {
 		double len = length();
 
         if (len == 0) {
             throw new ArithmeticException();
         }
 
         this.x = this.x / len;
         this.y = this.y / len;
         this.z = this.z / len;
 	}
 
 	/**
 	 * Compares to a given vector
 	 * 
 	 * @param a
 	 *            Vector
 	 * @return True if have same values, false otherwise
 	 */
 	public boolean equals(Vec a) {
 		return ((a.x == x) && (a.y == y) && (a.z == z));
 	}
 
 	/**
 	 * Returns the angle in radians between this vector and the vector
 	 * parameter; the return value is constrained to the range [0,PI].
 	 * 
 	 * @param v1
 	 *            the other vector
 	 * @return the angle in radians in the range [0,PI]
 	 */
 	public final double angle(Vec v1) {
 		return Math.acos(dotProd(v1)/(v1.length()*length()));
 	}
 
 	/**
 	 * Computes the cross product between two vectors using the right hand rule
 	 * 
 	 * @param a
 	 *            Vector1
 	 * @param b
 	 *            Vector2
 	 * @return Vector1 x Vector2
 	 */
 	public static Vec crossProd(Vec a, Vec b) {	
 		return new Vec(
                 a.y * b.z - a.z * b.y,
                 a.z * b.x - a.x * b.z,
                 a.x * b.y - a.y * b.x);
 	}
 
 	/**
 	 * Adds vectors a and b
 	 * 
 	 * @param a
 	 *            Vector
 	 * @param b
 	 *            Vector
 	 * @return a+b
 	 */
 	public static Vec add(Vec a, Vec b) {
 		Vec v = new Vec(a);
         v.add(b);
         return v;
 	}
 
 	/**
 	 * Subtracts vector b from a
 	 * 
 	 * @param a
 	 *            Vector
 	 * @param b
 	 *            Vector
 	 * @return a-b
 	 */
 	public static Vec sub(Vec a, Vec b) {
         Vec v = new Vec(a);
         v.sub(b);
         return v;
 	}
 
 	/**
 	 * Inverses vector's direction
 	 * 
 	 * @param a
 	 *            Vector
 	 * @return -1*a
 	 */
 	public static Vec negate(Vec a) {
         Vec v = new Vec(a);
         v.negate();
         return v;
 	}
 
 	/**
 	 * Scales vector a by scalar s
 	 * 
 	 * @param s
 	 *            Scalar
 	 * @param a
 	 *            Vector
 	 * @return s*a
 	 */
 	public static Vec scale(double s, Vec a) {
         Vec v = new Vec(a);
         v.scale(s);
         return v;
 	}
 
 	/**
 	 * Pair-wise scales vector a by vector b
 	 * 
 	 * @param a
 	 *            Vector
 	 * @param b
 	 *            Vector
 	 * @return a.*b
 	 */
 	public static Vec scale(Vec a, Vec b) {
         Vec v = new Vec(a);
         v.scale(b);
         return v;
 	}
 
 	/**
 	 * Compares vector a to vector b
 	 * 
 	 * @param a
 	 *            Vector
 	 * @param b
 	 *            Vector
 	 * @return a==b
 	 */
 	public static boolean equals(Vec a, Vec b) {
         return a.equals(b);
 	}
 
 	/**
 	 * Dot product of a and b
 	 * 
 	 * @param a
 	 *            Vector
 	 * @param b
 	 *            Vector
 	 * @return a.b
 	 */
 	public static double dotProd(Vec a, Vec b) {
         Vec v = new Vec(a);
         return v.dotProd(b);
 	}
 
 	/**
 	 * Returns a string that contains the values of this vector. The form is
 	 * (x,y,z).
 	 * 
 	 * @return the String representation
 	 */
 	public String toString() {
 		return "(" + this.x + ", " + this.y + ", " + this.z + ")";
 	}
 
 	@Override
 	public Vec clone() {
 		return new Vec(this);
 	}
 }
