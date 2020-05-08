 package com.gentics.cr.lucene.indexer.index;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.store.LockFactory;
 import org.apache.lucene.store.RAMDirectory;
 
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.util.generics.Instanciator;
 
 /**
  * Creates and caches directories.
  * @author Christopher
  *
  */
 public final class LuceneDirectoryFactory {
 	/**
 	 * Logger.
 	 */
 	protected static final Logger LOG = Logger.getLogger(LuceneDirectoryFactory.class);
 	/**
 	 * Key that determines if a directory should be created in memory.
 	 */
 	protected static final String RAM_IDENTIFICATION_KEY = "RAM";
 	/**
 	 * Key to fetch the configured lock factory class.
 	 */
 	protected static final String LOCK_FACTORY_CLASS_KEY = "lockFactoryClass";
 
 	/**
 	 * ConcurrentHashMap to cache directories.
 	 */
 	private static ConcurrentHashMap<String, Directory> cachedDirectories = new ConcurrentHashMap<String, Directory>();
 
 	/**
 	 * Private constructor.
 	 */
 	private LuceneDirectoryFactory() {
 	}
 
 	/**
 	 * Fetches a directory for the given location. 
 	 * If there is none it creates a directory 
 	 * on the given location. Directories will be cached by its location.
 	 * @param directoyLocation String pointing to the location. 
 	 * 			If the string starts with RAM,
 	 * 			the directory will be created in memory.
 	 * @param config Configuration that may contain a configured lock factory.
 	 * @return directory.
 	 */
 	public static Directory getDirectory(final String directoyLocation, final CRConfig config) {
 		Directory dir = cachedDirectories.get(directoyLocation);
 		if (dir == null) {
 			dir = createNewDirectory(directoyLocation, config);
 		}
 		return dir;
 	}
 	
 	/**
 	 * Fetches a directory for the given location. 
 	 * If there is none it creates a directory 
 	 * on the given location. Directories will be cached by its location.
 	 * @param directoyLocation String pointing to the location. 
 	 * 			If the string starts with RAM,
 	 * 			the directory will be created in memory.
 	 * @return directory.
 	 */
 	public static Directory getDirectory(final String directoyLocation) {
 		return getDirectory(directoyLocation, null);
 	}
 	
 	/**
 	 * Create a new directory.
 	 * @param directoyLocation directoryLocation.
 	 * @param config configuration that may contain the configured lock factory.
 	 * @return new directory
 	 */
 	private static synchronized Directory createNewDirectory(
 			final String directoyLocation,
 			final CRConfig config) {
 		Directory dir = cachedDirectories.get(directoyLocation);
 		if (dir == null) {
 			Directory newDir = createDirectory(directoyLocation, config);
 			dir = cachedDirectories.putIfAbsent(directoyLocation, newDir);
 			if (dir == null) {
 				dir = newDir;
 			}
 		}
 		return dir;
 	}
 
 	/**
 	 * Creates a new directory.
 	 * @param directoryLocation location
 	 * @param config configuration that may contain a configured lock factory.
 	 * @return directory
 	 */
 	private static Directory createDirectory(final String directoryLocation, final CRConfig config) {
 		Directory dir;
 		if (RAM_IDENTIFICATION_KEY.equalsIgnoreCase(directoryLocation) 
 				|| directoryLocation == null
 				|| directoryLocation.startsWith(RAM_IDENTIFICATION_KEY)) {
 			dir = createRAMDirectory(directoryLocation);
 
 		} else {
 			File indexLoc = new File(directoryLocation);
 			try {
 				dir = createFSDirectory(indexLoc, directoryLocation);
 				if (dir == null) {
 					dir = createRAMDirectory(directoryLocation);
 				}
 			} catch (IOException ioe) {
 				dir = createRAMDirectory(directoryLocation);
 			}
 		}
 		if (config != null) {
 			String lockFactoryClass = config.getString(LOCK_FACTORY_CLASS_KEY);
 			if (lockFactoryClass != null && !"".equals(lockFactoryClass)) {
 				
 				LockFactory lockFactory = (LockFactory) Instanciator.getInstance(lockFactoryClass, new Object[][] {
 						new Object[] {}, 
 						new Object[] {config}
 				});
				try {
					dir.setLockFactory(lockFactory);
				} catch (IOException e) {
					LOG.error("Error while setting lock factory.", e);
 				}
 			}
 		}
 		return dir;
 	}
 
 	/**
 	 * Creates a FSDirectory on the given location.
 	 * @param indexLoc location.
 	 * @param name name for logging
 	 * @return FSDirectory
 	 * @throws IOException on error.
 	 */
 	protected static Directory createFSDirectory(final File indexLoc,
 			final String name) throws IOException {
 		if (!indexLoc.exists()) {
 			LOG.debug("Indexlocation did not exist. Creating directories...");
 			indexLoc.mkdirs();
 		}
 		Directory dir = FSDirectory.open(indexLoc);
 		LOG.debug("Creating FS Directory for Index [" + name + "]");
 		return (dir);
 	}
 
 	/**
 	 * Creates a Directory in memory.
 	 * @param name name of the directory
 	 * @return directory.
 	 */
 	protected static Directory createRAMDirectory(final String name) {
 		Directory dir = new RAMDirectory();
 		LOG.debug("Creating RAM Directory for Index [" + name + "]");
 		return (dir);
 	}
 }
