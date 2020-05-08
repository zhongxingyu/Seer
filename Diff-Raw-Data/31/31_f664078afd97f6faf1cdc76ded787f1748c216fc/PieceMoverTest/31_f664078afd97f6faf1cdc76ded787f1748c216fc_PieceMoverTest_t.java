 package chess.pieces;
 
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 
 import chess.Board;
 import chess.BoardBuilder;
 import chess.Location;
 import chess.player.Move;
 
 public class PieceMoverTest {
 
 	private BoardBuilder builder;
 	private PieceMover mover;
 	private Board board;
 
 	@Before
 	public void setup() {
 		builder = BoardBuilder.build();
 		board = builder.getBoard();
 		mover = new PieceMover(board);
 	}
 
 	@Test
 	public void testPawn() throws Exception {
 		builder.addWhite("Pb2");
 		assertNextMoves("b2", 2);
 	}
 
 	@Test
	@Ignore
 	public void testCheck() throws Exception {
 		builder.addWhite("Ka1");
 		builder.addBlack("Qf2");
 		builder.getBoard().log();
		// TODO
 		assertNextMoves("a1", 1);
 	}
 
 	@Test
 	public void testKnight() throws Exception {
 		builder.addWhite("Nd4");
 		assertNextMoves("d4", 8);
 	}
 
 	@Test
 	public void testBishop() throws Exception {
 		builder.addWhite("Bc4");
 		assertNextMoves("c4", 11);
 	}
 
 	@Test
 	public void testBishop_Best() throws Exception {
 		builder.addWhite("Bc4,Bb5");
 		assertNextMoves("c4", 9);
 	}
 
 	@Test
 	public void testBishop_CanKill() throws Exception {
 		builder.addWhite("Bc4").addBlack("Pb3");
 		assertNextMoves("c4", 10);
 	}
 
 	@Test
 	public void testRook_Kill() throws Exception {
 		builder.addWhite("Pa1").addBlack("Ra8");
 		move("a8", "a1");
 		Assert.assertEquals(1, board.getPieces().size());
 	}
 
 	private void move(String from, String to) throws Exception {
 		Move move = new Move(new Location(from), new Location(to));
 		mover.move(move);
 	}
 
 	private void assertNextMoves(String location, int expectedMoves) {
 		Set<Location> nextMoves = mover.getNextMoves(getPieceAtLocation(location));
 		Assert.assertEquals(expectedMoves, nextMoves.size());
 	}
 
 	private Piece getPieceAtLocation(String location) {
 		return board.getPieceAtLocation(new Location(location));
 	}
 
 }
