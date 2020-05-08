 package org.jboss.seam.examples.seamcrm.aaa;
 
 import java.io.Serializable;
 import java.util.TimeZone;
 
 import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.event.Event;
 import javax.enterprise.event.Observes;
 import javax.enterprise.inject.Produces;
 import javax.faces.context.ExternalContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.http.HttpServletRequest;
 
 import org.jboss.seam.examples.seamcrm.user.User;
 import org.jboss.seam.international.Alter;
 import org.jboss.solder.core.Client;
 import org.jboss.solder.logging.Logger;
 
 
 /**
  * 
  * @author Cody Lerum
  * 
  */
 @Stateful
 @SessionScoped
 public class SessionUserManager implements Serializable {
 
     private static final long serialVersionUID = 1L;
     
     private SessionUser sessionUser;
     
     @Inject
     private Logger log;
     
     @Inject
     private HttpServletRequest httpServletRequest;
 
     @Produces
    @RequestScoped
     @Named
     public SessionUser sessionUser() {
         return sessionUser;
     }
 
     @Inject
     @Client
     @Alter
     private Event<TimeZone> tzEvent;
 
     public void onLogin(@Observes @Authenticated User user, ExternalContext externalContext) {
         sessionUser = new SessionUser(user, httpServletRequest.getRemoteAddr());
         httpServletRequest.getSession().setMaxInactiveInterval(3600);
         TimeZone userTZ = sessionUser.getUser().getContact().getTimeZone().getTimeZone();
         log.info("Altering UserTimeZone to: " + userTZ.getID());
         tzEvent.fire(userTZ);
     }
 
 }
