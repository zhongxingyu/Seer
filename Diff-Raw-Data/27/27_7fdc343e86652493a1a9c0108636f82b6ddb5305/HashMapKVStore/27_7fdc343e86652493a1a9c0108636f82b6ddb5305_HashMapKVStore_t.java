 package org.neuronbucket.kv.store.hashmap;
 
 import java.util.HashMap;
 
 import org.neuronbucket.kv.AbstractKVStore;
 import org.neuronbucket.kv.KVStoreContext;
 
 public class HashMapKVStore<K, V> extends AbstractKVStore<K, V> {
 
 	private HashMap<K, V> mStore;
 	private HashMapKVStoreContext<K, V> mContext;
 
 	public HashMapKVStore() {
		this(new HashMap<K, V>());
	}

	public HashMapKVStore(HashMap<K, V> store) {
		mStore = store;
 		mContext = new HashMapKVStoreContext<K, V>(this, mLock, mStore);
 	}
 
 	@Override
 	protected KVStoreContext<K, V> doNewContext() {
 		return mContext;
 	}
 
 	public void flush() {
 	}
 
 }
