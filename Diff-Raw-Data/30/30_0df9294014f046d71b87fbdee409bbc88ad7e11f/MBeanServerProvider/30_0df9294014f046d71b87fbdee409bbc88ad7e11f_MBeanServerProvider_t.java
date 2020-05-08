 package net.sf.jguard.core.jmx;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import net.sf.jguard.core.ApplicationName;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import javax.annotation.Nullable;
 import javax.management.MBeanServer;
 import javax.management.MBeanServerFactory;
 import java.util.Arrays;
 import java.util.List;
 
 class MBeanServerProvider implements Provider<MBeanServer> {
     private String applicationName;
     private String mbeanServerForConnector;
     private static final Logger logger = LoggerFactory.getLogger(MBeanServerProvider.class.getName());
 
     @Inject
     public MBeanServerProvider(@ApplicationName String applicationName, @Nullable @MBeanServerForConnector String mbeanServerForConnector) {
         this.applicationName = applicationName;
         this.mbeanServerForConnector = mbeanServerForConnector;
     }
 
     public MBeanServer get() {
         MBeanServer mbs;
         if (mbeanServerForConnector == null || "".equals(mbeanServerForConnector) || "new".equals(mbeanServerForConnector)) {
             mbs = MBeanServerFactory.createMBeanServer(applicationName);
             logger.info("Starting JMX Server ...");
         } else if (mbeanServerForConnector.startsWith("position#")) {
             //user provides position#$position
             //for example 'position#2'
             mbeanServerForConnector = mbeanServerForConnector.substring(0, 9);
             int position = Integer.parseInt(mbeanServerForConnector);
             //does each webapp create its own MBeanServer
             //or do webapps share their MBeans on the same MBeanServer ?
             List mbeanServers = MBeanServerFactory.findMBeanServer(null);
             mbs = (MBeanServer) mbeanServers.get(position);
 
         } else {
             //user provides serverName#$position
             //for example 'myPrettyServer#3'
             //if user provides 'myPrettyServer#' or 'myPrettyServer',
             //we will get the first MBeanServer => like 'myPrettyServer#0'
             List tokens = Arrays.asList(mbeanServerForConnector.split("#"));
             String mbeanServerName = (String) tokens.get(0);
             String position;
             if (tokens.size() >= 2) {
                 position = (String) tokens.get(1);
             } else {
                 position = "0";
             }
             List<MBeanServer> mbeanServers = MBeanServerFactory.findMBeanServer(mbeanServerName);
 
             mbs = mbeanServers.get(Integer.parseInt(position));
 
         }
         return mbs;
     }
 }
