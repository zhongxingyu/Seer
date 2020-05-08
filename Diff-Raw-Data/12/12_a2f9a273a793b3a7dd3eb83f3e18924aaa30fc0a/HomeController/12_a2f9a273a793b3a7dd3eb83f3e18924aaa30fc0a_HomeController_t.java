 package org.springframework.social.appdotnet.example;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.social.appdotnet.api.Appdotnet;
 import org.springframework.social.appdotnet.api.data.post.ADNPost;
 import org.springframework.social.appdotnet.connect.AppdotnetConnectionFactory;
 import org.springframework.social.connect.Connection;
 import org.springframework.social.connect.ConnectionData;
 import org.springframework.social.oauth2.AccessGrant;
 import org.springframework.social.oauth2.GrantType;
 import org.springframework.social.oauth2.OAuth2Parameters;
 import org.springframework.social.support.URIBuilder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.CookieValue;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletResponse;
 import java.util.List;
 
 /**
  * @author Arik Galansky
  */
 @Controller
 public class HomeController {
     public static final String SESSION = "SESSION";
     public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
     Logger logger = LoggerFactory.getLogger(HomeController.class);
     // TODO Arikg: init params
    private String clientId = "gMXzGEPv6wV7unatUXJ2pFdg52UADQxq";
    private String clientSecret = "WrWs4r3wVg5EfA9BadpwQG2vNQUG4Ub6";
    private String redirectUrl = "http://79.182.3.171:8080/appdotnet-example/";
     private String scope = "stream,email,write_post,follow,messages";
 
     @RequestMapping("/")
     public ModelAndView start() {
         AppdotnetConnectionFactory connectionFactory = new AppdotnetConnectionFactory(clientId, clientSecret);
         OAuth2Parameters params = new OAuth2Parameters();
         params.add("scope", scope);
         params.add("redirect_uri", createRedirectUri(redirectUrl));
         String authUrl = connectionFactory.getOAuthOperations().buildAuthenticateUrl(GrantType.AUTHORIZATION_CODE, params);
         return new ModelAndView(new RedirectView(authUrl));
     }
 
     @RequestMapping(value = "/", params = "code")
     @ResponseBody
     public String oauth2Callback(@RequestParam("code") String code,
                                  HttpServletResponse response,
                                  Model model) {
         ConnectionData connectionData = completeConnection(code);
         if (connectionData != null) {
             Cookie sessionCookie = new Cookie(SESSION, connectionData.getProviderUserId());
             response.addCookie(sessionCookie);
             Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN, connectionData.getAccessToken());
             response.addCookie(accessTokenCookie);
             return connectionData.getDisplayName();
         }
         return null;
     }
 
     @RequestMapping("/stream")
     @ResponseBody
     public List<ADNPost> stream(@CookieValue(ACCESS_TOKEN) String accessToken) {
         if (accessToken != null) {
             Connection<Appdotnet> connection = getConnection(accessToken);
            return connection.getApi().postsOperations().getPersonalStream().getPosts();
         }
 
         return null;
     }
 
     @RequestMapping("/post")
     @ResponseBody
     public String post(@CookieValue(ACCESS_TOKEN) String accessToken,
                       @RequestParam("text") String text) {
         if (accessToken != null) {
             Connection<Appdotnet> connection = getConnection(accessToken);
             connection.getApi().postsOperations().create(text);
             return "sent new post: " + text;
         }
 
         return "no access token";
     }
 
     private Connection<Appdotnet> getConnection(String accessToken) {
         AppdotnetConnectionFactory connectionFactory = new AppdotnetConnectionFactory(clientId, clientSecret);
         return connectionFactory.createConnection(new AccessGrant(accessToken));
     }
 
     @RequestMapping(params = "error")
     public ModelAndView error(@RequestParam("error") String error,
                               @RequestParam("error_description") String errorDescription,
                               @RequestParam("error_uri") String errorUri) {
         StringBuilder sb = new StringBuilder().append("error: ").append(error)
                 .append(" error_description: ").append(errorDescription)
                 .append(" error_uri: ").append(errorUri);
         logger.error(sb.toString());
         ModelAndView mav = new ModelAndView("error");
         mav.getModel().put("error_msg", sb.toString());
         return mav;
     }
 
     public ConnectionData completeConnection(String code) {
         ConnectionData connection = null;
         if (code != null) {
             AppdotnetConnectionFactory connectionFactory = new AppdotnetConnectionFactory(clientId, clientSecret);
             AccessGrant accessGrant = connectionFactory.getOAuthOperations().exchangeForAccess(
                     code, redirectUrl, null);
             connection = connectionFactory.createConnection(accessGrant).createData();
         }
         return connection;
     }
 
     private String createRedirectUri(String baseUri) {
         return URIBuilder.fromUri(baseUri).build().toString();
     }
 }
