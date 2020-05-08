 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.net.ServerSocket;
 
public class JMTC {
     public static void main(String[] args) throws IOException {
         String mysqlHost = System.getProperty("mysqlHost");
         int mysqlPort = Integer.parseInt(System.getProperty("mysqlPort"));
         int port = Integer.parseInt(System.getProperty("port"));
         boolean listening = true;
         ServerSocket listener = null;
         ArrayList<Proxy_Plugin> plugins = new ArrayList<Proxy_Plugin>();
         
         try {
             listener = new ServerSocket(port);
         }
         catch (IOException e) {
             System.out.println("Could not listen on port");
             System.exit(-1);
         }
         
         String[] ps = System.getProperty("plugins").split(",");
         for (String p: ps) {
             try {
                 plugins.add((Proxy_Plugin) Proxy_Plugin.class.getClassLoader().loadClass(p).newInstance());
             }
             catch (java.lang.ClassNotFoundException e) {
                 continue;
             }
             catch (java.lang.InstantiationException e) {
                 continue;
             }
             catch (java.lang.IllegalAccessException e) {
                 continue;
             }
         }
         
         while (listening)
             new Proxy(listener.accept(), mysqlHost, mysqlPort, plugins).start();
  
         listener.close();
     }
 }
