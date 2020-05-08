 package test;
 
 import boardEditor.core.StaticBoards;
 import gameObjects.Board;
 import gameObjects.BoardBase;
 import gameObjects.Piece;
 
 public class testMove 
 {
 	public static void main(String... args)
 	{
 		Board b = StaticBoards.getEmptyBoard();
 		/*b.pieceAt(5, 2).setValue(5);
 		
 		Piece p = b.pieceAt(5, 2);
 		
 		System.out.println(b);
 		
 		b.move(p, BoardBase.Direction.DOWN);
 		
 		System.out.println(b);*/
 		
 		b.pieceAt(0, 2).setValue(1);
 		b.pieceAt(1, 2).setValue(2);
 		b.pieceAt(2, 2).setValue(3);
 		b.pieceAt(3, 2).setValue(4);
 		b.pieceAt(4, 2).setValue(5);
 		b.pieceAt(5, 2).setValue(6);
 		
 		System.out.println(b);
 		
		System.out.println(b.pieceAt(4,2));
		
 		System.out.println(b.getAllInColumn(b.pieceAt(4,2)));
 		
 		for(Piece p : b.getAllInColumn(b.pieceAt(4, 2)))
 		{
 			System.out.println(p);
 		}
 	}
 }
