 package DerpyAI;
 
 public class Driver {
 
 	public static void main(String[] args) {
 
 		// October 22, 2012 - Makes 20 random moves and prints the board each
 		// time. Pieces should be functional at this point.
 
 		DerpyBoard db = new DerpyBoard();
 		DerpyAI aiOne = new DerpyAI(true);
 		DerpyAI aiTwo = new DerpyAI(false);
 		for (int i = 0; i < 20; i++) {
 			db = aiOne.makeMove(db);
 			System.out.println();
 		}
 	}
 
 }
