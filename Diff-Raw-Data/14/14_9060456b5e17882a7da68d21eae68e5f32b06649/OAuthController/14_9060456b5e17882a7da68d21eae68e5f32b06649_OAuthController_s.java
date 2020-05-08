 package se.kudomessage.torsken;
 
 import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
 import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
 import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.jackson.JacksonFactory;
 import java.io.IOException;
 import javax.faces.bean.ManagedBean;
 import javax.faces.context.FacesContext;
 
 @ManagedBean
 public class OAuthController {
    
    private String accessToken;
     
     private static final String CALLBACK_URL = Constants.REDIRECT_URI;
     private static final HttpTransport TRANSPORT = new NetHttpTransport();
     private static final JsonFactory JSON_FACTORY = new JacksonFactory();
     private static final String CLIENT_ID = Constants.CLIENT_ID;
     private static final String CLIENT_SECRET = Constants.CLIENT_SECRET;
     
     public void redirectToGoogleOAuth () throws IOException {
         FacesContext.getCurrentInstance().getExternalContext().redirect("https://accounts.google.com/o/oauth2/auth?redirect_uri="+ Constants.REDIRECT_URI +"&response_type=code&client_id=" + Constants.CLIENT_ID + "&approval_prompt=force&scope=https%3A%2F%2Fmail.google.com+https%3A%2F%2Fwww.google.com%2Fm8%2Ffeeds%2F+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email");
     }
     
    public String geaTokenFromCode(String code) throws IOException {
         GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant authRequest = 
                 new GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant(
                 TRANSPORT, JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, code, CALLBACK_URL);
         authRequest.useBasicAuthorization = false;
         AccessTokenResponse authResponse = authRequest.execute();
        accessToken = authResponse.accessToken;
        GoogleAccessProtectedResource access = new GoogleAccessProtectedResource(
                accessToken, TRANSPORT, JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, authResponse.refreshToken);
        TRANSPORT.createRequestFactory(access);
        
         return authResponse.accessToken;
     }
 
     public String getAccessToken() {
         return accessToken;
     }
     
 }
