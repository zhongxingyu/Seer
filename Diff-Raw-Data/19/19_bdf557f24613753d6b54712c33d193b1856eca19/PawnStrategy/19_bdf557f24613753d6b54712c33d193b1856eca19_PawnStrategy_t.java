 package se.lannstrom.chesssensei.model.rules;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.lannstrom.chesssensei.model.Board;
 import se.lannstrom.chesssensei.model.Board.ChessColor;
 import se.lannstrom.chesssensei.model.BoardPosition;
 import se.lannstrom.chesssensei.model.ChessMove;
 
 public class PawnStrategy implements ChessPieceStrategy {
 
 	@Override
 	public boolean isValid(Board b, ChessMove move) {
 		List<ChessMove> valid =  getValid(b, move.getFrom(), move.getColor());
 		return valid.contains(move);
 	}
 
 	@Override
 	public List<ChessMove> getValid(Board b, BoardPosition from,
 			ChessColor color) {
 		ArrayList<ChessMove> moves = new ArrayList<ChessMove>(); 
 		
 		BoardPosition enPassantTarget = b.getEnPassantTarget();
 
 		int orient = 0;
		int pawnRank = 0;
 		if (color == ChessColor.WHITE) {
 			orient = -1;
			pawnRank = b.getSize() - 2;
 		} else {
 			orient = 1;
			pawnRank = 1;
 		}
 		
 		BoardPosition singleForward = new BoardPosition(from.getX(), from.getY() + orient * 1);
 		BoardPosition doubleForward = new BoardPosition(from.getX(), from.getY() + orient * 2);
 		BoardPosition diagForward = new BoardPosition(from.getX() + 1, from.getY() + orient * 1);
 		
 		if (singleForward.insideBoard(b) && b.isEmptyAt(singleForward)) {
 			moves.add(new ChessMove(from, singleForward, color));
 			
			/* Allow double step move on first move */
			if (from.getY() == pawnRank && b.isEmptyAt(doubleForward)) {
 				moves.add(new ChessMove(from, doubleForward, color));
 			}
 		}
 		
 		if (diagForward.insideBoard(b) && 
 				(b.isOpponentAt(diagForward, color) || diagForward.equals(enPassantTarget))) {
 			moves.add(new ChessMove(from, diagForward, color));
 		}
 		
 		return moves;
 	}
 
 }
