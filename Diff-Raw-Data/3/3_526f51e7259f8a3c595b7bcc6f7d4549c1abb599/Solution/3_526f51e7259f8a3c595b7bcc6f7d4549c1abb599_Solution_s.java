 /**
  * @author raido
  */
 public class Solution {
 
    private Cells cells;
    
    public Solution(Cells cells) {
       this.cells = cells;
       int size = cells.getSize();
       long start = System.currentTimeMillis();
       if (solve(size, 0)) {
          System.out.println("Solution found in "+(System.currentTimeMillis()-start)+" ms!");
          output();
       }
    }
    
    public void output() {
       cells.output();
    }
    
    public Cells getCells() {
       return cells;
    }
 
    private boolean solve(int size, int count) {
      if (++count == (size*size)) return true;
       int row = count / size;
       int col = count % size;
       
       if (cells.getCell(row, col) == 0) { // is cell preset or not?
          for (int val = 1; val <= size; val++) { // loop through valid values
             if (isLegal(row,col,  val)) { // can this value be put in current cell
                cells.setCell(row, col, val); // sets cell's value
                if (solve(size, count)) return true;
             }
          } // end of loop through valid values
          cells.setCell(row, col, 0); // didn't find suitable value, reset cell
       } else { // next cell if this one is not empty
          if (solve(size, count)) return true;
       }
       return false;
    }
    
    private boolean isLegal(int row, int col, int value) {
       int i = 0, j = 0;
       int top = row / 3;
       int left = col / 3;
       for (int[] rowOfCells : cells.getCells()) {
          for (int cellValue : rowOfCells) {
             if ((j == row || i == col || (left == i/3 && top == j/3)) && cellValue == value) {
                return false;
             }
             i++;
          }
          j++;
          i = 0;
       }
       return true;
    }
    
 }
