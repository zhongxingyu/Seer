 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2010 Per Cederberg. All rights reserved.
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
 
 /**
  * A general data array. Compared to the standard ArrayList, this
  * class provides a number of improvements;
  *
  * <ul>
  *   <li><strong>Access Methods</strong> -- Methods to provide easy
  *       access to integer, boolean and string values without casting.
  *   <li><strong>Sealing</strong> -- Simple creation of read-only
  *       objects.
  *   <li><strong>Deep Copies</strong> -- Provides a meaningful way to
  *       clone or copy data.
  *   <li><strong>Serialization</strong> -- Utility classes are
  *       available for serializing to JSON, XML, etc.
  *   <li><strong>Debug Info</strong> -- The toString() method provides
  *       a more usable default implementation.
  * </ul>
  *
  * The values in the array may have any type, but recommended types
  * are String, Integer, Boolean, Dict and Array. Circular or
  * self-referencing structures should not be used, since most data
  * serialization cannot handle them.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class Array {
 
     /**
      * A list of indexable array values.
      */
     private ArrayList list = null;
 
     /**
      * The sealed flag. When this flag is set to true, no further
      * changes are permitted to this array. Any calls to the
      * modifier methods will result in a run-time exception.
      */
     private boolean sealed = false;
 
     /**
      * Creates a new empty array.
      */
     public Array() {
         // Nothing to do here
     }
 
     /**
      * Creates a new empty array. By default an array is created
      * with a null value list, but if this constructor is used the
      * list will be initialized with the specified capacity.
      *
      * @param initialCapacity the initial array capacity
      */
     public Array(int initialCapacity) {
         if (initialCapacity > 0) {
             list = new ArrayList(initialCapacity);
         }
     }
 
     /**
      * Returns a string representation of this object.
      *
      * @return a string representation of this object
      */
     public String toString() {
         StringBuilder  buffer = new StringBuilder();
         int            len = size();
 
         buffer.append("[");
         for (int i = 0; i < 5 && i < len; i++) {
             if (i > 0) {
                 buffer.append(",");
             }
             buffer.append(" ");
             buffer.append(list.get(i));
         }
         if (len > 5) {
             buffer.append(", ...");
         }
         if (len > 0) {
             buffer.append(" ");
         }
         buffer.append("]");
         return buffer.toString();
     }
 
     /**
      * Creates a copy of this array. The copy is a "deep copy", as
      * all dictionary and array values will be recursively copied.
      *
      * @return a deep copy of this array
      */
     public Array copy() {
         Array   res;
         Object  value;
 
         res = new Array(size());
         for (int i = 0; i < size(); i++) {
             value = list.get(i);
             if (value instanceof Dict) {
                 value = ((Dict) value).copy();
             } else if (value instanceof Array) {
                 value = ((Array) value).copy();
             }
             res.list.add(value);
         }
         return res;
     }
 
     /**
      * Seals this array and prohibits any further modifications.
      * If the seal is applied recursively, any dictionary or array
      * values in this object will also be sealed. Once sealed, this
      * instance is an immutable read-only object.
      *
      * @param recursive      the recursive flag
      */
     public void seal(boolean recursive) {
         Object  value;
 
         sealed = true;
         if (recursive && list != null) {
             for (int i = 0; i < list.size(); i++) {
                 value = list.get(i);
                 if (value instanceof Dict) {
                     ((Dict) value).seal(recursive);
                 } else if (value instanceof Array) {
                     ((Array) value).seal(recursive);
                 }
             }
         }
     }
 
     /**
      * Returns the size of the array, i.e. the number of elements in
      * it.
      *
      * @return the length of the array, or
      *         zero (0) if empty
      */
     public int size() {
         return (list == null) ? 0 : list.size();
     }
 
     /**
      * Checks if the specified index is defined in this array, i.e.
      * if the index is in a valid range. Note that an index may be
      * defined but still contain a null value.
      *
      * @param index          the array index
      *
      * @return true if the array index is defined, or
      *         false otherwise
      */
     public boolean containsIndex(int index) {
         return list != null && index >= 0 && index < list.size();
     }
 
     /**
      * Checks if the specified value is contained in this array. Note
      * that equals() comparison is used, so only simple values may be
      * checked.
      *
      * @param value          the value to check for
      *
      * @return true if the value exists, or
      *         false otherwise
      */
     public boolean containsValue(Object value) {
         return indexOf(value) >= 0;
     }
 
     /**
      * Checks if all of the values in the specified array is
      * contained in this array. Note that equals() comparison is
      * used, so only simple values may be checked. If the specified
      * array is empty or null, true will be returned.
      *
      * @param arr            the array with values to check
      *
      * @return true if all values exist, or
      *         false otherwise
      */
     public boolean containsAll(Array arr) {
         int sz = (arr == null) ? 0 : arr.size();
         for (int i = 0; i < sz; i++) {
             if (!containsValue(arr.get(i))) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Checks if one or more of the values in the specified array is
      * contained in this array. Note that equals() comparison is
      * used, so only simple values may be checked. If the specified
      * array is empty or null, false will be returned.
      *
      * @param arr            the array with values to check
      *
      * @return true if at least one value exists, or
      *         false otherwise
      */
     public boolean containsAny(Array arr) {
         int sz = (arr == null) ? 0 : arr.size();
         for (int i = 0; i < sz; i++) {
             if (containsValue(arr.get(i))) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns the first array index having the specified value. Note
      * that equals() comparison is used, so only simple values may be
      * checked.
      *
      * @param value          the value to check for
      *
      * @return the array index, or
      *         -1 if the value wasn't found
      */
     public int indexOf(Object value) {
         return (list == null) ? -1 : list.indexOf(value);
     }
 
     /**
      * Returns the array value at the specified index.
      *
      * @param index          the array index
      *
      * @return the array element value, or
      *         null if the index is not defined
      */
     public Object get(int index) {
         return containsIndex(index) ? list.get(index) : null;
     }
 
     /**
      * Returns the array value at the specified index. If the index
      * is not defined or if the value is set to null, a default
      * value will be returned instead.
      *
      * @param index          the array index
      * @param defaultValue   the default value
      *
      * @return the array element value, or
      *         the default value if the index is not defined
      */
     public Object get(int index, Object defaultValue) {
         Object  value = get(index);
 
         return (value == null) ? defaultValue : value;
     }
 
     /**
      * Returns the array string value for the specified index. If
      * the index is not defined or if the value is set to null, a
      * default value will be returned instead. If the value object
      * is not a string, the toString() method will be called.
      *
      * @param index          the array index
      * @param defaultValue   the default value
      *
      * @return the array element value, or
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
      * Returns the array boolean value for the specified index. If
      * the index is not defined or if the value is set to null, a
      * default value will be returned instead. If the value object
      * is not a boolean, any object that does not equal FALSE, "",
      * "false" or 0 will be converted to true.
      *
      * @param index          the array index
      * @param defaultValue   the default value
      *
      * @return the array element value, or
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
      * Returns the array integer value for the specified index. If
      * the index is not defined or if the value is set to null, a
      * default value will be returned instead. If the value object
      * is not a number, a conversion of the toString() value of the
      * object will be attempted.
      *
      * @param index          the array index
      * @param defaultValue   the default value
      *
      * @return the array element value, or
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
      * Returns the array dictionary value for the specified index. If
      * the value is not a dictionary, an exception will be thrown.
      *
      * @param index          the array index
      *
      * @return the array element value, or
      *         null if the index is not defined
      *
      * @throws ClassCastException if the value is not a dictionary
      *             instance (or null)
      */
     public Dict getDict(int index) throws ClassCastException {
         return (Dict) get(index);
     }
 
     /**
      * Returns the array array value for the specified index. If
      * the value is not a dictionary, an exception will be thrown.
      *
      * @param index          the array index
      *
      * @return the array element value, or
      *         null if the index is not defined
      *
      * @throws ClassCastException if the value is not an array
      *             instance (or null)
      */
     public Array getArray(int index) throws ClassCastException {
         return (Array) get(index);
     }
 
     /**
      * Modifies or defines the array value for the specified index.
      * The array will automatically be padded with null values to
      * accommodate an index beyond the current valid range.
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
 
         if (sealed) {
            String msg = "cannot modify sealed data object";
             throw new UnsupportedOperationException(msg);
         }
         if (list == null) {
             list = new ArrayList(index + 1);
         }
         while (index >= list.size()) {
             list.add(null);
         }
         list.set(index, value);
     }
 
     /**
      * Modifies or defines the boolean array value for the specified
      * index. The array will automatically be padded with null values
      * to accommodate an index beyond the current valid range.
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
      * Modifies or defines the integer array value for the specified
      * index. The array will automatically be padded with null values
      * to accommodate an index beyond the current valid range.
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
      * Adds an array value to the end of the list. This method will
      * increase the array size by one.
      *
      * @param value          the array value
      *
      * @return the array index used (== the previous array size)
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public int add(Object value) throws UnsupportedOperationException {
         int index = size();
 
         if (sealed) {
            String msg = "cannot modify sealed data object";
             throw new UnsupportedOperationException(msg);
         }
         set(index, value);
         return index;
     }
 
     /**
      * Adds a boolean array value to the end of the list. This method
      * will increase the array size by one.
      *
      * @param value          the array value
      *
      * @return the array index used (== the previous array size)
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public int addBoolean(boolean value)
         throws UnsupportedOperationException {
 
         return add(Boolean.valueOf(value));
     }
 
     /**
      * Adds an integer array value to the end of the list. This
      * method will increase the array size by one.
      *
      * @param value          the array value
      *
      * @return the array index used (== the previous array size)
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      */
     public int addInt(int value) throws UnsupportedOperationException {
         return add(Integer.valueOf(value));
     }
 
     /**
      * Adds all entries from another array into this one.
      *
      * @param arr            the array to add elements from
      */
     public void addAll(Array arr) {
         if (arr != null && arr.size() > 0) {
             if (list == null) {
                 list = new ArrayList(arr.size());
             } else {
                 list.ensureCapacity(list.size() + arr.size());
             }
             for (int i = 0; i < arr.size(); i++) {
                 add(arr.get(i));
             }
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
         if (sealed) {
            String msg = "cannot modify sealed data object";
             throw new UnsupportedOperationException(msg);
         }
         if (containsIndex(index)) {
             list.remove(index);
         }
     }
 
     /**
      * Returns the relative complement of this array and another
      * array. The resulting array will contain all elements from the
      * other array that weren't found in this one. None of the two
      * arrays will be modified, but a new array will only be created
      * if some elements exist in both arrays.
      *
      * @param arr            the array to filter
      *
      * @return the complement of this array, or
      *         null if the specified array was null
      */
     public Array complement(Array arr) {
         if (size() <= 0 || !containsAny(arr)) {
             return arr;
         } else {
             Array res = new Array(arr.size());
             for (int i = 0; i < arr.size(); i++) {
                 Object value = arr.get(i);
                 if (!containsValue(value)) {
                     res.add(value);
                 }
             }
             return res;
         }
     }
 
     /**
      * Returns the intersection of this array and another array. The
      * resulting array will only contain those elements that were
      * found in both arrays. None of the two arrays will be modified,
      * but a new array will not be created if either is empty.
      *
      * @param arr            the array to intersect with
      *
      * @return the intersection of the two arrays, or
      *         null if the specified array was null
      */
     public Array intersection(Array arr) {
         if (arr == null || arr.size() <= 0) {
             return arr;
         } else if (size() <= 0) {
             return this;
         } else {
             Array res = new Array(Math.min(size(), arr.size()));
             for (int i = 0; i < arr.size(); i++) {
                 Object value = arr.get(i);
                 if (containsValue(value)) {
                     res.add(value);
                 }
             }
             return res;
         }
     }
 
     /**
      * Returns the union of this array and another array. The
      * resulting array will contain all elements from this array and
      * all elements from the other array that weren't in this one.
      * None of the two arrays will be modified, but a new array will
      * not be created if either is empty or the overlap is 100%.
      *
      * @param arr            the array to combine with
      *
      * @return the union of the two arrays
      */
     public Array union(Array arr) {
         Array comp = complement(arr);
         if (comp == null || comp.size() <= 0) {
             return this;
         } else if (size() <= 0) {
             return comp;
         } else {
             Array res = new Array(size() + comp.size());
             res.addAll(this);
             res.addAll(comp);
             return res;
         }
     }
 
     /**
      * Sorts all values in this array according to their natural
      * ordering. Note that the array MUST NOT contain dictionaries,
      * arrays or other objects that are not comparable (will result
      * in a ClassCastException). Also, all entries must be comparable
      * with each other, as the natural order of different data types
      * is undefined.
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      * @throws ClassCastException if the array values are not
      *             comparable with each other
      *
      * @see #sort(String)
      */
     public void sort()
         throws UnsupportedOperationException, ClassCastException {
 
         sort((Comparator) null);
     }
 
     /**
      * Sorts all values in this array according to the natural
      * ordering of the specified dictionary key. Note that the
      * array MUST contain dictionaries with comparable key values if
      * this method is used, or a ClassCastException will be thrown.
      *
      * @param key            the dictionary key name
      *
      * @throws UnsupportedOperationException if this object has been
      *             sealed
      * @throws ClassCastException if the array values are not
      *             dictionaries
      *
      * @see #sort()
      */
     public void sort(String key)
         throws UnsupportedOperationException, ClassCastException {
 
         sort(new DictComparator(key));
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
 
         if (sealed) {
            String msg = "cannot modify sealed data object";
             throw new UnsupportedOperationException(msg);
         }
         if (list != null) {
             if (c == null) {
                 Collections.sort(list);
             } else {
                 Collections.sort(list, c);
             }
         }
     }
 }
