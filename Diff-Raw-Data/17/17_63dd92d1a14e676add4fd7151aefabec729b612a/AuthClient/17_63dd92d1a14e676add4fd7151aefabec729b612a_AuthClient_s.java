 /**
  *  Copyright 2012 Rackspace
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  */
 
 package com.rackspacecloud.client.service_registry.clients;
 
 import com.google.gson.Gson;
 import com.rackspacecloud.client.service_registry.Region;
 import com.rackspacecloud.client.service_registry.auth.AuthData;
 import com.rackspacecloud.client.service_registry.auth.Token;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 public class AuthClient {
     private String username;
     private String apiKey;
 
     private String region;
     private String authUrl;
     private HttpClient client;
 
     public Token authToken = null;
 
     private static final Map<String, String> DEFAULT_AUTH_URLS  = new HashMap<String, String>() {{
         put(Region.US, "https://identity.api.rackspacecloud.com/v2.0");
         put(Region.UK, "https://lon.identity.api.rackspacecloud.com/v2.0");
     }};
 
     private static Logger logger = Logger.getLogger(AuthClient.class);
 
     public AuthClient(HttpClient client, String username, String apiKey, String region) {
         this(client, username, apiKey, region, null);
     }
 
     public AuthClient(HttpClient client, String username, String apiKey, String region, String authUrl) {
         this.client = client;
         this.username = username;
         this.apiKey = apiKey;
         this.region = region; // todo: validate region.
 
         if (authUrl != null) {
             this.authUrl = authUrl;
         }
         else {
             if (!AuthClient.DEFAULT_AUTH_URLS.containsKey(region)) {
                 throw new IllegalArgumentException("Invalid region: " + region);
             }
 
             this.authUrl = AuthClient.DEFAULT_AUTH_URLS.get(region);
         }
     }
 
    public synchronized void refereshToken() throws Exception {
        this.refereshToken(false);
     }
 
    public synchronized void refereshToken(boolean force) throws Exception {
         Long now = new Date().getTime();
 
         if (force || (this.authToken == null || (this.authToken.getExpires() <= now))) {
             logger.debug("Token is not set or has expired, refreshing it...");
             this.authenticate();
         }
     }
 
     public synchronized boolean authenticate() throws Exception {
         // TODO: Move into separate keystone package
         HttpPost method = new HttpPost(this.authUrl + "/tokens");
         String payload = "{\"auth\": {\"RAX-KSKEY:apiKeyCredentials\": {\"username\":  \"" + this.username + "\", \"apiKey\": \"" + this.apiKey + "\"}}}";
         StringEntity payloadEntity = new StringEntity(payload);
         payloadEntity.setContentType("application/json");
         method.setEntity(payloadEntity);
 
         logger.debug(String.format("Authenticating against auth API server: authUrl=%s, username=%s, apiKey=%s",
                 this.authUrl, this.username, this.apiKey));
 
         HttpResponse response = this.client.execute(method);
         int statusCode = response.getStatusLine().getStatusCode();
 
         if (statusCode != 200 & statusCode != 203)  {
             throw new Exception("Unexpected status code: " + statusCode);
         }
 
         HttpEntity entity = response.getEntity();
         String data = EntityUtils.toString(entity);
 
         AuthData ad = new Gson().fromJson(data, AuthData.class);
         this.authToken = ad.getAccess().getToken();
 
         return true;
     }
 }
