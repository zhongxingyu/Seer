 package simulator.victimcaches;
 
 import java.util.LinkedList;
 
 /**
  *
  * @author Ruben Verhack
  */
 public class PlainVictimCache {
 
     protected LinkedList<Long> victimCache;
     protected int size;
 
     public PlainVictimCache(int size) {
         this.size = size;
         this.victimCache = new LinkedList<Long>();
     }
 
     public boolean contains(long memAddress) {
 
         return victimCache.contains((Long) memAddress);
     }
 
     public void add(long memAddress) {
         victimCache.addFirst(memAddress);
        if(size > victimCache.size()) {
             victimCache.removeLast();
         }
     }
 
     public boolean switchAddresses(long oldMemAddress, long newMemAddress) {
         victimCache.addFirst(newMemAddress);
         return victimCache.remove((long) oldMemAddress);
     }
 
     /**
      * Get the value of size
      *
      * @return the value of size
      */
     public int getSize() {
         return size;
     }
 
     /**
      * Set the value of size
      *
      * @param size new value of size
      */
     public void setSize(int size) {
         this.size = size;
     }
 
     @Override
     public String toString() {
         return "PlainVictimCache";
     }
 }
