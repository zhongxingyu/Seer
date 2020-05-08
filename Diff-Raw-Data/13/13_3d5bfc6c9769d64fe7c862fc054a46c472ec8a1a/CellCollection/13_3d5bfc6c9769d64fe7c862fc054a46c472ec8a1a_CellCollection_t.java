 package avanwieringen.sudoku;
 
 import org.apache.commons.lang3.ArrayUtils;
 
 /**
  * A collection of cells representing the rows, columns and nonets of a Sudoku
  * @author arjan
  *
  */
 public class CellCollection {
 	
 	/**
 	 * Array of cell values in this collection
 	 */
 	protected Cell[] cells;
 	
 	/**
 	 * Internal counter used for adding the cells
 	 */
 	protected int internalCounter = 0;
 	
 	/**
 	 * The type of the collection (Row, Column or Nonet)
 	 */
 	protected Type type;
 	
 	/**
 	 * Enumeration of the possible types
 	 *
 	 */
 	public enum Type {
 		ROW,
 		COLUMN,
 		NONET;
 	}
 	
 	/**
 	 * Constructs a new cell collection of specified size and type
 	 * @param size The size of the cell collection
 	 * @param type The type (Row, Column or Nonet)
 	 */
 	public CellCollection(int size, Type type) {
 		this.cells = new Cell[size];
 		this.type = type;
 	}
 	
 	/**
 	 * Adds a cell to the collection, where the cell is also updated such that there is a double reference
 	 * @param c The cell to add
 	 */
 	public void addCell(Cell c) {
 		if(internalCounter == this.cells.length) {
 			throw new IndexOutOfBoundsException("Cannot add more cells, limit of " + this.cells.length + " ");
 		}
 		this.cells[internalCounter] = c;
 		
 		switch(this.type) {
 		case ROW:
 			c.setRow(this);
 			break;
 			
 		case COLUMN:
 			c.setColumn(this);
 			break;
 			
 		case NONET:
 			c.setNonet(this);
 			break;
 		}
 		internalCounter++;
 	}
 	
 	/**
 	 * Returns whether or not a Cell is contained in this collection
 	 * @param c Cell
 	 * @return boolean
 	 */
 	public boolean contains(Cell c) {
 		return ArrayUtils.contains(this.cells, c);
 	}
 	
 	/**
 	 * Returns all Cells in this collection
 	 * @return Cell[]
 	 */
 	public Cell[] getCells() {
 		return this.cells;
 	}
 }
