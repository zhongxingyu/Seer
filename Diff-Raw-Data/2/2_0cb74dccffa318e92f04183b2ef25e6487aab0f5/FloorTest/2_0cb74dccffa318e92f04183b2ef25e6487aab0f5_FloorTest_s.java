 package com.github.joakimpersson.tda367.model.tiles.walkable;
 
 import static org.hamcrest.CoreMatchers.instanceOf;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.joakimpersson.tda367.model.constants.PointGiver;
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.positions.Position;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 import com.github.joakimpersson.tda367.model.tiles.walkable.Floor;
 
 public class FloorTest {
 
 	private Floor floor;
 
 	@Before
 	public void setUp() throws Exception {
 		floor = new Floor();
 	}
 
 	@Test
 	public void testGetToughness() {
 		assertEquals(0, floor.getToughness());
 	}
 
 	@Test
 	public void testOnFire() {
 		Tile tile = floor.onFire();
 		assertThat(tile, is(instanceOf(Floor.class)));
 	}
 
 	@Test
 	public void testPlayerEnter() {
 		Position pos = new Position(1, 1);
 		Player player = new Player(1, "Kalle", pos);
 		Tile tile = floor.playerEnter(player);
 		assertThat(tile, is(instanceOf(Floor.class)));
 	}
 
 	@Test
 	public void testIsWalkable() {
 		assertTrue(floor.isWalkable());
 	}
 
 	@Test
 	public void testGetTileType() {
 		String expected = "floor";
 		String actual = floor.getTileType();
		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testGetPointGiver() {
 		PointGiver expected = PointGiver.Floor;
 		PointGiver actual = floor.getPointGiver();
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testEquals() {
 		Tile otherFloor = new Floor();
 		Tile otherTile = new BombStackUpdateItem();
 
 		// testing for null and self
 		assertTrue(floor.equals(floor));
 		assertFalse(floor.equals(null));
 
 		// should be true
 		assertTrue(floor.equals(otherFloor));
 
 		// should be false since an box is not an wall
 		assertFalse(floor.equals(otherTile));
 	}
 
 	@Test
 	public void testHashCode() {
 
 		Tile otherFloor = new Floor();
 		Tile otherTile = new BombStackUpdateItem();
 
 		// should be true
 		assertTrue(floor.hashCode() == otherFloor.hashCode());
 
 		// should be false since an box is not an wall
 		assertFalse(floor.hashCode() == otherTile.hashCode());
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		floor = null;
 	}
 
 }
