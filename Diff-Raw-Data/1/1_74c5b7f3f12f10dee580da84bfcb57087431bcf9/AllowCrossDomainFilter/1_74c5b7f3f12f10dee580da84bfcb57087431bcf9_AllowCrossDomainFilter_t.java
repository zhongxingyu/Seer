 package com.crowdstore.web.common.interceptors;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.filter.OncePerRequestFilter;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * @author fcamblor
  */
 public class AllowCrossDomainFilter extends OncePerRequestFilter {
     static Logger LOG = LoggerFactory.getLogger(AllowCrossDomainFilter.class);
 
     @Override
     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
         // CORS "pre-flight" request
         if ("dev".equals(System.getProperty("env"))) {
             // Allowing yeoman server requests
             response.addHeader("Access-Control-Allow-Origin", "http://localhost:3501");
             response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
             response.addHeader("Access-Control-Allow-Headers", "origin, x-csrftoken, x-requested-with, content-type, accept");
            // More infos there : http://stackoverflow.com/a/7189502/476345
             response.addHeader("Access-Control-Allow-Credentials", "true");
             response.addHeader("Access-Control-Max-Age", "1800");//30 min
         }
 
         filterChain.doFilter(request, response);
     }
 }
