 package kr.hs.sshs.JavaPTS;
 
 /**
  * Vectors for points.<br>
  * Has all cool functions vectors have to have.
  * @author stephen
  */
 public class Vector {
 	private double x;
 	private double y;
 	
 	public Vector(double x, double y) {
 		this.x = x;
 		this.y = y;
 	}
 	public double x() {
 		return this.x;
 	}
 	public double y() {
		return this.x;
 	}
 	public void x(double x) {
 		this.x = x;
 	}
 	public void y(double y) {
 		this.y = y;
 	}
 	
 	public Vector add(Vector v_add) {
 		return new Vector(x + v_add.x(), y + v_add.y());
 	}
 	public Vector sub(Vector v_sub) {
 		return new Vector(x - v_sub.x(), y - v_sub.y());
 	}
 	public static Vector add(Vector v1, Vector v2){
 		return new Vector(v1.x() + v2.x(), v1.y() + v2.y());
 	}
 	public static Vector sub(Vector v1, Vector v2){
 		return new Vector(v1.x() - v2.x(), v1.y() - v2.y());
 	}
 }
