 package com.dgrid.helpers.impl;
 
 import java.util.Date;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.danga.MemCached.MemCachedClient;
 import com.danga.MemCached.SockIOPool;
 import com.dgrid.errors.TransportException;
 import com.dgrid.gen.InvalidApiKey;
 import com.dgrid.helpers.MemcacheHelper;
 import com.dgrid.service.DGridClient;
 
 public class MemcacheHelperImpl implements MemcacheHelper {
 
 	private Log log = LogFactory.getLog(getClass());
 
 	private DGridClient gridClient;
 
 	private MemCachedClient client = new MemCachedClient();
 
	public void setDGridClient(DGridClient gridClient) {
 		this.gridClient = gridClient;
 	}
 
 	public void init() throws TransportException, InvalidApiKey {
 		log.trace("init()");
 		SockIOPool pool = SockIOPool.getInstance();
 		String serverString = gridClient.getSetting("memcache.servers",
 				"localhost:11211");
 		String[] servers = serverString.split(" ");
 		boolean compat = Boolean.parseBoolean(gridClient.getSetting(
 				"memcache.compatibility", Boolean.toString(false)));
 		if (compat) {
 			pool.setHashingAlg(SockIOPool.NEW_COMPAT_HASH);
 			client.setPrimitiveAsString(true);
 			client.setSanitizeKeys(false);
 		}
 		pool.setServers(servers);
 		pool.initialize();
 	}
 
 	@Override
 	public boolean add(String key, Object value) {
 		log.trace("add()");
 		return client.add(key, value);
 	}
 
 	@Override
 	public boolean add(String key, Object value, Date expiry) {
 		log.trace("add()");
 		return client.add(key, value, expiry);
 	}
 
 	@Override
 	public boolean add(String key, Object value, Date expiry, int hashCode) {
 		log.trace("add()");
 		return client.add(key, value, expiry, hashCode);
 	}
 
 	@Override
 	public boolean add(String key, Object value, int hashCode) {
 		log.trace("add()");
 		return client.add(key, value, hashCode);
 	}
 
 	@Override
 	public long addOrDecr(String key) {
 		log.trace("addOrDecr()");
 		return client.addOrDecr(key);
 	}
 
 	@Override
 	public long addOrDecr(String key, long inc) {
 		log.trace("addOrDecr()");
 		return client.addOrDecr(key, inc);
 	}
 
 	@Override
 	public long addOrDecr(String key, long inc, int hashCode) {
 		log.trace("addOrDecr()");
 		return client.addOrDecr(key, inc, hashCode);
 	}
 
 	@Override
 	public long addOrIncr(String key) {
 		log.trace("addOrIncr()");
 		return client.addOrIncr(key);
 	}
 
 	@Override
 	public long addOrIncr(String key, long inc) {
 		log.trace("addOrIncr()");
 		return client.addOrIncr(key, inc);
 	}
 
 	@Override
 	public long addOrIncr(String key, long inc, int hashCode) {
 		log.trace("addOrIncr()");
 		return client.addOrIncr(key, inc, hashCode);
 	}
 
 	@Override
 	public boolean delete(String key) {
 		log.trace("delete()");
 		return client.delete(key);
 	}
 
 	@Override
 	public boolean delete(String key, Date expiry) {
 		log.trace("delete()");
 		return client.delete(key, expiry);
 	}
 
 	@Override
 	public boolean delete(String key, int hashCode, Date expiry) {
 		log.trace("delete()");
 		return client.delete(key, hashCode, expiry);
 	}
 
 	@Override
 	public boolean flushAll() {
 		log.trace("flushAll()");
 		return client.flushAll();
 	}
 
 	@Override
 	public Object get(String key) {
 		log.trace("get()");
 		return client.get(key);
 	}
 
 	@Override
 	public Object get(String key, int hashCode) {
 		log.trace("get()");
 		return client.get(key, hashCode);
 	}
 
 	@Override
 	public long getCounter(String key) {
 		log.trace("getCounter()");
 		return client.getCounter(key);
 	}
 
 	@Override
 	public long getCounter(String key, int hashCode) {
 		log.trace("getCounter()");
 		return client.getCounter(key, hashCode);
 	}
 
 	@Override
 	public MemCachedClient getMemCachedClient() {
 		log.trace("getMemCachedClient()");
 		return client;
 	}
 
 	@Override
 	public boolean keyExists(String key) {
 		log.trace("keyExists()");
 		return client.keyExists(key);
 	}
 
 	@Override
 	public boolean replace(String key, Object value) {
 		log.trace("replace()");
 		return client.replace(key, value);
 	}
 
 	@Override
 	public boolean replace(String key, Object value, Date expiry) {
 		log.trace("replace()");
 		return client.replace(key, value, expiry);
 	}
 
 	@Override
 	public boolean replace(String key, Object value, Date expiry, int hashCode) {
 		log.trace("replace()");
 		return client.replace(key, value, expiry, hashCode);
 	}
 
 	@Override
 	public boolean set(String key, Object value) {
 		log.trace("set()");
 		return client.set(key, value);
 	}
 
 	@Override
 	public boolean set(String key, Object value, Date expiry) {
 		log.trace("set()");
 		return client.set(key, value, expiry);
 	}
 
 	@Override
 	public boolean set(String key, Object value, Date expiry, int hashCode) {
 		log.trace("set()");
 		return client.set(key, value, expiry, hashCode);
 	}
 
 	@Override
 	public boolean set(String key, Object value, int hashCode) {
 		log.trace("set()");
 		return client.set(key, value, hashCode);
 	}
 
 	@Override
 	public Map stats() {
 		log.trace("stats()");
 		return client.stats();
 	}
 
 	@Override
 	public boolean storeCounter(String key, long counter) {
 		log.trace("storeCounter()");
 		return client.storeCounter(key, counter);
 	}
 
 	@Override
 	public boolean storeCounter(String key, long counter, int hashCode) {
 		log.trace("storeCounter()");
 		return client.storeCounter(key, counter, hashCode);
 	}
 
 }
