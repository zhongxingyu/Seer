 package cukepresentation;
 
 import com.sun.net.httpserver.HttpServer;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 
 /**
  * @author: Stephen Abrams
  */
 public class HelloWorldWebServer {
 
     private HttpServer server;
     int port;
 
     public HelloWorldWebServer(int port) {
         this.port = port;
     }
 
     public void start() throws IOException {
         server = HttpServer.create(new InetSocketAddress(this.port), 0);
        server.createContext("/hello_world", new HelloWorldHandler());
         server.setExecutor(null);
         server.start();
     }
 
     public void stop() {
        if (server != null)
            server.stop(0);
     }
 }
