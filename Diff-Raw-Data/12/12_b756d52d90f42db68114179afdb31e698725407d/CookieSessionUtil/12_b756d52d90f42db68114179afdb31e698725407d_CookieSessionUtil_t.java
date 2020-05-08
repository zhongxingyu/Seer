 package com.punmac.footballmatchup.webapp.util;
 
 import com.google.gson.ExclusionStrategy;
 import com.google.gson.FieldAttributes;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.punmac.footballmatchup.core.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 public final class CookieSessionUtil {
 
    private static final Logger log = LoggerFactory.getLogger(CookieSessionUtil.class);

     private static final String COOKIE_SESSION_NAME = "player";
     private static final int COOKIE_MAX_AGE = 2628000; // 1 month.
 
     /**
      * Get logged in player from cookie or session.
      * @param request
      * @return Player
      */
     public static Player getLoggedInPlayer(HttpServletRequest request) {
         Gson gson = new Gson();
         Cookie cookie = getCookie(request, COOKIE_SESSION_NAME);
         if(cookie != null) { // Cookie.
             return gson.fromJson(cookie.getValue(), Player.class);
         } else if (request.getSession().getAttribute(COOKIE_SESSION_NAME) != null) { // Session.
             return gson.fromJson(request.getSession().getAttribute(COOKIE_SESSION_NAME).toString(), Player.class);
         }
         return null;
     }
 
     /**
      * Delete cookie and session of logged in player.
      */
     public static void deleteLoggedInPlayer(HttpServletRequest request, HttpServletResponse response) {
         deleteCookie(request, response, COOKIE_SESSION_NAME);
         // We don't use session anymore.
         // request.getSession().invalidate();
     }
 
     /**
      * Create logged in session.
      * @param request
      * @param player
      * @return HttpSession
      */
     public static HttpSession createLoggedInSession(HttpServletRequest request, Player player) {
         Gson gson = new Gson();
         request.getSession().setAttribute(COOKIE_SESSION_NAME, gson.toJson(player));
         return request.getSession();
     }
 
     /**
      * Create logged in cookie.
      * @param response
      * @param player
      * @return Cookie
      */
     public static Cookie createLoggedInCookie(HttpServletResponse response, Player player) {
         Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
             @Override
             public boolean shouldSkipField(FieldAttributes f) {
                 // GSON will not serialize email, password.
                 if(f.getName().equals("email") || f.getName().equals("password")) {
                     return true;
                 }
                 return false;
             }
             @Override
             public boolean shouldSkipClass(Class<?> clazz) {
                 return false;
             }
         }).create();
         player.setPassword(null);
         return createCookie(response, COOKIE_SESSION_NAME, gson.toJson(player));
     }
 
 
     /**
      * Create cookie.
      * @param response
      * @param name
      * @param value
      * @return Cookie
      */
     public static Cookie createCookie(HttpServletResponse response, String name, String value) {
         Cookie cookie = new Cookie(name, value);
         cookie.setMaxAge(COOKIE_MAX_AGE);
         cookie.setPath("/");
         response.addCookie(cookie);
         return cookie;
     }
 
     /**
      * Get cookie by name.
      * @param request
      * @param name
      * @return Cookie
      */
     public static Cookie getCookie(HttpServletRequest request, String name) {
        log.debug("Request = {}", request);
        if(request == null) {
            return null;
        }
        log.debug("Cookie = {}", request.getCookies());
        log.debug("Cookie Size = {}", request.getCookies().length);
         for(Cookie cookie : request.getCookies()) {
             if(name.equals(cookie.getName())) {
                 return cookie;
             }
         }
         return null;
     }
 
     /**
      * Delete cookie.
      * @param request
      * @param name
      */
     public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
         for(Cookie cookie : request.getCookies()) {
             if(name.equals(cookie.getName())) {
                 cookie.setMaxAge(0);
                 cookie.setPath("/");
                 response.addCookie(cookie);
             }
         }
     }
 }
