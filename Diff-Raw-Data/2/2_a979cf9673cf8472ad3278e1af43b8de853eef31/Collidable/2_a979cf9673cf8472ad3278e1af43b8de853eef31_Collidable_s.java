 package org.bgj05.entity;
 
 import org.lwjgl.util.Rectangle;
 import org.lwjgl.util.vector.Vector2f;
 
 public class Collidable {		
 	protected Rectangle boundingBox;
 	public Rectangle boundingBox() { return boundingBox; }
 	
 	public Collidable(Vector2f pos, Vector2f dim) {		
 		this.boundingBox = new Rectangle((int) pos.x, 
 				(int) pos.y, (int) dim.x, (int) dim.y);
 	}
 	
 	public boolean collides(Collidable collidable) {
		return collidable.boundingBox.intersects(this.boundingBox);
 	}
 }
