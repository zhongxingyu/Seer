 import java.util.Random;
 
 /* This class is designed to generate sudokus */
 public class GenerateSudoku {
 	
 	/* Constants defined here for clarity */
 	private final int MAX_NUM_COLUMNS = 9;
 	private final int MAX_NUM_ROWS = 9;
 
	private SudokuBoard board = new SudokuBoard();
 	/* Constructor */
 	public GenerateSudoku() { 
 		
 	}
 	
	public SudokuBoard getBoard(){
		return this.board;
	}
 	/* Methods */
 	/**
 	 * Generates a 2D array of random numbers
 	 * @return a random sudoku board
 	 */
 	public SudokuBoard generateRandomBoard() { 
 		
 		int newBoard[][] = new int[MAX_NUM_ROWS][MAX_NUM_COLUMNS];
 		Random rand = new Random();
 		
 		for (int rows = 0; rows < newBoard.length; rows++) {
 			for (int columns = 0; columns < newBoard[rows].length; columns++) {
 				newBoard[rows][columns] = rand.nextInt(9);
 			}
 		}
 
 		SudokuBoard randomBoard = new SudokuBoard(newBoard);
 		
 		return randomBoard; 
 	}
 	
	private void digging (int number){
		//Random random = new Random();
		
		int c = 0;
		int depth = 0;
		
		while(depth < number){
			int a = randomNumber();
			int b = randomNumber();
			if(board.getCellNum(a, b) == null || !board.getCellNum(a, b).equals(0)) {
				if(board.getCellNum(a, b) == null){
					c=0;
				}
			}
		}
		
		
	}
	
	private int randomNumber(){
		Random random = new Random();
		return random.nextInt(9);
	}
 	
 	
 }
