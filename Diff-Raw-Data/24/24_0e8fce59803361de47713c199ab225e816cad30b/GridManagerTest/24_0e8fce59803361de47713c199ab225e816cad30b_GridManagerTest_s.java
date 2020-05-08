 package net.novacodex.hibernate.search.spatial;
 
 import org.junit.Test;
 
 import java.util.Map;
 
 public class GridManagerTest {
 	@Test
 	public void getCellIndexTest() {
 		String cellIndex= GridManager.getCellIndex(0.1,0.3,1);
 		org.junit.Assert.assertEquals("0",cellIndex);
 		String cellIndex2= GridManager.getCellIndex(0.2,0.3,1);
 		org.junit.Assert.assertEquals("1",cellIndex2);
 
 		String cellIndex3= GridManager.getCellIndex(3,10,4);
 		org.junit.Assert.assertEquals("4",cellIndex3);
 		String cellIndex4= GridManager.getCellIndex(6,10,4);
 		org.junit.Assert.assertEquals("9",cellIndex4);
 	}
 
 	@Test
 	public void getGridCellIdTest() {
 		Point point= new Point(45,4);
 
 		String cellId= GridManager.getGridCellId(point,5);
		org.junit.Assert.assertEquals("0|4",cellId);
 
 		String cellId2= GridManager.getGridCellId(point,7);
		org.junit.Assert.assertEquals("2|16",cellId2);
 
 		String cellId3= GridManager.getGridCellId(point,14);
		org.junit.Assert.assertEquals("257|2048",cellId3);
 
 		Point point2= new Point(-12,-179);
 
 		String cellId4= GridManager.getGridCellId(point2,5);
		org.junit.Assert.assertEquals("-32|-2",cellId4);
 
 		String cellId5= GridManager.getGridCellId(point2,7);
		org.junit.Assert.assertEquals("-125|-5",cellId5);
 
 		String cellId6= GridManager.getGridCellId(point2,14);
		org.junit.Assert.assertEquals("-15937|-547",cellId6);
 	}
 
 	@Test public void getGridCellIdsTest() {
 		Point point= new Point(45,4);
 
 		Map<Integer,String> cellIdsByTile= GridManager.getGridCellIds(point,0,15);
 
		org.junit.Assert.assertEquals("0|4",cellIdsByTile.get(5));
		org.junit.Assert.assertEquals("2|16",cellIdsByTile.get(7));
		org.junit.Assert.assertEquals("257|2048",cellIdsByTile.get(14));
 	}
 
 	@Test
 	public void findBestTileLevelForSearchRangeTest() {
 		int bestTileLevel= GridManager.findBestTileLevelForSearchRange(50);
 
 		org.junit.Assert.assertEquals(9,bestTileLevel);
 
 		int bestTileLevel2= GridManager.findBestTileLevelForSearchRange(1);
 
 		org.junit.Assert.assertEquals(15,bestTileLevel2);
 	}
 }
