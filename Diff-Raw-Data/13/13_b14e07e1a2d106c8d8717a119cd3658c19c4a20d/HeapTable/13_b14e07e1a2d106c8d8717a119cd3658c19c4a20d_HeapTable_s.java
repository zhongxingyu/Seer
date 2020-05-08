 package com.barchart.store.heap;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.barchart.store.api.ColumnDef;
 import com.barchart.store.api.ObservableIndexQueryBuilder;
 import com.barchart.store.api.ObservableQueryBuilder;
 
 public class HeapTable<K, V> {
 
 	protected final Map<String, HeapRow<K>> rows;
 	protected final Map<String, ColumnDef> columns;
 
 	public HeapTable(final ColumnDef... columns_) {
 		rows = new ConcurrentHashMap<String, HeapRow<K>>();
 		columns = new HashMap<String, ColumnDef>();
 	}
 
 	protected HeapRowMutator<K> mutator(final String key) {
 		return new HeapRowMutator<K>(this, key);
 	}
 
 	public ObservableQueryBuilder<K> fetch(final String... keys)
 			throws Exception {
 
 		if (keys != null && keys.length > 0) {
 
 			final List<HeapRow<K>> matches = new ArrayList<HeapRow<K>>();
 
 			for (final String key : keys) {
 				final HeapRow<K> row = rows.get(key);
 				if (row != null) {
 					matches.add(row);
 				}
 			}
 
 			return new HeapQueryBuilder<K>(
 					Collections.unmodifiableCollection(matches));
 
 		} else {
 
 			return new HeapQueryBuilder<K>(
 					Collections.unmodifiableCollection(rows.values()));
 
 		}
 
 	}
 
 	public ObservableIndexQueryBuilder<K> query() throws Exception {
 		// Unsupported operation, no indexes
 		return new HeapIndexQueryBuilder<K>(null);
 	}
 
 	protected HeapRow<K> remove(final String key) {
 		return rows.remove(key);
 	}
 
 	protected HeapRow<K> get(final String key) {
 		return rows.get(key);
 	}
 
 	protected HeapRow<K> put(final String key, final HeapRow<K> row) {
 		return rows.put(key, row);
 	}
 
 }
