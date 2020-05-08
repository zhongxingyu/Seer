 import java.util.ArrayList;
 
 public abstract class Player {
 
 	private Board myBoard;
 	private Robot gameRobot;
 
 	public Player (Robot startGameRobot) {
 		this.gameRobot = startGameRobot;
 	}
 
 	public Piece[] getPlayerPieces () {
 		Piece[] result = new Piece[this.myBoard.totalPiecesLeft(this)];
 		int i = 0;
 		for (Piece p : this.myBoard.getPiecesOnBoard()) {
 			if (p.getPlayer() == this) {
 				result[i++] = p;
 			}
 		}
 		return result;
 	}
 
 	public Move calculateBestMove (Board b, int recursionDepth) {
 		return this.rankBestMoves(b,recursionDepth)[0];
 	}
 
 	public Move[] rankBestMoves (Board b, int recursionDepth) {
 		
 	}
 
 	public static void performMove(Move myMove, Board theBoard) {
 		myMove.getMovePiece().setLocation(myMove.getDestination());
 		for (Piece deadPiece : myMove.calculatePiecesToJump()) {
 			theBoard.removePiece(deadPiece);
 		}
 	}
 	
 	public void setBoard (Board newBoard) {
 		this.myBoard = newBoard;
 	}
 
 	public Board getBoard () {
 		return this.myBoard;
 	}
 
 	public Move[] getAllMoves () {
 		ArrayList<Piece> result;
		for (Piece playerPiece : this.getPlayerPieces()) {
 			if (playerPiece.getIsKing()) {
 				for (byte[] displacement : new byte[][]{new byte[]{1,1}, new byte[] {1,-1}, new byte[] {-1,1}, new byte[] {-1,-1}) {
 					byte[] potentialEndpoint
 				}
 			}
 			else {
 				for (byte[] displacement : new byte[][]{new byte[]{1,1}, new byte[] {-1,1})
 			}
 		}
 	}
 
 	public abstract void takeTurn();
 }
