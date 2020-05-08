 package com.janrain.backplaneclient;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * @author Tom Raney
  */
 public class AccessTokenManager {
 
     public static AccessToken getAccessToken(ClientCredentials clientCredentials, String scope)
             throws UnauthorizedScopeException, BackplaneClientException {
         return getAccessToken(clientCredentials, scope, "client_credentials", null);
     }
 
 
     public static AccessToken refreshAccessToken(ClientCredentials clientCredentials, String scope)
             throws UnauthorizedScopeException, BackplaneClientException {
         return getAccessToken(clientCredentials, scope, "refresh_token", clientCredentials.getAccessToken());
     }
 
 
     public static AccessToken getAccessToken(ClientCredentials clientCredentials, String scope, String grantType, AccessToken accessToken)
             throws UnauthorizedScopeException, BackplaneClientException {
 
         HttpClient httpClient = getHttpClient();
 
        PostMethod httpMethod = new PostMethod(clientCredentials.getBackplansServerUrl() + "/v2/token");
         httpMethod.addRequestHeader("Authorization", "Basic " +
                 createBasicAuth(clientCredentials.getClientId(), clientCredentials.getClientSecret()));
         if (scope != null) {
             httpMethod.setQueryString("?grant_type=" + grantType + "&scope=" + scope);
         } else {
             httpMethod.setQueryString("?grant_type=" + grantType);
         }
         if (accessToken != null && grantType.equals("refresh_token")) {
             httpMethod.addParameter("refresh_token", accessToken.getRefresh_token());
         }
 
         try {
             String response = makeCall(httpClient, httpMethod);
             if (httpMethod.getStatusCode() != 200) {
                 if (response.contains("unauthorized scope")) {
                     throw new UnauthorizedScopeException(response);
                 }
                 throw new BackplaneClientException();
             }
             return new ObjectMapper().readValue(response, new TypeReference<AccessToken>() {});
         } catch (IOException e) {
             throw new BackplaneClientException(e.getMessage());
         }
     }
 
     public static AccessToken getRegularAccessToken(String backplaneServerUrl, String bus)
             throws BackplaneClientException, UnauthorizedScopeException {
 
         HttpClient httpClient = getHttpClient();
 
         HttpMethod httpMethod = new GetMethod(backplaneServerUrl + "/v2/token");
         httpMethod.setQueryString("?callback=f&bus=" + bus);
 
         String json = makeCall(httpClient, httpMethod);
         // strip out callback wrapper
         json = json.substring(2, json.length()-2);
 
         try {
             if (httpMethod.getStatusCode() != 200) {
                 if (json.contains("unauthorized scope")) {
                     throw new UnauthorizedScopeException(json);
                 }
                 throw new BackplaneClientException();
             }
             return new ObjectMapper().readValue(json, new TypeReference<AccessToken>() {});
         } catch (IOException e) {
             throw new BackplaneClientException(e.getMessage());
         }
 
 
     }
 
     static String makeCall(HttpClient httpClient, HttpMethod httpMethod) {
 
         BufferedInputStream bis = null;
 
         try {
 
             httpClient.executeMethod(httpMethod);
             InputStream is = httpMethod.getResponseBodyAsStream();
             if (is != null) {
                 bis = new BufferedInputStream(is);
                 StringBuilder sb = new StringBuilder();
                 byte[] buffer= new byte[1024];
                 int bytesRead = 0;
                 while ( (bytesRead = bis.read(buffer)) > -1) {
                     sb.append(new String(buffer, 0, bytesRead));
                 }
 
                 String response = sb.toString();
                 logger.debug("backplane server response: " + response);
                 return response;
             }
 
         } catch (HttpException e) {
             logger.error(e);
         } catch (IOException e) {
             logger.error(e);
         } finally {
             if (bis != null) {
                 try {
                     bis.close();
                 } catch (IOException e) {
                     //ignore
                 }
             }
         }
 
         return null;
 
     }
 
     private static final int TIMEOUT = 5000;
     private static final Logger logger = Logger.getLogger(AccessTokenManager.class);
 
     private static HttpClient getHttpClient() {
         HttpClient httpClient = new HttpClient();
         httpClient.getHttpConnectionManager().getParams().setSoTimeout(TIMEOUT);
         httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(TIMEOUT);
         return httpClient;
     }
 
     private static String createBasicAuth(
             String clientId,
             String clientSecret) {
         return new String(Base64.encodeBase64((clientId + ":" + clientSecret).getBytes()));
     }
 }
