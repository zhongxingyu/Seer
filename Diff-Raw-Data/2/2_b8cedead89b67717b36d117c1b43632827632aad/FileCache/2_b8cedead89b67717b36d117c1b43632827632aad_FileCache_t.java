 package edu.teco.dnd.util;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.lang.ref.SoftReference;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * A cache for File data that uses SoftReferences to be memory friendly.
  * 
  * @author Philipp Adolf
  */
 public class FileCache {
 	/**
 	 * The cache. If a File's data is loaded a SoftReference to the data is put in here.
 	 */
 	private final Map<File, SoftReference<byte[]>> cache = new HashMap<File, SoftReference<byte[]>>();
 	
 	/**
 	 * Used to synchronize access to {@link #cache}.
 	 */
 	private final ReadWriteLock lock = new ReentrantReadWriteLock();
 	
 	/**
 	 * Retrieves the File's data. If the File's data is cached the cached value is returned. Otherwise
 	 * the File is read and data is put into the cache. There is currently no way to invalidate a cache
 	 * entry (for example if a File has been modified).
 	 * 
 	 * @param file the File to read
 	 * @return the contents of the File. May be a cached and potentially outdated version of the File.
 	 * @throws IOException if an error occurs while reading the File
 	 */
 	public byte[] getFileData(final File file) throws IOException {
 		SoftReference<byte[]> reference = null;
 		byte[] data = null;
 		
 		lock.readLock().lock();
 		try {
 			reference = cache.get(file);
 		} finally {
 			lock.readLock().unlock();
 		}
 		
 		if (reference != null) {
 			data = reference.get();
 			if (data != null) {
 				return data;
 			}
 		}
 		
 		data = new byte[(int) file.length()];
 		final DataInputStream dis = new DataInputStream(new FileInputStream(file));
 		try {
 			dis.readFully(data);
 		} finally {
 			dis.close();
 		}
 		
 		lock.writeLock().lock();
 		try {
 			reference = cache.get(file);
 			if (reference == null || reference.get() == null) {
 				cache.put(file, new SoftReference<byte[]>(data));
 			}
 		} finally {
 			lock.writeLock().unlock();
 		}
 		
		return data;
 	}
 }
