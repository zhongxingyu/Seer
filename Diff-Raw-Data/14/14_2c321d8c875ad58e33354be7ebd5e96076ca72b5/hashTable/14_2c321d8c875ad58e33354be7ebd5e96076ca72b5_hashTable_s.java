 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package da.alg1000.oblig3;
 
 
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Martin
  */
 public class hashTable<T> {
 
     ArrayList<LinkedList<AbstractMap.SimpleEntry<String,T>>> buckets;
 
     public hashTable(int numberOfBuckets) {
         buckets = new ArrayList<>();
         for (int i = 0; i < numberOfBuckets; i++) {
             buckets.add(new LinkedList<AbstractMap.SimpleEntry<String,T>>());
         }
     }
 
     T get(String name) {
         return getBucketFor(name).get(getIndexFor(name)).getValue();
     }
     void set(String name, T value){
         getBucketFor(name).get(getIndexFor(name)).setValue(value);
     }
 
     LinkedList<AbstractMap.SimpleEntry<String,T>> getBucketFor(String name) {
         return buckets.get(getBucketIdFor(name));
     }
 
     int getIndexFor(String name) {
         LinkedList<AbstractMap.SimpleEntry<String,T>> bucket=getBucketFor(name);
         int i=0;
         for(Object o:bucket.toArray()){
             AbstractMap.SimpleEntry<String,T> entry=(AbstractMap.SimpleEntry<String,T>)o;
             if(entry.getKey().equals(name)) {
                 return i;
             }
             i++;
         }
         return 0;
     }
 
     String hash(String name) {
 
         String s = "";
         byte[] buf = name.getBytes();
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
 
     int getBucketIdFor(String name) {
         int i;
        String h = "";
        h = hash(name).substring(0, 7);
         i = Integer.parseInt(h.toLowerCase(), 16);
         return i % buckets.size();
     }
 
     int size() {
         int r = 0;
         for (LinkedList<AbstractMap.SimpleEntry<String,T>> bucket : buckets) {
             r += bucket.size();
         }
         return r;
     }
 
     int calculateLoadFactor() {
         return size() / buckets.size();
     }
 
     void add(String name, T value) {
         boolean add = getBucketFor(name).add(new AbstractMap.SimpleEntry<>(name,value));
     }
     void remove(String name){
        boolean remove = getBucketFor(name).remove(name);
     }
 }
