 /*
  * TestMap.java	
  * Version 1.0 (2013-05-31)
  */
 
 package battleships.game;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Testing the class Map, a tool representing an ocean in the game battlefield.
  * 
  * @author Åsa Waldhe
  *
  */
 public class TestMap {
 	
 	/**
 	 * Instances of class to test.
 	 */
 	Coordinate c = null;
 	Coordinate c1 = null;
 	Coordinate c2 = null;
 	Coordinate c3 = null;
 	Coordinate c4 = null;
 	Coordinate c5 = null;
 	Coordinate c6 = null;
 	Coordinate c7 = null;
 	
 	Map map;
 	
 
 	@Before
 	public void setUp() throws Exception {
 		c = new Coordinate(1,3);
 		c1 = new Coordinate(4,8);
 		c2 = new Coordinate(0,0);
 		c3 = new Coordinate(9,9);
 		c4 = new Coordinate(0,1);
 		c5 = new Coordinate(2,5);
 		c6 = new Coordinate(2,6);
 		c7 = new Coordinate(2,7);
 		
 		map = new Map();
 	}
 	
 	/**
 	 * Testing to set values in a map and to print the array from it.
 	 */
 	@Test
 	public void test() {
 		map.setValue(c1, map.BOM);
 		assertEquals(2, map.getValue(c1), 0.1);
 		assertEquals(map.BOM, map.getValue(c1), 0.1);
 		
 		map.setValue(c2, map.BOM);
 		map.setValue(c3, map.HIT);
 		map.setValue(c4, map.SUNK);
 		int[][] a = map.getOcean();
 		for(int i=0; i<a.length; i++){
 			for(int j=0; j<a.length; j++)
 				System.out.println(a[i][j]);
 		}		
 	}
 
 }
