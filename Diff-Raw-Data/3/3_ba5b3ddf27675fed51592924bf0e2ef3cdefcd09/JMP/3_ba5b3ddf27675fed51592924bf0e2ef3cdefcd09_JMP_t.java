 /*
  * Java Mysql Proxy
  * Main binary. Just listen for connections and pass them over
  * to the proxy module
  */
 
 import java.io.*;
 import java.util.*;
 import java.net.ServerSocket;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 public class JMP {
     public static void main(String[] args) throws IOException {
         String mysqlHost = System.getProperty("mysqlHost");
         int mysqlPort = Integer.parseInt(System.getProperty("mysqlPort"));
         int port = Integer.parseInt(System.getProperty("port"));
         boolean listening = true;
         ServerSocket listener = null;
         ArrayList<Proxy_Plugin> plugins = new ArrayList<Proxy_Plugin>();
         
         Logger logger = Logger.getLogger("JMP");
         PropertyConfigurator.configure(System.getProperty("logConf"));
         
         try {
             listener = new ServerSocket(port);
         }
         catch (IOException e) {
             logger.fatal("Could not listen on port "+port);
             System.exit(-1);
         }
         
         String[] ps = System.getProperty("plugins").split(",");
         
         while (listening) {
            plugins = new ArrayList<Proxy_Plugin>();
             for (String p: ps) {
                 try {
                     plugins.add((Proxy_Plugin) Proxy_Plugin.class.getClassLoader().loadClass(p).newInstance());
                     logger.info("Loaded plugin "+p);
                 }
                 catch (java.lang.ClassNotFoundException e) {
                     logger.error("Failed to load plugin "+p);
                     continue;
                 }
                 catch (java.lang.InstantiationException e) {
                     logger.error("Failed to load plugin "+p);
                     continue;
                 }
                 catch (java.lang.IllegalAccessException e) {
                     logger.error("Failed to load plugin "+p);
                     continue;
                 }
             }
             new Proxy(listener.accept(), mysqlHost, mysqlPort, plugins).start();
         }
  
         listener.close();
     }
 }
