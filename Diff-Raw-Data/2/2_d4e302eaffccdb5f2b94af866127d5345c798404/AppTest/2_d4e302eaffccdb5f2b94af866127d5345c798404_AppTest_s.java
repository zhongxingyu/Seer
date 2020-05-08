 package com.alfredwesterveld;
 
 import com.alfredwesterveld.webserver.WebServerConfigurator;
 import com.ning.http.client.AsyncHttpClient;
 import com.ning.http.client.Response;
 import java.util.concurrent.Future;
 import org.atmosphere.grizzly.AtmosphereSpadeServer;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * Unit test for simple App.
  */
 public class AppTest {
     public static final String HOST = "http://localhost:8888/";
     
     /**
      * 
      * @throws Exception
      */
     @Test
     public void testServer() throws Exception {
         AtmosphereSpadeServer setup = 
             WebServerConfigurator.setup(HOST);
         setup.start();
         AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
         Future<Response> execute = 
            asyncHttpClient.prepareGet(HOST + "schedule/info").execute();
         assertEquals("scheduler", execute.get().getResponseBody());
     }
 }
