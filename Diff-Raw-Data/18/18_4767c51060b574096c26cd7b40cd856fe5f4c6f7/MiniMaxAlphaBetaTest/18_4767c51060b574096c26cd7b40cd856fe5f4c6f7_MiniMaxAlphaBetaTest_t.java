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
		LineOfActionBoard b = new LineOfActionBoard("4022222440000004420000044000000440000004400000040000000002222220");
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
