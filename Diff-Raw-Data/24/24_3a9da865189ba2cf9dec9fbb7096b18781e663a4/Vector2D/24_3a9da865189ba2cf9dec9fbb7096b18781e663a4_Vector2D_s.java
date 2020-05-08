 package core.engine;
 
 public class Vector2D {
 	public double x;
 	public double y;
 	public Vector2D(double x, double y){
 		this.x = x;
 		this.y = y;
 	}
 	public static Vector2D transform(Vector2D point, double trans[][]){
 		Vector2D result = new Vector2D(0,0);
 		result.x = trans[0][0]*point.x + trans[0][1]*point.y;
 		result.y = trans[1][0]*point.x + trans[1][1]*point.y;
 		return result;
 	}
 	public static double[][] rotationMatrix(double angle){
 		double [][] result = new double [2][2];
 		result [0][0] = Math.cos(angle);
 		result [0][1] = -Math.sin(angle);
 		result [1][0] = Math.sin(angle);
 		result [1][1] = Math.cos(angle);
 		return result;
 	}
 	public static Vector2D add(Vector2D v1, Vector2D v2){
 		return new Vector2D (v1.x+v2.x, v1.y+v2.y);
 	}
 	public static Vector2D dot(Vector2D v1, Vector2D v2){
 		return new Vector2D (v1.x*v2.x, v1.y*v2.y);
 	}
 }
