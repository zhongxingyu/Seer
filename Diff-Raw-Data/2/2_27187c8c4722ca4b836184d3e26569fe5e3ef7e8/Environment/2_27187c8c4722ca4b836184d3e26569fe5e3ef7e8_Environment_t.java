 package es.deusto.ingenieria.maze;
 
 import java.awt.Point;
 
 public class Environment {
     
     private Cell[][] cells;
     private Point startLocation;
     private Point endLocation;
     private Point currentLocation;
     
     /**
      * Constructor to be used the first time the enviroment is created. The
      * current location will be equal to the start location.
      */
     public Environment(Cell[][] cells, Point startLocation, Point endLocation) {
         this.cells = cells;
         this.startLocation = startLocation;
         this.endLocation = endLocation;
         this.currentLocation = startLocation;
     }
     
     private Environment(Cell[][] cells, Point startLocation,
                         Point endLocation, Point currentLocation) {
         this.cells = cells;
         this.startLocation = startLocation;
         this.endLocation = endLocation;
         this.currentLocation = currentLocation;
     }
     
     public Cell getCellAt(int column, int row) {
         return cells[column][row];
     }
     
     public Cell getCellAt(Point p) {
         return getCellAt(p.x, p.y);
     }
     
     public void setCellAt(int column, int row, Cell cell) {
         cells[column][row] = cell;
     }
     
     public void setCellAt(Point p, Cell cell) {
         setCellAt(p.x, p.y, cell);
     }
     
     public int getRowCount() {
         return cells[0].length;
     }
 
     public int getColumnCount() {
         return cells.length;
     }
     
     public Point getEndLocation() {
         return endLocation;
     }
 
     public void setEndLocation(Point endLocation) {
         this.endLocation = endLocation;
     }
 
     public Point getStartLocation() {
         return startLocation;
     }
 
     public void setStartLocation(Point startLocation) {
         this.startLocation = startLocation;
     }
     
     public Point getCurrentLocation() {
         return currentLocation;
     }
 
     public void setCurrentLocation(Point currentLocation) {
         this.currentLocation = currentLocation;
     }
 
     public Cell[][] getCells() {
         return cells;
     }
 
     public void setCells(Cell[][] cells) {
         this.cells = cells;
     }
     
     /**
      * A factory method that clones this Environment, creating a new derived
      * one with the currentLocation given as parameter.
      * 
      * Use this to create new derived environments and not the public
      * constructor, which is intended only to be used the first time the
      * environment is created.
      * 
      * @return the new derived Environment
      */
     public Environment derive(Point newCurrentLocation) {
         return new Environment(cells, startLocation, endLocation,
                                newCurrentLocation);
     }
 
     @Override
     public boolean equals(Object obj) {
         if ((obj != null)&&(obj instanceof Environment)) {
             return 
                ((Environment)obj).getCurrentLocation().equals(this.currentLocation);
         } else
             return false;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 29 * hash + (this.currentLocation != null
                             ? this.currentLocation.hashCode()
                             : 0);
         return hash;
     }
     
 }
