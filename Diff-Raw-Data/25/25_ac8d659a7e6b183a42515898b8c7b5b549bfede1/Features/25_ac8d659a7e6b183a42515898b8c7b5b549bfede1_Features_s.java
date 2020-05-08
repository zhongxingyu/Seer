 
 public class Features {
 
     public static int contactAreaScore(State s, int[] move) {
         int result = 0;
         //to be continued..
         return result;
     }
 
     //Maximum Altitude - The height of the tallest column on the game board
     //note that it does not care about whether the rows get clear anot
     //it will only compute the maximum achievable height if a piece is place there
     public static int maximumAltitude(State s, int[] move) {
         int highest = -1;
         int nextPiece = s.getNextPiece();
         int orientation = move[0];
         int slot = move[1];
         int pieceHt = State.getpHeight()[nextPiece][orientation];
 
         for (int w = 0; w < State.getpWidth()[nextPiece][orientation]; w++) {
             highest = Math.max(s.getTop()[slot + w] + pieceHt, highest);
         }
 
         return highest;
     }
 
     //The difference in height between the tallest column and the shortest column
     public static int altitudeDelta(State s, int [] move) {
         return Features.maximumAltitude(s, move) - Features.findMinHeight(s);
     }
 
     //Total lines clear so far -_-
     public static int linesCleared(State s, int[] move) {
         return s.getRowsCleared() + computelinesClearScore(s, move);
     }
 
     public static int computelinesClearScore(State s, int[] move) {
         int nextPiece = s.getNextPiece();
         int orientation = move[0];
         int slot = move[1];
         //new height, the top of that particular row - the bottom of that particular piece
         //- becos 1 = a hole at the bottom
         int newHt = s.getTop()[slot] - State.getpBottom()[nextPiece][orientation][0];
 
         //for the tromino width, do the same thing
         for (int w = 1; w < State.getpWidth()[nextPiece][orientation]; w++) {
             //favor clearing the top rows first
             newHt = Math.max(s.getTop()[slot + w] - State.getpBottom()[nextPiece][orientation][w], newHt);
         }
 
         int [][]clonedField = cloneField(s.getField());
 
         int linesCleared = 0;
         //from the new height + the next piece height (-1 cos index starts from 0)
         //down to newHt
         for (int h = newHt + State.getpHeight()[nextPiece][orientation] - 1; h >= newHt; h--) {
            if (h >= State.ROWS) {
                 continue; //GGPOK already, dont consider -.-
            }
             boolean completeRow = true;
             for (int c = 0; c < State.COLS; c++) {
                 if (clonedField[h][c] == 0) { //if got hole
                     completeRow = false;
                     break;
                 }
             }
             if (completeRow) {
                 linesCleared += 1;
             }
         }
 
         return linesCleared;
     }
 
     private static int findMinHeight(State s) {
         int[] top = s.getTop();
         int lowest = Integer.MAX_VALUE;
         for (int i : top) {
             lowest = Math.min(i, lowest);
         }
 
         return lowest;
     }
 
     private static int[][] cloneField(int [][] source)
     {
         //Clone a field
         int[][] clonedField = new int[source.length][source[0].length];
         for (int i = 0; i < source.length; i++) {
             System.arraycopy(source[i], 0, clonedField[i], 0, source[i].length);
         }
         return clonedField;
     }
 }
