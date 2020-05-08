 package org.xbrlapi.data.bdbxml.tests;
 
 import java.net.URI;
 import java.util.List;
 
 import org.xbrlapi.data.bdbxml.BaseTestCase;
 import org.xbrlapi.grabber.Grabber;
 import org.xbrlapi.grabber.SecGrabberImpl;
 import org.xbrlapi.loader.discoverer.DiscoveryManager;
 
 public class SecAsyncGrabberImplTest extends BaseTestCase {
     
     public SecAsyncGrabberImplTest(String arg0) {
         super(arg0);
     }
 
     private List<URI> resources = null;
     protected void setUp() throws Exception {
         super.setUp();
         String secFeed = configuration.getProperty("real.data.sec");
         URI feedURI = new URI(secFeed);
         Grabber grabber = new SecGrabberImpl(feedURI);
         resources = grabber.getResources();
         assertTrue("# resources = " + resources.size(),resources.size() > 50);
     }
 
     protected void tearDown() throws Exception {
         super.tearDown();
     }   
     
     public void testSecGrabberResourceRetrieval() {
         try {
 
             int cnt = 2;
             List<URI> r1 = resources.subList(0,cnt);
            DiscoveryManager d1 = new DiscoveryManager(loader, r1, 20000);
            Thread t1 = new Thread(d1);
            t1.start();
 
            while (t1.isAlive()) {
                 Thread.sleep(2000);
                if (store.getDocumentURIs().size()>10)
                     loader.requestInterrupt();
             }
             
         } catch (Exception e) {
             e.printStackTrace();
             fail("An unexpected exception was thrown.");
         }
     }
     
 
     
 }
