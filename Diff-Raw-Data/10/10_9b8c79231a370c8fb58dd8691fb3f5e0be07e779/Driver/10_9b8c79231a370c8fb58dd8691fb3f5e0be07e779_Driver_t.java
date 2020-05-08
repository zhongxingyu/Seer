 package DerpyAI;
import java.util.ArrayList;
import java.awt.Point; 

 
 public class Driver {
 
 	public static void main(String[] args) {
 
 		
 		//October 7 2012: This needs to stay like this, folks! An AI is created, it's passed board, makes a move, returns the board, and prints it
 		
 		DerpyBoard db = new DerpyBoard();
 		DerpyAI aiOne = new DerpyAI(true);
		ArrayList<Point> test = new ArrayList<Point>();
		test.add(new Point(4,5));
		test.add(new Point(5,6));
		System.out.println("Array List 1: " + test);
		System.out.println("Array List 2: " + aiOne.movablePoints(db.getBoardArray()[5][6]));
 		db = aiOne.randomMove();
 
 		db.printBoard();
 		
 	}
 
 }
