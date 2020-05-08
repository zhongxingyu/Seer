 package org.racenet.framework;
 
 /**
  * Represents a polygon defined by at
  * least three points
  * 
  * @author soh#zolex
  */
 public class Polygon {
 
 	public static final short TOP = 0;
 	public static final short LEFT = 1;
 	public static final short RAMPUP = 2;
 	public static final short RAMPDOWN = 3;
 	public static final short BOTTOM = 4;
 	
 	/**
 	 * The borders of the polygon represented by
 	 * line segments
 	 */
 	public Vector2[] vertices;
 	public float width, height;
 	
 	/**
 	 * Structure which holds information about a collision
 	 *
 	 */
 	public class CollisionInfo {
 		
 		public boolean collided;
 		public short type;
 		public float distance;
 	}
 	
 	/**
 	 * Initialize a new polygon using multiple points
 	 * 
 	 * @param Vector2 ... points
 	 */
 	public Polygon(Vector2 ... vertices) {
 		
 		this.vertices = vertices;
 		this.calcWidth();
 		this.calcHeight();
 	}
 	
 	/**
 	 * Check if this polygon intersects with another one
 	 * 
 	 * NOTE: This is a very simplified collision detection.
 	 * "this" is always the player, an axis aligned rectangle.
 	 * "other" my be a rectangular triangle or an axis aligned rectangle
 	 * 
 	 * @param Polygon other
 	 * @return boolean
 	 */
 	public CollisionInfo intersect(Polygon other){
 				
 		CollisionInfo info = new CollisionInfo();
 		info.collided = false;
 		
 		float thisX = this.getPosition().x;
 		float thisY = this.getPosition().y;
 		
 		float otherX = other.getPosition().x;
 		float otherY = other.getPosition().y;
 		
 		if (other.vertices.length == 4) {
 			
 			float otherHeight = other.getHeightAt(thisX);
 			
 			if (thisX + this.width > otherX && thisX < otherX + other.width &&
 				thisY + this.height > otherY && thisY + this.height < otherY + otherHeight) {
 				
 				float distanceX = thisX + this.width - otherX;
				float distanceY = otherY - thisY + this.height;
 				
 				if (distanceX < distanceY && other.height > this.height) {
 					
 					info.type = LEFT;
 					info.distance = distanceX;
 					
 				} else {
 					
 					info.type = BOTTOM;
 					info.distance = distanceY;
 				}
 				
 				info.collided = true;
 				return info;
 			}
 			
 			if (thisX + this.width > otherX && thisX < otherX + other.width &&
 				thisY > otherY && thisY < otherY + otherHeight) {
 				
 				float distanceX = thisX + this.width - otherX;
 				float distanceY = otherY + other.height - thisY;
 				
 				if (distanceX < distanceY && other.height > this.height) {
 					
 					info.type = LEFT;
 					info.distance = distanceX;
 					
 				} else {
 					
 					info.type = TOP;
 					info.distance = distanceY;
 				}
 				
 				info.collided = true;
 				return info;
 			}
 			
 		} else if (other.vertices.length == 3) {
 		
 			// ramp up
 			if (other.vertices[1].x == other.vertices[2].x) {
 				
 				float otherHeight = other.getHeightAt(thisX);
 				if (thisX + this.width > otherX && thisX < otherX + other.width && thisY <= otherY + otherHeight) {
 					
 					info.collided = true;
 					info.distance = 0;
 					info.type = RAMPUP;
 					return info;
 				}
 			}
 			
 			// ramp down
 			if (other.vertices[0].x == other.vertices[1].x) {
 				
 				float otherHeight = other.getHeightAt(thisX);
 				if (thisX + this.width > otherX && thisX < otherX + other.width && thisY <= otherY + otherHeight) {
 
 					info.collided = true;
 					info.distance = 0;
 					info.type = RAMPDOWN;
 					return info;
 				}
 			}
 		}
 		
 		return info;
 	}
 	
 	public float getHeightAt(float x) {
 		
 		if (this.vertices.length == 3) {
 		
 			// ramp up
 			if (this.vertices[1].x == this.vertices[2].x) {
 				
 				return (this.vertices[2].y - this.vertices[0].y) / (this.vertices[2].x - this.vertices[0].x) * (x - this.vertices[0].x);
 			}
 			
 			// ramp down
 			if (this.vertices[0].x == this.vertices[1].x) {
 				
 				return (this.vertices[0].y - this.vertices[2].y) / (this.vertices[0].x - this.vertices[2].x) * (x - this.vertices[2].x) - this.height;
 			}
 		}
 		
 		return this.height;
 	}
 	
 	/**
 	 * Calculate the width of the polygon by determining
 	 * the minimal and maximal x coordinates
 	 * 
 	 * @return float
 	 */
 	public void calcWidth() {
 		
 		float minX = 320000000;
 		float maxX = -320000000;
 		int length = this.vertices.length;
 		for (int i = 0; i < length; i++) {
 			
 			if (this.vertices[i].x < minX) minX = this.vertices[i].x;
 			if (this.vertices[i].x > maxX) maxX = this.vertices[i].x;
 		}
 		
 		this.width = maxX - minX;
 	}
 
 	/**
 	 * Calculate the height of the polygon by determining
 	 * the minimal and maximal x coordinates
 	 * 
 	 * @return float
 	 */
 	public void calcHeight() {
 		
 		float minY = 320000000;
 		float maxY = -320000000;
 		int length = this.vertices.length;
 		for (int i = 0; i < length; i++) {
 			
 			if (this.vertices[i].y < minY) minY = this.vertices[i].y;
 			if (this.vertices[i].y > maxY) maxY = this.vertices[i].y;
 		}
 		
 		this.height = maxY - minY;
 	}
 	
 	/**
 	 * TODO: for now just use the first given
 	 * point as the position
 	 * 
 	 * @return Vector2
 	 */
 	public Vector2 getPosition() {
 		
 		return this.vertices[0];
 	}
 	
 	/**
 	 * Set the position by moving all borders
 	 * of the polygon
 	 * 
 	 * @param Vector2 position
 	 */
 	public void setPosition(Vector2 position) {
 		
 		float diffX = position.x - this.getPosition().x;
 		float diffY = position.y - this.getPosition().y;
 		
 		int length = this.vertices.length;
 		for (int i = 0; i < length; i++) {
 			
 			this.vertices[i].x += diffX;
 			this.vertices[i].y += diffY;
 		}
 	}
 	
 	/**
 	 * Set the position by moving all borders
 	 * of the polygon
 	 * 
 	 * @param Vector2 position
 	 */
 	public void addToPosition(float x, float y) {
 		
 		int length = this.vertices.length;
 		for (int i = 0; i < length; i++) {
 			
 			this.vertices[i].x += x;
 			this.vertices[i].y += y;
 		}
 	}
 }
