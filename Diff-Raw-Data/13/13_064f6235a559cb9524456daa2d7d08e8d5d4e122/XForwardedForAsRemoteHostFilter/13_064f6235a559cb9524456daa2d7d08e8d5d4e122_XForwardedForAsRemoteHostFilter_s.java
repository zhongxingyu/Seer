 /*
  * Copyright 2002-2008 the original author or authors.
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
 package fr.xebia.servlet.filter;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 
 /**
  * @author <a href="mailto:cyrille.leclerc@pobox.com">Cyrille Le Clerc</a>
  */
 public class XForwardedForAsRemoteHostFilter implements Filter {
     
     /**
      * TODO add headers <code>x-original-remote-host</code> and <code>x-original-remote-addr</code> if
      * {@link ServletRequest#getRemoteHost()} and {@link ServletRequest#getRemoteHost()} are overridden by <code>x-forwarded-for</code> http
      * header value.
      */
     public static class XForwardedForAsRemoteHostRequest extends HttpServletRequestWrapper {
         
         protected List<String> headerNames;
         
         protected String originalRemoteAddr;
         
         protected String originalRemoteHost;
         
         protected String xForwardedFor;
         
         public XForwardedForAsRemoteHostRequest(HttpServletRequest request) {
             super(request);
             
             xForwardedFor = request.getHeader(X_FORWARDED_FOR_HEADER_NAME);
             if (xForwardedFor == null || xForwardedFor.length() == 0) {
                 throw new IllegalStateException("xForwardedFor can NOT be empty");
             }
             
             originalRemoteAddr = request.getRemoteAddr();
             originalRemoteHost = request.getRemoteHost();
         }
         
         @Override
         public String getHeader(String name) {
            if (X_ORIGINAL_REMOTE_ADDR_HEADER_NAME.equalsIgnoreCase(name) || X_ORIGINAL_REMOTE_HOST_HEADER_NAME.equalsIgnoreCase(name)) {
                return xForwardedFor;
             } else {
                 return super.getHeader(name);
             }
         }
         
         @SuppressWarnings("unchecked")
         @Override
         public Enumeration<?> getHeaderNames() {
             if (headerNames == null) {
                 headerNames = Collections.list(super.getHeaderNames());
                 headerNames.add(X_ORIGINAL_REMOTE_ADDR_HEADER_NAME);
                 headerNames.add(X_ORIGINAL_REMOTE_HOST_HEADER_NAME);
             }
             return Collections.enumeration(headerNames);
         }
         
         @Override
         public Enumeration<?> getHeaders(String name) {
            if (X_ORIGINAL_REMOTE_ADDR_HEADER_NAME.equalsIgnoreCase(name) || X_ORIGINAL_REMOTE_HOST_HEADER_NAME.equalsIgnoreCase(name)) {
                return Collections.enumeration(Collections.singletonList(xForwardedFor));
             } else {
                 return super.getHeaders(name);
             }
         }
         
         @Override
         public String getRemoteAddr() {
             return xForwardedFor;
         }
         
         @Override
         public String getRemoteHost() {
             return xForwardedFor;
         }
     }
     
     private static final String X_FORWARDED_FOR_HEADER_NAME = "x-forwarded-for";
     
     private static final String X_ORIGINAL_REMOTE_ADDR_HEADER_NAME = "x-original-remote-addr";
     
     private static final String X_ORIGINAL_REMOTE_HOST_HEADER_NAME = "x-original-remote-host";
     
     public void destroy() {
     }
     
     /**
      * Wrap the incoming <code>request</code> in a {@link XForwardedForAsRemoteHostRequest} if the http header <code>x-forwareded-for</code>
      * is not empty.
      */
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         if (request instanceof HttpServletRequest) {
             HttpServletRequest httpServletRequest = (HttpServletRequest)request;
             String xForwardedFor = httpServletRequest.getHeader(X_FORWARDED_FOR_HEADER_NAME);
             
             if (xForwardedFor == null || xForwardedFor.length() == 0) {
                 chain.doFilter(request, response);
             } else {
                 chain.doFilter(new XForwardedForAsRemoteHostRequest(httpServletRequest), response);
             }
         } else {
             chain.doFilter(request, response);
         }
     }
     
     public void init(FilterConfig filterConfig) throws ServletException {
     }
     
 }
