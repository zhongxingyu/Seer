 package com.secondhand.model;
 
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;

 import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
 
 import junit.framework.TestCase;
 
 public class EntityTest extends TestCase {
 
 	public void testConstructor() {
 		Vector2 vector = new Vector2();
 		float rad = 5;
 		boolean isEdible = true;
		// TODO add test for Shape parameter
		Entity entity = new Entity(vector, rad, null, isEdible) {};
 		
 		assertEquals(entity.getRadius(), rad);
 		assertEquals(entity.isEdible(), isEdible);
 	}
 	
 	public void testSetRadius() {
 		Vector2 vector = new Vector2();
 		float rad = 5;
		boolean isEdible = true;
		Entity entity = new Entity(vector, rad, null, isEdible) {};
 		
 		assertEquals(entity.getRadius(), rad);
 		rad = 3;
 		entity.setRadius(rad);
 		assertEquals(entity.getRadius(), rad);
 	}
 	
	public void testSetBody() {
		Vector2 vector = new Vector2();
		float rad = 5;
		boolean isEdible = true;
		Entity entity = new Entity(vector, rad, null, isEdible) {};
		
		Body testBody = new PhysicsWorld(new Vector2(),true).createBody(new BodyDef());
		entity.setBody(testBody);
		assertEquals(entity.getBody(), testBody);
	}
 }
