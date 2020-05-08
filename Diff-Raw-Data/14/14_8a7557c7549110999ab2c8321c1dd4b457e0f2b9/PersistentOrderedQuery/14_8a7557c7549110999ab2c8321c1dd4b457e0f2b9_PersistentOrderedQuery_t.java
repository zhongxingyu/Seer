 package com.brightcove.johnny.parts;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import com.brightcove.johnny.coll.ClojureHelper;
 import com.brightcove.johnny.coll.MapEntry;
 
 import clojure.lang.IPersistentCollection;
 import clojure.lang.IPersistentMap;
 import clojure.lang.ITransientMap;
 import clojure.lang.ITransientVector;
 import clojure.lang.PersistentHashMap;
 import clojure.lang.PersistentTreeSet;
 import clojure.lang.PersistentVector;
 
 /**
  * An immutable query representation that maintains the original order
  * of key-value pairs, appends to the end, and can replace existing
  * pairs in-place. Accepts null values. Finding the last value for a key
  * takes sublinear time.
  */
 public class PersistentOrderedQuery implements Query {
 
     /**
      * A blank instance.
      */
     public static final PersistentOrderedQuery EMPTY = new PersistentOrderedQuery();
 
     /**
      * Ordered list of key-value entries (Entry), with nulls indicating
      * deleted entries.
      */
     private final PersistentVector entries;
     /**
      * Number of nil entries in {@link #entries}.
      */
     private final int deleted;
 
     /**
      * Map of string keys to non-empty {@link PersistentTreeSet}
      * of {@link #entries} indices ({@link Integer}) where values lie.
      */
     private final PersistentHashMap keylocs;
 
     /** Create a new, empty query (prefer to use {@link #EMPTY}.) */
     public PersistentOrderedQuery() {
         entries = PersistentVector.EMPTY;
         deleted = 0;
         keylocs = PersistentHashMap.EMPTY;
     }
 
     /** Mutation constructor. */
     private PersistentOrderedQuery(IPersistentCollection entries, int deleted, IPersistentMap keylocs) {
         this.entries = (PersistentVector) entries;
         this.deleted = deleted;
         this.keylocs = (PersistentHashMap) keylocs;
     }
 
     private PersistentTreeSet indicesFor(String key) {
         return (PersistentTreeSet) keylocs.get(key);
     }
 
     /*== Functional ==*/
 
     private interface F2<I,O> {
         public O f(I in);
     }
 
     /**
      * Map f over a source.
      * @param source Nullable iterable
      * @return Non-null list of results.
      */
     private static <I, O> List<O> map(Iterable<I> source, F2<I, O> f) {
         LinkedList<O> out = new LinkedList<O>();
         if (source == null) {
             return out;
         }
         for (Iterator<I> iter = source.iterator(); iter.hasNext(); ) {
             out.addLast(f.f(iter.next()));
         }
         return out;
     }
 
     /*== Accessors ==*/
 
     /** Return highest index, or null if indices is empty or null. */
     private Integer maxIndex(PersistentTreeSet indices) {
         return indices == null ? null : (Integer) indices.rseq().first();
     }
 
     /** Return val at index; null if null index, null value, or null entry. */
     @SuppressWarnings("unchecked")
     private String valAtIndex(Integer i) {
         if (i == null) {
             return null;
         }
         Entry<String, String> e = (Entry<String, String>) entries.get(i);
         if (e == null) {
             return null;
         }
         return e.getValue();
     }
 
     @SuppressWarnings("unchecked")
     private Integer findLast(String key, String val) {
         PersistentTreeSet indices = (PersistentTreeSet) keylocs.get(key);
         if (indices == null) {
             return null;
         }
         for (Integer i : (Iterable<Integer>) indices.rseq()) {
             String curv = valAtIndex(i);
             if (val == null ? curv == null : val.equals(curv)) {
                 return i;
             }
         }
         return null;
     }
 
     /*== Interface implementation ==*/
 
     public boolean hasKey(String key) {
         PersistentTreeSet indices = (PersistentTreeSet) keylocs.get(key);
        return indices != null;
     }
 
     public boolean hasPair(String key, String val) {
         return findLast(key, val) != null;
     }
 
     public String getLast(String key) {
         PersistentTreeSet indices = (PersistentTreeSet) keylocs.get(key);
         if (indices == null) {
             return null;
         } else { // if present, not empty
             Integer index = maxIndex(indices);
             return valAtIndex(index);
         }
     }
 
     @SuppressWarnings("unchecked")
     public Collection<String> getAll(String key) {
         return map((Set<Integer>) indicesFor(key), new F2<Integer, String>() {
             public String f(Integer in) {
                 return valAtIndex(in);
             }
         });
     }
 
     @SuppressWarnings("unchecked")
     public Collection<Entry<String, String>> getPairs() {
         LinkedList<Entry<String, String>> ret = new LinkedList<Entry<String,String>>();
         for (Entry<String, String> e : (Collection<Entry<String, String>>) entries) {
             if (e != null) {
                 ret.addLast(e);
             }
         }
         return ret;
     }
 
     public int countKeys() {
         return keylocs.size();
     }
 
     public int countPairs() {
         return entries.size() - deleted;
     }
 
     @SuppressWarnings("unchecked")
     public Query removeAll(String key) {
         PersistentTreeSet indices = indicesFor(key);
         if (indices == null) {
             return this;
         }
         ITransientVector newEntries = entries.asTransient();
         for (Integer i : (Set<Integer>) indices) {
             newEntries = newEntries.assocN(i, null);
         }
         return new PersistentOrderedQuery(
                 newEntries.persistent(),
                 deleted + indices.size(),
                 keylocs.without(key));
     }
 
     @SuppressWarnings("unchecked")
     public Query removeAll(String key, String val) {
         PersistentTreeSet indices = indicesFor(key);
         if (indices == null) {
             return this;
         }
         int removed = 0;
         ITransientVector newEntries = entries.asTransient();
         for (Integer i : (Set<Integer>) indices) {
             Entry<String, String> e = (Entry<String, String>) entries.get(i);
             // trust that entry is not null, since it was referred to by index
             String curv = e.getValue();
             if (val == null ? curv == null : val.equals(curv)) {
                 newEntries = newEntries.assocN(i, null);
                 removed++;
                 indices = (PersistentTreeSet) indices.disjoin(i);
             }
         }
         return new PersistentOrderedQuery(
                 newEntries.persistent(),
                 deleted + removed,
                indices.isEmpty() ? keylocs.without(key)
                                  : keylocs.assoc(key, indices));
     }
 
     public Query append(String key, String val) {
         PersistentTreeSet indices = (PersistentTreeSet) keylocs.get(key);
         if (indices == null) {
             indices = PersistentTreeSet.EMPTY;
         }
         return new PersistentOrderedQuery(
                 entries.cons(new MapEntry<String, String>(key, val)),
                 deleted,
                 keylocs.assoc(key, indices.cons(entries.size())));
     }
 
     public Query appendAll(Iterable<Entry<String, String>> source) {
         ITransientMap keylocsT = keylocs.asTransient();
         int nextIndex = entries.size();
         for (Iterator<Entry<String, String>> iter = source.iterator(); iter.hasNext(); ) {
             Entry<String, String> e = iter.next();
             PersistentTreeSet indices = (PersistentTreeSet) keylocsT.valAt(e.getKey());
             if (indices == null) {
                 indices = PersistentTreeSet.EMPTY;
             }
             keylocsT = keylocsT.assoc(e.getKey(), indices.cons(nextIndex));
             nextIndex++;
         }
         return new PersistentOrderedQuery(
                 ClojureHelper.into(entries, source.iterator()),
                 deleted,
                 keylocsT.persistent());
     }
 
     public Query replace(String key, String val) {
         return removeAll(key).append(key, val);
     }
 
     public Query replaceLast(String key, String val) {
         PersistentTreeSet indices = (PersistentTreeSet) keylocs.get(key);
         if (indices == null) {
             return append(key, val);
         } else {
             Integer lastMatchingIndex = maxIndex(indices);
             return new PersistentOrderedQuery(
                     entries.assocN(lastMatchingIndex,
                                    new MapEntry<String, String>(key, val)),
                     deleted,
                     keylocs);
         }
     }
 
     public boolean implPreservesRepeatedKeys() {
         return true;
     }
 
     public boolean implPreservesValueOrderPerKey() {
         return true;
     }
 
     public boolean implPreservesPairOrder() {
         return true;
     }
 
     public boolean implImmutable() {
         return true;
     }
 }
