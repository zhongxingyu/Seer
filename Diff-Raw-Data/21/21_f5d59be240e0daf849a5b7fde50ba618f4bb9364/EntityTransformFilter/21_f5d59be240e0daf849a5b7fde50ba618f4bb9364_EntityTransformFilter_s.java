 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.falcon.filter;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletInputStream;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.falcon.resource.proxy.BufferedRequest;
 
 /**
  * Entity xml filter to replace 'ivory' with 'falcon'.
  */
 public class EntityTransformFilter implements Filter {
    public static final Pattern IVORY_NS = Pattern.compile(".*xmlns=\"uri:(ivory):(cluster|feed|process):0\\.1\".*");
 
     /**
      * Request wrapper to override input stream.
      */
     public static class CustomHttpRequestWrapper extends HttpServletRequestWrapper {
 
         private final String entityXml;
 
         public CustomHttpRequestWrapper(HttpServletRequest request, String in) {
             super(request);
             this.entityXml = in;
         }
 
         @Override
         public ServletInputStream getInputStream() throws IOException {
             return getServletInputStream(IOUtils.toInputStream(entityXml));
         }
 
         @Override
         public BufferedReader getReader() throws IOException {
             return new BufferedReader(new InputStreamReader(IOUtils.toInputStream(entityXml)));
         }
 
         private ServletInputStream getServletInputStream(final InputStream stream) {
             return new ServletInputStream() {
                 @Override
                 public int read() throws IOException {
                     return stream.read();
                 }
             };
         }
     }
 
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
     }
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
                                                                         IOException, ServletException {
         if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
             throw new IllegalStateException("Invalid request/response object");
         }
         HttpServletRequest httpRequest = (HttpServletRequest) request;
 
        String contextPath = httpRequest.getContextPath();
         BufferedRequest bufferedRequest = new BufferedRequest(httpRequest);
        if (contextPath.contains("submit") || contextPath.contains("update")
                || contextPath.contains("submitAndSchedule")) {
             StringWriter writer = new StringWriter();
             IOUtils.copy(bufferedRequest.getInputStream(), writer);
             bufferedRequest.getInputStream().reset();
             String entityXml = writer.toString();
             Matcher matcher = IVORY_NS.matcher(entityXml);
             if (matcher.matches()) {
                 entityXml = entityXml.substring(0, matcher.start(1)) + "falcon"
                                                + entityXml.substring(matcher.end(1), entityXml.length());
                 CustomHttpRequestWrapper requestWrapper = new CustomHttpRequestWrapper(httpRequest,
                         entityXml);
                 chain.doFilter(requestWrapper, response);
                 return;
             }
         }
         chain.doFilter(bufferedRequest, response);
     }
 
     @Override
     public void destroy() {
     }
 }
