 package de.mh.mothership.http;
 
 import com.google.common.base.Throwables;
 import com.google.inject.Inject;
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.handler.AbstractHandler;
 import org.mortbay.jetty.handler.ErrorHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static com.google.common.base.Throwables.propagate;
 
 public class WebServer {
     private static final Logger LOG = LoggerFactory.getLogger(WebServer.class);
 
     public static final int PORT = 33602;
     private final Server server;
 
     @Inject
     public WebServer(GeneratePdfRequestHandler requestHandler) {
         server = new Server(PORT);
         try {
             LOG.info("Starting Webserver on "+PORT);
 
             server.setHandler(requestHandler);
             server.start();
             LOG.info("Webserver Running. Surf to http://localhost:"+PORT);
            server.join();
 
         } catch (Exception e) {
             propagate(e);
         }
     }
 
     public void stop() {
         try {
             server.stop();
         } catch (Exception e) {
             propagate(e);
         }
     }
 
 }
