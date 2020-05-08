 /*
  * SpecialPosition.java
  * 
  */
 
 package no.hist.aitel.chess.position;
 
 import no.hist.aitel.chess.board.Board;
 import no.hist.aitel.chess.piece.Piece;
 import static no.hist.aitel.chess.piece.PieceConstants.*;
 
 /**
  *
  * @author martin
  */
 public class SpecialPosition extends Position {
 
     /**
      * Creates a position object for a board which is used to validate regular and special moves
      * @param board
      */
     public SpecialPosition(Board board) {
         super(board);
     }
 
     /**
      * Check if pawn can be promoted
      * @return True if pawn can be promoted
      */
     public boolean isPromotion() {
         Piece fromPiece = board.getPiece(from);
         if (fromPiece.getType() == PAWN) {
             if (fromPiece.getColor() == WHITE && to >= 48 && to <= 63) {
                 return true;
             } else if (fromPiece.getColor() == BLACK && to >= 0 && to <= 7) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Check if we're castling
      * @return True if we're castling or false otherwise
      */
     public boolean isCastling() {
         Piece fromPiece = board.getPiece(from);
         int rookFrom;
         if (fromPiece.getType() == KING && !fromPiece.isMoved()) {
             if (fromPiece.getColor() == WHITE) {
                 if (to == 6 && isEmptyRange(5, 6)) {
                     rookFrom = 7;
                 } else if (to == 2 && isEmptyRange(1, 3)) {
                     rookFrom = 0;
                 } else {
//                    throw new IllegalPositionException("Can't perform castling because fields" +
//                            " between king and rook aren't empty");
                    return false;
                 }
                 Piece rook;
                 if (rookFrom == 0 || rookFrom == 7) {
                     rook = board.getPiece(rookFrom);
                 } else {
                     return false;
                 }
                 if (rook.getColor() == WHITE && rook.getType() == ROOK && !rook.isMoved()) {
                     return true;
                 }
             } else if (fromPiece.getColor() == BLACK) {
                 if (to == 62 && isEmptyRange(61, 62)) {
                     rookFrom = 63;
                 } else if (to == 58 && isEmptyRange(57, 59)) {
                     rookFrom = 56;
                 } else {
//                    throw new IllegalPositionException("Can't perform castling because fields" +
//                            " between king and rook aren't empty");
                    return false;
                 }
                 Piece rook;
                 if (rookFrom == 56 || rookFrom == 63) {
                     rook = board.getPiece(rookFrom);
                 } else {
                     return false;
                 }
                 if (rook.getColor() == BLACK && rook.getType() == ROOK && rook.isMoved()) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Perform castling
      */
     public void doCastling() {
         if (from != 4 && from != 60) {
             throw new IllegalPositionException("Castling can only be performed from position 4 " +
                     "(white) or 60 (black)");
         }
         int rookTo = -1, rookFrom = -1;
         if (from == 4 || from == 60) {
             if (to == (from + 2)) {
                 rookTo = from + 1;
                 rookFrom = from + 3;
             } else if (to == (from - 2)) {
                 rookTo = from - 2;
                 rookFrom = from - 4;
             }
             board.setPiece(to, board.getPiece(from));
             board.setPiece(from, new Piece());
             board.setPiece(rookTo, board.getPiece(rookFrom));
             board.setPiece(rookFrom, new Piece());
         }
     }
 
     /**
      * Check if all positions between (and including) two positions is empty
      * @param from
      * @param to
      * @return True if range is empty and false otherwise
      */
     private boolean isEmptyRange(int from, int to) {
         if (to < from) {
             for (int i = from; i >= to; i--) {
                 if (!board.getPiece(i).isEmpty()) {
                     return false;
                 }
             }
         } else {
             for (int i = from; i <= to; i++) {
                 if (!board.getPiece(i).isEmpty()) {
                     return false;
                 }
             }
         }
         return true;
     }
 
 }
