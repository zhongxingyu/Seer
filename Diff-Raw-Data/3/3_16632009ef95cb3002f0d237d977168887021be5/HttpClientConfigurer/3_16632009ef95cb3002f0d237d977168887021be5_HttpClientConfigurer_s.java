 package gnutch.http;
 
 import org.apache.http.HttpVersion;
 import org.apache.http.client.HttpClient;
 import org.apache.http.params.CoreProtocolPNames;
 
 public class HttpClientConfigurer implements org.apache.camel.component.http4.HttpClientConfigurer {
 
     private String userAgent;
 
     public HttpClientConfigurer(String userAgent) {
         this.userAgent = userAgent;
     }
 
     @Override
     public void configureHttpClient(HttpClient client) {
        client.getParams()
            .setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1)
         if (userAgent != null) {
             client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
         }
     }
 }
