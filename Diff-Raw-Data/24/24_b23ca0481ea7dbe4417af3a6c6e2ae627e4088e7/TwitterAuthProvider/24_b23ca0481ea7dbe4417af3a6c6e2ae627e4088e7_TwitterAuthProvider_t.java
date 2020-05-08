 package com.digt.auth;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.json.JSONObject;
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Token;
 import org.scribe.model.Verb;
 import org.scribe.model.Verifier;
 import org.scribe.oauth.OAuthService;
 
 import com.digt.auth.helpers.TwitterApiWrapper;
 import com.digt.util.HttpUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
 import org.apache.rave.model.User;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 @Component(value=TwitterAuthProvider.AUTH_TYPE)
 public class TwitterAuthProvider extends AuthProvider {
 	
 	public static final String AUTH_TYPE = "twitter";
 
 	private static final Logger LOG = Logger.getLogger(
 			TwitterAuthProvider.class.getName());
 	
 	private String TW_DATA;
     private OAuthService service;
     private String clientId;
     private String clientSecret;
     
     @Autowired
     public TwitterAuthProvider(@Value("${com.digt.providers.twitter.clientid}") String clientId,
                                @Value("${com.digt.providers.twitter.clientsecret}") String clientSecret,
                                @Value("${com.digt.providers.twitter.data}") String data) {
         
         this.clientId = clientId;
         this.clientSecret = clientSecret;
         this.TW_DATA = data;
         this.authType = AUTH_TYPE;
     }
     
     @Override
     public User authorize(HttpServletRequest req, HttpServletResponse resp, String state) 
             throws AuthorizationException {
         
         try {
             String verifier = req.getParameter("oauth_verifier");
             Token requestToken;
             if (verifier == null) {
                initService(req, state);
                 requestToken = service.getRequestToken();
                 String authUrl = service.getAuthorizationUrl(requestToken);
                 resp.sendRedirect(authUrl);
                 return null;
             }
 
             Verifier v = new Verifier(verifier);
             requestToken = new Token(req.getParameter("oauth_token"), "");
             Token accessToken = service.getAccessToken(requestToken, v);
             
             return getUser(accessToken);
         } catch (Exception e) {
             LOG.log(Level.SEVERE, null, e);
             throw new AuthorizationException(e.getMessage());
         }
     }
     
     protected User getUser(Token accessToken) throws AuthorizationException {
         Response response = null;
         try {
             List<NameValuePair> qparams = new ArrayList<NameValuePair>();
             URLEncodedUtils.parse(qparams, new Scanner(accessToken.getRawResponse()), "UTF-8");
             String uid = null;
             String screenName = null;
             for (NameValuePair i : qparams)
             {
                 if (i.getName().equals("user_id"))
                     uid = i.getValue();
                 else if (i.getName().equals("screen_name"))
                     screenName = i.getValue();
             }
 
             OAuthRequest get = new OAuthRequest(Verb.GET, TW_DATA);
             get.addQuerystringParameter("user_id", uid);
             get.addQuerystringParameter("screen_name", screenName);
             service.signRequest(accessToken, get);
             response = get.send();
             if (!response.isSuccessful()) {
                 throw new AuthorizationException(response.getBody());
             }
 
             JSONObject jo = new JSONObject(response.getBody());
             String name = jo.optString("name");
             String[] names = name.replaceAll(" +", " ").split(" ");
 				String fName = (names.length > 0)? names[0] : name;
 				String lName = (names.length > 1)? names[1] : "";
 
             return HttpUtils.createUser(jo.getString("id"), null, null, fName, lName);
         } catch (Exception e) {
             throw new AuthorizationException(e.getMessage());
         } finally {
             if (response != null)
                 try {
                     response.getStream().close();
                 } catch (IOException ex) {}
         }
     }
     
    protected void initService(HttpServletRequest request, String state) 
            throws UnsupportedEncodingException {
 
         if (service == null) {
             service =  new ServiceBuilder()
                 .provider(TwitterApiWrapper.class)
                 .apiKey(clientId)
                 .apiSecret(clientSecret)
                 .callback(request.getScheme() + "://" + request.getServerName() +
                    ":" + request.getServerPort() + request.getRequestURI() 
                    + "?auth_type=" + AUTH_TYPE + "&state=" + URLEncoder.encode(state, "UTF-8"))
                 .build();
         }
     }
 }
