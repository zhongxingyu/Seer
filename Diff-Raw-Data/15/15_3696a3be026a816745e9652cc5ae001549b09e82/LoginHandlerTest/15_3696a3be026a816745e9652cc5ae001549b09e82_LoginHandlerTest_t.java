 package com.forum.sercurity;
 
 import com.forum.security.LoginHandler;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import static org.mockito.Mockito.*;
 
 public class LoginHandlerTest {
 
     Authentication authentication;
     HttpServletResponse httpServletResponse;
     HttpServletRequest httpServletRequest;
     GrantedAuthority grantedAuthority;
     Collection grantedAuthorities;
     LoginHandler loginHandler;
 
     @Before
     public void setup(){
         authentication = mock(Authentication.class);
         httpServletRequest = mock(HttpServletRequest.class);
         httpServletResponse = mock(HttpServletResponse.class);
         grantedAuthority = mock(GrantedAuthority.class);
         grantedAuthorities = new ArrayList<GrantedAuthority>();
         loginHandler = new LoginHandler();
     }
 
     @Test
     public void shouldAuthenticateAdminAttemptingLogIn() throws IOException, ServletException{
         when(grantedAuthority.getAuthority()).thenReturn("ROLE_ADMIN");
         setupAuthentication();
 
        when(httpServletRequest.getParameter("url")).thenReturn("");
         loginHandler.onAuthenticationSuccess(httpServletRequest,httpServletResponse,authentication);
 
         verify(httpServletResponse).sendRedirect("adminDashboard");
     }
 
     @Test
     public void shouldAuthenticateUserAttemptingLogIn() throws IOException, ServletException{
         when(grantedAuthority.getAuthority()).thenReturn("ROLE_USER");
         setupAuthentication();
 
         loginHandler.onAuthenticationSuccess(httpServletRequest,httpServletResponse,authentication);
 
         verify(httpServletResponse).sendRedirect(null);
     }
 
     private void setupAuthentication() {
         grantedAuthorities.add(grantedAuthority);
         when(authentication.getAuthorities()).thenReturn(grantedAuthorities);
     }
 }
