 package prefix;
 
 import java.util.HashMap;
 
 /**
  * Created by travis on 12/9/13.
  */
 public class PrefixHashMap<T, Y> extends HashMap {
 
    private long value;
 
     public PrefixHashMap() {
         super(26);
         value = 0;
     }
 
    public long getValue() {
         return value;
     }
 
 
     public void incValue(int incr) {
         value += incr;
     }
 
    public long sum(String prefix) {

         // base case
         if (prefix.length() == 1) {
            return ((PrefixHashMap<T, Y>) get(prefix.charAt(0))).getValue();
         }
 
         // dive in one letter deeper
        // catch an exception in case the letter does not exist
        try {
            System.out.println(prefix.substring(1));
            return ((PrefixHashMap<T, Y>) get(prefix.charAt(0))).sum(prefix.substring(1));
        } catch (NullPointerException e) {
            return 0;
        }
     }
 }
