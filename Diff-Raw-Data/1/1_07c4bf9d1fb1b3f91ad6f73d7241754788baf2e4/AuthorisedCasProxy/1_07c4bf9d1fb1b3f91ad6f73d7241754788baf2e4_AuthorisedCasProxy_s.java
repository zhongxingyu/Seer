 package net.pizey.cas.proxy;
 
 import org.apache.commons.cli.*;
 
 import java.io.File;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Vector;
 
 /**
  * 
  * @see http://java.sun.com/developer/technicalArticles/Networking/Webserver/
  * @see http://java.sun.com/developer/technicalArticles/Networking/Webserver/WebServer.java
  */
 
 public class AuthorisedCasProxy {
     protected static String password;
     protected static String ticketGrantingServiceUrl;
     protected static String host;
     protected static String user;
     public static final int HTTP_OK = 200;
     public static final int HTTP_NOT_FOUND = 404;
     public static final int HTTP_BAD_METHOD = 405;
 
     static boolean keepGoing = true;
     private static int port;
 
     protected static void log(String s) {
         System.out.println(s);
     }
 
     /* Where worker threads stand idle */
     static Vector<ProxyWorker> threads = new Vector<ProxyWorker>();
 
     /* the web server's virtual root */
     static File root;
 
     /* timeout on client connections */
     static int timeout = 0;
 
     /* max # worker threads */
     static int workers = 5;
 
 
 
     public static void main(String[] args) throws Exception {
         configure(args);
 
         /* start worker threads */
         for (int i = 0; i < workers; ++i) {
             ProxyWorker w = new ProxyWorker();
             (new Thread(w, "worker #" + i)).start();
             threads.addElement(w);
         }
 
         ServerSocket ss = new ServerSocket(port);
 
         while (keepGoing) {
 
             Socket s = ss.accept();
 
             ProxyWorker w;
             synchronized (threads) {
                 if (threads.isEmpty()) {
                     ProxyWorker ws = new ProxyWorker();
                     ws.setSocket(s);
                     (new Thread(ws, "additional worker")).start();
                 } else {
                     w = threads.elementAt(0);
                     threads.removeElementAt(0);
                     w.setSocket(s);
                 }
             }
         }
 
     }
 
     public static void stop() {
       keepGoing = false;
       for (ProxyWorker pw : threads) {
           System.err.println("Stopping " + pw);
           pw.notify();
       }
     }
     public static void configure(String[] args) throws ParseException {
 
         root = new File(System.getProperty("user.dir"));
         timeout = 5000;
         workers = 5;
         port = 7777;
 
         Options options = new Options();
 
         Option h = new Option("host", true, "The protected host, required");
         h.setRequired(true);
         options.addOption(h);
 
         Option tgu = new Option("ticketGrantingServiceUrl", true, "The ticket granting service url, required");
         tgu.setRequired(true);
         options.addOption(tgu);
 
         Option u = new Option("user", true, "The user to authenticate as, required");
         h.setRequired(true);
         options.addOption(u);
         Option p = new Option("password", true, "The user password, required");
         p.setRequired(true);
         options.addOption(p);
 
 
         // create the parser
         CommandLineParser parser = new GnuParser();
         CommandLine line = parser.parse(options, args);
 
         host = line.getOptionValue("host");
         ticketGrantingServiceUrl = line.getOptionValue("ticketGrantingServiceUrl");
         user = line.getOptionValue("user");
         password = line.getOptionValue("password");
 
         log("root=" + root);
         log("timeout=" + timeout);
         log("workers=" + workers);
         log("port=" + port);
         log("host=" + host);
         log("user=" + user);
         log("password=****");
         log("ticketGrantingServiceUrl=" + ticketGrantingServiceUrl);
 
     }
 }
