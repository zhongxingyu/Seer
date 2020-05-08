 package com.fathomdb.config;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Objects;
 
 public abstract class FilesystemConfigProvider<T extends ConfigObject> extends ConfigProvider<T> implements
 		HasConfiguration {
 	static final Logger log = LoggerFactory.getLogger(FilesystemConfigProvider.class);
 
 	private final File baseDir;
 	final boolean convertToLowerCase;
 
 	protected static final boolean NORMALIZE_LOWER_CASE = true;
 	protected static final boolean MIXED_CASE = false;
 
 	private final ScheduledExecutorService threadPool;
 
 	public FilesystemConfigProvider(ScheduledExecutorService threadPool, File baseDir, boolean convertToLowerCase) {
 		this.threadPool = threadPool;
 		this.baseDir = baseDir;
 		this.convertToLowerCase = convertToLowerCase;
 	}
 
 	@Override
 	public void initialize() {
 		threadPool.scheduleWithFixedDelay(new UpdateChecker(), UpdateChecker.INTERVAL, UpdateChecker.INTERVAL,
 				TimeUnit.SECONDS);
 	}
 
 	class UpdateChecker implements Runnable {
 		static final int INTERVAL = 30;
 
 		@Override
 		public void run() {
 			refresh();
 		}
 	};
 
 	@Override
 	protected T buildConfig(String key) {
		File file = toFile(key);
 		if (!file.exists()) {
 			// TODO: Return dummy HostConfig
 			return buildNullResult(key);
 		}
 
 		FileInputStream fis = null;
 		try {
 			fis = new FileInputStream(file);
 			String version = toVersion(file);
 			return loadConfig(key, version, fis);
 		} catch (IOException e) {
 			throw new IllegalStateException("Error loading host configuration", e);
 		} finally {
 			if (fis != null) {
 				try {
 					fis.close();
 				} catch (IOException e) {
 					log.warn("Error closing file", e);
 				}
 			}
 		}
 	}
 
 	protected abstract T buildNullResult(String key);
 
 	protected abstract T loadConfig(String key, String version, InputStream is) throws IOException;
 
 	public String toVersion(File file) {
 		long lastModified = file.lastModified();
 		return String.valueOf(lastModified);
 	}
 
	private File toFile(String key) {
		String fileName = toFileName(key);
		File file = new File(baseDir, fileName);
		return file;
	}

 	private String toFileName(String key) {
 		char[] sanitizedChars = null;
 
 		for (int i = 0; i < key.length(); i++) {
 			char c = key.charAt(i);
 			char sanitized = c;
 
 			if (c >= 'a' && c <= 'z') {
 				// OK
 			} else if (c >= 'A' && c <= 'Z') {
 				if (convertToLowerCase) {
 					sanitized = (char) (c - 'A' + 'a');
 				}
 				// OK
 			} else if (c >= '0' && c <= '9') {
 				// OK
 			} else {
 				switch (c) {
 				case '.':
 				case '-':
 					break;
 
 				default:
 					throw new IllegalArgumentException();
 				}
 			}
 
 			if (sanitized != c) {
 				if (sanitizedChars == null) {
 					// Faster than doing char at a time, maybe?
 					sanitizedChars = key.toCharArray();
 				}
 			}
 
 			if (sanitizedChars != null) {
 				sanitizedChars[i] = sanitized;
 			}
 		}
 
 		if (sanitizedChars != null) {
 			return new String(sanitizedChars);
 		} else {
 			return key;
 		}
 	}
 
 	private static String toLowerCaseFileName(String host) {
 		for (int i = 0; i < host.length(); i++) {
 			char c = host.charAt(i);
 			if (c >= 'a' && c <= 'z') {
 				continue;
 			}
 			if (c >= 'A' && c <= 'Z') {
 				continue;
 			}
 			if (c >= '0' && c <= '9') {
 				continue;
 			}
 			switch (c) {
 			case '.':
 			case '-':
 				break;
 
 			default:
 				throw new IllegalArgumentException();
 			}
 		}
 
 		return host;
 	}
 
 	static final Object CONFIGURE_LOCK = new Object();
 
 	@Override
 	public void refresh() {
 		synchronized (CONFIGURE_LOCK) {
 			// TODO: Is this breaking LRU??
 
 			try {
 				log.info("Starting filesystem refresh");
 
 				Collection<String> keys = getKeysSnapshot();
 
 				for (String key : keys) {
 					T config = cache.getIfPresent(key);
 					if (config == null) {
 						log.info("Key concurrently removed: " + key);
 						continue;
 					}
 
					File file = toFile(key);
 					if (!file.exists()) {
 						if (!config.isPresent()) {
 							log.debug("Up-to-date (not present) on: " + key);
 							continue;
 						} else {
 							log.info("Removed from filesystem: " + key);
 							cache.refresh(key);
 							continue;
 						}
 					}
 
 					String versionKey = toVersion(file);
 					if (!Objects.equal(versionKey, config.getVersion())) {
 						log.info("Out of date: " + key);
 						cache.refresh(key);
 						continue;
 					}
 
 					log.debug("Up-to-date on: " + key);
 				}
 			} catch (Throwable t) {
 				log.warn("Error on background task", t);
 			}
 		}
 	}
 
 }
