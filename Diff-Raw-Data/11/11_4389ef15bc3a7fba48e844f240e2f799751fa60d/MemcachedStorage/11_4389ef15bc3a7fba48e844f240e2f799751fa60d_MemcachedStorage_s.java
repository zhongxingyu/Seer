 package git.volkov.kvstorage.mongo;
 
 import java.io.IOException;
 import java.util.concurrent.TimeoutException;
 
 import net.rubyeye.xmemcached.MemcachedClient;
 import net.rubyeye.xmemcached.MemcachedClientBuilder;
 import net.rubyeye.xmemcached.XMemcachedClientBuilder;
 import net.rubyeye.xmemcached.exception.MemcachedException;
 import net.rubyeye.xmemcached.utils.AddrUtil;
 import git.volkov.kvstorage.Storage;
 
 /**
  * Storage interface for memcached database.
  * 
  * @author Sergey Volkov
  * 
  */
 public class MemcachedStorage implements Storage {
 
 	/**
 	 * Host with memcached database.
 	 */
 	private String host;
 
 	/**
 	 * Memcached client.
 	 */
 	private MemcachedClient client;
 
 	@Override
 	public void init() throws Exception {
 		MemcachedClientBuilder memcachedClientBuilder = new XMemcachedClientBuilder(
 				AddrUtil.getAddresses(host));
 		client = memcachedClientBuilder.build();
 	}
 
 	@Override
 	public void put(String key) throws Exception {
		client.set(key, 0, "true");
 	}
 
 	@Override
 	public boolean has(String key) throws Exception {
		return (client.get(key) != null);
 	}
 
 	@Override
 	public void clean() throws Exception {
 		client.shutdown();
 	}
 
 	/**
 	 * @param host
 	 *            the host to set
 	 */
 	public void setHost(String host) {
 		this.host = host;
 	}
 
 	/**
 	 * @return the host
 	 */
 	public String getHost() {
 		return host;
 	}

 }
