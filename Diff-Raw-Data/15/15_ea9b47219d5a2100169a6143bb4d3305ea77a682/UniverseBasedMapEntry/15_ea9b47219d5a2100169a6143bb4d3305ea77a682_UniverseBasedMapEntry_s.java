 package org.emmef.cheapsets;
 
 import java.util.Map.Entry;
 
 import com.google.common.base.Objects;
 
 /**
  * Entry object for the {@link UniverseBasedMap}, that implements
  * {@link #equals(Object)}, {@link #hashCode()} and {@link #toString()} contract
  * according to the specifications in {@link Entry}.
  * 
  * @see Entry
  * @see UniverseBasedMap
  */
 final class UniverseBasedMapEntry<K, V> implements Entry<K, V> {
 	private final int index;
 	private final UniverseBasedMap<K, V> map;
 
 	UniverseBasedMapEntry(UniverseBasedMap<K, V> map, int index) {
 		this.map = map;
 		this.index = index;
 	}
 
 	@Override
 	public K getKey() {
 		return map.getSubset().elementAt(index);
 	}
 
 	@Override
 	public V getValue() {
		return (V) map.getAt(index);
 	}
 
 	@Override
 	public V setValue(V value) {
		return map.setAt(index, value);
 	}
 	
 	/**
 	 * @see Entry#hashCode()
 	 */
 	@Override
 	public final int hashCode() {
 		Object key = getKey(); 
 		Object value = getValue();
 		
		return (key != null ? key.hashCode() : 0) ^ (value != null ? value.hashCode() : 0); 
 	};
 	
 	/**
 	 * @see Entry#equals(Object)
 	 */
 	@Override
 	public final boolean equals(Object obj) {
 		if (obj == this) {
 			return true;
 		}
 		
 		if (!(obj instanceof Entry)) {
 			return false;
 		}
 		
 		Entry<?,?> entry = (Entry<?,?>)obj;
 		
 		return Objects.equal(getKey(), entry.getKey()) && Objects.equal(getValue(), entry.getValue());
 	}
 	
 	
 	@Override
 	public final String toString() {
 		K key = getKey();
 		if (key == null) {
 			return "null=" + getValue();
 		}
 		return key.toString() + "=" + getValue();
 	}
 }
