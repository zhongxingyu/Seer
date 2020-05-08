 package com.thimbleware.jmemcached.test;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 
 import com.thimbleware.jmemcached.*;
 import net.spy.memcached.MemcachedClient;
 
 import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap;
 import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap.EvictionPolicy;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class Issue24RegressionTest {
 
     private MemCacheDaemon<?> _daemon;
     private MemcachedClient _client;
 
     @Before
     public void setUp() throws Throwable {
         final InetSocketAddress address = new InetSocketAddress( "localhost", AvailablePortFinder.getNextAvailable() );
         _daemon = createDaemon( address );
         _daemon.start();
 
         _client = new MemcachedClient( address );
     }
 
     @After
     public void tearDown() throws Exception {
         _client.shutdown();
         _daemon.stop();
     }
 
     public static MemCacheDaemon<? extends CacheElement> createDaemon( final InetSocketAddress address ) throws IOException {
         final MemCacheDaemon<LocalCacheElement> daemon = new MemCacheDaemon<LocalCacheElement>();
         final ConcurrentLinkedHashMap<Key, LocalCacheElement> cacheStorage = ConcurrentLinkedHashMap.create(
                 EvictionPolicy.LRU, 100000, 1024*1024 );
         daemon.setCache( new CacheImpl( cacheStorage ) );
         daemon.setAddr( address );
         daemon.setVerbose( false );
         return daemon;
     }
 
     @Test
     public void testGetsAfterAdd() {
         _client.add( "foo-n1", 5, "baz-n1" ); // fails
         // _client.set( "foo-n1", 5, "bar" ); // would be fine
        _client.gets( "foo-n1" );
     }
 
 }
