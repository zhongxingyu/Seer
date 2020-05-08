 package nl.astraeus.http;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import java.io.IOException;
 import java.net.*;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * User: rnentjes
  * Date: 4/3/12
  * Time: 7:36 PM
  */
 public class SimpleWebServer implements Runnable {
     private SimpleServletContext    context;
     private Thread                  serverThread;
     private volatile boolean        running = true;
     private int                     port = 8080;
 
     private SortedMap<String, HttpServlet> servlets = new TreeMap<String, HttpServlet>(new Comparator<String>() {
         public int compare(String o1, String o2) {
             int result = 0;
 
             if (o1 != null && o2 != null) {
                 result = (o1.length() - o2.length()) > 0 ? -1 : 1;
             }
 
             return result;
         }
     });
 
     private Map<String, SimpleHttpSession> sessions = new ConcurrentHashMap<String, SimpleHttpSession>(new HashMap<String, SimpleHttpSession>());
 
     public SimpleWebServer (int port) {
         this.serverThread = new Thread(this);
         this.context = new SimpleServletContext(this);
         this.port = port;
     }
 
     public void start() {
         try {
             for (HttpServlet servlet : servlets.values()) {
                 servlet.init();
             }
 
             serverThread.start();
         } catch (ServletException e) {
             throw new IllegalStateException(e);
         }
     }
 
     public void stop() {
         running = false;
     }
 
     public void run() {
         try {
             ServerSocketChannel ssc = ServerSocketChannel.open();
             InetSocketAddress isa = new InetSocketAddress(port);
             ssc.socket().bind(isa);
 
             try {
                 while (running) {
                     SocketChannel sc = ssc.accept();
                     try {
 
                         //new SimpleRequestThread(sock).run();
                         Thread thread = new Thread(new SimpleRequestThread(this, sc));
 
                         thread.start();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             } finally {
                 ssc.close();//always close the ServerSocket
             }
         } catch (BindException B) {
             //handling exception generated if they are already running server
             System.out.println("SERVER Already Running");
         } catch (Throwable e) {
             e.printStackTrace();
         }
 
     }
 
     public void addServlet(HttpServlet servlet, String uri) {
         if (uri == null) {
             uri = "/*";
         }
 
         servlets.put(uri, servlet);
     }
 
     HttpServlet findHandlingServlet(String requestURI) {
         HttpServlet result = null;
 
         if (requestURI == null) {
             requestURI = "";
         }
 
         for (Map.Entry<String, HttpServlet> entry : servlets.entrySet()) {
             String uri = entry.getKey();
             if (uri.endsWith("*")) {
                 if (requestURI.startsWith(uri.substring(0, uri.length()-1))) {
                     result = entry.getValue();
                     break;
                 }
             } else {
                 if (requestURI.equals(uri)) {
                     result = entry.getValue();
                     break;
                 }
             }
         }
 
         return result;
     }
 
     SimpleHttpSession getSession(String id) {
         SimpleHttpSession result;
 
         if (id == null || id.length() == 0) {
             id = createSessionId();
         }
 
         result = sessions.get(id);
 
         if (result == null) {
             result = new SimpleHttpSession(this, id);
 
             sessions.put(id, result);
         }
 
         return result;
     }
 
     synchronized String createSessionId() {
        int hash = ((Long)System.nanoTime()).hashCode();
 
        if (sessions.containsKey(hash)) {
            hash = ((Long)System.nanoTime()).hashCode();
         }
 
        String result = String.valueOf(hash);

        return result;
     }
 
     SimpleServletContext getServletContext() {
         return context;
     }
 
     void addRequestLog(HttpServlet servlet, SimpleHttpRequest request, long l) {
     }
 
     int getPort() {
         return port;
     }
 }
