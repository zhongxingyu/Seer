 package Filters;
 
 import javax.servlet.*;
 import javax.servlet.annotation.WebFilter;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.security.Principal;
 
 /**
  * Created with IntelliJ IDEA.
  * User: brdegenaars
  * Date: 3/8/13
  * Time: 5:36 PM
  * To change this template use File | Settings | File Templates.
  */
 @WebFilter("/*")
 public class LoginFilter implements Filter {
 
     public void doFilter(ServletRequest req, ServletResponse response, FilterChain chain) throws ServletException, IOException {
 
         HttpServletRequest request = (HttpServletRequest) req;
         Principal userPrincipal = request.getUserPrincipal();
 
        if (userPrincipal == null && !"/Start".equals(request.getServletPath()))
        {
 
             String fullRequestPath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
             ((HttpServletResponse) response).sendRedirect("http://my.neumont.edu/nuid/service.aspx?ReturnUrl=" + fullRequestPath + "/Start");
         } else
             chain.doFilter(req, response);
     }
 
     public void init(FilterConfig config) throws ServletException {}
 
     public void destroy() {}
 }
