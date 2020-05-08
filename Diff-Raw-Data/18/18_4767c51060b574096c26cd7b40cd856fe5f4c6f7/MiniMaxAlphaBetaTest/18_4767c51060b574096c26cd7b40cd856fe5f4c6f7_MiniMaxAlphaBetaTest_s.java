 package test;
 
 import org.junit.Test;
 
 import constants.LOAConstants;
 
 import utils.MiniMaxAlphaBeta;
 import utils.MoveGenerator;
 
 import model.LineOfActionBoard;
 import model.Move;
 
 public class MiniMaxAlphaBetaTest {
 
 	public MiniMaxAlphaBetaTest() {
 	}
 
 	@Test
 	public void test() {
		LineOfActionBoard b = new LineOfActionBoard("2222220000004004400000044000000440000004222200044000000422000000");
 		b.printBoard();
 		MoveGenerator m = new MoveGenerator();
 		MiniMaxAlphaBeta ab = new MiniMaxAlphaBeta();
 		//b.printBoard();
 		Move move = ab.calculateBestMove(b.getBoard(), LOAConstants.PIECE_TYPE_WHITE, 6);
 		//b.printBoard();
 		System.out.println("-- Best move --");
 		System.out.println("Weight: "+move.getWeight());
 		System.out.println("Origin: "+move.getOrigin());
 		System.out.println("Destination: "+move.getDestination());
 		System.out.println("Distance: " + move.getDistance());
 		
 	}
 
 }
