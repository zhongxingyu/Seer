 package org.neuronbucket.kv.store.hashmap;
 
import java.util.HashMap;

 import org.neuronbucket.kv.KVStoreTest;
 
 public class HashMapStoreTest extends KVStoreTest<HashMapKVStore<String, byte[]>> {
 
	protected HashMap<String, byte[]> mStore = new HashMap<String, byte[]>();

 	@Override
 	public HashMapKVStore<String, byte[]> createStore() {
		return new HashMapKVStore<String, byte[]>(mStore);
 	}
 
 	@Override
 	public void destroyStore(HashMapKVStore<String, byte[]> store) {
		mStore = new HashMap<String, byte[]>();
 	}
 
 }
