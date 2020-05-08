 package tests;
 
 import static org.junit.Assert.*;
 
 import math.Vector2;
 
 import org.junit.Test;
 
 public class Vector2Tests {
 	
 	@Test
 	public void testAddition() {
 	
 		float x1 = (float)Math.random() * 10;
 		float x2 = (float)Math.random() * 10;
 		float y1 = (float)Math.random() * 10;
 		float y2 = (float)Math.random() * 10;
 		
 		Vector2 vector0 = new Vector2(x1,y1);
 		Vector2 vector1 = new Vector2(x2, y2);
 		
 		Vector2 result = Vector2.add(vector0, vector1);
 		
 		assertTrue(result.X == x1 + x2);
 		assertTrue(result.Y == y1 + y2);
 	}
 }
