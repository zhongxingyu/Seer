 /*
  *  @author Intexon
  */
 package core.Map;
 
 import com.tinyline.tiny2d.i;
 import core.Field;
 import core.Tile.Base;
 import core.Tile.Spawn;
 import core.Tile.Tile;
 import core.Unit.Unit;
 import java.awt.Point;
 import java.util.*;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.util.pathfinding.Path;
 
 /**
  *
  * @author Intexon
  */
 public class Map
 {
   private TileMap tiles;
   private UnitMap units;
   
   private int width, height;
   
   private Point spawn, base;
   
   public Map(int width, int height) {
     this.width = width;
     this.height = height;
     
     tiles = new TileMap(width, height);
     units = new UnitMap(width, height);
   }
   
   public void generateMap() throws Exception
   {
     // place spawn and base
     Random random = new Random();
     spawn = new Point(0, random.nextInt(height));
     base = new Point(width - 1, random.nextInt(height));
     
     addTile(new Spawn(spawn), spawn);
     addTile(new Base(base), base);
     
     for (int i = 0; i < width; i++) {
       for (int j = 0; j < height; j++) {
         tiles.tileAt(i, j).setBaseDistance((int) Math.sqrt(Math.pow(i - base.x, 2) + Math.pow(j - base.y, 2)));
       }
     }
   }
   
   public void update(Field field, GameContainer container, int delta)
   {
     for (int i = 0; i < width; i++) {
       for (int j = 0; j < height; j++) {
         try {
           getTileAt(i, j).update(field, container, delta);
         } catch (Exception e) {
           System.out.println("null tile");
         }
       }
     }
     
     Iterator<Unit> iterator = getAllUnits();
     while (iterator.hasNext()) {
       Unit unit = iterator.next();
       if (!unit.followsPath()) {
         unit.followPath(findPath(unit.getCoords(), base));
       }
       
       unit.update(this, delta);
       
      if (unit.getCoords() == base) {
         iterator.remove();
         removeUnit(unit);
         field.decrementHP();
       }
     }
   }
   
   private Path findPath(Point source, Point destination) {
     Path path = new Path();
     Point bestCoords = source;
     
     int best = width * height; // some larger-than-necessary best
     
     do { 
       for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
           if (dx == 0 && dy == 0) {
             continue;
           }
           
           try {
             Tile t = tiles.tileAt(bestCoords.x + dx, bestCoords.y + dy);
             if (t.getBaseDistance() < best) {
               best = t.getBaseDistance();
               bestCoords = new Point(bestCoords.x + dx, bestCoords.y + dy);
               path.appendStep(bestCoords.x, bestCoords.y);
             }
           } catch (Exception e) {
             // no neighbor tile
           }
         }
       }
     } while (bestCoords.x != destination.x || bestCoords.y != destination.y);
     
     return path;
   }
   
   /**
    * units
    */
   
   public void addUnit(Unit unit) {
     units.addUnit(unit);
   }
   
   public void removeUnit(Unit unit) {
     units.removeUnit(unit);
   }
   
   public Iterator<Unit> getAllUnits() {
     return units.getAllUnits();
   }
  
   
   /**
    * tiles
    */
   
   public Tile getTileAt(Point coords) throws Exception {
     return getTileAt(coords.x, coords.y);
   }
   
   public Tile getTileAt(int x, int y) throws Exception {
     return tiles.tileAt(x, y);
   }
   
   public void addTile(Tile tile, Point coords) throws Exception {
     tiles.addTile(tile, coords.x, coords.y);
   }
   
   public boolean isTileOverwriteable(Point coords) {
     return tiles.isTileOverwriteable(coords.x, coords.y);
   }  
   
   public HashMap<Point, Tile> getNeighborTiles(Point coords) {
     HashMap<Point, Tile> neighborTiles = new HashMap<Point, Tile>();
     
     for (int x = -1; x <= 1; x++) {
       for (int y = -1; y <= 1; y++) {
         if (x == 0 && y == 0) {
           continue;
         }
         
         try {
           neighborTiles.put(new Point(coords.x + x, coords.y + y), tiles.tileAt(x, y));
         } catch (Exception e) {
           // ignore, no neighbor tile to add
         }
       }
     }
     return neighborTiles;
   }
 }
