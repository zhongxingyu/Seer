 
 /* This class defines a plain sudoku board for values to be added */
 public class SudokuBoard {
 
 	/* Constants defined here for clarity */
 	private int sBoard[][];		// this represents the 9x9 board
 	private static final int MAX_NUM_COLUMNS = 8;
 	private static final int MAX_NUM_ROWS = 8;
 	
 	/* Constructor */
<<<<<<< HEAD
 	/*
 	 * This one creates a new board
 	 */
 	public SudokuBoard() { 
 		sBoard = new int[9][9];
 	}
 	
 	/*
 	 * This one constructs a board with a give board
 	 */
 	public SudokuBoard(int board[][]) { 
 		
 		this.sBoard = board;
 	}
 	
 	/* Methods */
 	
 	/**
 	 * Prints the sudoku board
 	 */
	public void printBoard() {
=======
	public SudokuBoard () { 
		// initialising the board
		this.board = new int[8][8];
		
	}
	
 	public void printBoard () {
>>>>>>> 63e0989f94848d3b6767a2dc70973d86cdf2d323
 		
 		for (int i = 0; i < sBoard.length; i++) {
 			for (int j = 0; j < sBoard[i].length; j++) {
 				System.out.print(sBoard[i][j]);
 				System.out.println(" ");
 			}
 		}
 	}
<<<<<<< HEAD
 	
 	public int[][] getBoard() { 
 		
 		return sBoard;
 	}
	
	
=======
>>>>>>> 63e0989f94848d3b6767a2dc70973d86cdf2d323
 }
