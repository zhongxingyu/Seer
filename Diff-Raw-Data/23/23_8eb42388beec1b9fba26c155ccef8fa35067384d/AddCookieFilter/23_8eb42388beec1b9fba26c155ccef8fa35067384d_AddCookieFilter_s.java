 package com.epam.urlchopper.filter.cookie;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 import com.epam.urlchopper.domain.User;
 import com.epam.urlchopper.repository.UserRepository;
 
 /**
  * Servlet Filter implementation class AddCookieFilter
  */
@Component
 public class AddCookieFilter implements Filter {
 
     private Logger logger = LoggerFactory.getLogger(CookieFilter.class);
 
     private UserRepository userRepository;
 
     public AddCookieFilter() {
         // TODO Auto-generated constructor stub
     }
 
    public AddCookieFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

     /**
      * @see Filter#destroy()
      */
     public void destroy() {
         // TODO Auto-generated method stub
     }
 
     /**
      * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
      */
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         // TODO Auto-generated method stub
         // place your code here
 
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         HttpServletResponse httpResponse = (HttpServletResponse) response;
 
         Cookie[] cookies = httpRequest.getCookies();
 
         if (cookies != null) {
             int i = 0;
             while (i < cookies.length && !cookies[i].getName().equals(CookieFilter.USER_COOKIE_NAME)) {
                 i++;
             }
             if (i >= cookies.length) {
                 logger.info("cookie does not exists in browser");
                 User user = createUser();
                 Cookie cookie = createCookie(user, httpResponse);
                 addUserIdToSession(httpRequest, cookie.getValue());
             }
         }
 
         // pass the request along the filter chain
         chain.doFilter(request, response);
     }
 
     /**
      * @see Filter#init(FilterConfig)
      */
     public void init(FilterConfig fConfig) throws ServletException {
         // TODO Auto-generated method stub
     }
 
     private User createUser() {
         User user = userRepository.create(new User());
         logger.info("new user added with id: " + user.getUserId());
         return user;
     }
 
     private Cookie createCookie(User user, HttpServletResponse response) {
         Cookie cookie = new Cookie("urlchopper_userid", user.getUserIdAsString());
         cookie.setMaxAge(Integer.MAX_VALUE);
         response.addCookie(cookie);
         logger.info("cookie added to response with userid: " + cookie.getValue());
         return cookie;
     }
 
     private void addUserIdToSession(HttpServletRequest httpRequest, String userId) {
         if (httpRequest.getSession().getAttribute(CookieFilter.USER_COOKIE_NAME) == null) {
             httpRequest.getSession().setAttribute(CookieFilter.USER_COOKIE_NAME, userId);
         }
     }

 }
