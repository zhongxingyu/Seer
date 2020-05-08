 package org.xbrlapi.data.bdbxml.tests;
 
 import org.xbrlapi.data.Store;
 import org.xbrlapi.loader.Loader;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * Test the ability to reconnect to a data store. 
  * @author Geoffrey Shuetrim (geoff@galexy.net)
 */
 public class AddToExistingStoreTestCase extends BaseTestCase {
 	
 	private final String STARTING_POINT = "test.data.small.schema";
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		loader.discover(this.getURL(STARTING_POINT));		
 	}
 
 	protected void tearDown() throws Exception {
 		if (secondStore != null) {
 			secondStore.close();
 			secondStore = null;
 		}
 		super.tearDown();
 	}
 
 	public AddToExistingStoreTestCase(String arg0) {
 		super(arg0);
 	}
 	
 	Store secondStore = null;
 	/**
 	 * Test reconnection to an existing store.
 	 */
 	public void testReconnectionToAnExistingStore() {
 
 		try {
 			
 			secondStore = createStore();
 			
 			Loader secondLoader = createLoader(secondStore);
 
			int count = secondStore.getStoredURLs().size();
			assertTrue(count > 10);
 			
 			secondLoader.discover(this.getURL("test.data.small.instance"));
 
			assertEquals(2,secondStore.getStoredURLs().size() - count);
 			
 			secondStore.close();
 			secondStore = null;
 			
 		} catch (XBRLException e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 
 	}
 
 }
