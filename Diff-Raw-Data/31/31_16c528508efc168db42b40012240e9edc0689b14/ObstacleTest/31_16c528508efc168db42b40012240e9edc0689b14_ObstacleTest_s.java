 package com.secondhand.model;
 
import com.badlogic.gdx.math.Vector2;

 import junit.framework.TestCase;
 
 public class ObstacleTest extends TestCase{
 
 	public void testConstructor() {
 
 		Vector2 pos = new Vector2(2f, 4f);
 		float rad = 3.2f;
 		
		Obstacle obstacle = new Obstacle(pos, rad);
 		
 		assertEquals(rad, obstacle.getRadius());
		assertEquals(pos.x, obstacle.getPosition().x);
		assertEquals(pos.y, obstacle.getPosition().y);
 	}
 	
 }
