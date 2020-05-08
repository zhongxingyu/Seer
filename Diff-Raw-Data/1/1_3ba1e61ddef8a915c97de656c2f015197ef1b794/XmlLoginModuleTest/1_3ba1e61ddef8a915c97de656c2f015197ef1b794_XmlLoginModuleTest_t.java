 package net.sf.jguard.ext.authentication.loginmodules;
 
 import net.sf.jguard.core.authentication.credentials.JGuardCredential;
 import net.sf.jguard.core.authentication.manager.AuthenticationManager;
 import net.sf.jguard.core.authentication.manager.JGuardAuthenticationManagerMarkups;
 import net.sf.jguard.core.authorization.permissions.RolePrincipal;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import javax.security.auth.Subject;
 import javax.security.auth.callback.*;
 import javax.security.auth.login.CredentialException;
 import javax.security.auth.login.LoginException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class XmlLoginModuleTest {
     private XmlLoginModule module;
     private Subject subject;
     private CallbackHandler handler;
     private HashMap<String, Object> sharedState;
     private HashMap<String, Object> options;
     private AuthenticationManager authenticationManager;
     private static final String PASSWORD = "password";
     private static final String LOGIN = "login";
     private static final String ADMIN_LOGIN = "admin";
     private static final String ADMIN_PASSWORD = "admin";
 
     @Before
     public void setUp() {
         module = new XmlLoginModule();
         subject = new Subject();
         handler = new CallbackHandler() {
 
             public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                 for (Callback callback : callbacks) {
                     if (callback instanceof NameCallback) {
                         ((NameCallback) callback).setName(ADMIN_LOGIN);
                     } else if (callback instanceof PasswordCallback) {
                         ((PasswordCallback) callback).setPassword(ADMIN_PASSWORD.toCharArray());
                     }
                 }
             }
         };
         sharedState = new HashMap<String, Object>();
         options = new HashMap<String, Object>();
         authenticationManager = mock(AuthenticationManager.class);
         when(authenticationManager.getCredentialId()).thenReturn("id");
         when(authenticationManager.getCredentialPassword()).thenReturn("password");
        when(authenticationManager.getGuestSubject()).thenReturn(new Subject());
         Set<Subject> users = new HashSet<Subject>();
         users.add(authenticationManager.getGuestSubject());
         when(authenticationManager.getUsers()).thenReturn(users);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testInitializeWithNullOptionsMap() {
         module.initialize(subject, handler, sharedState, null);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testInitializeWithNullAuthenticationManager() {
         module.initialize(subject, handler, sharedState, options);
     }
 
     @Test
     public void testInitializeWithAuthenticationManagerInOptions() {
         options.put(JGuardAuthenticationManagerMarkups.AUTHENTICATION_MANAGER.getLabel(), authenticationManager);
         module.initialize(subject, handler, sharedState, options);
     }
 
     @Test(expected = CredentialException.class)
     public void testFailedLogin() throws LoginException {
         options.put(JGuardAuthenticationManagerMarkups.AUTHENTICATION_MANAGER.getLabel(), authenticationManager);
         module.initialize(subject, handler, sharedState, options);
         try {
             module.login();
         } catch (LoginException lex) {
             Assert.assertTrue(XmlLoginModule.LOGIN_ERROR.equals(lex.getMessage()));
             throw lex;
         }
     }
 
     @Test
     public void testSucceedLogin() throws LoginException {
 
         //mock authenticationManager
         when(authenticationManager.getCredentialId()).thenReturn(LOGIN);
         when(authenticationManager.getCredentialPassword()).thenReturn(PASSWORD);
         Set<Subject> users = new HashSet<Subject>();
         Set<JGuardCredential> privateCredentials = new HashSet<JGuardCredential>();
         JGuardCredential login = new JGuardCredential(LOGIN, ADMIN_LOGIN);
         JGuardCredential password = new JGuardCredential(PASSWORD, ADMIN_PASSWORD);
         Set<JGuardCredential> publicCredentials = new HashSet<JGuardCredential>();
         publicCredentials.add(login);
         privateCredentials.add(password);
         Subject adminSubject = new Subject(false, new HashSet<RolePrincipal>(), publicCredentials, privateCredentials);
         users.add(adminSubject);
         when(authenticationManager.getUsers()).thenReturn(users);
         options.put(JGuardAuthenticationManagerMarkups.AUTHENTICATION_MANAGER.getLabel(), authenticationManager);
         module.initialize(subject, handler, sharedState, options);
         module.login();
     }
 }
