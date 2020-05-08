 package org.cotrix.web.common.server.util;
 
 import javax.annotation.Priority;
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.inject.Alternative;
 import javax.enterprise.inject.Produces;
 
 import org.cotrix.common.Constants;
 import org.cotrix.common.cdi.BeanSession;
 import org.cotrix.common.cdi.Current;
 import org.cotrix.domain.user.User;
 import org.jboss.weld.context.RequestContext;
 import org.jboss.weld.context.http.Http;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Priority(Constants.RUNTIME)
 public class CdiProducers {
 
 	public static Logger log = LoggerFactory.getLogger(CdiProducers.class);
 	
 	@Produces @SessionScoped @Alternative
 	public @Current BeanSession session() {
 	
 		return new BeanSession();		
 	}
 	
 	@Produces @RequestScoped @Alternative
 	public @Current User currentUser(@Current BeanSession session,@Http RequestContext ctx) {
 		
 		try {
 				return session.get(User.class);
 		}
 		catch(IllegalStateException e) {
 
 			//this should never happen: let's be more specific
 			throw new IllegalAccessError("detected an attempt to access a protected resource without an authenticated user: is the authentication barrier configured?");
 			
 		}	
 		
 	}
 }
