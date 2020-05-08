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
        System.out.println("CLSADD: " + address);
         ring.insert(address);
     }
 
     @Override
     public void delAddress(PeerAddress address) {
        System.out.println("CLSDEL: " + address);
         ring.remove(address);
     }
 
     @Override
     public Set<PeerAddress> getNearest(AddressType addressType, Identifier identifier, int maxResults) {
        Set<PeerAddress> result = ring.getNearest(identifier, maxResults);
        System.out.println("CLSSET: " + result.size());
        return result;
     }
 }
