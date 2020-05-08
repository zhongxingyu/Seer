 package team01;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class BoardTest {
 
 	private Board board;
 	private Move move;
 	
 	@Before
 	public void setUp() throws Exception {
 		board = new Board(9, 5);
 		move = new Move(board);
 	}
 
 	@Test
	public void avaiableMovesTest() {
 		int player = Board.WHITE;
 		
 		assertEquals(board.numWhite(), 22);
 		assertEquals(board.numBlack(), 22);
 		assertEquals(board.numEmpty(), 1);
 		
 		board.setPosition(new Point(2, 2), Board.EMPTY);
 		board.setPosition(new Point(3, 2), Board.EMPTY);
 
 		assertEquals(board.numWhite(), 21);
 		assertEquals(board.numBlack(), 21);
 		assertEquals(board.numEmpty(), 3);
 		
 		System.out.println(board);
 		
 		int count = 0;
 		
 		System.out.println("available moves");
 		for (Point from : board)
 		{
 			if (board.getPosition(from) == player)
 			{
 				// TODO: possibly add a delta iterator?
 				for (int dx = Delta.MIN_DELTA; dx <= Delta.MAX_DELTA; dx++)
 				{
 					for (int dy = Delta.MIN_DELTA; dy <= Delta.MAX_DELTA; dy++)
 					{
 						Point to = from.getApproach(dx, dy);
 
 						if (move.isValidMove(from, to))
 						{
 							++ count;
 							System.out.printf("%s -> %s\n", from, to);
 						}
 					}
 				}
 			}
 		}
 		
 		assertEquals(count, 8);
 		
 		System.out.println();
 	}
 	
 	@Test
 	public void approachUpTest() {
 		assertEquals(board.numWhite(), 22);
 		assertEquals(board.numBlack(), 22);
 		assertEquals(board.numEmpty(), 1);
 		
 		Point from = new Point(4, 3);
 		Point to = new Point(4, 2);
 
 		System.out.println("before:");
 		System.out.println(board);
 		
 		assertTrue(move.isValidMove(from, to));
 		assertFalse(move.approach(from, to));
 		
 		assertEquals(board.numWhite(), 22);
 		assertEquals(board.numBlack(), 20);
 		assertEquals(board.numEmpty(), 3);
 
 		System.out.println("after:");
 		System.out.println(board);
 	}
 	
 	@Test
 	public void approachDiagTest() {
 		assertEquals(board.numWhite(), 22);
 		assertEquals(board.numBlack(), 22);
 		assertEquals(board.numEmpty(), 1);
 		
 		Point from = new Point(3, 3);
 		Point to = new Point(4, 2);
 
 		System.out.println("before:");
 		System.out.println(board);
 		
 		assertTrue(move.isValidMove(from, to));
 		assertFalse(move.approach(from, to));
 		
 		assertEquals(board.numWhite(), 22);
 		assertEquals(board.numBlack(), 20);
 		assertEquals(board.numEmpty(), 3);
 
 		System.out.println("after:");
 		System.out.println(board);
 	}
 	
 
 	
 	@Test
 	public void approachSideTest() {
 		assertEquals(board.numWhite(), 22);
 		assertEquals(board.numBlack(), 22);
 		assertEquals(board.numEmpty(), 1);
 		
 		Point from = new Point(3, 2);
 		Point to = new Point(4, 2);
 
 		System.out.println("before:");
 		System.out.println(board);
 		
 		assertTrue(move.isValidMove(from, to));
 		assertFalse(move.approach(from, to));
 		
 		assertEquals(board.numWhite(), 22);
 		assertEquals(board.numBlack(), 21);
 		assertEquals(board.numEmpty(), 2);
 
 		System.out.println("after:");
 		System.out.println(board);
 	}
 
 }
