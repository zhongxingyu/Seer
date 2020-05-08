 package org.sutemi.sudoku;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Created with IntelliJ IDEA.
  * User: chris
  * Date: 6/5/13
  * Time: 10:16 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SudokuGrid {
     List[][] grid;
 
     Logger logger = LoggerFactory.getLogger("org.sutemi.sudoku.SudokuGrid");
 
     public SudokuGrid() {
         grid = new List[9][9];
         for (int i = 0; i < 9; i++) {
             for (int j = 0; j < 9; j++) {
                 grid[i][j] = new ArrayList<Integer>();
                 for (int k = 1; k < 10; k++) {
                     grid[i][j].add(k);
                 }
             }
         }
     }
 
     public SudokuGrid(List<List<Integer>> givens) {
         this();
         if (givens.size() != 9) {
             throw new IllegalArgumentException("Insufficient rows");
         }
         for(List<Integer> rows: givens) {
             if (rows.size() != 9) {
                 throw new IllegalArgumentException("Insufficient columns");
             }
         }
         for (int row = 0; row < 9; row++) {
             List<Integer> rowgivens = givens.get(row);
             for (int col = 0; col < 9; col++) {
                 if (rowgivens.get(col) != 0) {
                     placeGiven(new CellPoint(row,col),rowgivens.get(col));
                 }
             }
         }
     }
 
     public SudokuGrid(String givens) {
         this();
         if (givens == null) {
             throw new IllegalArgumentException("Null givens");
         }
 
         if (givens.length() != 81) {
             throw new IllegalArgumentException("Too many or too few givens");
         }
 
         for (int i = 0; i < 81; i++) {
             char givenchar = givens.charAt(i);
             if (Character.isDigit(givenchar)) {
                 int given = Character.digit(givenchar, 10);
                 if (given != 0) {
                     placeGiven(new CellPoint(i/9,i%9),given);
                 }
             }
         }
 
     }
 
     public SudokuGrid(SudokuGrid that) {
         grid = new List[9][9];
         for (int i = 0; i < 9; i++) {
             for (int j = 0; j < 9; j++) {
                 grid[i][j] = new ArrayList<Integer>();
                 for (int k = 1; k < 10; k++) {
                     if (that.grid[i][j].contains(k)) {
                         grid[i][j].add(k);
                     }
                 }
             }
         }
     }
 
     public boolean isSolution() {
         for (int i = 0; i < 9; i++) {
             for (int j = 0; j < 9; j++) {
                 if (grid[i][j].size() != 1) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     public List<CellPoint> getRowPoints(CellPoint point) {
         List<CellPoint> rowPoints = new ArrayList<CellPoint>();
         for (int i = 0; i < 9; i++) {
             if (i != point.getCol()) {
                 rowPoints.add(new CellPoint(point.getRow(), i));
             }
         }
         return rowPoints;
     }
 
     public List<CellPoint> getColPoints(CellPoint point) {
         List<CellPoint> colPoints = new ArrayList<CellPoint>();
         for (int i = 0; i < 9; i++) {
             if (i != point.getRow()) {
                 colPoints.add(new CellPoint(i, point.getCol()));
             }
         }
         return colPoints;
     }
 
     public List<CellPoint> getPeerPoints(CellPoint point) {
         int rowOffset = point.getRow() / 3; //int division
         int colOffset = point.getCol() / 3; //int division
         List<CellPoint> peerPoints = new ArrayList<CellPoint>();
         for (int i = 0; i < 3; i++) {
             for (int j = 0; j < 3; j++) {
                 int row = i + (rowOffset * 3);
                 int col = j + (colOffset * 3);
                 if (!(row == point.getRow() && col == point.getCol())) {
                     peerPoints.add(new CellPoint(row,col));
                 }
             }
         }
         return peerPoints;
     }
 
     public List<Integer> getPossibilities(CellPoint cellPoint) {
         return new ArrayList<Integer>(grid[cellPoint.getRow()][cellPoint.getCol()]);
     }
 
     public void placeGiven(CellPoint cellPoint, int i) {
         place(cellPoint, i);
     }
 
     public SudokuGrid eliminatePossibility(CellPoint point, Integer possibility) {
         SudokuGrid newGrid = new SudokuGrid(this);
         return newGrid.eliminate(point,possibility);
     }
 
     private SudokuGrid eliminate(CellPoint point, int i) {
         logger.debug("Eliminating possibility {} at position ({},{})",i,point.getRow(),point.getCol());
         List<Integer> cell = grid[point.row][point.col];
         if (cell.contains(i)) {
             if (cell.size() == 1) {
                return this;
             }
             cell.remove((Integer)i); //cast so that we call remove(object) not remove(index)
            //logger.debug("Possibilities left: {}", cell.size());
             if (cell.size() == 1) {
                 return place(point,cell.get(0));
            } else if (cell.size() == 0) {
                throw new IllegalStateException("Removed all possibilities");
             } else {
                 return this;
             }
         } else {
             return this; // is it okay to eliminate a possibility multiple times?
         }
     }
 
     private SudokuGrid place(CellPoint cellPoint, int i) {
         logger.debug("Placing value {} at position ({},{})",i,cellPoint.getRow(),cellPoint.getCol());
 
         List<Integer> cell = grid[cellPoint.getRow()][cellPoint.getCol()];
         if (!cell.contains(i)) {
             return null;
         }
         cell.clear();
         cell.add(i);
         for (CellPoint point : getRowPoints(cellPoint)) {
             SudokuGrid modified = eliminate(point, i);
         }
         for (CellPoint point : getColPoints(cellPoint)) {
             SudokuGrid modified = eliminate(point, i);
         }
         for (CellPoint point : getPeerPoints(cellPoint)) {
             SudokuGrid modified = eliminate(point, i);
         }
         return this;
     }
 
     public SudokuGrid placeConjecture(CellPoint cellPoint, int i) {
         logger.debug("Placing conjecture {} at position ({},{})",i,cellPoint.getRow(),cellPoint.getCol());
         SudokuGrid newGrid = new SudokuGrid(this);
         return newGrid.place(cellPoint, i);
     }
 
     public String toString() {
         StringBuffer sb = new StringBuffer();
 
         for (int row = 0; row < 9; row++) {
             for (int col = 0; col < 9; col++) {
                 List<Integer> cell = grid[row][col];
                 if (cell.size() == 1) {
                     sb.append(cell.get(0));
                 } else {
                     sb.append(".");
                 }
 
                 if (col != 8) {
                     sb.append(" ");
                 }
                 if (col == 2 || col == 5) {
                     sb.append("| ");
                 }
             }
             if (row != 8) {
                 sb.append("\n");
             }
             if (row == 2 || row == 5) {
                 sb.append("---------------------\n");
             }
         }
         return sb.toString();
     }
 
     public CellPoint getMinimumPossibilityCell() {
         int minrow = 0;
         int mincol = 0;
         int minpossibilities = 9;
 
         for (int row = 0; row < 9; row++) {
             for (int col = 0; col < 9; col++) {
                 int possibilities = grid[row][col].size();
                 if (possibilities > 1 && possibilities < minpossibilities) {
                     minrow = row;
                     mincol = col;
                     minpossibilities = possibilities;
                 }
             }
         }
 
         if (grid[minrow][mincol].size() == 1) {
             return null; // bug
         }
         return new CellPoint(minrow,mincol);
     }
 }
