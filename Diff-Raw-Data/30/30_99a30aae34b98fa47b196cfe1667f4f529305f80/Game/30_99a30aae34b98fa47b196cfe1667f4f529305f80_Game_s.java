 package charland.games.go;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 
 /**
  * The logic of the game is housed in this class.
  * 
  * @author Michael
  * 
  */
 public class Game extends Activity {
 
     private static final String GAME_BOARD = "GAME_BOARD";
 
     /**
      * The graphical end of things.
      */
     private PuzzleView puzzle;
 
     /**
      * The only instance of the game board.
      */
     private Board board;
    
     /** The amount of black stones that have been captured. */
     private int blackStonesCaptured;
    
     /** The amount of white stones that have been captured. */
     private int whiteStonesCaptured;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Log.d(Go.TAG, "onCreate");
         if (savedInstanceState != null && savedInstanceState.containsKey(GAME_BOARD)) {
             board = new Board(savedInstanceState.getShortArray(GAME_BOARD));
         } else {
             board = new Board();
         }
        
         puzzle = new PuzzleView(this);
         setContentView(puzzle);
         puzzle.requestFocus();
     }
 
     /** Who's turn is it? */
     private int turn;
 
     /**
      * The current player executes there turn.
      * 
      * @param x
      *            The x location.
      * @param y
      *            The y location.
      * @return Did the spot get occupied?
      */
     public boolean playTurn(int x, int y) {
        if (board.isOccupied(x, y) == Board.EMPTY) {
             return true;
         }
        if (turn % 2 == 0) {
             board.occupyBlack(x, y);
         } else {
             board.occupyWhite(x, y);
         }
 
        if (board.checkAllLibertiesFor(turn % 2 == 0 ? Board.BLACK : Board.WHITE)) {
 
             // TODO: This call is inefficient and should be looked at later to just redraw the stones removed.
             puzzle.invalidate();
         }
 
         ++turn;
         return false;
     }
 
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
         // Store the game board into the bundle so it is not lost when for example the device is rotated.
         savedInstanceState.putShortArray(GAME_BOARD, board.getShortArray());
     }
 
     /**
      * @return The game board.
      */
     public short[][] getGameBoard() {
         return board.getBoard();
     }
 
     public int getWhiteStonesCaptured() {
         return whiteStonesCaptured;
     }
 
     public int getBlackStonesCaptured() {
         return blackStonesCaptured;
     }
 
 }
