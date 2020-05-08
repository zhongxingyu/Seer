 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class LoginFilter implements Filter {
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
         HttpServletRequest httpRequest = (HttpServletRequest) request;
 
         if (((HttpServletRequest) request).getSession().getAttribute("user") != null) {
             filterChain.doFilter(request, response);
         } else {
            ((HttpServletResponse) response).sendRedirect(httpRequest.getContextPath() + "/login.jsp"); // Not logged in, show login page. You can eventually show the error page instead.
         }
     }
 
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
     }
 
     @Override
     public void destroy() {
     }
 }
