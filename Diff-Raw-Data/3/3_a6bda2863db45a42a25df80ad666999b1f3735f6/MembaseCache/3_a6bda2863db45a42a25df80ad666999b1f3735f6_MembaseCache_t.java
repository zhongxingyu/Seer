 package railo.extension.io.cache.membase;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import net.spy.memcached.AddrUtil;
 import net.spy.memcached.ConnectionFactoryBuilder;
 import net.spy.memcached.MemcachedClient;
 
 import railo.commons.io.cache.Cache;
 import railo.commons.io.cache.CacheEntry;
 import railo.commons.io.cache.CacheEntryFilter;
 import railo.commons.io.cache.CacheKeyFilter;
 import railo.extension.util.Functions;
 import railo.loader.engine.CFMLEngine;
 import railo.loader.engine.CFMLEngineFactory;
 import railo.runtime.config.Config;
 import railo.runtime.exp.PageException;
 import railo.runtime.type.Struct;
 import railo.runtime.util.Cast;
 
 public class MembaseCache implements Cache {
 	
 	private Functions func = new Functions();
 	private MemcachedClient mc;
 	private List<InetSocketAddress> addrs;
 	private String cacheName;
 	private String host;
 	private Logger log = Logger.getLogger(MemcachedClient.class);
 	
 	@Override
 	public void init(String cacheName, Struct arguments) throws IOException {
 		this.cacheName = cacheName;
 		CFMLEngine engine = CFMLEngineFactory.getInstance();
 		Cast caster = engine.getCastUtil();
 		
 		log.setLevel(Level.INFO);
 		
 	    try {
 	    	this.host = caster.toString(arguments.get("host"));   
 	    	this.addrs = AddrUtil.getAddresses(this.host);
         	this.mc = new MemcachedClient(new ConnectionFactoryBuilder().setProtocol(ConnectionFactoryBuilder.Protocol.TEXT).build(),this.addrs);                        
         	
         	log.info("Cache id : " + cacheName + " initilized using the Membase Driver");
         	
 	    } catch (Exception e) {
             e.printStackTrace();
         }		
 
 	}
 	
 	public void init(Config config ,String[] cacheName,Struct[] arguments){
 		//Not used at the moment
 	}
 	
 	public List<InetSocketAddress> getAddresses(){
 		return this.addrs;
 	}
 
 	public MemcachedClient getMC(){
 		return this.mc;
 	}
 
 	@Override
 	public boolean contains(String key) {
 		try{
 			getCacheEntry(key);
 			return true;
 		}
 		catch(IOException e){
 			return false;
 		}	
 	}
 
 	@Override
 	public List entries() {
 		try{
 			throw(new IOException("The method [entries] is not supported in Membase Cache"));			
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public List entries(CacheKeyFilter arg0) {
 		try{
 			throw(new IOException("The method [entries] is not supported in Membase Cache"));			
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public List entries(CacheEntryFilter arg0) {
 		try{
 			throw(new IOException("The method [entries] is not supported in Membase Cache"));			
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public CacheEntry getCacheEntry(String key) throws IOException {
 		
 		Object obj=null;
 		
 		//try to get the key for max 5 sec
 		Future<Object> f = this.mc.asyncGet(key.toLowerCase());
 			try{		    
 				obj = f.get(5, TimeUnit.SECONDS);
 				if(obj == null){
 					throw(new IOException("Key [" + key + "] was not found"));
 				}
 				obj = func.evaluate(obj);
 				MembaseCacheEntry entry = new MembaseCacheEntry(new MembaseCacheItem(this,key,obj));
 				return entry;
 
 				}catch(PageException e){
 					f.cancel(false);
 					throw(new IOException(e.getMessage()));
 				}catch(TimeoutException e) {
 					f.cancel(false);
 					throw(new IOException(e.getMessage()));
 				}catch(InterruptedException e){
 					f.cancel(false);
 					throw(new IOException(e.getMessage()));
 				}catch(ExecutionException e){
 					f.cancel(false);
 					throw(new IOException(e.getMessage()));
 			}
 				
 	}
 
 	@Override
 	public CacheEntry getCacheEntry(String key, CacheEntry filter) {
 		try{
 			throw(new IOException("The method [getCacheEntry(String,CacheEntry)] is not supported in Membase Cache"));			
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public Struct getCustomInfo() {
 		CFMLEngine engine = CFMLEngineFactory.getInstance();
 		Cast caster = engine.getCastUtil();
 		Struct res = null;		
 		try{
 			res = caster.toStruct(this.mc.getStats().get(this.addrs.get(0)));	
 		}catch(PageException e){
 			e.printStackTrace();
 		}
 		return res;
 	}
 
 	@Override
 	public Object getValue(String key) throws IOException {
 		try{
 			CacheEntry entry = getCacheEntry(key);
 			Object result = entry.getValue();
 			Map stats = this.mc.getStats();
 			return result;
 		}catch(IOException e){
            e.printStackTrace();
            return null;
 		}
 		
 	}
 
 	@Override
 	public Object getValue(String key, Object defaultValue) {
 		try{
 			Object result = getValue(key);
 			if(result == null){
 				return defaultValue;
 			}
 			return result;
 		}catch(IOException e){
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	@Override
 	public long hitCount() {
 		Map<SocketAddress,Map<String,String>> stats = this.mc.getStats();
 		SocketAddress add = this.addrs.get(0);
 	    long hits = Long.parseLong(stats.get(add).get("get_hits"));
 		return hits;
 	}
 
 	@Override
 	public List keys() {
 		try{
 			throw(new IOException("The method [keys] is not supported in Membase Cache"));			
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public List keys(CacheKeyFilter arg0) {
 		try{
 			throw(new IOException("The method [keys] is not supported in Membase Cache"));			
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public List keys(CacheEntryFilter arg0) {
 		try{
 			throw(new IOException("The method [keys] is not supported in Membase Cache"));			
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public long missCount() {
 		Map<SocketAddress,Map<String,String>> stats = this.mc.getStats();
 		SocketAddress add = this.addrs.get(0);
 	    long misses = Long.parseLong(stats.get(add).get("get_misses"));
 		return misses;
 	}
 
 	@Override
 	public void put(String key, Object value, Long idleTime, Long lifeSpan) {
 		Object obj = null;
 		long span = lifeSpan==null?0:lifeSpan;
 		int seconds = (int)TimeUnit.MILLISECONDS.toSeconds(span);
 		
 		try{
 			obj = func.serialize(value);			
 		}
 		catch(PageException e){
 			e.printStackTrace();
 		}
 		this.mc.set(key.toLowerCase(),seconds,obj);
 	}
 
 	@Override
 	public boolean remove(String key) {
 		this.mc.delete(key.toLowerCase());
 		return false;
 	}
 
 	@Override
 	public int remove(CacheKeyFilter filter) {
 		
 		// Does not really test nothing here. Just flush the whole cache.
 		// Memcached does not yes support the keys listing so is not possible to iterate
 		
 		this.mc.flush();
 		
 		return 0;
 	}
 
 	@Override
 	public int remove(CacheEntryFilter arg0) {
 
 		// Does not really test nothing here. Just flush the whole cache.
 		// Memcached does not yes support the keys listing so is not possible to iterate
 		
 		this.mc.flush();
 		
 		return 0;
 	}
 
 	@Override
 	public List values() {
 		try{
 			throw(new IOException("The method [values] is not supported in Membase Cache"));			
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public List values(CacheKeyFilter arg0) {
 		try{
 			throw(new IOException("The method [values] is not supported in Membase Cache"));			
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public List values(CacheEntryFilter arg0) {
 		try{
 			throw(new IOException("The method [values] is not supported in Membase Cache"));			
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 }
