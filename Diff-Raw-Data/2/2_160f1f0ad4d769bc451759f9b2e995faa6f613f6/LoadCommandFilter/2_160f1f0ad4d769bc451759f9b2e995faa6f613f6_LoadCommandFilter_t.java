 /*
  * Copyright 2013 Mobile Helix, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.helix.mobile.filters;
 
 import java.io.IOException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.helix.mobile.model.LoadCommandAction;
 
 /**
  *
  * @author shallem
  */
 public class LoadCommandFilter implements Filter {
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         HttpServletRequest req = (HttpServletRequest) request;
         HttpServletResponse res = (HttpServletResponse) response;
         
         String loadKey = req.getParameter("__hxLoadKey");
         if (loadKey != null) {
             LoadCommandAction lca = (LoadCommandAction)req.getServletContext().getAttribute(loadKey);
             
             /* Run the load command, then get the result. */
             Object thisObj = req.getAttribute("folderView"); 
                     //lca.doLoad();
             String jsonToReturn = lca.getAndSerialize(thisObj);
             
             response.setContentType("application/json");
             response.getWriter().write(jsonToReturn);
             response.flushBuffer();
         } else {
             chain.doFilter(request, response);
         }
     }
 
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void destroy() {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
 }
