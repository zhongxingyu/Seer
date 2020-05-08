 package dk.itu.kf04.g4tw.controller;
 
 import dk.itu.kf04.g4tw.model.HTTPConstants;
 
 import java.io.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.URLConnection;
 import java.util.Date;
 
 /**
  *
  */
 public class WebServer implements HTTPConstants {
 
     /**
      * Tests whether the server already has been started.
      */
     private static boolean isInitialized = false;
 
     /**
      * A flag to ensure the server doesn't go on forever.
      */
     private static boolean isInterrupted = false;
     
     /**
      * The port of the webserver.
      */
     private static int port = 80;

    /**
     * The root directory of the www files.
     */
    private static String webRoot = "www/";
     
     /**
      * Initialize the server and prepare to respond on incoming requests.
      *
      * @return  boolean  A boolean flag indicating success or failure
      */
     public static boolean init() {
         // Return if webserver already has been started
         if (isInitialized) return false;
 
         // Try to initialize a ServerSocket
         try {
             ServerSocket server = new ServerSocket(port);
             isInitialized = true;
 
             // Loop
             while(true) {
                 // Wait for a connection
                 Socket s = server.accept();
 
                 // Read request from socket input stream
                 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                 String request = in.readLine();
                 s.shutdownInput();
 
                 // Get output-stream
                 OutputStream out = new BufferedOutputStream(s.getOutputStream());
                 PrintStream pout = new PrintStream(out);
 
                 // Turn down bad requests
                 if (request == null ||
                     !request.startsWith("GET") ||
                     request.endsWith("HTTP/1.0") || // We do not accept 1.0
                     !request.endsWith("HTTP/1.1") ||
                     request.charAt(4) != '/') {
                     pout.println("Bad request. The server could not understand your query: " + request);
                 } else {
                     // Find the request for a file
                     String fileRequest = request.substring(5, request.length() - 9);
 
                     // Set the input stream and content-type
                     InputStream input = null;
                     String contentType = null;
 
                     // Match for map requests
                     if (fileRequest.startsWith("?")) {
                         // Set input stream via
                         input = RequestParser.parseToInputStream(fileRequest.substring(1, fileRequest.length()));
 
                         // Set the content type
                         if (input != null) {
                             contentType = "text/xml";
                         }
 
                     // Process request as normal
                     } else {
                        // Include the webRoot
                        fileRequest = webRoot + (fileRequest.equals("") ? "index.html" : fileRequest);
 
                         try {
                             // Load file
                             File file = new File(fileRequest);
                             input = new FileInputStream(file);
                             contentType = URLConnection.guessContentTypeFromName(fileRequest);
                         } catch (FileNotFoundException e) {
                             System.out.println("File " + fileRequest + " not found.");
                         }
                     }
                     
                     // Output the response
                     if (input != null) {
                         // Print HTTP status code
                         pout.println("HTTP/1.1 200 OK");
                         pout.println("Server: KraXServer/1.0");
                         pout.println("Date: " + new Date());
                         if (contentType != null) {
                             pout.println("Content-Type: " + contentType);
                         }
                         pout.println(); // Create line between meta code and content
 
                         // Send object
                         byte[] buffer = new byte[1000];
                         while(input.available() > 0) {
                             out.write(buffer, 0, input.read(buffer));
                         }
                     }
                 }
 
                 // Flush the streams
                 pout.flush();
                 
                 // Close down the socket
                 s.shutdownOutput();
                 s.close();
 
                 // Interrupt the server, if necessary.
                 if (isInterrupted) {
                     throw new InterruptedException("Server has been interrupted.");
                 }
             }
         } catch (InterruptedException e) { // SocketException
             System.out.println("Interrupted with: " + e.getMessage());
         } catch (SocketException e) { // SocketException
             System.out.println("SocketException");
             e.printStackTrace();
             isInitialized = false;
         } catch (IOException e) { // IOException
             System.out.println("IOException: ");
             e.printStackTrace();
         }
         return isInitialized;
     }
 
     /**
      * Returns the port of the WebServer.
      * @return int  The port the WebServer listens to when initialized.
      */
     public static int getPort() { return port; }
 
     /**
      * Stop the server in the next loop.
      */
     public static void stop() { isInterrupted = true; }
     
     /**
      * Sets the port of the WebServer.
      * @param port  The port between 0 and 0xFFFF
      */
     public static void setPort(int port) {
         WebServer.port = port;
     }
 
 }
