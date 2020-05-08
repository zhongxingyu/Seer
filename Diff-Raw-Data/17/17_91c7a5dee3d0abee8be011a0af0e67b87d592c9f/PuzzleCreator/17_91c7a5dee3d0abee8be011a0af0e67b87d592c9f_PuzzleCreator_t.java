 import java.util.Random;
 
 /**
  * Used to create unique puzzles to varying
  * difficulties.
  */
 public class PuzzleCreator {
 	/* ###################
 	   ##	CONSTANTS   ##
 	   ################### */
 	private final int SIZE = 9;			//The magic number of Sudoku
 	private final int BLOCK = 3;		//The number of blocks	
 	private final int NUM_GAMES = 2;	//The number of defined terminal solutions
 	private final int MAX_SWAPS = 5;	
 	private final int RAND_COLS = 2;
 	private final int ASCENDING = 0;
 	private final int DESCENDING = 1;
 	
 	/* ###################
 	   ##	SOLUTIONS   ##
 	   ################### */
 	private final int[][] SOLUTION_1 = {{4,9,2,5,3,8,1,6,7},
 										{1,5,7,9,6,2,3,4,8},
 										{3,6,8,4,1,7,2,5,9},
 										{5,8,6,3,2,4,9,7,1},
 										{9,3,1,7,8,6,4,2,5},
 										{7,2,4,1,5,9,6,8,3},
 										{8,4,3,2,9,5,7,1,6},
 										{2,1,5,6,7,3,8,9,4},
 										{6,7,9,8,4,1,5,3,2}};
 	
 	private final int[][] SOLUTION_2 = {{6,8,2,9,4,7,5,1,3},
 										{3,1,4,6,2,5,7,9,8},
 										{9,7,5,8,3,1,4,6,2},
 										{2,5,7,3,8,6,9,4,1},
 										{1,4,6,7,9,2,3,8,5},
 										{8,9,3,1,5,4,6,2,7},
 										{7,6,9,2,1,3,8,5,4},
 										{4,2,8,5,7,9,1,3,6},
 										{5,3,1,4,6,8,2,7,9}};
 	
 	/* ###################
 	   ##	  FIELDS    ##
 	   ################### */
 	private Puzzle puzzle;
 	private Puzzle solution;
 	private int[][] pGrid;
 	private int[][] solvedGrid;
 	private int[][] solutionGrid;
 	private int[][] other;
 	
 	/**
 	 * Default constructor
 	 */
 	public PuzzleCreator()
 	{
 		puzzle = new Puzzle();
 		solution = new Puzzle();
 		pGrid = new int[SIZE][SIZE];
 		solutionGrid = new int[SIZE][SIZE];
 		solvedGrid = new int[SIZE][SIZE];
 		other = new int[SIZE][SIZE];
 	}
 	
 	/* ###################
 	   ##	  PUBLIC    ##
 	   ################### */
 	
 	/**
 	 * A stand-alone function that causes the class to
 	 * create a new puzzle based off the given difficulty.
 	 * This function does NOT return a puzzle, only replaces
 	 * the current puzzles stored in this class	
 	 * @param difficulty
 	 */
 	public void generateNewPuzzle(Difficulty difficulty)
 	{
 		//create a solution
 		Random rand = new Random();
 		createSolution();
 		copySolution();
 		//get the number of 'givens'to leave based on the 
 		//difficulty entered
 		int diff;
 		
 		if (difficulty.equals(Difficulty.EASY)) diff = rand.nextInt(14) + 36;
 		else if (difficulty.equals(Difficulty.MEDIUM)) diff = rand.nextInt(4) + 32;
 		else if (difficulty.equals(Difficulty.HARD)) diff = rand.nextInt(4) + 28;
 		else if(difficulty.equals(Difficulty.EXPERT)) diff = rand.nextInt(4) + 24;
 		else diff = 50;		
 		//remove the squares from the puzzle board
 		if (difficulty.equals(Difficulty.EASY)) {
			System.out.println("Easy Selected");
//			digRandom(diff);
			digJumpOne(diff);
			System.out.println("Easy Complete");
 		}
 		else if (difficulty.equals(Difficulty.EXPERT)) {
			System.out.println("Expert Selected");
 			digOneByOne(diff);
			System.out.println("Expert Complete");
 		}
 		else {
			System.out.println("Medium - Hard Selected");
 			digJumpOne(diff);
			System.out.println("Medium - Hard Complete");
 		}
 		
 	}
 	
 	//Gets the LAST puzzle to be created, returning the inital 
 	//of the puzzle with blank cells fill with zero's
 	public Puzzle retreiveInitialPuzzleState()
 	{
 		return puzzle;
 	}
 	
 	//Gest the LAST puzzle to be created, returning the entire
 	//solution.
 	public Puzzle retreivePuzzleSolution()
 	{		
 		return solution;
 	}
 	
 	/* ###################
 	   ##	 PRIVATE    ##
 	   ################### */
 	
 	/**
 	 * Removes a set amount of squares, from left to right, top to bottom.
 	 * To be used for expert difficulty.
 	 * @param diff The number of givens.
 	 */
 	private void digOneByOne (int diff) {
 		int removeCount = 0;
 		int original;
 		int startCol = 0;
 		int startRow = 0;
 		int loop = 0;
 		
 		boolean completed = false;
 //		copyGrid(pGrid,solvedGrid);
 		while (!completed) {
 			for (int row = startRow; row < SIZE; row++) {
 				for (int col = startCol; col < SIZE; col++) {
 					if (pGrid[col][row] != 0 && removeCount < (81 - diff)) {
 						copyGrid(pGrid,solvedGrid);
 //						original = solvedGrid[col][row];
 						solvedGrid[col][row] = 0;
 						if (hasUniqueSolution()) {
 							pGrid[col][row] = 0;
 //							puzzle.set(col, row, 0);
 							removeCount++;
 							//TODO: debugging
 							System.out.println("removed " + removeCount + "out of " + (81 - diff));
 						}
 						
 //						else solvedGrid[col][row] = original;
 						startCol = 0;
 					}
 				}
 			}
 			loop++;
 			if (removeCount == (81 - diff)) {
 				completed = true;
 				setPuzzleGrid(pGrid);
 			}
 			else {
 				System.out.println("Reset");
 				removeCount = 0;
 				copyGrid(solutionGrid,pGrid);
 				if (startCol < SIZE){
 					
 					startCol = loop % SIZE;
 					System.out.println("col " + startCol);
 				}				
 				else  {
 					startCol = 0;
 					if(startRow < SIZE) startRow++;
 					System.out.println("row " + startRow);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Removes a set amount of squares, skipping one square as it iterates.
 	 * To be used on Medium difficulty.
 	 * @param diff
 	 */
 	private void digJumpOne (int diff) {
 		Random rand = new Random();
 		int startCol = 0;
 		int startRow = 0;
 		int removeCount = 0;
 //		int colValue = 0;
 		while (removeCount < (81 - diff)) {
 			for (int row = startRow; row < SIZE; row++) {
 				for (int col = startCol; col < SIZE; col+= 2) {
 //					colValue = col;
 					if(col < SIZE) {
 						if (pGrid[col][row] != 0 && removeCount < (81-diff)) {
 							copyGrid(pGrid,solvedGrid);
 							solvedGrid[col][row] = 0;
 							if (hasUniqueSolution()) {
 								pGrid[col][row] = 0;
 								puzzle.set(col, row, 0);
 								removeCount++;
 								//TODO debugging
 								System.out.println("removed " + removeCount + "out of " + (81 - diff));
 							}
 						}
 					}
 				}
 				startCol = rand.nextInt(2);
 			}			
 			startCol = rand.nextInt(SIZE);
 			startRow = rand.nextInt(SIZE);
 		}
 	}
 	
 	/**
 	 * Remove a set amount of squares from the solution at random locations.
 	 * Ensuring that a unique solution is maintained. This technique should
 	 * be used only for the easy difficulty.
 	 * @param diff The number of squares to remove.
 	 */	
 	private void digRandom (int diff) {
 		Random rand = new Random();
 		int row,col;
 		for (int i = 81; i >= diff; i--) {
 			boolean removed = false;
 			while (!removed) {
 				col = rand.nextInt(SIZE);
 				row = rand.nextInt(SIZE);
 				if (pGrid[row][col] != 0) {
 					copyGrid(pGrid,solvedGrid);
 					solvedGrid[row][col] = 0;
 					if (hasUniqueSolution()) {
 						pGrid[col][row] = 0;
 						puzzle.set(col, row, 0);
						//TODO: debugging
						System.out.println("removed " + ((81 - i)+1 ) + " out of " + (81 - diff));
					
 						removed = true;
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Checks if the current Sudoku puzzle has a unique solution.
 	 * @return Returns true if there is only one solution, false otherwise.
 	 */
 	private boolean hasUniqueSolution () {
 		boolean unique = false;
 		copyGrid(solvedGrid,other);
 		if (solver(0,0,ASCENDING,solvedGrid) && solver(0,0,DESCENDING,other)) {
 			//solver(0,0,ASCENDING,solvedGrid);
 			//solver(0,0,DESCENDING,other);
 			if (gridsEqual(solvedGrid,other)) {
 				if (gridsEqual(solvedGrid,solutionGrid)) {
 					//TODO: debugging
 //					System.out.println("UNIQUE");
 					unique = true;
 				}
 			}	
 		}
 		return unique;		
 	}
 	/**
 	 * Solves a Sudoku puzzle.
 	 * Recursively solves a sudoku puzzle by iterating through numbers
 	 * 1 - 9, in ascending or descending order.	
 	 * @param col The column to start from.
 	 * @param row The row to start from.
 	 * @param direction Iterate through the numbers in this direction.
 	 * @param grid The Sudoku grid.
 	 * @return Returns true if there is a complete solution, false otherwise.
 	 */
 	private boolean solver (int col, int row, int direction, int[][] grid) {
 		
 		if (col == SIZE) {
 			col = 0;
 			row++;
 			if (row == SIZE)
 				return true;
 		}
 		// if the current cell has a number
 		if (grid[col][row] != 0)			
 			return solver(col + 1,row,direction,grid);
 		// iterate from 1-9 
 		if (direction == ASCENDING) {
 			for (int i = 1; i <= SIZE; i++) {
 				if (legal(col,row,i,grid)) {
 					grid[col][row] = i;
 					if (solver(col + 1,row,direction,grid))				
 						return true;				
 				}
 			}			
 		}
 		// iterate from 9-1
 		if (direction == DESCENDING){
 			for (int i = SIZE; i > 0; i--) {
 				if (legal(col,row,i,grid)) {
 					grid[col][row] = i;
 					if (solver(col + 1,row,direction,grid))				
 						return true;				
 				}
 			}
 		}
 		grid[col][row] = 0;
 		return false;
 	}
 	
 	/**
 	 * Copy one 2D integer array to another of the same size.
 	 * @param source The array to make a copy of.
 	 * @param destination The copy.
 	 */
 	private void copyGrid (int[][]source, int[][]destination) {
 		for (int row = 0; row < SIZE; row++) {
 			for (int col = 0; col < SIZE; col++) {
 				destination[row][col] = source[row][col];
 			}
 		}
 	}
 	
 	private void setPuzzleGrid (int[][]newGame) {
 		for (int i = 0; i < 9; i++) {
 			for (int j = 0; j < 9; j++) {
 				puzzle.set(i, j, newGame[i][j]);
 
 			}
 		}
 	}
 	/**
 	 * Check if two grids are equal.
 	 * @param g1 Grid 1
 	 * @param g2 Grid 2
 	 * @return Returns true if the two grids are equal, false otherwise.
 	 */
 	private boolean gridsEqual (int[][] g1, int[][] g2) {
 		boolean equal = true;
 		for (int row = 0; row < SIZE; row++) {
 			for (int col = 0; col < SIZE; col++) {
 				if (g1[row][col] != g2[row][col])
 					equal = false;
 			}
 		}
 		return equal;
 	}
 	
 	/**
 	 * Checks if a number entered is valid.
 	 * Checks the the column, row and region constraints.
 	 * @param col The column number.
 	 * @param row The row number.
 	 * @param val The value to check.
 	 * @param grid The Sudoku grid.
 	 * @return Returns true if the move is valid, false otherwise.
 	 */
 	private boolean legal(int col, int row, int val,int[][] grid) {
         //Check the row
 		boolean valid = true;
 		for (int i = 0; i < SIZE; ++i) {  
             if (val == grid[col][i])
                 valid = false;
 		}
         //Check the column
         for (int i = 0; i < SIZE; ++i){ 
             if (val == grid[i][row])
                 valid = false;
         }
         //Check the 3x3 region
         int boxRowOffset = (col / 3)*3;
         int boxColOffset = (row / 3)*3;
         for (int i = 0; i < 3; ++i) {
             for (int j = 0; j < 3; ++j) {
                 if (val == grid[boxRowOffset+i][boxColOffset+j])
                     valid = false;
             }
         }    
         return valid;     
 	}
 		
 	/**
 	 * Copy the solution to the puzzle.
 	 */
 	private void copySolution () {
 		for (int row = 0; row < SIZE; row++) {
 			for (int col = 0; col < SIZE; col++) {
 				puzzle.set(col, row, solution.get(col, row));
 			}
 		}
 	}
 	
 	private void createSolution () {
 		int[][] newGame;
 		// Get a solution from a random number MOD 
 		// the number of predefined solutions
 		Random rand = new Random();		
 		int gameSelect = rand.nextInt(NUM_GAMES);
 		if (gameSelect == 0) {
 			newGame = SOLUTION_1;
 		}
 		else {
 			newGame = SOLUTION_2;
 		}
 		newGame = transformGrid(newGame);
 		//TODO: this if statement is just for debugging
 		if (!validGame(newGame)) {
 			System.out.println("Error");
 		}		
 		else {
 			for (int i = 0; i < 9; i++) {
 				for (int j = 0; j < 9; j++) {
 					solution.set(i, j, newGame[i][j]);
 
 				}
 			}
 			
 		}
 		for (int k = 0; k < 9; k++) {
 			for (int j = 0; j < 9; j++) {
 				pGrid[k][j] = newGame[k][j];
 				solutionGrid[k][j] = newGame[k][j];
 			}
 		}
 		//pGrid = newGame.clone();
 		//solutionGrid = newGame.clone();
 	}
 	
 	/**
 	 * Change the puzzle pattern using the four types of
 	 * propagation.
 	 * @param grid The Solution to transform.
 	 * @return A new random solution.
 	 */
 	private int[][] transformGrid (int[][] grid) {
 		// Create a random number generator
 		Random generator = new Random();		
 		// Propagation 1: The mutual exchange of two digits.
 		int numSwaps = generator.nextInt(MAX_SWAPS);
 		for (int i = 0; i <= numSwaps; i++) {
 			// randomise the numbers to swap
 			swapDigits(grid,generator.nextInt(8) + 1,generator.nextInt(8) + 1);
 		}
 		// Propagation 2: The mutual exchange of two columns in the same block
 		numSwaps = generator.nextInt(3);
 		for (int i = 0; i <= numSwaps; i++) {		
 			swapColumns(grid,generator.nextInt(RAND_COLS) * 3, 
 						generator.nextInt(RAND_COLS),generator.nextInt(RAND_COLS));
 		}
 		// Propagation 3: The mutual exchange of two blocks of columns
 		swapColumnBlocks(grid,generator.nextInt(RAND_COLS)*3,
 							generator.nextInt(RAND_COLS) * 3);
 		
 		return grid;
 	}
 	/**
 	 * Check that the given grid is a valid Sudoku Game.
 	 * This function checks the three basic rules of
 	 * Sudoku; that there are no duplicates in the rows,
 	 * columns and 3x3 regions.
 	 * @param grid
 	 * @return
 	 */
 	public boolean validGame (int[][] grid) {
 		boolean allValid = true;
 		if (!validRegions(grid)) {
 			allValid = false;
 		}
 		else if (!validColumns(grid)) {
 			allValid = false;
 		}
 		else if (!validRows(grid)) {
 			allValid = false;
 		}
 		return allValid;
 	}
 	/**
 	 * Check that all 3x3 regions have unique values. 
 	 * @param grid
 	 * @return
 	 */
 	private boolean validRegions (int[][] grid) {
 		boolean allValid = true;
 		for (int row = 0; row < 7; row += 3) {
 			for (int col = 0; col < 7; col += 3) {
 				boolean[] valid = new boolean[SIZE];
 				for (int x = 0; x < 3; x++) {
 					for (int y = 0; y < 3; y++) {
 						if (!valid[grid[col + x][row + y] - 1]) {
 							valid[grid[col + x][row + y] - 1] = true;
 						}
 						else { 
 							allValid = false;
 						}
 					}
 				}
 			}			
 		}
 		return allValid;
 	}
 	/**
 	 * Check that all columns have unique values
 	 * @return
 	 */
 	private boolean validColumns (int[][] grid) {
 		boolean allValid = true;
 		for (int col = 0; col < SIZE; col++) {
 			boolean[] valid = new boolean[SIZE];
 			for (int row = 0; row < SIZE; row++) {
 					if (!valid[grid[col][row] - 1]) {
 						valid[grid[col][row] - 1] = true;
 					}
 					else {
 						allValid = false;
 					}
 //				}
 			}
 		}		
 		return allValid;
 	}
 	/**
 	 * Check that all rows have unique values.
 	 * @param grid
 	 * @return
 	 */
 	private boolean validRows (int[][] grid) {
 		boolean allValid = true;
 		for (int row = 0; row < SIZE; row++) {
 			boolean[] valid = new boolean[SIZE];
 			for (int col = 0; col < SIZE; col++) {
 				if (!valid[grid[col][row] - 1]) {
 					valid[grid[col][row] - 1] = true;
 				}
 				else {
 					allValid = false;
 				}
 			}
 		}		
 		return allValid;
 	}
 	/**
 	 * Mutual exchange of two columns in the same block of columns.
 	 * @param grid
 	 * @param colBlock Must be in {0, 3, 6}.
 	 * @param colA Must be in the range of [0, 2].
 	 * @param colB Must be in the range of [0, 2].
 	 */
 	private void swapColumns (int[][] grid, int colBlock, int colA, int colB) {
 		int temp;
 		if (colA != colB) {
 			for (int row = 0; row < SIZE; row++) {
 				temp = grid[colBlock + colA][row];
 				grid[colBlock + colA][row] = grid[colBlock + colB][row];
 				grid[colBlock + colB][row] = temp;				
 			}
 		}
 	}
 	/**
 	 * Mutual exchange of two blocks of columns.
 	 * @param grid
 	 * @param blockA Must be in {0,3,6}.
 	 * @param blockB Must be in {0,3,6}.
 	 */
 	private void swapColumnBlocks (int[][]grid, int blockA, int blockB) {
 		if (blockA != blockB) {
 			// create a temp array 3x9
 			int[][] temp = new int[BLOCK][SIZE];
 			// copy blockA into temp
 			for (int col = blockA; col < blockA + BLOCK; col++) {
 				for (int row = 0; row < SIZE; row++) {
 					temp[col % BLOCK][row] = grid[col][row];
 				}
 			}
 			// blockA = blockB
 			for (int col = 0; col < BLOCK; col++) {
 				for (int row = 0; row < SIZE; row++) {
 					grid[col + blockA][row] = grid[col + blockB][row];
 				}
 			}
 			// blockB = temp
 			for (int col = 0; col < BLOCK; col++) {
 				for (int row = 0; row < SIZE; row++) {
 					grid[col + blockB][row] = temp[col][row];
 				}
 			}
 		}		
 	}
 	
 	/**
 	 * Mutual exchange of two digits.
 	 * @param numA
 	 * @param numB
 	 */
 	private void swapDigits (int[][] grid, int numA, int numB) {
 		//TODO: Delete boxIndex - just used for conceptualising 
 		/*
 		int[][] boxIndex = {{0,0},{3,0},{6,0},
 							{0,3},{3,3},{6,3},
 							{0,6},{3,6},{6,6}};
 		*/
 		for (int row = 0; row < 7; row += 3) {
 			for (int col = 0; col < 7; col += 3) {
 				for (int x = 0; x < 3; x++) {
 					for (int y = 0; y < 3; y++) {
 						if (grid[col + x][row + y] == numA) {
 							grid[col + x][row + y] = numB;
 						}
 						else if (grid[col + x][row + y] == numB) {
 							grid[col + x][row + y] = numA;
 						}
 					}
 				}
 			}			
 		}
 	}
 	
 	/* ###################
 	   ##	  DEBUG     ##
 	   ################### */
 	
 	//TODO: THIS IS FOR DEBUGGING PURPOSES ONLY
 	// DELETE BEFORE SUBMISSION
 	public void printPuzzle (Puzzle puz) {
 		for (int i = 0; i < 9; i++) {
 			for (int j = 0; j < 9; j++) {
 				System.out.print("" + puz.get(j, i) + " ");
 			}
 			System.out.print("\n");
 		}
 	}
 	
 	@SuppressWarnings("unused")
 	private void printGrid (int[][] grid) {
 		for (int i = 0; i < 9; i++) {
 			for (int j = 0; j < 9; j++) {
 				System.out.print("" + grid[i][j] + " ");
 			}
 			System.out.print("\n");
 		}
 		System.out.print("\n");
 	}
 	
 }
