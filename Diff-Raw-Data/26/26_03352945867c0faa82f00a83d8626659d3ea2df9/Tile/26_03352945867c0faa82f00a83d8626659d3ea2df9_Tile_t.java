 package dungeon.models;
 
 import dungeon.models.messages.Transform;
 
 public class Tile {
   /**
    * true, wenn die Platte nicht begehbar ist; false, wenn die Platte begehbar ist
    */
   private final boolean blocking;
 
   private final Position position;
 
   public Tile (boolean blocking, Position position) {
     this.blocking = blocking;
     this.position = position;
   }
 
   public boolean isBlocking () {
     return this.blocking;
   }
 
   public Position getPosition () {
    return this.position;
   }
 
   public Tile apply (Transform transform) {
     return this;
   }
 }
