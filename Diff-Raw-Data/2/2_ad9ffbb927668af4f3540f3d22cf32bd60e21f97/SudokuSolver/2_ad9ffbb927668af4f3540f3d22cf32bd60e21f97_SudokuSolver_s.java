 import java.io.FileNotFoundException;
 import java.io.IOException;
 import javax.swing.*;
 
 /**
  * SudokuSolver.java
  * Solves all given sudokus and prints the progress (since it can take a 
  * while), number of puzzles, percentage solved, invalid puzzles, time total 
  * and per puzzle.
  * 
  * @version 1.0
  * @author Jouke van der Maas & Koen Keune
  * 
  */
 public class SudokuSolver {
 
 	/**
 	 * Main method of the application. Initiates the gui or
 	 * batch mode.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		String filename = "";
 		int depth = 4;
 		boolean batchMode = false;
 		
 		if(args.length > 0) {
 			filename = args[0];
 		}
 		
 		for(int i = 1; i < args.length; i++) {
 			String arg = args[i];
 			try {
 				int value = Integer.parseInt(arg);
 				depth = value;
 			} catch(NumberFormatException ex) {
 				if(arg.equals("-b")) {
 					batchMode = true;
 				} else {
 					errorExit("Syntax:\njava SudokuSolver <filename> [-b] [max depth]");
 				}
 					
 			}
 		}
 		
 		ForcingChainsStrategy.setMaxDepth(depth);
 		Loader loader = null;
 		try {
 			loader = Loader.loadPremadeFile(filename);
 		} catch (FileNotFoundException e) {
 			JOptionPane.showMessageDialog(null, "File not found.", "Error", JOptionPane.ERROR_MESSAGE);
 			System.exit(1);
 		}
 		
 		if(batchMode)
 			executeBatchMode(loader);
 		
 		JFrame gui = new JFrame();
 		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		gui.setSize(660, 500);
 		gui.setContentPane(new SudokuPanel(loader));
 		gui.setVisible(true);
 
 	}
 
 	/**
 	 * Executes batch mode, where the entire file is solved and statistics
 	 * are reported to the command line.
 	 * 
 	 * @param loader
 	 * Loader to read the sudokus from the specified file.
 	 */
 	private static void executeBatchMode(Loader loader) {
 		System.out.println("Starting batchmode\nThis might take a while.");
 		
 		int solved = 0;
 		int puzzles = 0;
 		int invalid = 0;
 		long totalTime = 0;
 		long end, start;
 
 		int[][] puzzle;
 
 		try {
 			puzzle = loader.getNext();		
 			while (puzzle != null) {
 				puzzles++;
 				System.out.print("\rSolving puzzle " + puzzles);
 				try {
 					Sudoku sudoku = new Sudoku(puzzle);
 					Solver solver = new Solver(sudoku);
 
 					start = System.currentTimeMillis();
 					solver.solve();
 					end = System.currentTimeMillis();
 					totalTime += end - start;
 					
 					if(sudoku.isSolved())
 						solved++;
 					
 				} catch(InvalidSudokuException ex) {
 					invalid++;
 				} catch(SudokuException ex) {
 					// count as not solved
 				}
 				puzzle = loader.getNext();
 			}
 		} catch (SudokuReaderException e) {
 			errorExit("Invalid file.");
 		} catch (IOException e) {
 			errorExit("Error reading the file.");
 		}
 		
 		if(puzzles == 0) {
 			errorExit("No puzzles found.");
 		}
 		System.out.printf("\n\nPuzzles: %d\nSolved: %d (%.1f%%)\nInvalid puzzles: %d\nTime: %.2fs (%.2fms average per puzzle)\n\n", 
 				puzzles, solved, (double)solved/puzzles * 100, invalid, (double)totalTime/1000, (double)totalTime/puzzles);
 		
 		System.exit(0);	
 	}
 	
 	/**
 	 * Prints the specified message to stderr and exits the program.
 	 * 
 	 * @param message
 	 * The message to print.
 	 */
 	private static void errorExit(String message) {
 		System.err.println("\n" + message);
 		System.exit(1);
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
