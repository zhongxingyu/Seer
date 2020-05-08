 package com.brightcove.johnny.parts;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.brightcove.johnny.coll.MapEntry;
 
 /**
  * An implementation of {@link Query} that only preserves value ordering
  * per-key, is mutable (and thus completely thread-unsafe), and accepts null
  * vals.
  */
 public class MutableMultimapQuery implements Query {
 
     private static final List<String> NO_VALS = Collections.unmodifiableList(new ArrayList<String>());
 
     /** Map of keys to null-or-nonempty lists of values. */
     private Map<String, ArrayList<String>> byKey = new HashMap<String, ArrayList<String>>();
     private int pairCount = 0;
 
     /**
      * Create empty Query.
      */
     public MutableMultimapQuery() { }
 
     /**
      * Build an instance from a list of key-value pairs.
      */
     public MutableMultimapQuery(Iterable<Map.Entry<String, String>> pairs) {
         for (Map.Entry<String, String> pair : pairs) {
             prepareForAppend(pair.getKey()).add(pair.getValue());
             pairCount++;
         }
     }
 
     /**
      * Get or create the val list at a key -- ONLY to be used when list is
      * going to grow, since this would otherwise leave the store in an
      * inconsistent state.
      */
     private ArrayList<String> prepareForAppend(String key) {
         ArrayList<String> list = byKey.get(key);
         if (list == null) {
             list = new ArrayList<String>();
             byKey.put(key, list);
         }
         return list;
     }
 
     /**
      * Find last occurrance in a list.
      * @param haystack Non-null list
      * @param needle Nullable item to find
      * @return Last index, or null if not found.
      */
     private Integer findLast(List<String> haystack, String needle) {
         for (int i = haystack.size() - 1; i >= 0; i--) {
             String cur = haystack.get(i);
             if (needle == null ? cur == null : needle.equals(cur)) {
                 return Integer.valueOf(i);
             }
         }
         return null;
     }
 
     /*== Interface implementation ==*/
 
     public boolean hasKey(String key) {
         return byKey.containsKey(key);
     }
 
     public boolean hasPair(String key, String val) {
         List<String> all = byKey.get(key); // nullable, non-empty
         return all == null ? false : findLast(all, val) != null;
     }
 
     public String getLast(String key) {
         List<String> all = byKey.get(key); // nullable, non-empty
         return all == null ? null : all.get(all.size() - 1);
     }
 
     public List<String> getAll(String key) {
         List<String> found = byKey.get(key);
         return found == null ? NO_VALS : Collections.unmodifiableList(found);
     }
 
     public List<Entry<String, String>> getPairs() {
         LinkedList<Entry<String, String>> ret = new LinkedList<Map.Entry<String,String>>();
         for (String key : byKey.keySet()) {
             for (String val : getAll(key)) {
                 ret.add(new MapEntry<String, String>(key, val));
             }
         }
         return Collections.unmodifiableList(ret);
     }
 
     public int countKeys() {
         return byKey.size();
     }
 
     public int countPairs() {
         return pairCount;
     }
 
     public Query removeAll(String key) {
         List<String> removed = byKey.remove(key);
         if (removed != null) {
             pairCount -= removed.size();
         }
         return this;
     }
 
     public Query removeAll(String key, String val) {
         List<String> victim = byKey.get(key);
         if (victim == null) {
             return this;
         }
         for(Iterator<String> iter = victim.iterator(); iter.hasNext(); ) {
             String cur = iter.next();
             if (val == null ? cur == null : val.equals(cur)) {
                 iter.remove();
                 pairCount--;
             }
         }
        if (victim.isEmpty()) {
            byKey.remove(key);
        }
         return this;
     }
 
     public Query appendAll(Iterable<Entry<String, String>> source) {
         for (Entry<String, String> e : source) {
             append(e.getKey(), e.getValue());
         }
         return this;
     }
 
     public Query append(String key, String val) {
         prepareForAppend(key).add(val);
         pairCount++;
         return this;
     }
 
     public Query replace(String key, String val) {
         return removeAll(key).append(key, val);
     }
 
     public Query replaceLast(String key, String val) {
         ArrayList<String> vals = byKey.get(key);
         if (vals == null || vals.isEmpty()) {
             append(key, val);
         } else {
             vals.set(vals.size() - 1, val);
         }
         return this;
     }
 
     public boolean implPreservesRepeatedKeys() {
         return true;
     }
 
     public boolean implPreservesValueOrderPerKey() {
         return true;
     }
 
     public boolean implPreservesPairOrder() {
         return false;
     }
 
     public boolean implImmutable() {
         return false;
     }
 }
