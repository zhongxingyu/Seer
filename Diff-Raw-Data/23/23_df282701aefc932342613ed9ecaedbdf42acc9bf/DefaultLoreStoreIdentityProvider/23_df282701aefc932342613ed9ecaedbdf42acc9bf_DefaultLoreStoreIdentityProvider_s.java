 package net.metadata.openannotation.lorestore.access;
 
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.security.core.Authentication;
 import org.springframework.util.Assert;
 
 import au.edu.diasb.chico.mvc.AuthenticationContext;
 import au.edu.diasb.emmet.model.EmmetUser;
 import au.edu.diasb.emmet.model.EmmetUserWrapper;
 
 public class DefaultLoreStoreIdentityProvider implements LoreStoreIdentityProvider, InitializingBean {
 
     private AuthenticationContext authenticationContext;
     
     public DefaultLoreStoreIdentityProvider() {
     	
     }
     
 	public DefaultLoreStoreIdentityProvider(AuthenticationContext ac) {
 		this.authenticationContext = ac;
 	}
 	public String obtainUserURI() {
         String uri = null;
         Authentication auth = authenticationContext.getAuthentication(null);
         if (auth != null) {
             Object p = auth.getPrincipal();
            if (p instanceof EmmetUserWrapper) {
                 uri = ((EmmetUserWrapper) p).unwrap().getPrimaryUri();
             } else if (p instanceof EmmetUser) {
                 uri = ((EmmetUser) p).getPrimaryUri();
             } else if (p instanceof String) {
             	return (String)p;
             }
         }
         return uri;
 	}
 	public void setAuthenticationContext(AuthenticationContext authenticationContext) {
 		this.authenticationContext = authenticationContext;
 	}
 	public AuthenticationContext getAuthenticationContext() {
 		return authenticationContext;
 	}
 	
 	public void afterPropertiesSet() throws Exception {
 		Assert.notNull(authenticationContext, "authenticationContext not set");
 	}
 
 }
