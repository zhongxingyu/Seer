 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.servlet;
 
 import com.janrain.backplane.server.BackplaneServerException;
 import com.janrain.backplane.server.utils.BackplaneSystemProps;
 import com.janrain.backplane2.server.InvalidRequestException;
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Pattern;
 
 /**
  * @author Tom Raney
  */
 public class IPRangeFilter implements Filter {
 
     Pattern[] whiteListProxies;
 
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
         String ipWhiteList = System.getProperty(BackplaneSystemProps.IP_WHITE_LIST);
         if (StringUtils.isNotBlank(ipWhiteList)) {
             List<String> items = Arrays.asList(ipWhiteList.split("\\s*,\\s*"));
             ArrayList<Pattern> patternArrayList = new ArrayList<Pattern>();
             for (String item : items) {
                 patternArrayList.add(Pattern.compile(item));
             }
             whiteListProxies = (Pattern[]) patternArrayList.toArray(new Pattern[0]);
             logger.info("white listed ips: " + ipWhiteList);
         }
     }
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         HttpServletResponse httpResponse = (HttpServletResponse) response;
 
         // this call will give us the load balancer IP, if it exists, which we don't want...
         String remoteIp = httpRequest.getRemoteAddr();
 
         //if we're behind a load balancer, we'll get the caller's IP address in the "x-forwarded-for" header:
         //X-Forwarded-For: xxx.xxx.xxx.xxx, ...
         String forwardedIps = httpRequest.getHeader("x-forwarded-for");
         if (StringUtils.isNotBlank(forwardedIps)) {
             List<String> items = Arrays.asList(forwardedIps.split("\\s*,\\s*"));
             // there may be more than one, but we're only interested in the first item
             if (!items.isEmpty()) {
                 remoteIp = items.get(0);
             }
         }
 
         // match first on internal ips...
         if (matches(remoteIp, internalProxies) || matches(remoteIp, whiteListProxies)) {
             chain.doFilter(request,response);
         } else {
            logger.warn("remote address restricted: " + remoteIp + " for " + ((HttpServletRequest) request).getRequestURI());
             throw new InvalidRequestException("Access restricted");
         }
     }
 
     @Override
     public void destroy() {
 
     }
 
     // - PRIVATE
 
     private static final Logger logger = Logger.getLogger(IPRangeFilter.class);
 
     private boolean matches(String ip, Pattern... patterns) {
         if (patterns == null) {
             return false;
         }
         for (Pattern pattern: patterns) {
             if (pattern.matcher(ip).matches()) {
                 return true;
             }
         }
         return false;
     }
 
     // see org.apache.catalina.filters.RemoteIpFilter source...
     private Pattern[] internalProxies = new Pattern[] {
         Pattern.compile("10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"), Pattern.compile("192\\.168\\.\\d{1,3}\\.\\d{1,3}"),
         Pattern.compile("169\\.254\\.\\d{1,3}\\.\\d{1,3}"), Pattern.compile("227\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")
     };
 }
