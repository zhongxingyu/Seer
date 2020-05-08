 package main.board;
 
 
 import main.globals.Globals;
 import main.globals.Globals.Side;
 import main.pieces.Piece;
 import main.pieces.type.Pawn;
 import main.position.Position;
 
 import java.awt.*;
 import java.util.ArrayList;
 
 /**
  * @author Alex Telon
  */
 public class Board {
     private ArrayList<Piece> pieces = new ArrayList<Piece>();
 
 
     // A map of the board with all its pieces
     private Piece[][] board = new Piece[8][8];
 
     /**
      * Default chess board is created
      */
     public Board() {
         // white:
         for (int i = 0; i < 8; i++) {
             board[6][i] = new Pawn(this, Side.White, i, 6);
         }
 
         // black
         for (int i = 0; i < 8; i++) {
             board[1][i] = new Pawn(this, Side.Black, i, 1);
         }
 
     }
 
     /**
      * Removes piece from the board
      * @param piece piece to be removed
      */
     public void remove(Piece piece) {
         this.pieces.remove(piece);
 
         int x = piece.getPosition().getX();
         int y = piece.getPosition().getY();
         board[y][x] = null;
     }
 
     /**
      * Sees if the new position is occupied by an piece from the same side
      * @param newPosition possible position of a piece
      * @param side side to compare the (possible) other piece with
      * @return true if on the same side
      */
     public boolean isSameSidePiece(Position newPosition, Side side) {
         int x  = newPosition.getX();
         int y  = newPosition.getY();
         Piece piece = board[y][x];
 
         return ( piece != null &&  piece.getSide() == side);
     }
 
     /**
      * Returns what is on the position. null or Piece
      * @param position
      * @return null or Piece
      */
     public Piece getPiece(Position position) {
         return board[position.getY()][position.getX()];
     }
 
     /**
      * Turns a pixel position into a board position.
      * @param pos in pixels
      * @return the position on the board
      */
     public Position getPosOnGridFromPixelPos(Position pos) {
         int xPixel = pos.getX();
         int yPixel = pos.getY();
         int xtmp = Globals.getPieceSize();
         int ytmp = Globals.getPieceSize();
         int xGrid = 0;
         int yGrid = 0;
 
         while (xPixel >= xtmp) {
             xtmp += Globals.getPieceSize();
             xGrid++;
         }
         while (yPixel >= ytmp) {
             ytmp += Globals.getPieceSize();
             yGrid++;
         }
 
         return new Position(xGrid,yGrid);
     }
 
     /**
      * Moves a piece and removes the piece it lands on.
      * @param piece to be moved
      * @param newPosition where the piece is to be moved
      */
     public void movePieceTo(Piece piece, Position newPosition) {
         int x  = newPosition.getX();
         int y  = newPosition.getY();
         if (board[y][x] != null) {
             board[y][x].remove();
         }
         board[y][x] = piece;
     }
 
     /**
      * Empties a position
      * @param position position to be emptied
      */
     public void empty(Position position) {
         int x  = position.getX();
         int y  = position.getY();
         board[y][x] = null;
     }
 
     /**
      * Tries to move a piece. If another piece is on the new position already or
      * if new position is invalid then return false.
      * @param piece piece to be moved
      * @param newPosition new position for the piece
      * @return true if successful
      */
     public boolean tryMove(Piece piece, Position newPosition) {
         if (isSameSidePiece(newPosition, piece.getSide()))
             return false;
 
         Position originalPosition = new Position(piece.getPosition());
 
         // Try to give a new position to the piece
         if (piece.getPosition().setY(newPosition.getY()) &&
                 piece.getPosition().setX(newPosition.getX()) ) {
             empty(originalPosition);
             movePieceTo(piece, newPosition);
             return true;
         }
         return false;
     }
 }
