 public class SudokuSolver {
   protected int[][] board;
 
   public SudokuSolver(int[][] inputBoard) {
     board = inputBoard;
   }
 
   public SudokuSolver(){
     board = new int[9][9];
   }
 
 
   /**
    * Set the board instance variable.
    */
 
   public void setBoard(int[][] inputBoard){
     board = inputBoard;
   }
 
   /**
    * Get the board instance variable
    */
 
   public int[][] getBoard(){
     return board;
   }
 
   /**
    * Clear the board, set every element to zero
    */
   public void clear(){
     board = new int[9][9];
   }
 
   /** 
    * Solve method that solves the sudoku board by calling the recursive
    * solve method.
    *
    * @param newBoard  the input board to solve
    */
 
   public boolean solve(){
     System.out.println("Inside solve");
     if( checkForIllegalBoard() ){
       return false;
     }
     System.out.println("After check, attempting to solve");
     boolean result = solve(0,0,board);
     System.out.println("After solve, about to return");
     return result;
   }
 
   /**
    * Recursive method that solves the board and fills in the board
    * instance variable with the correct values. Returns true/false, if
    * the board was able to be solved.
    *
    * @return whether the board could be solved or not
    */
 
   private boolean solve(int i, int j, int[][] tempBoard) {
     if (j == 9) {
       j = 0;
       if (++i == 9)
         return true;
     }
     if (tempBoard[i][j] != 0)
       return solve(i, j + 1, tempBoard);
     /* try each value in this spot */
     for (int val = 1; val <= 9; val++) {
       if (correct(i, j, val, tempBoard)) {
         tempBoard[i][j] = val;
         if (solve(i, j + 1, tempBoard))
           return true;
       }
     }
     tempBoard[i][j] = 0;
     return false;
 
   }
 
   /**
    * Tests if the given value in the given spot is correct by checking
    * the rest of the values around it (the row, col, and box).
    *
    */
 
   public boolean correct(int row, int col, int val, int[][] tempBoard){
     /* check the rows and columns to see if the values are valid by
        sudoku rules */
     for(int i = 0; i < 9; i++){
       // Check the column
       if(i != row && tempBoard[i][col] == val)
         return false;
       // Check the row
       if(i != col && tempBoard[row][i] == val)
         return false;
     }
 
     /* check the current box for validity */
     
     //find the first row and col
     int rowNum = (row/3)*3;
     int colNum = (col/3)*3;
 
     for(int i = rowNum; i < rowNum + 3; i++){
       for(int j = colNum; j < colNum + 3; j++){
         if( i != row && j != col && tempBoard[i][j] == val)
           return false;
       }
     }
 
     //return true if we find that the value is not invalid (yes, not invalid)
     return true;
 
   }
 
   /**
    * Checks the board to make sure that the current board is actually
    * solvable. This check is performed before the recursive solve function
    * is called.
    */
 
   public boolean checkForIllegalBoard(){
     for(int i=0; i<board.length; i++){
       for(int j=0; j<board[0].length; j++){
        if( !correct(i,j,board[i][j], board) )
           return true;
       }
     }
     return false;
   }
 
 }
