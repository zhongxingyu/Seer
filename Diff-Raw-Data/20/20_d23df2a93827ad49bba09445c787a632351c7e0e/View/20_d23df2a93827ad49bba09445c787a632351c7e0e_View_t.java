 package com.github.pmcompany.pustomario.io;
 
 /**
  * @author dector (dector9@gmail.com)
  */
 public class View {
     public static final int TILE_WIDTH = 16;
     public static final int TILE_HEIGHT = 16;

//    public static final int TILE_WIDTH = 32;
//    public static final int TILE_HEIGHT = 32;
 
     public static final PColor WALL_COLOR = PColor.BLACK;
     public static final PColor HERO_COLOR = PColor.RED;
     public static final PColor EYE_COLOR = PColor.BLACK;
     public static final PColor BACK_COLOR = new PColor(0.3f, 0.3f, 0.3f, 0.3f);
     private int leftX;
     private int leftY;
 
     public View() {
     }
 
     public int getLeftX() {
         return leftX;
     }
 
     public void setLeftX(int leftX) {
         this.leftX = leftX;
     }
 
     public int getLeftY() {
         return leftY;
     }
 
     public void setLeftY(int leftY) {
         this.leftY = leftY;
     }
 
     public int getTileX(int absoluteX) {
         return absoluteX / TILE_WIDTH;
     }
 
     public int getTileY(int absoluteY) {
         return absoluteY / TILE_HEIGHT;
     }
 }
