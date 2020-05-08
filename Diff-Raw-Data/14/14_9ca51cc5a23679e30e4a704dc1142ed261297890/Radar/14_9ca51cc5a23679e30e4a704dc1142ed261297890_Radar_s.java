 import java.util.Arrays;
 
 /**
  * The radar to show what has been discovered of the enemy
  * The default size is 10x10 which represents the 1:10 and A:J
  * and leaves column zero and row zero blank
  *
  * X represents a hit on a ship
  * O represents a miss
  * * represents an unused space
  */
 public class Radar extends Board {
 
     public Radar() {
         this(10, 10);
     }
 
     /* If we want a 10x10 board we create an 11x11 array so that we can omit
      * row and column zero and just use 1:n and 1:m indicies */
     public Radar(int boardHeight, int boardWidth) {
         this.boardWidth = boardHeight+1;
         this.boardHeight = boardWidth+1;
         this.board = new char[this.boardHeight][this.boardWidth];
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
      * Marks a given shot on the radar
      * @param row Row to mark
      * @param col Column to mark.
      * @param hitOrMiss 'h' or 'H' indicates a hit. 'm' or 'M' indicates a miss.
      * @return Returns true if the spot is successfully marked. False otherwise
      * with a printed error message
      */
     public boolean markRadar(int row, int col, char hitOrMiss) {
         if(!checkShotParams(row, col, hitOrMiss))
             return false;
 
         if(hitOrMiss == 'h' || hitOrMiss == 'H') {
             this.board[row][col] = 'X';
             return true;
         }
         else { // no need to check for 'm' or 'M' since we passed the check params
             this.board[row][col] = 'O';
             return true;
         }
     }
 
     /* returns true if it is a valid location, false otherwise */
     private boolean checkLocation(int row, int col) {
         /* check if the coordinates are in the playable range */
         if(row < 1 || row > this.boardHeight-1) {
             System.err.println(String.format("Error: The row must be between 1 and %d", this.boardHeight-1));
             return false;
         }
         else if(col < 1 || col > this.boardWidth-1) {
             System.err.println(String.format("Error: The column must be between 1 and %d", this.boardWidth-1));
             return false;
         }
 
         // Looks ok
         return true;
     }
 
     /* returns true if the parameters are valid, false otherwise */
     private boolean checkShotParams(int row, int col, char hitOrMiss) {
         if(!checkLocation(row, col))
             return false;
 
         /* is the hitOrMiss one we know? */
         else if(hitOrMiss != 'h' && hitOrMiss != 'H' && hitOrMiss != 'm' && hitOrMiss != 'M') {
            System.err.println(String.format("Error: Unrecognized his or miss value '%c'", hitOrMiss));
             return false;
         }
 
         /* Everything looks good! */
         return true;
     }
 
     /**
      * Returns the value stored in the radar at a given location
      * @param row The row to check.
      * @param col The column to check.
      * @return Will return the character stored at that location if it is a
      * valid spot, or the null character for any invalid locations.
      */
     public char getLocation(int row, int col) {
         if(!checkLocation(row, col))
             return '\0';
 
         return this.board[row][col];
     }
 
     public char[][] getBoard() {
         return this.board;
     }
 
     public int getRadarWidth() {
         return this.boardWidth;
     }
 
     public int getRadarHeight() {
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
