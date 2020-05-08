 package util.math;
 
 import game.Main;
 import java.util.logging.Level;
 
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.util.vector.Matrix3f;
 import org.lwjgl.util.vector.Vector3f;
 
 
 /**
  * 
  * @author Felix & Michael
  *
  */
 public abstract class MathHelper
 {
 	/**
 	 * The pythagorean-theorem
 	 * @param a the first cathetus
 	 * @param b the second cathetus
 	 * @return the hypotenuse
 	 */
 	public static double pyth(float a, float b)
 	{
 		return Math.sqrt (a * a + b * b);
 	}
 	
 	/**
 	 * Clamps a value between two others
 	 * @param value
 	 * @param min
 	 * @param max
 	 * @return min if the value is smaller then min, max if the value is greater than max and the value itself if it is between min and max
 	 */
 	public static float clamp (float value, float min, float max)
 	{
 		return Math.max(min, Math.min(value, max));
 	}
 	
 	/**
 	 * This method copies a vector and returns the copy. The copy contains no references to the original vector
 	 * @param vector
 	 * @return A copy of the vector
 	 */
 	public static Vector3f cloneVector(Vector3f vector)
 	{
 		return new Vector3f(vector.x, vector.y, vector.z);
 	}
 	
 	public static Vector3f[] cloneVectorArray(Vector3f[] vArr)
 	{
 		ArrayList<Vector3f> newArr = new ArrayList<Vector3f>();
 		for(Vector3f v : vArr)
 			newArr.add(cloneVector(v));
 		return (Vector3f[])newArr.toArray();
 	}
 	
 	/**
 	 * A method for rotating a vector in a plane (around its normal vector) about degree degree
 	 * @param vector the vector which should be rotated
 	 * @param degree the degree about which the vector should be rotated
 	 * @param rotationPlane the plane within the vector should be rotated
 	 */
 	public static void rotateVector(Vector3f vector, float degree, Plane rotationPlane)
 	{
 		float tempDegree = (float)Math.toRadians(degree);
 		rotationPlane.transformToHesseNormalForm();
 		//Creating a Matrix for rotating the Vector
 		Matrix3f rotationMatrix = new Matrix3f();
 		rotationMatrix.m00 = (float)(rotationPlane.getNormal().x * rotationPlane.getNormal().x * (1 - Math.cos (tempDegree)) + Math.cos (tempDegree));
 		rotationMatrix.m01 = (float)(rotationPlane.getNormal().x * rotationPlane.getNormal().y * (1 - Math.cos (tempDegree)) - rotationPlane.getNormal().z * Math.sin (tempDegree));
 		rotationMatrix.m02 = (float)(rotationPlane.getNormal().x * rotationPlane.getNormal().z * (1 - Math.cos (tempDegree)) + rotationPlane.getNormal().y * Math.cos (tempDegree));
 		rotationMatrix.m10 = (float)(rotationPlane.getNormal().y * rotationPlane.getNormal().x * (1 - Math.cos (tempDegree)) + rotationPlane.getNormal().z * Math.cos (tempDegree));
 		rotationMatrix.m11 = (float)(rotationPlane.getNormal().y * rotationPlane.getNormal().y * (1 - Math.cos (tempDegree)) + Math.cos (tempDegree));
		rotationMatrix.m12 = (float)(rotationPlane.getNormal().y * rotationPlane.getNormal().z * (1 - Math.cos (tempDegree)) - rotationPlane.getNormal().x * Math.cos (tempDegree));
		rotationMatrix.m20 = (float)(rotationPlane.getNormal().z * rotationPlane.getNormal().x * (1 - Math.cos (tempDegree)) - rotationPlane.getNormal().y * Math.cos (tempDegree));
 		rotationMatrix.m21 = (float)(rotationPlane.getNormal().z * rotationPlane.getNormal().y * (1 - Math.cos (tempDegree)) + rotationPlane.getNormal().x * Math.cos (tempDegree));
 		rotationMatrix.m22 = (float)(rotationPlane.getNormal().z * rotationPlane.getNormal().z * (1 - Math.cos (tempDegree)) + Math.cos (tempDegree));
 		Vector3f temp = cloneVector(vector);
 		//adopt rotatoinMatrix on vector
 		Matrix3f.transform (rotationMatrix, temp, vector);
 	}
 	
 	/**
 	 * creates a normalised perpendicular vector to the starting vector
 	 * @param startingVector the Vector on which the other one should be vertical on
 	 * @return the perpendicular vector
 	 */
 	public static Vector3f createPerpendicularVector(Vector3f startingVector)
 	{
 		Vector3f temp = new Vector3f(1, 1, 1);
 		if(startingVector == temp)
 			temp = new Vector3f(1, -2, 3);
 		return Vector3f.cross(startingVector, temp, null);
 	}
 	
 	public static float calculateDistancePointToPoint(Vector3f point1, Vector3f point2){
 		float distance = 0;
 		Vector3f temp = new Vector3f();
 		Vector3f.sub(point1, point2, temp);
 		distance = (float) Math.sqrt(temp.getX()*temp.getX()+temp.getY()*temp.getY()+temp.getZ()*temp.getZ());
 		return distance;
 	}
 
 	public static FloatBuffer asFloatBuffer(float[] fs)
 	{
 		FloatBuffer buffer = BufferUtils.createFloatBuffer(fs.length);
 		buffer.put(fs);
 		buffer.flip();
 		return buffer;
 	}
 }
