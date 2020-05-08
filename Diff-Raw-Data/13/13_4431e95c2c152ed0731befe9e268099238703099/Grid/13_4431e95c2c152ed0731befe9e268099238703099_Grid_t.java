 package sudoku;
 
 import java.awt.Point;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /** A sudoku grid.
  * @author Antoine MOISE and Adrien RICCIARDI
  */
 public class Grid
 {
     /** Hold the cells content. */
     private byte _cells[][];
     /** Width of the grid in cells. */
     private int _width;
     /** Height of the grid in cells. */
     private int _height;
     /** How many numbers are used by the grid. */
     private int _numbersCount;
     /** How many squares are represented in the grid. */
     private int _squaresCount;
     
     /** Used by showDifference() to store the last shown grid. */
     private byte _lastGridCells[][];
     
     /** Create a new grid from a file.
      * @param fileName Name of the file describing the grid.
      */
     public Grid(String fileName) throws FileNotFoundException
     {
         Scanner scanner = new Scanner(new File(fileName));
         
         /** @todo : for now we use a fixed grid size */
         _width = 9;
         _height = 9;
         _numbersCount = _width;
         _squaresCount = 3;
         
         // Create grids
         _cells = new byte[_width][_height];
         _lastGridCells = new byte[_width][_height];
         
         // Read content from file
         for (int row = 0; row < _height; row++)
         {
             for (int column = 0; column < _width; column++)
             {
                 byte cellValue;
                 try
                 {
                      cellValue = (byte) scanner.nextInt();
                 }
                 catch (Exception exception)
                 {
                     throw new RuntimeException("Bad grid file format. " + exception.getMessage());
                 }
                         
                 _cells[row][column] = cellValue;
                 _lastGridCells[row][column] = cellValue;
             }
         }       
     }
     
     /** Create a new grid filled with the content of another. */
     public Grid(Grid grid)
     {
         _width = grid._width;
         _height = grid._height;
         _numbersCount = grid._numbersCount;
         _squaresCount = grid._squaresCount;
         
         for (int row = 0; row < grid._height; row++)
         {
             for (int column = 0; column < grid._width; column++)
             {
                 _cells[row][column] = grid._cells[row][column];
                 _lastGridCells[row][column] = grid._lastGridCells[row][column];
             }
         }
     }
     
     /** Display grid content to the console. */
     public void show()
     {
         for (int row = 0; row < _height; row++)
         {
             for (int column = 0; column < _width; column++)
             {
                 if (_cells[row][column] == 0) System.out.print(". ");
                 else System.out.print(_cells[row][column] + " ");
             }
             System.out.println();
         }
         System.out.println();
     }
     
     /** Display the grid and colorize changes between the current grid and the last call of this function. */
     public void showDifferences()
     {
         for (int row = 0; row < _height; row++)
         {
             for (int column = 0; column < _width; column++)
             {
                 // Check if there is a difference between this last cell and current cell
                boolean hasColorChanged = false;
                 if (_lastGridCells[row][column] != _cells[row][column])
                 {
                     System.out.print("\u001B[34m"); // Use VT100 escape sequence
                    hasColorChanged = true;
                 }
                 
                 // Show value
                 if (_cells[row][column] == 0) System.out.print(". ");
                 else System.out.print(_cells[row][column] + " ");
                 
                 // Restore color if needed
                if (hasColorChanged) System.out.print("\u001B[0m");
                 
                 // Update last grid in the same time
                 _lastGridCells[row][column] = _cells[row][column];
             }
             System.out.println();
         }
         System.out.println();
     }
     
     /** Get a list of the allowed missing numbers for a specified cell.
      * @param row Cell row coordinate.
      * @param column Cell column coordinate.
      * @return The allowed numbers for this cell or null if the cell is not empty.
      */
     public ArrayList<Byte> getPossibleNumberAt(int row, int column)
     {
        // Check if the cell is empty
        if (_cells[row][column] != 0) return null;
         
         // Tell if a number is existing or not at the end of the process
         boolean existingNumbers[] = new boolean[_numbersCount + 1];
                     
         // List existing numbers on the cell row
         for (int i = 0; i < _width; i++)
         {
             byte cellValue = _cells[row][i];
             if (cellValue != 0) existingNumbers[cellValue] = true;
         }
         
         // List existing numbers on the cell column (it's done separately from cell row in case of different grid width and height)
         for (int i = 0; i < _height; i++)
         {
             byte cellValue = _cells[i][column];
             if (cellValue != 0) existingNumbers[cellValue] = true;
         }
         
         // Find square bounds in which the cell is located
         int squareRowStart = row / _squaresCount;
         int squareRowEnd = (row / _squaresCount) + _squaresCount;
         int squareColumnStart = column / _squaresCount;
         int squareColumnEnd = (column /_squaresCount) + _squaresCount;
         
         for (int squareRow = squareRowStart; squareRow < squareRowEnd; squareRow++)
         {
             for (int squareColumn = squareColumnStart; squareColumn < squareColumnEnd; squareColumn++)
             {
                 byte cellValue = _cells[squareRow][squareColumn];
                 if (cellValue != 0) existingNumbers[cellValue] = true;
             }
         }        
         
         // Fill list with missing numbers
         ArrayList<Byte> result = new ArrayList<Byte>();
         for (byte i = 1; i < existingNumbers.length; i++)
         {
             if (!existingNumbers[i]) result.add(i);
         }
         return result;
     }
     
     /** Get the width of the grid.
      * @return The grid width.
      */
     public int getWidth()
     {
         return _width;
     }
     
     /** Get the height of the grid.
      * @return The grid height.
      */
     public int getHeight()
     {
         return _height;
     }
     
     /**
      * Compute figure that can be put at cass (i,j) according to Sudoku rule
      *
      * @param i row index
      * @param j column index
      * @return all possible figure
      */
     /*public ArrayList<Byte> getPossibleNumberAt(int i, int j) {
         boolean figureRow[] = new boolean[9];
         boolean figureColumn[] = new boolean[9];
         ArrayList<Byte> possibleNumber = new ArrayList<Byte>();
         for (int k = 0; k < _width; k++) {
             figureRow[k] = false;
             figureColumn[k] = false;
         }
         
         for(int k = 0;k<_width;k++){
             figureRow[_cells[i][k]-1] = true;
             figureColumn[_cells[k][j]-1] = true;  
         }
         
         for(int k =0;k<_width;k++){
             if(!figureRow[k] && !figureColumn[k])
                 possibleNumber.add(new Byte((byte)(k+1)));
         }
         return possibleNumber;
     }*/
     
     /** Tell if all cells are filled in or not.
      * @return true if all cells are filled or false if not.
      */
     public boolean isEntirelyFilled()
     {
         for (int row = 0; row < _height; row++)
         {
             for (int column = 0; column < _width; column++)
             {
                 if (_cells[row][column] == 0) return false;
             }
         }
         return true;
     }
     
     /** Set a cell value.
      * @param row Cell row coordinate.
      * @param column Cell column coordinate.
      * @param value Cell value.
      */
     public void setCell(int row, int column, byte value)
     {
         assert row > 0;
         assert row < _height;
         assert column > 0;
         assert column < _width;
         _cells[row][column] = value;
     }
     
     /** Tell if the grid is correctly filled according to Sudoku rules.
      * This function does not care about empty cells.
      * @return true if the grid is correctly filled or false if there is a mistake.
      */
     public boolean isCorrectlyFilled()
     {
         boolean isNumberFound[] = new boolean[_numbersCount];
         
         // Check each row to find one and only one instance of each number
         for (int row = 0; row < _height; row++)
         {
             // Reset array
             for (int i = 0; i < isNumberFound.length; i++) isNumberFound[i] = false;
             
             for (int column = 0; column < _width; column++)
             {
                 byte cellValue = _cells[row][column];
                 if (cellValue == 0) continue; // Avoid empty cells
                 if (isNumberFound[cellValue]) return false;
                 isNumberFound[cellValue] = true;
             }
         }
         
         // Check each column to find one and only one instance of each number
         for (int column = 0; column < _width; column++)
         {
             // Reset array
             for (int i = 0; i < isNumberFound.length; i++) isNumberFound[i] = false;
             
             for (int row = 0; row < _height; row++)
             {
                 byte cellValue = _cells[row][column];
                 if (cellValue == 0) continue; // Avoid empty cells
                 if (isNumberFound[cellValue]) return false;
                 isNumberFound[cellValue] = true;
             }
         }
         
         // Check each square to find one and only one instance of each number
         for (int squareRow = 0; squareRow < _squaresCount; squareRow++)
         {
             for (int squareColumn = 0; squareColumn < _squaresCount; squareColumn++)
             {
                 // Reset array
                 for (int i = 0; i < isNumberFound.length; i++) isNumberFound[i] = false;
                 
                 // Check square row after row
                 int rowStart = squareRow * _squaresCount;
                 int rowEnd = (squareRow * _squaresCount) + _squaresCount;
                 int columnStart = squareColumn * _squaresCount;
                 int columnEnd = (squareColumn * _squaresCount) + _squaresCount;
                 
                 for (int row = rowStart; row < rowEnd; row++)
                 {
                     for (int column = columnStart; column < columnEnd; column++)
                     {
                         byte cellValue = _cells[row][column];
                         if (cellValue == 0) continue; // Avoid empty cells
                         if (isNumberFound[cellValue]) return false;
                         isNumberFound[cellValue] = true;
                     }
                 }
             }
         }
         return true;
     }
     
     /** Get all empty cells of the grid.
      * @return A list of points (x = column; y = row) representing all empty cells.
      */
     public ArrayList<Point> getEmptyCells()
     {
         ArrayList<Point> result = new ArrayList<Point>();
         
         for (int row = 0; row < _height; row++)
         {
             for (int column = 0; column < _width; column++)
             {
                 if (_cells[row][column] == 0) result.add(new Point(column, row));
             }
         }
         return result;
     }
 }
