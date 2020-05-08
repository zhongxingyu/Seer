 package edu.ualberta.med.scannerconfig.preferences.scanner.plateposition;
 
 import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
 
 /**
  * Tracks the grid's attributes for the currently scanned plate image.
  * 
  */
 public class PlateGrid<T extends Number> {
     public enum Orientation {
         LANDSCAPE,
         PORTRAIT
     };
 
     protected String name;
 
     protected T left;
 
     protected T top;
 
     protected T width;
 
     protected T height;
 
     protected Orientation orientation;
 
     private int rows;
 
     private int columns;
 
     public PlateGrid() {
         left = top = width = height = null;
         orientation = Orientation.LANDSCAPE;
         rows = 8;
         columns = 12;
     }
 
     public PlateGrid(String name, T left, T top, T right, T bottom,
         Orientation orientation, int rows, int columns) {
         set(name, left, top, right, bottom, orientation, rows, columns);
     }
 
     public PlateGrid(PlateGrid<T> region) {
         set(region);
     }
 
     public void set(String name, T left, T top, T right, T bottom,
         Orientation orientation, int rows, int columns) {
         this.name = name;
         this.left = left;
         this.top = top;
         this.width = right;
         this.height = bottom;
         this.orientation = orientation;
         this.rows = rows;
         this.columns = columns;
     }
 
     public void set(PlateGrid<T> region) {
         set(region.name, region.left, region.top, region.width, region.height,
             region.orientation, region.getRows(), region.getColumns());
     }
 
     public T getLeft() {
         return left;
     }
 
     public void setLeft(T left) {
         this.left = left;
     }
 
     public T getTop() {
         return top;
     }
 
     public void setTop(T top) {
         this.top = top;
     }
 
     public T getWidth() {
         return width;
     }
 
     public void setWidth(T width) {
         this.width = width;
     }
 
     public T getHeight() {
         return height;
     }
 
     public void setHeight(T height) {
         this.height = height;
     }
 
     public Orientation getOrientation() {
         return orientation;
     }
 
     public void setOrientation(Orientation orientation) {
         this.orientation = orientation;
     }
 
     @SuppressWarnings("nls")
     public void setOrientation(String orientationStr) {
         if (orientationStr.equals(PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE)) {
             this.orientation = Orientation.LANDSCAPE;
         } else if (orientationStr.equals(PreferenceConstants.SCANNER_PALLET_ORIENTATION_PORTRAIT)) {
             this.orientation = Orientation.PORTRAIT;
         } else {
             throw new RuntimeException("orientation string invalid: " + orientationStr);
         }
     }
 
     @SuppressWarnings("nls")
     @Override
     public String toString() {
         return "left/" + left + " top/" + top + " width/" + width + " height/"
             + height;
     }
 
     public static String orientationToString(Orientation orientation) {
         switch (orientation) {
         case LANDSCAPE:
             return PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE;
         case PORTRAIT:
             return PreferenceConstants.SCANNER_PALLET_ORIENTATION_PORTRAIT;
         default:
             return null;
         }
     }
 
     public static Orientation orientationFromString(String orientation) {
         if (orientation.equals(PreferenceConstants.SCANNER_PALLET_ORIENTATION_LANDSCAPE))
             return Orientation.LANDSCAPE;
         else if (orientation.equals(PreferenceConstants.SCANNER_PALLET_ORIENTATION_PORTRAIT))
             return Orientation.PORTRAIT;
         else return Orientation.LANDSCAPE;
     }
 
     public int getRows() {
         return rows;
     }
 
     public void setRows(int rows) {
         this.rows = rows;
     }
 
     public int getColumns() {
         return columns;
     }
 
     public void setColumns(int columns) {
         this.columns = columns;
     }
 
     public void setGridDimensions(int rows, int columns) {
         this.rows = rows;
         this.columns = columns;
     }
 
     @SuppressWarnings("nls")
     public void setGridDimensions(String dimensionsStr) {
         if (dimensionsStr.equals(PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS8COLS12)) {
            this.rows = 8;
            this.columns = 12;
         } else if (dimensionsStr.equals(PreferenceConstants.SCANNER_PALLET_GRID_DIMENSIONS_ROWS10COLS10)) {
             this.rows = 10;
             this.columns = 10;
         } else {
             throw new RuntimeException("dimensions string invalid: " + dimensionsStr);
         }
     }
 }
