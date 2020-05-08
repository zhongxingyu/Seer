 package DynamicWebBrowser.protocols;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Properties;
 
 /**
  *
  * @author Steffen, Mark, Shane
  */
 public class ProtocolFinder {
     
     private final String PROP_FILE = "ProtocolFinder.properties";
     
     private HashMap<String, Class> knownProtocols;
     
     private HTTPClassLoader classLoader;
     
     public ProtocolFinder() {
         knownProtocols = new HashMap<>();
         
         Properties properties = new Properties();
         try {
             //load a properties file
             properties.load(this.getClass().getResourceAsStream(PROP_FILE));
         } catch (IOException e) {
             System.err.println("Failed to open properties file.");
         }
         
         String host = properties.getProperty("host");
         int port = Integer.parseInt(properties.getProperty("port"));
         
         classLoader = new HTTPClassLoader(host, port);
     }
     
     /**
      * Finds protocol from cache or server
      * 
      * @param protocolName
      * @return will return null if protocol cannot be found
      */
     public Protocol findProtocol(String protocolName) {
         Protocol protocol = null;
         
         try {
             if (knownProtocols.containsKey(protocolName)) {
                 protocol = (Protocol) knownProtocols.get(protocolName).newInstance();
             } else {
                 Class protocolClass = classLoader.findClass(protocolName);
                 if (protocolClass != null) {
                     protocol = (Protocol) protocolClass.newInstance();
                    knownProtocols.put(protocolName, protocolClass);
                 }
             }
         } catch (InstantiationException e) {
             System.err.println("Failed to instantiate protocol");
         } catch (IllegalAccessException e) {
             System.err.println("IllegalAccessExeption when instantiating class");
         }
         
         return protocol;
     }
 }
