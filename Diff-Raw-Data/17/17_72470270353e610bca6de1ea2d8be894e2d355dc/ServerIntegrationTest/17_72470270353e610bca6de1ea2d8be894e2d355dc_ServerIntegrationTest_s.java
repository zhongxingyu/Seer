 package org.led.http;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
 import org.junit.Test;
 
 public class ServerIntegrationTest {
 
    private Server server;
 
    @Before
    public void setup() throws Exception {
         Thread serverThread = new Thread(new Runnable() {
 
             @Override
             public void run() {
                 server = new Server(new ServerConfiguration(8889));
                 try {
                     server.start();
                     Thread.sleep(1000);
                 } catch (Exception e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                     throw new RuntimeException(e);
                 }
 
             }
         });
         serverThread.start();
 
         while (server == null || !server.isStarted()) {
             // wait until server is started
             Thread.sleep(1000);
         }
 
     }
 
    @After
    public void after() {
         server.stop();
     }
 
     @Test
     public void shouldProcessGET() throws Exception {
 
         // setup
 
         // act
         HttpGet get = new HttpGet("http://localhost:8889/test");
         HttpClient client = new DefaultHttpClient();
         HttpResponse response = client.execute(get);
 
         // assert
         System.err.println("RESPONSE: " + response.toString());
 
     }
 
     @Test
     public void shouldProcessSeveralGets() throws Exception {
         DefaultHttpClient client = new DefaultHttpClient();
         HttpGet get = new HttpGet("http://localhost:8889/test");
         for (int i = 0; i < 10; i++) {
             HttpResponse response = client.execute(get);
             EntityUtils.consumeQuietly(response.getEntity());
 
         }
 
     }
 
 }
