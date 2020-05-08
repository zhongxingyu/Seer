 package pl.mjedynak;
 
 import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
 import com.sun.jersey.api.core.PackagesResourceConfig;
 import com.sun.jersey.api.core.ResourceConfig;
 import org.glassfish.grizzly.http.server.HttpServer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.core.UriBuilder;
 import java.io.IOException;
 import java.net.URI;
 
 public class App {
 
     private static final Logger logger = LoggerFactory.getLogger(App.class);
 
     private static final URI BASE_URI = UriBuilder.fromUri("http://localhost/").port(9998).build();
 
     protected static HttpServer startServer() throws IOException {
         logger.debug("Starting grizzly...");
         ResourceConfig resourceConfig = new PackagesResourceConfig("pl.mjedynak.resource");
         return GrizzlyServerFactory.createHttpServer(BASE_URI, resourceConfig);
     }
 
     public static void main(String[] args) throws IOException {
         HttpServer httpServer = startServer();
        logger.debug(String.format("Jersey app started with WADL available at %sapplication.wadl"), BASE_URI);
         logger.debug(String.format("Try out %sresource\nHit enter to stop it...", BASE_URI));
         System.in.read();
         httpServer.stop();
     }
 }
