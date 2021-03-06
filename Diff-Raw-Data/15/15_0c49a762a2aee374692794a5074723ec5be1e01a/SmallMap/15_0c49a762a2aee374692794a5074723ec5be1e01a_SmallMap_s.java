 /*******************************************************************************
  * Copyright (c) 2002 - 2006 IBM Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package com.ibm.wala.util.collections;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.ibm.wala.util.debug.Assertions;
 import com.ibm.wala.util.debug.UnimplementedError;
 
 /**
  * 
  * A simple implementation of Map; intended for Maps with few elements.
  * Optimized for space, not time -- use with care.
  * 
  * @author sfink
  */
 public class SmallMap<K,V> implements Map<K,V> {
 
   private static final boolean DEBUG_USAGE = false;
 
   private static final int DEBUG_MAX_SIZE = 20;
 
   // this Map contains keysAndValues.length / 2 entries.
   // in the following array, entries 0 ... keysAndValues.length/2 - 1 are keys.
   // entries keysAndValues.length/2 .. keysAndValues.length are values.
   private Object[] keysAndValues;
 
   /*
    */
   public int size() {
     if (keysAndValues == null) {
       return 0;
     } else {
       return keysAndValues.length / 2;
     }
   }
 
   /**
    * Use with care.
    * 
    * @return the ith key
    */
   @SuppressWarnings("unchecked")
   public K getKey(int i) throws IllegalStateException {
     if (keysAndValues == null) {
       throw new IllegalStateException("getKey on empty map");
     }
     return (K) keysAndValues[i];
   }
 
   /**
    * Use with care.
    * 
    * @param i
    * @return the ith key
    */
   public Object getValue(int i) throws IllegalStateException{
     if (keysAndValues == null) {
       throw new IllegalStateException("getValue on empty map");
     }
     return keysAndValues[size() + i];
   }
 
   /*
    * @see java.util.Map#isEmpty()
    */
   public boolean isEmpty() {
     return (keysAndValues == null);
   }
 
   /*
    * @see java.util.Map#containsKey(java.lang.Object)
    */
   public boolean containsKey(Object key) {
     for (int i = 0; i < size(); i++) {
       if (keysAndValues[i].equals(key)) {
         return true;
       }
     }
     return false;
   }
 
   /*
    * @see java.util.Map#containsValue(java.lang.Object)
    */
   public boolean containsValue(Object value) {
     if (keysAndValues == null) {
       return false;
     }
     for (int i = size(); i < keysAndValues.length; i++) {
       if (keysAndValues[i].equals(value)) {
         return true;
       }
     }
     return false;
   }
 
   /*
    * @see java.util.Map#get(java.lang.Object)
    */
   @SuppressWarnings("unchecked")
   public V get(Object key) {
    for (int i = 0; i < size(); i++) {
      if (keysAndValues[i].equals(key)) {
        return (V) keysAndValues[size() + i];
       }
    }
     return null;
   }
 
   private void growByOne() {
     Object[] old = keysAndValues;
     int length = (old == null) ? 0 : old.length;
     keysAndValues = new Object[length + 2];
     for (int i = 0; i < length / 2; i++) {
       keysAndValues[i] = old[i];
     }
     for (int i = 0; i < length / 2; i++) {
       keysAndValues[i + 1 + length / 2] = old[length / 2 + i];
     }
 
   }
 
   /*
    * @see java.util.Map#put(java.lang.Object, java.lang.Object)
    */
   @SuppressWarnings("unchecked")
   public V put(Object key, Object value) {
     if (Assertions.verifyAssertions) {
       Assertions._assert(key != null);
     }
     for (int i = 0; i < size(); i++) {
      if (keysAndValues[i].equals(key)) {
         V result = (V) keysAndValues[size() + i];
         keysAndValues[size() + i] = value;
         return result;
       }
     }
     if (DEBUG_USAGE && size() >= DEBUG_MAX_SIZE) {
       Assertions.UNREACHABLE("too many elements in a SmallMap");
     }
     growByOne();
     keysAndValues[size() - 1] = key;
     keysAndValues[keysAndValues.length - 1] = value;
     return null;
   }
 
   /*
    * @see java.util.Map#remove(java.lang.Object)
    */
   public V remove(Object key) throws UnsupportedOperationException {
     throw new UnsupportedOperationException();
   }
 
   /*
    * @see java.util.Map#putAll(java.util.Map)
    */
   public void putAll(Map<? extends K, ? extends V> t) throws UnsupportedOperationException {
     throw new UnsupportedOperationException();
   }
 
   /*
    * @see java.util.Map#clear()
    */
   public void clear() {
     keysAndValues = null;
   }
 
   /*
    * @see java.util.Map#keySet()
    */
   @SuppressWarnings("unchecked")
   public Set<K> keySet() {
     // TODO: use a better set implementation, SOON!!
     HashSet<K> result = HashSetFactory.make(size());
     for (int i = 0; i < size(); i++) {
       result.add((K) keysAndValues[i]);
     }
     return result;
   }
 
   /*
    * @see java.util.Map#values()
    */
   @SuppressWarnings("unchecked")
   public Collection<V> values() {
     int s = size();
     if (s == 0) {
       return Collections.emptySet();
     }
     HashSet<V> result = HashSetFactory.make(s);
     for (int i = s; i < keysAndValues.length; i++) {
       result.add((V) keysAndValues[i]);
     }
     return result;
   }
 
   /*
    * @see java.util.Map#entrySet()
    */
   public Set<Map.Entry<K,V>> entrySet() throws UnimplementedError {
     Assertions.UNREACHABLE("must implement entrySet");
     return null;
   }
 
 }
