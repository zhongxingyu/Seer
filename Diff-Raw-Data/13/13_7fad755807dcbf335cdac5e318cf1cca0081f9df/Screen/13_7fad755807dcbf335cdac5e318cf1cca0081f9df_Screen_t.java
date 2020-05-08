 package me.limebyte.rain.graphics;
 
 import me.limebyte.rain.level.tile.Tile;
 
 public class Screen {
 
     public int width, height;
     public int[] pixels;
 
     private int xOffset, yOffset;
 
     public Screen(int width, int height) {
         this.width = width;
         this.height = height;
         pixels = new int[width * height];
     }
 
     public void clear() {
         for (int i = 0; i < pixels.length; i++) {
             pixels[i] = 0;
         }
     }
 
     public void renderTile(int xp, int yp, Tile tile) {
         xp -= xOffset;
         yp -= yOffset;
         for (int y = 0; y < tile.sprite.SIZE; y++) {
             int ya = yp + y;
             for (int x = 0; x < tile.sprite.SIZE; x++) {
                 int xa = xp + x;
                if (xa < -tile.sprite.SIZE || xa >= width || ya < 0 || ya >= height) break;
                if (xa < 0) xa = 0;
                 pixels[xa + ya * width] = tile.sprite.pixels[x + y * tile.sprite.SIZE];
             }
         }
     }
 
     public void setOffset(int xOffset, int yOffset) {
         this.xOffset = xOffset;
         this.yOffset = yOffset;
     }
 
 }
