 package pxc.util.polyominos;
 
 public class Board implements Cloneable
 {
 
     private int      numrows = 0;
     private int      numcols = 0;
     private char[][] board;
 
     // Constructors -----------------------------------------------
     public Board()
     {
         this(0, 0);
     }
 
     public Board(int rows, int cols)
     {
         this.numrows = rows;
         this.numcols = cols;
         this.board = new char[rows][cols];
 
         // Initialize the board
         for (int i = 0; i < rows; i++)
         {
             for (int j = 0; j < cols; j++)
             {
                 board[i][j] = '.';
             }
         }
     }
 
     protected Board clone() throws CloneNotSupportedException
     {
         Board clone = new Board(numrows, numcols);
 
         for (int i = 0; i < numrows; i++)
         {
             for (int j = 0; j < numcols; j++)
             {
                 clone.setValue(i, j, board[i][j]);
             }
         }
 
         return clone;
     }
 
     // Getters and setters -------------------------------------------------------------
     public void setValue(int row, int col, char value)
     {
         board[row][col] = value;
     }
 
     public char getValue(int row, int col)
     {
         return board[row][col];
     }
 
     public int numRows()
     {
         return numrows;
     }
 
     public int numCols()
     {
         return numcols;
     }
 
     // Public methods ---------------------------------------------------------------
     public String deepToString()
     {
         StringBuilder result = new StringBuilder();
         result.append("[[");
 
         for (int i = 0; i < numrows; i++)
         {
             for (int j = 0; j < numcols; j++)
             {
                 result.append((char) board[i][j]);
                 result.append(", ");
             }
 
             result.delete(result.length() - 2, result.length());
             result.append("], [");
         }
 
         result.delete(result.length() - 3, result.length());
         result.append("]");
 
         return result.toString();
     }
 
     public void printBoard()
     {
         for (int i = 0; i < numrows; i++)
         {
             for (int j = 0; j < numcols; j++)
             {
                System.out.print(board[i][j] + ' ');
             }
 
             System.out.println();
         }
 
         System.out.println("-----");
     }
 }
