 package no.hist.aitel.android.tictactoe;
 
 import com.google.common.base.Strings;
 
 /**
  * This class handles board logic
  */
 public class GameBoard {
 
     private GamePlayer[][] board;
     private int inRow;
     private int moveCount;
     private int x;
     private int y;
     private GamePlayer player;
 
     /**
      * Create an empty board of the given size
      *
      * @param size Board size
      */
     public GameBoard(final int size) {
         this.board = new GamePlayer[size][size];
         for (int i = 0; i < size; i++) {
             for (int j = 0; j < size; j++) {
                 this.board[i][j] = GamePlayer.EMPTY;
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
      * Set the position to the given player
      *
      * @param x X coordinate
      * @param y Y coordinate
      * @param p Player to set
      * @return Game state
      */
     public GameState put(int x, int y, GamePlayer p) {
         if (x < 0 || x >= board.length) {
             throw new IllegalArgumentException(
                     String.format("x must in range(0,%d)", board.length - 1));
         }
         if (y < 0 || y >= board[x].length) {
             throw new IllegalArgumentException(
                     String.format("y must be in range(0,%d)", board[x].length - 1));
         }
         if (p == player) {
             throw new IllegalArgumentException(
                     String.format("Player %s had previous move, can't move again", p));
         }
         if (board[x][y] == GamePlayer.EMPTY) {
             board[x][y] = p;
             this.x = x;
             this.y = y;
             this.player = p;
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
      * @return Player in the given position
      */
     public GamePlayer get(int x, int y) {
         if (x < 0 || x >= board.length) {
             throw new IllegalArgumentException(
                     String.format("x must be in range(0,%d)", board.length - 1));
         }
         if (y < 0 || y >= board[x].length) {
             throw new IllegalArgumentException(
                     String.format("y must be in range(0,%d)", board[x].length - 1));
         }
         return board[x][y];
     }
 
     /**
      * Get the current game state
      *
      * @return State of the game
      */
     public GameState getState() {
         // Column
         for (int i = 0; i < inRow; i++) {
             if (board[x][i] != player) {
                 break;
             }
             if (i == inRow - 1) {
                 return GameState.WIN;
             }
         }
         // Row
         for (int i = 0; i < inRow; i++) {
             if (board[i][y] != player) {
                 break;
             }
             if (i == inRow - 1) {
                 return GameState.WIN;
             }
         }
         // Diagonal
         if (x == y) {
             for (int i = 0; i < inRow; i++) {
                 if (board[i][i] != player) {
                     break;
                 }
                 if (i == inRow - 1) {
                     return GameState.WIN;
                 }
             }
         }
         // Reverse diagonal
         for (int i = 0; i < inRow; i++) {
             if (board[i][(inRow - 1) - i] != player) {
                 break;
             }
             if (i == inRow - 1) {
                 return GameState.WIN;
             }
         }
         // Draw
         if (moveCount == Math.pow(board.length, 2) - 1) {
             return GameState.DRAW;
         }
         return GameState.NEUTRAL;
     }
 
     /**
      * Format enums as traditional Tic-tac-toe symbols
      *
      * @param player Player to convert
      * @return Symbol representing player 1, player 2 or empty
      */
     private char playerToSymbol(GamePlayer player) {
         switch (player) {
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
     @Override
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
        for (GamePlayer[] row : board) {
             out.append(separator);
             out.append("+\n| ");
            for (int j = 0; j < rowLength; j++) {
                out.append(playerToSymbol(row[j]));
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
 
