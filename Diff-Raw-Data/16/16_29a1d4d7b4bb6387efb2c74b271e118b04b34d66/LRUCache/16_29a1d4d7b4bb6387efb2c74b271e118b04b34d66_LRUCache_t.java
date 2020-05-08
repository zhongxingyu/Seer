package edu.jhu.cs.damsl.utils;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 public class LRUCache<K,V> extends LinkedHashMap<K, V> {
   long capacity;
   
   public LRUCache(long capacity) {
     this.capacity = capacity;
   }
   
   @Override
   protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
     return size() > capacity;
   }
 
   /**
    * 
    */
   private static final long serialVersionUID = 419771866081560106L;
 
 }
