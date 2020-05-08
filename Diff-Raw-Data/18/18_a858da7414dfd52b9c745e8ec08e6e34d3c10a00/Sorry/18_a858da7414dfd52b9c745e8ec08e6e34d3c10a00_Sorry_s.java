 import java.util.Random;
 
 
 /**
  * Sorry game driver class.
  *
  * @author Sam Brown, Caleb Cousins, Taylor Krammen, and Yucan Zhang
  */
 public class Sorry {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args){
 		//first we need a game board
 		SRGameBoard board = new SRGameBoard();
 		Random rand = new Random();
 		int dist = 0;
 		SRCard card = new SRCard(dist);
 		int [] moves;
 		
 		//simulate a bunch of random turns
 		for (int i=0;i<1000;i++){
 			for (int j=0;j<2;j++){
 				int player = j;
 				int pawnID = rand.nextInt(4);
 				//moves = board.getMoves(pawn, distance)
 				//board.movePawnTo(pawn, moves[random choice of move])
 				dist = rand.nextInt(12);
 				
				moves = board.findMoves(board.getPlayerPawn(player, pawnID), card);
 				/*for (int m=0;m<moves.length;m++){
 					System.out.print(moves[m]+" ");
 				}*/
 				//System.out.println();
 				board.movePawn(board.getPlayerPawn(player, pawnID), dist);
 			}
 		}
 	}	
 }
