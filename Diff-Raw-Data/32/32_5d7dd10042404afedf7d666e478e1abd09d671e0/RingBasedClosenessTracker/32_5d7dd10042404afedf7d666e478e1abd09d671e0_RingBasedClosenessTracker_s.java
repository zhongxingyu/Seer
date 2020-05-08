 package org.aldeon.dht.closeness;
 
 import org.aldeon.model.Identifier;
 import org.aldeon.networking.common.AddressType;
 import org.aldeon.networking.common.PeerAddress;
 
 import java.util.Set;
 
 public class RingBasedClosenessTracker implements ClosenessTracker {
 
     private final Ring ring;
 
     public RingBasedClosenessTracker(Ring ring) {
         this.ring = ring;
     }
 
     @Override
     public void addAddress(PeerAddress address) {
         ring.insert(address);
     }
 
     @Override
     public void delAddress(PeerAddress address) {
         ring.remove(address);
     }
 
     @Override
     public Set<PeerAddress> getNearest(AddressType addressType, Identifier identifier, int maxResults) {
        return ring.getNearest(identifier, maxResults);
     }
 }
