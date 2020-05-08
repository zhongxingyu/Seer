 package mechanics;
 
 /**
  * Class that represents a cell - coordinates of X and Y on the game board.
  * 
  * @author Ajee Interactive
  * 
  */
 public final class Cell {
 
 	/**
 	 * X coordinate of the cell.
 	 */
 	private int row;
 
 	/**
 	 * Y coordinate of the cell.
 	 */
 	private int column;
 
 	/**
 	 * An array that stores all possible cells on the board. Done in this way
 	 * because of performance issues.
 	 */
 	private static Cell[][] cellStorage = new Cell[12][12];
 
 	/**
 	 * Initializes a storage with all possible cells.
 	 */
 	public static void init() {
 		for (int i = 0; i < 12; i++)
 			for (int j = 0; j < 12; j++)
 				cellStorage[i][j] = new Cell(i, j);
 	}
 
 	/**
 	 * Constructs a new cell.
 	 * 
 	 * @param newRow
 	 *            X coordinate of the cell
 	 * @param newColumn
 	 *            Y coordinate of the cell
 	 */
 	private Cell(int newRow, int newColumn) {
 		row = newRow;
 		column = newColumn;
 	}
 
 	/**
 	 * Returns a cell by given coordinates.
 	 * 
 	 * @param row
 	 *            X coordinate of the cell
 	 * @param column
 	 *            Y coordinate of the cell
 	 * @return cell instance
 	 */
 	public static Cell get(int row, int column) {
 //		if (row > 9 || row < 1 || column > Board.getMaxColumn(row)
 //				|| column < Board.getMinColumn(row))
 //			return cellStorage[10][10];
 		return cellStorage[row][column];
 	}
 
 	/**
 	 * Returns the X coordinate of the cell.
 	 * 
 	 * @return X coordinate
 	 */
 	public int getRow() {
 		return row;
 	}
 
 	/**
 	 * Returns the Y coordinate of the cell.
 	 * 
 	 * @return Y coordinate
 	 */
 	public int getColumn() {
 		return column;
 	}
 
 	/**
 	 * Returns a cell that is next to this cell in a given direction.
 	 * 
 	 * @param d
 	 *            direction in which to search next cell
 	 * @return cell instance
 	 */
 	public Cell shift(Direction d) {
 		switch (d) {
 		case NorthWest:
			if (row > 1 && column > Board.getMinColumn(row))
 				return get(row - 1, column - 1);
 			break;
 		case North:
			if (row > 1)
 				return get(row - 1, column);
 			break;
 		case East:
			if (column < Board.getMaxColumn(row))
 				return get(row, column + 1);
 			break;
 		case SouthEast:
			if (row < 9 && column < Board.getMaxColumn(row))
 				return get(row + 1, column + 1);
 			break;
 		case South:
			if (row < 9)
 				return get(row + 1, column);
 			break;
 		case West:
			if (column > Board.getMinColumn(row))
 				return get(row, column - 1);
 			break;
 		}
 		return get(row, column);
 	}
 
 	/**
 	 * Tests if this cell is on any line with a given cell.
 	 * 
 	 * @param c
 	 *            another cell
 	 * @return true if two cells are on the same line, false otherwise
 	 */
 	public boolean isOnAnyLine(Cell c) {
 		for (Direction d : Direction.values()) {
 			if (isOnLine(c, d))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Tests if this cell is on line of a given direction with a given cell.
 	 * 
 	 * @param c
 	 *            another cell
 	 * @param d
 	 *            direction of the line
 	 * @return true if two cells are on this line, false otherwise
 	 */
 	public boolean isOnLine(Cell c, Direction d) {
 		if (d == Direction.NorthWest || d == Direction.SouthEast) {
 			return Math.abs(row - c.getRow()) == Math.abs(column
 					- c.getColumn());
 		} else if (d == Direction.North || d == Direction.South) {
 			return Math.abs(column - c.getColumn()) == 0;
 		} else
 			return Math.abs(row - c.getRow()) == 0;
 	}
 
 	/**
 	 * Computes the Manhattan distance between this cell and a given cell.
 	 * 
 	 * @param c
 	 *            another cell
 	 * @return Manhattan distance between two cells
 	 */
 	public int findDistance(Cell c) {
 		int cols = column - c.getColumn();
 		int rows = row - c.getRow();
 		if ((cols < 0) ^ (rows < 0))
 			return Math.abs(cols) + Math.abs(rows);
 		else
 			return Math.max(Math.abs(cols), Math.abs(rows));
 
 	}
 
 	/**
 	 * Returns a string representing the cell, e.g. "A5", "E7".
 	 */
 	@Override
 	public String toString() {
 		return Character.toString((char) ((int) 'A' + row - 1))
 				+ Integer.toString(column);
 	}
 	
 	/**
 	 * Tests if a current cell is equal to a given cell.
 	 * @param c another cell
 	 * @return true if two cells are equal, false otherwise
 	 */
 	public boolean equals(Cell c) {
 		return (row == c.getRow() && column == c.getColumn());
 	}
 
 }
