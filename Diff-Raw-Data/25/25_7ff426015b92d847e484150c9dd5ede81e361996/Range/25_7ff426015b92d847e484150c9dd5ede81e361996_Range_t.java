 package spreadsheet;
 
 import java.util.ArrayList;
import java.util.List;
 
 final public class Range {
 	
 	private int rowMax;
 	private int rowMin;
 	private int columnMax;
 	private int columnMin;
 	private Position posA;
 	private Position posB;
	private List<Position> posList;
 
 	/**
 	 * Construct a immutable object Range which holds the corners of
 	 * two positions
 	 * @param a non null Position
 	 * @param b non null Position
 	 * @throws IllegalPosition 
 	 */
 	public Range(final Position a, final Position b) {
 		this.rowMin = Math.min(a.getRow(), b.getRow()); 
 		this.rowMax = Math.max(a.getRow(), b.getRow()); 
 		this.columnMin = Math.min(a.getColumn(), b.getColumn()); 
 		this.columnMax = Math.max(a.getColumn(), b.getColumn()); 
 		this.posA = a;
 		this.posB = b;
 	}
 	
 	/**
 	 * Returns a list of positions. 
 	 * Creates a new list if it hasn't been used before
 	 * @return ArracolumnList<Position> of all Positions in ranges
 	 */
	public List<Position> getPositionsInRange() {
 		if (posList == null) {
 			posList = new ArrayList<Position>();
 			addPositions();
 		}
 		return posList;	
 	}
 	
 	public String getDescription() {
 	    final StringBuilder builder = new StringBuilder();
 		builder.append(this.posA.getDescription());
 		builder.append(":");
 		builder.append(this.posB.getDescription());
 		return builder.toString();
 	}
 
 	/**
 	 * Adds all position in a Range to posList
 	 */
 	private void addPositions() {
 		int row = rowMin;
 		int column = columnMin;
 		while (column <= columnMax) {
 			if (row <= rowMax) {
 				posList.add(new Position(column, row));
 				row++;
 			}
 			else {
 				row = rowMin;
 				column++;
 			}
 		}
 	}
 }
