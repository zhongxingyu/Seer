 package org.yajug.users.servlets.auth;
 
 import java.io.IOException;
 import java.util.Arrays;
 
import org.apache.commons.lang.StringUtils;
 import org.yajug.users.domain.User;
 
 import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
 import com.google.api.client.auth.oauth2.Credential;
 import com.google.api.client.auth.oauth2.CredentialStore;
 import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
 import com.google.api.client.http.GenericUrl;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpRequestFactory;
 import com.google.api.client.http.HttpResponse;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.json.JsonFactory;
 import com.google.gson.FieldNamingPolicy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.inject.name.Named;
 
 /**
  * Helps you to authenticate a user using the Google OAuth 2 API. 
  * 
  * @see https://developers.google.com/accounts/docs/OAuth2
  * 
  * @author Bertrand Chevrier <bertrand.chevrier@yajug.org>
  */
 @Singleton
 public class GoogleOAuthHelper {
 
 	
 	private final static String[] SCOPES = {
 		"https://www.googleapis.com/auth/userinfo.profile",
 		"https://www.googleapis.com/auth/userinfo.email"
 	};
 	
 	private final static String USER_INFOS_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
 	
 	@Inject @Named("auth.clientid")  	private String clientId;
 	@Inject @Named("auth.clientsecret") private String clientSecret;
 	@Inject @Named("auth.redirecturi")  private String redirectUri;
 	@Inject @Named("auth.domain") private String domain;
 	
 	@Inject private CredentialStore credentialStore;
 	@Inject private HttpTransport httpTransport;
 	@Inject private JsonFactory jsonFactory;
 	
 	
 	/**
 	 * Build a {@link AuthorizationCodeFlow} instance used by a servlet 
 	 * to do the OAuth job for us.
 	 * @return the AuthorizationCodeFlow
 	 */
 	public AuthorizationCodeFlow getAuthorizationCodeFlow(){
 		return new GoogleAuthorizationCodeFlow.Builder(
 				httpTransport, 
 				jsonFactory,
 				clientId, 
 				clientSecret,
 		        Arrays.asList(SCOPES)
 	        )
 			.setCredentialStore(credentialStore)
 	        .build();
 	}
 	
 	public User getUser(Credential credential) throws IOException{
 		
 		User user = null;
 		
 		
 		GenericUrl url = new GenericUrl(USER_INFOS_URL);
 		url.set("access_token", credential.getAccessToken());
 		
 		HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
 		HttpRequest request = requestFactory.buildGetRequest(url);
 		
 		HttpResponse response = request.execute();
 		if(response.getStatusCode() == 200){
 			String json = response.parseAsString();
 			if(StringUtils.isNotBlank(json)){
 				Gson serializer = new GsonBuilder()
 										.serializeNulls()
 										.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
 										.create();
 				
 				user = serializer.fromJson(json, User.class);
 			}
 		}
 		
 		return user;
 	}
 	
 	public boolean isAllowed(User user){
 		return user != null 
 				&& user.isVerifiedEmail() 
 				&& domain.equals(user.getHd());
 	}
 	
 	public String getRedirectUri() {
 		return redirectUri;
 	}
 }
