 package client;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.HttpResponse;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.json.simple.JSONObject;
 import java.net.URI;
 
 class ServiceUpdateManager {
    private static double DEFAULT_REFRESH = 5;
    private final String URL;
    private HttpClient Client = new DefaultHttpClient();
   private static String PATH = "/resource/careding/job";
   private static int PORT = 5000;
 
    public ServiceUpdateManager(String address) {
       URL = address;
    }
 
    protected double getRefreshRate() {
       double rate = 0d;
       rate = DEFAULT_REFRESH;
       /*HttpGet httpGet = new HttpGet(URL);
       setRequestHeaders(httpGet);*/
 
       return rate;
    }
 
   protected void postRequest(JSONObject output) throws IOException {
       URI weburi = null;
 
       try {
         weburi = new URI("http://" + URL + ":" + PORT + PATH);
       } catch (Exception e) {
          System.out.println(e);
       }
 
       HttpPost post = new HttpPost(weburi);
       setRequestHeaders(post);

      StringEntity entOut = new StringEntity(output.toString());
       post.setEntity(entOut);
       HttpResponse response = Client.execute(post);
       handleResponse(response);
    }
 
    private void setRequestHeaders(HttpRequestBase base) {
       base.setHeader("accept", "application/json");
       base.setHeader("Content-Type", "application/json");
    }
    
    protected void handleResponse(HttpResponse response) throws IOException {
       System.out.println(response.getStatusLine().getStatusCode());
 
       if (response.getStatusLine().getStatusCode() != 200) {
          throw new RuntimeException("Failed : HTTP error code : "
                          + response.getStatusLine().getStatusCode());
       }
 
       BufferedReader br = new BufferedReader(
                           new InputStreamReader((response.getEntity().getContent())));
 
       String output;
       System.out.println("Output from Server .... \n");
       while ((output = br.readLine()) != null) {
          System.out.println(output);
       }
    }
 
    protected void shutdown() {
       Client.getConnectionManager().shutdown();
    }
 }
