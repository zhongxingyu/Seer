 package mapmaker;
 
 import java.awt.Graphics;
 import java.awt.*;
 import java.util.*;
 
 /**
  * A Map is a three layered tile map. Layer 1 is the base layer. If a tile in
  * this layer is null, it is replaced by the base tile (the first tile in the
  * tile set).<p>
  * Layer 2 is a special layer in which tiles are rendered in front of or behind
  * any sprites, depending on its location.<p>
  * Layer 3 is rendered last, over the top of everything else.<p>
  *
  * Tiles do not have to be the same size. A standard tile is 32 by 32 pixels. If
  * a tile is larger, it will be rendered in place, with the origin at the
  * bottom-right corner of the image. This means a large tile will extend above
  * and to the left over the top of other tiles behind it.<p>
  *
 * the map is only responsble for drawing unchanging tilesets. All interactive
  * stuff usually happens in the "Scene", using sprites, although the map does
  * support changing tiles on the fly if for example a switch were to move a
  * wall.
  *
  *
  */
 public class Map {
     /* change the below two numbers if you want the grid size to be different.
      * individual tile images may be any size. */
 
     int tileWidth = 32;
     int tileHeight = 32;
     int zoomWidth = 32;
     int zoomHeight = 32;
     int viewWidth = 400;
     int viewHeight = 400;
     GraphicsBank gfx;
     ArrayList changeListeners;
     final static int LAYERS = 3;
     Tile[][][] tiles; //ground layer
 
     /**
      * Maps are constructed with a width and height, originally having all null
      * tiles.
      */
     public Map(int width, int height) {
         tiles = new Tile[width][height][LAYERS];
         changeListeners = new ArrayList();
     }
 
     /**
      * Maps are constructed with a width and height, originally having all null
      * tiles.
      *
      * You can also specify the base tile width and height.
      */
     public Map(int width, int height, int tileWidth, int tileHeight) {
         this(width, height);
         this.tileWidth = tileWidth;
         this.tileHeight = tileHeight;
         zoomWidth = tileWidth;
         zoomHeight = tileHeight;
     }
 
     /**
      * set the tile at location x, y in layer z. Layers are: 0: ground level. 1:
      * on or just above ground. 2: above ground (and everything else).
      *
      * @param x the X-location of the tile
      * @param y the Y-location of the tile.
      * @param z the layer the tile is on.
      * @param t the new Tile to set.
      */
     public void setTile(int x, int y, int z, Tile t) {
         tiles[x][y][z] = t;
     }
 
     void setZoom(float z) {
         zoomWidth = (int) (tileWidth * z);
         zoomHeight = (int) (tileHeight * z);
 
     }
 
     /**
      * used only by the game. Sets the width and height in pixels to draw when
      * rendering this map.
      */
     public void setViewSize(int width, int height) {
         this.viewWidth = width;
         this.viewHeight = height;
     }
 
     /**
      * used by the game. Renders the map to the given graphics context at the
      * given offsets.
      */
     public void render(Graphics g, int offsetX, int offsetY) {
         //System.out.println("Render Map");
 
         int minX = Math.max(offsetX / zoomWidth, 0);
         int maxX = Math.min((offsetX + viewWidth) / zoomWidth, tiles.length);
 
         int minY = Math.max(offsetY / zoomHeight, 0);
         int maxY = Math.min((offsetY + viewHeight) / zoomHeight, tiles[0].length);
 
         for (int z = 0; z < LAYERS; z++) {
             for (int x = minX; x < maxX; x++) {
                 for (int y = minY; y < maxY; y++) {
                     if (tiles[x][y][z] != null) {
                         tiles[x][y][z].render(g, x * zoomWidth - offsetX, y * zoomHeight - offsetY);
                     }
 
                 }
             }
         }
     }
 
     public void render(Graphics g, Camera c) {
         setViewSize(c.viewWidth, c.viewHeight);
         render(g, (int) (c.viewx - viewWidth / 2), (int) (c.viewy - viewHeight / 2));
     }
 
     /**
      * used by the level editor, renders a portion of the map with the given
      * origin and visible dimension.<p>
      * The origin and size are needed to increase efficiency by only rendering
      * what is on screen.
      *
      * @param origin the top-left visible corner of the map.
      * @param size the size to render.
      */
     public void render(Graphics g, Point origin, Dimension size) {
         //System.out.println("Render Map");
 
         double minX = Math.max(origin.getX() / zoomWidth, 0);
         double maxX = Math.min((origin.getX() + size.getWidth()) / zoomWidth, tiles.length);
 
         double minY = Math.max(origin.getY() / zoomHeight, 0);
         double maxY = Math.min((origin.getY() + size.getHeight()) / zoomHeight, tiles[0].length);
 
         for (int z = 0; z < LAYERS; z++) {
             for (int y = (int) minY; y < maxY; y++) {
                 for (int x = (int) minX; x < maxX; x++) {
                     if (tiles[x][y][z] != null) {
                         tiles[x][y][z].render(g, x * zoomWidth + zoomWidth, y * zoomHeight + zoomHeight);
                     }
                 }
             }
         }
     }
 
     public void render(Graphics g, Point origin, Dimension size, int layer) {
         //System.out.println("Render Map");
 
         double minX = Math.max(origin.getX() / zoomWidth, 0);
         double maxX = Math.min((origin.getX() + size.getWidth()) / zoomWidth, tiles.length);
 
         double minY = Math.max(origin.getY() / zoomHeight, 0);
         double maxY = Math.min((origin.getY() + size.getHeight()) / zoomHeight, tiles[0].length);
         int z = layer;
         for (int y = (int) minY; y < maxY; y++) {
             for (int x = (int) minX; x < maxX; x++) {
                 if (tiles[x][y][z] != null) {
                     tiles[x][y][z].render(g, x * zoomWidth + zoomWidth, y * zoomHeight + zoomHeight);
                 }
             }
         }
     }
 
     /**
      * gets the width of the map in tiles
      */
     public int getWidth() {
         return tiles.length;
     }
 
     /**
      * gets the height of the map in tiles.
      */
     public int getHeight() {
         return tiles[0].length;
     }
 
     /**
      * gets the standard width of a tile in the map.
      */
     public int getTileWidth() {
         return tileWidth;
     }
 
     /**
      * gets the standard height of a tile in the map.
      */
     public int getTileHeight() {
         return tileHeight;
     }
 
     public int getZoomWidth() {
         return zoomWidth;
     }
 
     public int getZoomHeight() {
         return zoomHeight;
     }
 
     /**
      * gets the tile at the given location.
      */
     public Tile getTile(int x, int y, int z) {
         return tiles[x][y][z];
     }
 
     /**
      * Resize the map to newWidth, newHeight. may clip the edges.
      *
      */
     void resize(int newWidth, int newHeight) {
         resize(newWidth, newHeight, LAYERS);
     }
 
     /**
      * Resize with layers
      *
      */
     void resize(int newWidth, int newHeight, int newLayers) {
         System.out.println("Call resize");
         int w, h, l;
         newWidth = Math.max(1, newWidth);
         newHeight = Math.max(1, newHeight);
         Tile[][][] newTiles = new Tile[newWidth][newHeight][newLayers];
 
 
         w = Math.min(newWidth, tiles.length);
         h = Math.min(newHeight, tiles[0].length);
         l = Math.min(newLayers, tiles[0][0].length);
 
         for (int x = 0; x < w; x++) {
             for (int y = 0; y < h; y++) {
                 System.arraycopy(tiles[x][y], 0, newTiles[x][y], 0, l);
             }
         }
         tiles = newTiles;
     }
 
     /**
      * Move the map tiles
      *
      */
     void shift(int offX, int offY) {
         System.out.println("Shift to new offset " + offX + ", " + offY + ".");
         Tile[][][] newTiles = new Tile[tiles.length][tiles[0].length][LAYERS];
 
         int xStart = Math.max(0, -offX);
         int yStart = Math.max(0, -offY);
         int xEnd = Math.min(tiles.length, tiles.length - offX);
         int yEnd = Math.min(tiles[0].length, tiles[0].length - offY);
 
         for (int x = xStart; x < xEnd; x++) {
             for (int y = yStart; y < yEnd; y++) {
                 System.arraycopy(tiles[x][y], 0, newTiles[x + offX][y + offY], 0, LAYERS);
             }
         }
         tiles = newTiles;
     }
 
     void clear() {
         for (int x = 0; x < tiles.length; x++) {
             for (int y = 0; y < tiles[0].length; y++) {
                 for (int l = 0; l < LAYERS; l++) {
                     tiles[x][y][l] = null;
                 }
             }
         }
     }
 
     /**
      * Provides a no-nonsense integer array version of this map. The numbers are
      * the tile IDs. The dimensions are: x, y, layer.
      *
      */
     public int[][][] toIntArray() {
         int set[][][] = new int[tiles.length][tiles[0].length][tiles[0][0].length];
         for (int x = 0; x < tiles.length; x++) {
             for (int y = 0; y < tiles[0].length; y++) {
                 for (int l = 0; l < LAYERS; l++) {
                     if (tiles[x][y][l] != null) {
                         set[x][y][l] = tiles[x][y][l].number;
                     } else {
                         set[x][y][l] = 0;
                     }
                 }
             }
         }
         return set;
     }
 
     /**
      * Means of setting all the tiles on a map easily. You can set a different
      * GraphicsBank as well.
      *
      */
     public void setAllTiles(int[][][] set, GraphicsBank bank) {
         gfx = bank;
         resize(tiles.length, tiles[0].length, tiles[0][0].length);
 
         /*
          if(set.length == tiles.length &&
          set[0].length == tiles[0].length &&
          set[0][0].length == tiles[0][0].length) {
          */
 
         for (int x = 0; x < tiles.length; x++) {
             for (int y = 0; y < tiles[0].length; y++) {
                 for (int l = 0; l < LAYERS; l++) {
                     tiles[x][y][l] = bank.getTile(set[x][y][l]);
                 }
             }
         }
         /*
          } else {
          System.out.println("Use resize() Before calling setAllTiles().");
          throw new RuntimeException("The int array provided does not match the map dimensions.");
          }
          */
     }
 
     /* Note: Behaviour unknown. */
     public void setTileset(GraphicsBank gfx) {
         int[][][] set = this.toIntArray();
         this.setAllTiles(set, gfx);
     }
 
     public void addChangeListener(MapChangeListener l) {
         changeListeners.add(l);
     }
 
     public void removeChangeListener(MapChangeListener l) {
         changeListeners.remove(l);
     }
 
     private void fireChangingEvent(boolean m) {
         Iterator i = changeListeners.iterator();
         ((MapChangeListener) i.next()).mapChanging(m);
     }
 
     private void fireChangedEvent(boolean m) {
         Iterator i = changeListeners.iterator();
         ((MapChangeListener) i.next()).mapChanged(m);
     }
 }
