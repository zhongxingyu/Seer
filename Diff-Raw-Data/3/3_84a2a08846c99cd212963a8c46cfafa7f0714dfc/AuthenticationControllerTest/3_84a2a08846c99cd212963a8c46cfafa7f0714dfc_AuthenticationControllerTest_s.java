 package com.atlassian.refapp.ctk.test;
 
 import com.atlassian.functest.junit.SpringAwareTestCase;
 import com.atlassian.sal.api.auth.AuthenticationController;
 
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import javax.servlet.http.HttpServletRequest;
 
 import java.security.Principal;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 
 public class AuthenticationControllerTest extends SpringAwareTestCase
 {
     private AuthenticationController controller;
 
     public void setController(AuthenticationController controller)
     {
         this.controller = controller;
     }
 
     @Test
     public void testAuthenticationControllerAvailable()
     {
         assertNotNull("AuthenticationController should be available to plugins", controller);
     }
 
     @Test
     public void testShouldAttemptAuthenticationWhenRequestUnauthenticated()
     {
         assertTrue("should return true if not authenticated", controller.shouldAttemptAuthentication(createMockUnauthenticatedRequest()));
     }
 
     @Test
     public void testShouldNotAttemptAuthenticationWhenRequestAlreadyAuthenticated()
     {
         assertFalse("should return false if already authenticated", controller.shouldAttemptAuthentication(createMockAuthenticatedRequest("hoho")));
     }
 
     private HttpServletRequest createMockRequest()
     {
         HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
         Mockito.when(request.getScheme()).thenReturn("http");
         Mockito.when(request.getServerName()).thenReturn("example.com");
         Mockito.when(request.getServerPort()).thenReturn(8080);
         Mockito.when(request.getContextPath()).thenReturn("/context");
         return request;
     }
 
     private HttpServletRequest createMockUnauthenticatedRequest()
     {
         HttpServletRequest request = createMockRequest();
         Mockito.when(request.getUserPrincipal()).thenReturn(null);
         return request;
     }
 
     private HttpServletRequest createMockAuthenticatedRequest(String authenticatedUserName)
     {
         HttpServletRequest request = createMockRequest();
         Mockito.when(request.getUserPrincipal()).thenReturn(new DummyPrincipal(authenticatedUserName));
         return request;
     }
 
     private static class DummyPrincipal implements Principal
     {
         private String name;
 
         private DummyPrincipal(String name)
         {
             this.name = name;
         }
 
         public String getName()
         {
             return name;
         }
     }
 }
