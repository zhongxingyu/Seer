 package com.golddigger.utils;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.golddigger.model.Map;
 import com.golddigger.model.Coordinate;
 
 public class DTeleportUtilityTest {
 	private DTeleportUtility dTeleportUtility;
 	
 	@Before
 	public void before() {
 		dTeleportUtility = new DTeleportUtility();
 	}
 
 	@Test
 	public void testAssignDTeleports() {
 		
 		 final String mapString =
 					"www\n" + 
 					"wbw\n" + 
 					"w.w\n" + 
 					"w.w\n" + 
 					"w.w\n" + 
 					"www";
 		
 		Map map = MapMaker.parse(mapString);
 
 		Coordinate src1, src2, src3, dst1, dst2, dst3;
 		src1 = new Coordinate(2, 1);
 		src2 = new Coordinate(1, 1); /* This is a base tile, should be invalid */
 		src3 = new Coordinate(3, 1); /* This teleports to a wall, should be invalid */
 		
 		dst1 = new Coordinate(4, 1);
 		dst2 = new Coordinate(4, 1);
 		dst3 = new Coordinate(0, 0);
 		ArrayList<Coordinate[]> pairs = new ArrayList<Coordinate[]>();
 		
 		pairs.add(new Coordinate[]{src1, dst1});
 		pairs.add(new Coordinate[]{src2, dst2});
 		pairs.add(new Coordinate[]{src3, dst3});
 
 		dTeleportUtility.assignDTeleports(pairs, map);
 
 		assertEquals(dst1, map.get(src1).getTeleportDestination());
 
 		assertNull(map.get(src2).getTeleportDestination());
 		assertNull(map.get(src3).getTeleportDestination());
 
 	}
 
 	@Test
 	public void testFindReachableTilesSquare() {
 		
 		final String mapString =
 				"www\n" +
 				"wbw\n" +
 				"w.w\n" +
 				"w.w\n" +
 				"www\n" +
 				"w.w\n" +
 				"www";
 
 		HashSet<String> reachableTiles = new HashSet<String>();
 		Coordinate base = new Coordinate(1,1); /* No function to get bases from map */
 		
 		Map map = MapMaker.parse(mapString);
 		
 		dTeleportUtility.findReachableTiles(base, reachableTiles, map,  new ArrayList<Coordinate[]>(), 4);
 		
 		assertTrue(reachableTiles.size() == 3);
 		
 		assertTrue(reachableTiles.contains("(1,1)"));
 		assertTrue(reachableTiles.contains("(2,1)"));
 		assertTrue(reachableTiles.contains("(3,1)"));
 		
 		/* First row of boundary walls */
 		assertFalse(reachableTiles.contains("(0,0)"));
 		assertFalse(reachableTiles.contains("(0,1)"));
 		assertFalse(reachableTiles.contains("(0,2)"));
 			
 		/* Walls and trap area */
 		assertFalse(reachableTiles.contains("(4,1)"));
 		assertFalse(reachableTiles.contains("(5,1)"));
 		assertFalse(reachableTiles.contains("(6,1)"));
 		
 	}
 
 	@Test
 	public void testFindReachableTilesHex(){
 		
 		final String mapString =
 				"wwww\n" +
 				"wb.w\n" +
 				"w..w\n" +
 				"w..w\n" +
 				"wwww\n" +
 				"w..w\n" +
 				"wwww";
 
 		HashSet<String> reachableTiles = new HashSet<String>();
 		Coordinate base = new Coordinate(1,1);
 		
 		Map map = MapMaker.parse(mapString);
 		
 		dTeleportUtility.findReachableTiles(base, reachableTiles, map, new ArrayList<Coordinate[]>(), 6);
 		
 		assertTrue(reachableTiles.contains("(1,1)"));
 		assertTrue(reachableTiles.contains("(2,1)"));
 		assertTrue(reachableTiles.contains("(3,1)"));
 		
 		assertTrue(reachableTiles.contains("(1,2)"));
 		assertTrue(reachableTiles.contains("(2,2)"));
 		assertTrue(reachableTiles.contains("(3,2)"));
 		
 		assertFalse(reachableTiles.contains("(0,0)"));
 		assertFalse(reachableTiles.contains("(0,1)"));
 		assertFalse(reachableTiles.contains("(0,2)"));
 		assertFalse(reachableTiles.contains("(0,3)"));
 				
 		assertFalse(reachableTiles.contains("(5,1)"));
 		assertFalse(reachableTiles.contains("(5,2)"));
 		
 	}
 	
 	@Test
 	public void testValidDTeleportsHex() {
 
 		/* This test needs work */
 		final String MAP_STRING =
 							"wwwwwwwwwww\n" +
 							"w....b....w\n" +
 							"w.s.......w\n"	+ 
 							"w....m..h.w\n" + 
 							"w.m.......w\n" + 
 							"w.........w\n"	+ 
 							"w....www..w\n" + 
 							"w.w..whw..w\n" + 
 							"w....www..w\n" + 
 							"w.........w\n" + 
 							"wwwwwwwwwww";
		final String[] STRING = {"2,2->7,2", "3,5->4,2", "3,8->7,6", "1,5->1,9"};
 		
 		Map map = MapMaker.parse(MAP_STRING);
 		ArrayList<Coordinate[]> pairs = dTeleportUtility.formatDTeleports(STRING);
 		pairs = dTeleportUtility.validDTeleports(pairs, map, 6);
 		
 		assertTrue(pairs.size() == 1);
 		
 		assertArrayEquals(new Coordinate[]{new Coordinate(3,5), new Coordinate(4,2)}, pairs.get(0));
 		
 		assertFalse(pairs.contains(new Coordinate[]{new Coordinate(2,2), new Coordinate(7,2)}));
 		assertFalse(pairs.contains(new Coordinate[]{new Coordinate(3,8), new Coordinate(7,6)}));
 		
 	}
 	
 	@Test
 	public void testValidDTeleportsSquare() {
 
 		final String MAP_STRING =
 							"wwwwwwwwww\n" +
 							"wb.....w.w\n" +
 							"w......www\n" +
 							"w........w\n" +
 							"w........w\n" +
 							"wwwwwwwwww";
 		//Duplicate, duplicates invalid as last, corner trap x,y->z, base as source, trap to 1,8, teleport to wall, base surrounded...
 		final String[] STRING = {"1,1->2,1", "2,1->2,2", "1,2->2,2", "3,1->1,8", "3,8->4,8", "4,7->4,8", "4,1->4,2", "4,1->1,1", "4,1->99,99", "0,0->1,1", "3,5->2,8"};
 		Coordinate src, dst;
 		Coordinate[] pair; 
 		
 		Map map = MapMaker.parse(MAP_STRING);
 		ArrayList<Coordinate[]> pairs = dTeleportUtility.formatDTeleports(STRING);
 		pairs = dTeleportUtility.validDTeleports(pairs, map, 4);
 		
 		/* Base tile as source, never can get to base, always teleported elsewhere */
 		src = new Coordinate(1,1);
 		
 		assertNull(dTeleportUtility.hasDestination(src, pairs));
 		
 		/* This is valid */
 		src = new Coordinate(2,1);
 		dst = new Coordinate(2,2);
 		
 		pair = new Coordinate[]{src, dst};
 		
 		assertEquals(dst, dTeleportUtility.hasDestination(pair[0], pairs));
 		
 		/* This is invalid since it surrounds the base */
 		src = new Coordinate(1,2);
 
 		assertNull(dTeleportUtility.hasDestination(src, pairs));
 		
 		/* This is invalid, it teleports to an isolated area */
 		src = new Coordinate(3,1);
 		
 		assertNull(dTeleportUtility.hasDestination(src, pairs));
 		
 		/* This is valid */
 		src = new Coordinate(3,8);
 		dst = new Coordinate(4,8);
 		
 		pair = new Coordinate[]{src, dst};
 		
 		assertEquals(dst, dTeleportUtility.hasDestination(pair[0], pairs));
 		
 		/* This is invalid, since the previous teleport plus this one creates a trap */
 		src = new Coordinate(4,7);
 
 		assertNull(dTeleportUtility.hasDestination(src, pairs));
 		
 		/* This is valid, but there are duplicates from (4,1), the last valid one should count */
 		src = new Coordinate(4,1);
 		dst = new Coordinate(1,1);
 		
 		pair = new Coordinate[]{src, dst};
 		
 		assertEquals(dst, dTeleportUtility.hasDestination(pair[0], pairs));
 		
 		/* This is a wall */
 		src = new Coordinate(0,0);
 
 		assertNull(dTeleportUtility.hasDestination(src, pairs));
 		
 		/* This teleports to a wall */
 		src = new Coordinate(3,5);
 
 		assertNull(dTeleportUtility.hasDestination(src, pairs));
 	}
 	
 	
 	@Test
 	public void testFormatDTeleports() {
 
 		final String[] STRING = {"2,3->4,5", "6,7->8,9", "abcd", " ", "10.5,11->12,13"};
 		ArrayList<Coordinate[]> teleportPairs = dTeleportUtility.formatDTeleports(STRING);
 		
 		assertTrue(teleportPairs.size() == 2);
 		
 		assertEquals(new Coordinate(2,3), teleportPairs.get(0)[0]);
 		assertEquals(new Coordinate(4,5), teleportPairs.get(0)[1]);
 		
 		assertEquals(new Coordinate(6,7), teleportPairs.get(1)[0]);
 		assertEquals(new Coordinate(8,9), teleportPairs.get(1)[1]);
 		
 	}
 
 }
