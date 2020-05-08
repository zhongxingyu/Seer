 package org.fazio.dominion.rest;
 
 import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
 import com.sun.jersey.api.core.PackagesResourceConfig;
 import com.sun.jersey.api.core.ResourceConfig;
 import com.sun.jersey.api.json.JSONConfiguration;
 import org.fazio.dominion.config.DominionConfig;
 import org.glassfish.grizzly.http.server.HttpServer;
 
 import javax.ws.rs.core.UriBuilder;
 import java.io.IOException;
 import java.net.URI;
 
 /**
  * Created with IntelliJ IDEA.
  * @author Michael Fazio
  * Date: 10/27/12 - 12:09 AM
  */
 public class DominionRestMain {
 
 	public static final String WEB_SERVICE_URL = DominionConfig.WEB_SERVICE_URL;
 	public static final int WEB_SERVICE_PORT = Integer.valueOf(DominionConfig.WEB_SERVICE_PORT);
 
     public static final URI BASE_URI
 		= UriBuilder.fromUri("http://" + WEB_SERVICE_URL).port(WEB_SERVICE_PORT).build();
    public static final String BASE_RESOURCE_PACKAGE = DominionConfig.REST_RESOURCE_LOCATION;
 
     static HttpServer startServer() throws IOException {
         System.out.println("Starting Dominion server");
         ResourceConfig rc = new PackagesResourceConfig(BASE_RESOURCE_PACKAGE);
         rc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
 
         return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
     }
 
     public static void main(String[] args) throws IOException {
         HttpServer server = startServer();
     }
 }
