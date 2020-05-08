 package chess.player.ai;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import chess.BoardBuilder;
 import chess.Location;
 import chess.pieces.Color;
 import chess.player.Move;
 
 public class PlayerTest {
 
 	private BoardBuilder builder;
 
 	@Before
 	public void setup() {
 		builder = BoardBuilder.build();
 	}
 
 	@Test
 	public void testNextPawnMove() throws Exception {
 		builder.addWhite("Pd3").addBlack("Pd6");
 		assertMove("d6", "d5", suggestBlackMove(1));
 	}
 
 	@Test
 	public void testQueen_BadMove() throws Exception {
 		builder.addWhite("Ke1,Pd2").addBlack("Ke8,Qd8");
 		assertBadMove("d8", "d2", suggestBlackMove(1));
 	}
 
 	@Test
 	public void testKing_OutOfCheck() throws Exception {
 		builder.addWhite("Ka8,Rb8,Rf6").addBlack("Kh8,Pg7");
 		assertMove("h8", "h7", suggestBlackMove(2));
 	}
 
 	@Test
 	public void testRook_Kill() throws Exception {
 		builder.addWhite("Ka8,Rb8,Rf6").addBlack("Kh8,Pg7");
 		assertMove("h8", "h7", suggestBlackMove(1));
 	}
 
 	private Move suggestBlackMove(int intelligence) {
 		Player player = new Player(Color.Black, intelligence);
 		return player.suggestMove(builder.getBoard());
 	}
 
 	private static void assertBadMove(String expectedFrom, String expectedTo, Move actualMove) {
 		String msg = actualMove.toString() + " is considered a bad move";
 		Move badMove = new Move(new Location(expectedFrom), new Location(expectedTo));
 		assertFalse(msg, badMove.equals(actualMove));
 	}
 
 	private static void assertMove(String expectedFrom, String expectedTo, Move actualMove) {
 		assertEquals(expectedFrom, actualMove.getFrom().toString());
 		assertEquals(expectedTo, actualMove.getTo().toString());
 	}
 }
