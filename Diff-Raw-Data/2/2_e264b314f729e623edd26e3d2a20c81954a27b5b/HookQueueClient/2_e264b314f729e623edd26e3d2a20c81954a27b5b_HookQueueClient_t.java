 package net.caprazzi.giddone.hook;
 
 import com.fasterxml.jackson.databind.DeserializationFeature;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.google.common.base.Optional;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.util.EntityUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 
 
 public class HookQueueClient {
 
     private static final Logger Log = LoggerFactory.getLogger(HookQueueClient.class);
 
     private final HttpClient client;
     private final String baseUrl;
 
     private final static ObjectMapper mapper = new ObjectMapper();
 
     static {
         mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
     }
 
     public HookQueueClient(HttpClient client, String baseUrl) {
         this.client = client;
         this.baseUrl = baseUrl;
     }
 
     public Optional<QueueElement> headValue() throws IOException {
         // TODO: info log
         Optional<Integer> head = head();
         if (!head.isPresent()) {
             return Optional.absent();
         }
         return value(head.get());
     }
 
     private Optional<Integer> head() throws IOException {
 
         HttpGet get = new HttpGet(baseUrl + "/github/post-receive-hooks/chrome/hooks/head");
        Log.debug("Fetching queue value from {}", get.getRequestLine());
         try {
             HttpResponse response = client.execute(get);
             Log.debug("Response to {}: {}", get, response);
             if (response.getStatusLine().getStatusCode() == 200) {
                 int id = Integer.parseInt(EntityUtils.toString(response.getEntity(), "UTF-8"));
                 Log.debug("Obtained head {}", id);
                 return Optional.of(id);
             }
             return Optional.absent();
         }
         finally {
             get.releaseConnection();
         }
     }
 
     private Optional<QueueElement> value(int id) throws IOException {
         // TODO: debug log
         HttpGet get = new HttpGet(baseUrl + "/github/post-receive-hooks/chrome/hooks/" + id + "/value");
         Log.debug("Fetching value for {} from {}", get.getRequestLine());
         try {
             HttpResponse response = client.execute(get);
             Log.debug("Response to {}: {}", get, response);
             if (response.getStatusLine().getStatusCode() == 200) {
                 return Optional.of(new QueueElement(id, mapper.readValue(response.getEntity().getContent(), PostReceiveHook.class)));
             }
             return Optional.absent();
         }
         finally {
             get.releaseConnection();
         }
     }
 
     public void success(int id) {
         // TODO: report success back to server
     }
 
     public void error(int id, Throwable t) {
         // TODO: report error back to server
     }
 
 }
