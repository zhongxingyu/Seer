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
 public class ChainedMap extends AbstractMap implements Cloneable, Serializable {
 
    /**
     * The keys of the Map.
     */
    private List _keys = new ArrayList();
 
    /**
     * The key/pair entries of the Map.
     */
    private List _entries = new ArrayList();
 
    /**
     * Creates a new instance of <code>ChainedMap</code>.
     */
    public ChainedMap() {
       // empty
    }
 
    public Set entrySet() {
       return new ChainedSet(_entries);
    }
 
    public Collection values() {
       List values = new ArrayList();
       Iterator itEntries = _entries.iterator();
       while (itEntries.hasNext()) {
          EntryMap entry = (EntryMap) itEntries.next();
          values.add(entry.getValue());
       }
       return values;
    }
 
    public Object put(Object key, Object value) {
 
       // Find the index of the current setting
       int oldKeyPos = _keys.indexOf(key);
 
       // There is no current setting
       if (oldKeyPos == -1) {
          _keys.add(key);
          _entries.add(new EntryMap(key, value));
          return null;
 
       // There is a current setting
       } else {
          Object oldValue = ((Map.Entry) _entries.get(oldKeyPos)).getValue();
          _entries.set(oldKeyPos, new EntryMap(key, value));
          return oldValue;
       }
    }
 
    public Object clone() {
       try {
          return super.clone();
       } catch (CloneNotSupportedException cnse) {
          ChainedMap clone = new ChainedMap();
          clone.putAll(this);
          return clone;
       }
    }
 
    /**
     * The <code>Map.Entry</code> for this <code>ChainedMap</code>.
     *
     * @version $Revision: 1.18 $
     * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
     */
    private static class EntryMap implements Map.Entry {
 
       /**
        * The key. Can be <code>null</code>.
        */
       private Object _key;
 
       /**
        * The value. Can be <code>null</code>.
        */
       private Object _value;
 
       /**
        * Creates a new <code>EntryMap</code> instance.
        *
        * @param key
        *    the key for the entry, can be <code>null</code>.
        *
        * @param value
        *    the value for the entry, can be <code>null</code>.
        */
       public EntryMap(Object key, Object value) {
          _key = key;
          _value = value;
       }
 
        public Object getKey() {
           return _key;
        }
 
        public Object getValue() {
           return _value;
        }
 
        public Object setValue(Object value) {
           Object oldValue = _value;
           _value = value;
           return oldValue;
        }
 
        public int hashCode() {
           return (_key == null ? 0 : _key.hashCode()) ^
                 (_value == null ? 0 : _value.hashCode());
        }
 
        public boolean equals(Object o) {
           if (!(o instanceof Map.Entry)) {
              return false;
           }
           Map.Entry e2 = (Map.Entry)o;
           return (_key.equals(e2.getKey()))  &&
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
    private static class ChainedSet extends AbstractSet {
 
       /**
        * The values of the set.
        */
       private List _values = new ArrayList();
 
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
       public ChainedSet(Collection collection) {
          Iterator itCollection = collection.iterator();
          while (itCollection.hasNext()) {
             _values.add(itCollection.next());
          }
       }
 
       public int size() {
          return _values.size();
       }
 
       public Iterator iterator() {
          return _values.iterator();
       }
    }
 }
