 package org.cognitor.server.openid.web.security;
 
 import org.cognitor.server.openid.web.OpenIdManager;
 import org.cognitor.server.platform.security.UniqueKeyUserDetails;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.security.core.Authentication;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 import static org.mockito.Mockito.*;
 
 /**
  * @author Patrick Kranz
  */
 @RunWith(MockitoJUnitRunner.class)
 public class OpenIdAuthenticationSuccessHandlerTest {
 
     @Mock
     private HttpServletRequest requestMock;
 
     @Mock
     private HttpServletResponse responseMock;
 
     @Mock
     private Authentication authenticationMock;
 
     @Mock
     private OpenIdManager openIdManagerMock;
 
     @Mock
     private UniqueKeyUserDetails userDetailsMock;
 
     @Test
     public void shouldSendRedirectWhenSuccessfulLoginGiven() throws IOException, ServletException {
         OpenIdAuthenticationSuccessHandler handler = new OpenIdAuthenticationSuccessHandler(openIdManagerMock);
         when(openIdManagerMock.isOpenIdRequest(requestMock)).thenReturn(true);
        when(authenticationMock.getPrincipal()).thenReturn(userDetailsMock);
         when(userDetailsMock.getUniqueKey()).thenReturn("12345");
         when(authenticationMock.isAuthenticated()).thenReturn(true);
         when(openIdManagerMock.getAuthenticationResponseReturnToUrl(requestMock, "12345", true)).thenReturn(
                 "http://return.url");
 
         handler.onAuthenticationSuccess(requestMock, responseMock, authenticationMock);
 
         verify(responseMock, times(1)).sendRedirect("http://return.url");
     }
 }
