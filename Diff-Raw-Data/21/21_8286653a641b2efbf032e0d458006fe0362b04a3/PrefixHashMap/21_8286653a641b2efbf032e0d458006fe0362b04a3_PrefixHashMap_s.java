 package prefix;
 
 import java.util.HashMap;
 
 /**
  * Created by travis on 12/9/13.
  */
 public class PrefixHashMap<T, Y> extends HashMap {
 
    private int value;
 
     public PrefixHashMap() {
         super(26);
         value = 0;
     }
 
    public int getValue() {
         return value;
     }
 
 
     public void incValue(int incr) {
         value += incr;
     }
 
    public int sum(String prefix) {
         // base case
         if (prefix.length() == 1) {
            return value;
         }
 
         // dive in one letter deeper
        return ((PrefixHashMap<T, Y>) get(prefix.charAt(0))).sum(prefix.substring(1));
     }
 }
