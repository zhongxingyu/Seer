 package org.xbrlapi.grabber.tests;
 
 import java.net.URL;
 import java.util.List;
 
 import org.xbrlapi.data.dom.tests.BaseTestCase;
 import org.xbrlapi.grabber.Grabber;
 import org.xbrlapi.grabber.SecGrabberImpl;
 
 public class SecGrabberImplTest extends BaseTestCase {
 
     public SecGrabberImplTest(String arg0) {
         super(arg0);
     }
     
     private List<URL> resources = null;
 	protected void setUp() throws Exception {
 		super.setUp();
 		String secFeed = configuration.getProperty("real.data.sec");
         URL feedUrl = new URL(secFeed);             
         Grabber grabber = new SecGrabberImpl(feedUrl);
         resources = grabber.getResources();
         assertTrue(resources.size() > 1900);
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}	
 	
 	public void testSecGrabberResourceRetrieval() {
 		try {
 
 			long start = System.currentTimeMillis();
 			for (URL resource: resources) {
 				if (! loader.getStore().hasDocument(resource.toString()))
 				loader.discover(resource);
 				System.out.println("Time taken = " + ((System.currentTimeMillis() - start) / 1000));
				if ((new Integer(loader.getStore().getNextFragmentId())).intValue() > 2000) {
 				    break;
 				}
 			}
 			System.out.println("All done and dusted!");
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail("An unexpected exception was thrown.");
 		}
 	}	
 	
 }
