 /*
  * Copyright 2012 Goodow.com
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.goodow.moon.web.server.auth;
 
 import com.google.common.net.UriEscapers;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.walkaround.util.server.RetryHelper.PermanentFailure;
 import com.google.walkaround.util.server.auth.InvalidSecurityTokenException;
 import com.google.walkaround.wave.server.auth.AccountStore;
 import com.google.walkaround.wave.server.auth.AccountStore.Record;
 import com.google.walkaround.wave.server.auth.ServletAuthHelper.AccountLookup;
 import com.google.walkaround.wave.server.auth.StableUserId;
 import com.google.walkaround.wave.server.auth.XsrfHelper;
 import com.google.walkaround.wave.server.auth.XsrfHelper.XsrfTokenExpiredException;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.annotation.Nullable;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class TokenBasedAccountLookup implements AccountLookup {
   private static final Logger log = Logger.getLogger(InteractiveAuthFilter.class.getName());
   public static final String USER_ID_COOKIE_KEY = "UID";
   public static final String SECRET_TOKEN_COOKIE_KEY = "S";
   public static final String USER_ID_HEADER_KEY = "UID";
   public static final String SECRET_TOKEN_HEADER_KEY = "Authorization";
 
   public static void redirectToLoginPage(HttpServletRequest req, HttpServletResponse resp)
       throws IOException {
     String originalRequest =
         req.getRequestURI() + (req.getQueryString() == null ? "" : "?" + req.getQueryString());
     String targetUrl = "/login?originalRequest=" + queryEncode(originalRequest);
     resp.sendRedirect(targetUrl);
   }
 
   private static String getCookie(HttpServletRequest req, String cookieName) {
     Cookie[] cookies = req.getCookies();
     if (cookies != null) {
       for (Cookie cookie : cookies) {
         if (cookieName.equals(cookie.getName())) {
           return cookie.getValue();
         }
       }
     }
     return null;
   }
 
   private static String queryEncode(String s) {
     return UriEscapers.uriQueryStringEscaper(false).escape(s);
   }
 
   private final Provider<AccountStore> accountStore;
   private final Provider<XsrfHelper> xsrfHelper;
   private Record record;
   private final Provider<UserContext> userCtx;
 
   @Inject
   TokenBasedAccountLookup(Provider<AccountStore> accountStore, Provider<XsrfHelper> xsrfHelper,
       Provider<UserContext> userCtx) {
     this.accountStore = accountStore;
     this.xsrfHelper = xsrfHelper;
     this.userCtx = userCtx;
   }
 
   @Override
   @Nullable
   public Record getAccount() throws PermanentFailure, IOException {
     return record;
   }
 
   public boolean isUserLoggedIn(HttpServletRequest req) throws IOException {
     if (record != null) {
       return true;
     }
     String userId = getCookie(req, USER_ID_COOKIE_KEY);
     String secretToken = getCookie(req, SECRET_TOKEN_COOKIE_KEY);
     if (userId == null && secretToken == null) {
       userId = req.getHeader(USER_ID_HEADER_KEY);
       secretToken = req.getHeader(SECRET_TOKEN_HEADER_KEY);
     }
     if (userId == null && secretToken == null) {
       userId = req.getParameter(USER_ID_HEADER_KEY);
       secretToken = req.getParameter(SECRET_TOKEN_HEADER_KEY);
     }
     if (userId == null || secretToken == null) {
       return false;
     }
     Record record;
     try {
       record = accountStore.get().get(new StableUserId(userId));
      if (record == null) {
         return false;
       }
     } catch (PermanentFailure e) {
       throw new IOException("PermanentFailure getting account information", e);
     }
     userCtx.get().setUserId(new StableUserId(userId));
     try {
       xsrfHelper.get().verify(record.getOAuthCredentials().getAccessToken(), secretToken);
     } catch (XsrfTokenExpiredException e) {
       return false;
     } catch (InvalidSecurityTokenException e) {
       return false;
     }
     this.record = record;
     return true;
   }
 
 }
