 package edu.hm.androidsweeper.test.gamelogic;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 import edu.hm.androidsweeper.gamelogic.Coordinate;
 import edu.hm.androidsweeper.gamelogic.Field;
 
 public class FieldTest extends TestCase {
 
 	public void testEquals() {
 		Field field1 = new Field(new Coordinate(1, 1));
 		Field field2 = new Field(new Coordinate(1, 1));
 
 		assertEquals(field1, field2);
 	}
 
//	public void testGetNeighborCoords8() {
//		Coordinate coord = new Coordinate(2, 2);
//		Field field = new Field(coord);
//		Coordinate[] coords = new Coordinate[] { new Coordinate(1, 1),
//				new Coordinate(2, 1), new Coordinate(3, 1),
//				new Coordinate(1, 2), new Coordinate(3, 2),
//				new Coordinate(1, 3), new Coordinate(2, 3),
//				new Coordinate(3, 3) };
//
//		Coordinate[] neighbors = field.getNeighborCoords8();
//
//		for (int i = 0; i < coords.length; i++) {
//			assertEquals(coords[i], neighbors[i]);
//		}
//	}

 	public void testIncreaseValue() {
 		Field bomb = new Field(new Coordinate(2, 2));
 		bomb.setBomb(true);
 		Field field = new Field(new Coordinate(1, 1));
 
 		assertFalse(bomb.increaseValue());
 		assertEquals(0, bomb.getValue());
 		assertTrue(field.increaseValue());
 		assertEquals(1, field.getValue());
 		assertTrue(field.increaseValue());
 		assertEquals(2, field.getValue());
 
 	}
 
 	public void testGetNeighborFields8() {
 		Coordinate coord = new Coordinate(2, 2);
 		Field field = new Field(coord);
 		Field[] expectedfields = new Field[] { 
 				new Field(new Coordinate(1, 1)),
 				new Field(new Coordinate(2, 1)),
 				new Field(new Coordinate(3, 1)),
 				new Field(new Coordinate(1, 2)),
 				new Field(new Coordinate(3, 2)),
 				new Field(new Coordinate(1, 3)),
 				new Field(new Coordinate(2, 3)),
 				new Field(new Coordinate(3, 3)) };
 		
 		Map<Coordinate, Field> map = new HashMap<Coordinate, Field>();
 		for (Field f : expectedfields) {
 			map.put(f.getCoord(), f);
 		}
 		
 		Field[] actualNeighbors = field.getNeighborFields8(map);
 
 		for (int i = 0; i < expectedfields.length; i++) {
 			assertEquals(expectedfields[i], actualNeighbors[i]);
 		}
 	}
 
 }
