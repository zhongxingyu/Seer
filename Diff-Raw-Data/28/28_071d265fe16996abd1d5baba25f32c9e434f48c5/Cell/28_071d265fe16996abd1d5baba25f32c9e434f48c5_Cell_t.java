 package core;
 
 public class Cell {
 
    private static boolean defValueSet = false;
     private static char defFigureValue = ' ';
 
     private int x;
     private int y;
     private char figure;
 
     public Cell(int x, int y, char defFigureValue) {
 
        if (!Cell.defValueSet) {

             Cell.defFigureValue = defFigureValue;
            Cell.defValueSet = true;
         }
 
         this.x = x;
         this.y = y;
         this.figure = Cell.defFigureValue;
     }
 
    public Cell(int x, int y) {

        this(x, y, Cell.getDefFigureValue());
    }

     public static char getDefFigureValue() {
 
        return Cell.defFigureValue;
     }
 
     public int getX() {
 
         return x;
     }
 
     public int getY() {
 
         return y;
     }
 
     public char getFigure() {
 
         return figure;
     }
 
     public boolean setFigure(char figureType) {
 
        if (figureType == Cell.defFigureValue || figure == Cell.defFigureValue) {
 
             figure = figureType;
             return true;
         }
 
         return false;
     }
 
     @Override
     public String toString() {
 
         return "" + figure;
     }
 }
