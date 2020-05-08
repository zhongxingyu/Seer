 package ar.edu.unicen.ringo.elasticsearch;
 
 import java.text.SimpleDateFormat;
 
 import javax.ws.rs.client.Client;
 import javax.ws.rs.client.ClientBuilder;
 import javax.ws.rs.client.Entity;
 import javax.ws.rs.client.Invocation;
 import javax.ws.rs.client.WebTarget;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import ar.edu.unicen.ringo.persistence.InvocationData;
 import ar.edu.unicen.ringo.persistence.PersistenceService;
 
 /**
  * {@link PersistenceService} implementation that uses ES REST API to perform
  * the persistence.
  * 
  * @author psaavedra
  */
 public class RestBasedElasticSearchPersistenceService implements
         PersistenceService {
     private static final String URL_PATTERN = "http://%s:%d/agent/invocation/";
    private static final String DATA_PATTERN = "{sla:\"%s\", node: \"%s\", method: \"%s\", execution_time: %d, timestamp: \"%s\"}";
 
     private final String url;
 
     public RestBasedElasticSearchPersistenceService() {
         this("localhost", 9200);
     }
 
     public RestBasedElasticSearchPersistenceService(String host, int port) {
         this.url = String.format(URL_PATTERN, host, port);
     }
 
     @Override
     public void persist(InvocationData data) {
         System.out.println("Persisting invocation data: " + data);
         Client client = ClientBuilder.newClient();
         WebTarget target = client.target(url);
         Invocation post = target.request(MediaType.APPLICATION_JSON_TYPE)
                 .buildPost(buildPayload(data));
         Response response = post.invoke();
         int status = response.getStatus();
         String result = response.readEntity(String.class);
         System.out.println("Status: " + status + ", response: " + result);
     }
 
     protected Entity<String> buildPayload(InvocationData data) {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
         String result = String.format(DATA_PATTERN, data.getSla(),
                 data.getNode(), data.getMethod(), data.getExecutionTime(),
                 sdf.format(data.getTimestamp()));
         System.out.println("Sending request: " + result);
         return Entity.entity(result, MediaType.APPLICATION_JSON_TYPE);
     }
 }
