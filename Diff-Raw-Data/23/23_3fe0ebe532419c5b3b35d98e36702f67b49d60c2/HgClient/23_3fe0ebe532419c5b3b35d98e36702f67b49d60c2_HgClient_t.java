 package com.thoughtworks.healthgraphexplorer.hgclient;
 
 import android.net.Uri;
 
 import com.github.kevinsawicki.http.HttpRequest;
 import com.google.gson.Gson;
 import com.thoughtworks.healthgraphexplorer.hgclient.exceptions.AccessTokenRenewalException;
 import com.thoughtworks.healthgraphexplorer.hgclient.models.AccessTokenRenewalResponse;
 
 import org.apache.http.HttpStatus;
 
 import java.util.HashMap;
 
 import roboguice.util.Strings;
 
 public class HgClient {
     private static final String AUTH_BASE_URL = "https://runkeeper.com/apps";
     private static final String AUTH_TOKEN_URL = AUTH_BASE_URL + "/authorize";
     private static final String ACCESS_TOKEN_URL = AUTH_BASE_URL + "/token";
 
     private static final String API_ROOT_URL = "https://api.runkeeper.com/";
 
     private static final String ACCEPT_USER = "application/vnd.com.runkeeper.User+json";
     private static final String CONTENT_NEW_WEIGHT = "application/vnd.com.runkeeper.NewWeightSet+json";
 
     private final String clientId;
     private final String clientSecret;
     private final String redirectUri;
 
     private String authCode;
     private String accessToken;
 
     public HgClient(String clientId, String clientSecret, String redirectUri) {
         this.clientId = clientId;
         this.clientSecret = clientSecret;
         this.redirectUri = redirectUri;
     }
 
     public void setAuthCode(String authCode) {
         this.authCode = authCode;
     }
 
     public boolean isAuthorized() {
         return Strings.notEmpty(this.authCode);
     }
 
     public void postWeight(Double weight) throws AccessTokenRenewalException {
         renewAccessToken();
 
         String authHeader = "Bearer " + accessToken;
         HttpRequest request = HttpRequest
                 .get(API_ROOT_URL + "/user")
                 .accept(ACCEPT_USER)
                 .header(HttpRequest.HEADER_AUTHORIZATION, authHeader);
 
         int code = request.code();
         String body = request.body();
         if (code != HttpStatus.SC_OK) {
             throw new AccessTokenRenewalException("Response code: " + code + ", body: " + body);
         }
 
         Gson gson = new Gson();
         HashMap<String, String> hashMap = gson.fromJson(body, HashMap.class);
 
         String weightEndPoint = hashMap.get("weight");
 
         HashMap<String, String> weightInput = new HashMap<String, String>();
         weightInput.put("timestamp", "Sat, 1 Jun 2013 00:00:00");
         weightInput.put("weight", weight.toString());
 
         HttpRequest send = HttpRequest.post(API_ROOT_URL + weightEndPoint)
                 .contentType(CONTENT_NEW_WEIGHT)
                 .header(HttpRequest.HEADER_AUTHORIZATION, "Bearer " + accessToken)
                 .send(gson.toJson(weightInput));
 
         code = send.code();
         body = send.body();
         if (code != HttpStatus.SC_OK) {
             throw new AccessTokenRenewalException("Response code: " + code + ", body: " + body);
         }
     }
 
     private void renewAccessToken() throws AccessTokenRenewalException {
         HttpRequest response = HttpRequest.post(ACCESS_TOKEN_URL)
                 .send("grant_type=authorization_code"
                         + "&code=" + authCode
                         + "&client_id=" + clientId
                         + "&client_secret=" + clientSecret
                         + "&redirect_uri=" + Uri.encode(redirectUri));
 
         int code = response.code();
         String body = response.body();
 
         if (code != HttpStatus.SC_OK) {
             throw new AccessTokenRenewalException("Response code: " + code + ", body: " + body);
         }
 
         AccessTokenRenewalResponse accessTokenRenewalResponse =
                 new Gson().fromJson(body, AccessTokenRenewalResponse.class);
 
         accessToken = accessTokenRenewalResponse.getAccessToken();
     }
 
     public Uri getAuthIntentUri() {
         return Uri.parse(AUTH_TOKEN_URL + "?response_type=code&client_id=" + this.clientId + "&redirect_uri="
                 + Uri.encode(this.redirectUri));
     }
 
     public void processAuthCallback(Uri uri) {
         if (uri != null) {
             String code = uri.getQueryParameter("code");
 
             if (code != null && !code.isEmpty() && !code.contains("unauthorized")) {
                 setAuthCode(code);
             }
         }
     }
 
     public String getAuthCode() {
         return authCode;
     }
 
     public String getWeightList() {
         try {
             renewAccessToken();
         } catch (AccessTokenRenewalException e) {
            return e.getLocalizedMessage();
         }
         return "blaaaah";
     }
 }
