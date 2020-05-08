 package ChessAPI;
 
 import ChessAPI.ApiVersion;
 import ChessAPI.Board;
 import ChessAPI.GameDump;
 import ChessAPI.Piece.Color;
 import ChessAPI.Player;
 
 public class UserProgram {
 
 	public static void main(String s[]){
 		GameDump D;
 		Board b;
 		//req4 create a dump
 		b = new Board();
 		D = b.getDump();
 
 		ApiVersion.printVersionNo();
 		
 		String version = ApiVersion.getVersionNo();
 		System.out.println("Current Version is : "+version);
 
 		b = new Board();
 		Player pl1 = new Player(Color.white);
 		Player pl2 = new Player(Color.black);
 		
 		b.initBoard(pl1, pl2);
 		//req5 print board
 		b.displayBoard();
 		
 		//req6 move a piece, the next line moves W_Pawn from (2,1) to (3,1)
 		//pl1.moveTo(b.getSquare(2, 1), b.getSquare(3, 1));
 		pl1.moveTo(b.getSquare(2, 1), b.getSquare(3, 1));
 		b.displayBoard();
 		
		//pl1.moveTo(b.getSquare(8, 5), b.getSquare(8, 6));
 		//b.displayBoard();
 
 		//	pl1.moveTo(b.getSquare(2, 1), b.getSquare(3, 1));
 		//	b.displayBoard();
 
 		//nitin playing around
 
 	}
 }
 
