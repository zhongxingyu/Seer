 package ogo.spec.game.model;
 
 import java.util.HashSet;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.Set;
 
 public class CreaturePath
 {
 
     /**
      * The current path of the creature.
      *
      * This is a queue of the next tiles to visit.
      */
     ConcurrentLinkedQueue<Tile> path = new ConcurrentLinkedQueue<Tile>();
 
     /**
      * Game map.
      */
     GameMap map;
 
     /**
      * Current and previous position.
      */
     Tile current, previous;
 
     /**
      * Allowed types.
      */
     Set<TileType> allowedTypes;
 
     /**
      * Constructor.
      */
     public CreaturePath(GameMap map, Tile initial, Set<TileType> allowedTypes)
     {
         this.map = map;
         previous = initial;
         current = initial;
         this.allowedTypes = allowedTypes;
     }
 
     /**
      * Get the current tile.
      */
     synchronized public Tile getCurrentTile()
     {
         return current;
     }
 
     /**
      * Get the previous tile.
      */
     synchronized public Tile getPreviousTile()
     {
         return previous;
     }
 
     /**
      * Get the next tile.
      */
     synchronized public Tile getNextTile()
     {
         return path.peek();
     }
 
     /**
      * Go to the next tile.
      */
     synchronized public Tile step()
     {
         previous = current;
         current = path.poll();
         return current;
     }
 
     /**
      * Calculate a path to the given tile.
      *
      * This method uses the A* algorithm.
      */
     public void calculatePath(Tile tile)
     {
         path = new ConcurrentLinkedQueue<Tile>(map.calculatePath(current, tile, allowedTypes));
     }
 }
