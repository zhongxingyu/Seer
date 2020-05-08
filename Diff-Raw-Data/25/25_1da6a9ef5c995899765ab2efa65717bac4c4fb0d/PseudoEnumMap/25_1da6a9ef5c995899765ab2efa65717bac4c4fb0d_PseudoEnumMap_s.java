 package com.bezpredel.collections;
 
 import java.util.*;
 
 public class PseudoEnumMap<K extends PseudoEnum, V> extends AbstractMap<K, V> {
     private transient final MCArrayList<MapEntry<K, V>> underlying;
     private final Class<K> keyClass;
     private transient int size = 0;
     private transient Set<Entry<K, V>> entrySet;
 
     public PseudoEnumMap(Class<K> clazz) {
         keyClass = clazz;
         underlying = new MCArrayList<MapEntry<K, V>>(
                 Math.max(16, PseudoEnum.getCurrentCapacity(keyClass))
         );
     }
 
     public V get(Object keyObj) {
         MapEntry<K, V> entry = getEntry(keyObj);
         return entry == null ? null : entry.getValue();
     }
 
     private MapEntry<K, V> getEntry(Object keyObj) {
         if(!softCheckForLegalKey(keyObj)) return null;
         PseudoEnum key = (PseudoEnum) keyObj;
         if (underlying.size() <= key.getOrdinal()) {
             return null;
         } else {
             MapEntry<K, V> entry = underlying.get(key.getOrdinal());
             assert (entry.getKey() == keyObj);
             return entry;
         }
     }
 
     public int size() {
         return size;
     }
 
     public boolean containsKey(Object key) {
         return getEntry(key) != null;
     }
 
     public boolean containsValue(Object value) {
         for (int i = 0, c = underlying.size(); i < c; i++) {
             MapEntry<K, V> entry = underlying.get(i);
             if (entry != null) {
                 if (entry.getValue() == value || (value != null && value.equals(entry.getValue()))) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public V put(K key, V value) {
         checkForLegalKey(key);
 
         if(underlying.size() <= key.getOrdinal()) {
            underlying.ensureCapacity(key.getOrdinal() + 1);

            while(underlying.size() <=  key.getOrdinal()) {
                underlying.add(null);
            }

             underlying.set(key.getOrdinal(), createMapEntry(key, value));
             size++;
             return null;
         } else {
             MapEntry<K, V> entry = underlying.get(key.getOrdinal());
             if(entry==null) {
                 underlying.set(key.getOrdinal(), createMapEntry(key, value));
                 size++;
                 return null;
             } else {
                 underlying.incModCount();
                 return entry.setValue(value);
             }
         }
     }
 
     protected MapEntry<K, V> createMapEntry(K key, V value) {
         return new MapEntry<K, V>(key, value);
     }
 
     public V remove(Object keyObj) {
         checkForLegalKey(keyObj);
         PseudoEnum key = (PseudoEnum) keyObj;
 
         if (underlying.size() > key.getOrdinal()) {
            MapEntry<K, V> entry = underlying.remove(key.getOrdinal());
             if (entry != null) {
                 assert entry.getKey() == keyObj;
                 size--;
                 return entry.getValue();
             }
         } else {
             underlying.incModCount();
         }
         return null;
     }
 
     public void clear() {
         size = 0;
         underlying.clear();
     }
 
     public void putAll(Map<? extends K, ? extends V> m) {
         for(Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
             put(e.getKey(), e.getValue());
         }
     }
 
     @Override
     public Set<Entry<K, V>> entrySet() {
         if(entrySet == null) {
             entrySet = new EntrySet();
         }
         return entrySet;
     }
 
     private void checkForLegalKey(Object key) {
         if (key == null) throw new NullPointerException("Null keys unsupported");
         if (key.getClass() != keyClass) {
             throw new IllegalArgumentException("Keys of type " + key.getClass() + " are not supported");
         }
     }
 
 
     private boolean softCheckForLegalKey(Object key) {
         if (key == null) throw new NullPointerException("Null keys unsupported");
         return (key.getClass() == keyClass);
     }
 
 
 
     protected static class MapEntry<K extends PseudoEnum, V> implements Map.Entry<K, V> {
         private final K key;
         private V value;
 
         protected MapEntry(K key, V value) {
             this.key = key;
             this.value = value;
         }
 
         public K getKey() {
             return key;
         }
 
         public V getValue() {
             return value;
         }
 
         public V setValue(V value) {
             V retVal = this.value;
             this.value = value;
             return retVal;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             MapEntry mapEntry = (MapEntry) o;
 
             if (!key.equals(mapEntry.key)) return false;
             if (value != null ? !value.equals(mapEntry.value) : mapEntry.value != null) return false;
 
             return true;
         }
 
         @Override
         public int hashCode() {
             int result = key.hashCode();
             result = 31 * result + (value != null ? value.hashCode() : 0);
             return result;
         }
     }
 
     private class EntrySet extends AbstractSet<Entry<K, V>> {
         @Override
         public Iterator<Entry<K, V>> iterator() {
             return new NullSkippingIterator<Entry<K, V>>((Iterator) underlying.iterator());
         }
 
         @Override
         public int size() {
             return size;
         }
 
         @Override
         public boolean remove(Object o) {
             for (int i = 0; i < underlying.size(); i++) {
                 if (underlying.get(i) == o) {
                     underlying.set(i, null);
                     size--;
                     return true;
                 }
             }
             return false;
         }
     }
 
 
 
     private static class MCArrayList<T> extends ArrayList<T> {
         private static final long serialVersionUID = -8163687367554737653L;
 
         public void incModCount() {
             modCount++;
         }
 
         private MCArrayList(int initialCapacity) {
             super(initialCapacity);
         }

        private MCArrayList() {
        }

        private MCArrayList(Collection<? extends T> c) {
            super(c);
        }
     }
 
     public static MapFactory factory() {
         return factory;
     }
 
     private static final MapFactory factory = new MapFactory() {
         public <K, V> Map<K, V> createMap(Class<K> keyClass) {
             if(PseudoEnum.class.isAssignableFrom(keyClass)) {
                 return new PseudoEnumMap(keyClass);
             } else {
                 throw new IllegalArgumentException("Only subclasses of PseudoEnum are supported");
             }
         }
     };
 }
