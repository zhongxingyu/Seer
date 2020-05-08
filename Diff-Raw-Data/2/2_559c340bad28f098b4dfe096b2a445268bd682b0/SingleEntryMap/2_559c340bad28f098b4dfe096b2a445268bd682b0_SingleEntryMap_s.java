 /*
  * $Id$
  */
 package org.xins.util.collections;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * Implementation of a <code>Map</code> that only contains 1 entry.
  * 
  * This <code>Map</code> accept a <code>null</code> key and/or a 
  * <code>null</code> value.
  * To key and value of the entry are passed when you create the new 
  * <code>SingleEntryMap</code>, then you can change the value of the 
 * entry  by using the {@link #put(int, int) put} method.
  *
  * @version $Revision$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class SingleEntryMap implements Map {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>SingleEntryMap</code>.
     * 
     * @param key
     *    the key for the single entry, can be
     *    <code>null</code>.
     * @param value
     *    the value for the single entry, can be
     *    <code>null</code>.
     */
    public SingleEntryMap(Object key, Object value) {
       _key = key;
       _value = value;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The key of the single entry in the Map.
     */
    private Object _key;
 
    /**
     * The key of the single entry in the Map.
     */
    private Object _value;
    
    
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public int size() {
       return 1;
    }
    
    public boolean isEmpty() {
       return false;
    }
    
    public boolean containsKey(Object key) {
       if (key == null) {
          return (_key == null);
       }
       return (_key != null && _key.equals(key));
    }
    
    public boolean containsValue(Object value) {
       if (value == null) {
          return (_value == null);
       }
       return  (_value != null && _value.equals(value));
    }
    
    public Object get(Object key) {
       if (key == null) {
          if (_key == null) {
             return _value; 
          } else {
             return null;  
          }
       } else if (key.equals(_key)) {
          return _value; 
       } else {
          return null; 
       }
    }
    
    public Object put(Object key, Object value) {
       Object oldKey = _key;
       Object oldValue = _value;
       _key = key;
       _value = value;
       if (oldKey == null) {
          if (key == null) {
             return oldValue; 
          } else {
             return null;  
          }
       } else if (oldKey.equals(key)) {
          return oldValue;
       } else {
          return null; 
       }
    }
     
    public Object remove(Object key) {
       throw new UnsupportedOperationException();
    }
     
    public void putAll(Map t) {
       throw new UnsupportedOperationException();
    }
    
    public void clear() {
       throw new UnsupportedOperationException();
    }
    
    public Set keySet() {
     
       // XXX This could be optimized by creating a SingleEntrySet
       Set keys = new TreeSet();
       keys.add(_key);
       return keys;
    }
    
    public Collection values() {
     
       // XXX This could be optimized by creating a SingleEntryList
       Collection values = new ArrayList(1);
       values.add(_value);
       return values;
    }
    
    public Set entrySet() {
       Set entries = new TreeSet();
       entries.add(new SingleEntry());
       return entries;
    }
    
    public int hashCode() {
       return (_key == null ? 0 : _key.hashCode()) ^ 
              (_value == null ? 0 : _value.hashCode());   
    }
    
    public boolean equals(Object o) {
       if (!(o instanceof Map)) {
          return false; 
       }
       Map e2 = (SingleEntryMap)o;
       return entrySet().equals(e2.entrySet());
    }
    
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * The <code>Map.Entry</code> for this <code>SingleEntryMap</code>.
     * 
     * @version $Revision$
     * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
     */ 
    private class SingleEntry implements Map.Entry {
    
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
           return SingleEntryMap.this.hashCode();   
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
 }
