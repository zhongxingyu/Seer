 package no.hist.aitel.android.tictactoe;
 
 import com.google.common.base.Strings;
 
 public class GameBoard {
 
     private int inRow;
     private int moveCount;
     private GameState[][] board;
     private int x;
     private int y;
     private GameState player;
 
     /**
      * Create an empty board of the given size
      *
      * @param size Board size
      */
     public GameBoard(final int size) {
         this.board = new GameState[size][size];
         for (int i = 0; i < size; i++) {
             for (int j = 0; j < size; j++) {
                 this.board[i][j] = GameState.EMPTY;
             }
         }
     }
 
     /**
      * Set how many required pieces in row
      *
      * @param inRow Number of pieces in row
      */
     public void setInRow(int inRow) {
         this.inRow = inRow;
     }
 
    /**
     * Set the position to the given state
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param s State to set
     * @return
     */
     public GameState put(int x, int y, GameState s) {
         if (board[x][y] == GameState.EMPTY) {
             board[x][y] = s;
             this.x = x;
            this.y = y;
             this.player = s;
             moveCount++;
             return GameState.VALID_MOVE;
         } else {
             return GameState.INVALID_MOVE;
         }
     }
 
     /**
      * Get player of the current position
      *
      * @param x X coordinate
      * @param y Y coordinate
      * @return State in the given position
      */
     public GameState get(int x, int y) {
         if (x < 0 || x >= board.length) {
             throw new IllegalArgumentException(String.format("x must be between 0 and %s", board.length - 1));
         }
         if (y < 0 || y >= board[x].length) {
             throw new IllegalArgumentException(String.format("y must be between 0 and %s", board[x].length - 1));
         }
         return board[x][y];
     }
 
     /**
      * Get the current game state
      *
      * @return State of the game
      */
     public GameState getState() {
         //check end conditions
         //check col
         for (int i = 0; i < inRow; i++) {
             if (board[x][i] != player) {
                 break;
             }
             if (i == inRow - 1) {
                 //report win for player
                 return GameState.WIN;
             }
         }
         //check row
         for (int i = 0; i < inRow; i++) {
             if (board[i][y] != player) {
                 break;
             }
             if (i == inRow - 1) {
                 //report win for player
                 return GameState.WIN;
             }
         }
         //check diag
         if (x == y) {
             //we're on a diagonal
             for (int i = 0; i < inRow; i++) {
                 if (board[i][i] != player) {
                     break;
                 }
                 if (i == inRow - 1) {
                     //report win for player
                     return GameState.WIN;
                 }
             }
         }
         //check anti diag (thanks rampion)
         for (int i = 0; i < inRow; i++) {
             if (board[i][(inRow - 1) - i] != player) {
                 break;
             }
             if (i == inRow - 1) {
                 //report win for player
                 return GameState.WIN;
             }
         }
         //check draw
         if (moveCount == (inRow ^ 2) - 1) {
             //report draw
             return GameState.DRAW;
         }
         return GameState.NEUTRAL;
     }
 
     /**
      * Format enums as traditional Tic-tac-toe characters
      *
      * @param state State to convert
      * @return Character representing player 1, player 2 or empty
      */
     private char stateToChar(GameState state) {
         switch (state) {
             case PLAYER1: {
                 return 'X';
             }
             case PLAYER2: {
                 return 'O';
             }
             default: {
                 return ' ';
             }
         }
     }
 
     /**
      * String representation of the board
      *
      * @return The board
      */
     public String toString() {
         final StringBuilder out = new StringBuilder();
         final int fieldWidth = 3;
         final int rowLength = board.length > 0 ? board[0].length : 0;
         final StringBuilder separator = new StringBuilder();
         for (int i = 0; i < rowLength; i++) {
             separator.append("+");
             separator.append(Strings.repeat("-", fieldWidth));
         }
         final int lastIdx = rowLength - 1;
         for (int i = 0; i < board.length; i++) {
             out.append(separator);
             out.append("+\n| ");
             for (int j = 0; j < rowLength; j++) {
                 out.append(stateToChar(board[i][j]));
                 if (j != lastIdx) {
                     out.append(" | ");
                 }
             }
             out.append(" |\n");
         }
         out.append(separator);
         out.append('+');
         return out.toString();
     }
 }
 
