 package com.fastbiz.core.web.spring.security.authentication;
 
 import org.springframework.security.authentication.AuthenticationServiceException;
 import org.springframework.security.authentication.BadCredentialsException;
 import org.springframework.security.authentication.dao.SaltSource;
 import org.springframework.security.authentication.encoding.PasswordEncoder;
 import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.util.Assert;
 import com.fastbiz.core.tenant.TenantHolder;
 
 public class EclipseLinkMultiTenantAuthenticationProvider extends AbstractMultiTenantAuthenticationProvider{
 
     private PasswordEncoder    passwordEncoder = new PlaintextPasswordEncoder();
 
     private SaltSource         saltSource;
 
     private UserDetailsService userDetailsService;
 
     @Override
     protected UserDetails retrieveUser(String username, MultiTenantAuthenticationToken authentication)
                     throws AuthenticationException{
         UserDetails loadedUser;
         try {
             TenantHolder.setTenant(authentication.getTenantId());
             loadedUser = this.getUserDetailsService().loadUserByUsername(username);
         } catch (UsernameNotFoundException notFound) {
             throw notFound;
         } catch (Exception repositoryProblem) {
             throw new AuthenticationServiceException(repositoryProblem.getMessage(), repositoryProblem);
         }
         if (loadedUser == null) {
             String msg = "UserDetailsService returned null, which is an interface contract violation";
             throw new AuthenticationServiceException(msg);
         }
         return loadedUser;
     }
 
    protected void additionalAuthenticationChecks(UserDetails userDetails, MultiTenantAuthenticationToken authentication)
                     throws AuthenticationException{
         String msg = messages.getMessage("authentication.badCredentials", "Bad Credential");
         Object salt = null;
         if (this.saltSource != null) {
             salt = this.saltSource.getSalt(userDetails);
         }
         if (authentication.getCredentials() == null) {
             logger.debug("Authentication failed: no credentials provided");
             throw new BadCredentialsException(msg);
         }
         String presentedPassword = authentication.getCredentials().toString();
         if (!passwordEncoder.isPasswordValid(userDetails.getPassword(), presentedPassword, salt)) {
             logger.debug("Authentication failed: password does not match stored value");
             throw new BadCredentialsException(msg);
         }
     }
 
     protected void doAfterPropertiesSet() throws Exception{
         Assert.notNull(this.userDetailsService, "A UserDetailsService must be set");
     }
 
     public void setPasswordEncoder(PasswordEncoder passwordEncoder){
         Assert.notNull(passwordEncoder, "passwordEncoder cannot be null");
         this.passwordEncoder = passwordEncoder;
     }
 
     protected PasswordEncoder getPasswordEncoder(){
         return passwordEncoder;
     }
 
     public void setSaltSource(SaltSource saltSource){
         this.saltSource = saltSource;
     }
 
     protected SaltSource getSaltSource(){
         return saltSource;
     }
 
     public void setUserDetailsService(UserDetailsService userDetailsService){
         this.userDetailsService = userDetailsService;
     }
 
     protected UserDetailsService getUserDetailsService(){
         return userDetailsService;
     }
 }
