 package antichess;
 
 public class Board {
 
    private Piece[][] squares;
 
    public Board() {
       //Initialises the board.
       squares = new Piece[8][8];
       squares[0][0] = new Rook(0, 0, 'w');
       squares[1][0] = new Knight(0, 1, 'w');
       squares[2][0] = new Bishop(0, 2, 'w');
       squares[3][0] = new Queen(0, 3, 'w');
       squares[4][0] = new King(0, 4, 'w');
       squares[5][0] = new Bishop(0, 5, 'w');
       squares[6][0] = new Knight(0, 6, 'w');
       squares[7][0] = new Rook(0, 7, 'w');
       for (int i = 0; i < 8; i++) {
          squares[i][1] = new Pawn(i, 1, 'w');
          squares[i][6] = new Pawn(i, 1, 'b');
       }
       for (int i = 0; i < 8; i++) {
          for (int j = 2; i < 6; i++) {
             squares[i][j] = null;
          }
       }
       squares[0][7] = new Rook(0, 0, 'b');
       squares[1][7] = new Knight(0, 1, 'b');
       squares[2][7] = new Bishop(0, 2, 'b');
       squares[3][7] = new Queen(0, 3, 'b');
       squares[4][7] = new King(0, 4, 'b');
       squares[5][7] = new Bishop(0, 5, 'b');
       squares[6][7] = new Knight(0, 6, 'b');
       squares[7][7] = new Rook(0, 7, 'b');
    }
 
    public void drawBoard() {
       for (int row = 7; row >= 0; row--) {
          System.out.format("%d ", row + 1);
          for (int col = 0; col < 8; col++) {
             if (squares[col][row] != null) {
                System.out.print(squares[col][row].getAppearance() + " ");
             } else {
                System.out.print("  ");
             }
          }
          System.out.println();
       }
       System.out.println("  a b c d e f g h ");
       System.out.println();
       //if (isPathClear(new Move(5,0,2,3))) {
       //   System.out.println("Bishop is clear");
       //}
    }
 
    public boolean isPathClear(Move move) {
       int xDelta = move.newX - move.oldX;
       int yDelta = move.newY - move.oldY;
       int absXDelta = Math.abs(xDelta);
       int absYDelta = Math.abs(yDelta);
       int steps = Math.max(absXDelta, absYDelta);
 
       if (xDelta != 0 && yDelta != 0 && absXDelta != absYDelta) {
          //Not a valid path
          System.out.println("Path is no straight/diagonal");
          return false;
       } else {
          //Path is at least straight or diagonal
          int xIncrement = (move.newX - move.oldX) / steps;
          int yIncrement = (move.newY - move.oldY) / steps;
          for (int step = 1; step < steps ; step++) {
             if (squares[move.oldX + step * xIncrement][move.oldY + step * yIncrement] != null) {
                System.out.format("Problem on step = %d\n",step);
                return false;
             }
          }
       }
       return true;
    }
 
    public boolean isMoveValid(char playerColour, Move move) {
       //check destination isn't the same as origin
       if (move.newX == move.oldX && move.newY == move.oldY) {
          return false;
       }
 
       //check if the piece exists
       if (squares[move.oldX][move.oldY] == null) {
          return false;
       }
 
       //check if the piece belongs to the current player
       if (!squares[move.oldX][move.oldY].isPlayersPiece(playerColour)) {
          return false;
       }
 
       //check the destination piece (if any) doesn't belong to the player
       if (squares[move.newX][move.newY] != null &&
               squares[move.newX][move.newY].isPlayersPiece(playerColour)) {
          return false;
       }
 
       //check if the move is valid
       return squares[move.oldX][move.oldY].isMoveValid(this, move);
    }
 
    public boolean isMoveCapture(Move move) {
       //If there is no piece in the new square then it can't be a capture
       //otherwise it is capture (or not valid for some reason which should be
       //dealt with by isMoveValid() ).
      return (squares[move.newX][move.newY] == null);
    }
 
    public void makeMove(Move move) {
       /* Assuming that everything has been checked by isMoveValid() and
        * isMoveCapture() this function simply replaces the contents of the
        * new square with the contents of the old square and wipes the old
        * square
        */
       squares[move.newX][move.newY] = squares[move.oldX][move.oldY];
       squares[move.oldX][move.oldY] = null;
 
       //Updates the Piece instances position values
       squares[move.newX][move.newY].setPosition(move.newX, move.newY);
    }
 
    public boolean isCapturePossible(char playerColour) {
       //TO DO
       //Bit of a placeholder.
       //Currently loops over all squares, checks if it belongs to the player
       //and checks if so checks if the piece can capture. Obviously if any piece
       //can capture the answer is true so it can return straight away.
 
       for (int i = 0; i < 8; i++) {
          for (int j = 0; j < 8; j++) {
             if (squares[i][j] != null && squares[i][j].isPlayersPiece(playerColour)) {
                if (squares[i][j].isCapturePossible(this)) {
                   return true;
                }
             }
          }
       }
       return false;
    }
 
    public char isWon() {
       //Horribly crude function to check if there are zero black or zero white
       //pieces. Returns ' ' if both players still have pieces, 'w' if white has
       //no pieces and 'b' if black has no pieces.
       int whiteCount = 0;
       int blackCount = 0;
       for (int i = 0; i < 8; i++) {
          for (int j = 0; j < 8; j++) {
             if (squares[i][j] == null) {
                if (squares[i][j].pieceColour() == 'w') {
                   whiteCount++;
                } else {
                   blackCount++;
                }
             }
          }
       }
       if (blackCount == 0) {
          return 'b';
       } else if (whiteCount == 0) {
          return 'w';
       } else {
          return ' ';
       }
    }
 
    public boolean isStaleMate() {
       //TO DO
       //Apart from the whole bishops on different colour squares I'm not sure
       //what needs checking.
 
       /* (JC) - I think the best way to implement this is to handle each
        * different scenario of remaining pieces that would cause a stalemate
        * one by one.  For instance:
        * 1. Each player has one bishop remaining and the bishop is sitting on
        *    a square that is different in colour than the square occupied by the
        *    opposing bishop.
        *
        *   --- we can continue adding scenarios to this list, and then code each
        *   one as an if statement.
        */
       return false;
    }
 }
