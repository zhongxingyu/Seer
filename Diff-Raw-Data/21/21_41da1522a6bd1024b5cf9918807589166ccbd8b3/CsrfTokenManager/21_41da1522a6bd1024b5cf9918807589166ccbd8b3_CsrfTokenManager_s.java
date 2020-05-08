 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.web.csrf;
 
 import java.util.UUID;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 public final class CsrfTokenManager {
 
   public static final String CSRF_PARAM_NAME = "csrf-token";
  private static final String CSRF_SESSION_ATTR_NAME = "csrf-token";
 
   private CsrfTokenManager() {
   }
 
   public static boolean isTokenPresentInSession(HttpSession session) {
     return session.getAttribute(CSRF_SESSION_ATTR_NAME) != null;
   }
 
   public static String getTokenForSession(HttpSession session) {
     synchronized (session) {
       String token = (String) session.getAttribute(CSRF_SESSION_ATTR_NAME);
       if (token == null) {
         token = UUID.randomUUID().toString();
         session.setAttribute(CSRF_SESSION_ATTR_NAME, token);
       }
       return token;
     }
   }
 
   public static String getTokenFromRequest(HttpServletRequest request) {
     return request.getParameter(CSRF_PARAM_NAME);
   }
 }
