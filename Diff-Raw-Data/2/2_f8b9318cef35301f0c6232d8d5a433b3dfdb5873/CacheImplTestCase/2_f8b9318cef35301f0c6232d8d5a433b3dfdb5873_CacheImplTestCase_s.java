 package org.xbrlapi.cache.tests;
 
 /**
  * @author Geoffrey Shuetrim (geoff@galexy.net) 
  */
 
 import java.io.File;
 import java.net.URL;
 
 import org.xbrlapi.cache.CacheImpl;
 import org.xbrlapi.utilities.BaseTestCase;
 
 
 public class CacheImplTestCase extends BaseTestCase {
 
 	private String cacheRoot;
 
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		cacheRoot = configuration.getProperty("local.cache");
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	/**
 	 * @param arg0
 	 */
 	public CacheImplTestCase(String arg0) {
 		super(arg0);
 	}
 
 	/**
 	 * Test operations on a simple URL
 	 */
 	public final void testSimpleURL() {
 		try {
 			this.examineURL(new URL("http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd"));
 		} catch (Exception e) {
 			fail("Unexpected exception. " + e.getMessage());
 		}
 	}
 	
 	
 
 	public final void examineURL(URL originalURL) {
 		try {
 
 			CacheImpl cache = new CacheImpl(new File(cacheRoot));
 			assertFalse(cache.isCacheURL(originalURL));
 			File cacheFile = cache.getCacheFile(originalURL);
 			URL cacheURL = cache.getCacheURL(originalURL);
 			assertTrue(cache.isCacheURL(cacheURL));
 			URL newURL = cache.getOriginalURL(cacheURL);
			assertEquals(originalURL.toString(), newURL.toString());
 			
 		} catch (Exception e) {
 		    e.printStackTrace();
 			fail("Unexpected exception was thrown. " + e.getMessage());
 		}
 	}	
 	
 }
