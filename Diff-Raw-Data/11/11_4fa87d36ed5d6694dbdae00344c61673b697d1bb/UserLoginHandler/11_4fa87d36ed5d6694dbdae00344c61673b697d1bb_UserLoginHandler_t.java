 package de.atns.common.security;
 
 import com.google.inject.Inject;
import com.google.inject.extensions.security.SecurityFilter;
 import com.google.inject.extensions.security.SecurityService;
 import com.google.inject.extensions.security.SecurityUser;
 import com.google.inject.extensions.security.UserService;
 import de.atns.common.gwt.server.DefaultActionHandler;
 import de.atns.common.security.client.action.UserLogin;
 import de.atns.common.security.client.model.UserPresentation;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author tbaum
  * @since 23.10.2009
  */
 public class UserLoginHandler extends DefaultActionHandler<UserLogin, UserPresentation> {
 
     private static final Logger LOG = LoggerFactory.getLogger(UserLoginHandler.class);
     private final CheckSessionHandler checkSessionHandler;
     private final SecurityService securityService;
     private final UserService userService;
    private final SecurityFilter securityFilter;
 
     @Inject public UserLoginHandler(CheckSessionHandler checkSessionHandler, SecurityService securityService,
                                    UserService userService, SecurityFilter securityFilter) {
         this.checkSessionHandler = checkSessionHandler;
         this.securityService = securityService;
         this.userService = userService;
        this.securityFilter = securityFilter;
     }
 
     @Override public final UserPresentation executeInternal(final UserLogin action) {
         String login = action.getUserName();
         LOG.info("login " + login);
         final SecurityUser user = userService.findUser(login, action.getPassword());
         final String token = securityService.authenticate(user);
         if (token == null) {
             throw new RuntimeException("invalid login");
         }
        securityFilter.setSessionToken(token);
         return checkSessionHandler.checkSession();
     }
 }
