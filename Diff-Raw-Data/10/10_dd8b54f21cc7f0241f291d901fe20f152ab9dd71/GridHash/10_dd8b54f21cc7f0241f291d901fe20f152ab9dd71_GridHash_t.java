 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package be.kuleuven.socialmap.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A class that divides a rectangle into a grid. So when objects are added to
  * the rectangle, they are hashed to their respective unit in the grid, which
  * makes for faster lookup.
  *
  * @author Jasper Moeys
  */
 public class GridHash<T> {
 
     private List<T>[] hash;
     private int width;
     private int height;
     private int rowCount;
     private int unitCount;
     private double stepX;
     private double stepY;
 
     /**
      * Constructs a new GridHash. The amount of units in the grid will be
      * rowCount^2.
      *
      * @param width The width of the rectangle. Has to be greater than 0.
      * @param height The height of the rectangle. Has to be greater than 0.
      * @param rowCount The amount of rows in de grid. Has to be greater than 0.
      * @throws IllegalArgumentException If one of the above restrictions is
      * violated.
      */
     public GridHash(int width, int height, int rowCount) {
         if (width <= 0) {
             throw new IllegalArgumentException("Width has to be greater than 0.");
         }
         if (height <= 0) {
             throw new IllegalArgumentException("Height has to be greater than 0.");
         }
         if (rowCount <= 0) {
             throw new IllegalArgumentException("RowCount has to be greater than 0.");
         }
 
         this.height = height;
         this.width = width;
         this.rowCount = rowCount;
         this.unitCount = rowCount * rowCount;
         this.stepX = (double) width / rowCount;
         this.stepY = (double) height / rowCount;
         this.hash = (List<T>[]) new List[unitCount];
 
         for (int i = 0; i < unitCount; i++) {
             hash[i] = new ArrayList<T>();
         }
     }
 
     /**
      * Adds an object with (x,y) coordinates to the grid.
      *
      * @param object The object that has to be added.
      * @param x The x coordinate of the object in the rectangle.
      * @param y The y coordinate of the object in the rectangle.
      * @throws IllegalArgumentException If x or y don't lie within the bounds of
      * the rectangle.
      */
     public void addObject(T object, int x, int y) {
         if (x > width || y > height || x < 0 || y < 0) {
             throw new IllegalArgumentException("This object doesn't lie within the bounds of the specified rectangle.");
         }
         hash[calculateIndex(x, y)].add(object);
     }
 
     /**
      * Returns a list of all objects of the unit where the specified (x,y)
      * coordinate is located.
      *
      * @param x The x coordinate of the object in the rectangle.
      * @param y The y coordinate of the object in the rectangle.
      * @return The list of all objects inside the corresponding unit of the
      * grid.
      */
     public List<T> getUnitList(int x, int y) {
         return hash[calculateIndex(x, y)];
     }
 
     private int calculateIndex(int x, int y) {
         int gridX = (int) (x / stepX);
         int gridY = (int) (y / stepY);
        if (gridX >= rowCount) {
            gridX = rowCount - 1;
        }
        if (gridY >= rowCount) {
            gridY = rowCount - 1;
         }
        int index = gridX + gridY * rowCount;
         return index;
     }
 
     /**
      * Clears all objects from the grid.
      */
     public void clear() {
         for (int i = 0; i < unitCount; i++) {
             hash[i].clear();
         }
     }
 }
