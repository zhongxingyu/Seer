 package generalChess;
 
 import sharedfiles.Board;
 
import java.awt.Point;
 public class testClass {
 	public static void main(String[] args) {
 		v1Bobby a=new v1Bobby(new Board(), true);
 		a.printBoard();
		a.move(6, 6, 5, 1);
		a.move(0, 6, 3, 2);
		a.printBoard();
		int b=a.takeIfPossible(a.pMoves().get(0));
		System.out.println(b);
		a.move((int)((Point)a.bestPieceToTake().get(0)).getX(),(int)((Point)a.bestPieceToTake().get(0)).getY(), (int)((Point)a.bestPieceToTake().get(1)).getX(), (int)((Point)a.bestPieceToTake().get(1)).getY());
		a.printBoard();
		System.out.println((int)((Point)a.bestPieceToTake().get(0)).getX());
		System.out.println((int)((Point)a.bestPieceToTake().get(0)).getY());
		System.out.println((int)((Point)a.bestPieceToTake().get(1)).getX());
		System.out.println((int)((Point)a.bestPieceToTake().get(1)).getY());
		/*
 		System.out.println((int)a.bestPieceToTake().get(0).getX());
 		System.out.println((int)a.bestPieceToTake().get(0).getY());
 		System.out.println((int)a.bestPieceToTake().get(1).getX());
 		System.out.println((int)a.bestPieceToTake().get(1).getY());
 
 		a.move((int)a.bestPieceToTake().get(0).getX(),(int)a.bestPieceToTake().get(0).getY(),(int) a.bestPieceToTake().get(1).getX(),(int) a.bestPieceToTake().get(1).getY());
 		a.printBoard();
		*/
 		}
 	
 }
