 package com.customagent;
 
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.rmi.registry.LocateRegistry;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.management.MBeanServer;
 import javax.management.remote.JMXConnectorServer;
 import javax.management.remote.JMXConnectorServerFactory;
 import javax.management.remote.JMXServiceURL;
 import javax.management.remote.rmi.RMIConnectorServer;
 import javax.rmi.ssl.SslRMIClientSocketFactory;
 import javax.rmi.ssl.SslRMIServerSocketFactory;
 
 /**
  * Custom JMX Agent in order to resolve firewall issues with the RMI server.
  * 
  * @author Milo Casagrande
  */
 public class CustomJMXAgent {
     // Default port for the RMI Server
    private static final String DEFAULT_RMI_PORT = "8080";
 
     private CustomJMXAgent() {
         // Private and empty
     }
 
     /**
      * @param args
      * @throws IOException
      */
     public static void premain(final String args) throws IOException {
         System.setProperty("java.rmi.server.randomIDs", "true");
 
         // Get the port passed as argument, or set the default one
         final int newPort = Integer.parseInt(System.getProperty("rmi.custom.server.port", DEFAULT_RMI_PORT));
         LocateRegistry.createRegistry(newPort);
 
         String hostName;
         try {
             hostName = InetAddress.getLocalHost().getHostAddress();
         } catch (final UnknownHostException ex1) {
             // If something goes wrong, fallback to the localhost
             hostName = "localhost";
         }
 
         // Default MBeanServer
         final MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
 
         final Map<String, Object> envMap = new HashMap<String, Object>();
 
         final SslRMIClientSocketFactory clientSocketFact = new SslRMIClientSocketFactory();
         final SslRMIServerSocketFactory serverSocketFact = new SslRMIServerSocketFactory();
         envMap.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, clientSocketFact);
         envMap.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, serverSocketFact);
 
         // Create the RMI Server URL
         final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + hostName + ":" + newPort + "/jndi/rmi://"
                         + hostName + ":" + newPort + "/jmxrmi");
         // Create and start the connector server
         final JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, envMap, beanServer);
         cs.start();
     }
 }
