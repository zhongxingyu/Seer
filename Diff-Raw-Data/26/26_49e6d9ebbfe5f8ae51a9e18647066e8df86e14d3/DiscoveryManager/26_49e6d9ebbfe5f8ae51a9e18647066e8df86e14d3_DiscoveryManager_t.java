 package org.xbrlapi.loader.discoverer;
 
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.xbrlapi.loader.Loader;
 import org.xbrlapi.utilities.XBRLException;
 
 public class DiscoveryManager implements Runnable {
 
     static Logger logger = Logger.getLogger(DiscoveryManager.class);
     
     private Loader loader = null;
     private long interval = 600000;
     
     public DiscoveryManager(Loader loader) {
         try {
             if (loader == null) throw new XBRLException("The loader is null.");
             this.loader = loader;
         } catch (XBRLException e) {
             e.printStackTrace();
             logger.error(Thread.currentThread().getName() + ": The discoverer could not be instantiated.");
         }
     }
 
     public DiscoveryManager(Loader loader, long interval) {
         this(loader);
         this.interval = interval;
     }
     
     List<URL> resources = new LinkedList<URL>();
     
     /**
      * @param loader The loader to do the discovery with.
      * @param resources The URLs to discover.
      */
     public DiscoveryManager(Loader loader, List<URL> resources) {
         this(loader);
         if (resources != null) {
             this.resources = resources;
         }
     }
     
     public DiscoveryManager(Loader loader, List<URL> resources, long interval) {
         this(loader,interval);
         if (resources != null) {
             this.resources = resources;
         }
     }    
     
     public void run() {
         try {
             logger.info(Thread.currentThread().getName() + ": Successfully began.");            
             if (loader == null) {
                 logger.error(Thread.currentThread().getName() + ": Discovery failed because the discoverer does not have a loader.");
             } else {
                 
                 for (URL resource: resources) {
                     loader.stashURL(resource);
                }
 
                 //Store store = loader.getStore();
                 Discoverer discoverer = new Discoverer(loader);
                 Thread thread = new Thread(discoverer);
                 thread.start();
                 
                 while (thread.isAlive()) {
                     
                     int sleptTime = 0;
                     while (sleptTime < interval) {
                         Thread.sleep(1000);
                         sleptTime = sleptTime + 1000;
                         if (! thread.isAlive()) {
                             break;
                         }
                     }
                     
                     if (thread.isAlive()) {
                         loader.requestInterrupt();
                         while (thread.isAlive()) {
                             Thread.sleep(1000);
                         }
                         discoverer = new Discoverer(loader);
                         thread = new Thread(discoverer);
                         thread.start();
                     }
                 }
                 
             }
             logger.info(Thread.currentThread().getName() + ": Successfully exited."); 
             
         } catch (Exception e) {
             e.printStackTrace();
             logger.error(Thread.currentThread().getName() + ": The discovery manager experienced an Exception.");
         }
     }
 
 }
