 package tictactoe;
 
 /**
  * Represents a location on the board.
  * @author jdenvir
  *
  */
 public class Location {
 	private final int column ;
 	private final int row ;
 	
 	/**
 	 * Creates a location on the board corresponding to the specified column and row indexes (both zero-based).
 	 * @param column
 	 * @param row
 	 * @throws IllegalArgumentException if either column or row are less than zero or greater than or equal to 3.
 	 */
 	public Location(int column, int row) {
 		if (column < 0 || column >= 3 || row < 0 || row >= 3) {
			throw new IllegalArgumentException(String.format("[%d, %d] is not a valid location on the board", column, row));
 		}
 		this.column = column ;
 		this.row = row ;
 	}
 	
 	public int getRow() {
 		return row ;
 	}
 	
 	public int getColumn() {
 		return column ;
 	}
 	
 	@Override
 	public String toString() {
 		return String.format("[%d, %d]", column, row);
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + column;
 		result = prime * result + row;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Location other = (Location) obj;
 		if (column != other.column)
 			return false;
 		if (row != other.row)
 			return false;
 		return true;
 	}
 	
 }
