 package DerpyAI;
 
 public class Driver {
 
 	public static void main(String[] args) {
		/*DerpyPawn b = new DerpyPawn(false, new Point(0,0));
		System.out.println(b.changeLocation(new Point(0,2)));
		System.out.println(b.changeLocation(new Point(0,4)));
		System.out.println(b.getLocation());
		System.out.println(b.changeLocation(new Point(0,3)));
		System.out.println(b.getLocation());*/
 
		DerpyBoard db = new DerpyBoard();
		DerpyAI aiOne = new DerpyAI(false);
		//DerpyAI aiTwo = new DerpyAI(false);
 		
 		//October 7 2012: This needs to stay like this, folks! An AI is created, it's passed board, makes a move, returns the board, and prints it
		//DO NOT MODIFY
 
		//DO NOT MODIFY
		//aiOne.executeCzechDefense();
 		db = aiOne.makeMove(db);
 		db.printBoard();
 		
 	}
 
 }
