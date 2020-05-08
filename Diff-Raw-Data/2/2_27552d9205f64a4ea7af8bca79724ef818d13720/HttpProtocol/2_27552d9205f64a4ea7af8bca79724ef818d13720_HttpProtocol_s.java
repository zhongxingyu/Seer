 /*
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
  */
 package com.trickl.crawler.protocol.http;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.droids.api.ManagedContentEntity;
 import org.apache.droids.api.Protocol;
 import org.apache.droids.norobots.ContentLoader;
 import org.apache.droids.norobots.NoRobotClient;
 import org.apache.droids.norobots.NoRobotException;
 import org.apache.droids.protocol.http.DroidsHttpClient;
 import org.apache.droids.protocol.http.HttpClientContentLoader;
 import org.apache.droids.protocol.http.HttpContentEntity;
 import org.apache.http.*;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.HttpResponseException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.CoreProtocolPNames;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class HttpProtocol implements Protocol {
 
   private final Logger log = LoggerFactory.getLogger(HttpProtocol.class);
 
   private final HttpClient httpclient;
   private final ContentLoader contentLoader;
   
   private boolean forceAllow = false;
   private String method = HttpGet.METHOD_NAME;
   private Map<String, Object> postData = new HashMap<String, Object>();
   private Map<String, String> headerData = new HashMap<String, String>();
   private String userAgent = "Apache-Droids/1.1 (java 1.5)";
 
   public HttpProtocol(final HttpClient httpclient) {
     super();
     this.httpclient = httpclient;
     this.httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
     this.contentLoader = new HttpClientContentLoader(httpclient);
   }
   
   public HttpProtocol() {
     this(new DroidsHttpClient());
   }
 
   @Override
   public ManagedContentEntity load(URI uri) throws IOException {    
     HttpRequestBase httpRequest = null;
     
     if (method.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
         HttpPost httpPost = new HttpPost(uri);
        
         // Add header data
         for (Map.Entry<String, String> headerDataEntry : headerData.entrySet()) {
            httpPost.setHeader(headerDataEntry.getKey(), headerDataEntry.getValue());   
         }
         
         // Add post data
         String contentType = headerData.get("Content-Type");
         if (contentType == null || "application/x-www-form-urlencoded".equalsIgnoreCase(contentType)) {           
             List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
             for (Map.Entry<String, Object> postDataEntry : postData.entrySet()) {
                   nameValuePairs.add(new BasicNameValuePair(postDataEntry.getKey(), postDataEntry.getValue().toString()));        
             }
             httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
         }
         else if ("application/json".equalsIgnoreCase(contentType)) {
            ObjectMapper mapper = new ObjectMapper();         
            StringEntity se;
             try {
                String jsonString = mapper.writeValueAsString(postData);
                se = new StringEntity(jsonString);
                httpPost.setEntity(se);
             } catch (JsonGenerationException ex) {
                log.error("Failed to generate JSON.", ex);
             } catch (JsonMappingException ex) {
                log.error("Failed to generate JSON.", ex);
             }
         }
         httpRequest = httpPost;
     }
     else {
        httpRequest = new HttpGet(uri);
     }
     
     HttpResponse response = httpclient.execute(httpRequest);
     StatusLine statusline = response.getStatusLine();
     if (statusline.getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
       httpRequest.abort();
       throw new HttpResponseException(
           statusline.getStatusCode(), statusline.getReasonPhrase());
     }
     HttpEntity entity = response.getEntity();
     if (entity == null) {
       // Should _almost_ never happen with HTTP GET requests.
       throw new ClientProtocolException("Empty entity");
     }
     long maxlen = httpclient.getParams().getLongParameter(DroidsHttpClient.MAX_BODY_LENGTH, 0);
     return new HttpContentEntity(entity, maxlen);
   }
 
   @Override
   public boolean isAllowed(URI uri) throws IOException {
     if (forceAllow) {
       return forceAllow;
     }
 
     URI baseURI;
     try {
       baseURI = new URI(
           uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), 
           "/", null, null);
     } catch (URISyntaxException ex) {
       log.error("Unable to determine base URI for " + uri);
       return false;
     }
     
     NoRobotClient nrc = new NoRobotClient(contentLoader, userAgent);
     try {
       nrc.parse(baseURI);
     } catch (NoRobotException ex) {
       log.error("Failure parsing robots.txt: " + ex.getMessage());
       return false;
     }
     boolean test = nrc.isUrlAllowed(uri);
     if (log.isInfoEnabled()) {
       log.info(uri + " is " + (test ? "allowed" : "denied"));
     }
     return test;
   }
 
   public String getUserAgent() {
     return userAgent;
   }
 
   public void setUserAgent(String userAgent) {
     this.userAgent = userAgent;
     this.httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
   }
 
   /**
    * You can force that a site is allowed (ignoring the robots.txt). This should
    * only be used on server that you control and where you have the permission
    * to ignore the robots.txt.
    * 
    * @return <code>true</code> if you are rude and ignore robots.txt.
    *         <code>false</code> if you are playing nice.
    */
   public boolean isForceAllow() {
     return forceAllow;
   }
 
   /**
    * You can force that a site is allowed (ignoring the robot.txt). This should
    * only be used on server that you control and where you have the permission
    * to ignore the robots.txt.
    * 
    * @param forceAllow
    *                if you want to force an allow and ignore the robot.txt set
    *                to <code>true</code>. If you want to obey the rules and
    *                be polite set to <code>false</code>.
    */
   public void setForceAllow(boolean forceAllow) {
     this.forceAllow = forceAllow;
   }
   
   protected HttpClient getHttpClient() {
     return this.httpclient;
   }
 
    /**
     * @return the method
     */
    public String getMethod() {
       return method;
    }
 
    /**
     * @param method the method to set
     */
    public void setMethod(String method) {
       this.method = method;
    }
 
    /**
     * @return the postData
     */
    public Map<String, Object> getPostData() {
       return postData;
    }
 
    /**
     * @param postData the postData to set
     */
    public void setPostData(Map<String, Object> postData) {
       this.postData = postData;
    }
 
   public void setHeaderData(HashMap<String, String> headerData) {
       this.headerData = headerData;
    }
 }
