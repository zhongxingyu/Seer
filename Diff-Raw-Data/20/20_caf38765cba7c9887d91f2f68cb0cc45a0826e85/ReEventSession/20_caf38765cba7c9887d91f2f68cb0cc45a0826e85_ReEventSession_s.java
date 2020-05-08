 package reevent.web;
 
 import org.apache.commons.lang.Validate;
 import org.apache.wicket.Session;
 import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
 import org.apache.wicket.authroles.authorization.strategies.role.Roles;
 import org.apache.wicket.injection.Injector;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.request.Request;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import reevent.dao.UserDao;
 import reevent.domain.Location;
 import reevent.domain.User;
 import reevent.domain.UserRole;
 import reevent.service.UserService;
 import reevent.web.model.DaoIdModel;
 
 import java.util.UUID;
 
 public class ReEventSession extends AuthenticatedWebSession {
     /**
      * Profile of the currently signed in user.
      */
     UUID userSignedIn;
 
     @SpringBean
     UserService userService;
 
     @SpringBean
     UserDao userDao;
 
     {
         Injector.get().inject(this);
     }
 
     public static ReEventSession get() {
         return (ReEventSession) Session.get();
     }
 
     public ReEventSession(Request request) {
         super(request);
     }
 
     @Override
     public Roles getRoles() {
         if (!isSignedIn()) {
             // guest access
             return new Roles();
         } else {
             Validate.notNull(userSignedIn);
 
 
             Roles roles = new Roles();
             for (UserRole role : userDao.load(userSignedIn).getRoles()) {
                 roles.add(role.name());
             }
             return roles;
         }
     }
 
     @Override
     public boolean authenticate(String username, String password) {
        userSignedIn = userService.authenticate(username, password).getId();
         return userSignedIn != null;
     }
 
     @Override
     public void signOut() {
         userSignedIn = null;
         super.signOut();    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     public UUID getUserSignedIn() {
         return userSignedIn;
     }
 
     public IModel<User> getModUserSignedIn() {
         return new DaoIdModel<User>(userDao, userSignedIn);
     }
 
     Location location;
 
     public Location getLocation() {
         return location;
     }
 
     public void setLocation(Location location) {
         this.location = location;
     }
 }
