 package chord;
 
 import chord.PeerInformation;
 import model.Slice;
 
 import java.io.Serializable;
 
 public class PeerMessage implements Serializable {
     private static final long serialVersionUID = -8361483433000719806L;
 
     public enum Type {
         NOTIFY, FIND_SUCCESSOR, SUCCESSOR, FIND_PREDECESSOR, PREDECESSOR, PAYLOAD
     }
 
     public Type type;
     public long nodeIdentifier;						// FIND_SUCCESSOR, PAYLOAD
     public int fingerTableIndex;					// FIND_SUCCESSOR, SUCCESSOR
     public PeerInformation peer;					// NOTIFY, FIND_SUCCESSOR, SUCCESSOR, FIND_PREDECESSOR, PREDECESSOR
     public Slice payload;					        // PAYLOAD
 
     public PeerMessage(PeerInformation origin, long nodeIdentifier, int fingerTableIndex) {
         type = Type.FIND_SUCCESSOR;
         peer = origin;
         this.nodeIdentifier = nodeIdentifier;
         this.fingerTableIndex = fingerTableIndex;
     }
 
     public PeerMessage(int fingerTableIndex, PeerInformation successor) {
         type = Type.SUCCESSOR;
         this.fingerTableIndex = fingerTableIndex;
         this.peer = successor;
     }
 
     public PeerMessage(Type type, PeerInformation peer) {
         this.type = type;
         switch (type) {
             case NOTIFY:
             case PREDECESSOR:
             case FIND_PREDECESSOR:
                 this.peer = peer;
                 break;
             default:
                 throw new IllegalArgumentException();
         }
     }
 
    public PeerMessage(long nodeIdentifier, Slice payload) {
         type = Type.PAYLOAD;
         this.nodeIdentifier = nodeIdentifier;
         this.payload = payload;
     }
 }
