 package DerpyAI;
 
 public class Driver {
 
 	public static void main(String[] args) {
 
 		
 		//October 7 2012: This needs to stay like this, folks! An AI is created, it's passed board, makes a move, returns the board, and prints it
 		
 		DerpyBoard db = new DerpyBoard();
 		DerpyAI aiOne = new DerpyAI(true);

 		db = aiOne.randomMove();
 
 		db.printBoard();
 		
 	}
 
 }
