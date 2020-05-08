 package jamm.webapp;
 
 import java.io.IOException;
 import javax.servlet.Filter;
 import javax.servlet.FilterConfig;
 import javax.servlet.FilterChain;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @web:filter name="Authentication Filter"
  * @web:filter-mapping url-pattern="/private/*"
  */
 public class AuthenticationFilter implements Filter
 {
     public void init(FilterConfig config)
         throws ServletException
     {
         mConfig = config;
     }
 
     public void destroy()
     {
         mConfig = null;
     }
 
     public void doFilter(ServletRequest servletRequest,
                          ServletResponse servletResponse,
                          FilterChain chain)
         throws IOException, ServletException
     {
        if (servletRequest instanceof HttpServletRequest)
         {
             HttpServletRequest request = (HttpServletRequest) servletRequest;
             HttpServletResponse response =
                 (HttpServletResponse) servletResponse;
             HttpSession session = request.getSession();
 
             if ((session == null) ||
                 (session.getAttribute(AUTHENTICATION_KEY) == null))
             {
                 StringBuffer done = request.getRequestURL();
                 String query = request.getQueryString();
                 if (query != null)
                 {
                     done.append("?").append(query);
                 }
                 response.sendRedirect(request.getContextPath() +
                                       "/login.jsp?done=" + done.toString());
                 return;
             }
         }
 
         // User is authenticated
         chain.doFilter(servletRequest, servletResponse);
     }
 
     private FilterConfig mConfig;
     private static final String AUTHENTICATION_KEY = "is_authenticated";
 }
