 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2009 Per Cederberg & Dynabyte AB.
  * All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.core.data;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 
 /**
  * A generic, anonymous, and untyped data object. A data object is
  * more or less a hash table with name and value properties. The
  * data object can also contain an indexable array of values, or
  * a combination of both. The property names are non-empty strings,
  * and values may have any Java data object type, such as strings,
  * numbers, dates, etc. Values may also be set to data objects, but
  * circular references or custom-defined classes should be avoided
  * in order to keep compliance with the standard data serializers.
  *
  * @author   Per Cederberg, Dynabyte AB
  * @version  1.0
  */
 public class Data implements Cloneable {
 
     /**
      * A hash map containing property names and values.
      */
     private LinkedHashMap props = null;
 
     /**
      * An optional list of indexable array values.
      */
     private ArrayList list = null;
 
     /**
      * The sealed flag. When this flag is set to true, no further
      * changes are permitted to this data object. Any calls to the
      * modifier methods will result in a run-time exception.
      */
     private boolean sealed = false;
 
     /**
      * Creates a new empty data object.
      */
     public Data() {
         // Nothing to do here
     }
 
     /**
      * Creates a new empty data object for list values. By default a
      * data object is created with a null array value list, but if
      * this constructor is used the list will be initialized with
      * the specified capacity and by default represents an empty
      * list instead of an empty object.
      *
      * @param initialCapacity the initial array capacity
      */
     public Data(int initialCapacity) {
         if (initialCapacity >= 0) {
             list = new ArrayList(initialCapacity);
         }
     }
 
     /**
      * Returns a string representation of this object.
      *
      * @return a string representation of this object
      */
     public String toString() {
         StringBuffer  buffer = new StringBuffer();
         String[]      keys;
 
         if (this.props != null) {
             keys = this.keys();
             buffer.append("{ ");
             for (int i = 0; i < 3 && i < keys.length; i++) {
                 if (i > 0) {
                     buffer.append(", ");
                 }
                 buffer.append(keys[i]);
                 buffer.append(": ");
                 buffer.append(this.props.get(keys[i]));
             }
            if (this.list.size() > 3) {
                 buffer.append(", ...");
             }
             buffer.append(" }");
         } else if (this.list != null) {
             buffer.append("[");
             for (int i = 0; i < 5 && i < this.list.size(); i++) {
                 if (i > 0) {
                     buffer.append(",");
                 }
                 buffer.append(this.list.get(i));
             }
             if (this.list.size() > 5) {
                 buffer.append(",...");
             }
             buffer.append("]");
         } else {
             buffer.append("<empty data object>");
         }
         return buffer.toString();
     }
 
     /**
      * Creates a copy of this object. The copy is a "deep copy", as
      * all properties and array entries containing data objects will
      * be recursively cloned.
      *
      * @return a deep copy of this object
      */
     public Object clone() {
         Data      res;
         Iterator  iter;
         Object    name;
         Object    value;
 
         if (this.list != null) {
             res = new Data(this.list.size());
         } else {
             res = new Data();
         }
         if (this.props != null) {
             res.props = new LinkedHashMap(this.props.size());
             iter = this.props.keySet().iterator();
             while (iter.hasNext()) {
                 name = iter.next();
                 value = this.props.get(name);
                 if (value instanceof Data) {
                     value = ((Data) value).clone();
                 }
                 res.props.put(name, value);
             }
         }
         if (this.list != null) {
             res.list = new ArrayList(this.list.size());
             for (int i = 0; i < this.list.size(); i++) {
                 value = this.list.get(i);
                 if (value instanceof Data) {
                     value = ((Data) value).clone();
                 }
                 res.list.add(value);
             }
         }
         return res;
     }
 
     /**
      * Seals this object and prohibits any further modifications.
      * If the seal is applied recursively, any data objects
      * referenced by this object will also be sealed. Once sealed,
      * this instance is an immutable read-only data object.
      *
      * @param recursive      the recursive flag
      */
     public void seal(boolean recursive) {
         Iterator  iter;
         Object    name;
         Object    value;
 
         this.sealed = true;
         if (recursive && this.props != null) {
             iter = this.props.keySet().iterator();
             while (iter.hasNext()) {
                 name = iter.next();
                 value = this.props.get(name);
                 if (value instanceof Data) {
                     ((Data) value).seal(recursive);
                 }
             }
         }
         if (recursive && this.list != null) {
             for (int i = 0; i < this.list.size(); i++) {
                 value = this.list.get(i);
                 if (value instanceof Data) {
                     ((Data) value).seal(recursive);
                 }
             }
         }
     }
 
     /**
      * Returns the size of the properties map.
      *
      * @return the size of the properties map, or
      *         -1 if not used
      */
     public int mapSize() {
         if (this.props == null) {
             return -1;
         } else {
             return this.props.size();
         }
     }
 
     /**
      * Returns the size of the data array.
      *
      * @return the length of the data array, or
      *         -1 if not used
      */
     public int arraySize() {
         if (this.list == null) {
             return -1;
         } else {
             return this.list.size();
         }
     }
 
     /**
      * Checks if the specified property key is defined in this
      * object. Note that a key name may be defined but still have a
      * null value.
      *
      * @param key            the property key name
      *
      * @return true if the property key is defined, or
      *         false otherwise
      */
     public boolean containsKey(String key) {
         return this.props != null && this.props.containsKey(key);
     }
 
     /**
      * Checks if the specified array index is defined in this object.
      * Note that an index may be defined but still have a null value.
      *
      * @param index          the array index
      *
      * @return true if the array index is defined, or
      *         false otherwise
      */
     public boolean containsIndex(int index) {
         return this.list != null &&
                index >= 0 && index < this.list.size();
     }
 
     /**
      * Checks if the specified value is contained in this object.
      * Note that both the property and array values will be compared
      * to the specified value.
      *
      * @param value          the value to check for
      *
      * @return true if the value exists, or
      *         false otherwise
      */
     public boolean containsValue(Object value) {
         return keyOf(value) != null || indexOf(value) >= 0;
     }
 
     /**
      * Returns the first property key having the specified value.
      *
      * @param value          the value to check for
      *
      * @return the property key name, or
      *         null if the value wasn't found
      */
     public String keyOf(Object value) {
         Iterator  iter;
         String    name;
         Object    obj;
 
         if (this.props != null) {
             iter = this.props.keySet().iterator();
             while (iter.hasNext()) {
                 name = (String) iter.next();
                 obj = this.props.get(name);
                 if (obj == null && value == null) {
                     return name;
                 } else if (obj != null && obj.equals(value)) {
                     return name;
                 }
             }
         }
         return null;
     }
 
     /**
      * Returns the array index of the specified value.
      *
      * @param value          the value to check for
      *
      * @return the array index, or
      *         -1 if the value wasn't found
      */
     public int indexOf(Object value) {
         return (this.list == null) ? -1 : this.list.indexOf(value);
     }
 
     /**
      * Returns an array with all the defined property key names. The
      * keys are ordered as originally added to this object.
      *
      * @return an array with all property key names
      */
     public String[] keys() {
         String[]  res;
         Iterator  iter;
         int       pos = 0;
 
         if (this.props == null) {
             res = new String[0];
         } else {
             res = new String[this.props.size()];
             iter = this.props.keySet().iterator();
             while (iter.hasNext()) {
                 res[pos++] = (String) iter.next();
             }
         }
         return res;
     }
 
     /**
      * Returns the property value for the specified key.
      *
      * @param key            the property key name
      *
      * @return the property value, or
      *         null if the key is not defined
      */
     public Object get(String key) {
         if (this.props == null) {
             return null;
         } else {
             return this.props.get(key);
         }
     }
 
     /**
      * Returns the array value at the specified index.
      *
      * @param index          the array index
      *
      * @return the array value, or
      *         null if the index is not defined
      */
     public Object get(int index) {
         if (this.list == null || index >= this.list.size()) {
             return null;
         } else {
             return list.get(index);
         }
     }
 
     /**
      * Returns the property value for the specified key. If the key
      * is not defined or if the value is set to null, a default
      * value will be returned instead.
      *
      * @param key            the property key name
      * @param defaultValue   the default value
      *
      * @return the property value, or
      *         the default value if the key is not defined
      */
     public Object get(String key, Object defaultValue) {
         Object  value = get(key);
 
         return (value == null) ? defaultValue : value;
     }
 
     /**
      * Returns the array value at the specified index. If the index
      * is not defined or if the value is set to null, a default
      * value will be returned instead.
      *
      * @param index          the array index
      * @param defaultValue   the default value
      *
      * @return the array value, or
      *         the default value if the index is not defined
      */
     public Object get(int index, Object defaultValue) {
         Object  value = get(index);
 
         return (value == null) ? defaultValue : value;
     }
 
     /**
      * Returns the string property value for the specified key. If
      * the key is not defined or if the value is set to null, a
      * default value will be returned instead. If the value object
      * is not a string, the toString() method will be called.
      *
      * @param key            the property key name
      * @param defaultValue   the default value
      *
      * @return the property string value, or
      *         the default value if the key is not defined
      */
     public String getString(String key, String defaultValue) {
         Object  value = get(key);
 
         if (value == null) {
             return defaultValue;
         } else if (value instanceof String) {
             return (String) value;
         } else {
             return value.toString();
         }
     }
 
     /**
      * Returns the string array value for the specified index. If
      * the index is not defined or if the value is set to null, a
      * default value will be returned instead. If the value object
      * is not a string, the toString() method will be called.
      *
      * @param index          the array index
      * @param defaultValue   the default value
      *
      * @return the array string value, or
      *         the default value if the index is not defined
      */
     public String getString(int index, String defaultValue) {
         Object  value = get(index);
 
         if (value == null) {
             return defaultValue;
         } else if (value instanceof String) {
             return (String) value;
         } else {
             return value.toString();
         }
     }
 
     /**
      * Returns the boolean property value for the specified key. If
      * the key is not defined or if the value is set to null, a
      * default value will be returned instead. If the value object
      * is not a boolean, any object that does not equal FALSE, "",
      * "false" or 0 will be converted to true.
      *
      * @param key            the property key name
      * @param defaultValue   the default value
      *
      * @return the property boolean value, or
      *         the default value if the key is not defined
      */
     public boolean getBoolean(String key, boolean defaultValue) {
         Object  value = get(key);
 
         if (value == null) {
             return defaultValue;
         } else if (value instanceof Boolean) {
             return ((Boolean) value).booleanValue();
         } else {
             return !value.equals(Boolean.FALSE) &&
                    !value.equals("") &&
                    !value.equals("false") &&
                    !value.equals(Integer.valueOf(0));
         }
     }
 
     /**
      * Returns the boolean array value for the specified index. If
      * the index is not defined or if the value is set to null, a
      * default value will be returned instead. If the value object
      * is not a boolean, any object that does not equal FALSE, "",
      * "false" or 0 will be converted to true.
      *
      * @param index          the array index
      * @param defaultValue   the default value
      *
      * @return the array boolean value, or
      *         the default value if the index is not defined
      */
     public boolean getBoolean(int index, boolean defaultValue) {
         Object  value = get(index);
 
         if (value == null) {
             return defaultValue;
         } else if (value instanceof Boolean) {
             return ((Boolean) value).booleanValue();
         } else {
             return !value.equals(Boolean.FALSE) &&
                    !value.equals("") &&
                    !value.equals("false") &&
                    !value.equals(Integer.valueOf(0));
         }
     }
 
     /**
      * Returns the integer property value for the specified key. If
      * the key is not defined or if the value is set to null, a
      * default value will be returned instead. If the value object
      * is not a number, a conversion of the toString() value of the
      * object will be attempted.
      *
      * @param key            the property key name
      * @param defaultValue   the default value
      *
      * @return the property integer value, or
      *         the default value if the key is not defined
      *
      * @throws NumberFormatException if the value didn't contain a
      *             valid integer
      */
     public int getInt(String key, int defaultValue)
         throws NumberFormatException {
 
         Object  value = get(key);
 
         if (value == null) {
             return defaultValue;
         } else if (value instanceof Number) {
             return ((Number) value).intValue();
         } else {
             return Integer.parseInt(value.toString());
         }
     }
 
     /**
      * Returns the integer array value for the specified index. If
      * the index is not defined or if the value is set to null, a
      * default value will be returned instead. If the value object
      * is not a number, a conversion of the toString() value of the
      * object will be attempted.
      *
      * @param index          the array index
      * @param defaultValue   the default value
      *
      * @return the array integer value, or
      *         the default value if the index is not defined
      *
      * @throws NumberFormatException if the value didn't contain a
      *             valid integer
      */
     public int getInt(int index, int defaultValue)
         throws NumberFormatException {
 
         Object  value = get(index);
 
         if (value == null) {
             return defaultValue;
         } else if (value instanceof Number) {
             return ((Number) value).intValue();
         } else {
             return Integer.parseInt(value.toString());
         }
     }
 
     /**
      * Returns the data object property value for the specified key.
      * If the value is not a data object, an exception will be
      * thrown.
      *
      * @param key            the property key name
      *
      * @return the property data object value, or
      *         null if the key is not defined
      *
      * @throws ClassCastException if the value is not a data object
      *             instance (or null)
      */
     public Data getData(String key) throws ClassCastException {
         return (Data) get(key);
     }
 
     /**
      * Returns the data object array value for the specified index.
      * If the value is not a data object, an exception will be
      * thrown.
      *
      * @param index          the array index
      *
      * @return the array data object value, or
      *         null if the index is not defined
      *
      * @throws ClassCastException if the value is not a data object
      *             instance (or null)
      */
     public Data getData(int index) throws ClassCastException {
         return (Data) get(index);
     }
 
     /**
      * Modifies or defines the property value for the specified key.
      *
      * @param key            the property key name
      * @param value          the property value
      *
      * @throws NullPointerException if the key is null or an empty
      *             string
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public void set(String key, Object value)
         throws NullPointerException, UnsupportedOperationException {
 
         String  msg;
 
         if (this.sealed) {
             msg = "cannot modify sealed data object";
             throw new UnsupportedOperationException(msg);
         }
         if (key == null || key.length() == 0) {
             msg = "property key cannot be null or empty";
             throw new NullPointerException(msg);
         }
         if (this.props == null) {
             this.props = new LinkedHashMap();
         }
         this.props.put(key, value);
     }
 
     /**
      * Modifies or defines the array value for the specified index.
      * The array will automatically be padded with null values to
      * accommodate positive indexes.
      *
      * @param index          the array index
      * @param value          the array value
      *
      * @throws IndexOutOfBoundsException if index is negative
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public void set(int index, Object value)
         throws IndexOutOfBoundsException, UnsupportedOperationException {
 
         if (this.sealed) {
             String msg = "cannot modify sealed data object";
             throw new UnsupportedOperationException(msg);
         }
         if (this.list == null) {
             this.list = new ArrayList(index + 1);
         }
         while (index >= this.list.size()) {
             this.list.add(null);
         }
         this.list.set(index, value);
     }
 
     /**
      * Modifies or defines the boolean property value for the
      * specified key.
      *
      * @param key            the property key name
      * @param value          the property value
      *
      * @throws NullPointerException if the key is null or an empty
      *             string
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public void setBoolean(String key, boolean value)
         throws NullPointerException, UnsupportedOperationException {
 
         set(key, Boolean.valueOf(value));
     }
 
     /**
      * Modifies or defines the boolean array value for the specified
      * index.
      *
      * @param index          the array index
      * @param value          the array value
      *
      * @throws IndexOutOfBoundsException if index is negative
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public void setBoolean(int index, boolean value)
         throws IndexOutOfBoundsException, UnsupportedOperationException {
 
         set(index, Boolean.valueOf(value));
     }
 
     /**
      * Modifies or defines the integer property value for the
      * specified key.
      *
      * @param key            the property key name
      * @param value          the property value
      *
      * @throws NullPointerException if the key is null or an empty
      *             string
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public void setInt(String key, int value)
         throws NullPointerException, UnsupportedOperationException {
 
         set(key, Integer.valueOf(value));
     }
 
     /**
      * Modifies or defines the integer array value for the specified
      * index.
      *
      * @param index          the array index
      * @param value          the array value
      *
      * @throws IndexOutOfBoundsException if index is negative
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public void setInt(int index, int value)
         throws IndexOutOfBoundsException, UnsupportedOperationException {
 
         set(index, Integer.valueOf(value));
     }
 
     /**
      * Adds a property value using the specified key if possible. If the key is
      * already in use, a new unique key will be generated instead. This will
      * ensure that an existing value will not be overwritten.
      *
      * @param key            the suggested property key name
      * @param value          the property value
      *
      * @return the property key name used
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public String add(String key, Object value)
         throws UnsupportedOperationException {
 
         String  keyName = key;
         int     attempt = 0;
 
         while (containsKey(keyName)) {
             attempt++;
             keyName = key + "_" + attempt;
         }
         set(keyName, value);
         return keyName;
     }
 
     /**
      * Adds an array value at the first available index. This method
      * uses the current array size to determine which index to use.
      *
      * @param value          the array value
      *
      * @return the array index used
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public int add(Object value) throws UnsupportedOperationException {
         int index = arraySize();
 
         if (this.sealed) {
             String msg = "cannot modify sealed data object";
             throw new UnsupportedOperationException(msg);
         }
         if (index < 0) {
             index = 0;
         }
         set(index, value);
         return index;
     }
 
     /**
      * Adds a boolean property value using the specified key if possible. If
      * the key is already in use, a new unique key will be generated instead.
      * This will ensure that an existing value will not be overwritten.
      *
      * @param key            the suggested property key name
      * @param value          the property value
      *
      * @return the property key name used
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public String addBoolean(String key, boolean value)
         throws UnsupportedOperationException {
 
         return add(key, Boolean.valueOf(value));
     }
 
     /**
      * Adds a boolean array value at the first available index. This
      * method uses the current array size to determine which index
      * to use.
      *
      * @param value          the array value
      *
      * @return the array index used
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public int addBoolean(boolean value)
         throws UnsupportedOperationException {
 
         return add(Boolean.valueOf(value));
     }
 
     /**
      * Adds an integer property value using the specified key if possible. If
      * the key is already in use, a new unique key will be generated instead.
      * This will ensure that an existing value will not be overwritten.
      *
      * @param key            the suggested property key name
      * @param value          the property value
      *
      * @return the property key name used
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public String addInt(String key, int value)
         throws UnsupportedOperationException {
 
         return add(key, Integer.valueOf(value));
     }
 
     /**
      * Adds an integer array value at the first available index. This
      * method uses the current array size to determine which index
      * to use.
      *
      * @param value          the array value
      *
      * @return the array index used
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public int addInt(int value) throws UnsupportedOperationException {
         return add(Integer.valueOf(value));
     }
 
     /**
      * Deletes the specified property key and its value.
      *
      * @param key            the property key name
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public void remove(String key) throws UnsupportedOperationException {
         if (this.sealed) {
             String msg = "cannot modify sealed data object";
             throw new UnsupportedOperationException(msg);
         }
         if (this.props != null) {
             this.props.remove(key);
         }
     }
 
     /**
      * Deletes the specified array index and its value. All
      * subsequent array values will be shifted forward by one step.
      *
      * @param index          the array index
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public void remove(int index) throws UnsupportedOperationException {
         if (this.sealed) {
             String msg = "cannot modify sealed data object";
             throw new UnsupportedOperationException(msg);
         }
         if (this.list != null && index < this.list.size()) {
             this.list.remove(index);
         }
     }
 
     /**
      * Sorts all values in this array according to their natural
      * ordering. Note that the array MUST NOT contain data objects
      * if this method is used, since they are not comparable (will
      * result in a ClassCastException).
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      * @throws ClassCastException if the array values are not
      *             comparable (for example, strings and integers)
      *
      * @see #sort(String)
      */
     public void sort()
         throws UnsupportedOperationException, ClassCastException {
 
         sort((Comparator) null);
     }
 
     /**
      * Sorts all values in this array according to the natural
      * ordering of the specified data object key. Note that the
      * array MUST contain data objects with comparable key values if
      * this method is used, or a ClassCastException will be thrown.
      *
      * @param key            the property key name
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      * @throws ClassCastException if the array values are not
      *             data objects
      *
      * @see #sort()
      */
     public void sort(String key)
         throws UnsupportedOperationException, ClassCastException {
 
         sort(new DataComparator(key));
     }
 
     /**
      * Sorts all values in this array according to the comparator
      * specified.
      *
      * @param c              the object comparator to use
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      * @throws ClassCastException if the array values were not
      *             comparable
      */
     public void sort(Comparator c)
         throws UnsupportedOperationException, ClassCastException {
 
         if (this.sealed) {
             String msg = "cannot modify sealed data object";
             throw new UnsupportedOperationException(msg);
         }
         if (this.list != null) {
             if (c == null) {
                 Collections.sort(this.list);
             } else {
                 Collections.sort(this.list, c);
             }
         }
     }
 }
