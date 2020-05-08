 package com.epam.urlchopper.filter.cookie;
 
 import java.io.IOException;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.web.filter.OncePerRequestFilter;
 
 import com.epam.urlchopper.repository.UserRepository;
 
 /**
  * Runs before every request, to check if an application specific cookie was sent in the request.
  * Adds the cookie value to the session, to be able to handle the user's generation history.
  */
 @Component("CookieFilter")
 public class CookieFilter extends OncePerRequestFilter {
 
     public static final String USER_COOKIE_NAME = "urlchopper_userid";
 
     private Logger logger = LoggerFactory.getLogger(CookieFilter.class);
 
     private Cookie[] cookies;
 
     @Autowired
     private UserRepository userRepository;
 
     /**
      * The cookie value is used only once in a session.
      * It is then stored in the session, if it's not already available.
      * If the user rewrites the cookie value in the same session, his history still remains visible.
      */
     @Override
     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
         if (userIdNotFoundInSession(request)) {
             logger.info("User not found in session.");
 
             cookies = request.getCookies();
 
             if (cookies != null) {
                 Cookie cookie = getUserCookie(cookies);
                 if (cookie != null) {
                     logger.info("User cookie already exists in the browser with userid: " + cookie.getValue());
                     if (cookieIsValid(cookie)) {
                         addUserIdToSession(request, cookie.getValue());
                     }
                 } else {
                     logger.info("User cookie not found in the request");
                 }
             } else {
                 logger.info("No cookies were sent in the request");
             }
 
         } else {
             logger.info("User found in session: " + request.getSession().getAttribute(USER_COOKIE_NAME).toString());
         }
         filterChain.doFilter(request, response);
     }
 
     private boolean userIdNotFoundInSession(HttpServletRequest request) {
         return request.getSession().getAttribute(USER_COOKIE_NAME) == null;
     }
 
     private Cookie getUserCookie(Cookie[] cookies) {
         Cookie ret = null;
         int i = incrementWhileCookieFound(cookies, 0);
         if (cookieFound(cookies, i)) {
             ret = cookies[i];
         }
         return ret;
     }
 
     private boolean cookieIsValid(Cookie cookie) {
         boolean ret = true;
         try {
             Long userId = Long.parseLong(cookie.getValue());
             if (userRepository.findUser(userId) == null) {
                 ret = false;
                 logger.error("User cookie is invalid in request, no corresponding user was found in database");
             }
         } catch (NumberFormatException e) {
             ret = false;
             logger.error("User cookie is invalid in request, it cannot be cast to long" + e.getMessage());
         }
        return false;
     }
 
     private boolean cookieFound(Cookie[] cookies, int i) {
         return i < cookies.length;
     }
 
     private int incrementWhileCookieFound(Cookie[] cookies, int i) {
         int ret = i;
         while (ret < cookies.length && !cookies[ret].getName().equals(USER_COOKIE_NAME)) {
             ret++;
         }
         return ret;
     }
 
     private void addUserIdToSession(HttpServletRequest httpRequest, String userId) {
         httpRequest.getSession().setAttribute(USER_COOKIE_NAME, userId);
         logger.info("User added to session with id: " + userId);
     }
 
     public Logger getLogger() {
         return logger;
     }
 
     public void setLogger(Logger logger) {
         this.logger = logger;
     }
 
     public UserRepository getUserRepository() {
         return userRepository;
     }
 
     public void setUserRepository(UserRepository userRepository) {
         this.userRepository = userRepository;
     }
 
     public Cookie[] getCookies() {
         return cookies;
     }
 
     public void setCookies(Cookie[] cookies) {
         this.cookies = cookies;
     }
 
 }
