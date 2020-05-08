 package math;
 
 public final class Vector2 {
 	
 	public final float X;
 	public final float Y;
 	
 	/**
 	 * Creates a new instance of a Vector2.
  	 * @param x - xCoordinate of the vector.
 	 * @param y - yCoordinate of the vector.
 	 */
 	public Vector2(float x, float y) {
 		this.X = x;
 		this.Y = y;
 	}
 	
 	/**Adds 2 vectors together.
 	 * 
 	 * @param vector0 the first vector.
 	 * @param vector1 the second vector.
 	 * @return the vector result.
 	 */
 	public static Vector2 add(Vector2 vector0, Vector2 vector1) {
 		return new Vector2(vector0.X + vector1.X, vector0.Y + vector1.Y);
 	}
 	
 	/**Subtracts one vector from the other.
 	 * 
 	 * @param vector0 the vector operated on.
 	 * @param vector1 the vector to subtract.
 	 * @return the vector result of the subtraction.
 	 */
 	public static Vector2 subtract(Vector2 vector0, Vector2 vector1) {
 		return new Vector2(vector0.X - vector1.X, vector0.Y - vector1.Y);
 	}
 	
 	/**Multiplies a vector by a scalar.
 	 * 
 	 * @param scalar the scalar.
 	 * @param vector the vector.
 	 * @return the multiplication result.
 	 */
 	public static Vector2 mul(float scalar, Vector2 vector) {
 		return new Vector2(scalar * vector.X, scalar * vector.Y);
 	}
 	
 	/**Calculates the dot product( scalar product) of 2 vectors.
 	 * 
 	 * @param vector0 the first vector.
 	 * @param vector1 the second vector.
 	 * @return the dot product result.
 	 */
 	public static float dot(Vector2 vector0, Vector2 vector1) {
 		return vector0.X * vector1.X + vector0.Y * vector1.Y;
 	}
 	
 	/**
 	 * Calculates the distance between two vectors.
 	 * @param vector0 the first vector.
 	 * @param vector1 the second vector.
 	 * @return
 	 */
 	public static float distance(Vector2 vector0, Vector2 vector1) {
 		return (float)Math.sqrt(distanceSquared(vector0, vector1));
 	}
 	
 	/**Calculates the distance squared between two vectors.
 	 * 
 	 * @param vector0
 	 * @param vector1
 	 * @return
 	 */
 	public static float distanceSquared(Vector2 vector0, Vector2 vector1) {
 		float a = vector0.X - vector1.X;
 		float b = vector0.Y - vector1.Y;
 		return (a*a + b*b);
 	}
 
 	/**
 	 * Calculates the length of the vector.
 	 * @return the length of the vector.
 	 */
 	public float length() {
 		return (float)(Math.sqrt(X*X + Y*Y));
 	}
 	
 	/**Calculates the orthogonal Projection of one vector upon another.
 	 * 
 	 * @param toProject the vector that is projected. 
 	 * @param toBeProjectedOn the vector that we project upon.
 	 * @return the orthogonal projection vector.
 	 */
 	public static Vector2 orthogonalProjection(Vector2 toProject, Vector2 toBeProjectedOn) {
 		
 		float numerator = Vector2.dot(toBeProjectedOn, toProject);
 		float denominator = Vector2.dot(toBeProjectedOn, toBeProjectedOn);
 		
 		return Vector2.mul(numerator / denominator, toBeProjectedOn);
 	}
 	
 	/**
 	 * Calculates the angle between the vectors.
 	 * @param vector0 the first.
 	 * @param vector1 the second.
 	 * @return the result of the calculation (in radians).
 	 */
 	public static float angle(Vector2 vector0, Vector2 vector1) {
 
 		float numerator = Vector2.dot(vector0, vector1);
 		float denominator = vector0.length() * vector1.length();
 		
 		return (float)Math.acos(numerator / denominator);
 	}
 	
	public float angle() {
		return (float)Math.atan2(Y, X);
	}
	
 	public static float twoPiAngle(Vector2 vector0, Vector2 vector1){
 		float temp = (float) Math.atan2(vector0.Y - vector1.Y, vector0.X - vector1.X);
 		System.out.println(temp);
 		return temp;
 	}
 	/**
 	 * Normalizes the 2-vector v
 	 * @param v
 	 * @return the normalized vector.
 	 */
 	public static Vector2 norm(Vector2 v) {
 		return Vector2.mul(1/v.length(), v);
 	}
 	
 	/**
 	 * A vector with both elements set to Zero.
 	 */
 	public final static Vector2 Zero = new Vector2(0.0f,0.0f);
 	
 	/**
 	 * A vector with both elements set to One.
 	 */
 	public final static Vector2 One = new Vector2(1.0f,1.0f);
 	
 }
