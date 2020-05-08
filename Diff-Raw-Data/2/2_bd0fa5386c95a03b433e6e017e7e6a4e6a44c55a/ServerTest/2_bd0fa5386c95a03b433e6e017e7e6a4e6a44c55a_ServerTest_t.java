 package com.aprelev.pitchfork.server;
 
 import org.glassfish.grizzly.http.server.HttpServer;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import javax.ws.rs.client.Client;
 import javax.ws.rs.client.ClientBuilder;
 import javax.ws.rs.client.WebTarget;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * Created by alexander on 8/18/13
  */
 public class ServerTest {
 
     private HttpServer server;
     private WebTarget target;
 
     @Before
     public void setUp() throws Exception {
         // start the server
         server = Server.startServer();
         // create the client
         Client c = ClientBuilder.newClient();
 
         // uncomment the following line if you want to enable
         // support for JSON in the client (you also have to uncomment
         // dependency on jersey-media-json module in pom.xml and Main.startServer())
         // --
         //c.configuration().enable(new org.glassfish.jersey.media.json.JsonJaxbFeature());
 
         target = c.target(Server.BASE_URI);
     }
 
     @After
     public void tearDown() throws Exception {
         server.stop();
     }
 
     /**
      * Test to see that the message "Got it!" is sent in the response.
      */
     @Test
     public void testGetIt() {
         String responseMsg = target.path("releases")
             .path("list")
             //.path("ids").path("R01")
             .request().get(String.class);
      assertEquals("{\"webResourceList\":{\"resources\":[{\"description\":\"R05\",\"url\":\"http:\\/\\/localhost:8887\\/pitchfork\\/releases\\/ids\\/R05\"},{\"description\":\"R10\",\"url\":\"http:\\/\\/localhost:8887\\/pitchfork\\/releases\\/ids\\/R10\"},{\"description\":\"R01\",\"url\":\"http:\\/\\/localhost:8887\\/pitchfork\\/releases\\/ids\\/R01\"}]}}", responseMsg);
     }
 
 }
