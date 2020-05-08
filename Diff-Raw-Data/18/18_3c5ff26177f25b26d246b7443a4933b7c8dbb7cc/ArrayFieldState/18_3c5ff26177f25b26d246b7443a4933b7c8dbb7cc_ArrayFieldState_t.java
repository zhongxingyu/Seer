 package ru.alepar.minesweeper;
 
 public class ArrayFieldState implements FieldState {
 
     private final Cell[][] cells;
 
     public ArrayFieldState(Cell[][] cells) {
         assertEqualWidth(cells);
         this.cells = cells;
     }
 
     @Override
     public int width() {
         return cells[0].length;
     }
 
     @Override
     public int height() {
         return cells.length;
     }
 
     @Override
     public Cell cellAt(Point p) {
         return cells[p.y][p.x];
     }
 
     private static void assertEqualWidth(Cell[][] cells) {
         if(cells == null) {
             throw new IllegalArgumentException("null cells are not permitted");
         }
         if(cells.length < 1) {
             return;
         }
         for (int i = 1; i < cells.length; i++) {
             if(cells[i-1].length != cells[i].length) {
                 throw new IllegalArgumentException("inconsistent width in rows");
             }
         }
     }
 
     @Override
     public int hashCode() {
         int h = 0;
         for (Cell[] row : cells) {
             for (Cell cell : row) {
                 h = h*31 + cell.hashCode();
             }
         }
         return h;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         ArrayFieldState that = (ArrayFieldState) o;
 
         if(cells.length != that.cells.length) {
             return false;
         }
 
         for (int i = 0; i < cells.length; i++) {
             if(cells[i].length != that.cells[i].length) {
                 return false;
             }
             for (int j = 0; j < cells[i].length; j++) {
                 if(!cells[i][j].equals(that.cells[i][j])) {
                     return false;
                 }
             }
         }
 
         return true;
     }
 
     public ArrayFieldState mutate(Point p, Cell cell) {
         cells[p.y][p.x] = cell;
         return this;
     }
 
     @Override
     public String toString() {
         StringBuilder stateString = new StringBuilder();
        for (Cell[] row : cells) {
             stateString.append('\t');
            for (Cell cell : row) {
                stateString.append(toChar(cell));
             }
             stateString.append('\n');
         }
         return "ArrayFieldState{\n" + stateString.toString() + '}';
     }
 
     private char toChar(Cell cell) {
         if (cell.isOpened()) {
             return (char)((int)'0' + cell.value);
         }
         if (cell == Cell.BOMB) {
             return 'x';
         }
         if (cell == Cell.CLOSED) {
             return '.';
         }
         throw new IllegalArgumentException("don't know how to translate this cell to char: " + cell);
     }
 }
