 package net.sf.jguard.jee.authentication.http;
 
 import net.sf.jguard.core.authentication.LoginContextWrapper;
 import net.sf.jguard.core.authentication.credentials.JGuardCredential;
 import net.sf.jguard.core.authentication.manager.AuthenticationManager;
 import net.sf.jguard.core.authorization.permissions.RolePrincipal;
 import net.sf.jguard.core.authorization.permissions.UserPrincipal;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import javax.security.auth.Subject;
 import java.util.HashSet;
 import java.util.Set;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class JEERequestWrapperUtilTest {
 
     public static final String MY_LOGIN = "myLogin";
     public static final String IDENTITY_KEY = "login";
     public static final String DUMMY_APPLICATION_NAME = "applicationName";
     public static final String UNKNOWN_APPLICATION_NAME = "unKnownApplicationName";
     public static final String DUMMY_ROLE = "crazyRole";
     public static final String UNKNOWN_ROLE = "unknownRole";
 
     @Mock
     private LoginContextWrapper mockLoginContextWrapper;
     @Mock
     private AuthenticationManager mockAuthenticationManager;
 
     private Subject subject;
     private Set<JGuardCredential> publicCredentials = new HashSet<JGuardCredential>();
     private Set<JGuardCredential> privateCredentials = new HashSet<JGuardCredential>();
     private Set principals = new HashSet();
 
     @Before
     public void setUp() {
         JGuardCredential identityCredential = new JGuardCredential(IDENTITY_KEY, MY_LOGIN);
         publicCredentials.add(identityCredential);
         RolePrincipal rolePrincipal = new RolePrincipal(DUMMY_ROLE, DUMMY_APPLICATION_NAME);
         principals.add(rolePrincipal);
         subject = new Subject(false, principals, publicCredentials, privateCredentials);
         when(mockLoginContextWrapper.getSubject()).thenReturn(subject);
         when(mockAuthenticationManager.getIdentityCredential(subject)).thenReturn(identityCredential);
         when(mockAuthenticationManager.getCredentialId()).thenReturn(IDENTITY_KEY);
 
     }
 
     // getRemoteUser tests
     @Test
     public void testGetRemoteUserWithNullLoginContextWrapper() throws Exception {
         String user = JEERequestWrapperUtil.getRemoteUser(null, mockAuthenticationManager);
         Assert.assertThat(user, is(nullValue()));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testGetRemoteUserWithNullAuthenticationManager() throws Exception {
         JEERequestWrapperUtil.getRemoteUser(mockLoginContextWrapper, null);
     }
 
     @Test
     public void testGetRemoteUserWithNullArguments() throws Exception {
         String user = JEERequestWrapperUtil.getRemoteUser(null, null);
         Assert.assertThat(user, is(nullValue()));
     }
 
     @Test
     public void testGetRemoteUserWithNullSubject() throws Exception {
         when(mockLoginContextWrapper.getSubject()).thenReturn(null);
         String user = JEERequestWrapperUtil.getRemoteUser(mockLoginContextWrapper, mockAuthenticationManager);
         Assert.assertThat(user, is(nullValue()));
     }
 
     @Test
     public void testGetRemoteUser() throws Exception {
         String user = JEERequestWrapperUtil.getRemoteUser(mockLoginContextWrapper, mockAuthenticationManager);
         Assert.assertThat(user, is(MY_LOGIN));
     }
 
     @Test
     public void testGetRemoteUserWithNullIdentityCredentialInSubject() throws Exception {
         when(mockLoginContextWrapper.getSubject()).thenReturn(subject);
        when(mockAuthenticationManager.getIdentityCredential(subject)).thenReturn(null);

         String user = JEERequestWrapperUtil.getRemoteUser(mockLoginContextWrapper, mockAuthenticationManager);
         Assert.assertThat(user, is(nullValue()));
     }
 
 
     //end getRemoteUser tests
 
     //isUserInRole tests
     @Test(expected = IllegalArgumentException.class)
     public void testIsUserInRoleWithNullArguments() throws Exception {
         JEERequestWrapperUtil.isUserInRole(null, null, null);
     }
 
 
     @Test(expected = IllegalArgumentException.class)
     public void testIsUserInRoleWithNullApplicationName() throws Exception {
         JEERequestWrapperUtil.isUserInRole(null, DUMMY_ROLE, mockLoginContextWrapper);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testIsUserInRoleWithNullRole() throws Exception {
         JEERequestWrapperUtil.isUserInRole(DUMMY_APPLICATION_NAME, null, mockLoginContextWrapper);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testIsUserInRoleWithEmptyRole() throws Exception {
         JEERequestWrapperUtil.isUserInRole(DUMMY_APPLICATION_NAME, "", mockLoginContextWrapper);
     }
 
 
     @Test(expected = IllegalArgumentException.class)
     public void testIsUserInRoleWithNullLoginContextWrapper() throws Exception {
         JEERequestWrapperUtil.isUserInRole(DUMMY_APPLICATION_NAME, DUMMY_ROLE, null);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testIsUserInRoleWithNullSubject() throws Exception {
         when(mockLoginContextWrapper.getSubject()).thenReturn(null);
         JEERequestWrapperUtil.isUserInRole(DUMMY_APPLICATION_NAME, DUMMY_ROLE, mockLoginContextWrapper);
     }
 
 
     @Test
     public void testIsUserInRole() throws Exception {
         when(mockLoginContextWrapper.getSubject()).thenReturn(subject);
         boolean isUserInRole = JEERequestWrapperUtil.isUserInRole(DUMMY_APPLICATION_NAME, DUMMY_ROLE, mockLoginContextWrapper);
         Assert.assertThat(isUserInRole, is(true));
     }
 
     @Test
     public void testIsUserInRoleWithGoodApplicationNameAndBadRoleName() throws Exception {
         when(mockLoginContextWrapper.getSubject()).thenReturn(subject);
         boolean isUserInRole = JEERequestWrapperUtil.isUserInRole(DUMMY_APPLICATION_NAME, UNKNOWN_ROLE, mockLoginContextWrapper);
         Assert.assertThat(isUserInRole, is(false));
     }
 
 
     @Test
     public void testIsUserInRoleWithBadApplicationNameAndGoodRoleName() throws Exception {
         when(mockLoginContextWrapper.getSubject()).thenReturn(subject);
         boolean isUserInRole = JEERequestWrapperUtil.isUserInRole(UNKNOWN_APPLICATION_NAME, DUMMY_ROLE, mockLoginContextWrapper);
         Assert.assertThat(isUserInRole, is(false));
     }
 
     //end test isUserInRole
 
     //getUSerPrincipal tests
     @Test(expected = IllegalArgumentException.class)
     public void testGetUserPrincipalWithNullLoginContextWrapper() throws Exception {
         JEERequestWrapperUtil.getUserPrincipal(null);
     }
 
     @Test
     public void testGetUserPrincipal() throws Exception {
         UserPrincipal userPrincipal = (UserPrincipal) JEERequestWrapperUtil.getUserPrincipal(mockLoginContextWrapper);
         Assert.assertThat(userPrincipal, is(new UserPrincipal(subject)));
     }
 
 
     //end getUSerPrincipal tests
 }
