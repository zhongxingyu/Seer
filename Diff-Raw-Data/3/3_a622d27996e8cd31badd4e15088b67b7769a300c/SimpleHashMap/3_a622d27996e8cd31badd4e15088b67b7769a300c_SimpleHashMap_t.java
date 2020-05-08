 package map;
 
 import java.util.Iterator;
 
 public class SimpleHashMap<K,V> implements Map<K,V> {
 	
 	public static final int INITIAL_CAPACITY = 16;
 	public static final double MAX_LOAD_FACTOR = 0.75;
 	private Entry<K,V>[] table;
 	private int size;
 	
 	/** Constructs an empty hashmap with the default initial capacity (16)
 	 *  and the default load factor (0.75). */
 	public SimpleHashMap() {
 		table = createTable(INITIAL_CAPACITY);
 		size = 0;
 	}
 	
 	/** Constructs an empty hashmap with the specified initial capacity
 	 *  and the default load factor (0.75). */
 	public SimpleHashMap(int capacity) {
 		table = createTable(capacity);
 	}
 	
 	@Override
 	public V get(Object arg0) {
 		if(arg0 == null){
 			throw new NullPointerException();
 		}
 		K k = (K)arg0;
 		Entry<K,V> e = find(index(k),k);
 		if(e == null){
 			return null;
 		}else {
 			return e.value;
 		}
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return size == 0;
 	}
 
 	@Override
 	public V put(K k, V v) { //TODO: FIX!
 		if(k == null){
 			throw new NullPointerException();
 		}
 		int i = index(k);
 		Entry<K,V> e = find(i,k);
 		V old = null;
 		if(e == null){
 			e = new Entry<K,V>(k,v);
 			insert(e,i,table);
 			size++;
 			rehashIfNeeded();
 		}else{
 			old = e.getValue();
 			e.setValue(v);
 		}
 		return old;
 	}
 
 	@Override
 	public V remove(Object arg0) {
 		if (arg0 == null) {
 			throw new NullPointerException();
 		}
 		K k = (K)arg0;
 		int i = index(k);
 		LinkIterator itr = new LinkIterator(i);
 		if (itr.hasNext()) {
 			Entry<K,V> prev = itr.next();
 			if (prev.key.equals(k)) {
 				table[i] = prev.next;
 				size--;
 				return prev.value;
 			}
 			while (itr.hasNext()) {
 				Entry<K,V> e = itr.next();
 				if (e.key.equals(k)) {
 					prev.next = e.next;
 					size--;
 					return e.value;
 				}
				prev = e;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public int size() {
 		return size;
 	}
 	
 	
 
 	public String show(){
 		StringBuilder sb = new StringBuilder();
 		for(int i=0; i < table.length; i++){
 			sb.append(i);
 			sb.append("\t");
 			LinkIterator itr = new LinkIterator(i);
 			while(itr.hasNext()){
 				Entry<K,V> e = itr.next();
 				sb.append(" " + e.toString());
 			}
 			sb.append("\n");
 		}
 		return sb.toString();
 	}
 
 	
 	private int index(K key) {
 		return index(key,table.length);
 	}
 	private int index(K key, int length){
 		return Math.abs(key.hashCode()) % length;
 	}
 	
 	private Entry<K,V> find(int index, K key) {
 		LinkIterator itr = new LinkIterator(index);
 		while(itr.hasNext()) {
 			Entry<K,V> e = itr.next();
 			if (e.key.equals(key)) {
 				return e;
 			}
 		}
 		return null;
 	}
 	
 	private void rehashIfNeeded(){
 		if(size < MAX_LOAD_FACTOR*table.length){//TODO! make smaller?
 			return;
 		}
 		
 		int newLength = table.length*2;
 		Entry<K,V>[] newTable = createTable(newLength);
 		int check_size = 0;
 		for(int i=0; i< table.length; i++){
 			LinkIterator itr = new LinkIterator(i);
 			while(itr.hasNext()){
 				Entry<K,V> e = itr.next();
 				e.next = null;
 				insert(e,index(e.key,newLength),newTable);
 				check_size++;
 			}
 		}
 		table = newTable;
 	}
 	
 	private void insert(Entry<K,V> e, int i, Entry<K,V>[] table){
 		if(table[i] != null){
 			e.next = table[i];
 		}
 		table[i] = e;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Entry<K,V>[] createTable(int size){
 		return (Entry<K,V>[]) new Entry[size];
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
 			return key;
 		}
 
 		@Override
 		public V getValue() {
 			return value;
 		}
 
 		@Override
 		public V setValue(V value) {
 			V old = this.value;
 			this.value = value;
 			return old;
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
 			return next != null;
 		}
 
 		@Override
 		public Entry<K,V> next() {
 			if(!hasNext()){
 				return null;
 			}
 			Entry<K,V> next = this.next;
 			this.next = this.next.next;
 			return next;
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 			
 		}
 	}
 
 }
