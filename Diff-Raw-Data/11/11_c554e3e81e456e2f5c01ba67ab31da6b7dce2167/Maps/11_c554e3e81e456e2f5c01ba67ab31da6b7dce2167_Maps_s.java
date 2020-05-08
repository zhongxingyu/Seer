 /*
  *   Project: Speedith.Core
  * 
  * File name: Maps.java
  *    Author: Matej Urbas [matej.urbas@gmail.com]
  * 
  *  Copyright © 2011 Matej Urbas
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
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package propity.util;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 import static propity.i18n.Translations.*;
 
 /**
  * Provides convenience methods for easier work with maps.
  * @author Matej Urbas [matej.urbas@gmail.com]
  */
 public final class Maps {
 
     // <editor-fold defaultstate="collapsed" desc="Map Construction Methods">
     /**
      * Creates a {@link TreeMap} from the given collections of keys and values.
      * <p><span style="font-weight:bold">Note</span>: the collections must
      * be non-null and of equal size, otherwise this method throws an {@link IllegalArgumentException}.</p>
      * @param <K> the type of the keys.
      * @param <V> the type of values.
      * @param keys the collection of keys for the new map.
      * @param values the collection of values for the new map.
      * @return a new map of key-value pairs {@code (k_i, v_i)} where {@code k_i}
      * and {@code v_i} are <span style="font-style:italic;">i</span>-th elements
      * of {@code keys} and {@code values} respectively.
      */
     public static <K, V> TreeMap<? extends K, ? extends V> createTreeMap(Collection<? extends K> keys, Collection<? extends V> values) {
         TreeMap<K, V> theMap = new TreeMap<K, V>();
         if (keys == null || values == null || keys.size() != values.size()) {
             throw new IllegalArgumentException(i18n("MAPS_CREATE_ILLEGAL_ARGS"));
         }
         Iterator<? extends K> kIter = keys.iterator();
         Iterator<? extends V> vIter = values.iterator();
         while (kIter.hasNext()) {
             theMap.put(kIter.next(), vIter.next());
         }
         return theMap;
     }
     
     /**
      * Creates a {@link TreeMap} from the given arrays of keys and values.
      * <p><span style="font-weight:bold">Note</span>: the arrays must
      * be non-null and of equal size, otherwise this method throws an {@link IllegalArgumentException}.</p>
      * @param <K> the type of the keys.
      * @param <V> the type of values.
      * @param keys the array of keys for the new map.
      * @param values the array of values for the new map.
      * @return a new map of key-value pairs {@code (k_i, v_i)} where {@code k_i}
      * and {@code v_i} are <span style="font-style:italic;">i</span>-th elements
      * of {@code keys} and {@code values} respectively.
      */
     public static <K, V> TreeMap<K, V> createTreeMap(K[] keys, V... values) {
         TreeMap<K, V> theMap = new TreeMap<>();
         if (keys == null || values == null || keys.length != values.length) {
             throw new IllegalArgumentException(i18n("MAPS_CREATE_ILLEGAL_ARGS"));
         }
         for (int i = 0; i < keys.length; i++) {
             K k = keys[i];
             theMap.put(k, values[i]);
         }
         return theMap;
     }
     
     /**
      * Adds key-value pairs to the given map from the given arrays of
      * keys and values.
      * <p><span style="font-weight:bold">Note</span>: the arrays must
      * be non-null and of equal size, otherwise this method throws an {@link IllegalArgumentException}.</p>
      * @param <K> the type of the keys in the map.
      * @param <V> the type of values in the map.
      * @param theMap the map to which to add the key-and-value pairs.
      * @param keys the array of keys for the new map.
      * @param values the array of values for the new map.
      * @return puts into the given map the key-value pairs {@code (k_i, v_i)}, where {@code k_i}
      * and {@code v_i} are <span style="font-style:italic;">i</span>-th elements
      * of {@code keys} and {@code values} respectively.
      */
     public static <K, V> Map<K, V> addToMap(Map<K, V> theMap, K[] keys, V... values) {
         if (keys == null || values == null || keys.length != values.length) {
             throw new IllegalArgumentException(i18n("MAPS_CREATE_ILLEGAL_ARGS"));
         }
         for (int i = 0; i < keys.length; i++) {
             K k = keys[i];
             theMap.put(k, values[i]);
         }
         return theMap;
     }
     
     /**
      * Adds key-value pairs to the given map from the given collections of
      * keys and values.
      * <p><span style="font-weight:bold">Note</span>: the collections must
      * be non-null and of equal size, otherwise this method throws an {@link IllegalArgumentException}.</p>
      * @param <K> the type of the keys in the map.
      * @param <V> the type of values in the map.
      * @param theMap the map to which to add the key-and-value pairs.
      * @param keys the collection of keys for the new map.
      * @param values the collection of values for the new map.
      * @return puts into the given map the key-value pairs {@code (k_i, v_i)}, where {@code k_i}
      * and {@code v_i} are <span style="font-style:italic;">i</span>-th elements
      * of {@code keys} and {@code values} respectively.
      */
     public static <K, V> Map<K, V> addToMap(Map<K, V> theMap, Collection<? extends K> keys, Collection<? extends V> values) {
         if (keys == null || values == null || keys.size() != values.size()) {
             throw new IllegalArgumentException(i18n("MAPS_CREATE_ILLEGAL_ARGS"));
         }
         Iterator<? extends K> kIter = keys.iterator();
         Iterator<? extends V> vIter = values.iterator();
         while (kIter.hasNext()) {
             theMap.put(kIter.next(), vIter.next());
         }
         return theMap;
     }
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Disabled Constructor">
     private Maps() {
     }
     // </editor-fold>
 }
