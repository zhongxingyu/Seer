 package javacity.world;
 import javacity.lib.Point;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Observer;
 import java.util.Observable;
 import java.util.Random;
 
 /**
  * A city can be thought of as a map for the city to reside in.
  * A city consists of a grid of tiles, which start out as grass tiles but
  * can become zones or roads by the player actions. Game logic objects
  * run each tick of the game loop on an instance of this City class
  * which provides methods for finding tiles to calculate things
  * @author Tom
  */
 public class City implements Observer
 {
     private Random r;
     private Tile[][] grid;
     private HashMap<Tile, Point> locations;
     private HashMap<String, ArrayList<Tile>> types;
     
     /**
      * Construct our city.
      */
     public City(int xsize, int ysize)
     {    
         r = new Random();
         this.grid = new Tile[xsize][ysize];
         this.locations = new HashMap<Tile, Point>();
         this.types = new HashMap<String, ArrayList<Tile>>();
         
         for (int x = 0; x < xsize; x++) {
             for (int y = 0; y < ysize; y++) {
                 Tile t = new Tile();
                 this.grid[x][y] = t;
                 this.locations.put(t, new Point(x, y));
                 t.addObserver(this);
                 t.setType("grass");
             }
         }
     }
     
     /**
      * Get tiles from the world by their type
      * @param string type the type of the tile
      * @return ArrayList<Tile>
      */
     public ArrayList<Tile> getTilesByType(String type)
     {
         if (this.types.containsKey(type)) {
             return (ArrayList<Tile>)this.types.get(type).clone();            
         } else {
             return new ArrayList<Tile>();
         }
     }
     
     /**
      * Switch the type of a random tile of type from, to type of type to.
      * Clear as mud, right. Used for occupying / deserting zones.
      * @param string from
      * @param string to 
      */
     public void switchTypeOfRandom(String from, String to)
     {
         ArrayList<Tile> tiles = this.getTilesByType(from);
         if (tiles.size() > 0) {
             tiles.get(r.nextInt(tiles.size())).setType(to);           
         }
     }
     
     /**
      * Get the location of a tile in the game world.
      * @param Tile t The tile to get location of
      * @return Point2D the location
      */
     public Point getLocationOfTile(Tile t)
     {
         return this.locations.get(t);
     }
     
     /**
      * Get tiles by their location
      * @param location
      * @return 
      */
     public Tile getByLocation(Point location)
     {
         return this.getByLocation(location.getX(), location.getY());
     }
     
     /**
      * Get tiles by X & Y co-ordinates
      * @param int x
      * @param int y
      * @return 
      */
     public Tile getByLocation(int x, int y)
     {
         return this.grid[x][y];
     }
     
     /**
      * Test whether a location is within the city limits
      * @param int x
      * @param int y
      * @return bool
      */
     public Boolean isValidLocation(int x, int y)
     {
        return (x > 0 && x < this.grid.length && y > 0 && y < this.grid[0].length);
     }
     
     /**
      * Get the size of our city
      * @return int
      */
     public int getXSize()
     {
         return this.grid.length;
     }
     
     public int getYSize()
     {
         return this.grid[0].length;
     }
     
     /**
      * Get neighbours of a particular tile for a given radius
      * @param t The tile to get neighbours of
      * @param int radius the radius to find
      * @return ArrayList<Tile> of tiles
      */
     public ArrayList<Tile> getNeighbours(Tile t, int radius)
     {
         ArrayList<Tile> ret = new ArrayList<Tile>();
         Point loc = this.getLocationOfTile(t);
         for(int x = loc.getX()-radius; x<= loc.getX()+radius; x++) {
             for (int y = loc.getY()-radius; y <= loc.getY()+radius; y++) {
                 if (this.isValidLocation(x, y) && (y != loc.getY() || x != loc.getX())) {
                     ret.add(this.getByLocation(x, y));
                 }
             }
         }
         return ret;
     }
     
     /**
      * Get the direct neighbours of a given tile
      * @param t The tile to get neigbours of
      * @return 
      */
     public ArrayList<Tile> getNeighbours(Tile t)
     {
         return this.getNeighbours(t, 1);
     }
     
     /**
      * Register an observer to all tiles,
      * listening for changes of their state
      * @param o 
      */
     public void registerTileObserver(Observer o)
     {
         for (Tile t : this.locations.keySet()) {
             t.addObserver(o);
         }
     }
     
     /**
      * Remove an observer from all tiles.
      * @param o 
      */
     public void removeTileObserver(Observer o)
     {
         for (Tile t : this.locations.keySet()) {
             t.deleteObserver(o);
         }        
     }
     
     /**
      * Handle observing tile changes
      * so the internal arrays can be rebuilt
      * @param t 
      */
     @Override
     public void update(Observable o, Object args)
     {
         if (o instanceof Tile) {
             
             Tile t = (Tile)o;
             if (args != null) {
                 String oldType = (String)args;
                 this.types.get(oldType).remove(t);
             }
             String type = t.getType();
             if (!this.types.containsKey(type)) {
                 this.types.put(type, new ArrayList<Tile>());
             }
             this.types.get(type).add(t);            
         }
     }
 }
