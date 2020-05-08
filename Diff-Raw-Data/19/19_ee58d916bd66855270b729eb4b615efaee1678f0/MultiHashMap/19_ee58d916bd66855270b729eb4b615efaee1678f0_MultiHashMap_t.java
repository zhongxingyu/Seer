 package org.magnolialang.util.depgraph;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 public class MultiHashMap<K, V> implements IMultiMap<K, V> {
 	private final HashMap<K, Set<? extends V>>	map	= new HashMap<K, Set<? extends V>>();
 
 
 	@Override
 	public void clear() {
 		map.clear();
 	}
 
 
 	@Override
 	public boolean containsKey(Object key) {
 		return map.containsKey(key);
 	}
 
 
 	@Override
 	public MultiHashMap<K, V> copy() {
 		MultiHashMap<K, V> multiMap = new MultiHashMap<K, V>();
 		for(Entry<K, Set<? extends V>> entry : entrySet())
 			multiMap.put(entry.getKey(), entry.getValue());
 		return multiMap;
 	}
 
 
 	@Override
 	public Set<Entry<K, Set<? extends V>>> entrySet() {
 		return map.entrySet();
 	}
 
 
 	@Override
 	public boolean equals(Object obj) {
 		if(this == obj)
 			return true;
 		if(obj == null)
 			return false;
 		if(!(obj instanceof IMultiMap))
 			return false;
 		if(!keySet().equals(((IMultiMap<?, ?>) obj).keySet()))
 			return false;
 		@SuppressWarnings("unchecked")
 		IMultiMap<K, ?> other = (IMultiMap<K, ?>) obj;
 		for(K key : keySet())
 			if(!map.get(key).equals(other.get(key)))
 				return false;
 		return true;
 	}
 
 
 	@Override
 	public Set<V> get(K key) {
 		return (Set<V>) map.get(key);
 	}
 
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (map == null ? 0 : map.hashCode());
 		return result;
 	}
 
 
 	@Override
 	public boolean isEmpty() {
 		return map.isEmpty();
 	}
 
 
 	@Override
 	public boolean isEmpty(K key) {
 		if(map.containsKey(key))
 			return map.get(key).isEmpty();
 		else
 			return true;
 	}
 
 
 	@Override
 	public Set<K> keySet() {
 		return map.keySet();
 	}
 
 
 	@Override
 	public int numKeys() {
 		return map.size();
 	}
 
 
 	@Override
 	public void put(K key) {
 		Set<V> c = (Set<V>) map.get(key);
 		if(c == null) {
 			c = new HashSet<V>();
 			map.put(key, c);
 		}
 	}
 
 
 	@Override
 	public void put(K arg0, Set<? extends V> val) {
 		Set<V> c = (Set<V>) map.get(arg0);
 		if(c == null) {
 			c = new HashSet<V>();
 			map.put(arg0, c);
 		}
		c.addAll(val);
 	}
 
 
 	@Override
 	public void put(K key, V val) {
 		Set<V> c = (Set<V>) map.get(key);
 		if(c == null) {
 			c = new HashSet<V>();
 			map.put(key, c);
 		}
		c.add(val);
 	}
 
 
 	@Override
 	public void putAll(IMultiMap<? extends K, ? extends V> arg0) {
 		for(Entry<? extends K, ?> entry : arg0.entrySet())
 			put(entry.getKey(), (Set<V>) entry.getValue());
 	}
 
 
 	@Override
 	public void putAll(Map<? extends K, ? extends V> arg0) {
 		for(Entry<? extends K, ? extends V> entry : arg0.entrySet())
 			put(entry.getKey(), entry.getValue());
 	}
 
 
 	@Override
 	public Set<V> remove(K key) {
 		return (Set<V>) map.remove(key);
 	}
 
 
 	@Override
 	public boolean remove(K key, V value) {
 		Set<V> c = (Set<V>) map.get(key);
 		if(c == null)
 			return false;
 		return c.remove(value);
 	}
 }
