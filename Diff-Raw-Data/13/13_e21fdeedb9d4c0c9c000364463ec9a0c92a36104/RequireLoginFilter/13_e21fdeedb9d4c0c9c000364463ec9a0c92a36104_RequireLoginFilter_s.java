 package com.dateengine.filters;
 
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.appengine.api.users.User;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import java.io.IOException;
 
 public class RequireLoginFilter implements Filter {
    public void destroy() {
    }
 
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
       UserService userService = UserServiceFactory.getUserService();
       User user = userService.getCurrentUser();
 
       if(user != null) {
          req.setAttribute("user", user);
          chain.doFilter(req, resp);
       } else {
          ((HttpServletResponse) resp).sendRedirect(
                  userService.createLoginURL(((HttpServletRequest)req).getRequestURI()));
       }
    }
 
    public void init(FilterConfig config) throws ServletException {
 
    }
 
 }
