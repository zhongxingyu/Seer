 package com.fatwire.gst.web.servlet.profiling.jmx;
 
 import java.lang.management.ManagementFactory;
 import java.util.Set;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import COM.FutureTense.Util.ftTimedHashtable;
 
 public class CacheManager implements CacheManagerMBean {
 
     private CacheManagerRunnable runnable;
 
     private MBeanServer server;
 
     private Object signal = new Object();
 
     public int getNumberOfCaches() {
         return ftTimedHashtable.getAllCacheNames().size();
     }
 
     public void shutdown() throws Exception {
         if (runnable != null) {
             runnable.shutDown();
             server=null;
         }
 
     }
 
     public void start() throws Exception {
         if (runnable == null) {
             server = ManagementFactory.getPlatformMBeanServer();
             runnable = new CacheManagerRunnable();
             final Thread t = new Thread(runnable, "CacheManager JMX Thread");
             t.setDaemon(true);
             t.start();
         }
 
     }
 
     class CacheManagerRunnable implements Runnable {
         private final Log log = LogFactory.getLog(this.getClass());
 
         private final long waitTime = 5000L;
 
         private boolean stop = false;
 
         public void run() {
 
             while (!stop) {
                 try {
                     synchronized (signal) {
                         signal.wait(waitTime);
                     }
                     registerMBeans();
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                 }
 
             }

         }
 
         /**
          * registers ftTimedHashtable's RuntimeStats as MBeans in the MBean server
          */
         @SuppressWarnings("unchecked")
         private void registerMBeans() {
             //TODO: deregistering of MBeans when ftTimedHashtable no longer exists
             final Set<String> hashNames = ftTimedHashtable.getAllCacheNames();
             for (final String hashName : hashNames) {
                 try {
 
                     final ObjectName name = new ObjectName(
                              "com.fatwire.gst.web.servlet:type=Cache,name="
                             + ObjectName.quote(hashName));
                     //log.debug(name);
                    if (!server.isRegistered(name)) {
                         server.registerMBean(new CacheStats(hashName), name);
 
                     }
 
                 } catch (final MalformedObjectNameException e) {
                     log.error(e.getMessage(), e);
                 } catch (final InstanceAlreadyExistsException e) {
                     log.error(e.getMessage(), e);
                 } catch (final MBeanRegistrationException e) {
                     log.error(e.getMessage(), e);
                 } catch (final NotCompliantMBeanException e) {
                     log.error(e.getMessage(), e);
                 }
             }
 
         }
 
         synchronized void shutDown() {
             stop = true;
             synchronized (signal) {
                 signal.notifyAll();
             }
 
         }
 
     }
 
 
 }
