 /*
  * Copyright 2010 Ning, Inc.
  *
  * Ning licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 
 package com.ning.metrics.goodwill.access;
 
 import com.ning.http.client.AsyncCompletionHandler;
 import com.ning.http.client.AsyncHttpClient;
 import com.ning.http.client.AsyncHttpClientConfig;
 import com.ning.http.client.Response;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.Future;
 
 public class GoodwillAccessor
 {
     private static final Logger log = Logger.getLogger(GoodwillAccessor.class);
 
     private final static ObjectMapper mapper = new ObjectMapper();
 
     private final AsyncHttpClient client;

     private final String host;
     private final int port;
     private String url;
 
 
     public GoodwillAccessor(String host, int port)
     {
         this.host = host;
         this.port = port;
 
         this.url = String.format("http://%s:%d/registrar", host, port);
 
         client = createHttpClient();
     }
 
     // note: if called from base-class constructor, couldn't sub-class; hence just make static
     private static AsyncHttpClient createHttpClient()
     {
         // Don't limit the number of connections per host
         // See https://github.com/ning/async-http-client/issues/issue/28
         AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
         builder.setMaximumConnectionsPerHost(-1);
         return new AsyncHttpClient(builder.build());
     }
 
     /**
      * Get the Goodwill schema associated to a schema name.
      * </p>
      * Typical invocation:
      * <pre>
      * try {
      *   GoodwillSchema type = accessor.getSchema("test").get();
      * ...
      * }
      * catch (Exception e) {
      *   // Connection exception? Goodwill server down?
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
 
                     InputStream in = response.getResponseBodyAsStream();
                     try {
                         return mapper.readValue(in, GoodwillSchema.class);
                    }
                    finally {
                         closeStream(in);
                     }
                 }
 
                 @Override
                 public void onThrowable(Throwable t)
                 {
                     log.warn(t);
                 }
             });
         }
         catch (IOException e) {
             log.warn(String.format("Error getting Schema list from %s:%d (%s)", host, port, e.getLocalizedMessage()));
             return null;
         }
     }
 
     /**
      * Get all schemata.
      * <p/>
      * Use schemata, instead of schemas, which is closer to the original σχήματα.
      *
      * @return a future on a list of Schema
     * @see #getSchema(String) for an invocation example
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
 
                     InputStream in = response.getResponseBodyAsStream();
                     try {
                         HashMap<String, List<GoodwillSchema>> map = mapper.readValue(in,
                            new TypeReference<HashMap<String, List<GoodwillSchema>>>()
                            {
                            });
                         List<GoodwillSchema> goodwillSchemata = map.get("types");
                         return goodwillSchemata;
                    }
                    finally {
                         closeStream(in);
                     }
                 }
 
                 @Override
                 public void onThrowable(Throwable t)
                 {
                     log.warn(t);
                 }
             });
         }
         catch (IOException e) {
             log.warn(String.format("Error getting Schema list from %s:%d (%s)", host, port, e.getLocalizedMessage()));
             return null;
         }
     }
 
     private final void closeStream(InputStream in)
     {
         if (in != null) {
             try {
                 in.close();
            }
            catch (IOException e) {
                 log.warn(String.format("Failed to close http-client - provided InputStream: %s", e.getLocalizedMessage()));
             }
         }
     }
 }
