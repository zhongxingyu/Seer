 package net.bluedash.resteasy.test.unit;
 
 
 import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import static org.junit.Assert.assertEquals;
 
public class TestTJWSEmbeddedJAXRSServer {
 
     private static TJWSEmbeddedJaxrsServer tjws;
 
     @Before
     public void setUp() {
         tjws = new TJWSEmbeddedJaxrsServer();
         tjws.setPort(8081);
         tjws.getDeployment().getActualResourceClasses().add(HelloWorldServiceImpl.class);
         tjws.start();
     }
 
     @After
     public void tearDown() {
         tjws.stop();
     }
 
     @Test
     public void testEmbeddedJAXRSServer() throws IOException {
         URL url = new URL("http://127.0.0.1:8081/helloworld");
         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
         connection.setRequestMethod("GET");
         connection.setRequestProperty("Content-Type", "text/plain");
         connection.setDoOutput(true);
         connection.setInstanceFollowRedirects(false);
         connection.setConnectTimeout(1000);
 
         BufferedReader ir = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
         assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
         assertEquals("Hello, world!", ir.readLine());
         ir.close();
         connection.disconnect();
 
     }
 
 }
