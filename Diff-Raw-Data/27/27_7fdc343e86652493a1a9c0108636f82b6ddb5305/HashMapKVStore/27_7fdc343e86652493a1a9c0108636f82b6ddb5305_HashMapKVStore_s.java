 package org.neuronbucket.kv.store.hashmap;
 
 import java.util.HashMap;
 
 import org.neuronbucket.kv.AbstractKVStore;
 import org.neuronbucket.kv.KVStoreContext;
 
 public class HashMapKVStore<K, V> extends AbstractKVStore<K, V> {
 
 	private HashMap<K, V> mStore;
 	private HashMapKVStoreContext<K, V> mContext;
 
 	public HashMapKVStore() {
		mStore = new HashMap<K, V>();
 		mContext = new HashMapKVStoreContext<K, V>(this, mLock, mStore);
 	}
 
 	@Override
 	protected KVStoreContext<K, V> doNewContext() {
 		return mContext;
 	}
 
 	public void flush() {
 	}
 
 }
