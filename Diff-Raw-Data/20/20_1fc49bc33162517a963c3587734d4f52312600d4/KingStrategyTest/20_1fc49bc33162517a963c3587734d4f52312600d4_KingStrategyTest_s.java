 package se.lannstrom.chesssensei.model.rules.tests;
 
 import java.io.PrintWriter;
 import java.util.List;
 
 import se.lannstrom.chesssensei.model.Board;
 import se.lannstrom.chesssensei.model.Board.ChessColor;
 import se.lannstrom.chesssensei.model.BoardPosition;
 import se.lannstrom.chesssensei.model.ChessMove;
 import se.lannstrom.chesssensei.model.rules.KingStrategy;
 import se.lannstrom.chesssensei.model.util.FENParser;
 import junit.framework.TestCase;
 
 public class KingStrategyTest extends TestCase {
 
 	private KingStrategy kingStrategy;
 	private Board startingBoard;
 	private Board testBoard1;
 	private Board testBoard2;
 	
 	BoardPosition a1 = new BoardPosition("A1");
 	BoardPosition a2 = new BoardPosition("A2");
 	BoardPosition a3 = new BoardPosition("A3");
 	BoardPosition a7 = new BoardPosition("A7");
 	BoardPosition a8 = new BoardPosition("A8");
 	BoardPosition b1 = new BoardPosition("B1");
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		kingStrategy = new KingStrategy();
 		
 		startingBoard = Board.createStartingBoard();
 		FENParser parser = new FENParser();
 		testBoard1 = parser.parse("b7/8/8/8/8/8/8/8 w KQkq - 0 1", new PrintWriter(System.out));
 		testBoard2 = parser.parse("1b6/8/8/8/8/8/8/8 w KQkq - 0 1", new PrintWriter(System.out));
 	}
 	
 	public void testIsValid() {
 		boolean s1 = kingStrategy.isValid(testBoard1, new ChessMove(a1, a2, ChessColor.WHITE));
 		assertTrue(s1);
 		boolean s2 = kingStrategy.isValid(testBoard1, new ChessMove(a1, b1, ChessColor.WHITE));
 		assertTrue(s2);
 		boolean s3 = kingStrategy.isValid(testBoard1, new ChessMove(a1, a3, ChessColor.WHITE));
 		assertFalse(s3);
 		boolean s4 = kingStrategy.isValid(testBoard1, new ChessMove(a7, a8, ChessColor.WHITE));
 		assertTrue(s4);
 	}
 	
 	public void testGetValid() {
 		List<ChessMove> moves1 = kingStrategy.getValid(startingBoard, a1, ChessColor.BLACK);
 		assertTrue(moves1.size() == 3);
 		assertFalse(moves1.contains(new ChessMove(a1, a1, ChessColor.BLACK)));
 		List<ChessMove> moves2 = kingStrategy.getValid(startingBoard, a2, ChessColor.BLACK);
		assertTrue(moves2.size() == 4);
 		
 		List<ChessMove> moves3 = kingStrategy.getValid(testBoard2, a7, ChessColor.WHITE);
		assertTrue(moves3.size() == 4);
 		assertTrue(moves3.contains(new ChessMove(a7, a8, ChessColor.WHITE)));
 		List<ChessMove> moves4 = kingStrategy.getValid(testBoard2, a7, ChessColor.BLACK);
		assertTrue(moves4.size() == 3);
 	}
 
 }
