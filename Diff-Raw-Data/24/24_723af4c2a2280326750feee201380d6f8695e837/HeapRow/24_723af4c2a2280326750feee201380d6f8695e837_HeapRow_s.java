 package com.barchart.store.heap;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
import java.util.Map;
 import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
 
 import com.barchart.store.api.StoreColumn;
 import com.barchart.store.api.StoreRow;
 
 public class HeapRow<K> implements StoreRow<K> {
 
 	private final String key;
	private final SortedSet<K> names;
	private final Map<K, HeapColumn<K>> columns;
 
 	public HeapRow(final String key_) {
 		key = key_;
		names = new ConcurrentSkipListSet<K>();
		columns = new ConcurrentHashMap<K, HeapColumn<K>>();
 	}
 
 	@Override
 	public String getKey() {
 		return key;
 	}
 
 	protected SortedSet<K> columnsImpl() {
		return Collections.unmodifiableSortedSet(names);
 	}
 
 	@Override
 	public Collection<K> columns() {
 		return Collections.unmodifiableCollection(columns.keySet());
 	}
 
 	@Override
 	public StoreColumn<K> getByIndex(int index) {
 
		final Iterator<K> iter = names.iterator();
 
 		while (index > 0) {
 			if (!iter.hasNext()) {
 				throw new ArrayIndexOutOfBoundsException();
 			}
 			iter.next();
 			index--;
 		}
 
 		if (!iter.hasNext()) {
 			throw new ArrayIndexOutOfBoundsException();
 		}
 
 		return columns.get(iter.next());
 
 	}
 
 	@Override
 	public StoreColumn<K> get(final K name) {
 		return columns.get(name);
 	}
 
 	protected HeapColumn<K> getImpl(final K name) {
 		return columns.get(name);
 	}
 
 	protected void update(final HeapColumn<K> column) {
		names.add(column.getName());
 		columns.put(column.getName(), column);
 	}
 
 	protected void delete(final K name) {
		names.remove(name);
 		columns.remove(name);
 	}
 
 	@Override
 	public boolean equals(final Object that) {
 		if (that instanceof HeapRow) {
 			return key.equals(((HeapRow<?>) that).key);
 		}
 		return false;
 	}
 
 }
