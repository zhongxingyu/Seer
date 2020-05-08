 
 public class SequentialSudoku {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		SudokuPuzzle puzzle = new SudokuPuzzle("hintGenNoSolve.txt");
 		puzzle.printPuzzle();
 		long start = System.currentTimeMillis();
 		puzzle.solve();
 		long stop = System.currentTimeMillis();
 		puzzle.printPuzzle();
 		
 		System.out.println((stop - start) + " msec");
 	}
 
 }
