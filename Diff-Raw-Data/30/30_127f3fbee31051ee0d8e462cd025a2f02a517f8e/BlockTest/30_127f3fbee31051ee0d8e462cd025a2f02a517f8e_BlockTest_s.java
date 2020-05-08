 package org.goplayer.game;
 
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.goplayer.go.Goban;
 import org.goplayer.go.Stone;
 import org.goplayer.go.StoneColor;
 import org.goplayer.util.Coord;
 import org.junit.Test;
 
 public class BlockTest {
 
 	@Test
 	public void testCollectionInitialization() {
 		Stone s1 = new Stone(StoneColor.BLACK);
 		Stone s2 = new Stone(StoneColor.BLACK);
 		Stone s3 = new Stone(StoneColor.BLACK);
 		Collection<Stone> stones = Arrays.asList(s1, s2, s3);
 		Block block = new Block(stones);
 		assertEquals(3, block.getStones().size());
 		assertTrue(block.getStones().contains(s1));
 		assertTrue(block.getStones().contains(s2));
 		assertTrue(block.getStones().contains(s3));
 	}
 
 	@Test
 	public void testArgumentsInitialization() {
 		Stone s1 = new Stone(StoneColor.BLACK);
 		Stone s2 = new Stone(StoneColor.BLACK);
 		Stone s3 = new Stone(StoneColor.BLACK);
 		Block block = new Block(s1, s2, s3);
 		assertEquals(3, block.getStones().size());
 		assertTrue(block.getStones().contains(s1));
 		assertTrue(block.getStones().contains(s2));
 		assertTrue(block.getStones().contains(s3));
 	}
 
 	@Test
 	public void testGenerationFromFreePlace() {
 		int size = 5;
 		Goban goban = new Goban(size);
 		int startRow = 2;
 		int startCol = 3;
 		Block block = Block.generateFrom(goban, startRow, startCol);
 		assertEquals(0, block.getStones().size());
 	}
 
 	@Test
 	public void testGenerationFromIsolatedPlace() {
 		int size = 5;
 		Goban goban = new Goban(size);
 		int startRow = 2;
 		int startCol = 3;
 		goban.setCoordContent(startRow, startCol, new Stone(StoneColor.BLACK));
 
 		Block block = Block.generateFrom(goban, startRow, startCol);
 		assertEquals(1, block.getStones().size());
 		assertTrue(block.getStones().contains(
 				goban.getCoordContent(startRow, startCol)));
 	}
 
 	@Test
 	public void testGenerationFromFullGoban() {
 		int size = 5;
 		Goban goban = Goban.createFullGoban(size, size, StoneColor.BLACK);
 		int startRow = 2;
 		int startCol = 3;
 
 		Block block = Block.generateFrom(goban, startRow, startCol);
 		assertEquals(size * size, block.getStones().size());
 		for (int row = 0; row < size; row++) {
 			for (int col = 0; col < size; col++) {
 				assertTrue(block.getStones().contains(
 						goban.getCoordContent(row, col)));
 			}
 		}
 	}
 
 	@Test
 	public void testGenerationFromCustomBlock() {
 		int size = 5;
		Goban goban = Goban.createFullGoban(size, size, StoneColor.BLACK);
 
 		// create custom white block
 		Map<Coord, Stone> target = new HashMap<Coord, Stone>();
 		target.put(new Coord(1, 3), new Stone(StoneColor.WHITE));
 		target.put(new Coord(2, 2), new Stone(StoneColor.WHITE));
 		target.put(new Coord(2, 3), new Stone(StoneColor.WHITE));
 		target.put(new Coord(2, 4), new Stone(StoneColor.WHITE));
 		target.put(new Coord(3, 4), new Stone(StoneColor.WHITE));
 		target.put(new Coord(4, 4), new Stone(StoneColor.WHITE));
 		target.put(new Coord(4, 3), new Stone(StoneColor.WHITE));
 
 		// fill block
 		for (Map.Entry<Coord, Stone> entry : target.entrySet()) {
 			int row = entry.getKey().getRow();
 			int col = entry.getKey().getCol();
 			Stone stone = entry.getValue();
 			goban.setCoordContent(row, col, stone);
 		}
 
 		// add whites which touch only
 		goban.setCoordContent(0, 0, new Stone(StoneColor.WHITE));
 		goban.setCoordContent(1, 1, new Stone(StoneColor.WHITE));
 		goban.setCoordContent(3, 1, new Stone(StoneColor.WHITE));
 
 		// check equivalence whatever we take as a start
 		for (Coord start : target.keySet()) {
			Block block = Block.generateFrom(goban, start.getRow(),
					start.getCol());
 			assertTrue(block.getStones().containsAll(target.values()));
 			assertTrue(target.values().containsAll(block.getStones()));
 		}
 	}
 }
