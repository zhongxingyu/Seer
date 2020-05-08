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
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 import com.google.walkaround.util.server.servlet.BadRequestException;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 @Singleton
 public class AdminAuthFilter implements Filter {
 
   private static final Logger log = Logger.getLogger(AdminAuthFilter.class.getName());
   private final Provider<UserContext> userCtx;
 
   @Inject
   public AdminAuthFilter(Provider<UserContext> userCtx) {
     this.userCtx = userCtx;
   }
 
   @Override
   public void destroy() {
   }
 
   @Override
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
       throws IOException, ServletException {
     if (!userCtx.get().isUserAdmin()) {
       log.severe("Admin page requested by non-admin user: "
           + (userCtx.get().hasParticipantId() ? userCtx.get().getParticipantId()
               : "(not logged in)"));
       throw new BadRequestException();
     }
    filterChain.doFilter(request, response);
   }
 
   @Override
   public void init(FilterConfig arg0) throws ServletException {
   }
 }
