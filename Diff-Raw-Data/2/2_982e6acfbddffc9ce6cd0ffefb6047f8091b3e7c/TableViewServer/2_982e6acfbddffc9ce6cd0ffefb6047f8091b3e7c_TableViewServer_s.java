 package edu.umn.genomics.server;
 
 import edu.umn.genomics.table.TableView;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.SwingUtilities;
 
 public class TableViewServer {
 
     static final String http_response = "HTTP/1.1 204 No Content\n\n";
     private static final int NO_PORT = -1;
     private static final int default_server_port = 6085;
     private static final int ports_to_try = 1;
     private static final int CONNECT_TIMEOUT = 20000;   // If you can't connect in 20 seconds, fail.
     private static final int READ_TIMEOUT = 60000;	// If you can't read any data in 1 minute, fail.
         
     public static final String SERVLET_NAME = "TableViewControl";
     public static final String DEFAULT_SERVLET_URL = "http://localhost:"
             + default_server_port + "/" + SERVLET_NAME;
     private static final Logger ourLogger = Logger.getLogger(TableViewServer.class.getPackage().getName());
 
  
     private TableViewServer(TableView object, ServerSocket server) {
         try {
             while (true) {
                 Socket socket = server.accept();
                 ourLogger.log(Level.FINE, "Connection accepted {0}:{1}",
                         new Object[]{socket.getInetAddress(), socket.getPort()});
                 TableViewHttpRequestHandler request = new TableViewHttpRequestHandler(object, socket);
                 Thread thread = new Thread(request);
                 thread.start();
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
  
     public static void startServerSocket(final TableView object) {
         try {
             int serverPort = findAvailablePort(default_server_port);
 
             if (serverPort == NO_PORT) {
                 ourLogger.log(Level.SEVERE,
                         "Couldn't find an available port for TableView to listen to control requests on port {0}!"
                         + "\nTurning off TableView's URL-based control features", default_server_port);
             } else {
                 final ServerSocket serverSocket = new ServerSocket(serverPort);
                 Runnable r = new Runnable() {
                     @Override
                     public void run() {
                         new TableViewServer(object, serverSocket);
                     }
                 };
 
                 final Thread t = new Thread(r);
 
                 SwingUtilities.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                         t.start();
                     }
                 });
             }
 
         } catch (IOException ex) {
             ourLogger.log(Level.SEVERE, "I/O Problem", ex);
         }
     }
 
     /**
      * Find an available port. Start with the default_server_point and
      * incrementing up from there.
      *
      * @return port found
      */
     private static int findAvailablePort(int startPort) {
         // 
         int ports_tried = 0;
         int serverPort = startPort - 1;
         boolean available_port_found = false;
         while ((!available_port_found) && (ports_tried < ports_to_try)) {
             serverPort++;
             URL test_url;
             try {
                 test_url = new URL("http://localhost:" + serverPort
                         + "/" + SERVLET_NAME + "?ping=yes");
             } catch (MalformedURLException mfe) {
                 return TableViewServer.NO_PORT;
             }
 
             try {
                 // try and find an open port...
                 URLConnection conn = test_url.openConnection();
                 conn.setConnectTimeout(CONNECT_TIMEOUT);
                 conn.setReadTimeout(READ_TIMEOUT);
                 conn.connect();
                 // if connection is successful, that means we cannot use that port
                 // and must try another one.
                 ports_tried++;
             } catch (IOException ex) {
                 ourLogger.log(Level.INFO,
                        "Found available port for bookmark server: {0}", serverPort);
                 available_port_found = true;
             }
         }
 
         if (available_port_found) {
             return serverPort;
         } else {
             return TableViewServer.NO_PORT;
         }
     }
 }
