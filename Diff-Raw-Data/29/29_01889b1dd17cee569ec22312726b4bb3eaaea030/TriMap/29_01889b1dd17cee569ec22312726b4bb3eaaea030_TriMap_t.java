 package com.entrocorp.linearlogic.oneinthegun.util;
 
import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 
 public class TriMap<K, X extends Comparable<X>, Y extends Comparable<Y>> {
 
     protected HashMap<K, HLComparablePair<X, Y>> map = new HashMap<K, HLComparablePair<X, Y>>();
 
     public Set<K> keySet() {
         return map.keySet();
     }
 
     public X getX(K key) {
         return map.get(key).getX();
     }
 
    public List<X> getXValues() {
        List<X> output = new ArrayList<X>();
         for (HLComparablePair<X, Y> p : map.values())
            output.add(p.getX());
         return output;
     }
 
     public boolean setX(K key, X x) {
         if (!map.containsKey(key))
             return false;
         map.get(key).setX(x);
         return true;
     }
 
     public Y getY(K key) {
         return map.get(key).getY();
     }
 
    public List<Y> getYValues() {
        List<Y> output = new ArrayList<Y>();
         for (HLComparablePair<X, Y> p : map.values())
            output.add(p.getY());
         return output;
     }
 
     public boolean setY(K key, Y y) {
         if (!map.containsKey(key))
             return false;
         map.get(key).setY(y);
         return true;
     }
 
     public HLComparablePair<X, Y> getPair(K key) {
         return map.get(key);
     }
 
    public List<HLComparablePair<X, Y>> getPairs() {
        return new ArrayList<HLComparablePair<X, Y>>(map.values());
     }
 
     public Set<Entry<K, HLComparablePair<X, Y>>> entrySet() {
         return map.entrySet();
     }
 
     public Set<Pair<K, HLComparablePair<X, Y>>> sortedEntrySet() {
         TreeMap<HLComparablePair<X, Y>, K> sorted = new TreeMap<HLComparablePair<X, Y>, K>();
         for (Entry<K, HLComparablePair<X, Y>> entry : map.entrySet())
             sorted.put(entry.getValue(), entry.getKey());
         Set<Pair<K, HLComparablePair<X, Y>>> output = new LinkedHashSet<Pair<K, HLComparablePair<X, Y>>>();
         for (Entry<HLComparablePair<X, Y>, K> entry : sorted.entrySet())
             output.add(new Pair<K, HLComparablePair<X, Y>>(entry.getValue(), entry.getKey()));
         return output;
     }
 
     public boolean containsKey(K key) {
         return map.containsKey(key);
     }
 
     public HLComparablePair<X, Y> put(K key, X x, Y y) {
         return map.put(key, new HLComparablePair<X, Y>(x, y));
     }
 
     public HLComparablePair<X, Y> remove(K key) {
         return map.remove(key);
     }
 
     public void clear() {
         map.clear();
     }
 
     public int size() {
         return map.size();
     }
 }
