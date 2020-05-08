 package snozama.runtime.tests.mechanics;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import snozama.amazons.mechanics.Board;
 import snozama.amazons.mechanics.MoveManager;
 
 public class MoveManagerTest {
 	/**
 	 * Make sure nothing breaks on making a new MoveManager.
 	 */
 	@Test
 	public void testNew()
 	{
 		MoveManager moves = new MoveManager();
 		
 		assertTrue(moves.size() == 0);
 		assertTrue(moves.maxSize() == 2176);
 	}
 	
 	/**
 	 * Make sure basic functionality works.
 	 */
 	@Test
 	public void testAddBasics()
 	{
 		MoveManager moves = new MoveManager();
 		
 		moves.add(Board.BLACK, 0, 0, 0, 1, 1);
 		moves.add(Board.BLACK, 3, 1, 1, 2, 2);
 		moves.add(Board.WHITE, 1, 2, 2, 2, 6);
 		
 		// Test first move is correct.
 		assertTrue(moves.hasIterations());
 		int index = moves.nextIterableIndex();
 		assertEquals(index, 0);
 		assertEquals(moves.getColour(index), Board.BLACK);
 		assertEquals(moves.getAmazonIndex(index), 0);
 		assertEquals(moves.getFinishRow(index), 0);
 		assertEquals(moves.getFinishColumn(index), 0);
 		assertEquals(moves.getArrowRow(index), 1);
 		assertEquals(moves.getArrowColumn(index), 1);
 		
 		// Test second move is correct.
 		assertTrue(moves.hasIterations());
 		index = moves.nextIterableIndex();
 		assertEquals(index, 1);
 		assertEquals(moves.getColour(index), Board.BLACK);
 		assertEquals(moves.getAmazonIndex(index), 3);
 		assertEquals(moves.getFinishRow(index), 1);
 		assertEquals(moves.getFinishColumn(index), 1);
 		assertEquals(moves.getArrowRow(index), 2);
 		assertEquals(moves.getArrowColumn(index), 2);
 		
 		// Test third move is correct.
 		assertTrue(moves.hasIterations());
 		index = moves.nextIterableIndex();
 		assertEquals(index, 2);
 		assertEquals(moves.getColour(index), Board.WHITE);
 		assertEquals(moves.getAmazonIndex(index), 1);
 		assertEquals(moves.getFinishRow(index), 2);
 		assertEquals(moves.getFinishColumn(index), 2);
 		assertEquals(moves.getArrowRow(index), 2);
 		assertEquals(moves.getArrowColumn(index), 6);
 		
 		assertFalse(moves.hasIterations());
 		
 		moves.condense();
 		assertTrue(moves.size() == 3);
 		assertTrue(moves.maxSize() == 3);
 		moves.clearIteratorState();
 		
 		// Test first move is correct.
 		assertTrue(moves.hasIterations());
 		index = moves.nextIterableIndex();
 		assertEquals(index, 0);
 		assertEquals(moves.getColour(index), Board.BLACK);
 		assertEquals(moves.getAmazonIndex(index), 0);
 		assertEquals(moves.getFinishRow(index), 0);
 		assertEquals(moves.getFinishColumn(index), 0);
 		assertEquals(moves.getArrowRow(index), 1);
 		assertEquals(moves.getArrowColumn(index), 1);
 
 		// Test second move is correct.
 		assertTrue(moves.hasIterations());
 		index = moves.nextIterableIndex();
 		assertEquals(index, 1);
 		assertEquals(moves.getColour(index), Board.BLACK);
 		assertEquals(moves.getAmazonIndex(index), 3);
 		assertEquals(moves.getFinishRow(index), 1);
 		assertEquals(moves.getFinishColumn(index), 1);
 		assertEquals(moves.getArrowRow(index), 2);
 		assertEquals(moves.getArrowColumn(index), 2);
 
 		// Test third move is correct.
 		assertTrue(moves.hasIterations());
 		index = moves.nextIterableIndex();
 		assertEquals(index, 2);
 		assertEquals(moves.getColour(index), Board.WHITE);
 		assertEquals(moves.getAmazonIndex(index), 1);
 		assertEquals(moves.getFinishRow(index), 2);
 		assertEquals(moves.getFinishColumn(index), 2);
 		assertEquals(moves.getArrowRow(index), 2);
 		assertEquals(moves.getArrowColumn(index), 6);
 	}
 	
 	@Test
 	public void testMakeUndoMove()
 	{
 		Board board = new Board();
 		
 		MoveManager successors = board.getSuccessors(Board.WHITE, 0);
 		
 		int index = 0;
 		while (successors.hasIterations())
 		{
 			index = successors.nextIterableIndex();
 			
 			System.out.println(index);
 			
			successors.applyMove(board, index);
			
 			int row_s = Board.decodeAmazonRow(board.amazons[Board.WHITE][successors.getAmazonIndex(index)]);
 			int col_s = Board.decodeAmazonColumn(board.amazons[Board.WHITE][successors.getAmazonIndex(index)]);
 			
 			int row_f = successors.getFinishRow(index);
 			int col_f = successors.getFinishColumn(index);
 			int arrow_row = successors.getArrowRow(index);
 			int arrow_col = successors.getArrowColumn(index);
 			
 			assertTrue(board.isWhite(row_f, col_f));
 			assertTrue(board.isArrow(arrow_row, arrow_col));
 			
 			assertTrue(successors.undoMove(board, index, row_s, col_s));
 		}
 	}
 }
