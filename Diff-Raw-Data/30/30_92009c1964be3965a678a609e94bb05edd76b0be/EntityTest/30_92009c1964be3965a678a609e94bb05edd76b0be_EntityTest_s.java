 package com.secondhand.model;
 
 import com.badlogic.gdx.math.Vector2;
import com.secondhand.opengl.Circle;
 
 import junit.framework.TestCase;
 
 public class EntityTest extends TestCase {
 
 	public void testConstructor() {
 		Vector2 vector = new Vector2();
 		float rad = 5;
		Circle circle = new Circle(0, 0, 5);
 		boolean isEdible = true;
		Entity entity = new Entity(vector, rad, circle, isEdible) {};
 		
 		assertEquals(entity.getRadius(), rad);
		assertEquals(entity.getShape(), circle);		
 		assertEquals(entity.isEdible(), isEdible);
 	}
 	
 	public void testSetRadius() {
 		Vector2 vector = new Vector2();
 		float rad = 5;
		Circle circle = new Circle(0, 0, 5);
		Entity entity = new Entity(vector, rad, circle,true) {};
 		
 		assertEquals(entity.getRadius(), rad);
 		rad = 3;
 		entity.setRadius(rad);
 		assertEquals(entity.getRadius(), rad);
 	}
 	
 }
