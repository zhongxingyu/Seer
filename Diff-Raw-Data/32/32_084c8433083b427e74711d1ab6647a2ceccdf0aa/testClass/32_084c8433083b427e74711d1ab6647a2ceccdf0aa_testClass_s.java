 package generalChess;
 
 import sharedfiles.Board;
 
 public class testClass {
 	public static void main(String[] args) {
 		v1Bobby a=new v1Bobby(new Board(), true);
 		a.printBoard();
 		System.out.println((int)a.bestPieceToTake().get(0).getX());
 		System.out.println((int)a.bestPieceToTake().get(0).getY());
 		System.out.println((int)a.bestPieceToTake().get(1).getX());
 		System.out.println((int)a.bestPieceToTake().get(1).getY());
 
 		a.move((int)a.bestPieceToTake().get(0).getX(),(int)a.bestPieceToTake().get(0).getY(),(int) a.bestPieceToTake().get(1).getX(),(int) a.bestPieceToTake().get(1).getY());
 		a.printBoard();
 		}
 	
 }
