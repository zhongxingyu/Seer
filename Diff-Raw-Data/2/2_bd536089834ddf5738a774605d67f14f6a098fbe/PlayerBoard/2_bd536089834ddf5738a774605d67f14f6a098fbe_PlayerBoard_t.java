 import java.util.Arrays;
 
 /**
  * The board to hold the ships
  * The default size is 10x10 which represents the 1:10 and A:J
  * and leaves column zero and row zero blank
  *
  * S represents a ship
  * X represents a hit on a ship
  * O represents a miss
  * * represents an unused space
  */
 public class PlayerBoard extends Board {
     private int numShips;
 
     public PlayerBoard() {
         this(10, 10);
     }
 
     /* If we want a 10x10 board we create an 11x11 array so that we can omit
      * row and column zero and just use 1:n and 1:m indicies */
     public PlayerBoard(int boardHeight, int boardWidth) {
         this.boardWidth = boardHeight+1;
         this.boardHeight = boardWidth+1;
         this.board = new char[this.boardHeight][this.boardWidth];
         this.numShips = 0;
         boardInit();
     }
 
     protected void boardInit() {
         /* clear the zero'th row and column */
         for(int i = 0; i < this.boardHeight; i++)
             this.board[i][0] = ' ';
         for(int i = 0; i < this.boardWidth; i++)
             this.board[0][i] = ' ';
 
         for(int i = 1; i < this.boardHeight; i++) {
             for(int j = 1; j < this.boardWidth; j++) {
                 this.board[i][j] = '*';
             }
         }
     }
 
     /**
      * Attempts to place a ship on the board.
      * @param row starting row for the ship
      * @param col starting column for the ship
      * @param shipSize the amount of spaces the ship will occupy
      * @param orientation whether it is horizontal or vertical
      * @return Returns true if the ship was successfully placed, or false
      * if there was an error of some nature. An error message will be printed.
      */
 
     public boolean placeShip(int row, int col, int shipSize, char orientation, boolean isAI) {
         /* validate the input values before adding the ship */
         if(!checkAddShipParams(row, col, shipSize, orientation, isAI))
             return false;
         /* if someone is in the spot already we can't put another ship there */
         else if(spotTaken(row, col, shipSize, orientation, isAI))
             return false;
 
         /* We made it this far so everything must be ok, now place the ship */
         if(orientation == 'v' || orientation == 'V') {
             for(int i = 0; i < shipSize; i++)
                 this.board[row+i][col] = 'S';
 
             numShips++;
             return true;
         }
         else { // don't need to check if it equals H because it has to if it passed the param check
             for(int i = 0; i < shipSize; i++)
                 this.board[row][col+i] = 'S';
 
             numShips++;
             return true;
         }
     }
 
     private boolean spotTaken(int row, int col, int shipSize, char orientation, boolean isAI) {
         /* if you wanted this could easily be extended to return or print the
          * first index X,Y where there was a conflict */
         for(int i = 0; i < shipSize; i++) {
             if(orientation == 'h' || orientation == 'H') { // horizontal
                 if(this.board[row][col+i] != '*') {
                     /* Don't print messages for AI errors because the single
                      * player could then learn the AI positions */
                     if(!isAI)
                         System.err.println(String.format("Error: There is already part of a ship at ship at %d, %d", row, col+i));
                     return true;
                 }
             }
             else { // vertical
                 if(this.board[row+i][col] != '*') {
                     if(!isAI)
                         System.err.println(String.format("Error: There is already part of a ship at ship at %d, %d", row, col+i));
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     /* returns true if the ship fits and everything looks ok
      * returns false and prints an error message otherwise */
     private boolean checkAddShipParams(int row, int col, int shipSize, char orientation, boolean isAI) {
         /* check if the coordinates initially make sense */
         if(row < 1 || row > this.boardHeight-1) { // -1 since our 10x10 board is height and width 11
             if(!isAI)
                 System.err.println(String.format("Error: The row must be between 1 and %d", this.boardHeight-1));
             return false;
         }
         else if(col < 1 || col > this.boardWidth-1) { // -1 since our 10x10 board is height and width 11
             if(!isAI)
                 System.err.println(String.format("Error: The column must be between 1 and %d", this.boardWidth-1));
             return false;
         }
         /* is the orientation one we know? */
         else if(orientation != 'h' && orientation != 'H' && orientation != 'v' && orientation != 'V') {
             if(!isAI)
                 System.err.println(String.format("Error: Unrecognized orientation '%c'", orientation));
             return false;
         }
         /* will the ship fit on the board with that size and orientation? */
         else if((orientation == 'h' || orientation == 'H') && (col + (shipSize-1) > this.boardWidth-1)) {
             if(!isAI)
                 System.err.println("Error: The ship does not fit on the board there");
             return false;
         }
         else if((orientation == 'v' || orientation == 'V') && (row + (shipSize-1) > this.boardHeight-1)) {
             if(!isAI)
                 System.err.println("Error: The ship does not fit on the board there");
             return false;
         }
 
         /* Everything looks good! */
         return true;
     }
 
     /**
      * Checks if a given shot is a hit or miss.
      * @param row The target row for the shot.
      * @param col The target column for the shot.
      * @return Returns true if the shot was a hit. Returns false if the shot
      * was a miss or if the spot was already targetted */
     public boolean checkShot(int row, int col) {
         /* invalid board position */
         if(row < 1 || col < 1 || row > (this.boardHeight-1) || col > (this.boardWidth-1))
             return false;
 
         /* We have a hit! */
         if(this.board[row][col] == 'S') {
             this.board[row][col] = 'X';
 
             /* check if this was the last part of a ship, if so decrement the
              * number of ships left */
             if(this.board[row+1][col] != 'S' && this.board[row][col+1] != 'S' &&
                this.board[row-1][col] != 'S' && this.board[row][col-1] != 'S') {
                numShips--;
             }
 
             return true;
         }
         /* Did they really shoot at the same spot again? */
         else if(this.board[row][col] == 'X' || this.board[row][col] == 'O') {
             /* do nothing, penalize them for their foolish double shot! */
            return this.board[row][col] == 'X' ? true : false; // return whatever was already there
         }
         /* The must have missed then */
         else {
             this.board[row][col] = 'O';
             return false;
         }
     }
 
     public boolean hasShipsLeft() {
         return this.numShips > 0;
     }
 
     public char[][] getBoard() {
         return this.board;
     }
 
     public int getBoardWidth() {
         return this.boardWidth;
     }
 
     public int getBoardHeight() {
         return this.boardHeight;
     }
 
     public String toString() {
         StringBuilder s = new StringBuilder();
         for(int i = 0; i < this.boardHeight; i++) {
             s.append(Arrays.toString(this.board[i]));
             s.append("\n");
         }
 
         return s.toString();
     }
 }
