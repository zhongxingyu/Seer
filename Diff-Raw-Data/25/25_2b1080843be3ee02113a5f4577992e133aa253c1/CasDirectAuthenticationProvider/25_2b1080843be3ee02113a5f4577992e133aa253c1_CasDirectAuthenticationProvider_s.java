 package edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct;
 
 import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
 import org.acegisecurity.Authentication;
 import org.acegisecurity.AuthenticationException;
 import org.acegisecurity.AuthenticationServiceException;
 import org.acegisecurity.BadCredentialsException;
 import org.acegisecurity.providers.AuthenticationProvider;
 import org.acegisecurity.userdetails.UserDetailsService;
 
 import java.io.IOException;
 
 /**
  * @author Rhett Sutphin
  */
 public class CasDirectAuthenticationProvider implements AuthenticationProvider {
     private UserDetailsService userDetailsService;
     private String serviceUrl;
     private String loginUrl;
 
     @SuppressWarnings({ "RawUseOfParameterizedType" })
     public boolean supports(Class aClass) {
         return CasDirectUsernamePasswordAuthenticationToken.class.isAssignableFrom(aClass);
     }
 
     public Authentication authenticate(Authentication authentication) throws AuthenticationException {
         if (!supports(authentication.getClass())) {
             return null;
         }
 
         if (authentication.isAuthenticated()) {
             return authentication;
         }
 
         try {
             DirectLoginHttpFacade http = createLoginFacade();
 
             boolean loginSucceeded = executeDirectAuthentication(http, authentication);
             if (loginSucceeded) {
                 String username = getUsername(authentication);
                 return new CasDirectUsernamePasswordAuthenticationToken(
                    username, "[REMOVED PASSWORD]",
                    userDetailsService.loadUserByUsername(username).getAuthorities()
                 );
             } else {
                 throw new BadCredentialsException(
                     "Credentials are invalid according to direct CAS login");
             }
         } catch (IOException ioe) {
             throw new AuthenticationServiceException("Direct CAS login failed", ioe);
         } catch (CasDirectException cde) {
             throw new AuthenticationServiceException("Direct CAS login failed", cde);
         }
     }
 
     protected DirectLoginHttpFacade createLoginFacade() {
        return new DirectLoginHttpFacade(serviceUrl, loginUrl);
     }
 
     protected String getUsername(Authentication authentication) {
         return (String) authentication.getPrincipal();
     }
 
     protected boolean executeDirectAuthentication(DirectLoginHttpFacade http, Authentication authentication) throws IOException {
         LoginFormReader form = new LoginFormReader(http.getForm());
         return http.postCredentials(new MapBuilder<String, String>().
             put("username", (String) authentication.getPrincipal()).
             put("password", (String) authentication.getCredentials()).
             put("lt", form.getLoginTicket()).
             toMap());
     }
 
     public String getServiceUrl() {
         return serviceUrl;
     }
 
     public void setServiceUrl(String serviceUrl) {
         this.serviceUrl = serviceUrl;
     }
 
     public String getLoginUrl() {
         return loginUrl;
     }
 
     public void setLoginUrl(String loginUrl) {
         this.loginUrl = loginUrl;
     }
 
     public UserDetailsService getUserDetailsService() {
         return userDetailsService;
     }
 
     public void setUserDetailsService(UserDetailsService userDetailsService) {
         this.userDetailsService = userDetailsService;
     }
 }
