 import java.util.LinkedList;
 
 /* This class is where we solve the sudoku */
 public class SolveSudoku {
 	
 	// TODO: write your shit here to solve the read in sudoku sohaib
 	//		 if it doesn't work do it in the SudokuFileReader and i'll just rename the class
 	//		 daniel feel free to do some work
    private static final int SMALLBOX_SIZE = 3;
 	boolean[] digits;
 
    
 	private SudokuBoard sBoard;
 	
 	public SolveSudoku(SudokuBoard sBoard){
 		this.sBoard = sBoard;
 	}
 	
    public SudokuBoard recursiveBruteForceSolver() {
       for (int i = 0; i < sBoard.getBoard().length; i++)
       {
          for (int j = 0; i < sBoard.getBoard()[i].length; i++)
          {
             if (sBoard.getBoard()[i][j]==0) {
                for (Integer k : getPossibilities(i, j))
                {
                   sBoard.setCellNum(k.intValue(), i, j);
                   sBoard = recursiveBruteForceSolver();
                   if (isComplete() && isLegalBoard()) {
                      return sBoard;
                   }
                } 
             }
          }
       }
       return null;
    }
 
    private LinkedList<Integer> getPossibilities(int row, int column)
    {
       LinkedList<Integer> numbers = new LinkedList<Integer>();
       for (int i = 1; i < 10; i++) 
       {
          numbers.add(i);
       }
       for (Integer i: numbers)
       {
          for (int j = 0; j < 9; j++)
          {
             if (sBoard.getBoard()[row][j] == i.intValue() ||
                 sBoard.getBoard()[j][column] == i.intValue()) {
                numbers.remove(i);
             }
          }
       }
       return numbers;
    }
    
    /**
     * main function checks board is valid
     * @return
     */
    private boolean isLegalBoard(){
       for(int i = 0; i <9; i++ ){
     	  if(!rowValid(i)){
     		  return false;
     	  }
       }
       
       for(int i = 0; i < 9; i++){
     	  if(!columnValid(i)){
     		  return false;
     	  }
       }
       
       for(int i =0; i < 9; i++){
     	  for(int j = 0; j < 9; j += 3){
     		  if(!smallBoxValid(i, j)){
     			  return false;
     		  }
     	  }
       }
       return true;
    }
    
    /**
     * checks a row is valid
     * @param row
     * @return
     */
    public boolean rowValid (int row){
 	   resetnumbers();
 	   for( int k =0; k <9; k++){
 		   if(!numbersManager( sBoard.getBoard()[row][k])){
 			   return false;
 		   }
 	   }
 	   return true;
    }
    
    /**
     * checks a column is valid
     * @param column
     * @return
     */
    public boolean columnValid(int column){
 	   resetnumbers();
 	   for( int k =0; k <9; k++){
 		   if(!numbersManager( sBoard.getBoard()[column][k])){
 			   return false;
 		   }
 	   }
 	   return true;
    }
    
    /**
     * checks the smallboxes in sudoku are valid
     * @param row
     * @param column
     * @return
     */
    public boolean smallBoxValid(int row, int column){
 	   resetnumbers();
 	   
 	   for(int k =0; k < 3;  k++){
 		   for(int c = 0; c <3; c++){
 			   if(!numbersManager(sBoard.getBoard()[k + row][c + column])){
 				   return false;
 			   }
 		   }
 	   }
 	   return true;
    }
    /**
     * resets numbers to false
     */
    public void resetnumbers(){
 	   digits = new boolean[10];
    }
    
   /**
    * keeps track of numbers used in Sudoku
    * @param number
    * @return
    */
    public boolean numbersManager(int number){
 	   if( number != 0 && digits[number]){
 		   return false;
 	   }else{
 		   digits[number] = true;
		   return true;
 	   }
    }
    
    
    
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
    
    
    private boolean isComplete()
    {
       for (int i = 0; i < 9; i++)
       {
          for (int j = 0; j < 9; j++)
          {
             if (sBoard.getBoard()[i][j] == 0) {
                return false;
             }
          }
       }
       return true;
    }
    
 }
