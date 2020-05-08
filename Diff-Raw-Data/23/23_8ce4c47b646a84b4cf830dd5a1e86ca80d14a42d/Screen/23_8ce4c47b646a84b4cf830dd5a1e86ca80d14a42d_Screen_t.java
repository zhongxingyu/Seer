 package com.voracious.graphics.components;
 
 import java.awt.event.KeyEvent;
 
 import com.voracious.graphics.Game;
 
 /**
  * Canvas that can be drawn on and used by Game
  * Allows for Game to switch between seperate activities.
  * 
  * @author Andrew Kallmeyer
  */
 public class Screen {
     private int[] pixels;
     private int width, height;
     private int offsetX, offsetY = 0;
 
     public Screen(int width, int height) {
         pixels = new int[width * height];
         this.width = width;
         this.height = height;
     }
 
     public void setPixel(int color, int x, int y) {
        if (color != -1 && !(width * y + x >= pixels.length)) {
             pixels[width * y + x] = color;
         }
     }
 
     public void clear(int color) {
         for (int i = 0; i < pixels.length; i++) {
             pixels[i] = color;
         }
     }
 
     public void draw(int[] drawSurface) {
         int y = 0;
         int x = 0;
         for (int i = 0; i < drawSurface.length; i++) {
             int myIndex = width * (offsetY + y) + offsetX + x;
             if (pixels[myIndex] != -1) {
                 drawSurface[i] = pixels[myIndex];
             }
             x++;
             if (x >= Game.WIDTH) {
                 y++;
                 x = 0;
             }
         }
     }
 
     public int getWidth() {
         return width;
     }
 
     public int getHeight() {
         return height;
     }
 
     public int getOffsetX() {
         return offsetX;
     }
 
     public int getOffsetY() {
         return offsetY;
     }
 
     public void setOffsetX(int offset) {
         this.offsetX = offset;
     }
 
     public void setOffsetY(int offset) {
         this.offsetY = offset;
     }
     
     /**
      * Called when a key is pressed if this screen is registered with input handler
      * @param e
      */
     public void keyPressed(KeyEvent e) {
     }
     
     /**
      * Called when a key is pressed if this screen is registered with input handler
      * @param e
      */
     public void keyReleased(KeyEvent e) {
     }
     
     /**
      * Do calculations here
      * Called every tick when this screen is active and the game is not paused
      */
     public void tick() {
     }
 
     /**
      * Draw sprites and other graphics here
      * Called every frame when this screen is active
      */
     public void render() {
     }
     
     /**
      * Do initializations here
      * Called when this screen is first made active
      */
     public void start() {
     }
 
     /**
      * Do garbage collection here
      * Called when this screen is made in-active
      */
     public void stop() {
     }
 }
