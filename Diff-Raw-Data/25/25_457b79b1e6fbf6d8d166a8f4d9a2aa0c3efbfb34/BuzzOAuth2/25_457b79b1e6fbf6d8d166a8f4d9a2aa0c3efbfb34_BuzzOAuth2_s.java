 package com.google.buzz.oauth;
 
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.net.URI;
 import java.net.URISyntaxException;
 import javax.net.ssl.HttpsURLConnection;
 import java.io.UnsupportedEncodingException;
 import java.util.Map;
 import java.util.TreeMap;
 import org.json.simple.JSONValue;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.ParseException;
 import com.google.buzz.exception.BuzzAuthenticationException;
 import com.google.buzz.exception.BuzzIOException;
 import com.google.buzz.io.BuzzIO;
 
 /**
  * implementation of OAuth2 API. 
  * 
  * @author ruslan@shevchenko.kiev.ua
  */
 public class BuzzOAuth2 implements BuzzOAuth
 {
     /**
      * OAuth google endpoint to retrieve an access token
      */
     public static final String OAUTH2_DIALOG_URL = "https://accounts.google.com/o/oauth2/auth";
 
     public static final String AUTHORIZATION_CODE = "authorization_code";
     public static final String REFRESH_TOKEN = "refresh_token";
 
     private String        refreshToken;
     private String        accessToken;
     private long          expireTime;
     private String        consumerKey;
     private String        consumerSecret;
 
     public  int           getOAuthVersion()
      { return 2; }
 
     /**
      * This page is going to be used by the user to allow third parties applications access his/her
      * Google Buzz information and activities.
      * 
      * @param scope either BUZZ_SCOPE_READONLY or BUZZ_SCOPE_WRITE
      * @param consumerKey to retrieve the request token
      * @param consumerSecret to retrieve the request token
      * @param callbackUrl the url google should redirect the user after a successful login
      * @return the authentication url for the user to log in
      */
     public String getAuthenticationUrl( String scope, String consumerKey,
                                         String consumerSecret, String callbackUrl )
         throws BuzzAuthenticationException
     {
         String authUrl = null;
         Map<String,String> params = new TreeMap<String,String>();
         params.put("client_id",consumerKey);
         this.consumerKey = consumerKey;
         this.consumerSecret = consumerSecret;
         params.put("redirect_uri",callbackUrl);
         params.put("scope",scope);
         params.put("response_type","code");
         authUrl = createGetUrl(OAUTH2_DIALOG_URL,params);
         return authUrl;
     }
 
     public void  retrieveAccessToken(String code, String redirectUri)
         throws BuzzAuthenticationException
     {
      doAccessTokenRequest(code,redirectUri);
     }
 
     public void  doAccessTokenRequest(String code, String redirectUri)
         throws BuzzAuthenticationException
     {
      doTokenRequest(AUTHORIZATION_CODE,code,redirectUri);
     }
 
     public void  doRefreshTokenRequest()
         throws BuzzAuthenticationException
     {
      doTokenRequest(REFRESH_TOKEN,null,null);
     }
 
     private void doTokenRequest(String grant_type, String code, String redirectUri)
         throws BuzzAuthenticationException
     {
         try {
           HttpsURLConnection cn = BuzzIO.createRequest(
                                   "https://accounts.google.com/o/oauth2/token",
                                   BuzzIO.HTTP_METHOD_POST);
           cn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
           StringBuilder sb = new StringBuilder();
           if (code!=null) {
            sb.append("code=").append(URLEncoder.encode(code,"utf8")).append("&");
           }
          sb.append("cliend_id=").append(URLEncoder.encode(consumerKey,"utf8")).append("&")
          .append("client_secret=").append(URLEncoder.encode(consumerSecret,"uft8")).append("&");
           if (redirectUri!=null) {
            sb.append("redirect_uri=").append(URLEncoder.encode(redirectUri,"utf8")).append("&");
           }
           sb.append("grant_type=").append(grant_type);
           BuzzIO.addBody(cn,sb.toString());
           String sr = BuzzIO.send(cn);
           Object oJsonObject = JSONValue.parseWithException(sr);
           JSONObject jsonObject=null;
           if (oJsonObject instanceof JSONValue) {
              jsonObject = (JSONObject)oJsonObject;
           } else {
              throw new IllegalArgumentException("received reply for authorization token is not complex json:"+oJsonObject);
           }
           Object oAccessToken = jsonObject.get("access_token");
           Object oRefreshToken = jsonObject.get("refresh_token");
           Object oExpiresIn = jsonObject.get("expires_in");
           if (oAccessToken!=null) {
             this.accessToken = (String)oAccessToken;
           }
           if (oRefreshToken!=null) {
             this.refreshToken = (String)oRefreshToken;
           }
           if (oExpiresIn!=null) {
             long now = System.currentTimeMillis();
             this.expireTime = now + ((Integer)oExpiresIn)*1000L; 
           }
         } catch(BuzzIOException ex) {
            throw new BuzzAuthenticationException("Can't retrieve token:"+ex.getMessage(),ex);
         } catch(IllegalArgumentException ex) {
            throw new BuzzAuthenticationException("Can't retrieve token:"+ex.getMessage(),ex);
         } catch(UnsupportedEncodingException ex) {
           throw new BuzzAuthenticationException("Can't retrieve token:"+ex.getMessage(),ex);
         } catch(ParseException ex) {
            throw new BuzzAuthenticationException("Can't retrieve token:"+ex.getMessage(),ex);
         }
     }
 
 
     public void setConsumerForScope(String consumerKey, 
                                     String consumerSecret,
                                     String scope) 
         throws BuzzAuthenticationException
     {
       this.consumerKey=consumerKey;
       this.consumerSecret=consumerSecret;
     }
 
     /**
      * Retrieves the access token that will be used to sign requests by the consumer.<br/>
      * 
      * @param accessToken to sign requets
      * @throws BuzzAuthenticationException if any OAuth error occurs
      */
     public void setAccessToken(String accessToken )
         throws BuzzAuthenticationException
     {
        this.accessToken = accessToken;
     }
     
     public void setRefreshToken(String refreshToken)
     {
       this.refreshToken = refreshToken;
     }
 
     /**
      * set expire time in milliseconds.
      **/
     public void setExpireTime(long expireTime)
     {
       this.refreshToken = refreshToken;
     }
 
     /**
      * Set the token and secret to be used to authentication procedure.<br/>
      * 
      * @param accessToken
      * @param tokenSecret - refreshToken
      * @throws BuzzAuthenticationException if any OAuth error occurs
      */
     public void setTokenWithSecret(String accessToken, String tokenSecret) {
 		this.accessToken=accessToken;
                 this.refreshToken=tokenSecret;
     }
 
     /**
      * Sign the request to be send. <br/>
      * <b>BuzzOAuth.setAccessToken</b> method should be called before this method.
      * 
      * @param request to be signed with the access token
      * @throws BuzzAuthenticationException if an OAuth problem occurs
      */
     public void signRequest( URLConnection request )
         throws BuzzAuthenticationException
     {
        long millisNow = System.currentTimeMillis();
        if (expireTime-millisNow < (300L*1000)) {
           doRefreshTokenRequest();          
        }
        request.setRequestProperty("Authorization","OAuth "+accessToken);
     }
 
     private String createGetUrl(String baseUrl,Map<String,String> params)
     {
      StringBuilder sb = new StringBuilder();
      sb.append(baseUrl).append('?');
      boolean frs=true;
      for(Map.Entry<String,String> e : params.entrySet()) {
         if (!frs) {
          sb.append("&");
         } else {
          frs=false;
         }
         sb.append(e.getKey()).append("=");
         try {
            String encoded = new URI(null,null,null,e.getValue(),null).
                                             toASCIIString().substring(1);
            sb.append(encoded);
         } catch (URISyntaxException ex) {
            // impossible.
            throw new IllegalArgumentException("Can't code parameter "+e.getValue(),ex);
         }
      }
      return sb.toString();
     }
 
 }
