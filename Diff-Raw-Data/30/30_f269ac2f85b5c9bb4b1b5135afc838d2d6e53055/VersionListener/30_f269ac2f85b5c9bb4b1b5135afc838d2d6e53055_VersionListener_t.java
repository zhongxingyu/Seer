 package com.fatwire.gst.web.servlet.profiling.version;
 
 import java.lang.management.ManagementFactory;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.fatwire.gst.web.servlet.profiling.jmx.UnregisterMBeansCommand;
 
 public class VersionListener implements ServletContextListener {
     private final Log log = LogFactory.getLog(this.getClass());
 
     public void contextDestroyed(ServletContextEvent sce) {
         try {
             UnregisterMBeansCommand
                     .unregister("com.fatwire.gst.web.servlet:type=Version,*");
         } catch (Throwable e) {
             log.error(e.getMessage(), e);
         }
 
     }
 
     public void contextInitialized(final ServletContextEvent sce) {
         new Thread(new Runnable() {
 
             public void run() {
                 List<ProductInfo> productInfo = new LinkedList<ProductInfo>();
 
                 ProductInfoFactory f = new ProductInfoFactory();
                 productInfo.addAll(f.createList(sce.getServletContext()));
                 MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                 for (ProductInfo info : productInfo) {
                     try {
                         server.registerMBean(info, new ObjectName(
                                "com.fatwire.gst.web.servlet:type=Version,jar="
                                        + info.getProductJar() +",product="
                                        + info.getProductName() ));
                     } catch (Throwable e) {
                         log.error(e.getMessage(), e);
                     }
                 }
 
             }
 
         }).start();
 
     }
 
 }
