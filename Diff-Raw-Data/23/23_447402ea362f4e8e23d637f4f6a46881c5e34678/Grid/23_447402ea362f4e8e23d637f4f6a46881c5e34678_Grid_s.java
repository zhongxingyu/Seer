 /**
  *
  */
 package datamodel;
 
 import java.security.InvalidParameterException;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Vector;
 
 /**
  * Holds the grid data structure<br>
  * notifies observer if necessary
  *
  * @author Jakob Karolus, Kevin Munk
  * @version 1.0
  *
  */
 public class Grid extends Observable {
 
 	private GridElement[][] grid;
 	private int rows;
 	private int columns;
 
 	/**
 	 * Initiates the grid with the specified height and width.
 	 *
 	 * @param rows
 	 *            the height of the grid
 	 * @param columns
 	 *            the width of the grid
 	 * @param observer
 	 */
 	public Grid(int rows, int columns, Observer observer) {
 		if (rows > 0 && columns > 0) {
 			this.rows = rows;
 			this.columns = columns;
 		} else {
 			throw new InvalidParameterException(
 					"The row and column parameter must be greater than 0");
 		}
 
 		if (observer != null)
 			this.addObserver(observer);
 
 		initGridElements(observer);
 	}
 
 	/**
 	 * Initiates the grid with the given grid.
 	 *
 	 * @param grid
 	 *            the specified grid
 	 * @param observer
 	 */
 	public Grid(Grid grid, Observer observer) {
 		this(grid.getRows(), grid.getColumns(), observer);
 
 		// preset the state
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < columns; j++) {
 				this.getElementAt(i, j).setState(
 						grid.getElementAt(i, j).getState());
 			}
 		}
 	}
 
 	/**
 	 * Makes a new internal array and initiates all fields with
 	 * GridElementState.FREE.<br>
 	 * <br>
 	 * Notifies all observers that grid is ready to use.
 	 *
 	 * @param observer
 	 *            the observer for every element to set
 	 */
 	private void initGridElements(Observer observer) {
 		this.grid = new GridElement[rows][columns];
 
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < columns; j++) {
 				this.grid[i][j] = new GridElement(i, j);
 				if (observer != null)
 					this.grid[i][j].addObserver(observer);
 			}
 		}
 
 		this.setChanged();
 		this.notifyObservers(new UpdateEvent(UpdateEvent.GRID_READY, columns,
 				rows, null, null));
 	}
 
 	/**
 	 * Searches the element that has the 'start' state.
 	 *
 	 * @return the element that has the state 'start' or null if none of the
 	 *         elements has this state.
 	 */
 	public GridElement getStartElement() {
 		return GridElement.currentStart;
 	}
 
 	/**
 	 * Search the element that has the 'end' state.
 	 *
 	 * @return the element that has the state 'end' or null if none of the
 	 *         elements has this state.
 	 */
 	public GridElement getEndElement() {
 		return GridElement.currentEnd;
 	}
 
 	/**
 	 * Returns the element at the specified position.
 	 *
 	 * @param row
 	 *            the row of the element
 	 * @param column
 	 *            the column of the element
 	 * @return the element at the specified coordinates or null if the
 	 *         coordinates are out of bounds.
 	 */
 	public GridElement getElementAt(int row, int column) {
 		return grid[row][column];
 	}
 
 	/**
 	 * @return a list of knodes of the grid
 	 */
 	public Vector<GridElement> getKnodes() {
 		Vector<GridElement> knodes = new Vector<GridElement>();
 		for(int i=0; i<columns; i++)
 			for(int j=0; j<rows; j++)
 				knodes.add(grid[i][j]);
 		return knodes;
 	}
 
 	/**
 	 * delivers the neighbors of the given GridElement, depending on the second
 	 * parameter
 	 *
 	 * @param element
 	 *            the given element
 	 * @param diagonal
 	 *            if true returns horizontal, vertical and diagonal neighbors;<br>
 	 *            if false returns only horizontal and vertical
 	 * @return the neighbors of this GridElement
 	 */
 	public Neighbors getNeighborsFrom(GridElement element, boolean diagonal) {
 		return new Neighbors(diagonal, element);
 	}
 
 	/**
 	 * delivers the neighbors of the given GridElement, depending on the second
 	 * parameter
 	 *
 	 * @param element
 	 *            the given element
 	 * @return the neighbors of this GridElement
 	 */
 	public Neighbors getNeighborsFrom(GridElement element) {
 		return this.getNeighborsFrom(element, false);
 	}
 
 	/**
 	 * @return the row count of the grid
 	 */
 	public int getRows() {
 		return rows;
 	}
 
 	/**
 	 * @return the column count of the grid
 	 */
 	public int getColumns() {
 		return columns;
 	}
 
 	/**
 	 * Resets the GridElemetAlgoState of every element of this grid.
 	 */
 	public void resetGridsAlgoState() {
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < columns; j++) {
 				grid[i][j].resetElement();
 			}
 		}
 	}
 
 	/**
 	 * class, which encapsulates the neighbors of a GridElement
 	 *
 	 * @author Jakob Karolus, Kevin Munk
 	 * @version 1.0
 	 *
 	 */
 	public class Neighbors {
 
 		/**
 		 * saves the neighbors and wayCosts. The first Element in neighbors corresponds to the first
 		 * Element in wayCosts, etc.
 		 */
 		private Vector<GridElement> neighbors = new Vector<GridElement>();
 		private Vector<Integer> wayCosts = new Vector<Integer>();
 
 		/**
 		 *
 		 * @param diagonal
 		 *            if the neighbors should include the diagonal ones
 		 * @param element
 		 *            the element whose neighbors should be acquired
 		 */
 		private Neighbors(boolean diagonal, GridElement element) {
 			if (diagonal) {
 				this.getDiagonalNeighborsOf(element);
 			} else {
 				this.getNeighborsOf(element);
 			}
 		}
 
 		/**
 		 * Gets the horizontal and vertical neighbors of the element.
 		 *
 		 * @param element
 		 * @return the neighbors and the way costs to the element
 		 */
 		private void getNeighborsOf(GridElement element) {
 			int row = element.getRow();
 			int col = element.getColumn();
 			neighbors.clear();
 			wayCosts.clear();
 			
 			if (row > 0){
 				// a neighbor-element over element exist
 				if (getElementAt(row-1,col).getState() != GridElementState.BLOCKED){
 					// check if element blocked
 					neighbors.addElement(getElementAt(row-1,col));
 					wayCosts.addElement(getElementAt(row-1,col).getWeight());
 				}
 			}
 			if (col > 0){
 				// a neighbor-element left of element exist
 				if (getElementAt(row-1,col).getState() != GridElementState.BLOCKED){
 					// check if element blocked
 					neighbors.addElement(getElementAt(row,col-1));
 					wayCosts.addElement(getElementAt(row,col-1).getWeight());
 				}
 			}
 			if (col < grid.length-1){
 				// a neighbor-element right of element exist
 				if (getElementAt(row-1,col).getState() != GridElementState.BLOCKED){
 					// check if element blocked
 					neighbors.addElement(getElementAt(row, col+1));
 					wayCosts.addElement(getElementAt(row, col+1).getWeight());
 				}
 			}
 			if (row < grid.length-1){
 				// a neighbor-element down of element exist
 				if (getElementAt(row-1,col).getState() != GridElementState.BLOCKED){
 					// check if element blocked
 					neighbors.addElement(getElementAt(row+1, col));
 					wayCosts.addElement(getElementAt(row+1, col).getWeight());
 				}
 			}
 			
 		}
 
 		/**
 		 * Gets all neighbors of the element. Inclusive the diagonal laying
 		 * fields.
 		 *
 		 * @param currentElement
 		 * @return the neighbors(incl. diagonal neighbors) and the way costs to
 		 *         the element. The diagonal elements have 1,5x greater
 		 *         way costs.
 		 */
 		private void getDiagonalNeighborsOf(GridElement element) {
			//TODO:1,5 for diagonal
 			int row = element.getRow();
 			int col = element.getColumn();
 			neighbors.clear();
 			wayCosts.clear();
 			
 			if (row > 0){
 				// a neighbor-element over element exist
 				// x
 				// E
 				//
 				if (getElementAt(row-1,col).getState() != GridElementState.BLOCKED){
 					// check if element blocked
 					neighbors.addElement(getElementAt(row-1,col));
 					wayCosts.addElement(getElementAt(row-1,col).getWeight());
 				}
 			}
 			if (row > 0 && col > 0){
 				// a neighbor-element left over element exist
 				//x
 				// E
 				//
 				if (getElementAt(row-1,col).getState() != GridElementState.BLOCKED){
 					// check if element blocked
 					neighbors.addElement(getElementAt(row-1, col-1));
					wayCosts.addElement(getElementAt(row-1, col-1).getWeight());
 				}
 			}
 			if (col > 0){
 				// a neighbor-element left of element exist
 				//
 				//xE
 				//
 				if (getElementAt(row-1,col).getState() != GridElementState.BLOCKED){
 					// check if element blocked
 					neighbors.addElement(getElementAt(row,col-1));
 					wayCosts.addElement(getElementAt(row,col-1).getWeight());
 				}
 			}
 			if (row < grid.length-1 && col > 0){
 				// a neighbor-element left down of element exist
 				//
 				// E
 				//x
 				if (getElementAt(row-1,col).getState() != GridElementState.BLOCKED){
 					// check if element blocked
 					neighbors.addElement(getElementAt(row+1, col-1));
					wayCosts.addElement(getElementAt(row+1, col-1).getWeight());
 				}
 			}
 			if (row < grid.length-1){
 				// a neighbor-element down of element exist
 				//
 				// E
 				// x
 				if (getElementAt(row-1,col).getState() != GridElementState.BLOCKED){
 					// check if element blocked
 					neighbors.addElement(getElementAt(row+1, col));
 					wayCosts.addElement(getElementAt(row+1, col).getWeight());
 				}
 			}
 			if (row < grid.length-1 && col < grid.length){
 				// a neighbor-element right down of element exist
 				//
 				// E
 				//  x
 				if (getElementAt(row-1,col).getState() != GridElementState.BLOCKED){
 					// check if element blocked
 					neighbors.addElement(getElementAt(row+1, col+1));
					wayCosts.addElement(getElementAt(row+1, col+1).getWeight());
 				}
 			}
 			if (col < grid.length-1){
 				// a neighbor-element right of element exist
 				//
 				// Ex
 				//
 				if (getElementAt(row-1,col).getState() != GridElementState.BLOCKED){
 					// check if element blocked
 					neighbors.addElement(getElementAt(row, col+1));
 					wayCosts.addElement(getElementAt(row, col+1).getWeight());
 				}
 			}
 			if (row > 0 && col < grid.length){
 				// a neighbor-element right up of element exist
 				//  x
 				// E
 				//
 				if (getElementAt(row-1,col).getState() != GridElementState.BLOCKED){
 					// check if element blocked
 					neighbors.addElement(getElementAt(row-1, col+1));
					wayCosts.addElement(getElementAt(row-1, col+1).getWeight());
 				}
 			}
 		}
 
 		/**
 		 * @return the neighbors
 		 */
 		public Vector<GridElement> getNeighbors() {
 			return neighbors;
 		}
 
 		/**
 		 * @return the wayCosts
 		 */
 		public Vector<Integer> getWayCosts() {
 			return wayCosts;
 		}
 
 	}
 }
