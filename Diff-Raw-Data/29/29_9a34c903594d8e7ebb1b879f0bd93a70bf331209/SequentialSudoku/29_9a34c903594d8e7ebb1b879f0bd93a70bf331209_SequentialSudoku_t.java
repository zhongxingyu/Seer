 
 public class SequentialSudoku {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		SudokuPuzzle puzzle = new SudokuPuzzle("hintGenNoSolve.txt");
 		puzzle.printPuzzle();
 		long start = System.currentTimeMillis();
 		puzzle.solve();
		puzzle.checkRowsCols();
		
		// Both of the following lines cause duplicates in the puzzle after being run
		//puzzle.solve();
		//puzzle.checkRowsCols();
 		long stop = System.currentTimeMillis();
 		puzzle.printPuzzle();
 		
 		System.out.println((stop - start) + " msec");
 	}
 
 }
