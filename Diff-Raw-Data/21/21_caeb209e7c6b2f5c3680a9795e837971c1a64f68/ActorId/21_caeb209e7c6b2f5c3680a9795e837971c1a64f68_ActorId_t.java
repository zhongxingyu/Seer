 package actor;
 
 import java.io.Serializable;
 
 /**
  * ActorId is used to identify each actor as we transmit them over the network
  * ActorIds are never mutated once they have been created
  * @author Dustin Lundquist <dustin@null-ptr.net>
  */
 public class ActorId implements Serializable {
     private static final long serialVersionUID = -362157619097503904L;
     private static int lastId = 0;
     
     private final int playerId;
     private final int id;
 
     public ActorId(int playerId) {
         this.playerId = playerId;
         this.id = ++lastId;
     }
     
     /**
      * only used for testing
      * @param playerId
      * @param id
      */
     ActorId(int playerId, int id) {
         this.playerId = playerId;
         this.id = id;
     }
     
    @Override
    public boolean equals(Object other) {
       if(other == null || other.getClass() != getClass())
           return false;
       return equals((ActorId)other);
    }

     public boolean equals(ActorId other) {
         return this.id == other.id && this.playerId == other.playerId;
     }
     
    @Override
     public int hashCode() {   
         int hash = 1;
         hash = hash * 31 + playerId;
         hash = hash * 31 + id;
         return hash;
     }
 
    @Override
     public String toString() {
         return "#" + playerId + "." + id;
     }
 
     public int getPlayerId() {
         return playerId;
     }
 }
