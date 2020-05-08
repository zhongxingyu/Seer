 package org.xbrlapi.data.exist.tests;
 
 import java.util.Set;
 
 import org.xbrlapi.Fragment;
import java.util.List;
 import org.xbrlapi.impl.MockImpl;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * Test the eXist data store implementation.
  * @author Geoffrey Shuetrim (geoff@galexy.net) 
 */
 public class StoreImplTestCase extends BaseTestCase {
 	private final String STARTING_POINT = "test.data.small.schema";
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		loader.discover(this.getURI(STARTING_POINT));		
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public StoreImplTestCase(String arg0) {
 		super(arg0);
 	}
 
 	public void testAddFragment() {
 		try {
 			String index = "1";
 			MockImpl d = new MockImpl(index);
 			store.persist(d);
			assertEquals(index,store.getXMLResource(index).getIndex());
 		} catch (XBRLException e) {
 			fail("The addition of a document fragment to the Xindice data store failed." + e.getMessage());
 		}
 	}
 
 	public void testRemoveFragmentUsingIndex() {
 		try {
 			String index = "1";
 			store.persist(new MockImpl(index));
 			assertTrue(store.hasXMLResource(index));
 			store.remove(index);
 			assertFalse(store.hasXMLResource(index));
 		} catch (XBRLException e) {
 			fail("Unexpected exception. " + e.getMessage());
 		}
 	}
 
 
 
 
 	
 	public void testQueryData() {
 		try {
 	        String xpathQuery = "/" + Constants.XBRLAPIPrefix + ":" + "fragment";
 	        List<Fragment> fragments = store.<Fragment>queryForXMLResources(xpathQuery);
 			assertTrue(fragments.size() > 1);
 	        Fragment fragment = fragments.get(0);
 	        assertEquals("fragment",fragment.getMetadataRootElement().getLocalName());
 		} catch (XBRLException e) {
 			fail(e.getMessage());
 		}
 	}
 	
     public void testQueryForIndices() {
         try {
             String xpathQuery = "#roots#";
             Set<String> indices = store.queryForIndices(xpathQuery);
             assertTrue(! indices.isEmpty());
 /*            for (String index: indices) {
                 logger.info(index);
             }
 */        } catch (Exception e) {
             e.printStackTrace();
             fail(e.getMessage());
         }
     }
 	
 	
 	
 
 	public void testQueryLoadedFragments() {
 		try {
 			loader.discover();			
 		} catch (XBRLException e) {
 			fail(e.getMessage());
 		} 
 		
 		try {
 	        String query = "#roots#[*/" + Constants.XMLSchemaPrefix + ":element]";
 	        List<Fragment> fragments = store.<Fragment>queryForXMLResources(query);
 	        Fragment fragment = fragments.get(0);
 	        assertEquals("element",fragment.getDataRootElement().getLocalName());
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	}
 
 
 
 }
