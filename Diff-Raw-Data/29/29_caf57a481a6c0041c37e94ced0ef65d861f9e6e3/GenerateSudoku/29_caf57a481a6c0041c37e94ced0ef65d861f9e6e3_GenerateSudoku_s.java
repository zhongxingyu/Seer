 import java.util.Random;
 
 /* This class is designed to generate sudokus */
 public class GenerateSudoku {
 	
 	/* Constants defined here for clarity */
 	private final int MAX_NUM_COLUMNS = 9;
 	private final int MAX_NUM_ROWS = 9;
 
 	/* Constructor */
 	public GenerateSudoku() { 
 		
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
 	
 	
 	
 }
