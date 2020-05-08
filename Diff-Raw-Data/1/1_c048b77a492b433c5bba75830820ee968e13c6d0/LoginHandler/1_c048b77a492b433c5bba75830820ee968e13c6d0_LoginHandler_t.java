 package com.forum.security;
 
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Collection;
 
 public class LoginHandler implements AuthenticationSuccessHandler {
     @Override
     public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
         Collection grantedAuthorities = authentication.getAuthorities();
         GrantedAuthority grantedAuthority = (GrantedAuthority) grantedAuthorities.toArray()[0];
         String authority = grantedAuthority.getAuthority();
 
         String url = httpServletRequest.getParameter("url");
         if(authority.equals("ROLE_ADMIN") && url.isEmpty())
             httpServletResponse.sendRedirect("adminDashboard");
         else
             httpServletResponse.sendRedirect(url);
     }
 }
