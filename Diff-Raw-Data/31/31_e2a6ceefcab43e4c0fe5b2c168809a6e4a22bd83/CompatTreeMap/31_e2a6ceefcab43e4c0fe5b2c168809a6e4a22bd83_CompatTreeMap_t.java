 package me.kevinwells.darxen.compat;
 
 import java.util.Iterator;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 public class CompatTreeMap<K, V> extends TreeMap<K, V> {
 	
 	private static final long serialVersionUID = -5637661680354882597L;
 
	@Deprecated
 	public K lowerKey(K key) {
		//not available in android 2.2
		throw new UnsupportedOperationException();
	}
	
	@Deprecated
	public K higherKey(K key) {
		//not available in android 2.2
		throw new UnsupportedOperationException();
	}
	
	public K getLowerKey(K key) {
 		if (!containsKey(key))
 			return null;
 		
 		SortedMap<K, V> map = headMap(key);
 		if (map.size() < 1) 
 			return null;
 		
 		return map.lastKey();
 	}
 	
	public K getHigherKey(K key) {
 		if (!containsKey(key))
 			return null;
 		
 		SortedMap<K, V> map = tailMap(key);
 		if (map.size() <= 1)
 			return null;
 		
 		Iterator<K> it = map.keySet().iterator();
 		it.next();
 		return it.next();
 	}
 
 }
