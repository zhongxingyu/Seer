 package ru.kpfu.quantum.spring.filter;
 
 import ru.kpfu.quantum.spring.utils.UserUtils;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * @author sala
  */
 public class AuthenticationFilter implements Filter {
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
         if(servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
             HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
             HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
             final String requestURI = httpServletRequest.getRequestURI();
            if(!requestURI.startsWith("/admin") && !requestURI.startsWith("/registration") && !requestURI.startsWith("/resources")) {
                 if(!UserUtils.isLogined(httpServletRequest) && !"/".equals(requestURI)) {
                     httpServletResponse.sendRedirect("/");
                     return;
                 }
             }
             filterChain.doFilter(servletRequest, servletResponse);
         }
     }
 
     @Override
     public void destroy() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }
