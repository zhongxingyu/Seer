 package com.thalesgroup.tcs.tai.gwcalipso;
 
 import org.glassfish.grizzly.http.server.HttpServer;
 import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
 import org.glassfish.jersey.server.ResourceConfig;
 
 import java.io.IOException;
 import java.net.URI;
 
 public class Main {
     // Base URI the Grizzly HTTP server will listen on
     public static final String BASE_URI = "http://localhost:8080/gwcalipso/";
 
     public static HttpServer startServer() {
         // create a resource config that scans for JAX-RS resources and providers
         final ResourceConfig rc = new ResourceConfig().packages("com.thalesgroup.tcs.tai.gwcalipso");
 
         // create and start a new instance of grizzly http server
         // exposing the Jersey application at BASE_URI
         return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
     }
 
     public static void main(String[] args) throws IOException {
         final HttpServer server = startServer();
        //Redis.redis_init();
         System.out.println(String.format("Jersey app started with WADL available at "
                 + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
         System.in.read();
         server.stop();
     }
 }
