 package uk.ac.ebi.arrayexpress.servlets;
 
 /*
  * Copyright 2009-2011 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 
 import uk.ac.ebi.arrayexpress.app.ApplicationServlet;
 import uk.ac.ebi.arrayexpress.components.Users;
 import uk.ac.ebi.arrayexpress.utils.CookieMap;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.net.URLDecoder;
 import java.util.Arrays;
 import java.util.List;
 
 public abstract class AuthAwareApplicationServlet extends ApplicationServlet
 {
     private final static String AE_LOGIN_USER_COOKIE = "AeLoggedUser";
     private final static String AE_LOGIN_TOKEN_COOKIE = "AeLoginToken";
 
     private final static List<String> AE_PUBLIC_ACCESS = Arrays.asList("1");
     private final static List<String> AE_UNRESTRICTED_ACCESS = Arrays.asList();
 
     private static class AuthApplicationServletException extends ServletException
     {
         public AuthApplicationServletException( Throwable x )
         {
             super(x);
         }
     }
 
     protected abstract void doAuthenticatedRequest(
             HttpServletRequest request
             , HttpServletResponse response
             , RequestType requestType
             , List<String> authUserIDs
             ) throws ServletException, IOException;
 
     protected void doRequest( HttpServletRequest request, HttpServletResponse response, RequestType requestType )
             throws ServletException, IOException
     {
         if (!checkAuthCookies(request)) {
             invalidateAuthCookies(response);
         }
         doAuthenticatedRequest(request, response, requestType, getAuthUserIds(request));
     }
 
     private String getAuthUserName( HttpServletRequest request ) throws IOException
     {
         CookieMap cookies = new CookieMap(request.getCookies());
         if (cookies.containsKey(AE_LOGIN_USER_COOKIE)) {
             return URLDecoder.decode(cookies.get(AE_LOGIN_USER_COOKIE).getValue(), "UTF-8");
         }
         return null;
     }
 
     private String getAuthToken( HttpServletRequest request )
     {
         CookieMap cookies = new CookieMap(request.getCookies());
         if (cookies.containsKey(AE_LOGIN_TOKEN_COOKIE)) {
             return cookies.get(AE_LOGIN_TOKEN_COOKIE).getValue();
         } else {
             return null;
         }
     }
 
     private boolean checkAuthCookies( HttpServletRequest request ) throws ServletException
     {
         try {
             String userName = getAuthUserName(request);
             String token = getAuthToken(request);
            String userAgent = request.getHeader("User-Agent");
             Users users = (Users) getComponent("Users");
            return users.verifyLogin(
                    userName
                    , token
                    , request.getRemoteAddr().concat(
                        userAgent != null ? userAgent : "unknown"
                    )
            );
         } catch (Exception x) {
             throw new AuthApplicationServletException(x);
         }
     }
 
     private void invalidateAuthCookies( HttpServletResponse response )
     {
         // deleting user cookie
         Cookie userCookie = new Cookie(AE_LOGIN_USER_COOKIE, "");
         userCookie.setPath("/");
         userCookie.setMaxAge(0);
 
         response.addCookie(userCookie);
     }
 
     protected List<String> getAuthUserIds( HttpServletRequest request ) throws ServletException
     {
         if (!checkAuthCookies(request)) {
             return AE_PUBLIC_ACCESS;
         } else {
             try {
                 Users users = (Users) getComponent("Users");
                 String userName = getAuthUserName(request);
                 if (users.isPrivileged(userName)) {
                     return AE_UNRESTRICTED_ACCESS;
                 } else {
                     return users.getUserIDs(userName);
                 }
             } catch (Exception x) {
                throw new AuthApplicationServletException(x);
             }
         }
     }
 }
