 package com.thimbleware.jmemcached.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.*;
 import java.util.concurrent.Future;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import net.spy.memcached.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 /**
  * Integration test using Spymemcached 2.3
  * TODO flush
  * TODO shutdown
  * TODO delete
  * TODO more tests
  */
 @RunWith(Parameterized.class)
 public class SpyMemcachedIntegrationTest extends AbstractCacheTest {
 
 
     private MemcachedClient _client;
     private InetSocketAddress address;
 
     protected static final String KEY = "MyKey";
 
     protected static final int TWO_WEEKS = 1209600; // 60*60*24*14 = 1209600 seconds in 2 weeks.
 
     public SpyMemcachedIntegrationTest(CacheType cacheType, int blockSize, ProtocolMode protocolMode) {
         super(cacheType, blockSize, protocolMode);
     }
 
     @Before
     public void setUp() throws Exception {
         super.setup();
 
         this.address = new InetSocketAddress("localhost", getPort());
         if (getProtocolMode() == ProtocolMode.BINARY)
             _client = new MemcachedClient( new BinaryConnectionFactory(), Arrays.asList( address ) );
         else
             _client = new MemcachedClient( Arrays.asList( address ) );
     }
 
     @After
     public void tearDown() throws Exception {
         if (_client != null)
             _client.shutdown();
     }
 
 
     @Test
     public void testGetSet() throws IOException, InterruptedException, ExecutionException {
         Future<Boolean> future = _client.set("foo", 5000, "bar");
         assertTrue(future.get());
         assertEquals( "bar", _client.get( "foo" ) );
     }
 
     @Test
     public void testIncrDecr() throws ExecutionException, InterruptedException {
         Future<Boolean> future = _client.set("foo", 0, "1");
         assertTrue(future.get());
         assertEquals( "1", _client.get( "foo" ) );
         _client.incr( "foo", 5 );
         assertEquals( "6", _client.get( "foo" ) );
         _client.decr( "foo", 10 );
         assertEquals( "0", _client.get( "foo" ) );
     }
 
     @Test
     public void testPresence() {
         assertEquals("initial cache is empty", 0, getDaemon().getCache().getCurrentItems());
         assertEquals("initialize size is empty", 0, getDaemon().getCache().getCurrentBytes());
     }
 
     @Test
     public void testStats() throws ExecutionException, InterruptedException {
         Map<SocketAddress, Map<String, String>> stats = _client.getStats();
         Map<String, String> statsMap = stats.get(address);
         assertNotNull(statsMap);
         assertEquals("0", statsMap.get("cmd_gets"));
         assertEquals("0", statsMap.get("cmd_sets"));
         Future<Boolean> future = _client.set("foo", 86400, "bar");
         assertTrue(future.get());
         _client.get("foo");
         _client.get("bug");
         stats = _client.getStats();
         statsMap = stats.get(address);
         assertEquals("2", statsMap.get("cmd_gets"));
         assertEquals("1", statsMap.get("cmd_sets"));
     }
 
     @Test
     public void testBinaryCompressed() throws ExecutionException, InterruptedException {
         Future<Boolean> future = _client.add("foo", 86400, "foobarshoe");
         assertEquals(true, future.get());
         assertEquals("wrong value returned from cache", "foobarshoe",  _client.get("foo"));
         StringBuilder sb = new StringBuilder();
         sb.append("hello world");
         for(int i=0; i<15; i++){
             sb.append(sb);
         }
         _client.add("sb", 86400, sb.toString());
         assertNotNull("null get when sb.length()="+sb.length(), _client.get("sb"));
         assertEquals("wrong length for sb",sb.length(), _client.get("sb").toString().length());
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void testBigBinaryObject() throws ExecutionException, InterruptedException {
         Object bigObject = getBigObject();
         Future<Boolean> future = _client.set(KEY, TWO_WEEKS, bigObject);
         assertTrue(future.get());
         final Map<String, Double> map = (Map<String, Double>)_client.get(KEY);
         for (String key : map.keySet()) {
             Integer kint = Integer.valueOf(key);
             Double val = map.get(key);
             assertEquals(val, kint/42.0, 0.0);
         }
     }
 
     @Test
     public void testCAS() throws Exception {
         Future<Boolean> future = _client.set("foo", 32000, 123);
         assertTrue(future.get());
         CASValue<Object> casValue = _client.gets("foo");
         assertEquals( 123, casValue.getValue());
 
         CASResponse cr = _client.cas("foo", casValue.getCas(), 456);
 
         assertEquals(CASResponse.OK, cr);
 
         Future<Object> rf = _client.asyncGet("foo");
 
         assertEquals(456, rf.get());
     }
 
     @Test
    public void testCASAfterAdd() throws Exception {
        Future<Boolean> future = _client.add("foo", 32000, 123);
        assertTrue(future.get());
        CASValue<Object> casValue = _client.gets("foo"); // should not produce an error
        assertEquals( 123, casValue.getValue());
    }

    @Test
     public void testAppendPrepend() throws Exception {
         Future<Boolean> future = _client.set("foo", 0, "foo");
         assertTrue(future.get());
 
         _client.append(0, "foo", "bar");
         assertEquals( "foobar", _client.get( "foo" ));
         _client.prepend(0, "foo", "baz");
         assertEquals( "bazfoobar", _client.get( "foo" ));
     }
 
     @Test
     public void testBulkGet() throws IOException, InterruptedException, ExecutionException, TimeoutException {
         ArrayList<String> allStrings = new ArrayList<String>();
         ArrayList<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
         for (int i = 0; i < 500; i++) {
             futures.add(_client.set("foo" + i, 360000, "bar" + i));
             allStrings.add("foo" + i);
         }
 
         // wait for all the sets to complete
         for (Future<Boolean> future : futures) {
             assertTrue(future.get(5, TimeUnit.SECONDS));
         }
 
         // doing a regular get, we are just too slow for spymemcached's tolerances... for now
         Future<Map<String, Object>> future = _client.asyncGetBulk(allStrings);
         Map<String, Object> results = future.get();
 
         for (int i = 0; i < 500; i++) {
             assertEquals("bar" + i, results.get("foo" + i));
         }
 
     }
 
     protected static Object getBigObject() {
         final Map<String, Double> map = new HashMap<String, Double>();
 
         for (int i=0;i<13000;i++) {
             map.put(Integer.toString(i), i/42.0);
         }
 
         return map;
     }
 
 
 }
