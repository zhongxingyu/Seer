 package ar.edu.itba.sia.gps.impl;
 
 import java.awt.Point;
 
 import ar.edu.itba.sia.domain.Board;
 import ar.edu.itba.sia.domain.Piece;
import ar.edu.itba.sia.domain.costFunctions.ColorBasedCostFunction;
 import ar.edu.itba.sia.domain.costFunctions.DummyCostFunction;
 import ar.edu.itba.sia.gps.api.CostFunction;
 import ar.edu.itba.sia.gps.api.GPSRule;
 import ar.edu.itba.sia.gps.api.GPSState;
 import ar.edu.itba.sia.gps.exception.NotAppliableException;
 
 public class GPSRuleImpl implements GPSRule {
 
     private Piece piece;
     private int y;
     private int x;
     private CostFunction function;
 
     public GPSRuleImpl(Piece piece, int y, int x) {
         this.piece = piece;
         this.y = y;
         this.x = x;
         this.function = new DummyCostFunction();
     }
 
     public Integer getCost() {
         return function.getCost(piece);
     }
 
     public String getName() {
         return "Put " + piece.toString() + " in " + y + " " + x;
     }
 
     private boolean isValidEvalLocation(GPSState state, Board board) {
     	Point pieceLocation = state.getBoard().getPieceLocation();
     	
         if (pieceLocation == null) {
             return x == 0 && y == 0;
         }
 
         if (pieceLocation.x + 1 == board.getWidth() 
         		&& pieceLocation.y + 1 == board.getHeight()) {
             return false;
         }
 
         if (pieceLocation.x + 1 == board.getWidth()) {
             return pieceLocation.y + 1 == y && x == 0;
         } else {
             return pieceLocation.x + 1 == x && pieceLocation.y == y;
         }
     }
 
     private boolean isValidColorCount(Board board) {
         if (piece.getDownColor() != 0 &&
                 board.getColorCountFor(Board.Direction.UP, piece.getDownColor()) < 0) {
             return false;
         }
         if (piece.getUpColor() != 0 &&
                 board.getColorCountFor(Board.Direction.DOWN, piece.getUpColor()) < 0) {
             return false;
         }
         if (piece.getRightColor() != 0 &&
                 board.getColorCountFor(Board.Direction.LEFT, piece.getRightColor()) < 0) {
             return false;
         }
         if (piece.getLeftColor() != 0 &&
                 board.getColorCountFor(Board.Direction.RIGHT, piece.getLeftColor()) < 0) {
             return false;
         }
         return true;
     }
 
     public GPSState evalRule(GPSState state) throws NotAppliableException {
         Board board = state.getBoard();
 
         if (board.containsPiece(piece)) {
             return null;
         }
 
         if (!board.getPieceIn(x, y).isEmpty()) {
             return null;
         }
 
         return GPSStateImpl.fromParent(state, new Point(x, y), piece);
     }
 
 
 }
