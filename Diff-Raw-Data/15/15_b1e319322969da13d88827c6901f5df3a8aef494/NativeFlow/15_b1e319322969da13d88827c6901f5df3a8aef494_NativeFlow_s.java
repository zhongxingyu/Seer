 package org.trbecker.oauth2.core;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.util.List;
 
 import org.apache.http.client.HttpClient;
 
 /**
  * This class implements the native flow that uses a embedded web browser.
  * 
  * @author trbecker
  * 
  */
 public abstract class NativeFlow extends OAuth2Client {
     private static final String REDIRECT_URI = "redirect_uri";
     private static final String CLIENT_SECRET = "client_secret";
    private static final String CODE = "code";
 
     private URI redirectUri;
     private String clientSecret;
 
     protected NativeFlow(HttpClient connector) {
         super(connector);
     }
 
     public void setRedirectUri(URI redirectUri) {
         this.redirectUri = redirectUri;
     }
 
     public URI getRedirectUri() {
         return redirectUri;
     }
 
     public URI getAuthorizationURI() throws URISyntaxException,
             UnsupportedEncodingException {
         StringBuilder paramBuilder = new StringBuilder();
         paramBuilder.append(REDIRECT_URI).append("=")
                 .append(URLEncoder.encode(redirectUri.toString(), "utf8"))
                 .append("&").append(CLIENT_ID).append("=")
                 .append(URLEncoder.encode(clientId, "utf8"));
 
         URI authorizationUri = new URI(authorizationEndpoint.toString() + "?"
                 + paramBuilder.toString());
 
         return authorizationUri;
     }
 
     public void setClientSecret(String clientSecret) {
         this.clientSecret = clientSecret;
     }
 
     @Override
     protected void onSetAccessTokenParams(List<URLEncodedVariable> params) {
         super.onSetAccessTokenParams(params);
         params.add(new URLEncodedVariable(CLIENT_SECRET, clientSecret));
        params.add(new URLEncodedVariable(CODE, authorizationCode));
     }
 
 }
