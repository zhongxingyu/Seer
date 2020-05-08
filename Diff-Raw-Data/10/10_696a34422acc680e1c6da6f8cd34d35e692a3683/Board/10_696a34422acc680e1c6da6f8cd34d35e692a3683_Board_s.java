 package game;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 /**
  * Board as specified in Section 2.1
  * EXCEPT: we start counting at (0,0).
  * 
  * @author seba
  */
 public class Board {
   
   /**
    * FIRST coordinate is COLUMN;
    * SECOND coordinate is ROW.
    */
   public Cell[][] grid;
   
   public Board(int width, int height) {
     grid = new Cell[width][height];
   }
   
   /**
    * FIRST coordinate is COLUMN;
    * SECOND coordinate is ROW.
    */
   public Cell get(int n, int m) {
     return grid[n][m];
   }
 
   /**
    * FIRST coordinate is COLUMN;
    * SECOND coordinate is ROW.
    */
   public void set(int n, int m, Cell c) {
     grid[n][m] = c;
   }
   
   public static Board parse(String s) {
     List<List<Cell>> flippedBoard = new ArrayList<List<Cell>>();
     int colCount = -1;
     
     StringTokenizer tokenizer = new StringTokenizer(s, "\n");
     while (tokenizer.hasMoreTokens()) {
       String line = tokenizer.nextToken();
       List<Cell> row = new ArrayList<Cell>();
       flippedBoard.add(row);
       
       for (int i = 0; i < line.length(); i++)
         row.add(Cell.parse(line.charAt(i)));
       
       colCount = Math.max(colCount, line.length());
     }
     
     int rowCount = flippedBoard.size();
     
     Board board = new Board(colCount, rowCount);
     for (int row = 0; row < rowCount; row++)
       for (int col = 0; col < colCount; col++) {
        List<Cell> rowList = flippedBoard.get(row);
         if (col < rowList.size())
           board.grid[col][row] = rowList.get(col);
         else 
           // Section 2.5: "Shorter lines are assumed to be padded out with spaces"
           board.grid[col][row] = Cell.Empty;
       }
     
     return board;
   }
 }
