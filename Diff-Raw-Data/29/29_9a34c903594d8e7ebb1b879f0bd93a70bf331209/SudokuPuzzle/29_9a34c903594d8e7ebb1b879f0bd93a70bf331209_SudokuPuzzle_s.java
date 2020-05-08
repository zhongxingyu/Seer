 /**
  * Class to hold the puzzle data structure and methods to operate on it
  */
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class SudokuPuzzle {
 
 	public static int count;
 	
 	private Cell[][] _puzzle;
 	
 	/**
 	 * Default constructor
 	 */
 	public SudokuPuzzle() {
 		count = 9 * 9;
 		_puzzle = new Cell[9][9];
 	}
 	
 	/**
 	 * Constructor to initialize with values from a given file
 	 * 
 	 * @param filename
 	 */
 	public SudokuPuzzle(String filename) {
 		count = 9 * 9;
 		_puzzle = new Cell[9][9];
 		try {
 			Scanner scan = new Scanner(new File(filename));
 			scan.useDelimiter(",");
 			for (int x=0; x < 9; x++) {
 				for (int y=0; y < 9; y++) {
 					if (scan.hasNextInt()) {
 						int num = scan.nextInt();
 						_puzzle[x][y] = new Cell(x, y, num);
 					}
 				}
 				scan.nextLine();
 			}
 		} catch (Exception e) {
 			System.out.println(e.toString());
 		}
 	}
 	
 	/**
 	 * Function to get the specified row
 	 * 
 	 * @param row
 	 * @return
 	 */
 	public Cell[] getRow(int row) {
		return _puzzle[row];
 	}
 	
 	/**
 	 * Function to get the specified column
 	 * 
 	 * @param col
 	 * @return
 	 */
 	public Cell[] getCol(int col) {
 		Cell[] column = new Cell[9];
 		for (int x=0; x < 9; x++) {
 			column[x] = _puzzle[x][col];
 		}
 		return column;
 	}
 
 	/**
 	 * 
 	 * @param quad
 	 * @return
 	 */
 	public Cell[] getQuadrant(int quad) {
 		Cell[] quadrant = new Cell[9];
 		int i = quad / 3;
 		int j = quad % 3;
 		int index = 0;
 		for (int x=i*3; x < i*3+3; x++) {
 			for (int y=j*3; y < j*3+3; y++) {
 				quadrant[index] = _puzzle[x][y];
 				index++;
 			}
 		}
 		return quadrant;
 	}
 	
 	/**
 	 * Solve the puzzle
 	 */
 	public void solve() {
 		
 		// hint generation
 		boolean hintGen = true;
 		
 		while( hintGen && count != 0 ) {
 			hintGen = hintGenerator();
 		}
 		
 		if( count == 0 ) {
 			System.out.println( "Win!" );
 		}
 	}
 	
 	public boolean hintGenerator() {
 		
 		boolean changed = false;
 		
 		for( int i = 0; i < _puzzle.length; i++ ) {
 			for( int j = 0; j < _puzzle[ 0 ].length; j++ ) {
 				if( _puzzle[ i ][ j ].getValue() == 0 ) {
 					Cell[] row = getRow( _puzzle[ i ][ j ].getX() );
 					Cell[] col = getCol( _puzzle[ i ][ j ].getY() );
 					Cell[] qad = getQuadrant( _puzzle[ i ][ j ].getPos() );
 					for( int k = 0; k < row.length; k++ ) {
 						if( row[ k ].getValue() != 0 ) {
 							if (_puzzle[ i ][ j ].removeHint( row[ k ].getValue() )) {
 								changed = true;
 							}
 						}
 						if( col[ k ].getValue() != 0 ) {
 							if (_puzzle[ i ][ j ].removeHint( col[ k ].getValue() )) {
 								changed = true;
 							}
 						}
 						if( qad[ k ].getValue() != 0 ) {
 							if (_puzzle[ i ][ j ].removeHint( qad[ k ].getValue() )) {
 								changed = true;
 							}
 						}
 					}
 				}
 			}
 		}
 		return changed;
 	}
 	
 	/**
 	 * Print the contents of the puzzle
 	 */
 	public void printPuzzle() {
 		StringBuilder sb = new StringBuilder();
 		for (int x=0; x < 9; x++) {
 			for (int y=0; y < 9; y++) {
 				sb.append(_puzzle[x][y].getValue());
 				sb.append(" ");
 				if (y % 3 == 2) {
 					sb.append(" ");
 				}
 			}
 			sb.append("\n");
 			if (x % 3 == 2) {
 				sb.append("\n");
 			}
 		}
 		System.out.print(sb.toString());
 	}
 
 	/**
 	 * Row and Column Checker
 	 * 
 	 * collect any hints that is not shared by any other.
 	 * if hint is not shared by any other, it is the answer/Value for its owner.
 	 */
 	public void RCChecker( Cell [] col ){
 		// index of these array represent the hint with the value of index+1
 		int [] hintCounter = new int[9]; // counter for hint
 		Cell [] owner = new Cell[9]; // first encountered ownere for hint
 
 		for (int i=0; i<9; i++){ //iterate through the cells in col
 			if ( col[i].getValue()>0 ) continue; // skip cells with answer set already
 
 			ArrayList<Integer> hints = col[i].getHints(); // get the list of hints in 
 
 			for(int j =0; j<hints.size(); j++){ 
 				int hintVal = hints.get(j) - 1;
 				if( owner[hintVal] == null ) owner[hintVal] = col[i] ; // record a first seen hints owner
 				hintCounter[ hintVal ]++ ;  // for every hint seen increment its counter

 			}
 		}
 		
 		// go through hintCounter for any singlely owned hint(s)
 		for (int i=0; i<9; i++){
 			if (hintCounter[i] == 1) {
				owner[i].setValue( i );
 				//debugger
 				System.out.println(col[i].toString());
 			}
 		}
 	}
 	
 }
