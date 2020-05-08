 package net.anatolich.mahjong.mahjong;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Stack;
 import net.anatolich.mahjong.game.AvailableMove;
 import net.anatolich.mahjong.game.Board;
 import net.anatolich.mahjong.game.Coordinates;
 import net.anatolich.mahjong.game.GameEvent;
 import net.anatolich.mahjong.game.Piece;
 import net.anatolich.mahjong.game.rules.Rules;
import net.anatolich.mahjong.game.spi.AbstractGameSession;
 import net.anatolich.mahjong.game.spi.MutableBoard;
 
 /**
  * Game session keeps track of game and allows player to interact with game.
  *
  * @author Dmytro Kovalchuk<dimasmith@gmail.com>
 * @version 1.0
 * @since 1.0
  */
 public class GameSessionImpl extends AbstractGameSession {
 
     private final MutableBoard board;
     private final Rules rules;
     private boolean moveWasCompleted = false;
     private final AvailableMovesCollector availableMovesCollector;
     private List<AvailableMove> availableMoves;
     private Stack<Piece> pickedPieces = new Stack<>();
 
     public GameSessionImpl(MutableBoard board, Rules rules) {
         this(board, rules, new AvailableMovesCollector(board, rules));
     }
 
     GameSessionImpl(MutableBoard board, Rules rules, AvailableMovesCollector availableMovesCollector) {
         this.board = board;
         this.rules = rules;
         this.availableMovesCollector = availableMovesCollector;
         startGame();
     }
 
     @Override
     public Board getBoard() {
         return board;
     }
 
     @Override
     public List<Piece> getPickedPieces() {
         return new ArrayList<>(pickedPieces);
     }
 
     @Override
     public boolean wasMoveCompleted() {
         return moveWasCompleted;
     }
 
     @Override
     public void pickPieceAt(Coordinates coordinates) {
         moveWasCompleted = false;
         final Piece piece = board.getPieceAt(coordinates);
 
         if (noPickedPieces()) {
             if (rules.isPieceOpen(coordinates, board)) {
                 pickPiece(piece);
             }
         } else {
 
             if (rules.isMoveLegal(pickedPieces.peek().getCoordinates(), piece.getCoordinates(), board)) {
                 pickedPieces.push(piece);
                 completeMove(new ArrayList<>(pickedPieces));
             } else {
                 if (rules.isPieceOpen(coordinates, board)) {
                     pickedPieces.pop();
                     pickPiece(piece);
                 }
             }
         }
     }
 
     @Override
     public boolean hasMoreMoves() {
         return !getAvailableMoves().isEmpty();
     }
 
     @Override
     public boolean isGameEnded() {
         return isGameWon();
     }
 
     @Override
     public List<AvailableMove> getAvailableMoves() {
         return availableMoves;
     }
 
     final void startGame() {
         if (isMovesAvailable()) {
             fireNoMovesLeft();
         }
     }
 
    private void pickPiece(final Piece piece) {
         pickedPieces.push(piece);
         firePiecePickedEvent();
     }
 
     @Override
     public String toString() {
         return "GameSessionImpl{" + "moveWasCompleted=" + moveWasCompleted + '}';
     }
 
     private void firePiecePickedEvent() {
         final GameEvent event = new GameEvent(GameEvent.Type.SELECTION_CHANGED, this, pickedPieces.peek());
         firePickedPieceChanged(event);
     }
 
     @Override
    protected final boolean isGameWon() {
         return board.getAllPieces().isEmpty();
     }
 
     @Override
    protected final boolean isMovesAvailable() {
         this.availableMoves = availableMovesCollector.collectMoves();
         return availableMoves.isEmpty();
     }
 
     @Override
    protected final void doMove(List<Piece> affectedPieces) {
         for (Piece piece : affectedPieces) {
             board.removePieceAt(piece.getCoordinates());
         }
         pickedPieces.clear();
         moveWasCompleted = true;
     }
 
     private boolean noPickedPieces() {
         return pickedPieces.isEmpty();
     }
 }
