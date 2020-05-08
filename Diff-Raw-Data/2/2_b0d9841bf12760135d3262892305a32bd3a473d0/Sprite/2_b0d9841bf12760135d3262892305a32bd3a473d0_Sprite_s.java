 package me.limebyte.rain.graphics;
 
 import java.awt.Color;
 
 public class Sprite {
 
     public final int SIZE;
     private int x, y;
     public int[] pixels;
     private SpriteSheet sheet;
 
     /** Terrain **/
     public static Sprite voidSprite = new Sprite(16, new Color(0x1B87E0));
     public static Sprite grass = new Sprite(16, 0, 0, SpriteSheet.terrain);
 
     /** Entity **/
    public static Sprite player0 = new Sprite(16, 0, 0, SpriteSheet.sprites);
     public static Sprite player1 = new Sprite(16, 0, 1, SpriteSheet.sprites);
     public static Sprite player2 = new Sprite(16, 0, 2, SpriteSheet.sprites);
     public static Sprite player3 = new Sprite(16, 0, 3, SpriteSheet.sprites);
 
     public Sprite(int size, int x, int y, SpriteSheet sheet) {
         this.SIZE = size;
         this.x = x * size;
         this.y = y * size;
         this.pixels = new int[SIZE * SIZE];
         this.sheet = sheet;
         load();
     }
 
     public Sprite(int size, Color colour) {
         this.SIZE = size;
         this.pixels = new int[SIZE * SIZE];
         loadColour(colour);
     }
 
     private void load() {
         for (int y = 0; y < SIZE; y++) {
             for (int x = 0; x < SIZE; x++) {
                 pixels[x + y * SIZE] = sheet.getPixels()[(x + this.x) + (y + this.y) * sheet.getSize()];
             }
         }
     }
 
     private void loadColour(Color colour) {
         for (int y = 0; y < SIZE; y++) {
             for (int x = 0; x < SIZE; x++) {
                 pixels[x + y * SIZE] = colour.getRGB();
             }
         }
     }
 }
