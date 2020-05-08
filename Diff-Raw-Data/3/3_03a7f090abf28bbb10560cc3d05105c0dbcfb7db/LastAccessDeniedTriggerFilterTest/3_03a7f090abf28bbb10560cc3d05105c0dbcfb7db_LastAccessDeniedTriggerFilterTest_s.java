 package net.sf.jguard.core.authorization.filters;
 
 import com.mycila.testing.junit.MycilaJunitRunner;
 import net.sf.jguard.core.authorization.AuthorizationBindings;
 import net.sf.jguard.core.lifecycle.MockRequestAdapter;
 import net.sf.jguard.core.lifecycle.MockResponseAdapter;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import javax.inject.Inject;
 import javax.security.auth.Subject;
 import java.security.Permission;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 
 @RunWith(MycilaJunitRunner.class)
 public class LastAccessDeniedTriggerFilterTest extends AuthorizationFilterTest {
 
 
     @Inject
     MockLastAccessDeniedTriggerFilter filter;
 
 
     Subject authenticatedSubject;
 
     @Before
     public void setUp() {
         super.setUp(filter);
         authenticatedSubject = new Subject();
     }
 
 
     @Test
     public void test_doFilter_when_authentication_succeed_and_lastAccessDeniedPermission_is_null() {
         authenticationServicePoint.setAuthenticationSucceededDuringThisRequest(true);
 
         authenticationServicePoint.setCurrentSubject(authenticatedSubject);
         AuthorizationBindings<MockRequestAdapter, MockResponseAdapter> authorizationBindings = mock(AuthorizationBindings.class);
         filter.setAuthorizationBindings(authorizationBindings);
 
 
        when(request.getSessionAttribute(LastAccessDeniedFilter.LAST_ACCESS_DENIED_PERMISSION)).thenReturn(null);
         when(authorizationBindings.getPostAuthenticationPermission(any(MockRequestAdapter.class))).thenReturn(grantedPermission);
 
         policyEnforcementPoint.doFilter(request, response);
 
         //we test that lastAccessDeniedFilter call authorizationBindings.handlePermission with postAuthenticationSucceed Permission.
         //and call filterChain.doFilter()
         verify(authorizationBindings).handlePermission(any(MockRequestAdapter.class), any(MockResponseAdapter.class), eq(grantedPermission));
     }
 
     @Test
     public void test_doFilter_when_authentication_succeed_and_lastAccessDeniedPermission_is__not_null_but_not_granted() {
         authenticationServicePoint.setAuthenticationSucceededDuringThisRequest(true);
         authenticationServicePoint.setCurrentSubject(authenticatedSubject);
         AuthorizationBindings<MockRequestAdapter, MockResponseAdapter> authorizationBindings = mock(AuthorizationBindings.class);
         filter.setAuthorizationBindings(authorizationBindings);
        when(request.getSessionAttribute(LastAccessDeniedFilter.LAST_ACCESS_DENIED_PERMISSION)).thenReturn(notGrantedPermission);
         when(authorizationBindings.getPostAuthenticationPermission(any(MockRequestAdapter.class))).thenReturn(grantedPermission);
         policyEnforcementPoint.doFilter(request, response);
 
     }
 
 
     @Test
     public void test_doFilter_when_authentication_succeed_and_lastAccessDeniedPermission_is__not_null_and_granted() {
         authenticationServicePoint.setAuthenticationSucceededDuringThisRequest(true);
         AuthorizationBindings<MockRequestAdapter, MockResponseAdapter> authorizationBindings = mock(AuthorizationBindings.class);
         filter.setAuthorizationBindings(authorizationBindings);
         when(authorizationBindings.getPostAuthenticationPermission(any(MockRequestAdapter.class))).thenReturn(grantedPermission);
 
     }
 
     @Test
     public void test_doFilter_when_authentication_has_not_be_done_during_this_request() {
         authenticationServicePoint.setAuthenticationSucceededDuringThisRequest(false);
         AuthorizationBindings<MockRequestAdapter, MockResponseAdapter> authorizationBindings = mock(AuthorizationBindings.class);
         verify(authorizationBindings, never()).handlePermission(any(MockRequestAdapter.class), any(MockResponseAdapter.class), any(Permission.class));
 
     }
 
 
 }
