 package se.lannstrom.chesssensei.model.rules;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.lannstrom.chesssensei.model.Board;
 import se.lannstrom.chesssensei.model.Board.ChessColor;
 import se.lannstrom.chesssensei.model.BoardPosition;
 import se.lannstrom.chesssensei.model.ChessMove;
 
 public class KnightStrategy implements ChessPieceStrategy {
 
 	@Override
 	public boolean isValid(Board b, ChessMove move) {
 		BoardPosition from = move.getFrom();
 		BoardPosition to = move.getTo();
 		
 		return Math.abs(to.getX() - from.getX()) == 1 &&
 		       Math.abs(to.getY() - from.getY()) == 2;
 	}
 
 	@Override
 	public List<ChessMove> getValid(Board b, BoardPosition from,
 			ChessColor color) {
 		List<ChessMove> moves = new ArrayList<ChessMove>();
 
 		int[] u = {-1, 1};
 		int[] v = {-2, 2};
		
		for (int i = 0; i < u.length; i++) {
			for (int j = 0; j < v.length; j++) {
				int x = from.getX() + u[i];
				int y = from.getY() + v[j];
				BoardPosition bp = new BoardPosition(x, y);
				if (bp.insideBoard(b) && b.isEmptyOrOpponent(bp, color)) {
					moves.add(new ChessMove(from, bp, color));
 				}
 			}
 		}
 		
 		return moves;
 	}
 
 }
