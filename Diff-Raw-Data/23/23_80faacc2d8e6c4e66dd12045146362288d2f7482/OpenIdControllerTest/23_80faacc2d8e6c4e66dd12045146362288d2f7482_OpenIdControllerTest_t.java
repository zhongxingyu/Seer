 package org.cognitor.server.openid.web.controller;
 
 import org.cognitor.server.openid.web.OpenIdManager;
 import org.cognitor.server.openid.web.OpenIdMode;
 import org.cognitor.server.platform.security.UserDetailsImpl;
 import org.cognitor.server.platform.user.domain.User;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.openid4java.message.AssociationResponse;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.userdetails.UserDetails;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.*;
 
 /**
  * User: patrick
  * Date: 14.11.12
  */
 @RunWith(MockitoJUnitRunner.class)
 public class OpenIdControllerTest {
     @Mock
     private HttpServletRequest requestMock;
 
     @Mock
     private HttpServletResponse responseMock;
 
     @Mock
     private OpenIdManager openIdManagerMock;
 
     @Mock
     private AssociationResponse associationResponseMock;
 
     @Mock
     private Authentication authenticationMock;
 
     private OpenIdController controller;
 
     private UserDetails userDetails;
 
     @Before
     public void setUp() {
         controller = new OpenIdController(openIdManagerMock);
         User user = new User("testUser", "testPassword");
         user.setId(1L);
         userDetails = new UserDetailsImpl(user);
     }
 
     // ASSOCIATION
     @Test
     public void shouldSendAssociationResponseWhenAssociationRequestGiven() throws Exception {
         ByteArrayOutputStream outputStream = attachWriterToResponseMock();
 
         when(openIdManagerMock.associate(requestMock)).thenReturn("associationResponse");
         when(openIdManagerMock.getMode(requestMock)).thenReturn(OpenIdMode.ASSOCIATE);
 
         controller.dispatchOpenIdRequest(requestMock, responseMock, authenticationMock);
 
         assertEquals(outputStream.toString(), "associationResponse");
     }
 
     // SETUP
     @Test
     public void shouldRedirectToLoginPageWhenSetupRequestForUnauthenticatedUserGiven() throws Exception {
         when(openIdManagerMock.getMode(requestMock)).thenReturn(OpenIdMode.CHECKID_SETUP);
         when(openIdManagerMock.getLoginUrl(requestMock)).thenReturn("/login.html?openid.mode=checkid_setup");
 
         controller.dispatchOpenIdRequest(requestMock, responseMock, authenticationMock);
 
         ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
         verify(responseMock).sendRedirect(redirectCaptor.capture());
 
         assertEquals("/login.html?openid.mode=checkid_setup", redirectCaptor.getValue());
     }
 
     @Test
     public void shouldSendSuccessfulAuthenticationResponseWhenSetupRequestForAuthenticatedUserGiven() throws IOException {
         when(authenticationMock.isAuthenticated()).thenReturn(true);
         when(authenticationMock.getPrincipal()).thenReturn(userDetails);
         when(openIdManagerMock.getMode(requestMock)).thenReturn(OpenIdMode.CHECKID_SETUP);
         when(openIdManagerMock.getAuthenticationResponseReturnToUrl(requestMock, "1", true)).thenReturn(
                 "http://returnUrl");
 
         controller.dispatchOpenIdRequest(requestMock, responseMock, authenticationMock);
 
         ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
         verify(responseMock).sendRedirect(redirectCaptor.capture());
 
         assertEquals("http://returnUrl", redirectCaptor.getValue());
     }
 
     @Test
     public void shouldSendPositiveAuthenticationResponseWhenSetupRequestForAuthenticatedUserGiven()
             throws IOException {
         when(authenticationMock.isAuthenticated()).thenReturn(true);
         when(authenticationMock.getPrincipal()).thenReturn(userDetails);
         when(openIdManagerMock.getMode(requestMock)).thenReturn(OpenIdMode.CHECKID_SETUP);
         when(openIdManagerMock.getAuthenticationResponseReturnToUrl(requestMock, "1", true)).thenReturn(
                 "http://positive.return.url");
 
         controller.dispatchOpenIdRequest(requestMock, responseMock, authenticationMock);
 
         ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
         verify(responseMock).sendRedirect(redirectCaptor.capture());
 
         assertEquals("http://positive.return.url", redirectCaptor.getValue());
     }
 
     // CHECK_IMMEDIATE
 
     @Test
     public void shouldSendNegativeAuthenticationResponseWhenImmediateRequestForNotAuthenticatedUserGiven()
             throws IOException {
         when(authenticationMock.isAuthenticated()).thenReturn(false);
         when(requestMock.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));
         when(openIdManagerMock.getMode(requestMock)).thenReturn(OpenIdMode.CHECKID_IMMEDIATE);
         when(openIdManagerMock.getAuthenticationResponseReturnToUrl(requestMock, "http://localhost", false)).thenReturn(
                 "http://return.url");
 
         controller.dispatchOpenIdRequest(requestMock, responseMock, authenticationMock);
 
         ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
         verify(responseMock).sendRedirect(redirectCaptor.capture());
 
         assertEquals("http://return.url", redirectCaptor.getValue());
     }
 
     @Test
     public void shouldSendPositiveAuthenticationResponseWhenImmediateRequestForAuthenticatedUserGiven()
         throws IOException {
         when(authenticationMock.isAuthenticated()).thenReturn(true);
         when(authenticationMock.getPrincipal()).thenReturn(userDetails);
         when(openIdManagerMock.getMode(requestMock)).thenReturn(OpenIdMode.CHECKID_IMMEDIATE);
         when(openIdManagerMock.getAuthenticationResponseReturnToUrl(requestMock, "1", true)).thenReturn(
                 "http://positive.return.url");
 
         controller.dispatchOpenIdRequest(requestMock, responseMock, authenticationMock);
 
         ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
         verify(responseMock).sendRedirect(redirectCaptor.capture());
 
         assertEquals("http://positive.return.url", redirectCaptor.getValue());
     }
 
     // CHECK_AUTHENTICATE
 
     @Test
     public void shouldSendAuthenticationVerificationReponseWhenCheckAuthenticateRequestGiven() throws IOException {
         ByteArrayOutputStream outputStream = attachWriterToResponseMock();
 
         when(openIdManagerMock.getMode(requestMock)).thenReturn(OpenIdMode.CHECK_AUTHENTICATION);
         when(openIdManagerMock.verify(requestMock)).thenReturn("keyValueResponse");
 
         controller.dispatchOpenIdRequest(requestMock, responseMock, authenticationMock);
 
         verify(responseMock, times(1)).setContentType("text/plain");
         assertEquals("keyValueResponse", outputStream.toString());
     }
 
     // DISCOVERY
 
     @Test
     public void shouldSetXrdsLocationWithoutParametersWhenHeadRequestWithoutParametersGiven() {
         when(requestMock.getQueryString()).thenReturn(null);
         when(requestMock.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));
 
         controller.sendXrdsLocation(requestMock, responseMock);
 
         verify(responseMock, atLeastOnce()).addHeader("X-XRDS-Location", "http://localhost");
     }
 
     @Test
     public void shouldSetXrdsLocationWitParametersWhenHeadRequestWithParametersGiven() {
         when(requestMock.getQueryString()).thenReturn("key=value");
         when(requestMock.getRequestURL()).thenReturn(new StringBuffer("http://localhost"));
 
         controller.sendXrdsLocation(requestMock, responseMock);
 
         verify(responseMock, atLeastOnce()).addHeader("X-XRDS-Location", "http://localhost?key=value");
     }
 
     @Test
     public void shouldSendDiscoveryDocumentWithServerServiceWhenRequestWithoutIdGiven() throws IOException {
         ByteArrayOutputStream outputStream = attachWriterToResponseMock();
 
         when(openIdManagerMock.getMode(requestMock)).thenReturn(OpenIdMode.DISCOVERY);
         when(requestMock.getParameterMap()).thenReturn(new HashMap());
         when(requestMock.getRequestURL()).thenReturn(new StringBuffer("http://some.url"));
 
         controller.dispatchOpenIdRequest(requestMock, responseMock, authenticationMock);
 
         verify(responseMock, times(1)).setContentType("application/xrds+xml");
         assertTrue(outputStream.toString().contains("<Type>http://specs.openid.net/auth/2.0/server</Type>"));
     }
 
     @Test
     public void shouldSendDiscoveryDocumentWithSignOnServiceWhenRequestWithIdGiven() throws IOException {
         ByteArrayOutputStream outputStream = attachWriterToResponseMock();
        Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("id", "test");
 
         when(openIdManagerMock.getMode(requestMock)).thenReturn(OpenIdMode.DISCOVERY);
         when(requestMock.getParameterMap()).thenReturn(parameters);
         when(requestMock.getRequestURL()).thenReturn(new StringBuffer("http://some.url"));
 
         controller.dispatchOpenIdRequest(requestMock, responseMock, authenticationMock);
 
         verify(responseMock, times(1)).setContentType("application/xrds+xml");
         assertTrue(outputStream.toString().contains("<Type>http://specs.openid.net/auth/2.0/signon</Type>"));
     }
 
     private ByteArrayOutputStream attachWriterToResponseMock() throws IOException {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         PrintWriter writer = new PrintWriter(outputStream);
         when(responseMock.getWriter()).thenReturn(writer);
         return outputStream;
     }
 }
