 import java.util.Random;
 
 public class Part1 {
 	
 	/* ********************************** *
 	 * *  Part 1 - increment            * *
 	 * ********************************** */
 	// Task 1.1
 	public static boolean increment(int[] vec, int base) {
         // Iterate over all digits (from LSB to MSB) and nullify them if they reached the cap.
         // Otherwise increase just the current digit, no need to increase the rest towards the MSB.
         for (int i=vec.length -1; i>=0; i--) {
             if (vec[i] == base -1) {
                 vec[i] = 0;
             } else {
                 vec[i]++;
                 return true;
             }
         }
 
         // If all digits were nullified, we reached the cap.
         return false;
 	}
 
 	// Task 1.2
 	public static boolean increment(int[][] matrix, int base) {
         // Iterate over all indices and increment them.
         for (int i=matrix.length -1; i>=0; i--) {
             if ( increment(matrix[i], base) ) {
                 return true;  // Return true if current index was incremented.
             }
         }
 
         // If all indices were nullified, we reached our cap.
         return false;
 	}
 	
 	/* ********************************** *
 	 * *  Part 2 - validate solution    * *
 	 * ********************************** */
 	// Task 2.1
 	public static int[] findSameColorRec(int[][] board, int topRow, int leftColumn) {
         int rows = board.length;  // Set row & column length.
         int columns = board[0].length;
 
         int color = board[topRow][leftColumn];  // Set rectangle corner color.
 
         // Iterate over all rows and columns, starting from {TopRow, LeftColumn}, and test all corners of the current rectangle.
         for (int i=topRow +1; i<rows; i++) {
             for (int j=leftColumn +1; j<columns; j++) {
                 if (board[topRow][j] == color && board[i][leftColumn] == color && board[i][j] == color) {
                     int[] coords = {i,j};
                     return coords;
                 }
             }
         }
 
         return null;
 	}
 	
 	// Task 2.2
 	public static int[] findSameColorRec(int[][] board) {
         int rows = board.length;  // Set row & column length.
         int columns = board[0].length;
 
         int[] coords = null;
 
         // Iterate over all cells and test rectangles, where the current cell is the top-left corner.
         for (int i=0; i<rows; i++) {
             for (int j=0; j<columns; j++) {
                 coords = findSameColorRec(board, i, j);
 
                 if (coords != null) {
                     int[] rect = {i, j, coords[0], coords[1]};
                     return rect;
                 }
             }
         }
 
         return null;
 	}
 
 	// Task 2.3
 	public static boolean isValidSolution(int[][] board, int c) {
         int rows = board.length;  // Set row & column length.
         int columns = board[0].length;
 
         int i,j;
 
         // Verify the board.
         if (board == null) {
             return false;
         }
 
         // Test for legal row lengths.
         for (i=0; i<rows; i++) {
             if (board[i] == null || board[i].length != columns) {
                 return false;
             }
 
             // Test for legal colors.
             for (j=0; j<columns; j++) {
                 if (board[i][j] >= c || board[i][j] < 0) {
                     return false;
                 }
             }
         }
 
         // Test for no rectangles.
         if (findSameColorRec(board) != null) {
             return false;
         }
 
         return true;
 	}
 	
 	/* ********************************** *
 	 * *  Part 3 - Basic solver         * *
 	 * ********************************** */
 	public static int[][] solver(int n, int m, int c) {
 		int[][] board = new int[n][m];
         int i,j;
 
         // Return uninitialized board for special cases where board is 0xN / Nx0.
         if (n == 0 || m == 0) {
             return board;
         }
 
         // Init the board.
         for (i=0; i<board.length; i++) {
             for (j=0; j<board.length; j++) {
                 board[i][j] = 0;
             }
         }
         
         // Increment board and test for rectangles.
         do {
             if (isValidSolution(board, c)) {
                 return board;
             }
         } while (increment(board, c));
 
 		return null;
 	}
 
 	/* ********************************** *
 	 * *  Part 4 - Random solver        * *
 	 * ********************************** */
 	// Task 4.1
 	public static int[][] randomBoard(int n, int m, int c) {
 		int[][] board = new int[n][m];
 
         // Iterate over all cells and assign a legal color to each one.
         Random random = new Random();
         for(int i=0; i<n; i++) {
             for(int j=0; j<m; j++) {
                 board[i][j] = random.nextInt(c);
             }
         }
 
 		return board;
 	}
 	
 	// Task 4.2
 	public static void randomFix(int[][] board, int c, int[] twoCorners) {
         int topRow    = twoCorners[0];
         int bottomRow = twoCorners[2];
 
         int leftColumn  = twoCorners[1];
         int rightColumn = twoCorners[3];
 
         int oldColor = board[topRow][leftColumn];
         int newColor = oldColor;
 
         Random random = new Random();
 
         // Generate a new color, different from the original color.
         if (c > 1) {
             while (newColor == oldColor) {
                 newColor = random.nextInt(c);
             }
        } else { // If we've only got one color, there's nothing to generate.
             newColor = 0; 
         }
 
         // Generate a random corner.
         int row    = random.nextInt(bottomRow   - topRow +1)     + topRow;
         int column = random.nextInt(rightColumn - leftColumn +1) + leftColumn;
 
         // Assign the new color to the corner.
         board[row][column] = newColor;
 	}
 	
 	// Task 4.3
 	public static int[][] randomSolver(int n, int m, int c, int numFixes) {
 		int[][] board = randomBoard(n, m, c);
         int[] rect;
 
         // Test for rectangles until numFixes limit is reached, while testing and fixing one rectangle each iteration.
         rect = findSameColorRec(board);
         for (int fixes=0; fixes<numFixes; fixes++) {
             if (rect == null) {
                 return board;
             } else {
                 randomFix(board, c, rect);
                 rect = findSameColorRec(board);
             }
         }
 
         if (rect == null) {
             return board;
         }
 
         return null;
 	}
 	
 	// Task 4.3
 	public static int[][] randomSolver(int n, int m, int c, int numResets, int numFixes) {
 		int[][] board;
 
        // Generate a limited amount of random boards and try to solve them with limited amount of fixes for each one.
         for (int resets=0; resets<numResets; resets++) {
             board = randomSolver(n, m, c, numFixes);
 
             if (board != null) {
                 return board;
             }
         }
 
 		return null;
 	}
 
 	
 	/* ********************************** *
 	 * *  Main you may want to use      * *
 	 * ********************************** */
     public static void main(String[] args) {
        int n=15, m=15, c=7;
 
         long startTime=System.currentTimeMillis();
 
         //int[][] sol=solver(n, m, c);
         int[][] sol=randomSolver(n, m, c, n*m, n*m);
 
         long endTime=System.currentTimeMillis();
 
 
         System.out.println("Solution time : "+(endTime-startTime)+" ms");
         System.out.println("Solution found: "+(sol!=null));
 
         if(sol!=null) {
             System.out.println("Valid solution: "+isValidSolution(sol,c));
         }
 
         System.out.println();
 
         if (sol != null) {
             for (int i=0; i<sol.length; i++) {
                 for (int j=0; j<sol[i].length; j++) {
                     System.out.print(sol[i][j] + " ");
                 }
 
                 System.out.println();
             }
         } else {
             System.out.print("null");
         }
	}
 }
