 package com.janrain.backplaneclient;
 
 /**
  * @author Tom Raney
  */
 public class ClientCredentials {
 
     private String backplansServerUrl;
     private String clientId;
     private String clientSecret;
     private AccessToken accessToken;
 
     public ClientCredentials(String backplaneServerUrl, String clientId, String clientSecret) {
         this.backplansServerUrl = backplaneServerUrl;
         this.clientId = clientId;
         this.clientSecret = clientSecret;
     }
 
     public ClientCredentials(String backplansSeverUrl, String clientId, String clientSecret, AccessToken accessToken) {
         this(backplansSeverUrl, clientId, clientSecret);
         this.accessToken = accessToken;
     }
 
    public String getBackplaneServerUrl() {
         return backplansServerUrl;
     }
 
    public void setBackplaneServerUrl(String backplansServerUrl) {
         this.backplansServerUrl = backplansServerUrl;
     }
 
     public String getClientId() {
         return clientId;
     }
 
     public void setClientId(String clientId) {
         this.clientId = clientId;
     }
 
     public String getClientSecret() {
         return clientSecret;
     }
 
     public void setClientSecret(String clientSecret) {
         this.clientSecret = clientSecret;
     }
 
     public AccessToken getAccessToken() {
         return accessToken;
     }
 
     public void setAccessToken(AccessToken accessToken) {
         this.accessToken = accessToken;
     }
 }
