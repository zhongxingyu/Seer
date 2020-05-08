 package ua.bolt;
 
 import ua.bolt.ex.FullMapException;
 
 import java.util.Arrays;
 
 /**
  * Created by ackiybolt on 19.02.15.
  */
 public class HashMap {
 
     private int size;
     private int length;
     private Entry[] entries;
 
     public HashMap(int length) {
        if (length <= 0) throw new IllegalArgumentException("Map length must be > 0");
         this.length = length;
         entries = new Entry[length + (int)(length * 0.25)];
     }
 
     public Long put (Integer key, Long val) throws FullMapException {
 
         if (length == size) throw new FullMapException();
         if (key == null || val == null) throw new IllegalArgumentException("Null is not acceptable.");
 
         Long oldVal = null;
 
         for (int position = positionByKey(key); oldVal == null; position++) {
             position = position >= entries.length ? 0 : position;
 
             Entry entry = entries[position];
 
             if (entry != null && key.equals(entry.key)) {
                 oldVal = entry.val;
                 entry.val = val;
                 break;
 
             } else if (entry == null) {
                 entry = entries[position] = new Entry(key, val);
                 size++;
                 break;
             }
         }
 
         return oldVal;
     }
 
     public Long get (Integer key) {
         Long result = null;
 
         for (int position = positionByKey(key);; position++) {
             position = position >= entries.length ? 0 : position;
 
             Entry entry = entries[position];
 
             if (entry == null) break;
             if (entry.key.equals(key)) {
                 result = entry.val;
                 break;
             }
         }
 
         return result;
     }
 
     private int positionByKey(Integer key) {
         return Math.abs(((key.hashCode() >> 15) ^ key)) % length;
     }
 
     public int size () {
         return size;
     }
 
     @Override
     public String toString() {
         return Arrays.toString(entries);
     }
 
     private class Entry {
 
         private Integer key;
         private Long val;
 
         Entry(Integer key, Long val) {
             this.key = key;
             this.val = val;
         }
 
         @Override
         public String toString() {
             return key + ":" + val;
         }
     }
 }
