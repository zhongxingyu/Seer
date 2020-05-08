 import java.util.LinkedList;
 import java.util.Scanner;
 
 
 /**
  * @author Steven 
  * Holds the current board and evaluation state required to solve a puzzle
  */
 
 //TODO STEVEN I have commented out a couple of bits to get it to compile, they are marked with TODO - laura
 
 public class SudokuState {
 		private int board[][];
 		private int temp[][][];
 		private static final int BOARD_SIZE = 9;
 		private static final int SECTION_SIZE = 3;
 		private int cellsFilled;
 		private static final int EMPTY = -1;
 		private static final int NOT_POSSIBLE = -10;
 		private static final int POSSIBLE = -100;
 		private static final int SATISFIES_ENOUGH = 4;
 		
 		/** 
 		 * Constructs and initializes the board state given a 2D integer array of values with empty cells 
 		 * represented by -1.
 		 * @param inputBoard 2D integer array of board state.
 		 */
 		public SudokuState(int inputBoard[][]) {
 			// Initialize Board
 			this.board = new int[BOARD_SIZE][BOARD_SIZE];
 			this.temp = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
 			this.board = inputBoard;
 			this.cellsFilled = 0;
 			for(int row = 0; row < BOARD_SIZE; row++) {
 				for(int col = 0; col < BOARD_SIZE; col++) {
 					// Initialize all possible temp values to be possible;
 					for(int i = 0; i < BOARD_SIZE; i++) {
 						this.temp[row][col][i] = POSSIBLE;	
 					}
 					if(this.board[row][col] != EMPTY) {
 						this.cellsFilled++;
 					}
 					debug(this.board[row][col]);
 				}
 				debug("");
 			}
 			this.updateTempVals();
 			this.updateCellsFilled();
 		}
 		
 		/**
 		 * Checks if current board is solved.
 		 */
 		public boolean solved() {
 			if(this.updateCellsFilled() == BOARD_SIZE * BOARD_SIZE && this.valid()) {
 				return true;
 			}
 			return false;
 		}
 
 		/**
 		 * Solves a given puzzle stored in this board state.
 		 */
 		public void solve() {
 			debug("Updating Temp Vals");
 			this.updateTempVals();
 			System.out.println("_______________");
 			this.print2DBoard(this.board);
 			if(this.solved()) {
 				debug("Is Solved and Valid");
 			} else {
 				debug("Is Not Solved or Valid");
 			}
 			while(this.solveMediumSteps() && this.pointingPairsAndTriplesAlgorithm()) {
 				this.printTemps();
 			}
 			this.printTemps();
 			
 			//this.solveMediumSteps();
 			System.out.println("This is Valid = " + this.valid());
 			System.out.println("This is Solved = " + this.solved());
 			if(this.solved()) {
 				debug("Is Solved and Valid");
 			} else {
 				debug("Is Not Solved or Valid");
 			}
 			
 			
 		}
 
 		/**
 		 * Solves for cells which have all but 1 number missing in a column, row or square.
 		 */
 //TODO This stuff prevented comilation so i commented it out - laura
 /*<<<<<<< HEAD
 		public boolean solveSimpleSteps() {
 			boolean stepsSolved = false;
 =======
 */
		public boolean solveSimpleSteps()
 		{
 //>>>>>>> b15af50f3288580a53cb4cba497a5c14d7e681e6
			boolean stepsSolved = false;
 			boolean cellsFilled = false;
 			for(int row = 0; row < BOARD_SIZE; row++){
 				for(int col = 0; col < BOARD_SIZE; col++) {
 					if(this.board[row][col] == EMPTY) {
 						int cellVal = this.getTempFinalVal(row, col);
 						if(cellVal != NOT_POSSIBLE) {
 							this.board[row][col] = cellVal;
 							this.updateCellsFilled();
 							this.updateTempVals();
 							cellsFilled = true;
						} 
 					}
 				}
 			}
 			if(cellsFilled) {
 				this.solveSimpleSteps();
 				//TODO I COMMENTED OUT THIS - laura
				stepsSolved = true;
 			}
 			//TODO AND THIS - laura
			return stepsSolved;
 			
 		}
 
 		/**
 		 * Checks all other rows and columns for possible values in order to evaluate if a cell can be filled.
 		 * E.g.
 		 * ---------------------------------------------
 		 * | 1 | - | | | - | 3 | - | - | - | - | - | - |
 		 * ---------------------------------------------
 		 * | 2 | - | - | - | - | - | - | - | 3 | - | - |
 		 * ---------------------------------------------
 		 * | x | | | | |   |   |   |   |   |   |   |   |
 		 * ---------------------------------------------
 		 * |   | 3 | | |
 		 * -------------
 		 * |   | | | | |
 		 * -------------
 		 * |   | | | | |
 		 * -------------
 		 * |   | | | 3 |
 		 *  x = 3 since all other rows and columns already have a 3.
 		 *  @return True if any cells were solved.
 		 */
 		public boolean solveMediumSteps() {
 			boolean stepsSolved = false;
 			boolean cellsFilled = false;
 			int sectionRow = 0;
 			int sectionCol = 0;
 			LinkedList<Integer> possibleCellVal = null;
 			LinkedList<Integer> allOtherPossibleCellVals = new LinkedList<Integer>();
 			boolean sameCell = false;
 			debug("Solving Simple Steps");
 			this.solveSimpleSteps();
 			debug("Solving Medium Steps");
 			// For each row and column
 			for(int row = 0; row < BOARD_SIZE; row++) {
 				for(int col = 0; col < BOARD_SIZE; col++) {
 					possibleCellVal = this.possibleVals(row, col);
 					if(this.getTempFinalVal(row, col) == NOT_POSSIBLE) {
 						// For each possible number for this cell.
 						for(Integer num : possibleCellVal) {
 							sectionRow = 3*(row/3);
 							sectionCol = 3*(col/3);
 							allOtherPossibleCellVals.clear();
 							for(int rowTranslate = 0; rowTranslate < SECTION_SIZE; rowTranslate++) {								
 								for(int colTranslate = 0; colTranslate < SECTION_SIZE; colTranslate++) {
 									int currentRow = sectionRow + rowTranslate;
 									int currentCol = sectionCol + colTranslate;
 									sameCell = ((currentRow == row) && (currentCol == col));
 									if(!sameCell) {
 										allOtherPossibleCellVals.addAll(this.possibleVals(currentRow, currentCol));
 									}
 								}
 							}
 							this.removeDuplicates(allOtherPossibleCellVals);
 							debug("Num = " + num + " (" + row + ", " + col + ") " + allOtherPossibleCellVals + !allOtherPossibleCellVals.contains(num));
 							
 							if(!allOtherPossibleCellVals.contains(num)) {
 								this.board[row][col] = num;
 								this.updateCellsFilled();
 								this.updateTempVals();
 								cellsFilled = true;
 							}
 						}
 					}
 				}
 			}
 			if(cellsFilled) {
 				this.solveMediumSteps();
 				stepsSolved = true;
 			}
 			this.solveSimpleSteps();
 			return stepsSolved;
 		}
 		
 		public void solveHardSteps() {
 		}
 		
 		/**
 		 * http://www.sudokuwiki.org/Intersection_Removal 
 		 */
 		public boolean pointingPairsAndTriplesAlgorithm() {
 			debug("Pointing pairs algorithm ************");
 			boolean cellsFilled = true;
 			this.updateBoardVals();
 			this.updateTempVals();
 			
 				System.out.println("Pointing pairs algorithm ************");
 				this.solveMediumSteps();
 				cellsFilled = false;
 				for(int row = 0; row < this.BOARD_SIZE; row++) {
 					for(int col = 0; col < this.BOARD_SIZE; col++) {
 						int section = this.getSection(row, col);
 						int sectionFirstRow = this.getSectionFirstRow(section); 
 						int sectionFirstCol = this.getSectionFirstCol(section);
 						// Current Cell is First Cell in a Section
 						LinkedList<Integer> sectionRowPossibleVals = this.getSectionRowPossibleVals(row, col);
 						LinkedList<Integer> sectionColPossibleVals = this.getSectionColPossibleVals(row, col);
 						// Get all possible values of this section except this cell.
 						LinkedList<Integer> sectionPossibleVals = this.getSectionPossibleVals(row, col);
 						LinkedList<Integer> rowPossibleVals = this.getRowPossibleVals(row, col);
 						LinkedList<Integer> colPossibleVals = this.getColPossibleVals(row, col);
 						//this.printTempsSection(section);
 						for(int num = 1; num <= this.BOARD_SIZE; num++) {
 							int numOccurrencesInSection = this.numberOccurances(num, sectionPossibleVals);
 							int numOccurrencesInSectionRow = this.numberOccurances(num, sectionRowPossibleVals);
 							int numOccurrencesInSectionCol = this.numberOccurances(num, sectionColPossibleVals);
 							int numOccurrencesInRow = this.numberOccurances(num, rowPossibleVals);
 							int numOccurrencesInCol = this.numberOccurances(num, colPossibleVals);
 							boolean pairOrTripleInSection = (numOccurrencesInSection == 2 || numOccurrencesInSection == 3); 
 							boolean pairOrTripleInSectionRow = (numOccurrencesInSectionRow == 2 || numOccurrencesInSectionRow == 3);
 							boolean pairOrTripleInSectionCol = (numOccurrencesInSectionCol == 2 || numOccurrencesInSectionCol == 3);
 							boolean pairOrTripleInRow = (numOccurrencesInRow == 2 || numOccurrencesInRow == 3);
 							boolean pairOrTripleInCol = (numOccurrencesInCol == 2 || numOccurrencesInCol == 3);
 							// A Pair or Triple in a box - if they are aligned on a row, n can be removed from the rest of the row.
 							//                           - if they are aligned on a column, n can be removed from the rest of the column.
 							//debug("# of " + num + " in section " + section + " is " + numOccurrencesInSection + " with " + numOccurrencesInSectionRow + " in section row " + row + " and " + numOccurrencesInSectionCol + " in section col " + col);
 							if(row == sectionFirstRow && col == sectionFirstCol) {
 								// A Pair or Triple in box
 								if(pairOrTripleInSection) {
 									//debug("# of " + num + " in section " + section + " is " + numOccurrencesInSection + " with " + numOccurrencesInSectionRow + " in row " + row + " and " + numOccurrencesInSectionCol + " in col " + col);
 									if(numOccurrencesInSectionRow == numOccurrencesInSection) {
 										debug("# in section = # in row");
 										debug("pairOrTriple in section row " + row);
 										debug("pair Or Triple in Section = " + pairOrTripleInSection + " in sectionRow = " + pairOrTripleInSectionRow + " in sectionCol = " + pairOrTripleInSectionCol);
 										debug("Num is " + num + " vals in section " + sectionPossibleVals + " vals in section row " + sectionRowPossibleVals + " vals in section col " + sectionColPossibleVals);
 										for(int currentCol = 0; currentCol < this.BOARD_SIZE; currentCol++) {
 											// Check if currentCol is not contained within this section of the board.
 											if(this.getSection(row, currentCol) != section && this.possibleVals(row, currentCol).contains(num)) {
 												// Remove occurrences of this number from all columns in this row that are not in the same section.
 												
 												this.removeTempVal(num, row, currentCol);
 												if(!this.valid()) {
 													this.updateTempVals();
 												} else {
 													cellsFilled = true;
 													System.out.println("Removed possibility of " + num + " from (" + currentCol + ", " + row + ") From Section " + this.getSection(row, currentCol) + " due to Section " + section);
 													this.updateBoardVals();
 												}
 											}
 										}
 										debug("");
 										debug("");
 									}
 									if(numOccurrencesInSectionCol == numOccurrencesInSection) {
 										debug("pairOrTriple in section col " + col);
 										debug("pair Or Triple in Section = " + pairOrTripleInSection + " in sectionRow = " + pairOrTripleInSectionRow + " in sectionCol = " + pairOrTripleInSectionCol);
 										debug("Num is " + num + " vals in section " + sectionPossibleVals + " vals in section row " + sectionRowPossibleVals + " vals in section col " + sectionColPossibleVals);
 										//this.printTempsSection(section);
 										debug("# in section = # in col");
 										for(int currentRow = 0; currentRow < this.BOARD_SIZE; currentRow++) {
 											// Check if currentRow is not contained within this section of the board.
 											if(this.getSection(currentRow, col) != this.getSection(row, col) && this.possibleVals(currentRow, col).contains(num)) {
 												// Remove occurrences of this number from all rows in this column that are not in the same section.
 												
 												this.removeTempVal(num, currentRow, col);
 												if(!this.valid()) {
 													this.updateTempVals();
 												} else {
 													cellsFilled = true;
 													System.out.println("Removed possibility of " + num + " from (" + col + ", " + currentRow + ") From Section " + this.getSection(currentRow, col) + " due to Section " + section);
 													this.updateBoardVals();
 												}
 											}
 										}
 									}					
 									debug("");
 									debug("");
 								}
 							} 
 							// A Pair or Triple on a row - if they are all in the same box, n can be removed from the rest of the box.
 							if(numOccurrencesInSectionRow == numOccurrencesInRow && pairOrTripleInRow) {
 								
 								debug("A Pair or Triple on a row - if they are all in the same box = " + section + " row = " + row + " col = " + col);
 								for(int rowTranslate = 0; rowTranslate < this.SECTION_SIZE; rowTranslate++) {
 									for(int colTranslate = 0; colTranslate < this.SECTION_SIZE; colTranslate++) {
 										int currentRow = sectionFirstRow + rowTranslate;
 										int currentCol = sectionFirstCol + colTranslate;
 										if(currentRow != row && this.possibleVals(currentRow, currentCol).contains(num)) {
 											debug("# of " + num + " in row " + row + " is " + numOccurrencesInRow + " with " + numOccurrencesInSectionRow + " in section row " + row + " and " + numOccurrencesInSectionCol + " in section col " + col);
 											debug("pair Or Triple in Section = " + pairOrTripleInSection + " in sectionRow = " + pairOrTripleInSectionRow + " in sectionCol = " + pairOrTripleInSectionCol);
 											debug("Num is " + num + " vals in section " + sectionPossibleVals + " vals in section row " + sectionRowPossibleVals + " vals in section col " + sectionColPossibleVals);
 											
 											this.removeTempVal(num, currentRow, currentCol);
 											if(!this.valid()) {
 												this.updateTempVals();
 											} else {
 												System.out.println("Removed possibility of " + num + " from (" + currentCol + ", " + currentRow + ") ");
 												cellsFilled = true;
 												this.updateBoardVals();
 												debug("");
 											}
 											
 										}
 									}
 								}
 								debug("");
 								debug("");
 							} 
 							// A Pair or Triple on a column - if they are all in the same box, n can be removed from the rest of the box.
 							if(numOccurrencesInSectionCol == numOccurrencesInCol && pairOrTripleInCol) {
 								debug("# of " + num + " in section " + section + " is " + numOccurrencesInSection + " with " + numOccurrencesInSectionRow + " in row " + row + " and " + numOccurrencesInSectionCol + " in col " + col);
 								debug("A Pair or Triple on a col - if they are all in the same box = " + section + " row = " + row + " col = " + col);
 								for(int rowTranslate = 0; rowTranslate < this.SECTION_SIZE; rowTranslate++) {
 									for(int colTranslate = 0; colTranslate < this.SECTION_SIZE; colTranslate++) {
 										int currentRow = sectionFirstRow + rowTranslate;
 										int currentCol = sectionFirstCol + colTranslate;
 										if(currentCol != col && this.possibleVals(currentRow, currentCol).contains(num)) {
 											debug("# of " + num + " in col " + col + " is " + numOccurrencesInCol + " with " + numOccurrencesInSectionRow + " in section row " + row + " and " + numOccurrencesInSectionCol + " in section col " + col);
 											debug("pair Or Triple in Section = " + pairOrTripleInSection + " in sectionRow = " + pairOrTripleInSectionRow + " in sectionCol = " + pairOrTripleInSectionCol);
 											debug("Num is " + num + " vals in section " + sectionPossibleVals + " vals in section row " + sectionRowPossibleVals + " vals in section col " + sectionColPossibleVals);
 											
 											this.removeTempVal(num, currentRow, currentCol);
 											if(!this.valid()) {
 												this.updateTempVals();
 											} else {
 												System.out.println("Removed possibility of " + num + " from (" + currentCol + ", " + currentRow + ") ");
 												cellsFilled = true;
 												this.updateBoardVals();
 												debug("");
 											}
 										}
 									}
 								}
 							}
 							debug("");
 							debug("");
 							this.updateBoardVals();
 							this.updateTempVals();
 						}
 						//this.updateBoardVals();
 						//this.updateTempVals();
 					}
 				
 				//this.updateBoardVals();
 				//this.updateTempVals();
 			}
 			this.solveMediumSteps();
 			return cellsFilled;
 		}
 
 		
 		public void test() {
 			for(int row = 0; row < this.BOARD_SIZE; row++) {
 				for(int col = 0; col < this.BOARD_SIZE; col++) {
 					debug("Section is " + this.getSection(row, col));
 					this.printTempsSection(this.getSection(row, col));
 					debug("Possible Section Vals " + this.getSectionPossibleVals(row, col));
 					debug("Possible Section Row Vals " + this.getSectionRowPossibleVals(row, col));
 					debug("Possible Section Col Vals " + this.getSectionColPossibleVals(row, col));
 					debug("Possible Col Vals " + this.getColPossibleVals(row, col));
 					debug("Possible Row Vals " + this.getRowPossibleVals(row, col));
 				}
 			}
 		}
 		
 		
 		/**
 		 * Returns the number of occurrences of a single number within a list of integers.
 		 * @param number The number to count the occurrences of within the given list.
 		 * @param list The list of integers to search through.
 		 * @return The number of occurrences of a single number within a list of integers. 
 		 */
 		public int numberOccurances(int number, LinkedList<Integer> list) {
 			int numCounter = 0;
 			for(Integer currentNum : list) {
 				if(currentNum == number) {
 					numCounter++;
 				}
 			}
 			return numCounter;
 		}
 		
 		
 		/**
 		 * Returns section number from 1 to 9 as illustrated below:-
 		 * -------------
 		 * | 1 | 2 | 3 |
 		 * -------------
 		 * | 4 | 5 | 6 |
 		 * -------------
 		 * | 7 | 8 | 9 |
 		 * -------------
 		 * @param row
 		 * @param col
 		 * @return
 		 */
 		public int getSection(int row, int col) {
 			int sectionRow = 0;
 			int sectionCol = 0;
 			if(row < 3) {
 				sectionRow = 1;
 			} else if(row < 6 ) {
 				sectionRow = 2; 
 			} else if(row < 9) {
 				sectionRow = 3;
 			}
 			if(col < 3) {
 				sectionCol = 1;
 			} else if(col < 6) {
 				sectionCol = 2;
 			} else if(col < 9) {
 				sectionCol = 3;
 			}
 			return 3*(sectionRow-1) + sectionCol;
 		}
 
 		/** Returns the first column for a given section parsed
 		 * @param section Square of 3 by 3 cells contained on a Sudoku board labeled from 1-9 as shown below. <br/>
 		 * |1|2|3| <br/>
 		 * |4|5|6| <br/>
 		 * |7|8|9| <br/>
 		 * @return First column in a given section.
 		 */
 		public int getSectionFirstCol(int section) {
 			return (3 * ((section - 1) % 3));
 		}
 
 		/** Returns the first row for a given section parsed
 		 * @param section Square of 3 by 3 cells contained on a Sudoku board labeled from 1-9 as shown below. <br/>
 		 * |1|2|3| <br/>
 		 * |4|5|6| <br/>
 		 * |7|8|9| <br/>
 		 * @return First row in a given section.
 		 */
 		public int getSectionFirstRow(int section) {
 			return 3 * ((section - 1)/ 3);
 			
 		}
 		
 		/**
 		 * Returns list of all possible values for cells in this section of the row parsed.
 		 * @param row
 		 * @param col
 		 * @return
 		 */
 		public LinkedList<Integer> getSectionRowPossibleVals(int row, int col) {
 			int section = this.getSection(row, col);
 			int sectionFirstCol = this.getSectionFirstCol(section);
 			LinkedList<Integer> sectionRowTemps = new LinkedList<Integer>();
 			for(int colTranslate = 0; colTranslate < this.SECTION_SIZE; colTranslate++) {
 				int currentCol = sectionFirstCol + colTranslate;
 				if(this.getTempFinalVal(row, currentCol) == this.NOT_POSSIBLE) {
 					sectionRowTemps.addAll(this.possibleVals(row, currentCol));
 				}
 			}
 			return sectionRowTemps;
 		}
 		/**
 		 * Returns list of all possible values for cells in this section of the column parsed.
 		 * @param row
 		 * @param col
 		 * @return
 		 */
 		public LinkedList<Integer> getSectionColPossibleVals(int row, int col) {
 			int section = this.getSection(row, col);
 			int sectionFirstRow = this.getSectionFirstRow(section);
 			LinkedList<Integer> sectionColTemps = new LinkedList<Integer>();
 			for(int rowTranslate = 0; rowTranslate < this.SECTION_SIZE; rowTranslate++) {
 				int currentRow = sectionFirstRow + rowTranslate;
 				if(this.getTempFinalVal(currentRow, col) == this.NOT_POSSIBLE) {
 					sectionColTemps.addAll(this.possibleVals(currentRow, col));
 				}
 			}
 			return sectionColTemps;
 		}
 		
 		/**
 		 * Returns list of all possible values for cells in the column parsed.
 		 * @param row
 		 * @param col
 		 * @return
 		 */
 		public LinkedList<Integer> getColPossibleVals(int row, int col) {
 			LinkedList<Integer> sectionColTemps = new LinkedList<Integer>();
 			for(int currentRow = 0; currentRow < this.BOARD_SIZE; currentRow++) {
 				if(this.getTempFinalVal(currentRow, col) == this.NOT_POSSIBLE) {
 					sectionColTemps.addAll(this.possibleVals(currentRow, col));
 				}
 			}
 			return sectionColTemps;
 		}
 		
 		/**
 		 * Returns list of all possible values for cells in the row parsed.
 		 * @param row
 		 * @param col
 		 * @return
 		 */
 		public LinkedList<Integer> getRowPossibleVals(int row, int col) {
 			LinkedList<Integer> sectionRowTemps = new LinkedList<Integer>();
 			for(int currentCol = 0; currentCol < this.BOARD_SIZE; currentCol++) {
 				if(this.getTempFinalVal(row, currentCol) == this.NOT_POSSIBLE) {
 					sectionRowTemps.addAll(this.possibleVals(row, currentCol));
 				}
 			}
 			return sectionRowTemps;
 		}
 		
 		/**
 		 * Returns list of all possible values of cells contained within the section except those for the cell located at row and col.
 		 * @param row
 		 * @param col
 		 * @return
 		 */
 		public LinkedList<Integer> getSectionPossibleVals(int row, int col) {
 			int sectionFirstRow = 3*(row/3);
 			int sectionFirstCol = 3*(col/3);
 			int currentRow = 0;
 			int currentCol = 0;
 			LinkedList<Integer> sectionTemps = new LinkedList<Integer>();
 			for(int rowTranslate = 0; rowTranslate < this.SECTION_SIZE; rowTranslate++) {
 				for(int colTranslate = 0; colTranslate < this.SECTION_SIZE; colTranslate++) {
 					currentRow = sectionFirstRow + rowTranslate;
 					currentCol = sectionFirstCol + colTranslate;
 					// Exclude Final Values
 					if(this.getTempFinalVal(currentRow, currentCol) == this.NOT_POSSIBLE) {
 						sectionTemps.addAll(this.possibleVals(currentRow, currentCol));
 					}
 					
 				}
 			}
 			return sectionTemps;
 		}
 		
 		/**
 		 * Return the only possible value for a given cell given all other possibilities have been exhausted. 
 		 * @param row
 		 * @param col
 		 * @return
 		 */
 		public int getTempFinalVal(int row, int col) {
 			LinkedList<Integer> possibleCellVals = this.possibleVals(row, col);
 			if(possibleCellVals.size() == 1) {
 				return possibleCellVals.peek();
 			} 
 			return NOT_POSSIBLE;		
 		}
 
 		/**
 		 * Returns the number of filled (non empty) cells on the board
 		 */
 		public int getNumCellsFilled() {
 			return this.cellsFilled;
 		}
 
 		public boolean removeTempVal(int num, int row, int col) {
 			if(this.getTempFinalVal(row, col) == this.NOT_POSSIBLE) {
 				this.temp[row][col][num - 1] = this.NOT_POSSIBLE;
 				return true;
 			}
 			return false;
 		}
 
 		public void removeDuplicates(LinkedList<Integer> list) {
 			LinkedList<Integer> duplicateFreeList = new LinkedList<Integer>();
 			while(!list.isEmpty()) {
 				Integer currentElement = list.remove();
 				if(!duplicateFreeList.contains(currentElement)) {
 					duplicateFreeList.add(currentElement);
 				}
 			}
 			while(!duplicateFreeList.isEmpty()) {
 				list.add(duplicateFreeList.remove());
 			}
 		}
 		
 		/**
 		 * Eliminate the possibility for all temp values except that assigned by number.
 		 * @param number
 		 * @param row
 		 * @param col
 		 */
 		public void setTempFinalVal(int number, int row, int col) {
 			for(int num = 0; num < BOARD_SIZE; num++) {
 				if(number != (num + 1)) {
 					this.temp[row][col][num] = NOT_POSSIBLE;
 				} else {
 					this.temp[row][col][num] = POSSIBLE;
 				}
 			}
 		}
 	
 
 		public void debug(Object obj) {
 			//System.out.println(obj.toString());
 		}
 		/**
 		 * Provides linked list of possible values for a given cell at (row, col)
 		 * @param row Value from 0 to 9 representing the row number from top to bottom
 		 * @param col Value from 0 to 9 representing the column number from left to right
 		 * @return List of Integers of possible values for this cell.
 		 */
 		public LinkedList<Integer> possibleVals(int row, int col) {
 			LinkedList<Integer> possibleVals = new LinkedList<Integer>();
 			for(int number = 0; number < BOARD_SIZE; number++) {
 				if(this.temp[row][col][number] == POSSIBLE) {
 					possibleVals.add(number+1);
 				}
 			}
 			return possibleVals;
 		}
 		
 		
 		/**
 		 * Checks if a the current board state (all rows, columns and squares) have valid cell values.
 		 * @return
 		 */
 		public boolean valid() {
 			for(int row = 0; row < BOARD_SIZE; row++) {
 				for(int col = 0; col < BOARD_SIZE; col++) {
 					for(int number = 1; number <= BOARD_SIZE; number++) {
 						if(this.inColumn(number, col) > 1 || this.inRow(number, row) > 1 || this.inSection(number, row, col) > 1) {
 							System.out.println("row = " + row + " col = " + col);
 							return false;
 						}
 					}
 				}
 			}
 			debug("Is Valid");
 			return true;
 		}
 		
 		
 		/**
 		 * Sets all temp values in a given cell to be empty
 		 * @param row Value from 0 to 9 representing the row number from top to bottom
 		 * @param col Value from 0 to 9 representing the column number from left to right
 		 */
 		public void clearTemp(int row, int col) {
 			for(int i = 0; i < BOARD_SIZE; i++) {
 				this.temp[row][col][i] = POSSIBLE;
 			}
 		}
 		
 		/**
 		 * Updates and returns the number of filled cells in the current board state
 		 */
 		public int updateCellsFilled() {
 			int count = 0;
 			for(int row = 0; row < BOARD_SIZE; row++) {
 				for(int col = 0; col < BOARD_SIZE; col++) {
 					if(this.board[row][col] != this.EMPTY) {
 						count++;
 					}
 				}
 			}
 			this.cellsFilled = count;
 			return count;
 		}
 		
 		/**
 		 * Updates the value of temp cells based upon the filled cells in its given row, column and square.
 		 */
 		public void updateTempVals() {
 			for(int row = 0; row < BOARD_SIZE; row++) {
 				for(int col = 0; col < BOARD_SIZE; col++) {
 					int cellVal = this.board[row][col];
 					this.clearTemp(row, col);
 					if(cellVal != EMPTY && this.getTempFinalVal(row, col) == NOT_POSSIBLE) {
 						this.setTempFinalVal(cellVal, row, col);
 					} else {
 						for(cellVal = 1; cellVal <= BOARD_SIZE; cellVal++) {
 							if(!this.canBe(cellVal, row, col)) {
 								// Update possible temp vals
 								this.temp[row][col][cellVal-1] = NOT_POSSIBLE;
 							}
 						}
 					}
 				}
 			}
 		}
 		public boolean updateBoardVals() {
 			boolean updated = false;
 			for(int row = 0; row < BOARD_SIZE; row++) {
 				for(int col = 0; col < BOARD_SIZE; col++) {
 					if(this.getTempFinalVal(row, col) != NOT_POSSIBLE && this.board[row][col] == this.EMPTY) {
 						this.board[row][col] = this.getTempFinalVal(row, col);
 						updated = true;
 					}
 				}
 			}
 			return updated;
 		}
 		
 		/**
 		 * Returns the number of occurrences of a number in a given row.
 		 * @param number Number looked for in given row.
 		 * @param row Value from 0 to 9 representing the row number from top to bottom
 		 * @return 
 		 */
 		public int inRow(int number, int row) {
 			int answer = 0;
 			for(int tempCol = 0; tempCol < BOARD_SIZE; tempCol++) {
 				if(this.board[row][tempCol] == number) {
 					answer++;
 				}
 			}
 			return answer;
 		}
 		/**
 		 * Returns the number of occurrences of a number in a given row.
 		 * @param number Number looked for in given row.
 		 * @param col Value from 0 to 9 representing the column number from left to right
 		 * @return 
 		 */		
 		public int inColumn(int number, int col) {
 			int answer = 0;
 			for(int tempRow = 0; tempRow < BOARD_SIZE; tempRow++) {
 				if(this.board[tempRow][col] == number) {
 					answer++;
 				}
 			}
 			return answer;
 		}
 		
 		/**
 		 * Returns true if a number can be assigned to a cell without conflict
 		 * @param number
 		 * @param row Value from 0 to 9 representing the row number from top to bottom
 		 * @param col Value from 0 to 9 representing the column number from left to right
 		 * @return
 		 */
 		public boolean canBe(int number, int row, int col) {
 			if(this.inColumn(number, col) > 0 || this.inRow(number, row) > 0 || this.inSection(number, row, col) > 0) {
 				return false;
 			}
 			return true;
 		}
 		
 		/**
 		 * Returns number of occurrences of number within a section determined by the row 
 		 * and column of the number cell.
 		 * @param number
 		 * @param row Value from 0 to 9 representing the row number from top to bottom
 		 * @param col Value from 0 to 9 representing the column number from left to right
 		 * @return
 		 */
 		public int inSection(int number, int row, int col) {
 			int sectionRow = 3*(row /3);
 			int sectionCol = 3*(col /3);
 			int answer = 0;
 			for(int i = 0; i < SECTION_SIZE; i++) {
 				for(int j = 0; j < SECTION_SIZE; j++) {
 					if(this.board[sectionRow+i][sectionCol+j] == number) {
 						answer++;
 					}
 				}
 			}
 			return answer;
 		}
 		
 		/**
 		 * Prints 2D array representation of the board.
 		 * @param array
 		 */
 		public void print2DBoard(int array[][]) {
 			for(int row = 0; row < BOARD_SIZE; row++) {
 				for(int col = 0; col < BOARD_SIZE; col++) {
 					if(array[row][col] != EMPTY) {
 						System.out.print(array[row][col] + "|");	
 					} else {
 						System.out.print(" |");
 					}
 				}
 				System.out.println("");
 			}
 		}
 
 		/**
 		 * Prints temporary values of the board
 		 */
 		public void printTemps() {
 			for(int row = 0; row < BOARD_SIZE; row++) {
 				for(int col = 0; col < BOARD_SIZE; col++) {
 					LinkedList<Integer> possibleValues = this.possibleVals(row, col);
 						System.out.print(possibleValues);
 					System.out.print("                |                 ");
 				}
 				System.out.println("");
 				System.out.println("");
 			}
 		}
 		
 		public void printTempsSection(int section) {
 			int sectionRow = this.getSectionFirstRow(section);
 			int sectionCol = this.getSectionFirstCol(section);
 			for(int rowTranslate = 0; rowTranslate < this.SECTION_SIZE; rowTranslate++) {
 				for(int colTranslate = 0; colTranslate < this.SECTION_SIZE; colTranslate++) {
 					int currentRow = sectionRow + rowTranslate;
 					int currentCol = sectionCol + colTranslate;
 					LinkedList<Integer> possibleValues = this.possibleVals(currentRow, currentCol);
 					System.out.print(possibleValues);
 					System.out.print("     |      ");
 				}
 				System.out.println("");
 				System.out.println("");
 			}
 		}
 		
 		public static void main(String args[]) {
 			int board[][] = new int[BOARD_SIZE][BOARD_SIZE];
 			Scanner sc = new Scanner(System.in);
 			int i = 0;
 			while(i < 9) {
 				String input = sc.nextLine();
 				String[] vals = input.split(" ");
 				int j = 0;
 				for(String s : vals) {
 					board[i][j++] = Integer.valueOf(s);
 				}
 				i++;
 			}
 			
 			
 			System.out.println("Board");
 			
 			
 			SudokuState s = new SudokuState(board);
 			System.out.println("BoardState");
 			s.print2DBoard(s.board);
 			s.solve();
 			s.print2DBoard(s.board);
 		}
 }
 
