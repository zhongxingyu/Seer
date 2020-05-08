 package com.ning.metrics.goodwill.access;
 
 import com.ning.http.client.AsyncCompletionHandler;
 import com.ning.http.client.AsyncHttpClient;
 import com.ning.http.client.Response;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.Future;
 
 public class GoodwillAccessor
 {
     private static final Logger log = Logger.getLogger(GoodwillAccessor.class);
 
     private final String host;
     private final int port;
     private String url;
 
     private final AsyncHttpClient client = new AsyncHttpClient();
 
     public GoodwillAccessor(String host, int port)
     {
         this.host = host;
         this.port = port;
 
         this.url = String.format("http://%s:%d/registrar", host, port);
     }
 
     /**
      * Get the Goodwill schema associated to a schema name.
      * </p>
      * Typical invocation:
      * <pre>
      * {@code
      * try {
      * GoodwillSchema type = accessor.getSchema("test").get();
      * ...
      * }
      * catch (Exception e) {
      * // Connection exception? Goodwill server down?
      * }
      * }
      * </pre>
      *
      * @param schemaName schema name to lookup
      * @return a future on the Schema
      */
     public Future<GoodwillSchema> getSchema(String schemaName)
     {
         try {
             return client.prepareGet(String.format("%s/%s", url, schemaName)).addHeader("Accept", "application/json").execute(new AsyncCompletionHandler<GoodwillSchema>()
             {
                 @Override
                 public GoodwillSchema onCompleted(Response response) throws Exception
                 {
                     if (response.getStatusCode() != 200) {
                         return null;
                     }
 
                     BufferedReader reader = new BufferedReader(new InputStreamReader(response.getResponseBodyAsStream()));
                     ObjectMapper mapper = new ObjectMapper();
 
                     GoodwillSchema thrift = mapper.readValue(reader, GoodwillSchema.class);
 
                     reader.close();
 
                     return thrift;
                 }
 
                 @Override
                 public void onThrowable(Throwable t)
                 {
                     log.warn(t);
                 }
             });
         }
         catch (IOException e) {
             log.warn(String.format("Error getting Schema list from %s:%d", host, port));
             return null;
         }
     }
 
     /**
      * Get all schemata.
      * <p/>
      * Use schemata, instead of schemas, which is closer to the original σχήματα.
      *
      * @return a future on a list of Schema
      * @see #getSchema for an invocation example
      */
     public Future<List<GoodwillSchema>> getSchemata()
     {
         try {
             return client.prepareGet(url).addHeader("Accept", "application/json").execute(new AsyncCompletionHandler<List<GoodwillSchema>>()
             {
                 @Override
                 public List<GoodwillSchema> onCompleted(Response response) throws Exception
                 {
                     if (response.getStatusCode() != 200) {
                         return null;
                     }
 
                     BufferedReader reader = new BufferedReader(new InputStreamReader(response.getResponseBodyAsStream()));
                     ObjectMapper mapper = new ObjectMapper();
 
                    HashMap<String, List<GoodwillSchema>> map = mapper.readValue(
                        reader,
                        new TypeReference<HashMap<String, List<GoodwillSchema>>>()
                        {
                        });
                     List<GoodwillSchema> goodwillSchemata = map.get("types");
 
                     reader.close();
 
                     return goodwillSchemata;
                 }
 
                 @Override
                 public void onThrowable(Throwable t)
                 {
                     log.warn(t);
                 }
             });
         }
         catch (IOException e) {
             log.warn(String.format("Error getting Schema list from %s:%d", host, port));
             return null;
         }
     }
 }
