 package ru.gosuslugi.geps.ng.security;
 
 import org.springframework.security.authentication.AuthenticationServiceException;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
 import ru.gosuslugi.geps.ng.facebook.FacebookClient;
 import ru.gosuslugi.geps.ng.facebook.FacebookProfile;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: renatn
  * Date: 28.12.12
  * Time: 14:31
  */
 public class FbSignInFilter extends AbstractAuthenticationProcessingFilter {
 
     private String clientId;
     private String secret;
     private String siteUrl;
 
     protected FbSignInFilter() {
         super("/signin");
     }
 
     @Override
     public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse resp) throws AuthenticationException, IOException, ServletException {
         String code = req.getParameter("code");
         if (code == null || code.equals("")) {
             throw new AuthenticationServiceException("Facebook code is empty!");
         }
 
         FacebookClient client = new FacebookClient(clientId, secret, siteUrl);

        String token;
        try {
            token = client.requestAccessToken(code);
            if (token.startsWith("{")) {
                //TODO: log exception
                throw new AuthenticationServiceException("error on requesting token: " + token + " with code: " + code);
            }
        } catch (IOException e) {
            //TODO: log exception
            throw new AuthenticationServiceException("Error while request facebook access token");
        }

        FacebookProfile profile;
        try {
             profile = client.requestProfile(token);
        } catch (IOException e) {
            //TODO: log exception
            throw new AuthenticationServiceException("Error while request facebook user profile");
         }
 
         UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(profile.getName(), "N/A");
         authentication.setDetails(authenticationDetailsSource.buildDetails(req));
         return this.getAuthenticationManager().authenticate(authentication);
 
     }
 
     public String getClientId() {
         return clientId;
     }
 
     public void setClientId(String clientId) {
         this.clientId = clientId;
     }
 
     public String getSecret() {
         return secret;
     }
 
     public void setSecret(String secret) {
         this.secret = secret;
     }
 
     public String getSiteUrl() {
         return siteUrl;
     }
 
     public void setSiteUrl(String siteUrl) {
         this.siteUrl = siteUrl;
     }
 }
