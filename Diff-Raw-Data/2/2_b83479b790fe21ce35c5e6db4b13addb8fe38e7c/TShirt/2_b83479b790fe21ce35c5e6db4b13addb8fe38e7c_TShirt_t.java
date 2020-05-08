 package fr.ybonnel;
 
 import fr.ybonnel.simpleweb4j.exception.HttpErrorException;
 import fr.ybonnel.simpleweb4j.handlers.Response;
 import fr.ybonnel.simpleweb4j.handlers.Route;
 import fr.ybonnel.simpleweb4j.handlers.RouteParameters;
 
 import static fr.ybonnel.simpleweb4j.SimpleWeb4j.*;
 
 /**
  * Main class.
  */
 public class TShirt {
 
 
     /**
      * Start the server.
      * @param port http port to listen.
      * @param waitStop true to wait the stop.
      */
     public static void startServer(int port, boolean waitStop) {
         // Set the http port.
         setPort(port);
         // Set the path to static resources.
         setPublicResourcesPath("/fr/ybonnel/public");
 
         // Start the server.
         start(waitStop);
     }
 
     public static void main(String[] args) {
         // Default port 9999.
         // For main, we want to wait the stop.
        startServer(Integer.getInteger("app_port", 9999), true);
     }
 }
