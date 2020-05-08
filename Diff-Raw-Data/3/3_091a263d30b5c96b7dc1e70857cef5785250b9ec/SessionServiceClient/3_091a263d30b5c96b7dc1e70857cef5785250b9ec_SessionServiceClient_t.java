 /*
  * Copyright 2012 Herald, Southeast University.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package cn.edu.seu.herald.session.jee;
 
 import cn.edu.seu.herald.session.Session;
 import cn.edu.seu.herald.session.SessionService;
 import cn.edu.seu.herald.session.exception.SessionAccessException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author rAy <predator.ray@gmail.com>
  */
 public class SessionServiceClient {
     
     private SessionService sessionCacheService;
     
     public SessionServiceClient(SessionService sessionCacheService) {
         this.sessionCacheService = sessionCacheService;
     }
     
     private static String getSessionId(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }
         for (Cookie c: cookies) {
             String cookieName = c.getName();
             if (SessionJeeConstants.SESSION_COOKIE_NAME.equals(cookieName)) {
                 return c.getValue();
             }
         }
         return null;
     }
     
     public Session getSession(HttpServletRequest request,
             HttpServletResponse response) throws SessionAccessException {
         Cookie[] cookies = request.getCookies();
         String sessionId = getSessionId(cookies);
         boolean found = (sessionId != null);
         if (found) {
             return sessionCacheService.getSessionById(sessionId);
         }
         
         Session newSession = sessionCacheService.getSession();
         String newSessionId = newSession.getId();
         Cookie cookie = new Cookie(SessionJeeConstants.SESSION_COOKIE_NAME,
                 newSessionId);
         cookie.setDomain(SessionJeeConstants.HERALD_DOMAIN);
         response.addCookie(cookie);
         return newSession;
     }
 
 }
