 package math;
 
 import java.io.Serializable;
 
 // Differentiates between position and direction.
 // Vectors should be used for directions, not positions.
 public class Vector3 implements Serializable {
     private static final long serialVersionUID = -1520340574791669154L;
     public static final Vector3 UNIT_X = new Vector3(1, 0, 0);
     public static final Vector3 UNIT_Y = new Vector3(0, 1, 0);
     public static final Vector3 UNIT_Z = new Vector3(0, 0, 1);
     public static final Vector3 ORIGIN = new Vector3(0.0f,0.0f,0.0f);
     public static final Vector3 ZERO = ORIGIN; // makes semantic differences in some if checks
     private static java.util.Random gen = new java.util.Random();
 
     public float x,y,z;
 
     public Vector3(){
         x = 0.0f;
         y = 0.0f;
         z = 0.0f;
     }
 
     public Vector3(float x, float y, float z){
         this.x = x;
         this.y = y;
         this.z = z;
     }
 
     public Vector3(Vector3 xyz) {
         this.x = xyz.x;
         this.y = xyz.y;
         this.z = xyz.z;
     }
 
     public String toString() {
         return String.format("<%g, %g, %g>", x, y, z);
     }
     
     public float[] toFloatArray(){
         float[] array = new float[3];
         array[0] = x;
         array[1] = y;
         array[2] = z;
         return array;
     }
 
     public Vector3 times(float scalar){
         return new Vector3( x * scalar, y * scalar, z * scalar);
     }
 
     public Vector3 plus(Vector3 other) {
         return new Vector3(x + other.x, y + other.y, z + other.z);
     }
 
     public Vector3 plusEquals(Vector3 other){
         x += other.x;
         y += other.y;
         z += other.z;
 
         return this;
     }
 
     public Vector3 normalize(){
         float invserseMagnitude = 1.0f / magnitude();
 
         x *= invserseMagnitude;
         y *= invserseMagnitude;
         z *= invserseMagnitude;
         return this;
     }
     /*
      * Returns the dot product between this and another vector
      */
     public float dotProduct(Vector3 other){
         return this.x * other.x + this.y * other.y + this.z * other.z;
     }
 
     /**
      * 
      * @param rhs the right hand side of the cross product
      * @return the cross product of the this vector with other
      */
     public Vector3 cross(Vector3 rhs){
         return new Vector3(
                 y * rhs.z - z * rhs.y,
                 -(x * rhs.z - z * rhs.x),
                 x * rhs.y - y * rhs.x
         );
     }
      
     /**
      * Returns true if this and the input vector < 90 degrees apart
      * @param b
      * @return
      */
     public boolean sameDirection(Vector3 b){
         return dotProduct(b) > 0;
     }
 
     public Vector3 times(Quaternion rotation) {
         float[] m = rotation.toGlMatrix();
 
         return times(m);
     }
 
     public Vector3 times(float[] m) {
         Vector3 result = new Vector3();
 
         // NOTE we are assuming this is just a rotation matrix
         result.x = x * m[0] + y * m[4] + z * m[8];
         result.y = x * m[1] + y * m[5] + z * m[9];
         result.z = x * m[2] + y * m[6] + z * m[10];
         return result;
     }
 
     public float magnitude() {
         return (float)Math.sqrt(x*x + y*y + z*z);
     }
 
     /**
      * Subtracts Vectors
      * @param Vector to subtract
      * @return 
      */
     public Vector3 minus(Vector3 other) {
         return new Vector3(x - other.x, y - other.y, z - other.z);
     }
     public Vector3 minusEquals(Vector3 other) {
         x -= other.x;
         y -= other.y;
         z -= other.z;
 
         return this;
     }
 
     /**
      * 
      * @param other the other vector to compare against
      * @return checks if the members of this vector are equal to another
      *         and returns true if they are
      */
     public boolean equals(Vector3 other){
         return x == other.x && y == other.y && z == other.z;
     }
 
     /**
      * Override the object equals method so it doesn't get explicitly called accidentally
      */
     public boolean equals(Object other){
         if(other instanceof Vector3)
             return equals((Vector3)other);
         else return false;
     }
 
     /**
      * @return The square of the magnitude of this
      */
     public float magnitude2() {
         return x * x + y * y + z * z;
     }
 
     /**
      * 
      * @return The negation of this vector
      */
     public Vector3 negate() {
         return this.times(-1.0f);
     }
 
     /**
      * Generates a new random vector with each component between +max -max
      * @param max
      */
     public static Vector3 randomPosition(float max) {
         return new Vector3(
                 gen.nextFloat() * 2 * max - max,
                 gen.nextFloat() * 2 * max - max,
                 gen.nextFloat() * 2 * max - max);
     }
 
     public Vector3 timesEquals(Quaternion rotation) {
         // TODO OPTIMIZE
         Vector3 temp = this.times(rotation);
         x = temp.x;
         y = temp.y;
         z = temp.z;
         return this;
     }
 }
