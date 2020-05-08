 package com.fastbiz.core.web.spring.security.authentication;
 
 import java.util.Collection;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.context.MessageSource;
 import org.springframework.context.MessageSourceAware;
 import org.springframework.context.support.MessageSourceAccessor;
 import org.springframework.security.authentication.AuthenticationProvider;
 import org.springframework.security.authentication.BadCredentialsException;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
 import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsChecker;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.util.Assert;
 import com.fastbiz.core.tenant.TenantHolder;
 
 public abstract class AbstractMultiTenantAuthenticationProvider implements AuthenticationProvider, InitializingBean,
                 MessageSourceAware{
 
     protected final Log              logger                 = LogFactory.getLog(getClass());
 
     private boolean                  forcePrincipalAsString = false;
 
     private UserDetailsChecker       preAuthenticationChecks;
 
     private UserDetailsChecker       postAuthenticationChecks;
 
     private GrantedAuthoritiesMapper authoritiesMapper      = new NullAuthoritiesMapper();
 
     protected MessageSourceAccessor  messages               = SecurityMessageSource.getAccessor();
 
     protected abstract void additionalAuthenticationChecks(UserDetails userDetails,
                                                            MultiTenantAuthenticationToken authentication)
                     throws AuthenticationException;
 
     public final void afterPropertiesSet() throws Exception{
         doAfterPropertiesSet();
     }
 
     public Authentication authenticate(Authentication authentication) throws AuthenticationException{
         String message = "Only MultiTenantAuthenticationToken is supported";
         Assert.isInstanceOf(MultiTenantAuthenticationToken.class, authentication, message);
         MultiTenantAuthenticationToken mtat = (MultiTenantAuthenticationToken) authentication;
         TenantHolder.setTenant(mtat.getTenantId());
         String username = (authentication.getPrincipal() == null) ? "" : authentication.getName();
         UserDetails user = null;
         try {
             user = retrieveUser(username, mtat);
         } catch (UsernameNotFoundException notFound) {
             String msg = messages.getMessage("authentication.badCredentials", "Bad Credential");
             throw new BadCredentialsException(msg);
         }
         Assert.notNull(user, "retrieveUser returned null - a violation of the interface contract");
         if (preAuthenticationChecks != null) {
             preAuthenticationChecks.check(user);
         }
         additionalAuthenticationChecks(user, mtat);
         if (postAuthenticationChecks != null) {
             postAuthenticationChecks.check(user);
         }
         Object principalToReturn = user;
         return createSuccessAuthentication(principalToReturn, authentication, user);
     }
 
     protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user){
         MultiTenantAuthenticationToken mtat = (MultiTenantAuthenticationToken) authentication;
         String tenantId = mtat.getTenantId();
         Collection<? extends GrantedAuthority> mapAuthorities = authoritiesMapper.mapAuthorities(user.getAuthorities());
         MultiTenantAuthenticationToken result = new MultiTenantAuthenticationToken(tenantId, principal, mapAuthorities);
         result.setAuthenticated(true);
         result.setDetails(user);
         return result;
     }
 
     protected void doAfterPropertiesSet() throws Exception{}
 
     public boolean isForcePrincipalAsString(){
         return forcePrincipalAsString;
     }
 
     protected abstract UserDetails retrieveUser(String username, MultiTenantAuthenticationToken authentication)
                     throws AuthenticationException;
 
     public boolean supports(Class<?> authentication){
         return (MultiTenantAuthenticationToken.class.isAssignableFrom(authentication));
     }
 
     protected UserDetailsChecker getPreAuthenticationChecks(){
         return preAuthenticationChecks;
     }
 
     public void setPreAuthenticationChecks(UserDetailsChecker preAuthenticationChecks){
         this.preAuthenticationChecks = preAuthenticationChecks;
     }
 
     protected UserDetailsChecker getPostAuthenticationChecks(){
         return postAuthenticationChecks;
     }
 
     public void setPostAuthenticationChecks(UserDetailsChecker postAuthenticationChecks){
         this.postAuthenticationChecks = postAuthenticationChecks;
     }
 
     public void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper){
         this.authoritiesMapper = authoritiesMapper;
     }
 
     @Override
     public void setMessageSource(MessageSource messageSource){
         messages = new MessageSourceAccessor(messageSource);
     }
 }
