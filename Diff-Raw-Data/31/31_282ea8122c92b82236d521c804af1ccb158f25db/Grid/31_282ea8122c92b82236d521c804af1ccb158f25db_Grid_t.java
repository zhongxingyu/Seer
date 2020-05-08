 package vooga.towerdefense.gameeditor.gamemaker.mapeditor;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import vooga.towerdefense.model.Tile;
import vooga.towerdefense.model.tiles.factories.TileFactory;
 
 
 /**
  *
  * This class holds a tile element that is drawn on the
  * MapMaker screen. It enables the creation of paths.
  * The tile is snapped to the grid when
  * the game maker clicks on the grid.
  *
  * @author Leonard K. Ng'eno
  */
 public class Grid extends Rectangle {
     private static final long serialVersionUID = 1L;
     private static final Color DEFAULT_COLOR = Color.BLACK;
    private TileFactory myTile;
 
    public Grid (int x, int y, int width, int height, TileFactory tile) {
         super.x = x;
         super.y = y;
         super.width = width;
         super.height = height;
 
         myTile = tile;
     }
 
     /**
      * set the Tile for this grid
      * @param tile the tile to be contained in this grid
      */
    public void setTile (TileFactory tile) {
         myTile = tile;
     }
 
     /**
      * This method paints the map tile contained in this grid
      * 
      * @param pen
      */
     public void paint (Graphics2D pen) {
         double thickness = 2;
         pen.setStroke(new BasicStroke((float) thickness));
         pen.setColor(DEFAULT_COLOR);
         pen.drawRect(x, y, width, height);
         if (myTile != null) {
             myTile.getPixmap().paint(pen, new Point(x + (width / 2), y + (height / 2)),
                                      new Dimension(width, height));
         }
     }
 
     /**
      * 
      * @return top left corner of the grid
      */
     public Point getTopLeftCorner () {
         return new Point(x, y);
     }
 
     /**
      * 
      * @return tile's id
      */
    public String getTileId () {
         return myTile.getTileId();
     }
 
     /**
      * 
      * @return  The tile instance of this grid
      */
    public TileFactory getTile () {
         return myTile;
     }
 
 }
