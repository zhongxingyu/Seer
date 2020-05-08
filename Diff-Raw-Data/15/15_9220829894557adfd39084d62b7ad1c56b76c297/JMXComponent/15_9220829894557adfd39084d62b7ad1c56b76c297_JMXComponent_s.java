 package com.jclarity.had_one_dismissal.jmx;
 
 import java.lang.management.ManagementFactory;
 
 import javax.annotation.PreDestroy;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 
 public class JMXComponent {
 
     private MBeanServer server;
     private ObjectName name;
 
     protected void register(String address) throws Exception {
         server = ManagementFactory.getPlatformMBeanServer();
         name = new ObjectName(address);
        server.registerMBean(this, name);
     }
 
     @PreDestroy
     public void onShutdown() throws Exception {
         server.unregisterMBean(name);
     }
 
 }
