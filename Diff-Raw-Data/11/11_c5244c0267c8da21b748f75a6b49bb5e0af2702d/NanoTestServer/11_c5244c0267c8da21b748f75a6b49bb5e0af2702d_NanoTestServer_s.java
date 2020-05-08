 package me.fumiz.android.test.http.impl.nano;
 
 import me.fumiz.android.test.http.TestRequest;
 import me.fumiz.android.test.http.TestResponse;
 import me.fumiz.android.test.http.TestServer;
 
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * TestServer implementation with NanoHTTPd
  * User: fumiz
  * Date: 11/09/26
  * Time: 21:21
  */
 public class NanoTestServer extends TestServer {
     /**
      * Server Entity
      */
     private class Server extends NanoHTTPd {
         public Server(int port) throws IOException {
             super(port);
         }
 
         @Override
         public Response serve(String uri, String method, Properties header, Properties params, Properties files) {
             //TODO: implements files Map (this method and NanoHTTPd)
             TestResponse response = onRequest(new TestRequest(uri, method, header, params, null));
             return new Response(response.getStatusCode(), response.getMimeType(), response.getBody());
         }
     }
 
     /**
      * Server instance
      */
     private Server mServer = null;
 
     /**
      * init with listening port number
     * @param portNumber
      */
     public NanoTestServer(int portNumber) {
         super(portNumber);
     }
 
     /**
      * Start Test Server
      */
     @Override
     public void start() {
         try {
             mServer = new Server(getPortNumber());
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Stop Test Server
      */
     @Override
     public void stop() {
         mServer.stop();
     }
 }
