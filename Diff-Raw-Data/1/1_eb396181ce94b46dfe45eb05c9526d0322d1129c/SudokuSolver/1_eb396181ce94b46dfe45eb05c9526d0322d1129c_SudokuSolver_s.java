 package org.mikaelbrevik.sudokusolver;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author mikaelbrevik
  */
 public class SudokuSolver {
     
     public static final int NUMBER_OF_VALUES = 9*9;
 
     private int[][] original;
     private int[][] solved;
 
     public SudokuSolver(int[][] map) {
         original = map;
         solved = deepCloneArray(map);
     }
 
     public void setSudokuMap(int[][] map) {
         original = map;
     }
 
     public int[][] getSudokuMap() {
         return original.clone();
     }
 
     public String getOriginalSudokuAsString() {
         return Arrays.deepToString(original);
     }
 
     public int[][] getSolvedSudoku() {
         return solved.clone();
     }
     
     public String getSolvedSudokuAsString() {
         return Arrays.deepToString(solved);
     }
 
     /**
      * Check if input follows given constraints:
      * 
      * 1. Distinct values in row.
      * 2. Distinct values in column.
      * 3. Distinct values in 3x3 square.
      * 
      * @param pos Position of input
      * @param value Value of input
      * @return boolean Boolean value of if the input follows constraints.
      */
     private boolean followsConstraints(Point pos, int value) {
 
         // Constraint 1: Distinct in row
         for (int i = 0; i < Math.sqrt(NUMBER_OF_VALUES); i++) {
             if (solved[i][pos.y] == value) {
                 return false;
             }
         }
 
         // Constraint 2: Distinct in column
         for (int i = 0; i < Math.sqrt(NUMBER_OF_VALUES); i++) {
             if (solved[pos.x][i] == value) {
                 return false;
             }
         }
 
         // Constraint 3: Distinct in square
         for (int i = 0; i < 3; i++) {
             for (int j = 0; j < 3; j++) {
                 if (solved[(pos.x / 3) * 3 + i][(pos.y / 3) * 3 + j] == value) {
                     return false;
                 }
             }
         }
 
         // Follows all constraints. Return true. 
         return true;
     }
 
     /**
      * Creates an independent copy(clone) of an array.
      * 
      * @param array The array to be cloned.
      * @return An independent 'deep' structure clone of the array.
      */
     public static int[][] deepCloneArray(int[][] array) {
         int rows = array.length;
 
         //clone the 'shallow' structure of array
         int[][] newArray = (int[][]) array.clone();
         //clone the 'deep' structure of array
         for (int row = 0; row < rows; row++) {
             newArray[row] = (int[]) array[row].clone();
         }
 
         return newArray;
     }
 
     /**
      * Public accessible method for solving sudoku board. 
      * A wrapper for the recursive function. 
      * 
      * 
      * @return boolean
      */
     public boolean solveSudoku() {
         return solveSudoku(0);
     }
 
     /**
      * Recursive method for inserting values to sudoku board.
      * Uses test and generate paradigm from CLP. 
      * 
      * 
      * @param i - Should be initialized with 0. Used by recursion
      * @return boolean - If value is inserted. 
      */
     private boolean solveSudoku(int i) {
         
         if (i >= NUMBER_OF_VALUES) {
             return true;
         }
         
         int y = i / (int) Math.sqrt(NUMBER_OF_VALUES); // Find y value
         int x = i - y*(int) Math.sqrt(NUMBER_OF_VALUES); // find x value
         
         // System.out.println("("+x+","+y+")");
         
         // Check if pre-filled
         if(solved[x][y] != 0) {
             return solveSudoku(i+1);
         }
         
         for (int value = 1; value <= Math.sqrt(NUMBER_OF_VALUES); ++value) {
             if (!followsConstraints(new Point(x, y), value)) {
                 continue;
             }
             
             solved[x][y] = value;
             
             if (solveSudoku(i+1)) {
                 return true;
             }
         }
         
         // Could not insert value. Reset value.        
         solved[x][y] = 0;
         return false;
     }
 
     public boolean checkSudoku() {
 
         // Check for valid board. 
         for (int i = 0; i < Math.sqrt(NUMBER_OF_VALUES); i++) {
             List<Integer> exsistsRow = new ArrayList<Integer>();
             List<Integer> exsistsCol = new ArrayList<Integer>();
             List<Integer> exsistsSquare = new ArrayList<Integer>();
 
             for (int j = 0; j < Math.sqrt(NUMBER_OF_VALUES); j++) {
 
                 if (solved[i][j] == 0 || solved[j][i] == 0) {
                     return false;
                 }
                 if (exsistsRow.contains(solved[i][j])) {
                     return false;
                 }
 
 
                 if (exsistsCol.contains(solved[j][i])) {
                     return false;
                 }
 
                 exsistsRow.add(solved[i][j]);
                 exsistsCol.add(solved[j][i]);
 
                 if (j < 3) {
                     for (int k = 0; k < 3; k++) {
                         int val = solved[j + 3 * (i % 3)][k + 3 * (i / 3)];
                         if (exsistsSquare.contains(val)) {
                             return false;
                         }
                         exsistsSquare.add(val);
                     }
                 }
             }
 
         }
 
         return true;
     }
 
     public static void main(String[] args) {
         int[][] board1 = {
             {0, 6, 0, 1, 0, 4, 0, 5, 0},
             {0, 0, 8, 3, 0, 5, 6, 0, 0},
             {2, 0, 0, 0, 0, 0, 0, 0, 1},
             {8, 0, 0, 4, 0, 7, 0, 0, 6},
             {0, 0, 6, 0, 0, 0, 3, 0, 0},
             {7, 0, 0, 9, 0, 1, 0, 0, 4},
             {5, 0, 0, 0, 0, 0, 0, 0, 2},
             {0, 0, 7, 2, 0, 6, 9, 0, 0},
             {0, 4, 0, 5, 0, 8, 0, 7, 0}
         };
         
         
         int[][] board2 = {
           { 0, 8, 0, 4, 0, 2, 0, 6, 0 },
           { 0, 3, 4, 0, 0, 0, 9, 1, 0 },
           { 9, 6, 0, 0, 0, 0, 0, 8, 4 },
           { 0, 0, 0, 2, 1, 6, 0, 0, 0 },
           { 2, 0, 0, 0, 0, 9, 6, 0, 0 },
           { 0, 1, 0, 3, 5, 7, 0, 0, 8 },
           { 8, 4, 0, 0, 0, 0, 0, 7, 5 },
           { 0, 2, 6, 0, 0, 0, 1, 3, 0 },
           { 0, 9, 0, 7, 0, 1, 0, 4, 0 }
         };
 
 
         SudokuSolver solver = new SudokuSolver(board1);
         solver.solveSudoku();
         
         System.out.println(solver.getOriginalSudokuAsString());
         System.out.println(solver.getSolvedSudokuAsString());
 
         System.out.println(solver.checkSudoku());
         
         solver.setSudokuMap(board2);
         solver.solveSudoku();
         
         System.out.println(solver.getOriginalSudokuAsString());
         System.out.println(solver.getSolvedSudokuAsString());
 
         System.out.println(solver.checkSudoku());
         
     }
 }
