 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package da.alg1000.oblig3;
 
 import java.util.AbstractMap;
 import java.util.ArrayList;
 
 
 /**
  *
  * @author Martin
  */
public class hashTable<TKey, TValue>  {
 
     ArrayList</*java.util.*/LinkedList<AbstractMap.SimpleEntry<TKey, TValue>>> buckets;
 
     public hashTable(int numberOfBuckets) {
         buckets = new ArrayList<>();
         for (int i = 0; i < numberOfBuckets; i++) {
             buckets.add(new LinkedList<AbstractMap.SimpleEntry<TKey, TValue>>());
         }
     }
 
     TValue get(TKey Key) {
         if(this.size()!=0){
         return getBucketFor(Key).get(getIndexFor(Key)).getValue();
         }
         else return null;
     }
 
     void set(TKey Key, TValue value) {
         getBucketFor(Key).get(getIndexFor(Key)).setValue(value);
     }
 
     /*java.util.*/LinkedList<AbstractMap.SimpleEntry<TKey, TValue>> getBucketFor(TKey key) {
         return buckets.get(getBucketIdFor(key));
     }
 
     int getIndexFor(TKey Key) {
         /*java.util.*/LinkedList<AbstractMap.SimpleEntry<TKey, TValue>> bucket = getBucketFor(Key);
         int i = 0;
         for (Object o : bucket.toArray()) {
             AbstractMap.SimpleEntry<TKey, TValue> entry = (AbstractMap.SimpleEntry<TKey, TValue>) o;
             if (entry.getKey().equals(Key)) {
                 return i;
             }
             i++;
         }
         return 0;
     }
 
     String hash(TKey Key) {
 
         String s = "";
         byte[] buf = Key.toString().getBytes();
         for (byte b : buf) {
             s += toHex(b);
         }
         return s;
     }
 
     private static String toHex(byte b) {
         String s = "";
         int n = (b > 0) ? b : 256 + b;
         s += halfToHex(n >> 4);
         s += halfToHex(n & 15);
         return s;
     }
 
     private static char halfToHex(int i) {
         if (i < 10) {
             return (char) ('0' + i);
         } else {
             return (char) ('A' + (i - 10));
         }
     }
 
     int getBucketIdFor(TKey key) {
         int i;
         String h = hash(key).substring(0, key.toString().length() < 4 ? key.toString().length() : 7);
         i = Integer.parseInt(h.toLowerCase(), 16);
         return i % buckets.size();
     }
 
     int size() {
         int r = 0;
         for (/*java.util.*/LinkedList<AbstractMap.SimpleEntry<TKey, TValue>> bucket : buckets) {
             r += bucket.size();
         }
         return r;
     }
 
    double calculateLoadFactor() {
        return (double)size() / (double)buckets.size();
     }
 
     void add(TKey Key, TValue value) {
         getBucketFor(Key).add(new AbstractMap.SimpleEntry<>(Key, value));
     }
 
     void remove(TKey Key) {
         int i = 0;
         /*java.util.*/LinkedList<AbstractMap.SimpleEntry<TKey, TValue>> bucket = getBucketFor(Key);
         for (AbstractMap.SimpleEntry<TKey, TValue> se : bucket) {
             if (se.getKey().equals(Key)) {
                 bucket.remove(i);
                 break;
             }
             i++;
         }
     }
    Object[] toArray(){
        ArrayList<AbstractMap.SimpleEntry<TKey,TValue>> list=new ArrayList<>();
        
        for(LinkedList<AbstractMap.SimpleEntry<TKey,TValue>> bucket:buckets){
            for (Object o : bucket.toArray()){
                list.add((AbstractMap.SimpleEntry<TKey,TValue>)o);
            }
        }
        
        return list.toArray();
    }
 }
