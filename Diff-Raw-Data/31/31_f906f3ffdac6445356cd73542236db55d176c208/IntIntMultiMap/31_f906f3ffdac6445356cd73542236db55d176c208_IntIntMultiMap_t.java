 package com.jillesvangurp.efficientstring;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import com.google.common.collect.Sets;
 
 /**
  * Multi map for integers. May be used for storing efficient string indices. This class is thread safe.
  */
 public class IntIntMultiMap implements Iterable<Entry<Integer, Integer>> {
     private static final int BUCKETS = 50000;
     private final Bucket[] buckets = new Bucket[BUCKETS];
 
     public void put(int key, int value) {
         if (value < 0) {
             throw new IllegalArgumentException("put can only take positive values");
         }
         Bucket bucket = getOrCreateBucket(key);
         synchronized (bucket) {
             bucket.put(key, value);
         }
     }
 
     public int[] get(int key) {
         Bucket bucket = buckets[getBucketIndex(key)];
         if (bucket != null) {
             return bucket.get(key);
         }
         return null;
 
     }
 
     private Bucket getOrCreateBucket(int key) {
         int index = getBucketIndex(key);
         Bucket bucket = buckets[index];
         if (bucket == null) {
             synchronized (buckets) {
                 bucket = buckets[index];
                 if (bucket == null) {
                     bucket = new Bucket();
                     buckets[index] = bucket;
                 }
             }
         }
         return bucket;
     }
 
     private int getBucketIndex(int key) {
         return key % BUCKETS;
     }
 
     public Iterable<Entry<Integer, Set<Integer>>> values() {
         return new Iterable<Entry<Integer, Set<Integer>>>() {
 
             @Override
             public Iterator<Entry<Integer, Set<Integer>>> iterator() {
                 return new Iterator<Entry<Integer, Set<Integer>>>() {
                     Entry<Integer, Set<Integer>> next = null;
                     Iterator<Entry<Integer, Set<Integer>>> currentBucket = null;
                     int bucket = 0;
 
                     @Override
                     public boolean hasNext() {
                         if (next != null) {
                             return true;
                         } else {
                             if (currentBucket == null) {
                                 while (currentBucket == null && bucket < buckets.length) {
                                     Bucket nb = buckets[bucket++];
                                     if (nb != null) {
                                         currentBucket = nb.keyIterator();
                                     }
                                 }
                             }
                             if (currentBucket != null) {
                                 while (!currentBucket.hasNext() && bucket < buckets.length) {
                                     Bucket nb = buckets[bucket++];
                                     if (nb != null) {
                                         currentBucket = nb.keyIterator();
                                     }
                                 }
                                 if (currentBucket.hasNext()) {
                                     next = currentBucket.next();
                                     return true;
                                 }
                             }
                             return false;
                         }
                     }
 
                     @Override
                     public Entry<Integer, Set<Integer>> next() {
                         if (hasNext()) {
                             Entry<Integer, Set<Integer>> result = next;
                             next = null;
                             return result;
                         } else {
                             throw new NoSuchElementException();
                         }
                     }
 
                     @Override
                     public void remove() {
                         throw new UnsupportedOperationException("remove is not supported");
                     }
                 };
             }
         };
     }
 
     @Override
     public Iterator<Entry<Integer, Integer>> iterator() {
 
         return new Iterator<Entry<Integer, Integer>>() {
             int bucket = 0;
             Iterator<Entry<Integer, Integer>> bi = null;
             Entry<Integer, Integer> next = null;
 
             @Override
             public boolean hasNext() {
                 if (next != null) {
                     return true;
                 } else {
                     if (bi == null) {
                         while (bi == null && bucket < buckets.length) {
                             Bucket nb = buckets[bucket++];
                             if (nb != null) {
                                 bi = nb.iterator();
                             }
                         }
                     }
                     if (bi != null) {
                         while (!bi.hasNext() && bucket < buckets.length) {
                             Bucket nb = buckets[bucket++];
                             if (nb != null) {
                                 bi = nb.iterator();
                             }
                         }
                         if (bi.hasNext()) {
                             next = bi.next();
                             return true;
                         }
                     }
                 }
                 return false;
             }
 
             @Override
             public Entry<Integer, Integer> next() {
                 if (hasNext()) {
                     Entry<Integer, Integer> result = next;
                     next = null;
                     return result;
                 } else {
                     throw new NoSuchElementException();
                 }
             }
 
             @Override
             public void remove() {
                 throw new UnsupportedOperationException("remove is not supported");
             }
         };
     }
 
     class Bucket implements Iterable<Entry<Integer, Integer>> {
         private int[][] values = new int[5][0];
 
         public void put(int key, int value) {
             int keyIndex = -1;
             for (int i = 0; i < values.length; i++) {
                 int[] list = values[i];
                if (list==null||list.length == 0) {
                     // empty slot, use it
                     list = createList(key);
                     values[i] = list;
                     keyIndex = i;
                     break;
                 } else {
                     if (list[0] == key) {
                         keyIndex = i;
                         break;
                     }
                 }
             }
             if (keyIndex == -1) {
                 // we need more space
                 int oldLength = values.length;
                 values = Arrays.copyOf(values, (int) (oldLength * 1.3));
                 values[oldLength] = createList(key);
                 keyIndex = oldLength;
             }
             int[] keyList = values[keyIndex];
             for (int i = 1; i < keyList.length; i++) {
                 if (keyList[i] == -1) {
                     keyList[i] = value;
                     // we're done
                     return;
                 } else if (keyList[i] == value) {
                     // already contained
                     return;
                 }
             }
             // the keyList needs to be expanded
             int oldKeyListLength = keyList.length;
             keyList = Arrays.copyOf(keyList, (int) (oldKeyListLength * 1.3));
             for (int i = oldKeyListLength + 1; i < keyList.length; i++) {
                 keyList[i] = -1;
             }
             values[keyIndex] = keyList;
             keyList[oldKeyListLength] = value;
         }
 
         public Iterator<Entry<Integer, Set<Integer>>> keyIterator() {
             return new Iterator<Entry<Integer, Set<Integer>>>() {
 
                 int index = 0;
                 int[] current = null;
                 MapEntry next = null;
 
                 @Override
                 public boolean hasNext() {
                     if (next != null) {
                         return true;
                     } else {
                         while (current == null && index < values.length) {                            
                             current = values[index++];
                         }
                         if (current.length != 0) {
                             int empty = 0;
                             for (int j = current.length - 1; j >= 0; j--) {
                                 if (current[j] == -1) {
                                     empty++;
                                 } else {
                                     break;
                                 }
                             }
 
                             next = new MapEntry(current[0], Arrays.copyOfRange(current, 1, current.length - empty));
                             current=values[index++];
                             
                             return true;
                         }
                     }
                     return false;
                 }
 
                 @Override
                 public MapEntry next() {
                     if (hasNext()) {
                         MapEntry result = next;
                         next = null;
                         return result;
                     } else {
                         throw new NoSuchElementException();
                     }
                 }
 
                 @Override
                 public void remove() {
                     throw new UnsupportedOperationException("remove is not supported");
                 }
 
             };
         }
 
         private int[] createList(int key) {
             int[] list;
             list = new int[5];
             Arrays.fill(list, -1);
             list[0] = key;
             return list;
         }
 
         public int[] get(int key) {
             for (int i = 0; i < values.length; i++) {
                 int[] keyList = values[i];
                 if (keyList.length > 0 && keyList[0] == key) {
                     int empty = 0;
                     for (int j = keyList.length - 1; j >= 0; j--) {
                         if (keyList[j] == -1) {
                             empty++;
                         } else {
                             break;
                         }
                     }
 
                     return Arrays.copyOfRange(keyList, 1, keyList.length - empty);
                 }
             }
             return null;
         }
 
         @Override
         public Iterator<Entry<Integer, Integer>> iterator() {
             return new Iterator<Entry<Integer, Integer>>() {
                 Entry<Integer, Integer> next = null;
                 int row = 0;
                 int column = 1;
 
                 @Override
                 public boolean hasNext() {
                     if (next != null) {
                         return true;
                     }
                     if (row < values.length && column <= values[row].length) {
                         if (values[row].length > 0) {
                             if (values[row][column] != -1) {
                                 next = new IntIntEntry(values[row][0], values[row][column]);
                                 column++;
                                 if (column == values[row].length) {
                                     column = 1;
                                     row++;
                                 }
                                 return true;
                             } else {
                                 column = 1;
                                 row++;
                                 return hasNext();
                             }
                         } else {
                             column = 1;
                             row++;
                             return hasNext();
                         }
                     } else {
                         return false;
                     }
                 }
 
                 @Override
                 public Entry<Integer, Integer> next() {
                     if (hasNext()) {
                         Entry<Integer, Integer> result = next;
                         next = null;
                         return result;
                     } else {
                         throw new NoSuchElementException();
                     }
                 }
 
                 @Override
                 public void remove() {
                     throw new UnsupportedOperationException("remove is not supported");
                 }
             };
         }
     }
 
     private class MapEntry implements Entry<Integer, Set<Integer>> {
         private final int key;
         private final Set<Integer> value;
 
         public MapEntry(int key, int[] values) {
             this.key = key;
 
             this.value = Sets.newHashSet();
             for (int i : values) {
                 value.add(i);
             }
         }
 
         @Override
         public Integer getKey() {
             return key;
         }
 
         @Override
         public Set<Integer> getValue() {
             return value;
         }
 
         @Override
         public Set<Integer> setValue(Set<Integer> value) {
             throw new UnsupportedOperationException();
         }
 
     }
 
     private class IntIntEntry implements Entry<Integer, Integer> {
         private final int key;
         private final int value;
 
         public IntIntEntry(int key, int value) {
             this.key = key;
             this.value = value;
         }
 
         @Override
         public Integer getKey() {
             return key;
         }
 
         @Override
         public Integer getValue() {
             return value;
         }
 
         @Override
         public Integer setValue(Integer value) {
             throw new UnsupportedOperationException();
         }
     }
 }
