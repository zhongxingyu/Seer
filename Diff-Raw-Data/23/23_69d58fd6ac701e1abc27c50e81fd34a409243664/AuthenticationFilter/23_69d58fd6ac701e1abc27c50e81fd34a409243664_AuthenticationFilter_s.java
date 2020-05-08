 package org.eleventhlabs.ncomplo.web.filter;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eleventhlabs.ncomplo.web.util.SessionUtil;
 
 public class AuthenticationFilter implements Filter {
 
     
     public AuthenticationFilter() {
         super();
     }
     
     
     
     @Override
     public void doFilter(
             final ServletRequest request, final ServletResponse response, final FilterChain chain) 
                     throws IOException, ServletException {
         
         final HttpServletRequest httpServletRequest = (HttpServletRequest)request;
 
         if (!SessionUtil.isUserAuthenticated(httpServletRequest)) {
 
            if (!httpServletRequest.getServletPath().endsWith("/login") &&
                    !httpServletRequest.getServletPath().endsWith("/authenticate") &&
                    !httpServletRequest.getServletPath().endsWith("/css/ncomplo.css") &&
                    !httpServletRequest.getServletPath().endsWith("/js/ncomplo.js")) {
                
                ((HttpServletResponse)response).sendRedirect("/login");
                return;
                
             }
             
         }
 
         chain.doFilter(request, response);
         
     }
 
     
     
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
         // nothing to do
     }
 
     @Override
     public void destroy() {
         // nothing to do
     }
 
 }
