 package se.lannstrom.chesssensei.model.rules;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.lannstrom.chesssensei.model.Board;
 import se.lannstrom.chesssensei.model.Board.ChessColor;
 import se.lannstrom.chesssensei.model.BoardPosition;
 import se.lannstrom.chesssensei.model.ChessMove;
 
 public class KingStrategy implements ChessPieceStrategy {
 
 	@Override
 	public boolean isValid(Board b, ChessMove move) {
 		int fromX = move.getFrom().getX();
 		int fromY = move.getFrom().getY();
 		int toX = move.getTo().getX();
 		int toY = move.getTo().getY();
 		
 		/* Is the length of the move 1? */
 		if (Math.abs(toX - fromX) <= 1 &&
 			Math.abs(toY - fromY) <= 1) {
 			return b.isEmptyOrOpponent(move.getTo(), move.getColor());
 		} else {
 			return false;
 		}
 			
 	}
 
 	@Override
 	public List<ChessMove> getValid(Board b, BoardPosition from,
 			ChessColor color) {
 		ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
 		
 		int[] v = { -1, 0, 1 };
 		for (int i = 0; i < v.length; i++) {
 			for (int j = 0; j < v.length; j++) {
 				if (i == 1 && j == 1)
					continue;
 				
 				BoardPosition bp = new BoardPosition(from.getX() + v[i],
 													 from.getY() + v[j]);
 				if (bp.insideBoard(b) && b.isEmptyOrOpponent(bp, color)) {
 					moves.add(new ChessMove(from, bp, color));
 				}
 			}
 		}
 		
 		return moves;
 	}
 
 }
