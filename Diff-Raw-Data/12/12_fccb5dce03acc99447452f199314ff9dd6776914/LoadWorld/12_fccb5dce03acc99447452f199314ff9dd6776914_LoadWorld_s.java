 package dungeon.server.commands;
 
 import dungeon.messages.Message;
 import dungeon.models.World;
 
 import java.io.Serializable;
 
 /**
  * Replaces the world loaded on the server with the given one and updates the player ID.
  */
 public class LoadWorld implements Message, Serializable {
   private final int playerId;
 
   private final World world;
 
   public LoadWorld (int playerId, World world) {
     this.playerId = playerId;
     this.world = world;
   }
 
   public int getPlayerId () {
    return playerId;
   }
 
   public World getWorld () {
    return world;
   }
 }
