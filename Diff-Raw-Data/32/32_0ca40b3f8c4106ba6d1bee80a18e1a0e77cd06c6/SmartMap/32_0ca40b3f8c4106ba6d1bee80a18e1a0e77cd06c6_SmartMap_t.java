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
 
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import com.stefanmuenchow.functors.BinaryFunction;
 import com.stefanmuenchow.functors.MapBinaryFunction;
 import com.stefanmuenchow.functors.MapPredicate;
 import com.stefanmuenchow.functors.MapUnaryFunction;
 import com.stefanmuenchow.functors.MapVoidFunction;
 
 /**
  * A SmartMap is an object that maps keys to values. There can be no duplicate
  * keys. 
  * 
  * @see Map
  * @see SmartHashMap
  * @see SmartTreeMap
  *
  * @author Stefan Münchow
  */
 public interface SmartMap<K, V> extends Map<K, V> {
 
 	/**
 	 * Adds a key-value-pair to the map and returns it. The original map is
 	 * changed.
 	 * 
 	 * @param key		Key for new value
 	 * @param value		New value
 	 * @return			{@link SmartMap} with pair added
 	 */
 	SmartMap<K, V> putReturn(K key, V value);
 	
 	/**
 	 * Adds all elements of t to this map and returns the resulting map. The 
 	 * original map is changed.
 	 * 
 	 * @param t			Map containing elements to be added
 	 * @return			{@link SmartMap} with all key-value-pairs added
 	 */
 	SmartMap<K, V> putAllReturn(Map<K, V> t);
 	
 	/**
 	 * Removes an entry from the map and returns the result. The original map
 	 * is changed.
 	 * @param key		Key of entry to be removed
 	 * @return			{@link SmartMap} with entry removed
 	 */
 	SmartMap<K, V> removeReturn(K key);
 	
     /**
      * Returns the first element of the Map. If it is an unsorted map, the 
      * ordering is arbitrary.
      * 
      * @throws NoSuchElementException		If map is empty
      * @return 								First element of map					
      */
     Map.Entry<K, V> head() throws NoSuchElementException;
 
     /**
      * Returns the map without the first element. If it is an unsorted map, the 
      * ordering is arbitrary. A new map is created, the original one remains
      * unchanged.
      * 
      * @throws UnsupportedOperationException	If map is empty
      * @return 									Map without first element			
      */
     SmartMap<K, V> tail() throws UnsupportedOperationException;
 
     /**
      * Merges this map with anotherMap using the given merge function. Changes
      * the original map.
      *
      * @param anotherMap 		Map to merge with
      * @param mergeFunct		Merge function to be used
      * 
      * @see BinaryFunction
      * 
      * @return 					Resulting map
      */
     SmartMap<K, V> mergeWith(SmartMap<K, V> anotherMap, BinaryFunction<V, V> mergeFunct);
 
     /**
      * Gets the value to the given key if it exists. Otherwise returns 
      * defaultVal.
      *
      * @param key				Key to get value to
     * @param defaultVal		Default value
      * @return 					Value to given key or defaultVal
      */
     V get(K key, V defaultVal);
 
     /**
      * Returns the first value for which the predicate evaluates to true. If 
      * the predicate evaluates to false for all elements in the map, an 
      * exception is thrown.
      *
      * @param predicate                 Predicate to identify entry
      * @throws NoSuchElementException   If no entry matches precidate
      * @return                          Value satisfying predicate
      */
     V find(MapPredicate<? super K, ? super V> predicate) throws NoSuchElementException;
 
     /**
      * Retains all entries in map for which the predicate evaluates to true.
      * Changes the original map.
      *
      * @param predicate 		Predicate to identify entries
      * @return 					Map containing only entries for which predicate 
      * 							evaluates to true
      */
     SmartMap<K, V> filter(MapPredicate<? super K, ? super V> predicate);
 
     /**
      * Removes all entries from map for which the predicate evaluates to true.
      * Changes the original map.
      *
      * @param predicate 		Predicate to identify entries
      * @return 					Map containing only entries for which predicate 
      * 							evaluates to false
      */
     SmartMap<K, V> remove(MapPredicate<? super K, ? super V> predicate);
 
     /**
      * Searches the map for a given key-value-pair and replaces it with the
      * specified pair. The equals() method is used to compare keys and values. 
      * If the pair is not found, nothing is changed. Changes the original map.
      *
      * @param seekKey		Key to be replaced
      * @param seekValue		Value to be replaced
      * @param newKey		Replacement key
      * @param newValue		Replacement value
      * @return 				Map with entries replaced
      */
     SmartMap<K, V> replace(K seekKey, V seekValue, K newKey, V newValue);
 
     /**
      * Applies the function to each entry of the map and returns a list 
      * containing the result values. The original map remains unchanged. 
      *
      * @param function		Function applied to each element
      * @return 				List of return values
      */
     <R> SmartList<R> map(MapUnaryFunction<R, ? super K, ? super V> function);
 
     /**
      * Combines the elements of this map using a binary function and an initial
      * value. The original map remains unchanged.
      *
      * @param initial		Initial value to use
      * @param funct			Binary function combining two entries
      * 
      * @see BinaryFunction
      * 
      * @return 				Resulting value
      */
     <R> R reduce(R initial, MapBinaryFunction<R, ? super K, ? super V> funct);
 
     /**
      * Calls the toString() method for the key and value of each entry in the
      * map and inserts keyValDelimiter between them. Then entryDelimiter is 
      * inserted between each two entries and the result is returned.
      *
      * @param entryDelimiter	Delimiter to insert between each two entries
      * @param keyValDelimiter	Delimiter to insert between key and value of each 
      * 							entry
      * @return 					Resulting string representation
      */
     String join(String entryDelimiter, String keyValDelimiter);
 
     /**
      * Counts all entries for which the predicate evaluates to true.
      *
      * @param predicate			Predicate to identify elements
      * @return 					Number of elements in map for which predicate
      * 							evaluates to <code>true</code>
      */
     int count(MapPredicate<? super K, ? super V> predicate);
 
     /**
      * Checks if the predicate evaluates to true for any element in the map. 
      * If not, the result is false. Otherwise result is true.
      *
      * @param predicate			Predicate to identify entry
      * @return 					True if any element matches, otherwise false.
      */
     boolean exists(MapPredicate<? super K, ? super V> predicate);
 
     /**
      * Checks if the predicate evaluates to true for all elements in the map. 
      * If not, the result is false. Otherwise result is true.
      *
      * @param predicate			Predicate to check entries with
      * @return 					True if all elements matche, otherwise false.
      */
     boolean forall(MapPredicate<? super K, ? super V> predicate);
     
     /**
      * Applies a function to each entry of the map discarding the
      * result value. Can be used for causing side-effects. 
      * 
      * @param function			Function to be applied
      */
     void foreach(MapVoidFunction<? super K, ? super V> function);
 
     /**
      * Checks if this map describes a bijective function. This is the case when
      * each value in the map is unique.
      *
      * @return 					True if mapping is bijective, otherwise false.
      */
     boolean isBijective();
 
     /**
      * Swaps keys and values. If this map is bijective (can be checked with
      * isBijective() method) operation will be successful and return the new
      * map, otherwise a BadOperationException will be thrown. A new map is 
      * created, the original one remains unchanged.
      *
      * @return					Swapped map
      */
     SmartMap<V, K> swap() throws UnsupportedOperationException;
     
     /**
      * Returns the corresponding java standard map.
      * As smart maps are simple decorators around the standard 
      * maps, just the internal map is returned and no new
      * one has to be created. 
      * 
      * Changes to the returned map will also affect this one.
      * 
      * @return                Standard map wrapped by this decorator
      */
     Map<K, V> toStandardMap();
 }
