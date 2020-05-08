 package pl.edu.agh.student.pathfinding.tests;
 
 import static org.junit.Assert.*;
 
 import java.awt.Point;
 import java.io.File;
 import java.io.IOException;
 
 import org.junit.Test;
 
 import pl.edu.agh.student.pathfinding.map.BitmapMap;
 
 public class BitmapMapTest {
 	
 	@Test
 	public void startEndMapTest() throws IOException {
 		BitmapMap map = new BitmapMap(new File("maps/BitmapMapTest/simple_with_start_and_end.png"));
 		assertEquals("Wrong starting point!", new Point(0, 0), map.getStartingPoint());
 		assertEquals("Wrong ending point!", new Point(29, 29), map.getFinishPoint());
 	}
 	
 	@Test
 	public void mapWithoutEndTest() {
 		try {
 			new BitmapMap(new File("maps/BitmapMapTest/simple_without_end.png"));
 			fail();
 		} catch (IOException e) {
 			// expected behavior
 		}
 	}
 	
 	@Test
 	public void mapWithoutStartTest() {
 		try {
 			new BitmapMap(new File("maps/BitmapMapTest/simple_without_start.png"));
 			fail();
 		} catch (IOException e) {
 			// expected behavior
 		}
 	}
 	
 	@Test
 	public void pointsAccessibilityTest() throws IOException {
 		BitmapMap map = new BitmapMap(new File("maps/BitmapMapTest/accesibility_test.png"));
 		assertTrue("Point [0,1] should be accesible, but it is not.", map.isAccessible(new Point(0, 1)));
 		assertFalse("Point [1,0] should not be accesible, but it is.", map.isAccessible(new Point(1, 0)));
 	}
 	
 	@Test
 	public void pointCostTest() throws IOException {
 		BitmapMap map = new BitmapMap(new File("maps/BitmapMapTest/accesibility_test.png"));
		assertEquals("Got wrong cost!", 205*3, map.getCost(new Point(13, 15)));
 	}
 	
 }
