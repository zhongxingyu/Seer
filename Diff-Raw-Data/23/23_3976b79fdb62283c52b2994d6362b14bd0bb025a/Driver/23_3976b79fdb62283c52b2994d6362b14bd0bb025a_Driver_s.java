 package DerpyAI;
 
 import sharedfiles.Board;
 
 public class Driver {
 
 	public static void main(String[] args) {
 		/*DerpyPawn b = new DerpyPawn(false, new Point(0,0));
 		System.out.println(b.changeLocation(new Point(0,2)));
 		System.out.println(b.changeLocation(new Point(0,4)));
 		System.out.println(b.getLocation());
 		System.out.println(b.changeLocation(new Point(0,3)));
 		System.out.println(b.getLocation());*/
 
 		DerpyBoard db = new DerpyBoard();
 		DerpyAI aiOne = new DerpyAI(true);
		DerpyAI aiTwo = new DerpyAI(false);
 		
 		db = aiOne.makeMove(db);
		db = aiTwo.makeMove(db);
 		db.printBoard();
 		
 	}
 
 }
