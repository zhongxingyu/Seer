 package map;
 
 import java.util.Iterator;
 
 public class SimpleHashMap<K,V> implements Map<K,V> {
 	
 	public static final int INITIAL_CAPACITY = 16;
 	public static final double INITIAL_LOAD_FACTOR = 0.75;
 	private Entry<K,V>[] table;
 	private int size;
 	
 	/** Constructs an empty hashmap with the default initial capacity (16)
 	 *  and the default load factor (0.75). */
 	public SimpleHashMap() {
 		table = (Entry<K,V>[]) new Entry[INITIAL_CAPACITY];
 		size = 0;
 	}
 	
 	/** Constructs an empty hashmap with the specified initial capacity
 	 *  and the default load factor (0.75). */
 	public SimpleHashMap(int capacity) {
 		table = (Entry<K,V>[]) new Entry[capacity];
 	}
 	
 	@Override
 	public V get(Object arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return size == 0;
 	}
 
 	@Override
 	public V put(K arg0, V arg1) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public V remove(Object arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public int size() {
 		return size;
 	}
 	
 	
 	/*
 	public String show(){
 		StringBuilder sb = new StringBuilder();
 		for(int i=0; i < table.length; i++){
 			sb.append(i);
 			sb.append("\t");
 			Entry<K,V> e = table[i];
 			if(e )
 			sb.append(table[i].toString());
 		}
 		return sb.toString();
 	}
 	*/
 	
 	private int index(K key) {
 		return 0;
 	}
 	
 	private Entry<K,V> find(int index, K key) {
		Iterator itr = new LinkIterator(index);
 		while(itr.hasNext()) {
 			Entry<K,V> e = itr.next();
 			if (e.key == key) {
 				return e;
 			}
 		}
 		return null;
 	}
 	
 	private static class Entry<K,V> implements Map.Entry<K,V> {
 		
 		private K key;
 		private V value;
 		private Entry<K,V> next;
 		
 		public Entry(K key, V value) {
 			this.key = key;
 			this.value = value;
 		}
 		
 		@Override
 		public K getKey() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public V getValue() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public V setValue(V value) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 		
 		public String toString() {
 			return key.toString() + "=" + value.toString();
 		}
 		
 	}
 	
 	private class LinkIterator implements Iterator<Entry<K,V>>{
 		private Entry<K,V> next;
 		
 		public LinkIterator(int index){
 			next = table[index];
 		}
 		
 		@Override
 		public boolean hasNext() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public Entry<K,V> next() {
 			// TODO Auto-generated method stub
 			return next;
 		}
 
 		@Override
 		public void remove() {
 			// TODO Auto-generated method stub
 			
 		}
 	}
 
 }
