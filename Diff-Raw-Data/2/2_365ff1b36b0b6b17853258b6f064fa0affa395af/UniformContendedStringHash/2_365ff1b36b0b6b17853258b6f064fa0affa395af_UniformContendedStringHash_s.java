 package org.radargun.cachewrappers;
 
 import org.infinispan.distribution.ch.ConsistentHash;
 import org.infinispan.distribution.ch.DefaultConsistentHash;
 import org.infinispan.remoting.transport.Address;
 
 import java.util.*;
 
 /**
  * @author Diego Didona  - didona@gsd.inesc-id.pt
  *         Since 10/04/13
  */
 public class UniformContendedStringHash implements ConsistentHash {
 
     private Address[] caches;
     private volatile DefaultConsistentHash existing;
 
     public UniformContendedStringHash() {
         this.existing = new DefaultConsistentHash();
     }
     public UniformContendedStringHash(Set<Address> caches) {
         this.existing = new DefaultConsistentHash();
         setCaches(caches);
     }
 
     @Override
     public void setCaches(Set<Address> caches) {
         this.caches = new Address[caches.size()];
         int i = 0;
         for (Address c : caches)
             this.caches[i++] = c;
     }
 
     @Override
     public Set<Address> getCaches() {
         Set ret = new HashSet<Address>();
         Collections.addAll(ret, caches);
         return ret;
     }
 
     @Override
     public List<Address> locate(Object key, int replCount) {
         List<Address> ret = new LinkedList<Address>();
         rollAndFill(ret, replCount, (double) keyIndex(key), (double) this.caches.length);
         return ret;
     }
 
     private long keyIndex(Object key) {
         String s = (String) key;
         return Long.parseLong(s.split("_")[2]);
     }
 
 
     private void rollAndFill(List<Address> list, int replCount, double keyIndex, double numNodes) {
        int node = (int) (keyIndex / numNodes);
         for (int i = 0; i < replCount; i++) {
             list.add(this.caches[node + i]);
             if (i == numNodes - 1) //roll
                 node = 0;
         }
     }
 
     @Override
     public Map<Object, List<Address>> locateAll(Collection<Object> keys, int replCount) {
         Map<Object, List<Address>> ret = new HashMap<Object, List<Address>>();
         for (Object o : keys)
             ret.put(o, locate(o, replCount));
         return ret;
     }
 
     @Override
     public boolean isKeyLocalToAddress(Address a, Object key, int replCount) {
         return locate(key, replCount).contains(a);
     }
 
     @Override
     public List<Integer> getHashIds(Address a) {
         return new LinkedList<Integer>();
     }
 
     public List<Address> getStateProvidersOnLeave(Address leaver, int replCount) {
         return existing.getStateProvidersOnLeave(leaver, replCount);
     }
 
 
     public List<Address> getStateProvidersOnJoin(Address joiner, int replCount) {
         return existing.getStateProvidersOnJoin(joiner, replCount);
     }
 
     public List<Address> getBackupsForNode(Address node, int replCount) {
         return existing.getBackupsForNode(node, replCount);
     }
 
     @Override
     public Address primaryLocation(Object key) {
         int index = (int) (((double) keyIndex(key)) / (double) caches.length);
         return caches[index];
     }
 }
