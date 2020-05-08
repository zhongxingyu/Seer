 package jeudelavie;
 
 
 public class Board {
     private boolean board[][];
     private int rows;
     private int cols;
 
     /**
      * Create a new board
      * @param rows number of rows
      * @param cols number of columns
      */
     public Board(int rows, int cols) {
         this.rows = rows;
         this.cols = cols;
         this.board = new boolean[rows][cols];
     }
 
 
     /**
      *
      * @param x the row
      * @param y the column
      * @return the value at x,y
      */
     public boolean getCell(int x, int y) {
         return board[x][y];
     }
 
     /**
     * Set the value of a cell, unless it's out of bounds.
      * @param x the row
      * @param y the column
      * @param b the boolean value
      */
     public void setCell(int x, int y, boolean b) {
        if (x >= 0 && x < rows && y >= 0 && y < cols)
             board[x][y] = b;
     }
 
 
     /**
      * Compute the next generation of the game
      */
     public void nextGeneration() {
         Board newBoard = new Board(rows, cols);
 
         for (int i = 0; i < rows; ++i) {
             for (int j = 0; j < rows; ++j) {
                 newBoard.setCell(i, j, this.nextGeneration(i, j));
             }
         }
 
         board = newBoard.board;
     }
 
     /**
     * Compute the state of the next generation of a cell:
     * 	 2-3 neighbors to an occupied cell: stay the same
     *   3 neighbors to an empty cell: new cell
     *   4+ neighbors: death from over-population
     *   0-1 neighbors: death from starvation
      * @param x the row of the cell
      * @param y the column of the cell
      * @return boolean describing if the cell is alive or dead
      */
     public boolean nextGeneration(int x, int y) {
         int adjacent = occupiedAdjacentCells(x, y);
 
         if (adjacent == 3)
             return true;
         else if (adjacent >= 4)
             return false;
         else if (adjacent <= 1)
             return false;
 
         return getCell(x, y);
     }
 
     /**
      * Count the number of occupied adjacent cells
      * @param x the row of the cell
      * @param y the column of the cell
      * @return the number of neighbors
      */
     public int occupiedAdjacentCells(int x, int y) {
         int occupied = 0;
 
         for (int i = x-1; i <= x+1; ++i) {
             for (int j = y-1; j <= y+1; ++j) {
             	// Skip self
                 if (!(i == x && j == y)) {
                     try {
                         occupied += getCell(i, j) ? 1 : 0;
                     }
                     catch (IndexOutOfBoundsException e) {}
                 }
             }
         }
         return occupied;
     }
 
 
     /**
      * Return a string representation of the game board
      */
     public String toString() {
         StringBuilder out = new StringBuilder();
         for (boolean[] row: board) {
             for (boolean cell: row) {
                 out.append(cell ? "*" : ".");
             }
             out.append("\n");
         }
         return out.toString();
     }
 }
