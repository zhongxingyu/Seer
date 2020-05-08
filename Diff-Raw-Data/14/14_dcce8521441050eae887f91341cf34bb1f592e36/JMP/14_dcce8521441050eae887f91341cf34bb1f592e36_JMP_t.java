 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.net.ServerSocket;
 
 public class JMP {
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
            System.err.print("Could not listen on port\n");
             System.exit(-1);
         }
         
         String[] ps = System.getProperty("plugins").split(",");
         
         while (listening) {
             for (String p: ps) {
                 try {
                     plugins.add((Proxy_Plugin) Proxy_Plugin.class.getClassLoader().loadClass(p).newInstance());
                    System.err.print("Loaded plugin "+p+"\n");
                 }
                 catch (java.lang.ClassNotFoundException e) {
                    System.err.print("Failed to load plugin "+p+"\n");
                     continue;
                 }
                 catch (java.lang.InstantiationException e) {
                    System.err.print("Failed to load plugin "+p+"\n");
                     continue;
                 }
                 catch (java.lang.IllegalAccessException e) {
                    System.err.print("Failed to load plugin "+p+"\n");
                     continue;
                 }
             }
             new Proxy(listener.accept(), mysqlHost, mysqlPort, plugins).start();
            plugins.clear();
         }
  
         listener.close();
     }
 }
