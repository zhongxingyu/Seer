 import static org.junit.Assert.*;
 import org.junit.Test;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 
 /* This class is where we solve the sudoku */
 public class SolveSudoku {
 	
 	// TODO: write your shit here to solve the read in sudoku sohaib
 	//		 if it doesn't work do it in the SudokuFileReader and i'll just rename the class
 	//		 daniel feel free to do some work
    private static final int SMALLBOX_SIZE = 3;
 	boolean[] digits;
    
 	//private SudokuBoard sBoard;
 	
 	public SolveSudoku(){
 		//this.sBoard = sBoard;
 	   testIsValid();
 	}
 	
 	private void testIsValid(){
 	     
       Scanner sc = null;
       int[][] board = new int[9][9];
       int nextNumber = 0;
       
       try { 
          sc = new Scanner (new FileReader ("resources/input2"));
          while (sc.hasNext()) { 
             for (int row = 0; row < board.length; row++) {
                for (int column = 0; column < board[row].length; column++) {
                   nextNumber = sc.nextInt();
                   board[row][column] = nextNumber;
                }
             }
          }
       } catch (FileNotFoundException e) {}
         catch (NoSuchElementException e) {}
       
       SudokuBoard sBoard = new SudokuBoard(board);
       assertTrue(isValid(sBoard));
      
 	}
 	
 	public SudokuBoard recursiveBruteForceSolver(SudokuBoard sBoard) {
 	   System.out.print("-\n");
       sBoard.printBoard();
       int i, j;
       int[] emptyCell = findEmptyCell(sBoard);
       i = emptyCell[0];
       j = emptyCell[1];
       if (i == -1 || j == -1) {
          return sBoard;
       }
       System.out.print("Modifying cell ("+ i+","+j+")\n");
       LinkedList<Integer> possibilities= getPossibilities(i, j, sBoard);
       System.out.print("possible values:{");
       for (Integer k : possibilities)
       {
          System.out.print(k.intValue());
          System.out.print(" ");
       }
       System.out.print("}\n");
       if (possibilities.size() == 0) {
          System.out.print("no possibilities\n");
       }
       for (Integer k : possibilities)
       {
          System.out.print("Trying:" + k.intValue()+ " for cell ("+ i+","+j+")\n");
          sBoard.setCellNum(k.intValue(), i, j);
          sBoard.printBoard();
          SudokuBoard temp = copy(sBoard);
          temp = recursiveBruteForceSolver(temp);
          if (temp == null) {
             System.out.print("wrong branch\n");
          } else {
             if (isComplete(temp)) {
                System.out.print("full board\n");
                if (isValid(temp)) {
                   System.out.print("valid \n");
                   return temp;
                }
                System.out.print("not valid \n");
             }
          }
       } 
       return null;
    }
    
    private int[] findEmptyCell(SudokuBoard sBoard)
    {
       int[] emptyCell = {-1, -1};
       for (int i = 0; i < 9; i++)
       {
          for (int j = 0; j < 9; j++)
          {
             if (sBoard.getBoard()[i][j]==0) {
                emptyCell[0] = i;
                emptyCell[1] = j;
                return emptyCell;
             }
          }
       }
       return emptyCell;
    }
 
    private LinkedList<Integer> getPossibilities(int row, int column, SudokuBoard sBoard)
    {
       LinkedList<Integer> numbers = new LinkedList<Integer>();
       for (int i = 1; i < 10; i++) 
       {
          numbers.add(i);
       }
       for (int j = 0; j < 9; j++)
       {
          if (numbers.contains(new Integer(sBoard.getBoard()[row][j]))) {
             numbers.remove(new Integer(sBoard.getBoard()[row][j]));
          }
          if (numbers.contains(new Integer(sBoard.getBoard()[j][column]))) {
             numbers.remove(new Integer(sBoard.getBoard()[j][column]));
          }
       }
       
       int subGridRow = row - (row % 3);
       int subGridColumn = column - (column % 3);
       
       for(int i = subGridRow; i < subGridRow + 3; i++){
          for(int j = subGridColumn; j < subGridColumn + 3; j++){
             if (numbers.contains(new Integer(sBoard.getBoard()[i][j]))) {
                numbers.remove(new Integer(sBoard.getBoard()[i][j]));
             }
          }
       }
       
       return numbers;
    }
    
    public static boolean isValid(SudokuBoard sBoard){
 	   
 	   
 	   for(int i = 0; i < 9; i ++){
 		   if(!isValidRow(sBoard, i, 9)){
 			   //row has repetitions
 			   return false;
 			   }
 	   }   
 	   for(int j = 0; j < 9; j++){
 		   if(!isValidColumn(sBoard, j, 9)){
 			   // columns has repetitions
 			   return false;
 		   }
 	   }   
 	   return true;
    }
    
    private static boolean isValidSubGrid(SudokuBoard sBoard, int column, int row){
       int subGridRow = row - (row % 3);
       int subGridColumn = column- (column % 3);
       for(int i = subGridRow; i < subGridRow + 3; i++){
          for(int j = subGridColumn; j < subGridColumn + 3; j++){
             for(int k = subGridRow+1; i < subGridRow + 3; i++){
                for(int l = subGridColumn+1; j < subGridColumn + 3; j++){
                   if(sBoard.getBoard()[i][j] == sBoard.getBoard()[k][l]){
                      return false;
                   }
                }
             }
          }
       }
       return true;
    }
    
    private static boolean isValidColumn(SudokuBoard sBoard, int column, int height){
 	   for(int i = 0; i < 9; i++){
 		   for(int j = i + 1; j < 9; j++){
 			   if(sBoard.getBoard()[i][column] == sBoard.getBoard()[j][column]){
 				   return false;
 			   }
 		   }
 	   }
 	   return true;
    }
    
    private static boolean isValidRow(SudokuBoard sBoard, int row, int width){
 	   for(int i = 0; i < 9; i++){
 		   for(int j = i + 1; j < 9; j++){
			   if(sBoard.getBoard()[row][i] == sBoard.getBoard()[row][i]){
 				   return false;
 			   }
 		   }
 	   }
 	   return true;
    }
    
    /**
     * main function checks board is valid
     * @return
     */
 //   private boolean isLegalBoard(SudokuBoard sBoard){
 //      //if (sBoard == null) return false;
 //      for(int i = 0; i <9; i++ ){
 //    	  if(!rowValid(i, sBoard)){
 //    		  return false;
 //    	  }
 //      }
 //      
 //      for(int i = 0; i < 9; i++){
 //    	  if(!columnValid(i, sBoard)){
 //    		  return false;
 //    	  }
 //      }
 //      
 //      for(int i =0; i < 9; i++){
 //    	  for(int j = 0; j < 9; j += 3){
 //    		  if(!smallBoxValid(i, j, sBoard)){
 //    			  return false;
 //    		  }
 //    	  }
 //      }
 //      return true;
 //   }
 //   
 //   /**
 //    * checks a row is valid
 //    * @param row
 //    * @return
 //    */
 //   public boolean rowValid (int row, SudokuBoard sBoard){
 //	   resetnumbers();
 //	   for( int k =0; k <9; k++){
 //
 //		   if(!numbersManager(sBoard.getBoard()[row][k])){
 //			   return false;
 //		   }
 //	   }
 //	   return true;
 //   }
 //   
 //   /**
 //    * checks a column is valid
 //    * @param column
 //    * @return
 //    */
 //   // comments : numbersMANAGER (INT) _> (INT [][] 
 //  
 //   //find the location of col in the boad
 //   //basically you need ot test if col 
 //   //numbersMNAGER 
 //   
 //   public boolean columnValid(int column, SudokuBoard sBoard){
 //	   resetnumbers();
 //	     for( int k =0; k <9; k++){
 //		   
 //		   
 //		   
 //	   }
 //	   return true;
 //   }
 //   
 //   /**
 //    * checks the smallboxes in sudoku are valid
 //    * @param row
 //    * @param column
 //    * @return
 //    */
 //   public boolean smallBoxValid(int row, int column, SudokuBoard sBoard){
 //	   resetnumbers();
 //	   
 //	   for(int k =0; k < 3;  k++){
 //		   for(int c = 0; c <3; c++){
 //			   if(!numbersManager(sBoard.getBoard()[k + row][c + column])){
 //				   return false;
 //			   }
 //		   }
 //	   }
 //	   return true;
 //   }
 //   /**
 //    * resets numbers to false
 //    */
 //   public void resetnumbers(){
 //	   digits = new boolean[10];
 //   }
 //   
 //  /**
 //   * keeps track of numbers used in Sudoku
 //   * @param number
 //   * @return
 //   */
 //   public boolean numbersManager(int number){
 //	   if( number != 0 && digits[number]){
 //		   return false;
 //	   }else{
 //		   digits[number] = true;
 //		   return true;
 //	   }
 //   }
    
    
    
    /**
     * checks if board is legal and according to laws of Sudoku
     * @param number
     * @param row
     * @param column
     * @return
     */
 //   private boolean isLegalBoardCell(int number, int row, int column){
 //	   int boardrow = (row/SMALLBOX_SIZE) * SMALLBOX_SIZE;
 //	   int boardcolumn = (row/SMALLBOX_SIZE) * SMALLBOX_SIZE;
 //	   
 //	   for (int  i = 0; i < 9; i++){
 //		   if(sBoard.getCellNum(row, i)== number ||
 //			  sBoard.getCellNum(i, column) == number ||
 //			  sBoard.getCellNum(boardrow + (i % SMALLBOX_SIZE), boardcolumn + (i / SMALLBOX_SIZE) ) == number){
 //			   return false;
 //		   }
 //			   
 //	   }
 //	   return true;
 //   }
    
    
    private boolean isComplete(SudokuBoard sBoard)
    {
       for (int i = 0; i < 9; i++)
       {
          for (int j = 0; j < 9; j++)
          {
             /*System.out.print(i + "," + j + ":");
             System.out.print(sBoard.getBoard()[i][j]);
             System.out.print("\n");*/
             if (sBoard.getBoard()[i][j] == 0) {
                return false;
             }
          }
       }
       return true;
    }
    
    private SudokuBoard copy(SudokuBoard sBoard)
    {
       int[][] newGrid = new int[9][9];
       for (int i = 0; i < 9; i++) {
          for (int j = 0; j < 9; j++) {
           newGrid[i][j] = sBoard.getBoard()[i][j];
          }
         }
       SudokuBoard newBoard = new SudokuBoard(newGrid);
       return newBoard;
    }
    
 }
