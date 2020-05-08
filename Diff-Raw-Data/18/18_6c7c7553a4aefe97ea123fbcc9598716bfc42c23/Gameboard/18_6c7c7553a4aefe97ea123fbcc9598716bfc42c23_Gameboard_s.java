 /* Gameboard */
 
 import java.util.*;
 import player.*;
 
 public class Gameboard {
 
     protected int[][] board;
 
     public static final int INVALID = -1; //represents an invalid square
     public static final int EMPTY = 0; //represents empty square
     public static final int WHITE = 1; //represents a square containing white piece
     public static final int BLACK = 2; //represents a square containing black piece 
 
     public static final int TOTAL = 10; //represents total allotted pieces per player
     public static final int WHITEPLAYER = 1;
     public static final int BLACKPLAYER = 2;
 
     protected int whiteCount;
     protected int blackCount;
 
     protected int width;
     protected int height;
     
     /**
      * Gameboard constructor takes no parameters, and constructs
      * a default 8 x 8 2-D array of integers.
      */
     public Gameboard() {
         //fills all squares with EMPTY
         this.width = 8;
         this.height = 8;
         this.whiteCount = 10;
         this.blackCount = 10;
 
         this.board = new int[width][height];
         for (int i = 0; i < width; i++) {
             for(int j = 0; j < height; j++) {
                 board[i][j] = EMPTY;
             }
         }
         //sets corner squares (0,0), (0,7), (7,0), (7,7) to INVALID
         board[0][0] = INVALID;
         board[0][7] = INVALID;
         board[7][0] = INVALID;
         board[7][7] = INVALID;
     }
 
     /**
      * Gameboard constructor takes two parameters, and constructs
      * a width * height 2-D array of integers.
      */
     public Gameboard(int width, int height) {
         //fills all squares with EMPTY
         this.width = width;
         this.height = height;
         this.whiteCount = 10;
         this.blackCount = 10;
         int[][] board = new int[width][height];
         for (int i = 0; i < width; i++) {
             for(int j = 0; j < height; j++) {
                 board[i][j] = EMPTY;
             }
         }
         //sets corner squares (0,0), (0,7), (7,0), (7,7) to INVALID
         board[0][0] = INVALID;
         board[0][height - 1] = INVALID;
         board[width - 1][0] = INVALID;
         board[width - 1][width - 1] = INVALID;
     }
 
     /**
      * getWhiteCount() takes no parameters returns the number of white 
      * pieces remaining for "this" Gameboard.
      *
      * @return number of white pieces
      */
 
     public int getWhiteCount() {
         return whiteCount;
     }
 
     /**
      * getBlackCount() takes no parameters and returns the number of black 
      * pieces remaining for "this" Gameboard.
      *
      * @return number of black pieces
      */
 
     public int getBlackCount() {
         return blackCount;
     }
 
     /**
      * getNeighbors() takes 2 parameters - namely, the coordinate - and returns a 3 x 3 2-D square array
      * of representing squares around the given square.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      *
      * @return a 3 x 3 int[][] containing squares located orthogonally
      * and diagonally adjacent to the square at (x,y). If square is at the side/corner, it fills 
      * remainig 3X3 array with -1s
      */
 
     private int[][] getNeighbors(int x, int y) {
         int toReturn[][] = new int[3][3];
         if(x >= 0 && x<= this.width-1 && y >= 0 && y <= this.width-1){
             //int toReturn[][] = new int[3][3];
             toReturn[0][0] = get00(x,y);
             toReturn[0][1] = get01(x,y);
             toReturn[0][2] = get02(x,y);
             toReturn[1][0] = get10(x,y);
             toReturn[1][1] = INVALID;
             toReturn[1][2] = get12(x,y);
             toReturn[2][0] = get20(x,y);
             toReturn[2][1] = get21(x,y);
             toReturn[2][2] = get22(x,y);
         } else {
             System.out.println("Invalid x,y given in getNeighbors x: "+x+" y: "+y+" .");
             //int toReturn[][] = new int[3][3];
             for(int i = 0; i < 3; i++){
                 for(int j = 0; j < 3; j++){
                     toReturn[i][j] = INVALID;
                 }
             }
         }
         return toReturn; 
     }
     
     //helper methods for getNeighbors
     private int get00(int x, int y){
         if(x - 1 >= 0 && y - 1 >= 0){
             return this.board[x-1][y-1];
         } else {
             return INVALID;
         }
     }
 
     private int get10(int x, int y){
         if(y-1 >= 0){
             return this.board[x][y-1];
         } else {
             return INVALID;
         }
     }
 
     private int get20(int x, int y){
         if(x + 1 <= this.width-1 && y-1 >= 0){
             return this.board[x+1][y-1];
         } else {
             return INVALID;
         }
     }
 
     private int get01(int x, int y){
         if(x - 1 >= 0){
             return this.board[x-1][y];
         } else {
             return INVALID;
         }
     }
 
     private int get21(int x, int y){
         if(x+1 <= this.width-1){
             return this.board[x+1][y];
         } else {
             return INVALID;
         }
     }
 
     private int get02(int x, int y){
         if(x-1 >= 0 && y+1 <= this.width-1){
             return this.board[x-1][y+1];
         } else {
             return INVALID;
         }
     }
 
     private int get12(int x, int y){
         if(y+1 <= this.width-1){
             return this.board[x][y+1];
         } else {
             return INVALID;
         }
     }
 
     private int get22(int x, int y){
         if(x+1 <= this.width-1 && y+1<=this.width-1){
             return this.board[x+1][y+1];
         } else {
             return INVALID;
         }
     }
 
     /**
      * getType() takes 2 parameters, and returns an int representing the type
      * of the square.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      *
      * @return type of the square, a 0 represents EMPTY, a 1 represents WHITE,
      * a 2 represents BLACK.
      */
 
     public int getType(int x, int y) {
        return this.board[x][y];
     }
 
     /**
      * setType() takes 3 parameters, the coordinates, and an integer representing the new type, and 
      * changes the type of the square to the new type.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      * @param type represents the type which the square will be changed to. If
      * newType is the same as the type of the square, nothing is changed.
      */
 
     private void setType(int x, int y, int type) {
        this.board[x][y] = type;
     }
 
     /**
      * isValid() takes 2 parameters, the coordinates, and checks whether the type
      * of square is INVALID.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      *
      * @return true if type of the square is INVALID, false otherwise.
      */
 
     public boolean isValid(int x, int y) {
         if(getType(x,y) != INVALID){
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * isEmpty() takes 2 parameters, the coordinates, and checks whether the type 
      * of square is EMPTY.
      * 
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      *
      * @return true if type of the square is EMPTY, false otherwise.
      */
 
     public boolean isEmpty(int x, int y) {
         if(getType(x,y) == EMPTY){
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * isWhite() takes 2 parameters, the coordinate, and checks whether the type
      * of square is WHITE.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      *
      * @return true if type of the square is WHITE, false otherwise.
      */
 
     public boolean isWhite(int x, int y) {
         if(getType(x, y) == WHITE){
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * isBlack() takes 2 parameters, the coordinates, and checks whether the type
      * of square is BLACK.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      *
      * @return true if type of square is BLACK, false otherwise.
      */
 
     public boolean isBlack(int x, int y) {
         if(getType(x, y) == BLACK){
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * getColumn() takes 1 parameters, the x-coordainte, and returns an array of square ints
      * in the same row as x.
      *
      * @param x the x-coordinate of the row
      *
      * @return a int[] containing squares in the same row as int x.
      */
 
     private int[] getColumn(int x) {
         int columnChips[] = new int[8];
         for(int j = 0; j < this.height; j++){
             columnChips[j] = this.board[x][j];
         }
         return columnChips;
     }
 
     /**
      * getRow() takes 1 parameters, the y-coordinate, and returns an array of square ints
      * in the same column as the y.
      *
      * @param y the y-coordinate of the column
      *
      * @return a int[] containing squares in the same column as int y.
      */
 
     private int[] getRow(int y) {
         int rowChips[] = new int[8];
         for(int i = 0; i < this.width; i++){
             rowChips[i] = this.board[i][y];
         }
         return rowChips;
     }
 
     /**
      * getDiagonalLength() takes 3 parameters, the coordinates, the direction , and returns
      * and integer representing the number of squares in the same diagonal
      * as the square regardless of direction.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      * @param direction is an integer representing the direction of the diagonal to
      * return. More specifically, a -1 represents a northwest-southeast diagonal, 
      * and a 1 represents a northeast-southwest diagonal
      *
      * @return an integer representing the length of the diagonal
      */
 
     private int getDiagonalLength(int x, int y, int direction) {
     //this method uses a formula to compute the number of diagonal chips
     int len = x + y + 1;
         if(direction == -1){
             len = this.height - y + x;
             if(len > this.height){
               len = this.height - len % this.height;
             }
         } else if(direction == 1){
             if(len > this.height){
               len = this.height - len % this.height;
             }
         }
         return len;
     }
 
     /**
      * getDiagonal() takes 3 parameters, the coordiantes, a direction, and returns
      * an array of square objects in the same diagonal as the square
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      * @param direction is an integer representing the direction of the diagonal to
      * return. More specifically, a -1 represents a northwest-southeast diagonal, 
      * and a 1 represents a northeast-southwest diagonal.
      *
      * @return a int[] containing squares in the same diagonal as the square.
      */
 
     private int[] getDiagonal(int x, int y, int direction) {
         int diagonalLength = getDiagonalLength(x,y,direction);
         int diagonalChips[] = new int[diagonalLength];
         int startX = x;
         int startY = y;
         if(direction == -1){
             if(x > y){
                 startY = 0;
                 startX = x-y;
             } else if(y > x){
                 startX = 0;
                 startY = y-x;
             } else {
                 startX = 0;
                 startY = 0;
             }
             int xy = 0;
             while((startX < this.width && startY < this.height) && xy < diagonalLength){
                 diagonalChips[xy] = this.board[startX][startY];
                 startX += 1;
                 startY += 1;
                 xy += 1;
             }
         } else if(direction == 1){
             if(x + y > this.width - 1){
                 startX = this.width - 1;
                 startY = x + y - this.width + 1;
             } else {
                 startY = 0;
                 startX = x + y;
             }
             int xy2 = 0;
             while((startX > 0 && startY < this.height) && xy2 < diagonalLength){
                 diagonalChips[xy2] = this.board[startX][startY];
                 startX -= 1;
                 startY += 1;
                 xy2 += 1;
             }
         }
         return diagonalChips;
     }
 
     /**
      * findConnectedLDiagonal() takes 2 parameters, the coordinates, and returns a list of connected squares of the same type in the left diagonal.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      *
      * @return a int[] containing "connected" squares of the same type in the left diagonal
      */
 
     private int[][] findConnectedLDiagonal(int x, int y) {
         int type = getType(x, y);
 
         int[][] connectedChips = new int[2][2];
 
         int[] diagonal = getDiagonal(x, y, -1);
         int length = diagonal.length;
         int location = -1;
         if (y >= x) {
             location = x;
         } else {
             location = y;
         }
 
         for (int i = 1; location - i >= 0; i++) {
             if (diagonal[location - i] == type) {
                 connectedChips[0][0] = x - i;
                 connectedChips[0][1] = y - i;
                 break;
             }
             if (diagonal[location - i] != type && checkPiece(diagonal[location - i])) {
                 break;
             }
         }
 
         for (int i = 1; i + location < length; i++) {
             if (diagonal[i + location] == type) {
                 connectedChips[1][0] = x + i;
                 connectedChips[1][1] = y + i;
                 break;
             }
             if (diagonal[location + i] != type && checkPiece(diagonal[location + i])) {
                 break;
             }
         }
         return connectedChips;
     }
 
     /**
      * findConnectedRDiagonal() takes 2 parameters, the coordinates, and returns a list of connected squares of the same type in the right diagonal.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      *
      * @return a int[] containing "connected" squares of the same type in the right diagonal
      */
 
     private int[][] findConnectedRDiagonal(int x, int y) {
         int type = getType(x, y);
 
         int[][] connectedChips = new int[2][2];
 
         int[] diagonal = getDiagonal(x, y, 1);
         int length = diagonal.length;
         int location = -1;
         if (y > (this.width - x - 1)) {
             location = this.width - x - 1;
         } else {
             location = y;
         }
 
         for (int i = 1; location - i >= 0; i++) {
             if (diagonal[location - i] == type) {
                 connectedChips[0][0] = x + i;
                 connectedChips[0][1] = y - i;
                 break;
             if (diagonal[location - i] != type && checkPiece(diagonal[location - i])) {
                 break;
             }
         }
 
         for (int i = 1; i + location < length; i++) {
             if (diagonal[i + location] == type) {
                 connectedChips[1][0] = x - i;
                 connectedChips[1][1] = y + i;
                 break;
             if (diagonal[location + i] != type && checkPiece(diagonal[location + i])) {
                 break;
             }
         }
         return connectedChips;
     }
 
     /**
      * findConnectedRow() takes 2 parameters, the coordinates, and returns a list of connected squares of the same type in the same row.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      *
      * @return a int[] containing "connected" squares of the same type in the same row
      */
 
     private int[][] findConnectedRow(int x, int y) {
         int type = getType(x, y);
 
         int[][] connectedChips = new int[2][2];
 
         int[] row = getRow(y);
         int length = row.length;
         int location = x;
 
         for (int i = 1; location - i >= 0; i++) {
             if (row[location - i] == type) {
                 connectedChips[0][0] = x - i;
                 connectedChips[0][1] = y;
                 break;
             }
             if (row[location - i] != type && checkPiece(row[location - i])) {
                 break;
             }
         }
 
         for (int i = 1; i + location < length; i++) {
             if (row[i + location] == type) {
                 connectedChips[1][0] = x + i;
                 connectedChips[1][1] = y;
                 break;
             }
             if (row[location + i] != type && checkPiece(row[location + i])) {
                 break;
             }
         }
         return connectedChips;
     }
 
     /**
      * findConnectedColumn() takes 2 parameters, the coordinates, and returns a list of connected squares of the same type in the same column.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      *
      * @return a int[] containing "connected" squares of the same type in the same column.
      */
 
     private int[][] findConnectedColumn(int x, int y) {
         int type = getType(x, y);
 
         int[][] connectedChips = new int[2][2];
 
         int[] column = getColumn(x);
         int length = column.length;
         int location = y;
 
         for (int i = 1; location - i >= 0; i++) {
             if (column[location - i] == type) {
                 connectedChips[0][0] = x;
                 connectedChips[0][1] = y - i;
                 break;
             }
             if (column[location - i] != type && checkPiece(column[location - i])) {
                 break;
             }
         }
 
         for (int i = 1; i + location < length; i++) {
             if (column[i + location] == type) {
                 connectedChips[1][0] = x;
                 connectedChips[1][1] = y + i;
                 break;
             }
             if (column[location + i] != type && checkPiece(column[location + i])) {
                 break;
             }
         }
         return connectedChips;
     }
 
 
     /**
      * findConnectingChips() takes 2 parameters, the coordinates, and returns a list of square ints 
      * that the square is "connected" to.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      *
      * @return a int[] containing chips that is connected to given square
      */
 
     public int[] findConnectingChips(int x, int y) {
         int temp[] = new int[2];
         return temp;
     }
 
     /**
      * addPiece(), takes three parameters, and changes the type of the
      * indicated square on "this" Gameboard at the coordinates (x,y) to
      * the specified type.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      * @param type the new type of the square
      */
 
     public void addPiece(int x, int y, int type) throws AgainstRulesException {
         /*
         System.out.println("checkDimensions is " + checkDimensions(x,y) + " at (" + x + ", " + y + ")");
         System.out.println("checkNeighbor is " + checkNeighbors(x,y,type)  + " at (" + x + ", " + y + ") with " + type);
         System.out.println("checkSquare is " + checkSquare(x,y,type) + " at (" + x + ", " + y + ") with " + type);
         System.out.println("check Dimensions is " + checkPiece(type));
         System.out.println("------------------------------------------------------------------");
         */
         if ((checkDimensions(x,y) && checkPiece(type)) && (checkSquare(x,y,type) && checkNeighbors(x,y,type))) {
             setType(x, y, type);
             if (type == BLACK) {
                 blackCount--;
             }
             if (type == WHITE) {
                 whiteCount--;
             }
         } else {
             //throw new AgainstRulesException("attempt to add " + type + " fails at  (" + x + ", " + y + ")");
         }
     }
 
     /**
      * checkDimensions(), takes two parameters, the x-coordinate, and y-coordinate, and verifies
      * that they are within the dimensions of this.Gameboard.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      *
      * @return true if the coordinate satisfies the dimensions of the board, false otherwise.
      */
 
     private boolean checkDimensions(int x, int y) {
         if ((x < this.width && x >= 0) && (y < this.height && y >= 0)) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * checkSquare(), takes three parameters, the x-coordinate,  y-coordinate, and a piece, and verifies
      * that the square is not paired with a coordinate where the piece is not allowed. If the type is INVALID, automatically
      * returns false. An INVALID is not allowed anywhere! If the coordinates is at an INVALID spot, also returns false.
      * 
      * (RULE #1 and #2)
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      * @param type the piece to be inspected at (x,y)
      *
      * @return true if the piece can legitimately be placed on (x,y), false otherwise.
      */
 
     private boolean checkSquare(int x, int y, int type) {
         if (isEmpty(x,y)) { //if (x,y) is valid, and the type of square is valid
             if (type == BLACK) { //a BLACK can not be placed on the left/right edges
                 if (x != 0 && x != (this.width - 1)) { //if x-coordinate is not 0 and its not width - 1
                     return true;
                 } else {
                     return false;
                 }
             }
             if (type == WHITE) { //a WHITE can not be placed on the top/bottom edges
                 if (y != 0 && y != (this.height - 1)) { //if x-coordinate is not 0 and its not width - 1
                     return true; 
                 } else {
                     return false;
                 }
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     /**
      * pieceCheck(), takes one parameter, a type of a piece, returns whether or not the type is {BLACK, WHITE}
      * 
      * (RULE #1 and #2)
      * @param type the piece to be inspected
      *
      * @return true if the piece is WHITE or BLACK, false otherwise.
      */
 
     private boolean checkPiece(int type) {
         if (type == WHITE || type == BLACK) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * countNeighbor() takes in three parameters, an x-coordinate and y-coordinate, and a type and return the number of neighbors
      * around it, excluding itself.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      * @param type the type of the piece to be inspected
      *
      * @return the number of neighbors surrounding the specified piece, excluding itself,
      */
 
     private int countNeighbors(int x, int y, int type) {
         int count = 0;
         int[][] neighbors = getNeighbors(x, y);
         for (int j = 0; j < 3; j++) {
             for (int i = 0; i < 3; i++) {
                 if (neighbors[i][j] == type) {
                     //System.out.print(i + ", " + j + " --> " + count);
                     count++;
                 }
             }
         }
         //System.out.println("count is " + count + " at (" + x + ", " + y + ") with " + type);
         if (neighbors[1][1] == type) {
             count--;
         }
         return count;
     }
 
     /**
      * locateNeighbor() takes in three parameters, an x-coordinate, y-coordinate on the board and a type, adjusts 
      * the coordinates of 2-D array returned from getNeighbor() to overlay the coordinates of the piece on this.Gameboard. Only
      * returns the first neighbor it finds.
      *
      * @param x1 the x-coordinate of the square on the gameboard
      * @param y1 the y-coordinate of the square on the gameboard
      * @param type the type of the square we are inspecting
      *
      * @return an array of length 2, containing the coordinates of first neighbor on the this.Gameboard. The first index is the x-coordinate,
      * and the second index is the y-coordinates.
      */
 
     private int[] locateNeighbors(int x, int y, int type) {
         int[] location = new int[2];
         int[][] neighbors = getNeighbors(x, y);
         /*
         for (int j = 0; j < 3; j++) {
             for (int i = 0; i < 3; i++) {
                 System.out.print(neighbors[i][j] + "|");
             }
             System.out.println();
         }
         */
 
         for (int j = 0; j < 3; j++) {
             for (int i = 0; i < 3; i++) {
                 if (neighbors[i][j] == type) {
                     location[0] = x + i - 1; //true location
                     location[1] = y + j - 1; //true location
                 }
             }
         }
 
         //System.out.println(location[0] + ", " + location[1]);
         return location;
     }
 
 
     /**
      * checkNeighbors() takes in three parameters, an x-coordinate and y-coordinate, and a piece and checks whether placing the piece there
      * would violate the rule that a player may not have more than two chips in a connected group, whether connected
      * orthogonally or diagonally.
      * (RULE #4)
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      * @param type the type of the square
      *
      * @return true if the piece is can be placed according to the rules, false otherwise.
      */
 
     private boolean checkNeighbors(int x, int y, int type) {
         if (countNeighbors(x,y,type) == 0) {
             return true;
         }
         //System.out.println("countNeighbors " + countNeighbors(x,y,type) + " at (" + x + ", " + y + ") with " + type);
         if (countNeighbors(x,y,type) == 1) {
             int[] location = locateNeighbors(x,y,type);
             if (countNeighbors(location[0], location[1], type) <= 0) {
                 return true;
             }
         }
         return false;
     }
 
 
     /**
      * removePiece(), takes two parameters, and changes the type of the 
      * indicated square on "this" Gameboard at the coordinates (x,y) to EMPTY.
      *
      * @param x the x-coordinate of the square
      * @param y the y-coordinate of the square
      */
 
     private void removePiece(int x, int y) {
         if (checkPiece(getType(x,y)) && checkDimensions(x,y)) {
             this.setType(x, y, EMPTY);
         }   
     }
 
     /**
      * movePiece() "moves" a piece from one coordinate to another.
      * More specifically, it takes parameters (x1, y1, x2, y2) and
      * moves a piece on "this" Gameboard from (x1, y1) to (x2, y2).
      *
      *
      * @param x1 the x-coordinate of the square containing the piece
      * @param y1 the y-coordinate of the square containing the piece
      * @param x2 the x-coordinate of destination
      * @param y2 the y-coordinate of destination
      */
 
     public void movePieces(int x1, int y1, int x2, int y2) throws AgainstRulesException {
         try {
             addPiece(x2, y2, getType(x1,y1));
         }
         catch (AgainstRulesException e) {
             throw new AgainstRulesException("attempt to move " + getType(x1, y1) + " from  (" + x1 + ", " + y1 + ") fails.");
         }
         removePiece(x1, y1);
     }
 
     /**
      * isValidMove() takes one parameter, and checks if "move" 
      * can be legally made based on the game rules.
      * 
      * @param m is the move in question.
      *
      * @return true if the move can be legally made, false otherwise.
      */
 
     public boolean isvalidMove(Move m) {
         return true;
     }
     
     /**
      * listMoves() takes one parameter, the type of the current player, and 
      * returns an array of all possible moves.
      *
      * @param player is the type of the player in consideration
      *
      * @return a Move[] containing all the possible moves possible for "player"
      * "this" Gameboard.
      */
 
     public Move[] listMoves(int player) {
         Move[] list = new Move[1];
         return list;
     }
 
     /**
      * containsNetwork() takes one parameter, the type of the current "player", and
      * returns whether or not a network could be established with the "player"'s
      * pieces on "this" Gameboard
      *
      * @param player is the type of the player in consideration
      *
      * @return true if the current set of pieces contains a Network, false otherwise
      */
 
     public boolean containsNetwork(int player) {
         return true;
     }
 
     /**
      * isEmptyBoard() takes no parameters, and verifies that there are no pieces on the board. For internal testing only.
      *
      * @return empty is true if the board is empty, false otherwise.
      */
 
     private boolean isEmptyBoard() {
         boolean empty = true;
         for (int i = 0; i < this.width; i++) {
             for (int j = 0; j < this.height; j++) {
                 if (isBlack(i,j) || isWhite(i,j)) {
                     empty = false;
                 }
             }
         }
         return empty;
     }
 
     /**
      * toString() returns a String representation of the board.
      */
     public String toString() {
         String current = "      0    1    2    3    4    5    6    7  " + "\n" + "   -----------------------------------------";
         for(int j = 0; j < this.height; j++) {
             String[] printColumn = new String[this.width];
             for(int i = 0; i < this.width; i++) {
                 int typeAt = getType(i,j);
                 //System.out.println(typeAt + " at coordinates (" + i + ", " + j + ")");
                 switch(typeAt) {
                     case INVALID:
                         printColumn[i] = "XXXX";
                         break;
                     case EMPTY:
                         printColumn[i] = "    ";
                         break;
                     case WHITE:
                         printColumn[i] = " WW ";
                         break;
                     case BLACK:
                         printColumn[i] = " BB ";
                         break;
                     default:
                         printColumn[i] = "EROR";
                 }
             }
             current += "\n" + j + "  |";
             for(int k = 0; k < printColumn.length; k++) {
                 current += printColumn[k] + '|';
             }
             current += "\n" + "   -----------------------------------------";
         }
         return current;
     }
 
     public static void main(String args[]) {
         Gameboard testGame = new Gameboard();
         //System.out.println(testGame);
 
         try {
 
             //verifying board dimensions
             assert testGame.width == 8: "ERROR (Y): width of gameboard incorrect";
             assert testGame.height == 8: "ERROR (Y): height of gameboard incorrect";
 
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
 
             //verifying corner squares are indeed INVALID
             assert testGame.isValid(0,0) == false: "ERROR (S): invalid squares are valid";
             assert testGame.isValid(0,7) == false: "ERROR (S): invalid squares are valid";
             assert testGame.isValid(7,0) == false: "ERROR (S): invalid squares are valid";
             assert testGame.isValid(7,7) == false: "ERROR (S): invalid squares are valid";
 
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
 
             //verifying pieces can not be added to INVALID squares
             System.out.println("WARNING: AgainstRulesException in addPiece() is commented out");
             testGame.addPiece(0,0,BLACK);
             testGame.addPiece(0,7,BLACK);
             testGame.addPiece(7,0,BLACK);
             testGame.addPiece(7,7,BLACK);
             testGame.addPiece(0,0,WHITE);
             testGame.addPiece(0,7,WHITE);
             testGame.addPiece(7,0,WHITE);
             testGame.addPiece(7,7,WHITE);
 
             assert testGame.isValid(0,0) == false: "ERROR (Y): a piece has been added to an invalid square";
             assert testGame.isValid(0,7) == false: "ERROR (Y): a piece has been added to an invalid square";
             assert testGame.isValid(7,0) == false: "ERROR (Y): a piece has been added to an invalid square";
             assert testGame.isValid(7,7) == false: "ERROR (Y): a piece has been added to an invalid square";
             assert testGame.isEmptyBoard() == true: "ERROR (Y): the board should be empty";
 
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
 
             //verifying pieces of opposing color can not be added to a player's home rows
             //WHITE's side
             testGame.addPiece(0,1,BLACK);
             testGame.addPiece(0,2,BLACK);
             testGame.addPiece(0,3,BLACK);
             testGame.addPiece(0,4,BLACK);
             testGame.addPiece(0,5,BLACK);
             testGame.addPiece(0,6,BLACK);
 
             testGame.addPiece(7,1,BLACK);
             testGame.addPiece(7,2,BLACK);
             testGame.addPiece(7,2,BLACK);
             testGame.addPiece(7,3,BLACK);
             testGame.addPiece(7,4,BLACK);
             testGame.addPiece(7,5,BLACK);
             testGame.addPiece(7,6,BLACK);
             assert testGame.isEmptyBoard() == true: "ERROR (Y): the board should be empty";
 
             //BLACK's side
             testGame.addPiece(1,0,WHITE);
             testGame.addPiece(2,0,WHITE);
             testGame.addPiece(3,0,WHITE);
             testGame.addPiece(4,0,WHITE);
             testGame.addPiece(5,0,WHITE);
             testGame.addPiece(6,0,WHITE);
 
             testGame.addPiece(1,7,WHITE);
             testGame.addPiece(2,7,WHITE);
             testGame.addPiece(3,7,WHITE);
             testGame.addPiece(4,7,WHITE);
             testGame.addPiece(5,7,WHITE);
             testGame.addPiece(6,7,WHITE);
             assert testGame.isEmptyBoard() == true: "ERROR (Y): the board should be empty";
 
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
 
             //verifying addPiece() without neighbor conflicts
             testGame.addPiece(6,0,BLACK);
             testGame.addPiece(6,5,BLACK);
             testGame.addPiece(5,5,BLACK);
             testGame.addPiece(3,3,BLACK);
             testGame.addPiece(3,5,BLACK);
             testGame.addPiece(5,7,BLACK);
 
             assert testGame.getType(6,0) == BLACK: "ERROR (Y): square should be BLACK";
             assert testGame.getType(6,5) == BLACK: "ERROR (Y): square should be BLACK";
             assert testGame.getType(5,5) == BLACK: "ERROR (Y): square should be BLACK";
             assert testGame.getType(3,3) == BLACK: "ERROR (Y): square should be BLACK";
             assert testGame.getType(3,5) == BLACK: "ERROR (Y): square should be BLACK";
             assert testGame.getType(5,7) == BLACK: "ERROR (Y): square should be BLACK";
 
             assert testGame.blackCount == 4: "ERROR: blackCount is incorrect";
             assert testGame.isEmptyBoard() == false: "ERROR (Y): the board should have pieces on it";
 
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
 
             //verifying addPiece() with neighbor conflicts
             //BLACK piece initially at (6,0);
 
             //Test 1: exhaustive attempt to cause neighbor conflicts
 
             //Setup
             testGame.addPiece(6,1,BLACK);
             assert testGame.getType(6,1) == BLACK: "ERROR (Y): square should be BLACK";
             testGame.addPiece(1,7,BLACK);
             assert testGame.getType(1,7) == BLACK: "ERROR (Y): square should be BLACK";
             testGame.addPiece(6,7,BLACK);
             assert testGame.getType(6,7) == BLACK: "ERROR (Y): square should be BLACK";
             testGame.addPiece(2,1, BLACK);
             assert testGame.getType(2,1) == BLACK: "ERROR (Y): square should be BLACK";
             testGame.addPiece(1,0, BLACK);
             assert testGame.getType(1,0) == BLACK: "ERROR (Y): square should be BLACK";
 
             testGame.addPiece(4,2, BLACK);
             assert testGame.getType(4,2) == BLACK: "ERROR (Y): square should be BLACK";
             testGame.addPiece(3,7, BLACK);
             assert testGame.getType(3,7) == BLACK: "ERROR (Y): square should be BLACK";
 
             System.out.println(testGame);
 
             //Attempt to cause errors
             testGame.addPiece(2,0, BLACK);
             assert testGame.getType(2,0) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(5,0, BLACK);
             assert testGame.getType(5,0) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(1,1, BLACK);
             assert testGame.getType(1,1) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(3,1, BLACK);
             assert testGame.getType(3,1) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(5,1, BLACK);
             assert testGame.getType(5,1) == EMPTY: "ERROR (Y): square should be EMPTY";
 
             testGame.addPiece(2,2, BLACK);
             assert testGame.getType(2,2) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(3,2, BLACK);
             assert testGame.getType(3,2) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(5,2, BLACK);
             assert testGame.getType(5,2) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(6,2, BLACK);
             assert testGame.getType(6,2) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(4,3, BLACK);
             assert testGame.getType(4,3) == EMPTY: "ERROR (Y): square should be EMPTY";
 
             testGame.addPiece(2,4, BLACK);
             assert testGame.getType(2,4) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(3,4, BLACK);
             assert testGame.getType(3,4) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(4,4, BLACK);
             assert testGame.getType(4,4) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(5,4, BLACK);
             assert testGame.getType(5,4) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(6,4, BLACK);
             assert testGame.getType(6,4) == EMPTY: "ERROR (Y): square should be EMPTY";
 
             testGame.addPiece(4,5, BLACK);
             assert testGame.getType(4,5) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(2,6, BLACK);
             assert testGame.getType(2,6) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(3,6, BLACK);
             assert testGame.getType(3,6) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(4,6, BLACK);
             assert testGame.getType(4,6) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(5,6, BLACK);
             assert testGame.getType(5,6) == EMPTY: "ERROR (Y): square should be EMPTY";
 
             testGame.addPiece(6,6, BLACK);
             assert testGame.getType(6,6) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(2,7, BLACK);
             assert testGame.getType(2,7) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(4,7, BLACK);
             assert testGame.getType(4,7) == EMPTY: "ERROR (Y): square should be EMPTY";
 
             //Test 2: randomized attempt to insert WHITE around BLACK pieces
 
             testGame.addPiece(3,1, WHITE);
             assert testGame.getType(3,1) == WHITE: "ERROR (Y): square should be WHITE";
             testGame.addPiece(3,2, WHITE);
             assert testGame.getType(3,2) == WHITE: "ERROR (Y): square should be WHITE";
             testGame.addPiece(4,5, WHITE);
             assert testGame.getType(4,5) == WHITE: "ERROR (Y): square should be WHITE";
             testGame.addPiece(5,6, WHITE);
             assert testGame.getType(5,6) == WHITE: "ERROR (Y): square should be WHITE";
             testGame.addPiece(7,1, WHITE);
             assert testGame.getType(7,1) == WHITE: "ERROR (Y): square should be WHITE";
 
             testGame.addPiece(6,2, WHITE);
             assert testGame.getType(6,2) == WHITE: "ERROR (Y): square should be WHITE";
             testGame.addPiece(7,5, WHITE);
             assert testGame.getType(7,5) == WHITE: "ERROR (Y): square should be WHITE";
             testGame.addPiece(7,6, WHITE);
             assert testGame.getType(7,6) == WHITE: "ERROR (Y): square should be WHITE";
             testGame.addPiece(0,2, WHITE);
             assert testGame.getType(0,2) == WHITE: "ERROR (Y): square should be WHITE";
             testGame.addPiece(0,6, WHITE);
             assert testGame.getType(0,6) == WHITE: "ERROR (Y): square should be WHITE";
 
             testGame.addPiece(1,5, WHITE);
             assert testGame.getType(1,5) == WHITE: "ERROR (Y): square should be WHITE";
 
             //Attempt to cause errors
             testGame.addPiece(2,2, WHITE);
             assert testGame.getType(2,2) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(7,2, WHITE);
             assert testGame.getType(7,2) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(6,6, WHITE);
             assert testGame.getType(6,6) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(0,5, WHITE);
             assert testGame.getType(0,5) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(1,6, WHITE);
             assert testGame.getType(1,6) == EMPTY: "ERROR (Y): square should be EMPTY";
 
             testGame.addPiece(4,3, WHITE);
             assert testGame.getType(4,3) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(2,6, WHITE);
             assert testGame.getType(2,6) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(5,2, WHITE);
             assert testGame.getType(5,2) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(5,1, WHITE);
             assert testGame.getType(5,1) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(3,6, WHITE);
             assert testGame.getType(3,6) == EMPTY: "ERROR (Y): square should be EMPTY";
 
             System.out.println(testGame);
 
             //---------------------------------------------------------------------//
 
             //attempting to add an INVALID using addPiece()
             testGame.addPiece(1,1, INVALID);
             assert testGame.getType(1,1) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(3,0, INVALID);
             assert testGame.getType(3,0) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(0,5, INVALID);
             assert testGame.getType(0,5) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(0,1, INVALID);
             assert testGame.getType(0,1) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(6,6, INVALID);
             assert testGame.getType(6,6) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.addPiece(4,1, INVALID);
             assert testGame.getType(4,1) == EMPTY: "ERROR (Y): square should be EMPTY";
 
             //attempting to replace piece with an INVALID using addPiece()
             testGame.addPiece(1,0, INVALID);
             assert testGame.getType(1,0) == BLACK: "ERROR (Y): square should be BLACK";
             testGame.addPiece(3,1, INVALID);
             assert testGame.getType(3,1) == WHITE: "ERROR (Y): square should be WHITE";
             testGame.addPiece(6,2, INVALID);
             assert testGame.getType(6,2) == WHITE: "ERROR (Y): square should be WHITE";
             testGame.addPiece(6,5, INVALID);
             assert testGame.getType(6,5) == BLACK: "ERROR (Y): square should be BLACK";
             testGame.addPiece(0,6, INVALID);
             assert testGame.getType(0,6) == WHITE: "ERROR (Y): square should be WHITE";
             testGame.addPiece(3,3, INVALID);
             assert testGame.getType(3,3) == BLACK: "ERROR (Y): square should be BLACK";
 
             //attempting to replace piece with an EMPTY using addPiece()
             testGame.addPiece(1,0, EMPTY);
             assert testGame.getType(1,0) == BLACK: "ERROR (Y): square should be BLACK";
             testGame.addPiece(3,1, EMPTY);
             assert testGame.getType(3,1) == WHITE: "ERROR (Y): square should be WHITE";
             testGame.addPiece(6,2, EMPTY);
             assert testGame.getType(6,2) == WHITE: "ERROR (Y): square should be WHITE";
             testGame.addPiece(6,5, EMPTY);
             assert testGame.getType(6,5) == BLACK: "ERROR (Y): square should be BLACK";
             testGame.addPiece(0,6, EMPTY);
             assert testGame.getType(0,6) == WHITE: "ERROR (Y): square should be WHITE";
             testGame.addPiece(3,3, EMPTY);
             assert testGame.getType(3,3) == BLACK: "ERROR (Y): square should be BLACK";
 
             //attempting to add piece to out-of-bound coordinates using addPiece()
             testGame.addPiece(100,100, BLACK);
             testGame.addPiece(-20,45, WHITE);
 
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
 
             //checking removePiece()
 
             testGame.removePiece(6,0);
             assert testGame.getType(6,0) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(6,5);
             assert testGame.getType(6,5) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(5,5);
             assert testGame.getType(5,5) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(3,3);
             assert testGame.getType(3,3) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(3,5);
             assert testGame.getType(3,5) == EMPTY: "ERROR (Y): square should be EMPTY";
 
             testGame.removePiece(5,7);
             assert testGame.getType(5,7) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(6,1);
             assert testGame.getType(6,1) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(1,7);
             assert testGame.getType(1,7) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(6,7);
             assert testGame.getType(6,7) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(2,1);
             assert testGame.getType(2,1) == EMPTY: "ERROR (Y): square should be EMPTY";
 
             testGame.removePiece(1,0);
             assert testGame.getType(1,0) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(4,2);
             assert testGame.getType(4,2) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(3,7);
             assert testGame.getType(3,7) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(3,1);
             assert testGame.getType(3,1) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(3,2);
             assert testGame.getType(3,2) == EMPTY: "ERROR (Y): square should be EMPTY";
 
             testGame.removePiece(4,5);
             assert testGame.getType(4,5) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(5,6);
             assert testGame.getType(5,6) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(7,1);
             assert testGame.getType(7,1) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(6,2);
             assert testGame.getType(6,2) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(7,5);
             assert testGame.getType(7,5) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(7,6);
 
             assert testGame.getType(7,6) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(0,2);
             assert testGame.getType(0,2) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(0,6);
             assert testGame.getType(0,6) == EMPTY: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(1,5);
             assert testGame.getType(1,5) == EMPTY: "ERROR (Y): square should be EMPTY";
 
             //attempting to remove INVALID squares
             testGame.removePiece(0,0);
             assert testGame.getType(0,0) == INVALID: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(0,7);
             assert testGame.getType(0,7) == INVALID: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(7,0);
             assert testGame.getType(7,0) == INVALID: "ERROR (Y): square should be EMPTY";
             testGame.removePiece(7,7);
             assert testGame.getType(7,7) == INVALID: "ERROR (Y): square should be EMPTY";
 
             System.out.println(testGame);
 
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
 
             //Setup 
             testGame.addPiece(3,1, WHITE);
             testGame.addPiece(3,2, WHITE);
             testGame.addPiece(4,5, WHITE);
             testGame.addPiece(5,6, WHITE);
             testGame.addPiece(7,1, WHITE);
 
             testGame.addPiece(6,2, WHITE);
             testGame.addPiece(7,5, WHITE);
             testGame.addPiece(7,6, WHITE);
             testGame.addPiece(0,2, WHITE);
             testGame.addPiece(0,6, WHITE);
 
             testGame.addPiece(1,5, WHITE);
             testGame.addPiece(6,1, BLACK);
             testGame.addPiece(1,7, BLACK);
             testGame.addPiece(6,7, BLACK);
             testGame.addPiece(2,1, BLACK);
 
             testGame.addPiece(2,3, BLACK);
             testGame.addPiece(2,5, BLACK);
             testGame.addPiece(2,7, BLACK);
             testGame.addPiece(1,0, BLACK);
             testGame.addPiece(4,2, BLACK);
 
             testGame.addPiece(3,7, BLACK);
             testGame.addPiece(6,0, BLACK);
             testGame.addPiece(6,5, BLACK);
             testGame.addPiece(5,5, BLACK);
             testGame.addPiece(3,3, BLACK);
 
             testGame.addPiece(3,5, BLACK);
             testGame.addPiece(5,7, BLACK);
 
             System.out.println(testGame);
 
             //testing Connected Columns
             int[][] c02 = {{0,0}, {0,6}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(0,2), c02): "Connected Column Error";
             int[][] c06 = {{0,2}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(0,6), c06): "Connected Column Error";
             int[][] c10 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(1,0), c10): "Connected Column Error";
             int[][] c15 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(1,5), c15): "Connected Column Error";
             int[][] c17 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(1,7), c17): "Connected Column Error";
             
             int[][] c21 = {{0,0}, {2,3}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(2,1), c21): "Connected Column Error";
             int[][] c23 = {{2,1}, {2,5}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(2,3), c23): "Connected Column Error";
             int[][] c25 = {{2,3}, {2,7}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(2,5), c25): "Connected Column Error";
             int[][] c27 = {{2,5}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(2,7), c27): "Connected Column Error";
             int[][] c31 = {{0,0}, {3,2}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(3,1), c31): "Connected Column Error";
             int[][] c32 = {{3,1}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(3,2), c32): "Connected Column Error";
 
             int[][] c35 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(3,5), c35): "Connected Column Error";
             int[][] c42 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(4,2), c42): "Connected Column Error";
             int[][] c45 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(4,5), c45): "Connected Column Error";
             int[][] c55 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(5,5), c55): "Connected Column Error";
             int[][] c56 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(5,6), c56): "Connected Column Error";
 
             int[][] c57 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(5,7), c57): "Connected Column Error";
             int[][] c60 = {{0,0}, {6,1}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(6,0), c60): "Connected Column Error";
             int[][] c61 = {{6,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(6,1), c61): "Connected Column Error";
             int[][] c62 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(6,2), c62): "Connected Column Error";
             int[][] c65 = {{0,0}, {6,7}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(6,5), c65): "Connected Column Error";
 
             int[][] c67 = {{6,5}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(6,7), c67): "Connected Column Error";
             int[][] c71 = {{0,0}, {7,5}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(7,1), c71): "Connected Column Error";
             int[][] c75 = {{7,1}, {7,6}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(7,5), c75): "Connected Column Error";
             int[][] c76 = {{7,5}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedColumn(7,6), c76): "Connected Column Error";
 
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
 
             //testing Connected Rows
             int[][] r02 = {{0,0}, {3,2}};
             assert Arrays.deepEquals(testGame.findConnectedRow(0,2), r02): "Connected Row Error";
             int[][] r06 = {{0,0}, {5,6}};
             assert Arrays.deepEquals(testGame.findConnectedRow(0,6), r06): "Connected Row Error";
             int[][] r10 = {{0,0}, {6,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(1,0), r10): "Connected Row Error";
             int[][] r15 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(1,5), r15): "Connected Row Error";
             int[][] r17 = {{0,0}, {2,7}};
             assert Arrays.deepEquals(testGame.findConnectedRow(1,7), r17): "Connected Row Error";
             
             int[][] r21 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(2,1), r21): "Connected Row Error";
             int[][] r23 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(2,3), r23): "Connected Row Error";
             int[][] r25 = {{0,0}, {3,5}};
             assert Arrays.deepEquals(testGame.findConnectedRow(2,3), r23): "Connected Row Error";
             int[][] r27 = {{1,7}, {5,7}};
             assert Arrays.deepEquals(testGame.findConnectedRow(2,7), r27): "Connected Row Error";
             int[][] r31 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(3,1), r31): "Connected Row Error";
             int[][] r32 = {{0,2}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(3,2), r32): "Connected Row Error";
 
             int[][] r35 = {{2,5}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(3,5), r35): "Connected Row Error";
             int[][] r42 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(4,2), r42): "Connected Row Error";
             int[][] r45 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(4,5), r45): "Connected Row Error";
             int[][] r55 = {{0,0}, {6,5}};
             assert Arrays.deepEquals(testGame.findConnectedRow(5,5), r55): "Connected Row Error";
             int[][] r56 = {{0,6}, {7,6}};
             assert Arrays.deepEquals(testGame.findConnectedRow(5,6), r56): "Connected Row Error";
 
             int[][] r57 = {{2,7}, {6,7}};
             assert Arrays.deepEquals(testGame.findConnectedRow(5,7), r57): "Connected Row Error";
             int[][] r60 = {{1,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(6,0), r60): "Connected Row Error";
             int[][] r61 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(6,1), r61): "Connected Row Error";
             int[][] r62 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(6,2), r62): "Connected Row Error";
             int[][] r65 = {{5,5}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(6,5), r65): "Connected Row Error";
 
             int[][] r67 = {{5,7}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(6,7), r67): "Connected Row Error";
             int[][] r71 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(7,1), r71): "Connected Row Error";
             int[][] r75 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(7,5), r75): "Connected Row Error";
             int[][] r76 = {{5,6}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRow(7,6), r76): "Connected Row Error";
 
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
             /*
             //testing Connected LDiagonal
             int[][] ld02 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(0,2), ld02): "Connected LDiagonal Error";
             int[][] ld06 = {{0,0}, {1,5}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(0,6), ld06): "Connected LDiagonal Error";
             int[][] ld10 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(1,0), ld10): "Connected LDiagonal Error";
             int[][] ld15 = {{0,6}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(1,5), ld15): "Connected LDiagonal Error";
             int[][] ld17 = {{0,0}, {3,5}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(1,7), ld17): "Connected LDiagonal Error";
             
             int[][] ld21 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(2,1), ld21): "Connected LDiagonal Error";
             int[][] ld23 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(2,3), ld23): "Connected LDiagonal Error";
             int[][] ld25 = {{6,0}, {3,5}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(2,3), ld23): "Connected LDiagonal Error";
             int[][] ld27 = {{1,7}, {5,7}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(2,7), ld27): "Connected LDiagonal Error";
             int[][] ld31 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(3,1), ld31): "Connected LDiagonal Error";
             int[][] ld32 = {{0,2}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(3,2), ld32): "Connected LDiagonal Error";
 
             int[][] ld35 = {{2,5}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(3,5), ld35): "Connected LDiagonal Error";
             int[][] ld42 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(4,2), ld42): "Connected LDiagonal Error";
             int[][] ld45 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(4,5), ld45): "Connected LDiagonal Error";
             int[][] ld55 = {{0,0}, {6,5}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(5,5), ld55): "Connected LDiagonal Error";
             int[][] ld56 = {{0,6}, {7,6}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(5,6), ld56): "Connected LDiagonal Error";
 
             int[][] ld57 = {{2,7}, {6,7}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(5,7), ld57): "Connected LDiagonal Error";
             int[][] ld60 = {{1,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(6,0), ld60): "Connected LDiagonal Error";
             int[][] ld61 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(6,1), ld61): "Connected LDiagonal Error";
             int[][] ld62 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(6,2), ld62): "Connected LDiagonal Error";
             int[][] ld65 = {{5,5}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(6,5), ld65): "Connected LDiagonal Error";
 
             int[][] ld67 = {{5,7}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(6,7), ld67): "Connected LDiagonal Error";
             int[][] ld71 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(7,1), ld71): "Connected LDiagonal Error";
             int[][] ld75 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(7,5), ld75): "Connected LDiagonal Error";
             int[][] ld76 = {{5,6}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedLDiagonal(7,6), ld76): "Connected LDiagonal Error";
             */
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
             //---------------------------------------------------------------------//
 
             //testing Connected RDiagonal
 
             //Adjustments to cover all cases
             testGame.addPiece(5,3, BLACK);
 
             int[][] rd02 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(0,2), rd02): "Connected RDiagonal Error";
            int[][] rd06 = {{0,0}, {1,5}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(0,6), rd06): "Connected RDiagonal Error";
             int[][] rd10 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(1,0), rd10): "Connected RDiagonal Error";
            int[][] rd15 = {{0,6}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(1,5), rd15): "Connected RDiagonal Error";
            int[][] rd17 = {{0,0}, {3,5}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(1,7), rd17): "Connected RDiagonal Error";
             
             int[][] rd21 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(2,1), rd21): "Connected RDiagonal Error";
             int[][] rd23 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(2,3), rd23): "Connected RDiagonal Error";
             int[][] rd25 = {{0,0}, {6,1}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(2,3), rd23): "Connected RDiagonal Error";
             int[][] rd27 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(2,7), rd27): "Connected RDiagonal Error";
             int[][] rd31 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(3,1), rd31): "Connected RDiagonal Error";
             int[][] rd32 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(3,2), rd32): "Connected RDiagonal Error";
 
             int[][] rd35 = {{1,7}, {5,3}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(3,5), rd35): "Connected RDiagonal Error";
             int[][] rd42 = {{0,0}, {6,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(4,2), rd42): "Connected RDiagonal Error";
             int[][] rd45 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(4,5), rd45): "Connected RDiagonal Error";
             int[][] rd53 = {{3,5}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(5,3), rd53): "Connected RDiagonal Error";
             int[][] rd55 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(5,5), rd55): "Connected RDiagonal Error";
             int[][] rd56 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(5,6), rd56): "Connected RDiagonal Error";
 
             int[][] rd57 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(5,7), rd57): "Connected RDiagonal Error";
             int[][] rd60 = {{4,2}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(6,0), rd60): "Connected RDiagonal Error";
             int[][] rd61 = {{0,0}, {2,5}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(6,1), rd61): "Connected RDiagonal Error";
             int[][] rd62 = {{0,0}, {7,1}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(6,2), rd62): "Connected RDiagonal Error";
             int[][] rd65 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(6,5), rd65): "Connected RDiagonal Error";
 
             int[][] rd67 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(6,7), rd67): "Connected RDiagonal Error";
             int[][] rd71 = {{6,2}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(7,1), rd71): "Connected RDiagonal Error";
             int[][] rd75 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(7,5), rd75): "Connected RDiagonal Error";
             int[][] rd76 = {{0,0}, {0,0}};
             assert Arrays.deepEquals(testGame.findConnectedRDiagonal(7,6), rd76): "Connected RDiagonal Error";
             
             /*
             Gameboard sanchitGame = new Gameboard();
             sanchitGame.addPiece(0,1,WHITE);
             sanchitGame.addPiece(0,2,WHITE);
             sanchitGame.addPiece(0,3,WHITE);
             sanchitGame.addPiece(0,4,WHITE);
             sanchitGame.addPiece(0,5,WHITE);
             sanchitGame.addPiece(0,6,WHITE);
             
             sanchitGame.addPiece(1,1,WHITE);
             sanchitGame.addPiece(2,2,WHITE);
             sanchitGame.addPiece(3,3,WHITE);
             sanchitGame.addPiece(4,4,WHITE);
             sanchitGame.addPiece(5,5,WHITE);
             sanchitGame.addPiece(6,6,WHITE);
 
             sanchitGame.addPiece(7,1,WHITE);
             sanchitGame.addPiece(7,2,WHITE);
             sanchitGame.addPiece(7,3,WHITE);
             sanchitGame.addPiece(7,4,WHITE);
             sanchitGame.addPiece(7,5,WHITE);
             sanchitGame.addPiece(7,6,WHITE);
             sanchitGame.addPiece(5,7,WHITE);
 
             System.out.println(sanchitGame);
             
             assert sanchitGame.getDiagonalLength(0,0,-1) == 8: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(0,1,-1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(0,2,-1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(0,3,-1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(0,4,-1) == 4: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(0,5,-1) == 3: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(0,6,-1) == 2: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(0,7,-1) == 1: "ERROR (S): getDiagonalLength is wrong";
 
             assert sanchitGame.getDiagonalLength(1,0,-1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(1,1,-1) == 8: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(1,2,-1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(1,3,-1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(1,4,-1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(1,5,-1) == 4: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(1,6,-1) == 3: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(1,7,-1) == 2: "ERROR (S): getDiagonalLength is wrong";
 
             assert sanchitGame.getDiagonalLength(2,0,-1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(2,1,-1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(2,2,-1) == 8: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(2,3,-1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(2,4,-1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(2,5,-1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(2,6,-1) == 4: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(2,7,-1) == 3: "ERROR (S): getDiagonalLength is wrong";
 
             assert sanchitGame.getDiagonalLength(3,0,-1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(3,1,-1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(3,2,-1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(3,3,-1) == 8: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(3,4,-1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(3,5,-1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(3,6,-1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(3,7,-1) == 4: "ERROR (S): getDiagonalLength is wrong";
 
             assert sanchitGame.getDiagonalLength(4,0,-1) == 4: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(4,1,-1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(4,2,-1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(4,3,-1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(4,4,-1) == 8: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(4,5,-1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(4,6,-1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(4,7,-1) == 5: "ERROR (S): getDiagonalLength is wrong";
 
             assert sanchitGame.getDiagonalLength(5,0,-1) == 3: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(5,1,-1) == 4: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(5,2,-1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(5,3,-1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(5,4,-1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(5,5,-1) == 8: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(5,6,-1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(5,7,-1) == 6: "ERROR (S): getDiagonalLength is wrong";
 
             assert sanchitGame.getDiagonalLength(6,0,-1) == 2: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(6,1,-1) == 3: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(6,2,-1) == 4: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(6,3,-1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(6,4,-1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(6,5,-1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(6,6,-1) == 8: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(6,7,-1) == 7: "ERROR (S): getDiagonalLength is wrong";
 
             assert sanchitGame.getDiagonalLength(7,0,-1) == 1: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(7,1,-1) == 2: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(7,2,-1) == 3: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(7,3,-1) == 4: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(7,4,-1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(7,5,-1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(7,6,-1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(7,7,-1) == 8: "ERROR (S): getDiagonalLength is wrong";
 
             assert sanchitGame.getDiagonalLength(0,0,1) == 1: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(0,1,1) == 2: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(0,2,1) == 3: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(0,3,1) == 4: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(0,4,1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(0,5,1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(0,6,1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(0,7,1) == 8: "ERROR (S): getDiagonalLength is wrong";
 
             assert sanchitGame.getDiagonalLength(1,0,1) == 2: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(1,1,1) == 3: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(1,2,1) == 4: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(1,3,1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(1,4,1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(1,5,1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(1,6,1) == 8: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(1,7,1) == 7: "ERROR (S): getDiagonalLength is wrong";
 
             assert sanchitGame.getDiagonalLength(2,0,1) == 3: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(2,1,1) == 4: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(2,2,1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(2,3,1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(2,4,1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(2,5,1) == 8: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(2,6,1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(2,7,1) == 6: "ERROR (S): getDiagonalLength is wrong";
 
             assert sanchitGame.getDiagonalLength(3,0,1) == 4: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(3,1,1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(3,2,1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(3,3,1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(3,4,1) == 8: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(3,5,1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(3,6,1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(3,7,1) == 5: "ERROR (S): getDiagonalLength is wrong";
 
             assert sanchitGame.getDiagonalLength(4,0,1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(4,1,1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(4,2,1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(4,3,1) == 8: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(4,4,1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(4,5,1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(4,6,1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(4,7,1) == 4: "ERROR (S): getDiagonalLength is wrong";
 
             assert sanchitGame.getDiagonalLength(5,0,1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(5,1,1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(5,2,1) == 8: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(5,3,1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(5,4,1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(5,5,1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(5,6,1) == 4: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(5,7,1) == 3: "ERROR (S): getDiagonalLength is wrong";
 
             assert sanchitGame.getDiagonalLength(6,0,1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(6,1,1) == 8: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(6,2,1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(6,3,1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(6,4,1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(6,5,1) == 4: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(6,6,1) == 3: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(6,7,1) == 2: "ERROR (S): getDiagonalLength is wrong";
 
             assert sanchitGame.getDiagonalLength(7,0,1) == 8: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(7,1,1) == 7: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(7,2,1) == 6: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(7,3,1) == 5: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(7,4,1) == 4: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(7,5,1) == 3: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(7,6,1) == 2: "ERROR (S): getDiagonalLength is wrong";
             assert sanchitGame.getDiagonalLength(7,7,1) == 1: "ERROR (S): getDiagonalLength is wrong";   
             
             int[] a = {-1,0,0,0,0,0,0,-1};
             int[] b = {-1,2,1,2,1,2,1,-1};
             int[] c = {-1,0,0,0,0,0,0,-1};
             int[] d = {-1,2,2,2,2,2,2,-1};
             int[] e = {-1,2,1,2,1,2,1,-1};
             int[] f = {1,0,2,0,0};
             
             assert Arrays.equals(sanchitGame.getRow(0),a): "ERROR (S): getRow is wrong";
             assert Arrays.equals(sanchitGame.getDiagonal(0,0,-1),b): "ERROR (S): getDiagonal is wrong";
             assert Arrays.equals(sanchitGame.getRow(7),c): "ERROR (S): getRow is wrong";
             assert Arrays.equals(sanchitGame.getColumn(0),d): "ERROR (S): getColumn is wrong";
             assert Arrays.equals(sanchitGame.getDiagonal(2,2,-1),e): "ERROR (S): getDiagonal is wrong";
             assert Arrays.equals(sanchitGame.getDiagonal(5,5,1),f): "ERROR (S): getDiagonal is wrong";
 
             System.out.println(sanchitGame);
             */
         } catch(Exception e) {
             System.out.println(e);
         }
 
     }
 }
 
 
         
 
 
