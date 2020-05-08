 package se.lannstrom.chesssensei.model.rules.tests;
 
 import java.io.PrintWriter;
 import java.util.List;
 
 import se.lannstrom.chesssensei.model.Board;
 import se.lannstrom.chesssensei.model.BoardPosition;
 import se.lannstrom.chesssensei.model.ChessMove;
 import se.lannstrom.chesssensei.model.Board.ChessColor;
 import se.lannstrom.chesssensei.model.rules.PawnStrategy;
 import se.lannstrom.chesssensei.model.util.FENParser;
 import junit.framework.TestCase;
 
 public class PawnStrategyTest extends TestCase {
 
 	private Board startingBoard;
 	private Board testBoard1;
 	private Board testBoard2;
 	private PawnStrategy strategy;
 
 	BoardPosition a1 = new BoardPosition("A1");
 	BoardPosition b1 = new BoardPosition("B1");
 	BoardPosition a2 = new BoardPosition("A2");
 	BoardPosition a3 = new BoardPosition("A3");
 	BoardPosition a4 = new BoardPosition("A4");
 	BoardPosition b2 = new BoardPosition("B2");
 	BoardPosition b3 = new BoardPosition("B3");
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		startingBoard = Board.createStartingBoard();
 		FENParser parser = new FENParser();
 		testBoard1 = parser.parse("8/8/8/8/8/8/pp6/P7 w KQkq - 0 1", new PrintWriter(System.out));
 		testBoard2 = parser.parse("8/8/8/8/8/8/8/P7 w KQkq B2 0 1", new PrintWriter(System.out));
 		
 		strategy = new PawnStrategy();
 	}
 
 	public void testIsValid() {
 		boolean s1 = strategy.isValid(startingBoard, new ChessMove(a2, a3, ChessColor.WHITE));
 		assertTrue(s1);
 		boolean s2 = strategy.isValid(startingBoard, new ChessMove(a2, a4, ChessColor.WHITE));
 		assertTrue(s2);
 		boolean s3 = strategy.isValid(startingBoard, new ChessMove(a2, b2, ChessColor.WHITE));
 		assertFalse(s3);
 		boolean s4 = strategy.isValid(testBoard1, new ChessMove(a1, b2, ChessColor.WHITE));
 		assertTrue(s4);
 		boolean s5 = strategy.isValid(testBoard1, new ChessMove(a1, b2, ChessColor.BLACK));
 		assertFalse(s5);
 		boolean s6 = strategy.isValid(testBoard1, new ChessMove(b2, b1, ChessColor.BLACK));
 		assertTrue(s6);
 		
 		/* En Passant */
 		boolean s7 = strategy.isValid(testBoard2, new ChessMove(a1, b2, ChessColor.WHITE));
 		assertTrue(s7);
 	}
 
 	public void testGetValid() {
 		List<ChessMove> moves1 = strategy.getValid(startingBoard, a2, ChessColor.WHITE);
 		assertTrue(moves1.size() == 2);
 		List<ChessMove> moves2 = strategy.getValid(testBoard1, a1, ChessColor.WHITE);
 		assertTrue(moves2.size() == 1);
 		List<ChessMove> moves3 = strategy.getValid(testBoard2, a1, ChessColor.WHITE);
		assertTrue(moves3.size() == 3);
 	}
 
 }
