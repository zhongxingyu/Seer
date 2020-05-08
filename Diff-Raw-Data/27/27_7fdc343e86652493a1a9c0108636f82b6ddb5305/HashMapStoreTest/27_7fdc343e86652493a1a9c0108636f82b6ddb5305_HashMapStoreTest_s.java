 package org.neuronbucket.kv.store.hashmap;
 
 import org.neuronbucket.kv.KVStoreTest;
 
 public class HashMapStoreTest extends KVStoreTest<HashMapKVStore<String, byte[]>> {
 
 	@Override
 	public HashMapKVStore<String, byte[]> createStore() {
		return new HashMapKVStore<String, byte[]>();
 	}
 
 	@Override
 	public void destroyStore(HashMapKVStore<String, byte[]> store) {
 	}
 
 }
