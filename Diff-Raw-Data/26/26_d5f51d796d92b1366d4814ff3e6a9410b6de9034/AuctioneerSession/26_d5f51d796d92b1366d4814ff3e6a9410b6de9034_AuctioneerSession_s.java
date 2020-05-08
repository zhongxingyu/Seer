 package de.mancino.auctioneer.webapp;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.Request;
 import org.apache.wicket.Session;
 import org.apache.wicket.authentication.AuthenticatedWebSession;
 import org.apache.wicket.authorization.strategies.role.Roles;
 import org.apache.wicket.injection.web.InjectorHolder;
 import org.apache.wicket.spring.injection.annot.SpringBean;
import java.util.concurrent.atomic.AtomicInteger;
 
 import de.mancino.auctioneer.bo.UserBO;
 import de.mancino.auctioneer.dto.User;
 import de.mancino.auctioneer.exceptions.UserNameDoesnNotExistException;
 import de.mancino.auctioneer.exceptions.UserPasswordDoesntMatchException;
 
 public class AuctioneerSession extends AuthenticatedWebSession {
     /**
     * 
      */
     private static final long serialVersionUID = 6421724381592539978L;
 
 
     /**
      * Logger instance of this class.
      */
     private static final Log LOG = LogFactory.getLog(AuctioneerSession.class);
 
     @SpringBean(name="userBO")
     protected UserBO userBO;
 
     protected User currentUser = null;
 
     protected AtomicInteger lastErrors;
 
     public AuctioneerSession(final Request request) {
         super(request);
         InjectorHolder.getInjector().inject(this);
         this.lastErrors = new AtomicInteger(0);
     }
 
 
    public static AuthenticatedWebSession get() {
        return (AuthenticatedWebSession) Session.get();
     }
 
    public long getLastErrors() {
         return lastErrors.get();
     }
 
     public void setLastErrors(final int lastErrors) {
         this.lastErrors.set(lastErrors);
     }
 
     public static boolean isAdmin() {
         final AuctioneerSession session = (AuctioneerSession) Session.get();
         return session.currentUser != null && session.getRoles().hasRole(Roles.ADMIN);
     }
 
     @Override
     public boolean authenticate(final String username, final String password) {
         try {
             currentUser = userBO.authenticateUser(username, password);
             LOG.info("User '" + username + "' logged in!");
             return true;
         } catch (UserPasswordDoesntMatchException e) {
             LOG.warn("User '" + username + "' used wrong password!");
         } catch (UserNameDoesnNotExistException e) {
             LOG.warn("User '" + username + "' doesn't exist!");
         }
         currentUser = null;
         return false;
     }
 
     @Override
     public Roles getRoles() {
         if(isSignedIn()) {
             return new Roles(currentUser.getRoles());
         } else {
             return null;
         }
     }
    
     @Override
     public void invalidate() {
         LOG.info("User '" + currentUser.getUserName() + "' logged out.");
         super.invalidate();
         currentUser = null;
     }
 }
