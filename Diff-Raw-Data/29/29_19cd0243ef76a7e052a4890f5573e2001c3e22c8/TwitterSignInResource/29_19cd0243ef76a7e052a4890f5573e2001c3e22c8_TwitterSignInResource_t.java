 package eu.piotrbuda.twittory.signin;
 
 import eu.piotrbuda.twittory.storage.AccessTokenStorage;
 import org.springframework.stereotype.Component;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 
 import javax.annotation.Resource;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.NewCookie;
 import javax.ws.rs.core.Response;
 import java.net.URI;
 
 /**
  * .
  */
 @Path("/twitter")
 @Component
 public class TwitterSignInResource {
 
     @GET
     @Path("signin")
     public Response signIn(@Context HttpServletRequest request) {
         for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("accesstoken")) {
                 return redirectToTwittoryMainPage();
             }
         }
         try {
             String callback = createCallbackUrl(request);
             RequestToken requestToken = twitter.getOAuthRequestToken(callback);
             request.getSession(true).setAttribute("requestToken", requestToken);
             return redirectToTwitterForAuthentication(requestToken);
         } catch (TwitterException te) {
             throw new WebApplicationException(te);
         } catch (IllegalStateException ise) {
             return redirectToTwittoryMainPage();
         }
     }
 
     private Response redirectToTwitterForAuthentication(RequestToken requestToken) {
         return Response.status(302).location(URI.create(requestToken.getAuthenticationURL())).build();
     }
 
     private Response redirectToTwittoryMainPage() {
         return Response.seeOther(URI.create("../twittory.html")).build();
     }
 
     private String createCallbackUrl(HttpServletRequest request) {
         String requestUrl = request.getRequestURL().toString();
         String requestUri = request.getRequestURI();
         String server = requestUrl.substring(0, requestUrl.indexOf(requestUri));
         return server + "/api/twitter/callback";
     }
 
     @GET
     @Path("callback")
     public Response callback(@Context HttpServletRequest request,
                              @QueryParam("oauth_token") String oauth_token,
                              @QueryParam("oauth_verifier") String oauth_verifier) {
         RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
         if (requestToken == null) {
             throw new WebApplicationException(400);
         }
         try {
             AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, oauth_verifier);
             storeAccessToken(accessToken);
             return Response
                     .seeOther(URI.create("../twittory.html"))
                     .cookie(new NewCookie("accesstoken", accessToken.getToken()))
                     .build();
         } catch (TwitterException e) {
             throw new WebApplicationException(e);
         }
     }
 
     private void storeAccessToken(AccessToken accessToken) {
         storage.storeAccessToken(accessToken);
     }
 
 
     private Twitter twitter;
 
     @Resource
     public void setTwitter(Twitter twitter) {
         this.twitter = twitter;
     }
 
     private AccessTokenStorage storage;
 
     @Resource
     public void setStorage(AccessTokenStorage storage) {
         this.storage = storage;
     }
 }
