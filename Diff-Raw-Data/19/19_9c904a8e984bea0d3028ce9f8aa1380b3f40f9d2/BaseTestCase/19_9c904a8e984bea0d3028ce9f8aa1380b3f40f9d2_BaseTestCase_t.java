 package org.xbrlapi.data.dom.tests;
 
 import java.io.File;
import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.xbrlapi.SAXHandlers.EntityResolverImpl;
 import org.xbrlapi.cache.CacheImpl;
 import org.xbrlapi.data.Store;
 import org.xbrlapi.data.XBRLStore;
 import org.xbrlapi.data.dom.StoreImpl;
 import org.xbrlapi.loader.Loader;
 import org.xbrlapi.loader.LoaderImpl;
 import org.xbrlapi.utilities.XBRLException;
 import org.xbrlapi.xlink.XLinkProcessor;
 import org.xbrlapi.xlink.XLinkProcessorImpl;
 import org.xbrlapi.xlink.handler.XBRLCustomLinkRecogniserImpl;
 import org.xbrlapi.xlink.handler.XBRLXLinkHandlerImpl;
 import org.xml.sax.EntityResolver;
 
 /**
  * Provides a base test case for tests involving the XML DOM data store.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 public abstract class BaseTestCase extends org.xbrlapi.utilities.BaseTestCase {
 
 	protected String cache = configuration.getProperty("local.cache");
 	
 	protected XBRLStore store = null;
 	protected Loader loader = null;
 
 	protected List<Store> stores = new LinkedList<Store>();
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		store = createStore();
 		stores.add(store);
 		loader = createLoader(store);
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		for (int i=0; i<stores.size(); i++) {
 			cleanup(stores.get(i));
 		}
 	}
 	
 	public BaseTestCase(String arg0) {
 		super(arg0);
 	}
 
 	/**
 	 * @return the new store.
 	 * @throws XBRLException
 	 */
 	public StoreImpl createStore() throws XBRLException {
 		return new StoreImpl();
 	}
 
 	/**
 	 * @param store The store to use for the loader.
 	 * @return the loader
 	 * @throws XBRLException
 	 */
 	public Loader createLoader(Store store) throws XBRLException {
 		XBRLXLinkHandlerImpl xlinkHandler = new XBRLXLinkHandlerImpl();
 		XBRLCustomLinkRecogniserImpl clr = new XBRLCustomLinkRecogniserImpl(); 
 		XLinkProcessor xlinkProcessor = new XLinkProcessorImpl(xlinkHandler ,clr);
 		File cacheFile = new File(cache);
		
		// Rivet errors in the SEC XBRL data require these remappings.
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("http://www.xbrl.org/2003/linkbase/xbrl-instance-2003-12-31.xsd","http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd");
        map.put("http://www.xbrl.org/2003/instance/xbrl-instance-2003-12-31.xsd","http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd");
        map.put("http://www.xbrl.org/2003/linkbase/xbrl-linkbase-2003-12-31.xsd","http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd");
        map.put("http://www.xbrl.org/2003/instance/xbrl-linkbase-2003-12-31.xsd","http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd");
        map.put("http://www.xbrl.org/2003/instance/xl-2003-12-31.xsd","http://www.xbrl.org/2003/xl-2003-12-31.xsd");
        map.put("http://www.xbrl.org/2003/linkbase/xl-2003-12-31.xsd","http://www.xbrl.org/2003/xl-2003-12-31.xsd");
        map.put("http://www.xbrl.org/2003/instance/xlink-2003-12-31.xsd","http://www.xbrl.org/2003/xlink-2003-12-31.xsd");
        map.put("http://www.xbrl.org/2003/linkbase/xlink-2003-12-31.xsd","http://www.xbrl.org/2003/xlink-2003-12-31.xsd");

        EntityResolver resolver = new EntityResolverImpl(cacheFile,map);
 		Loader myLoader = new LoaderImpl(store,xlinkProcessor);
 		myLoader.setCache(new CacheImpl(cacheFile));
		myLoader.setEntityResolver(resolver);
 		xlinkHandler.setLoader(myLoader);
 		return myLoader;
 	}
 	
 	/**
 	 * Helper method to clean up and shut down the data store.
 	 * @param store the store for the XBRL data.
 	 * @throws XBRLException if the store cannot be deleted 
 	 */
 	public void cleanup(Store store) throws XBRLException {
 		store.delete();
 	}
 
 }
