 package interiores.business.models;
 
import interiores.utils.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 /**
  * This class extends java.awt.Rectangle to be used with orientations.
  * Rotations will not change the position of the upper-left corner.
  * @author alvaro
  */
 public class OrientedRectangle extends Rectangle {
     private Orientation orientation;
     
     public OrientedRectangle(int x, int y, int w, int h, Orientation orientation) {
         super(x, y, w, h);
         this.orientation = orientation; 
     }
     
     public OrientedRectangle(Point point, Dimension dimension, Orientation orientation) {
        super(point);
        
        this.setSize(dimension.width, dimension.depth);
         this.orientation = orientation; 
     }
     
     /**
      * Rotates the rectangle to the left.
      */
     public void rotateLeft() {
         setOrientation(this.orientation.rotateLeft());
     }
     
     /**
      * Rotates the rectangle to the right.
      */
     public void rotateRight() {
         setOrientation(this.orientation.rotateRight());
     }
     
     /**
      * Returns a rectangle expanded for all its sides by size
      * @param size The distance of the expansion
      * @return A new rectangle expanded
      */
     public OrientedRectangle enlarge(int size) {
         int newX = x - size;
         int newY = y - size;
         int newW = width + size;
         int newH = height + size;
         return new OrientedRectangle(newX, newY, newW, newH, this.orientation);
     }
     
     /**
      * Returns a rectangle with its width and height expanded independently
      * @param dimension The width and the height it should expand
      * @return A new rectangle expanded
      */
     public OrientedRectangle enlarge(Dimension dimension) {
         int newX = x - dimension.width;
        int newY = y - dimension.depth;
         int newW = width + dimension.width;
        int newH = height + dimension.depth;
         return new OrientedRectangle(newX, newY, newW, newH, this.orientation);
     }
     
     /**
      * Returns a rectangle with the side corresponding to <orientation> expanded
      * @param size The size of the expansion
      * @param orientation The orientation to expand
      * @return A new rectangle expanded
      */
     public OrientedRectangle enlarge(int size, Orientation orientation) {
         int newX = x;
         int newY = y;
         int newW = width;
         int newH = height;
         switch(orientation) {
             case W:
                 newX -= size;
             case N:
                 newY -= size;
             case E:
                 newW += size;
             case S:
                 newH += size;
         }
         return new OrientedRectangle(newX, newY, newW, newH, this.orientation);
     }
     
     /**
      * Rotates the rectangle to an arbritrary orientation.
      * @param orientation The orientation the rectangle will be facing
      */
     public void setOrientation(Orientation orientation) {
 
         int tr = this.orientation.distanceRight(orientation);
         
         if (tr % 2 != 0) {
             // To rotate the rectangle without moving its position we swap
             // the height and the width only if we turned an odd number of times;
             this.setSize(this.height, this.width);
         }
         
         this.orientation = orientation;  
     }
     
     /**
      * Get the orientation of the rectangle.
      * @return The current orientation of the rectangle
      */
     public Orientation getOrientation() {
         return this.orientation;
     }
     
     @Override
     public String toString() {
         return super.toString() + " " + this.orientation;
         
     }
 }
