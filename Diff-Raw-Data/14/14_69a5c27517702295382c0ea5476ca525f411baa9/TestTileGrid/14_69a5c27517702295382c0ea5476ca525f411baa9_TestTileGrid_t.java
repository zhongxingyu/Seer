 package rsmg.model;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.awt.Point;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import rsmg.model.object.unit.PCharacter;
 import rsmg.model.tile.AirTile;
 import rsmg.model.tile.EndTile;
 import rsmg.model.tile.GroundTile;
 import rsmg.model.tile.SpawnTile;
 import rsmg.model.tile.Tile;
 
 public class TestTileGrid {
 
 	TileGrid grid;
 	
 	@Before
 	public void before() {
 		Tile[][] tile = {{new AirTile(),new AirTile()},
 						 {new SpawnTile(), new  EndTile()},
 						 {new GroundTile(), new GroundTile()}};
 		grid = new TileGrid(tile);
 	}
 	
 	@Test
 	public void testSet() {
 		assertFalse(grid.getFromCoord(1, 0).isSolid());
 		grid.set(1, 0, new GroundTile());
 		assertTrue(grid.getFromCoord(1, 0).isSolid());
 	}
 	
 	@Test
 	public void testGetWith() {
 		assertTrue(grid.getWidth() == 2);
 	}
 	
 	@Test
 	public void testGetHeight() {
 		assertTrue(grid.getHeight() == 3);
 	}
 	
 	@Test
 	public void testGetSpawnPoint() throws Exception {
 		Point p = grid.getSpawnPoint();
 		Point point = new Point((int)p.getX()/Constants.TILESIZE,
 								(int)p.getY()/Constants.TILESIZE);
 		assertTrue(point.getX() == 0);
 		assertTrue(point.getY() == 1);
 	}
 	
 	@Test
 	public void testTilePosFromRealPos() {
 		assertTrue(grid.getTilePosFromRealPos(50) == 1);
 	}
 	
 	@Test
 	public void testIntersectsWith() {
 		PCharacter character = new PCharacter(50, 64, null);
 		
 		assertTrue(grid.intersectsWith(character));
 	}
 	
 	@Test
 	public void testLeftSideIntersection() {
 		int charX = 50;
 		int notYIntersect = Constants.TILESIZE*2-Constants.CHARACTERHEIGHT-1;
 		PCharacter character = new PCharacter(charX, notYIntersect, null);
         assertTrue(grid.leftSideIntersection(character) == 0);
         
         int yIntersect = notYIntersect+1;
         character.setY(yIntersect);
         int leftIntersect = Constants.TILESIZE*2-charX;
         assertTrue(grid.leftSideIntersection(character) == leftIntersect);
 	}
 	
 	@Test
 	public void testRightSideIntersection() {
 		int charX = 35;
 		int notYIntersect = Constants.TILESIZE*2-Constants.CHARACTERHEIGHT-1;
 		PCharacter character = new PCharacter(charX, notYIntersect, null);
 		assertTrue(grid.rightSideIntersection(character) == 0);
         
         int yIntersect = notYIntersect+1;
         character.setY(yIntersect);
         double rightIntersect = charX + Constants.CHARACTERWIDTH - Constants.TILESIZE - 0.00001;
        assertTrue((int)grid.rightSideIntersection(character) == (int)rightIntersect);
 	}
 	
 	@Test
 	public void testBottomSideIntersection() {
 		int charX = 35;
 		int notYIntersect = Constants.TILESIZE*2-Constants.CHARACTERHEIGHT-1;
 		PCharacter character = new PCharacter(charX, notYIntersect, null);
 		assertTrue(grid.bottomSideIntersection(character) == 0);
         
 		int intersectLength = 12;
         int yIntersect = notYIntersect+intersectLength;
         character.setY(yIntersect);
        assertTrue((int)(grid.bottomSideIntersection(character)) == (int)(intersectLength-1-0.00001));
 	}
 	
 	// No top atm
 	//@Test
 	public void testTopSideIntersection() {
 
 	}	
 }
