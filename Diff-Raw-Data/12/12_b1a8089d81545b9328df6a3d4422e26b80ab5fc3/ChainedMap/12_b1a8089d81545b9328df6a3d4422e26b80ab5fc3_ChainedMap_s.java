 /*
  * $Id: ChainedMap.java,v 1.18 2007/09/18 08:45:08 agoubard Exp $
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.collections;
 
 import java.io.Serializable;
 import java.util.AbstractMap;
 import java.util.AbstractSet;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * This class provides a Map that stores the key/value pairs in the order
  * that they were added to the Map.
  * If an entry already exists, the key/pair entry will be put at the same
  * position as the old one.
  *
  * @version $Revision: 1.18 $ $Date: 2007/09/18 08:45:08 $
  * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
  *
  * @since XINS 1.3.0
  *
  * @deprecated
  *    Since XINS 3.0, use the {@link java.util.LinkedHashMap} class, available
  *    since J2SE v1.4.
  */
 @Deprecated
 @SuppressWarnings(value = "unchecked")
 public class ChainedMap<K,V> extends AbstractMap<K,V> implements Cloneable, Serializable {
 
    /**
     * The keys of the Map.
     */
    private final List<K> _keys = new ArrayList<K>();
 
    /**
     * The key/pair entries of the Map.
     */
   private final List<MapEntry<K,V>> _entries = new ArrayList<MapEntry<K,V>>();
 
    /**
     * Creates a new instance of <code>ChainedMap</code>.
     */
    public ChainedMap() {
       // empty
    }
 
    @Override
    public Set<Map.Entry<K,V>> entrySet() {
       return new ChainedSet<Map.Entry<K,V>>(_entries);
    }
 
    @Override
    public Collection<V> values() {
       List<V> values = new ArrayList<V>();
      for (MapEntry entry : _entries) {
          values.add(entry.getValue());
       }
       return values;
    }
 
    @Override
    public V put(K key, V value) {
 
       // Find the index of the current setting
       int oldKeyPos = _keys.indexOf(key);
 
       // There is no current setting
       if (oldKeyPos == -1) {
          _keys.add(key);
          _entries.add(new MapEntry<K,V>(key, value));
          return null;
 
       // There is a current setting
       } else {
          V oldValue = _entries.get(oldKeyPos).getValue();
          _entries.set(oldKeyPos, new MapEntry(key, value));
          return oldValue;
       }
    }
 
    @Override
    public Object clone() throws CloneNotSupportedException {
       return super.clone();
    }
 
    /**
     * The <code>Map.Entry</code> for this <code>ChainedMap</code>.
     *
     * @version $Revision: 1.18 $
     * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
     */
    private static class MapEntry<K,V> implements Map.Entry<K,V> {
 
       /**
        * The key. Can be <code>null</code>.
        */
       private final K _key;
 
       /**
        * The value. Can be <code>null</code>.
        */
       private V _value;
 
       /**
        * Creates a new <code>EntryMap</code> instance.
        *
        * @param key
        *    the key for the entry, can be <code>null</code>.
        *
        * @param value
        *    the value for the entry, can be <code>null</code>.
        */
       public MapEntry(K key, V value) {
          _key = key;
          _value = value;
       }
 
        public K getKey() {
           return _key;
        }
 
        public V getValue() {
           return _value;
        }
 
        public V setValue(V value) {
           V oldValue = _value;
           _value = value;
           return oldValue;
        }
 
       @Override
       public int hashCode() {
          return (_key == null ? 0 : _key.hashCode()) ^ (_value == null ? 0 : _value.hashCode());
       }
 
       @Override
       public boolean equals(Object o) {
          if (!(o instanceof Map.Entry)) {
             return false;
          }
          Map.Entry e2 = (Map.Entry)o;
          return (_key.equals(e2.getKey())) &&
                 (_value == null ? e2.getValue() == null : _value.equals(e2.getValue()));
       }
    }
 
    /**
     * The <code>ChainedSet</code> used for the <code>entrySet</code> method of
     * this <code>ChainedMap</code>.
     *
     * @version $Revision: 1.18 $ $Date: 2007/09/18 08:45:08 $
     * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
     */
    private static class ChainedSet<T> extends AbstractSet<T> {
 
       /**
        * The values of the set.
        */
       private final List<T> _values = new ArrayList<T>();
 
       /**
        * Creates a new instance of <code>ChainedSet</code>.
        */
       public ChainedSet() {
          // empty
       }
 
       /**
        * Creates a new instance of <code>ChainedSet</code>.
        *
        * @param collection
        *    the collection that contains the values of the set, cannot be
        *    <code>null</code>.
        */
       public ChainedSet(Collection<T> collection) {
          for (T item : collection) {
             _values.add(item);
          }
       }
 
       @Override
       public int size() {
          return _values.size();
       }
 
       @Override
       public Iterator<T> iterator() {
          return _values.iterator();
       }
    }
 }
