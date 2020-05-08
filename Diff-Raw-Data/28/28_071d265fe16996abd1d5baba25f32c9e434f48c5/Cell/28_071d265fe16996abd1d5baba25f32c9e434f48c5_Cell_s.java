 package core;
 
 public class Cell {
 
    private static boolean defValueSet;
     private static char defFigureValue = ' ';
 
     private int x;
     private int y;
     private char figure;
 
     public Cell(int x, int y, char defFigureValue) {
 
        if (!defValueSet) {
             Cell.defFigureValue = defFigureValue;
            defValueSet = true;
         }
 
         this.x = x;
         this.y = y;
         this.figure = Cell.defFigureValue;
     }
 
     public static char getDefFigureValue() {
 
        return defFigureValue;
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
 
        if (figureType == defFigureValue || figure == defFigureValue) {
 
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
