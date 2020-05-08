 package org.hackystat.utilities.uricache;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 
 import java.util.GregorianCalendar;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 
 import org.hackystat.utilities.logger.OneLineFormatter;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Tests the UriCache class.
  * 
  * @author <a href="mailto:seninp@gmail.com">Pavel Senin<a>
  * 
  */
 public class TestUriCache {
 
   /** The cache itself */
   private UriCache<String, String> testCache;
 
   /** The default properties to use. */
   private UriCacheProperties prop;
 
   /** The formatter to use for formatting exceptions */
   private static OneLineFormatter formatter = new OneLineFormatter();
 
   /**
    * Instantiates default cache properties and creates objects to be cached.
    * 
    * @throws Exception if error encountered.
    */
   @Before
   public void setUp() throws Exception {
     prop = new UriCacheProperties();
     prop.setCacheStoragePath("sandbox/cache");
   }
 
   /**
    * Clears the cache and nulls it after.
    * 
    * @throws UriCacheException if error encountered.
    */
   @After
   public void tearDown() throws UriCacheException {
     testCache.shutdown();
   }
 
   /**
    * Cache exception test.
    */
   @Test
   public void testCacheException() {
     try {
       //
       // get cache instance and dump some data.
       //
       testCache = new UriCache<String, String>("testCache", prop);
       testCache.clear();
       int cnt = 10000;
       for (int i = 0; i < cnt; i++) {
         testCache.cache("key:" + i, "data:" + i);
       }
       //
       // now try to another cache with the same properties.
       // Should not be able to do this.
       //
       @SuppressWarnings("unused")
       UriCache<String, String> testCache2 = new UriCache<String, String>("testCache", prop);
      testCache2.clear();
       fail("Able to get the cache instance with the same name.");
     }
     catch (UriCacheException e) {
       assert true;
     }
   }
 
   /**
    * Cache persistence test.
    */
   @Test
   public void testCachePersistence() {
     try {
       //
       // get cache instance, clear any leftovers and load cache with new data
       //
       testCache = new UriCache<String, String>("testOptimizerCache", prop);
       testCache.clear();
       int cnt = 10000;
       for (int i = 0; i < cnt; i++) {
         testCache.cache("key:" + i, "data:" + i);
       }
       //
       // now, shut it down
       //
       testCache.shutdown();
       //
       // chill a little
       //
       // System.err.println("UriCache test: testing persistence. Sleeping for 10 seconds.");
       Thread.yield();
       Thread.sleep(1000);
       Thread.yield();
       //
       // get cache back alive
       //
       testCache = new UriCache<String, String>("testOptimizerCache", prop);
       //
       // should be ABLE to read data back
       //
       for (int i = 0; i < cnt; i++) {
         String element = (String) testCache.lookup("key:" + i);
         assertNotNull("Should have recevied an element. " + i, element);
         assertEquals("Element is wrong.", "data:" + i, element);
       }
     }
     catch (UriCacheException e) {
       fail("Unable to create cache instance: "
           + formatter.format(new LogRecord(Level.ALL, e.toString())));
     }
     catch (InterruptedException e) {
       fail("Unable to sleep >:-!```: " + formatter.format(new LogRecord(Level.ALL, e.toString())));
     }
 
   }
 
   /**
    * Cache shrinker test #1, test shrinking of particular elements.
    */
   @Test
   public void testCacheOptimizer1() {
     try {
       //
       // a little set up.
       //
       DatatypeFactory factory = null;
       factory = DatatypeFactory.newInstance();
 
       //
       // get cache instance, clear any leftovers and load cache with new data
       //
       testCache = new UriCache<String, String>("testOptimizerCache", prop);
       testCache.clear();
       int cnt = 5000;
       for (int i = 0; i < cnt; i++) {
         testCache.cache("key:" + i, "data:" + i);
       }
       // put "hot" items in cache now
       GregorianCalendar calendar = new GregorianCalendar();
       calendar.setTimeInMillis(System.currentTimeMillis() + 500);
       for (int i = cnt; i < cnt * 2; i++) {
         testCache.cache("key:" + i, "data:" + i, factory.newXMLGregorianCalendar(calendar));
       }
       //
       // chill a little, need time to close cache file.
       //
       // System.out.println("UriCache test: testing shrinker #1. Sleeping for 10 seconds.");
       Thread.yield();
       Thread.sleep(1000);
       Thread.yield();
       //
       // should be unable to read first data block back
       //
       for (int i = 0; i < cnt; i++) {
         String element = (String) testCache.lookup("key:" + i);
         assertNotNull("Should have recevied an element. " + i, element);
         assertEquals("Element is wrong.", "data:" + i, element);
       }
       //
       // and should be unable to read second data block back
       //
       for (int i = cnt; i < cnt * 2; i++) {
         testCache.remove("key:" + i);
         assertNull("Should have NOT recevied an element. " + i, testCache.lookup("key:" + i));
       }
     }
     catch (UriCacheException e) {
       fail("Unable to create cache instance: "
           + formatter.format(new LogRecord(Level.ALL, e.toString())));
     }
     catch (InterruptedException e) {
       fail("Unable to sleep >:-!```: " + formatter.format(new LogRecord(Level.ALL, e.toString())));
     }
     catch (DatatypeConfigurationException e) {
       fail("Unable to get DataFactory instance: "
           + formatter.format(new LogRecord(Level.ALL, e.toString())));
     }
 
   }
 
   /**
    * Cache shrinker test #2, tests default autoexpiration.
    */
   @Test
   public void testCacheOptimizer2() {
     try {
       //
       // get cache instance, clear any leftovers and load cache with new data
       //
       testCache = new UriCache<String, String>("testOptimizerCache", prop);
       testCache.setMaxMemoryIdleTimeSeconds(5L);
       testCache.clear();
       int cnt = 10000;
       for (int i = 0; i < cnt; i++) {
         testCache.cache("key:" + i, "data:" + i);
       }
       //
       // chill a little, need time to close cache file.
       //
       // System.out.println("UriCache test: testing shrinker #2. Sleeping for 10 seconds.");
       Thread.yield();
       Thread.sleep(1000);
       Thread.yield();
       //
       // should be unable to read data back
       //
       for (int i = 0; i < cnt; i++) {
         testCache.remove("key:" + i);
         assertNull("Should have NOT recevied an element. " + i, testCache.lookup("key:" + i));
       }
     }
     catch (UriCacheException e) {
       fail("Unable to create cache instance: "
           + formatter.format(new LogRecord(Level.ALL, e.toString())));
     }
     catch (InterruptedException e) {
       fail("Unable to sleep >:-!```: " + formatter.format(new LogRecord(Level.ALL, e.toString())));
     }
 
   }
 
   /**
    * Tests cache instantiation and simple cache, remove and clean routines.
    */
   @Test
   public void testCacheInstance() {
     try {
       //
       // get cache instance and clear any leftovers
       //
       testCache = new UriCache<String, String>("testCache", prop);
       testCache.clear();
       //
       // load cache with new data
       //
       int cnt = 10000;
       for (int i = 0; i < cnt; i++) {
         testCache.cache("key:" + i, "data:" + i);
       }
       //
       // read data back
       //
       for (int i = 0; i < cnt; i++) {
         String element = (String) testCache.lookup("key:" + i);
         assertNotNull("Should have recevied an element. " + i, element);
         assertEquals("Element is wrong.", "data:" + i, element);
       }
       //
       // clean cache one by one and check removal
       //
       for (int i = 0; i < cnt; i++) {
         testCache.remove("key:" + i);
         assertNull("Should have NOT recevied an element. " + i, testCache.lookup("key:" + i));
       }
       //
       // load cache again
       //
       for (int i = 0; i < cnt; i++) {
         testCache.cache("key:" + i, "data:" + i);
       }
       //
       // clean it
       //
       testCache.clear();
       //
       // check if it clean
       //
       for (int i = 0; i < cnt; i++) {
         assertNull("Should have NOT recevied an element. " + i, testCache.lookup("key:" + i));
       }
     }
     catch (UriCacheException e) {
       fail("Unable to create cache instance: "
           + formatter.format(new LogRecord(Level.ALL, e.toString())));
     }
 
   }
 }
