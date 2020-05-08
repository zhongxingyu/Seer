 package org.xbrlapi.data.bdbxml.tests;
 
 import java.util.List;
 
 import org.w3c.dom.Element;
 import org.xbrlapi.utilities.XBRLException;
 /**
  * Test the BDB XML XBRLAPI Store implementation of DOM recovery methods.
  * @author Geoffrey Shuetrim (geoff@galexy.net) 
 */
 public class DocumentRecoveryFromStoreTestCase extends BaseTestCase {
 	private final String STARTING_POINT = "test.data.small.schema";
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		loader.discover(this.getURL(STARTING_POINT));		
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public DocumentRecoveryFromStoreTestCase(String arg0) {
 		super(arg0);
 	}
 	
 	/**
 	 * Test the retrieval of a list of URLs
 	 * and test getting a document back as a DOM object.
 	 */
 	public void testGettingURLList() {
 		try {
 			List<String> urls = store.getStoredURLs();
			assertTrue(urls.size() > 10);
 			
 			Element e = store.getDocumentAsDOM(urls.get(0));
 			assertNotNull(e);
 			
 		} catch (XBRLException e) {
 			e.printStackTrace();
 			fail("Unexpected exception thrown.");
 		}
 	}
 	
 	/**
 	 * Test whether the full store can be recovered as a DOM.
 	 */
 	public void testGetDOM() {
 		try {
 			store.getStoreAsDOM();
 		} catch (XBRLException e) {
 			fail("Unexpected " + e.getMessage());
 		}
 	}
 	
 	/**
 	 * Test recovery of a single document from a store.
 	 */
 	public void testGetDocument() {
 		try {
 			Element root = store.getDocumentAsDOM(this.getURL(STARTING_POINT));
 			assertNotNull(root);
 			assertEquals(root.getLocalName(),"schema");
 		} catch (XBRLException e) {
 			fail("Unexpected " + e.getMessage());
 		}
 	}
 
 }
