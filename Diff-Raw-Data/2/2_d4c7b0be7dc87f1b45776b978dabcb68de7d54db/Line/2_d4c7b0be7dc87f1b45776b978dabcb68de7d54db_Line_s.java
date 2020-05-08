 package org.racenet.framework;
 
 import android.util.Log;
 
 /**
  * Represents a line segment between two points
  * 
  * @author al
  */
 public class Line {
 
 	/**
 	 * Start point of the line
 	 */
 	public Vector2 p1;
 	
 	/**
 	 * End point of the line
 	 */
 	public Vector2 p2;
 	
 	/**
 	 * Initialize a line given by two points
 	 * 
 	 * @param Vector p1
 	 * @param Vector p2
 	 */
 	public Line(Vector2 p1, Vector2 p2) {
 		
 		this.p1 = p1;
 		this.p2 = p2;
 	}
 	
 	/**
 	 * Calculate if this line and an other intersect
 	 * 
 	 * @param Line other
 	 * @return boolean
 	 */
 	public boolean intersect(Line other) {
 		
 		// See http://en.wikipedia.org/wiki/Line-line_intersection
 		float px = ((this.p1.x * this.p2.y - this.p1.y * this.p2.x) * (other.p1.x - other.p2.x) -
 					(this.p1.x - this.p2.x) * (other.p1.x * other.p2.y - other.p1.y * other.p2.x)) /
 					((this.p1.x - this.p2.x) * (other.p1.y - other.p2.y) - (this.p1.y - this.p2.y) *
 					(other.p1.x - other.p2.x));
 		
 		float py = ((this.p1.x * this.p2.y - this.p1.y * this.p2.x) * (other.p1.y - other.p2.y) -
 					(this.p1.y - this.p2.y) * (other.p1.x * other.p2.y - other.p1.y * other.p2.x)) /
 					((this.p1.x - this.p2.x) * (other.p1.y - other.p2.y) - (this.p1.y - this.p2.y) *
 					(other.p1.x - other.p2.x));
 		
 		Float fpx = new Float(px);
 		Float fpy = new Float(py);
 		
 		// in case the two lines are parallels or behind each other
 		if (fpx.compareTo(Float.NaN) == 0 || fpx.compareTo(Float.NEGATIVE_INFINITY) == 0 || fpx.compareTo(Float.POSITIVE_INFINITY) == 0 ||
 			fpy.compareTo(Float.NaN) == 0 || fpy.compareTo(Float.NEGATIVE_INFINITY) == 0 || fpy.compareTo(Float.POSITIVE_INFINITY) == 0) {
 			
 			return false;
 		}
 		
 		float l1l = this.p1.x < this.p2.x ? this.p1.x : this.p2.x;
 		float l1r = this.p1.x > this.p2.x ? this.p1.x : this.p2.x;
 		float l1b = this.p1.y < this.p2.y ? this.p1.y : this.p2.y;
 		float l1t = this.p1.y > this.p2.y ? this.p1.y : this.p2.y;
 		
 		float l2l = other.p1.x < other.p2.x ? other.p1.x : other.p2.x;
 		float l2r = other.p1.x > other.p2.x ? other.p1.x : other.p2.x;
 		float l2b = other.p1.y < other.p2.y ? other.p1.y : other.p2.y;
 		float l2t = other.p1.y > other.p2.y ? other.p1.y : other.p2.y;
 		
		// see of the intersection point is within the lines
 		if (px < l1l || px > l1r || px < l2l || px > l2r || py < l1b || py > l1t || py < l2b || py > l2t) {
 			
 			return false;
 			
 		} else {
 			
 			Log.d("INTERSECT", "x " + String.valueOf(new Float(px)) + " y " + String.valueOf(new Float(py)));
 			
 			return true;
 		}
 	}
 }
