 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package com.dmdirc.util;
 
 import java.util.AbstractMap.SimpleEntry;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  * An object that maps keys to values, and values back to keys. Currently
  * does no checking for duplicates. Does not allow null values.
  *
  * @param <A> The first type of data to be mapped
  * @param <B> The second type of data to be mapped
  */
 public class DoubleMap<A,B> implements Map<A,B> {
 
     /** The keys in this map. */
     protected final List<A> keys = new ArrayList<A>();
     /** The values in this map. */
     protected final List<B> values = new ArrayList<B>();
 
     /**
      * Retrieves the value associated with the specified key.
      *
      * @param key The key to search for
      * @return The value of the specified key
      */
     public B getValue(final A key) {
         if (keys.indexOf(key) == -1) {
             return null;
         }
         return values.get(keys.indexOf(key));
     }
 
     /**
      * Retrieves the key associated with the specified value.
      *
      * @param value The value to search for
      * @return The key of the specified value
      */
     public A getKey(final B value) {
         if (values.indexOf(value) == -1) {
             return null;
         }
         return keys.get(values.indexOf(value));
     }
 
     /** {@inheritDoc} */
     @Override
     public B put(final A key, final B value) {
         if (key == null || value == null) {
             throw new NullPointerException();
         }
 
         keys.add(key);
         values.add(value);
 
         return value;
     }
 
     /** {@inheritDoc} */
     @Override
     public Set<A> keySet() {
         return new HashSet<A>(keys);
     }
 
     /** {@inheritDoc} */
     @Override
     public int size() {
         return keys.size();
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean isEmpty() {
         return keys.isEmpty();
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean containsKey(final Object key) {
         return keys.contains((A) key);
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean containsValue(final Object value) {
         return values.contains((B) value);
     }
 
     /** {@inheritDoc} */
     @Override
     public B get(final Object key) {
         return getValue((A) key);
     }
 
     /** {@inheritDoc} */
     @Override
     public B remove(final Object key) {
         if (keys.indexOf(key) == -1) {
             return null;
         }
         final B value = values.remove(keys.indexOf(key));
         keys.remove(keys.indexOf(key));
         return value;
     }
 
     /** {@inheritDoc} */
     @Override
     public void putAll(final Map<? extends A, ? extends B> m) {
         for (Entry<? extends A, ? extends B> entry : m.entrySet()) {
             put(entry.getKey(), entry.getValue());
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void clear() {
         keys.clear();
         values.clear();
     }
 
     /** {@inheritDoc} */
     @Override
     public Collection<B> values() {
         return new ArrayList<B>(values);
     }
 
     /** {@inheritDoc} */
     @Override
     public Set<Entry<A, B>> entrySet() {
         final HashSet<Entry<A, B>> set = new HashSet<Entry<A, B>>();
         for (A key : keys) {
            set.add(new SimpleEntry(key, getValue(key)));
         }
         return set;
     }
 }
