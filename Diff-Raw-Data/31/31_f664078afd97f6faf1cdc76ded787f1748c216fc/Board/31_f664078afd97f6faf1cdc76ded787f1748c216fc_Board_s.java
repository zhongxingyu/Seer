 package chess;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import chess.pieces.Color;
 import chess.pieces.Piece;
 import chess.pieces.PieceType;
 
 public class Board {
 	Logger logger = LoggerFactory.getLogger(Board.class);
 
 	private Set<Piece> pieces = new HashSet<Piece>();
 
 	public Set<Piece> getPieces() {
 		return pieces;
 	}
 
 	public void kill(Piece piece) {
 		pieces.remove(piece);
 	}
 
 	public Piece getPieceAtLocation(Location location) {
 		for (Piece piece : pieces) {
 			if (piece.getLocation().isAt(location)) {
 				return piece;
 			}
 		}
 		return null;
 	}
 
 	public Piece getKing(Color color) {
 		for (Piece piece : pieces) {
 			if (piece.getColor() == color && piece.getType() == PieceType.King) {
 				return piece;
 			}
 		}
		logger.warn("For some reason, the {} king doesn't exist", color);
 		return null;
 	}
 
 	public Board clone() {
 		Board board = new Board();
 		for (Piece piece : pieces) {
 			board.getPieces().add(piece.clone());
 		}
 		return board;
 	}
 
 	public void log() {
 		logger.debug(BoardPrinter.print(this));
 	}
 }
