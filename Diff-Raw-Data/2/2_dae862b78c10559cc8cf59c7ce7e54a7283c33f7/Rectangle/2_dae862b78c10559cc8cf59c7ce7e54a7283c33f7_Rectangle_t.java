 package training4;
 
 public class Rectangle {
 
     private int x;
     private int y;
     private int height;
     private int width;
 
     /**
      * Creates a rectangle with the buttom-left corner described by x and y with
      * the given length in 2D.
      * 
      * @param x
      *            Coordinate of buttom-left corner
      * @param y
      *            Coordinate of buttom-left corner
      * @param height
      *            Height of the rectangle
      * @param width
      *            Width of the rectangle
      */
     public Rectangle(int x, int y, int height, int width) {
         if (height <= 0 || width <= 0)
             throw new RuntimeException("Height or width is zero or negativ!");
         this.x = x;
         this.y = y;
         this.height = height;
         this.width = width;
     }
 
     /**
      * @return Buttom-left corner x-Coordinate
      */
     public int getX() {
         return x;
     }
 
     public void setX(int x) {
         this.x = x;
     }
 
     /**
      * @return Buttom-left corner y-Coordinate
      */
     public int getY() {
         return y;
     }
 
     public void setY(int y) {
         this.y = y;
     }
 
     /**
      * @return Height of the rectangle
      */
     public int getHeight() {
         return height;
     }
 
     public void setHeight(int height) {
         this.height = height;
     }
 
     /**
      * @return Width of the rectangle
      */
     public int getWidth() {
         return width;
     }
 
     public void setWidth(int width) {
         this.width = width;
     }
 
     @Override
     public String toString() {
         return "x=" + x + ",y=" + y + " , width=" + width + " , height="
                 + height;
     }
 
     /**
      * Creates the smalles rectangle containing this and r2.
      * 
      * @param r2
      *            The other rectangle
      * @return A new Rectangle big enough to contain both
      */
     public Rectangle union(Rectangle r2) {
 
         return new Rectangle(Math.min(x, r2.getX()), Math.min(y, r2.getY()),
                 Math.max(width, r2.getWidth()),
                 Math.max(height, r2.getHeight()));
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null || !(obj instanceof Rectangle))
             return false;
         Rectangle r2 = (Rectangle) obj;
 
         return r2.getX() == this.x && r2.getY() == this.y
                 && r2.getWidth() == width && r2.getHeight() == height;
     }
 
     public boolean intersect(Rectangle r2) {
         // when both rectangles have the same attributs
         if (equals(r2))
             return true;
         // attributes of the other rectangle
         int x1 = r2.getX();
         int y1 = r2.getY();
         int height1 = r2.getHeight();
         int width1 = r2.getWidth();
 
         // check the corners of the other rectangle are inside this rectangle
         return isInside(x1 + width1, y1 + height1, x, y, width, height)
                 || isInside(x1, y1 + height1, x, y, width, height)
                 || isInside(x1 + width1, y1, x, y, width, height)
                 || isInside(x1, y1, x, y, width, height)
 
                 // check the corners of this rectangle are inside the other
                 // rectangle
                 || isInside(x + width, y + height, x1, y1, width1, height1)
                 || isInside(x, y + height, x1, y1, width1, height1)
                 || isInside(x + width, y, x1, y1, width1, height1)
                 || isInside(x, y, x1, y1, width1, height1);
     }
 
     /**
      * @param xP
      *            x-Coordinate of the point
      * @param yP
      *            y-Coordinate of the point
      * @param xR
      *            Buttom-left corner x-Coordinate
      * @param yR
      *            Buttom-left corner y-Coordinate
      * @param width
      *            The width of the rectangle
      * @param height
      *            The width of the rectangle
      * @return True when the point given by xP and yP is in the rectangle
      */
     public boolean isInside(int xP, int yP, int xR, int yR, int width,
             int height) {
         return (xP <= xR + width) && (xP >= xR) && (yP <= yR + height)
                 && (yP >= yR);
     }
 
 }
