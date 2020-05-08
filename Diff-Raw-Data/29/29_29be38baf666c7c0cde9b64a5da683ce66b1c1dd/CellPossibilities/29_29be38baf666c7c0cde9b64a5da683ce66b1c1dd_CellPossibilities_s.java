 package fr.eisti.ing1.solver;
 
 /**
  * Data structured used by the basic solver class. Describes for each cell, the
  * number of possible values.
  * 
  */
 public class CellPossibilities implements Comparable<CellPossibilities> {
 
 	/**
 	 * Row number.
 	 */
 	private int row;
 
 	/**
 	 * Column number.
 	 */
 	private int column;
 
 	/**
 	 * Number of possible values for this cell, according to the initial sudoku
 	 * grid.
 	 */
 	private int nbPossibleValues;
 
 	/**
 	 * Creates a new cell data.
 	 * 
 	 * @param row
 	 * @param column
 	 * @param nbPossibilities
 	 */
 	public CellPossibilities(int row, int column, int nbPossibilities) {
 		this.row = row;
 		this.column = column;
 		this.nbPossibleValues = nbPossibilities;
 	}
 
 	/**
 	 * Compares two cells. A cell is considered "better" if it has less possible
 	 * numbers.
 	 */
 	@Override
 	public int compareTo(CellPossibilities other) {
 		int nombre1 = other.getNbValeursPossibles();
 
 		if (nombre1 > nbPossibleValues) {
 			return -1;
 		} else if (nombre1 == nbPossibleValues) {
 			return 0;
 		} else {
 			return 1;
 		}
 	}
 
 	@Override
 	public boolean equals(Object obj) {
		CellPossibilities c = (CellPossibilities) obj;
		return (this.row == c.row && this.column == c.column && this.nbPossibleValues == c.nbPossibleValues);
 	}
 
 	/**
 	 * Gets the row number of this cell.
 	 * 
 	 * @return row number
 	 */
 	public int getLigne() {
 		return this.row;
 	}
 
 	/**
 	 * Gets the column number of this cell.
 	 * 
 	 * @return column number
 	 */
 	public int getColonne() {
 		return this.column;
 	}
 
 	/**
 	 * Gets the number of possible values for this cell.
 	 * 
 	 * @return number of possible values
 	 */
 	public int getNbValeursPossibles() {
 		return this.nbPossibleValues;
 	}
 
 }
