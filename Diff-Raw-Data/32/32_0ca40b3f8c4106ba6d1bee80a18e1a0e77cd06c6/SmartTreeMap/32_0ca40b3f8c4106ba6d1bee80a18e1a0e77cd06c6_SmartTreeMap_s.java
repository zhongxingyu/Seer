 /**
  * Copyright (c) Stefan Münchow. All rights reserved.
  * 
  * The use and distribution terms for this software are covered by the
  * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
  * which can be found in the file epl-v10.html at the root of this distribution.
  * By using this software in any fashion, you are agreeing to be bound by
  * the terms of this license.
  * You must not remove this notice, or any other, from this software.
  **/
 
 package com.stefanmuenchow.collections;
 
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 /**
  * Implementation of the {@link SmartSortedMap} interface decorating a
  * standard {@link TreeMap}.
  * 
  * @see TreeMap
  * 
  * @author Stefan Münchow
  */
 public class SmartTreeMap<K, V> extends AbstractSmartMap<K, V> implements SmartSortedMap<K, V> {
 
 	/**
 	 * Creates a new instance containig all entries of the specified map.
 	 * 
 	 * @param map	Entries to be contained
 	 */
     public SmartTreeMap(final Map<K, V> map) {
         super(new TreeMap<K, V>(map));
     }
     
     /**
      * Creates a new empty map.
      */
     public SmartTreeMap() {
         this(new TreeMap<K, V>());
     }
     
     /**
      * Creates a new map from a list of tuples. Each tuple is converted to a map
      * entry by using the first value as entry key and the second value as entry 
      * value.
      * 
     * @param tupleColl		Collection of tuples
      */
     public SmartTreeMap(SmartList<K> keys, SmartList<V> vals) {
     	this(keys.zipWith(vals));
     }
     
     /**
      * Creates a new map from two lists, using each element of the first list 
      * as a key and the element at the corresponding position in the second 
      * list as its value.
      * 
     * @param keys		List containing keys
     * @param vals		List containing values
      */
     public SmartTreeMap(Collection<Tuple<K, V>> tupleColl) {
     	this(new TreeMap<K, V>());
     	
     	for (Tuple<K, V> tuple : tupleColl) {
     		put(tuple.getFirst(), tuple.getSecond());
     	}
     }
 
     private SortedMap<K, V> getInternalMap() {
         return (SortedMap<K, V>) internalMap;
     }
 
     @Override
     protected SmartSortedMap<K, V> createNewInstance() {
         return new SmartTreeMap<K, V>();
     }
 
     @Override
     protected <S, R> SmartSortedMap<S, R> createNewInstance(final Map<S, R> aMap) {
         return new SmartTreeMap<S, R>(aMap);
     }
 
     /** SortedMap methods */
 
     @Override
     public Comparator<? super K> comparator() {
         return getInternalMap().comparator();
     }
 
     @Override
     public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
         return getInternalMap().subMap(fromKey, toKey);
     }
 
     @Override
     public SortedMap<K, V> headMap(final K toKey) {
         return getInternalMap().headMap(toKey);
     }
 
     @Override
     public SortedMap<K, V> tailMap(final K fromKey) {
         return getInternalMap().tailMap(fromKey);
     }
 
     @Override
     public K firstKey() {
         return getInternalMap().firstKey();
     }
 
     @Override
     public K lastKey() {
         return getInternalMap().lastKey();
     }
 
 	@Override
 	public SmartSortedMap<K, V> smartSubMap(K fromKey, K toKey) {
 		return createNewInstance(subMap(fromKey, toKey));
 	}
 
 	@Override
 	public SmartSortedMap<K, V> smartHeadMap(K toKey) {
 		return createNewInstance(headMap(toKey));
 	}
 
 	@Override
 	public SmartSortedMap<K, V> smartTailMap(K fromKey) {
 		return createNewInstance(tailMap(fromKey));
 	}
     
     @Override
     public SortedMap<K, V> toStandardMap() {
     	return (SortedMap<K, V>) getInternalMap();
     }
 }
