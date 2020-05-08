 package de.th.wildau.dsc.sne.webserver;
 
 import java.io.File;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 /**
  * HttpCache (Singleton).
  * 
  * @author David Schwertfeger [dsc] & Sebastian Nemak [sne]
  */
 public class HttpCache {
 
 	private static HttpCache INSTANCE;
 
 	private final int cacheSize;
 	private Queue<String> queue;
 	private Map<String, byte[]> cache;
 
 	/**
 	 * Hidden constructor. HttpCache is a Singleton.
 	 * 
 	 * @param cacheSize
 	 *            element size
 	 * @param cacheTime
 	 *            in seconds
 	 */
 	private HttpCache(int cacheSize) {
 
 		this.cacheSize = cacheSize;
 		this.queue = new ConcurrentLinkedQueue<String>();
 		this.cache = new ConcurrentHashMap<String, byte[]>();
 	}
 
 	/**
 	 * Use default constructor. Caches 25 elements.
 	 */
 	private HttpCache() {
 
 		this(25);
 	}
 
 	/**
 	 * Singleton call point.
 	 * 
 	 * @return {@link HttpCache}
 	 */
 	public static synchronized final HttpCache getInstance() {
 
 		if (INSTANCE == null) {
 			INSTANCE = new HttpCache();
 		}
 		return INSTANCE;
 	}
 
 	
 	public byte[] getValue(File resource) {
 		return INSTANCE.cache.get(resource.toString());
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param resource
 	 * @param header
 	 * @param body
 	 */
 	public void put(File resource, byte[] header, byte[] body) {
 
 		if (isInterpretedFile(resource)) {
 			Log.debug("Don't cache interpreted files.");
 			return;
 		}
 
 		byte[] content = new byte[header.length + body.length];
 		int headerLength = header.length;
 
 		for (int i = 0; i < content.length; ++i) {
 			content[i] = i < headerLength ? header[i] : body[i - headerLength];
 		}
 
 		put(resource, content);
 	}
 
 	private boolean isInterpretedFile(File file) {
 
 		for (ScriptLanguage scriptLanguage : WebServer.supportedScriptLanguages) {
 			if (file.getName().toLowerCase()
 					.endsWith(scriptLanguage.getFileExtension())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void put(File resource, byte[] content) {
 
 		if (INSTANCE.cache.size() >= INSTANCE.cacheSize) {
 			String remove = INSTANCE.queue.poll();
 			INSTANCE.cache.remove(remove);
 		}
 		INSTANCE.queue.add(resource.getName());
 		INSTANCE.cache.put(resource.toString(), content);
 	}
 
 	public boolean contains(File resource) {
 		return INSTANCE.cache.containsKey(resource.toString());
 	}
 }
