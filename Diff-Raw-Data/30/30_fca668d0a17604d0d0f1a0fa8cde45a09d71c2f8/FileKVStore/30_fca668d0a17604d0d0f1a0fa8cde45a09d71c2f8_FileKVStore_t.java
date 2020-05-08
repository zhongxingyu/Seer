 package org.neuronbucket.kv.store.file;
 
 import java.io.File;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.neuronbucket.kv.AbstractKVStore;
 import org.neuronbucket.kv.KVStoreContext;
 import org.neuronbucket.kv.util.Factory;
 import org.neuronbucket.kv.util.StreamTransformer;
 import org.neuronbucket.kv.util.Transformer;
 
 public class FileKVStore<K, V> extends AbstractKVStore<K, V> {
 
	public static <K, V> FileKVStore<K, V> newInstance(
			Factory<? extends Transformer<K, File>> fileNameTransformer,
			Factory<? extends StreamTransformer<V>> valueTransformer) {
		return new FileKVStore<K, V>(fileNameTransformer, valueTransformer);
	}

	private Factory<? extends Transformer<K, File>> mKeyTransformer;
	private Factory<? extends StreamTransformer<V>> mValueTransformer;
 	private ReadWriteLock mLock = new ReentrantReadWriteLock();
 
 	public FileKVStore(
			Factory<? extends Transformer<K, File>> fileNameTansformer,
			Factory<? extends StreamTransformer<V>> valueTransformer) {
 		mKeyTransformer = fileNameTansformer;
 		mValueTransformer = valueTransformer;
 	}
 
 	@Override
 	protected KVStoreContext<K, V> doNewContext() {
 		return new FileKVStoreContext<K, V>(
 				this,
 				mLock,
 				mKeyTransformer.newInstance(),
 				mValueTransformer.newInstance());
 	}
 
 	public void flush() {
 	}
 
 }
