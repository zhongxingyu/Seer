 /*
 * Author:: Joe Williams (j@boundary.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
 
 /*
 * some of this was based on https://github.com/jenkinsci/hudson-notifo-plugin
 */
 
 package hudson.plugins.boundary;
 
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.Hudson;
 
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.PostMethod;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ArrayList;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import java.io.IOException;
 
 public class Boundary
 {
     private final String BOUNDARY_URI = "https://api.boundary.com/";
     private HttpClient client;
     private String id;
     private String token;
     private String body;
 
     public Boundary() {}
 
     public Boundary( String id, String token )
     {
         this.id = id;
         this.token = token;
     }
 
     public void sendEvent(AbstractBuild<?, ?> build)
     {
 
         HashMap<String, Object> event = new HashMap<String, Object>();
 
        event.put("fingerprintFields", Arrays.asList(new String[]{build.getProject().getName(), "Jenkins Build", build.getDisplayName()}));
 
         Map<String, String> source = new HashMap<String, String>();
         source.put("ref", "jenkins");
         source.put("type", "build server");
         event.put("source", source);
 
         Map<String, String> properties = new HashMap<String, String>();
         properties.put("status", build.getResult().toString());
         properties.put("build number", build.getDisplayName());
         event.put("properties", properties);
 
         event.put("title", String.format("Jenkins Build Job - %s", build.getProject().getName()));
 
         ObjectMapper mapper = new ObjectMapper();
         String jsonOutput = new String();
 
         try {
             jsonOutput = mapper.writeValueAsString(event);
             System.out.println(jsonOutput);
         }
         catch(IOException ioe) {
             System.out.println("json error: " + ioe);
         }
 
         createClient(  );
 
         PostMethod post = new PostMethod( BOUNDARY_URI + id + "/" + "events");
 
         post.addRequestHeader("Content-Type", "application/json");
         post.setRequestBody( jsonOutput );
 
         try
             {
                 System.out.println(client.executeMethod( post ));
             }
 
         catch ( Exception e )
             {
                 System.out.println( "Unable to send message to Boundary API: \n" + e);
             }
 
         finally
             {
                 post.releaseConnection(  );
             }
     }
 
     private void createClient(  )
     {
         client = new HttpClient(  );
 
         Credentials defaultcreds = new UsernamePasswordCredentials( token, "" );
         client.getState(  ).setCredentials( AuthScope.ANY, defaultcreds );
         client.getParams(  ).setAuthenticationPreemptive( true );
     }
 }
