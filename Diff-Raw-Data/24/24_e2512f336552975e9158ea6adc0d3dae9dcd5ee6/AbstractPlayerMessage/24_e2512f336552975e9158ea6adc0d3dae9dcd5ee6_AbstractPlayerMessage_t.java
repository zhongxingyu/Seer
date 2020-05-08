 package dungeon.ui.messages;
 
 public class AbstractPlayerMessage implements PlayerMessage {
   private final int playerId;
 
   public AbstractPlayerMessage (int playerId) {
     this.playerId = playerId;
   }
 
   public int getPlayerId () {
    return this.playerId;
   }
 }
