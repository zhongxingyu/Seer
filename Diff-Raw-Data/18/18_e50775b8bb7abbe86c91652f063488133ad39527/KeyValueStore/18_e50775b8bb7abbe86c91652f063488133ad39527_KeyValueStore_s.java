 package com.fishkees.backend.dataaccess;
 
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.collect.Lists;
 
 public abstract class KeyValueStore<K, V> {
 	protected Map<K, V> map;
 	
 	public void put(K key, V value) {
 		map.put(key, value);
 	}
 	
 	public V get(K key) {
 		return map.get(key);
 	}
 	
 	public List<V> all() {
 		return Lists.newArrayList(map.values());
 	}
 	
 	public V remove(K key) {
 		return map.remove(key);
 	}
 	
 	public V update(K key, V value) {
 		if (this.map.containsKey(key)) {
			return map.put(key, value);
 		}
 		
 		return null;
 	};	
 	
 	public abstract K getNewId();
 	public abstract void reset();
 }
