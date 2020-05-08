 package fr.iutvalence.java.mp.tictactoe;
 
 /**
  * Grid
  * This class represent the grid where play player
  * A grid is composed by squares
  */
 public class Grid
 {
     
     /**
      * Default grid size (grid is supposed to have the same height and width)
      */
     public final static int DEFAULT_GRID_SIZE = 16;
 
     /**
      * grid[line][column] Game's grid
      */
    // TODO (fix) this field should be private
    public final Square[][] grid;
 
     /**
      * Generates an empty grid of DEFAULT_GRID_SIZE columns and DEFAULT_GRID_SIZE lines
      */
     public Grid()
     {
 
         this.grid = new Square[DEFAULT_GRID_SIZE][DEFAULT_GRID_SIZE];
 
         for (int lineNumber = 0; lineNumber < DEFAULT_GRID_SIZE; lineNumber++)
         {
             for (int columnNumber = 0; columnNumber < DEFAULT_GRID_SIZE; columnNumber++)
             {
                 this.grid[columnNumber][lineNumber] = new Square();
             }
         }
     }
 
     /**
      * Checks if a certain position is inside the grid's bounds
      * @param position the position checked
      * @return true if the position is inside the bounds, false if not
      */
     private boolean isPositionInBounds(Position position)
     {
         return  !((position.getColumn() >= DEFAULT_GRID_SIZE || position.getColumn() < 0 
                 || position.getLine() >= DEFAULT_GRID_SIZE || position.getLine() <0));
     }
     
     /**
      * Returns the square at given position
      * @param position Position of square
      * @return a Square
      * @throws PositionOutOfBoundsException if the square is out of the grid
      */
     public Square getSquareAt(Position position) throws PositionOutOfBoundsException
     {
        if (!this.isPositionInBounds(position))  throw new PositionOutOfBoundsException();
         return this.grid[position.getColumn()][position.getLine()];
     }
     
     /**
      * Create a position by translating of the current position
      * @param origin the current position
      * @param direction the direction in which the position is translated
      * @return The new position
      * @throws PositionOutOfBoundsException if the new position is out of the grid's bounds
      */
     public Position getNeighbourPosition(Position origin, Direction direction) throws PositionOutOfBoundsException
     {
         Position result = null;
         
         switch (direction)
         {
         case UP : 
             result = origin.translate(0, -1);
             break;
         case DOWN : 
             result = origin.translate(0, 1);
             break;
         case LEFT : 
             result = origin.translate(-1, 0);
             break;
         case RIGHT : 
             result = origin.translate(1, 0);
             break;
         case UP_LEFT : 
             result = origin.translate(-1, -1);
             break;
         case UP_RIGHT : 
             result = origin.translate(1, -1);
             break;
         case DOWN_LEFT : 
             result = origin.translate(-1, 1);
             break;
         case DOWN_RIGHT : 
         default:  
             result = origin.translate(1, 1);        
         }
         if (!this.isPositionInBounds(result)) throw new PositionOutOfBoundsException();
         return result;
     }
 
 
 }
