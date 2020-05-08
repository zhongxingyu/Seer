 import java.util.*;
 
 public class SudokuGame {
 
   int[][][] set = new int[9][9][9];
   int[][][] branches = new int[74][9][9];
   int[][] board = new int[9][9];
   int branchCounter = 0;
   int step = 0;
 
   /**
    * Defines board and creates the initial possible values array with numbers 1 - 9.
    *
    * @param in the starting game board.
    */
   public SudokuGame(int[][] in) {
     board = in;
 
     resetPossValues();
   }
 
 
   public void resetPossValues() {
     for (int a = 0; a < 9; a++)
       for (int b = 0; b < 9; b++)
         for (int c = 1; c < 10; c++)
           set[a][b][c-1] = c;
 
     makePossArr();
   }
 
 
   /**
    * Updates the array of possible values for each space on the board by taking each
    * filled space and removing that space's value from the possible values of each
    * space in the same row, column, and 3x3 square. The first two dimensions of set
    * represent the coordinates of the space on the board, and the third dimension
    * represents the remaining possible values for that space. Values that are no
    * longer possible for a space are replaced by 0s.
    *
    * Does not update board.
    *
    * @return the set of possible values for each space on board.
    */
   public int[][][] makePossArr() {
 
     for (int i = 0; i < 9; i++) {
       for (int j = 0; j < 9; j++) {
         int num = board[i][j];
 
         // Blank spaces on the board are represented by 0.
         if (num > 0) {
 
           // Removes num from possible values of the spaces in column j.
           for (int x = 0; x < 9; x++)
             set[x][j][num - 1] = 0;
 
           // Removes num from possible values of the spaces in row i.
           for (int y = 0; y < 9; y++)
             set[i][y][num - 1] = 0;
 
           // 2-value array that holds the square position of a space.
           int[] squarePos = new int[2];
 
           // Calculates the square position based on row i and column j.
           squarePos[0] = (i / 3) * 3;
           squarePos[1] = (j / 3) * 3;
 
           // Removes num from possible values of other spaces of the squarePos[0][1].
           for (int w = squarePos[0]; w < squarePos[0] + 3; w++)
             for (int z = squarePos[1]; z < squarePos[1] + 3; z++)
               set[w][z][num - 1] = 0;
 
           // Removes all possible values for the filled space.
           for (int e = 1; e < 9; e++)
             set[i][j][e] = 0;
 
           // Inserts num as the only possible value for the filled space.
           set[i][j][num - 1] = num;
         }
       }
     }
     return set;
   }
 
 
   /**
    * Updates the array of possible values. If there's only one possible value for a
    * space, inserts value into space in board. Then makes a new array of possible
    * values given the updated board.
    *
    * @return the set of possible values for each space on board.
    */
   public void solvePuzzle() {
     int changes = updateBoard();
     step++;
 
     if (changes > 0) {
       printBoard(board, "Step " + step, changes);
 
       if (isSolved())
         printBoard(board, "PUZZLE SOLVED", 0);
       else
         solvePuzzle();
     } else {
       if (createBranch()) {
         printBoard(board, "No solutions; branch " + branchCounter + " created", 0);
         set = makePossArr();
         solvePuzzle();
       } else {
         if (branchCounter > 0) {
           revertBranch();
           makePossArr();
           solvePuzzle();
         } else {
           System.out.println("\nERROR: Puzzle could not be solved.\n");
         }
       }
     }
   }
 
 
   public boolean createBranch() {
     int counter = 0;
 
     for (int i = 0; i < 9; i++) {
       for (int j = 0; j < 9; j++) {
         if (numPossValues(i,j) == 2) {
           branchCounter++;
          branches[branchCounter - 1] = board;
           System.out.printf("\n\nBRANCH CREATED AT [%d][%d]\n", i, j);
           splitBranches(branchCounter, i, j);
           return true;
         }
       }
     }
 
     return false;
   }
 
 
   public void splitBranches(int branchCounter, int i, int j) {
     boolean numPicked = false;
 
     for (int g = 0; g < 9; g++) {
       int val = g + 1;
       if (set[i][j][g] != 0 && set[i][j][g] != board[i][j]) {
        if (numPicked) {
          System.out.println("Possible option 2: " + val);
          branches[branchCounter - 1][i][j] = val;
        } else {
           System.out.println("Possible option 1: " + val);
           board[i][j] = val;
           numPicked = true;
         }
       }
     }
   }
 
 
   public void revertBranch() {
     board = branches[branchCounter - 1];
     resetPossValues();
     printBoard(board, "Board reverted to branch " + branchCounter, 0);
     branchCounter--;
   }
 
 
   public int updateBoard() {
     int counter = 0;
     int index = 0;
     int changes = 0;
 
     for (int i = 0; i < 9; i++) {
       for (int j = 0; j < 9; j++) {
         for (int g = 0; g < 9; g++) {
           if (set[i][j][g] != 0 && set[i][j][g] != board[i][j]) {
             counter++;
             index = g;
           }
         }
         if (counter == 1 && board[i][j] == 0) {
           changes++;
           board[i][j] = set[i][j][index];
           makePossArr();
         }
         counter = 0;
       }
     }
     return changes;
   }
 
 
   public boolean isSolved() {
     boolean solved = true;
 
     for (int i = 0; i < 9; i++)
       for (int j = 0; j < 9; j++)
         if (board[i][j] == 0)
           solved = false;
 
     return solved;
   }
 
 
   public int numPossValues(int i, int j) {
     int counter = 0;
 
     for (int g = 0; g < 9; g++)
       if (set[i][j][g] != 0 && set[i][j][g] != board[i][j])
         counter++;
 
     return counter;
   }
 
 
   public int[][] getBoard() {
     return board;
   }
 
 
   public void printBoard(int[][] board, String title, int changes)
   {
     if (changes == 1)
       System.out.printf("\n\n%s: (%d %s)\n", title, changes, "change");
     else if (changes > 1)
       System.out.printf("\n\n%s: (%d %s)\n", title, changes, "changes");
     else
       System.out.printf("\n\n%s:\n", title);
 
    checkFailure(board);
 
     System.out.println("|-----------|-----------|-----------|");
     for (int j = 0; j < 9; j++) {
       for (int i = 0; i < 9; i++) {
         if (i == 0)
           System.out.print("|");
         if (board[i][j] == 0)
           System.out.printf("   ");
         else
           System.out.printf(" %d ", board[i][j]);
         System.out.print("|");
         if (i == 8)
           System.out.println("");
       }
       if (j % 3 == 2)
         System.out.println("|-----------|-----------|-----------|");
     }
   }
 
 
  public void checkFailure(int[][] board) {
     int failures = 0;
 
     for (int i = 0; i < 9; i++) {
       for (int j = 0; j < 9; j++) {
         boolean possValues = false;
         for (int g = 0; g < 9; g++)
           if (set[i][j][g] > 0)
             possValues = true;
         if (possValues != true)
           failures++;
 
         int num = board[i][j];
 
         if (num > 0) {
           for (int x = 0; x < 9; x++)
             if (num == board[x][j] && x != i)
               failures++;
 
           for (int y = 0; y < 9; y++)
             if (num == board[i][y] && y != j)
               failures++;
 
           int[] squarePos = new int[2];
 
           squarePos[0] = (i / 3) * 3;
           squarePos[1] = (j / 3) * 3;
 
           for (int w = squarePos[0]; w < squarePos[0] + 3; w++)
             for (int z = squarePos[1]; z < squarePos[1] + 3; z++)
               if (num == board[w][z] && w != i && z != j)
                 failures++;
         }
       }
     }
    if (failures > 0)
      System.out.println("FAILURES: " + failures);
   }
 }
