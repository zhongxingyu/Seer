 package dungeon.models;
 
 import dungeon.models.messages.Transform;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 public class Room {
   private final String id;
 
   private final List<Enemy> enemies;
 
   private final List<Tile> tiles;
 
   public Room (String id, List<Enemy> enemies, List<Tile> tiles) {
     this.id = id;
     this.enemies = Collections.unmodifiableList(new ArrayList<>(enemies));
     this.tiles = Collections.unmodifiableList(new ArrayList<>(tiles));
   }
 
   public String getId () {
     return this.id;
   }
 
   public List<Enemy> getEnemies () {
     return this.enemies;
   }
 
   public List<Tile> getTiles () {
    return tiles;
   }
 
   /**
    * Returns the size along the X-axis whereas the size is the longest span occupied by tiles.
    */
   public float getXSize () {
     if (this.tiles.isEmpty()) {
       return 0;
     } else {
       Tile tile = Collections.max(this.tiles, new Comparator<Tile>() {
         @Override
        public int compare (Tile tile, Tile tile2) {
          return (int)Math.ceil(tile.getPosition().getX() - tile2.getPosition().getX());
         }
       });
 
       return (int)tile.getPosition().getX() + 1;
     }
   }
 
   /**
    * Returns the size along the Y-axis whereas the size is the longest span occupied by tiles.
    */
   public float getYSize () {
     if (this.tiles.isEmpty()) {
       return 0;
     } else {
       Tile tile = Collections.max(this.tiles, new Comparator<Tile>() {
         @Override
        public int compare (Tile tile, Tile tile2) {
          return (int)Math.ceil(tile.getPosition().getY() - tile2.getPosition().getY());
         }
       });
 
       return (int)tile.getPosition().getY() + 1;
     }
   }
 
   public Room apply (Transform transform) {
     return this;
   }
 }
