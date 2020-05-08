 package javalabra.chess.core.impl;
 
 import static org.junit.Assert.*;
 
 import javalabra.chess.domain.Bishop;
 import javalabra.chess.domain.Board;
 import javalabra.chess.domain.Color;
 import javalabra.chess.domain.King;
 import javalabra.chess.domain.Knight;
 import javalabra.chess.domain.Pawn;
 import javalabra.chess.domain.Piece;
 import javalabra.chess.domain.PlayerBlack;
 import javalabra.chess.domain.PlayerWhite;
 import javalabra.chess.domain.Queen;
 import javalabra.chess.domain.Rook;
 import javalabra.chess.domain.Square;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class BoardBuilderImplTest {
 
 	PlayerWhite white;
 	PlayerBlack black;
 	BoardBuilderImpl builder;
 	Board board;
 
 	@Before
 	public void setUp() throws Exception {
 		white = new PlayerWhite();
 		black = new PlayerBlack();
 		builder = new BoardBuilderImpl();
 		board = builder.build(white, black);
 	}
 
 	@Test
 	public void builderCreatesAllPieces() {
 		assertEquals(16, white.getPieces().size());
 		assertEquals(16, black.getPieces().size());
 	}
 
 	@Test
 	public void buildCreatesValidSetup() {
 		assertPawnRow(board, Color.WHITE);
 		assertPrimaryRow(board, Color.WHITE);
 		assertPawnRow(board, Color.BLACK);
 		assertPrimaryRow(board, Color.BLACK);
 	}
 
 	public static void assertPawnRow(Board board, Color color) {
 		final int row = Color.WHITE == color ? 1 : 6;
 		for (int i = 0; i < 8; ++i) {
 			assertSquare(board.getSquare(i, row), Pawn.class, color);
 		}
 	}
 
 	public static void assertPrimaryRow(Board board, Color color) {
 		final int row = Color.WHITE == color ? 0 : 7;
 
 		assertSquare(board.getSquare(0, row), Rook.class, color);
 		assertSquare(board.getSquare(1, row), Knight.class, color);
 		assertSquare(board.getSquare(2, row), Bishop.class, color);
 
		if (Color.WHITE == color) {
			assertSquare(board.getSquare(3, row), Queen.class, color);
			assertSquare(board.getSquare(4, row), King.class, color);
		} else {
			assertSquare(board.getSquare(3, row), King.class, color);
			assertSquare(board.getSquare(4, row), Queen.class, color);
		}
 
 		assertSquare(board.getSquare(5, row), Bishop.class, color);
 		assertSquare(board.getSquare(6, row), Knight.class, color);
 		assertSquare(board.getSquare(7, row), Rook.class, color);
 	}
 
 	public static void assertSquare(Square square, Class<?> type, Color color) {
 		final Piece piece = square.getPiece();
 		assertNotNull(square.toString() + " is empty", piece);
 		assertEquals(square.toString() + " contains wrong piece", type, piece.getClass());
 		assertEquals(square.toString() + " piece has wrong color", color, piece.getColor());
 	}
 
 }
