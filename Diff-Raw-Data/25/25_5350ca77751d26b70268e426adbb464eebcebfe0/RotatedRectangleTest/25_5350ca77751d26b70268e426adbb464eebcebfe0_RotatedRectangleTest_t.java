 package uk.co.samatkins.test;
 
 import static org.junit.Assert.*;
 
import org.junit.Before;
 import org.junit.Test;
 
import com.badlogic.gdx.math.Vector2;

import uk.co.samatkins.RotatedRectangle;

 public class RotatedRectangleTest {
	
	private RotatedRectangle rect;
	
	@Before
	public void start() {
		rect = new RotatedRectangle(20, 10, 100, 100, 45);
	}
 
 	@Test
 	public void testGetCorners() {
		Vector2[] corners = rect.getCorners();
		assertEquals("4 corners returned", 4, corners.length);
 	}
 
 	@Test
 	public void testGetBoundingRectangle() {
 		fail("Not yet implemented"); // TODO
 	}
 
 	@Test
 	public void testGetPolygon() {
 		fail("Not yet implemented"); // TODO
 	}
 
 }
