 /*
 jGuard is a security framework based on top of jaas (java authentication and authorization security).
 it is written for web applications, to resolve simply, access control problems.
 version $Name$
 http://sourceforge.net/projects/jguard/
 
 Copyright (C) 2004  Charles Lescot
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 
 jGuard project home page:
 http://sourceforge.net/projects/jguard/
 
 */
 package net.sf.jguard.core.authentication;
 
 import net.sf.jguard.core.ApplicationName;
 import net.sf.jguard.core.authorization.permissions.UserPrincipal;
 import net.sf.jguard.core.util.ThrowableUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.inject.Inject;
 import javax.security.auth.Subject;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.login.Configuration;
 import javax.security.auth.login.LoginContext;
 import javax.security.auth.login.LoginException;
 import java.util.Locale;
 import java.util.MissingResourceException;
 
 
 /**
  * LoginContext wrapper around the {@link Subject}, which permits to infer
  * on the Subject's LifeCycle (login, logout).
  *
  * @author <a href="mailto:diabolo512@users.sourceforge.net">Charles Lescot</a>
  */
 public class LoginContextWrapperImpl implements LoginContextWrapper {
 
     private LoginContext loginContext = null;
     private Subject subject = null;
     private AuthenticationStatus status;
     private boolean loggedOut = false;
     private static final Logger logger = LoggerFactory.getLogger(LoginContextWrapperImpl.class.getName());
     private String applicationName;
 
     @Inject
     public LoginContextWrapperImpl(@ApplicationName String applicationName) {
         super();
         this.applicationName = applicationName;
     }
 
     /**
     * authenticate SUbject
      *
      * @param callbackHandler
      * @param configuration   can be the global Configuration or a filtered one for impersonation
      * @throws LoginException
      */
     public void login(CallbackHandler callbackHandler, Configuration configuration) throws LoginException {
         loginContext = new LoginContext(applicationName, new Subject(), callbackHandler, configuration);
         try {
             loginContext.login();
         } catch (LoginException le) {
             try {
                 throw (LoginException) ThrowableUtils.localizeThrowable(le, Locale.getDefault());
             } catch (MissingResourceException mre) {
                 throw le;
             }
         }
 
         subject = loginContext.getSubject();
 
         if (subject != null) {
             // used in ABAC permissions
             UserPrincipal userPrincipal = new UserPrincipal(subject);
             subject.getPrincipals().add(userPrincipal);
         }
 
     }
 
     /**
      * retrieve the subject from the loginContext.
      *
      * @return authenticated Subject, otherwise <strong>null</strong>.
      */
     public Subject getSubject() {
         return subject;
     }
 
 
     /**
      * logout the user with the related LoginContext.
      */
     public void logout() {
         if (!loggedOut) {
             try {
 
                 if (loginContext != null) {
                     loginContext.logout();
                     loggedOut = true;
                 } else {
                     logger.debug(" user is not logged, so we don't logout him ");
                 }
 
             } catch (LoginException e) {
                 logger.debug(" error raised when the user logout " + e.getMessage(), e);
             }
         }
     }
 
 
     /**
      * @return the status
      */
     public AuthenticationStatus getStatus() {
         return status;
     }
 
     /**
      * @param status the status to set
      */
     public void setStatus(AuthenticationStatus status) {
         this.status = status;
     }
 
 
 }
