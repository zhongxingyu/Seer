 import java.util.ArrayList
 
 public class Board {
 
 	private ArrayList<Piece> piecesOnBoard;
 
 	public Board (Player[] players) {
		piecesOnBoard = new ArrayList<Piece>;
 		for (byte i=0; i<3; i++) {
 			for (byte j=0; j<4; j++) {
 				piecesOnBoard.add(new Piece({2*j+(i%2), i}, players[0]));
 				piecesOnBoard.add(new Piece({7-2*j+(i%2), 7-i}, players[1]));
 			}
 		}
 	}
 
 	public ArrayList<Piece> getPiecesOnBoard () {
 		return piecesOnBoard;
 	}
 
 	public void removePiece (Piece pieceToRemove) {
 		piecesOnBoard.remove(pieceToRemove);
 	}
 }
