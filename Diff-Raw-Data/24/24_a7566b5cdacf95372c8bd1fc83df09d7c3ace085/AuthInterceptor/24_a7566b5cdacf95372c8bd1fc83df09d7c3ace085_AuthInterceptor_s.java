 package com.directi.train.tweetapp.controllers;
 
 import com.directi.train.tweetapp.services.AuthStore;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 public class AuthInterceptor extends HandlerInterceptorAdapter {
     private AuthStore authStore;
 
     @Autowired
     public AuthInterceptor(AuthStore authStore) {
         this.authStore = authStore;
     }
 
     @Override
     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
             throws Exception {
 
         String cookieName = "accesstoken";
        System.out.println("Crap!");
        if (request.getAttribute(cookieName) != null) {
            String accessToken = (String) request.getParameter(cookieName);
            request.setAttribute(cookieName, accessToken);
 
            Boolean flag = authStore.isValid(accessToken);
 
            System.out.println(request.getAttribute(accessToken) + " " + flag + "....");
 
             if (flag) return flag;
         }
 
         Cookie[] cookies = request.getCookies();
        System.out.println(cookies.length + "~~~");
         for (Cookie cookie : cookies) {
             if (cookie.getName().equals(cookieName)) {
                 String accessToken = cookie.getValue();
                 request.setAttribute(cookieName, accessToken);
 
                System.out.println(request.getAttribute(cookieName) + "!!!");

                 Boolean flag = authStore.isValid(accessToken);
                 if (flag) return flag;
             }
         }
 
         response.sendRedirect("/");
         return false;
     }
 }
