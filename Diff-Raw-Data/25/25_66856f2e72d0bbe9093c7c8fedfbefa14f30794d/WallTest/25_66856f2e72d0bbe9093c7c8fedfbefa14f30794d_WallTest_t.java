 package com.github.joakimpersson.tda367.model.tiles.nonwalkable;
 
import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class WallTest {
 
 	private Wall wall;
 
 	@Before
 	public void setUp() throws Exception {
 		wall = new Wall();
 	}
 
 	@Test
 	public void testIsWalkable() {
 		assertFalse(wall.isWalkable());
 	}
 
 	@Test
	public void testGetTileType() {
		String expected = "wall";
		String actual = wall.getTileType();
		assertEquals(expected, actual);
	}

	@Test
 	public void testEquals() {
 		Tile otherWall = new Wall();
 		Tile otherPillar = new Pillar();
 
 		// testing for null and self
 		assertTrue(wall.equals(wall));
 		assertFalse(wall.equals(null));
 
 		// should be true
 		assertTrue(wall.equals(otherWall));
 
 		// should be false since an box is not an wall
 		assertFalse(wall.equals(otherPillar));
 	}
 
 	@Test
 	public void testHashCode() {
 
 		Tile otherWall = new Wall();
 		Tile otherPillar = new Pillar();
 
 		// should be true
 		assertTrue(wall.hashCode() == otherWall.hashCode());
 
 		// should be false since an box is not an wall
 		assertFalse(wall.hashCode() == otherPillar.hashCode());
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		wall = null;
 	}
 }
