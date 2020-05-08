 package vsue.faults;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 public class VSLURMap<K, V> extends LinkedHashMap<K, V> {
   
   private static final long serialVersionUID = -7954088846931414801L;
   private final int maxSize;
   
   /**
    * Erzeugt einen LUR Cache als Map
    * @param maxSize
    */
   public VSLURMap(int maxSize) {
     super(maxSize, 1.5f, true);
     this.maxSize = maxSize;
   }
 
   /* (non-Javadoc)
    * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
    */
   protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return size() > this.maxSize;
  }
 }
