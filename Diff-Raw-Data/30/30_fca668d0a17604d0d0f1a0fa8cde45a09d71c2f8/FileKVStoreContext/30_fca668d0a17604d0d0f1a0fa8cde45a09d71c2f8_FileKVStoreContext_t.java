 package org.neuronbucket.kv.store.file;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.concurrent.locks.ReadWriteLock;
 
 import org.neuronbucket.kv.AbstractKVStoreContext;
 import org.neuronbucket.kv.util.StreamTransformer;
 import org.neuronbucket.kv.util.Transformer;
 
 public class FileKVStoreContext<K, V> extends AbstractKVStoreContext<K, V> {
 
 	private Transformer<K, File> mKeyTransformer;
 	private StreamTransformer<V> mValueTransformer;
 
 	public FileKVStoreContext(
 			FileKVStore<K, V> parent,
 			ReadWriteLock lock,
 			Transformer<K, File> fileNameTansformer,
 			StreamTransformer<V> valueTransformer) {
 		super(parent, lock);
 		mKeyTransformer = fileNameTansformer;
 		mValueTransformer = valueTransformer;
 	}
 
 	protected void doRemove(K key) throws IOException {
 		File file = mKeyTransformer.transformTo(key);
 		file.delete();
 	}
 
 	@Override
 	protected V doGet(K key) throws IOException {
 		File file = mKeyTransformer.transformTo(key);
 		long len = file.length();
 		FileInputStream fis = null;
 		try {
 			fis = new FileInputStream(file);
 			return mValueTransformer.read(fis, (int) len);
 		} catch (FileNotFoundException e) {
 			return null;
 		} finally {
 			if (fis != null) {
 				fis.close();
 			}
 		}
 	}
 
 	@Override
 	protected void doPut(K key, V value) throws IOException {
 		File file = mKeyTransformer.transformTo(key);
		file.getParentFile().mkdirs();
 		File tmpFile = File.createTempFile("kvstore", "tmp", file.getParentFile());
 		FileOutputStream fos = new FileOutputStream(tmpFile);
 		try {
 			mValueTransformer.write(value, fos);
 		} finally {
 			fos.close();
 		}
 
 		if (!tmpFile.renameTo(file)) {
 			if (file.exists()) {
 				file.delete();
 			}
 			for (int i = 0; i < 10; i++) {
 				if (tmpFile.renameTo(file)) {
 					return;
 				}
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					throw new IOException("Interrupted while trying to rename file", e);
 				}
 			}
 		} else {
 			return;
 		}
 		tmpFile.delete();
 		throw new IOException("Unable to rename " + tmpFile + " to " + file);
 	}
 }
