 package com.forum.authentication;
 
 import org.hamcrest.core.IsEqual;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.security.core.AuthenticationException;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 
 import static org.junit.Assert.assertThat;
 
 public class LoginFailureHandlerTest {
 
     private LoginFailureHandler loginFailureHandler;
     private MockHttpServletResponse mockHttpServletResponse;
     private MockHttpServletRequest mockHttpServletRequest;
     private AuthenticationException mockException;
 
 
     @Before
     public void setUp() throws Exception {
 
         loginFailureHandler = new LoginFailureHandler();
         mockHttpServletRequest = new MockHttpServletRequest();
         mockHttpServletResponse = new MockHttpServletResponse();
         mockException = Mockito.mock(AuthenticationException.class);
 
     }
 
     @Test
     public void shouldAddUsernameAttributeInSession() throws IOException, ServletException {
         String expectedUsername = "failed_username";
        mockHttpServletRequest.addParameter(LoginFailureHandler.FORM_USERNAME_KEY, expectedUsername);
        loginFailureHandler.onAuthenticationFailure(mockHttpServletRequest, mockHttpServletResponse, mockException);
         String actualUsername = (String) mockHttpServletRequest.getSession().getAttribute("username");
         assertThat(actualUsername, IsEqual.equalTo(expectedUsername));
     }
 }
