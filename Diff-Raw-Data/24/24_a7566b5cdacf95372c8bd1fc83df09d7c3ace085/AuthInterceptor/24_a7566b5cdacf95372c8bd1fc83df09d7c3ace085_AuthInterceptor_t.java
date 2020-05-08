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
        System.out.println(request);
        System.out.println("Hello");
 
        if (request.getParameter(cookieName) != null) {
            String accessToken = request.getParameter(cookieName);
            request.setAttribute(cookieName, accessToken);
 
            System.out.println("~~~");
            System.out.println(accessToken);
 
            Boolean flag = authStore.isValid(accessToken);
             if (flag) return flag;
         }
 
        System.out.println("!!!");
         Cookie[] cookies = request.getCookies();
         for (Cookie cookie : cookies) {
             if (cookie.getName().equals(cookieName)) {
                 String accessToken = cookie.getValue();
                 request.setAttribute(cookieName, accessToken);
 
                 Boolean flag = authStore.isValid(accessToken);
                 if (flag) return flag;
             }
         }
 
         response.sendRedirect("/");
         return false;
     }
 }
