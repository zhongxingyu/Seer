 package org.neuronbucket.kv.store.file;
 
 import java.io.File;
 
 import org.neuronbucket.kv.KVStoreTest;
 import org.neuronbucket.kv.store.file.FileKVStore;
 import org.neuronbucket.kv.util.ByteStreamTransformer;
 import org.neuronbucket.kv.util.Factory;
 import org.neuronbucket.kv.util.SingletonFactory;
 import org.neuronbucket.kv.util.StreamTransformer;
 import org.neuronbucket.kv.util.StringToFileTransformer;
 import org.neuronbucket.kv.util.Transformer;
 
 public class FileStoreTest extends KVStoreTest<FileKVStore<String, byte[]>> {
 
 	private File mBaseDir;
 
 	@Override
 	public FileKVStore<String, byte[]> createStore() {
 		mBaseDir = new File("tmp");
		recursiveDelete(mBaseDir);
 		mBaseDir.mkdirs();
 
 		Factory<Transformer<String, File>> keyFactory = new SingletonFactory<Transformer<String, File>>(
 				new StringToFileTransformer(mBaseDir));
 
 		Factory<StreamTransformer<byte[]>> valueFactory = new Factory<StreamTransformer<byte[]>>() {
 			public StreamTransformer<byte[]> newInstance() {
 				return new ByteStreamTransformer();
 			}
 		};
 		return new FileKVStore<String, byte[]>(keyFactory, valueFactory);
 	}
 
 	@Override
 	public void destroyStore(FileKVStore<String, byte[]> store) {
 		recursiveDelete(mBaseDir);
 	}
 
 	private void recursiveDelete(File file) {
 		if (file.isDirectory()) {
 			for (File child : file.listFiles()) {
 				recursiveDelete(child);
 			}
 		}
 		file.delete();
 	}
 }
