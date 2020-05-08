 package org.cognitor.server.openid.web;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.openid4java.server.ServerManager;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.cognitor.server.openid.web.OpenIdManager.MODE;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.when;
 
 /**
  * User: patrick
  * Date: 06.12.12
  */
 @RunWith(MockitoJUnitRunner.class)
 public class OpenIdManagerTest {
 
     private OpenIdManager manager;
 
     @Mock
     private ServerManager serverManagerMock;
 
     @Mock
     private HttpServletRequest requestMock;
 
     @Before
     public void setUp() {
         manager = new OpenIdManager(serverManagerMock);
     }
 
     @Test
     public void shouldReturnTrueWhenOpenIdRequestGiven() {
         when(requestMock.getParameter(MODE)).thenReturn("discovery");
         assertTrue(manager.isOpenIdRequest(requestMock));
     }
 
     @Test
     public void shouldReturnFalseWhenNoRequestParametersGiven() {
         when(requestMock.getParameter(MODE)).thenReturn(null);
         assertFalse(manager.isOpenIdRequest(requestMock));
     }
 
     @Test
     public void shouldReturnOpenIdModeDiscoveryWhenNoOpenIdRequestGiven() {
         when(requestMock.getParameter(MODE)).thenReturn(null);
         assertEquals(OpenIdMode.DISCOVERY, manager.getMode(requestMock));
     }
 
     @Test
     public void shouldReturnOpenIdModeDiscoveryWhenUnknownOpenIdRequestGiven() {
         when(requestMock.getParameter(MODE)).thenReturn("unknown");
         assertEquals(OpenIdMode.DISCOVERY, manager.getMode(requestMock));
     }
 
     @Test
     public void shouldReturnOpenIdModeWhenValidOpenIdRequestGiven() {
         when(requestMock.getParameter(MODE)).thenReturn("checkid_immediate");
         assertEquals(OpenIdMode.CHECKID_IMMEDIATE, manager.getMode(requestMock));
     }

    private HttpServletRequest attachOpenIdModeToRequestMock(String mode) {
        Map parameters = new HashMap();
        parameters.put(MODE, mode);
        when(requestMock.getParameterMap()).thenReturn(parameters);
        when(requestMock.getParameter(MODE)).thenReturn(mode);
        return requestMock;
    }
 }
