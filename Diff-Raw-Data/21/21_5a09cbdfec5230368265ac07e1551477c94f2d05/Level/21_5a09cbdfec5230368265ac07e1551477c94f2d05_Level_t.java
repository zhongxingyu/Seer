 /*
  * Level Generation
  * and Level Updating
  */
 package mastergardner.level;
 
 import mastergardner.graphics.Screen;
 import mastergardner.level.tile.Tile;
 
 /**
  *
  * @author Christian
  */
 public class Level {
     
     /**
      * Width & Height of the Map
      */
     protected int width, height;
     protected int[] tiles;
     
     public Level(int width, int height) {
         this.width = width;
         this.height = height;
         tiles = new int[width * height];
          generateLevel();
     }
     
     public Level(String path) {
         loadLevel(path);
     }
 
     
     protected void generateLevel() {
     }
     
     public void loadLevel(String path) {
     }
     
     public void update() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
     private void time() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
     /**
      *
      * @param xScroll
      * @param yScroll
      * @param screen
      */
     public void render(int xScroll, int yScroll, Screen screen) {
         screen.setOffset(xScroll, yScroll);
         
         int x0, y0, x1, y1;
         x0 = xScroll >> 4;
        x1 = (xScroll + screen.width + 16) >> 4; //change to Sprite.SIZE in the future
         y0 = yScroll >> 4;
        y1 = (yScroll + screen.height + 16) >> 4; //change to Sprite.SIZE in the future
         
         for (int y = y0; y < y1; y++) {
             for (int x = x0; x < x1; x++) {
                 getTile(x, y).render(x, y, screen);
             }
             
         }
     }
     
     /**
      *
      * @param x
      * @param y
      * @return
      */
     public Tile getTile(int x, int y) {
         if(x < 0 || y < 0 || x >= width || y >= height) return Tile.voidTile; //out of boundarys preventer
         
         if (tiles[x + y * width] == 0) return Tile.grass;
         return Tile.voidTile;
     }
     
 }
