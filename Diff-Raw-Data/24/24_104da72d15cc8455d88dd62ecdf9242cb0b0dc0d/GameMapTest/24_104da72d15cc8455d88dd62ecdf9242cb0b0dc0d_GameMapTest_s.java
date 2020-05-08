 package com.github.joakimpersson.tda367.model.map;
 
 import static org.hamcrest.CoreMatchers.instanceOf;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.joakimpersson.tda367.model.constants.Parameters;
 import com.github.joakimpersson.tda367.model.positions.Position;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 import com.github.joakimpersson.tda367.model.tiles.nonwalkable.Box;
 import com.github.joakimpersson.tda367.model.tiles.nonwalkable.Pillar;
 import com.github.joakimpersson.tda367.model.tiles.nonwalkable.Wall;
 import com.github.joakimpersson.tda367.model.tiles.walkable.Floor;
 import com.github.joakimpersson.tda367.model.utils.MapLoader;
 import com.github.joakimpersson.tda367.model.utils.Utils;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class GameMapTest {
 	private GameMap map;
 
 	@Before
 	public void setUp() throws Exception {
 		MapLoader mapLoader = MapLoader.getInstance();
		Tile[][]gameField = mapLoader.getMap(0);
 		map = new GameMap(gameField);
 
 	}
 
 	@Test
 	public void testSetTile() {
 		Position pos = new Position(5, 6);
 		Tile expected = new Box();
 		map.setTile(expected, pos);
 		Tile actual = map.getTile(pos);
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testGetTile() {
 
 		Tile wall = map.getTile(new Position(0, 0));
 		Tile floor = map.getTile(new Position(1, 1));
 		Tile pillar = map.getTile(new Position(2, 2));
 		Tile box = map.getTile(new Position(6, 9));
 
 		assertThat(wall, is(instanceOf(Wall.class)));
 		assertThat(floor, is(instanceOf(Floor.class)));
 		assertThat(pillar, is(instanceOf(Pillar.class)));
 		assertThat(box, is(instanceOf(Box.class)));
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testGetTileException() {
 		Position pos = new Position(1, 15);
 		map.getTile(pos);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void setTileException() {
 		Position pos = new Position(1, -11);
 		map.setTile(new Floor(), pos);
 	}
 
 	@Test
 	public void testGetMap() {
 
 		Tile[][] mapTmp = map.getMap();
 
 		// checking that the map has correct size
 		int expectedWidth = Parameters.INSTANCE.getMapSize().width;
 		int expectedHeight = Parameters.INSTANCE.getMapSize().height;
 		int actualHeight = mapTmp.length;
 		int actualWidth = 0;
 
 		assertEquals(expectedHeight, actualHeight);
 
 		for (int i = 0; i < mapTmp.length; i++) {
 			actualWidth = mapTmp[i].length;
 			assertEquals(expectedWidth, actualWidth);
 		}
 
 		// checking that the matrix only contains classes that implements the
 		// tile interface
 
 		for (int i = 0; i < mapTmp.length; i++) {
 			for (int j = 0; j < mapTmp[i].length; j++) {
 				Tile tmpTile = mapTmp[i][j];
 				assertThat(tmpTile, is(instanceOf(Tile.class)));
 			}
 		}
 	}
 
 	@Test
 	public void testReset() {
 		Tile[][] expected = Utils.copyGameMap(map.getMap());
 
 		// modify the current map
 		modifyMap();
 
 		Tile[][] actual = map.getMap();
 		boolean result = Utils.mapMatrixEquals(expected, actual);
 		assertFalse(result);
 
 		// reset the current map
 		map.reset();
 
 		actual = map.getMap();
 
 		// testing the all the tiles in the two maps are equal
 		result = Utils.mapMatrixEquals(expected, actual);
 		assertTrue(result);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		map = null;
 	}
 
 	private void modifyMap() {
 		Position pos = new Position(5, 3);
 		map.setTile(new Pillar(), pos);
 		pos = new Position(5, 4);
 		map.setTile(new Pillar(), pos);
 		pos = new Position(5, 5);
 		map.setTile(new Pillar(), pos);
 	}
 }
