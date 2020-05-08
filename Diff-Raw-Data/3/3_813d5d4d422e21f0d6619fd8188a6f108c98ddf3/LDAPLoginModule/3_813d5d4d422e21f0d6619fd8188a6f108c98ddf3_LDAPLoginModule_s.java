 package org.carlspring.strongbox.jaas.ldap;
 
 import org.carlspring.strongbox.jaas.authentication.BaseLoginModule;
 import org.carlspring.strongbox.jaas.authentication.UserResolver;
 
 import javax.security.auth.Subject;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.login.LoginException;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * <p> This LoginModule authenticates users with a password against a database.
  * <p/>
  * <p> If user is successfully authenticated,
  * a <code>UserPrincipal</code> with the user's user name
  * is added to the Subject.
  * <p/>
  * <p> This LoginModule recognizes the debug option.
  * If set to true in the login Configuration,
  * debug messages will be output to the output stream, System.out.
  */
 public class LDAPLoginModule
         extends BaseLoginModule
 {
 
     private static Logger logger = LoggerFactory.getLogger(LDAPLoginModule.class);
 
     private UserResolver userResolver = new LDAPUserRealm();
 
 
     /**
      * Initialize this <code>LoginModule</code>.
      * <p/>
      * <p/>
      *
      * @param subject         the <code>Subject</code> to be authenticated. <p>
      * @param callbackHandler a <code>CallbackHandler</code> for communicating
      *                        with the end user (prompting for user names and
      *                        passwords, for example). <p>
      * @param sharedState     shared <code>LoginModule</code> state. <p>
      * @param options         options specified in the login
      *                        <code>Configuration</code> for this particular
      *                        <code>LoginModule</code>.
      */
     @Override
     public void initialize(Subject subject,
                            CallbackHandler callbackHandler,
                            Map<String, ?> sharedState,
                            Map<String, ?> options)
     {
         super.initialize(subject, callbackHandler, sharedState, options);
 
         logger.debug("LDAPLoginModule initialized!");
     }
 
     public void checkUserCredentials(String username, String password)
             throws LoginException
     {
         try
         {
             logger.debug("Checking authentication for: " + getPrincipal().getName() + " / " + password + "...");
 
             setUser(getUserAuthenticator().authenticate(username, password, userResolver));
         }
         catch (Exception e)
         {
             throw new LoginException(e.getMessage());
         }
     }
 
 }
