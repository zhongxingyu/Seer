 package se.chalmers.tda367.std.tests;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import se.chalmers.tda367.std.core.LevelMap;
 import se.chalmers.tda367.std.core.PlaceableTile;
 import se.chalmers.tda367.std.core.Properties;
 import se.chalmers.tda367.std.utilities.Position;
 import se.chalmers.tda367.std.utilities.SpriteCreator;
 
 
 public class TestLevelMap {
 
 
 	private LevelMap map;
 	private final int scale = Properties.INSTANCE.getTileScale();
 
 	@Before
 	public void before(){
 		SpriteCreator.setNativeSpriteClass(NativeDummySprite.class);
 		
 		map = new LevelMap(-1, 10, 10);
 		for(int y = 0; y<10; y++){
 			for(int x = 0; x<10; x++){
 				map.setMapItem(x, y, PlaceableTile.TERRAIN_TILE);
 			}
 		}
 	}
 
 	@Test
 	public void testSetMapItem(){
 		PlaceableTile[][] field = map.getMapField();
 		for(int y = 0; y<10; y++){
 			for(int x = 0; x<10; x++){
 				//Checks initial placement
 				assertTrue(field[x][y] == PlaceableTile.TERRAIN_TILE);
 				
 				for(PlaceableTile pt : PlaceableTile.values()){
 					map.setMapItem(x, y, pt);
 					field = map.getMapField();
 					assertTrue(field[x][y] == pt);
 					assertTrue(map.getMapItem(x, y) == pt);
 				}
 			}
 		}
 	}
 	
 	@Test
 	public void testGetPlayerBasePos(){
 
 		map.setMapItem(0, 0, PlaceableTile.PLAYER_BASE_TILE);
		assertTrue(map.getPlayerBasePos().getX() == 0 && map.getPlayerBasePos().getY() == 0);
 		
 		map.setMapItem(1, 1, PlaceableTile.PLAYER_BASE_TILE);
		assertTrue(map.getPlayerBasePos().getX() == 1 && map.getPlayerBasePos().getY() == 1);
 	}
 	
 	@Test
 	public void testWaypointsInLevelMap(){
 		assertTrue(map.getWaypointList().size() == 0);
 		
 		map.setMapItem(0, 0, PlaceableTile.WAYPOINT);
 		List<Position> waypoints = map.getWaypointList();
 		assertTrue(waypoints.size() == 1);
 		assertTrue(waypoints.get(0).getX() == 0 * scale && waypoints.get(0).getY() == 0 * scale);
 		
 		map.setMapItem(1, 1, PlaceableTile.WAYPOINT);
 		waypoints = map.getWaypointList();
 		assertTrue(waypoints.size() == 2);
 		assertTrue(waypoints.get(1).getX() == 1 * scale && waypoints.get(1).getY() == 1 * scale);
 		
 		map.clearWaypoints();
 		assertTrue(map.getWaypointList().size() == 0);
 
 	}
 	
 	@Test
 	public void testGetEnemyBasePos(){
 		map.setMapItem(0, 0, PlaceableTile.ENEMY_START_TILE);
 		assertTrue(map.getEnemyStartPos().getX() == 0 && map.getEnemyStartPos().getY() == 0);
 		
 		map.setMapItem(1, 1, PlaceableTile.ENEMY_START_TILE);
		assertTrue(map.getEnemyStartPos().getX() == 1 && map.getEnemyStartPos().getY() == 1);
 	}
 	
 }
