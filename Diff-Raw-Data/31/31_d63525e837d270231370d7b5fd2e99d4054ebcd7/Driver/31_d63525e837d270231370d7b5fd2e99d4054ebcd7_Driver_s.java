 package DerpyAI;
 
 import java.util.ArrayList;
 
 import sharedfiles.Board;
 import sharedfiles.Piece;
 
 public class Driver {
 
 	public static void main(String[] args) {
 			DerpyAI testAI = new DerpyAI(false, new Board()); 
			testAI.FindOurPieces(); 
 			ArrayList<Piece> a = testAI.getOurPieces(); 
 			for (int i = 0; i < a.size(); i++){
 				System.out.print(a.get(i) + " "); 
 			}
 
 	}
 
 }
