 package assignment2;
 
 public class Table<K extends Data, V extends Clonable> implements TableInterface<K, V> {
 	
 	List<KeyValuePair<K,V>> list;
 	
 	Table() {
 		list = new List<KeyValuePair<K,V>>();
 	}
 	
 	public boolean isEmpty() {
 		return list.isEmpty();
 	}
 	
 	public Table<K,V> init() {
 		list = new List<KeyValuePair<K,V>>();
 		return this;
 	}
 	
 	public int getSize () {
 		return list.getSize();
 	}
 	
 	public void insert (K k, V v) {
 		list.insert(new KeyValuePair<K,V>(k, v));
 	}
 	
 	public V retrieve (K k) {
 		list.setFirst();
 		do {
 			if (list.retrieve().k.compareTo(k) == 0) {
 				return list.retrieve().v;
 			}
 		} while (list.getNext());
 		return null;
 	}
 	
 	public Table<K,V> clone () {
		Table<K,V> t = new Table<K,V>();
		t.list = list.clone();
		return t;
 	}
 }
