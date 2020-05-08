 package com.github.joakimpersson.tda367.model.tiles.walkable;
 
 import static org.hamcrest.CoreMatchers.instanceOf;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 import com.github.joakimpersson.tda367.model.tiles.walkable.Fire;
 import com.github.joakimpersson.tda367.model.utils.Position;
 
 //TODO Fix the test for the new returntype: Map<Position, Direction>.
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class FireTest {
 
 	private Fire fire;
 
 	@Before
 	public void setUp() throws Exception {
 		fire = new Fire(null);
 	}
 
 	@Test
 	public void testGetToughness() {
 		// fire should not be able to destroy other fires
 		assertEquals(100, fire.getToughness());
 	}
 
 	@Test
 	public void testOnFire() {
 		Tile tile = fire.onFire();
		assertThat(tile, is(instanceOf(Fire.class)));
 	}
 
 	@Test
 	public void testIsWalkable() {
 		assertTrue(fire.isWalkable());
 	}
 
 	@Test
 	public void testPlayerEnter() {
 		Position pos = new Position(0, 0);
 		Player player = new Player("Kalle", pos);
 		int expected = player.getHealth() - 1;
 		fire.playerEnter(player);
 		int actual = player.getHealth();
 		assertEquals(expected, actual);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		fire = null;
 	}
 }
