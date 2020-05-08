 /*
  * Load all sprites
  *
  */
 package mastergardner.graphics;
 
 /**
  *
  * @author Christian
  */
 public class Sprite {
     
     public final int SIZE;
     private int x, y;
     public int[] pixels;
     private SpriteSheet sheet;
     
    // Sprites, make sure to leave size at 16 or change it at mastergardner.level.Level, in the x0, etc. setting.
     public static Sprite grass = new Sprite(16, 0, 0, SpriteSheet.tiles);
     public static Sprite voidTile = new Sprite(16, 0x1B87E0); //SIZE, COLOR
     
     /**
      * Constructor for accessing tiles from the spritesheet
      * @param size
      * @param x
      * @param y
      * @param sheet
      */
     public Sprite(int size, int x, int y, SpriteSheet sheet) {
         SIZE = size;
         pixels = new int[SIZE * SIZE];
         this.x = x * SIZE;
         this.y = y * SIZE;
         this.sheet = sheet;
         load();
     }
     
     /**
      * Alternative Constructor for generating pure colored tiles
      * @param size
      * @param color
      */
     public Sprite(int size, int color) {
         SIZE = size;
         pixels = new int[SIZE * SIZE];
         setColor(color);
     }
     
     private void load() {
         for (int Ly = 0; Ly < SIZE; Ly++) {
             for (int Lx = 0; Lx < SIZE; Lx++) {
                 pixels[Lx + Ly * SIZE] = sheet.pixels[( Lx + this.x) + (Ly + this.y) * sheet.SIZE];
             }
         }
     }
 
     private void setColor(int color) {
         for (int i = 0; i < SIZE*SIZE; i++) {
             pixels[i] = color;
         }
     }
 }
