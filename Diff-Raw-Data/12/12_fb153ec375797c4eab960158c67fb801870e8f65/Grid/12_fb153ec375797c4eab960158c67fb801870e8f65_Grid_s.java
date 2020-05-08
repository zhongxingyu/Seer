 package it.univr.gameoflife;
 
 import java.awt.Dimension;
 import java.awt.Point;
 
 /**
  * Stores a logical grid of cells.
  * @author Giacomo Annaloro
  * @author Davide Mazzoni
  *
  */
 public class Grid {
 	
 	private final boolean[][] cells;
 	
 	/**
 	 * Constructs and initializes a <code>Grid</code> of the specified size. All cells are initially dead.
 	 * @param size the size of the grid
 	 */
 	public Grid(Dimension size) {
 		cells = new boolean[size.height][size.width];
 	}
 	
 	/**
 	 * Constructs and initializes a <code>grid</code> of the specified size, starting from another grid.
 	 * @param size the size of the grid
 	 * @param other the grid to start from
 	 */
 	public Grid(Dimension size, Grid other) {
 		cells = new boolean[size.height][size.width];
 		for (int i = 0; (i < size.height) && (i < other.cells.length); i++)
 			for (int j = 0; (j < size.width) && (j < other.cells[0].length); j++)
 				cells[i][j] = other.cells[i][j];
 	}
 	
 	/**
 	 * Determines if the cell at position <code>p</code> in this grid is alive.
 	 * @param p the point representing the coordinates of the cell.
 	 * @return <code>true</code> if the cell is alive, <code>false</code> otherwise.
 	 */
 	public boolean isAlive(Point p) {
 		return cells[p.x][p.y];
 	}
 	
 	/**
 	 * Determines if the cell at the specified coordinates in this grid is alive.
 	 * @param i the row index
 	 * @param j the column index
 	 * @return <code>true</code> if the cell is alive, <code>false</code> otherwise.
 	 */
 	public boolean isAlive(int i, int j) {
 		return cells[i][j];
 	}
 	
 	/**
 	 * Switches the cell at position <code>p</code> in this grid to the opposite state.
 	 * If the cell is alive it becomes dead, and vice versa.
 	 * @param p the <code>Point</code> representing the coordinates of the cell.
 	 */
 	public void changeState(Point p) {
 		cells[p.x][p.y] = !cells[p.x][p.y];
 	}
 	
 	/**
 	 * Switches the cell at the specified coordinates in this grid to the opposite state.
 	 * If the cell is alive it becomes dead, and vice versa.
 	 * @param i the row index
 	 * @param j the column index
 	 */
 	public void changeState(int i, int j) {
 		cells[i][j] = !cells[i][j];
 	}
 	
 	/**
 	 * Inserts a shape into this grid, with its upper left corner positioned at <code>dest</code>.
 	 * @param dest the point representing the destination coordinates.
 	 * @param shape the shape to insert.
 	 */
 	public void insertShape(Point dest, Shape shape) {
 		int absX, absY;
 		for (Point shapePoint : shape) {
 			absX = dest.x + shapePoint.x;
 			absY = dest.y + shapePoint.y;
			if (absX < cells.length && absY < cells[0].length)
 				cells[absX][absY] = true;
 		}
 	}
 	
 }
