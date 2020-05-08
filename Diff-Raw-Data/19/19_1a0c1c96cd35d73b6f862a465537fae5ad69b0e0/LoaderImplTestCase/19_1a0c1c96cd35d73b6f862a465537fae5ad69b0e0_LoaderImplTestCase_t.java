 package org.xbrlapi.loader.tests;
 
 import java.net.URI;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.xbrlapi.Fragment;
 import org.xbrlapi.data.dom.tests.BaseTestCase;
 import org.xbrlapi.loader.Loader;
 import org.xbrlapi.loader.LoaderImpl;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * Test the loader implementation.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 public class LoaderImplTestCase extends BaseTestCase {
 	
 	private final String STARTING_POINT = "test.data.small.schema";
 	private final String STARTING_POINT_2 = "test.data.small.instance";
 	private URI uri1 = null;
 	private URI uri2 = null;
 	private List<URI> uris = new LinkedList<URI>();
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		uri1 = new URI(getURI(this.STARTING_POINT));
 		uri2 = new URI(getURI(this.STARTING_POINT_2));
 		uris.add(uri1);
 		uris.add(uri2);
 /*		loader.discover(this.getURI(STARTING_POINT));		
 		loader.discover(this.getURI(STARTING_POINT_2));		
 */	}
 	
 	public LoaderImplTestCase(String arg0) {
 		super(arg0);
 	}
 
 	/**
 	 * Test the ability to create a loader given a 
 	 * null data store and a XLink Processor.
 	 * Class under test for void LoaderImpl(Store, XLinkProcessor)
 	 */
 	public void testLoaderImplStoreXLinkProcessor_FailOnNullStore() throws Exception {
 		try {
 			new LoaderImpl(null,loader.getXlinkProcessor());
 			fail("Null data stores are not allowed for loaders.");
 		} catch (XBRLException expected) {
 			;
 		}
 	}
 
 	/**
 	 * Test the ability to create a loader given a 
 	 * data store and a null XLink Processor.
 	 * Class under test for void LoaderImpl(Store, XLinkProcessor)
 	 */
 	public void testLoaderImplStoreXLinkProcessor_FailOnNullXLinkProcessor() {
 		try {
 			new LoaderImpl(store,null);
 			fail("Null XLink processors are not allowed for loaders.");
 		} catch (XBRLException expected) {
 			;
 		}
 	}
 
 	/**
 	 * Tests the creation of loaders with empty and populated lists
 	 * Class under test for void LoaderImpl(Store, XLinkProcessor, List).
 	 */
 	public void testLoaderImplStoreXLinkProcessorList() {
 		try {
 			new LoaderImpl(store,loader.getXlinkProcessor(),uris);
 			new LoaderImpl(store,loader.getXlinkProcessor(),new LinkedList<URI>());
 		} catch (XBRLException e) {
 			fail("Unexpected exception creating a loader using a list of URIs.");
 		}
 	}
 
 	public void testLoaderImplStoreXLinkProcessorList_FailOnNullList() {
 		try {
 			List<URI> list = null;
 			new LoaderImpl(store,loader.getXlinkProcessor(),list);
 			fail("Null list is not allowed for loaders.");
 		} catch (XBRLException expected) {
 			;
 		}
 	}
 	
 	/**
 	 * Test the ability to get the store being used by the loader.
 	 *
 	 */
 	public void testGetStore() {
 		try {
 			Loader l = new LoaderImpl(store,loader.getXlinkProcessor());
 			assertEquals(store,l.getStore());
 		} catch (XBRLException e) {
 			fail(e.getMessage());
 		}
 	}
 
 	/**
 	 * Test the ability to get the Xlink processor
 	 * used by the loader.
 	 */
 	public void testGetXlinkProcessor() {
 		try {
 			Loader l = new LoaderImpl(store,loader.getXlinkProcessor());
 			assertEquals(loader.getXlinkProcessor(),l.getXlinkProcessor());
 		} catch (XBRLException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	/**
 	 * Test discovery given an array of URI starting points
 	 */
 	public void testDiscover() {
 		try {
 			loader.stashURI(uri1);
 			loader.discover();
 		} catch (XBRLException e) {
 			fail("Unexpected " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Test discovery given an XBRL instance as a starting point.
 	 */
 	public void testInstanceDiscover() {
 		try {
 			loader.stashURI(uri1);
 			loader.stashURI(uri2);
 			loader.discover();
 			
             List<String> uris = store.getStoredURIs();
             assertTrue(uris.size() > 0);
             
            boolean found1 = false;
            boolean found2 = false;
             for (String uri: uris) {
                 if (uri.equals(uri1.toString())) {
                    found1 = true;
                }
                if (uri.equals(uri2.toString())) {
                    found2 = true;
                 }
             }
            assertTrue(found1 && found2);
 			
 		} catch (XBRLException e) {
 		    e.printStackTrace();
 			fail("Unexpected " + e.getMessage());
 		}
 	}
 	
 	/**
 	 * Test discovery of a list of URIs passed in
 	 * as arguments to the discover method.
 	 */
 	public void testDiscoverURIList() {
 		try {
 			loader.discover(uris);
 		} catch (XBRLException e) {
 			fail(e.getMessage());
 		}		
 	}
 
 	/**
 	 * Test discovery given an array of URI starting points
 	 */
 	public void testProcessSchemaLocationAttributes() {
 		try {
 			loader.setSchemaLocationAttributeUsage(true);
 			loader.discover(uris);
 		} catch (XBRLException e) {
 			fail("Unexpected " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Test discovery one URI at a time
 	 */
 	public void testDiscoverNext() {
 		try {
 			loader.stashURI(uri1);
 			loader.stashURI(uri2);
 			while (! loader.getDocumentsStillToAnalyse().isEmpty()) {
 				for (String document: loader.getDocumentsStillToAnalyse()) {
 					logger.debug("still to process " + document);
 				}
 				loader.discoverNext();
 			}
 		} catch (XBRLException e) {
 			fail("Unexpected " + e.getMessage());
 		}
 	}
 	
 	
 	/** 
 	 * Test null return from a call to get a fragment from a loader
 	 * where no data has been loaded.
 	 */
 	public void testGetFragmentWhenNoFragmentsAreAvailable() {
 		try {
 			Fragment f = loader.getFragment();
             fail("Exception expected.");
             store.serialize(f);
 		} catch (XBRLException e) {
 		    ;// An exception should have been thrown.
 		}
 	}
 
 
 
 
 
 }
