 package com.stefanmuenchow.functors;
 
 /**
  * Unary function to be used to cause side effects by some special operations 
  * on smart maps. For this purpose this interface is implemented by an 
  * anonymous class that is passed to the map function.
  *
  * @author Stefan Münchow
  */
 public interface MapVoidFunction<K, V> {
 
 	/**
 	 * Executes this function.
 	 * 
     * @param key		Key of Map.Entry
     * @param value		Value of Map.Entry
 	 */
 	void apply(K key, V value);
 }
