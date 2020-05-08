 package com.edify.web.servlet;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 import org.springframework.web.servlet.support.RequestContextUtils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.UUID;
 
 /**
  * @author jarias
  * @since 6/2/12 7:10 PM
  */
public class EnvironmentInterceptor extends HandlerInterceptorAdapter {
     public static final String ENVIRONMENT_PARAM_NAME = "env";
     public static final String ASSETS_CACHE_BUSTER_PARAM_NAME = "cacheBuster";
 
     //Use for cache busting
     @Value("${GIT_SHA}")
     private String gitSha;
 
     @Override
     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
         WebApplicationContext webApplicationContext = RequestContextUtils.getWebApplicationContext(request);
         String[] activeProfiles = webApplicationContext.getEnvironment().getActiveProfiles();
 
         if (ArrayUtils.contains(activeProfiles, "production")) {
             request.setAttribute(ENVIRONMENT_PARAM_NAME, "production");
             request.setAttribute(ASSETS_CACHE_BUSTER_PARAM_NAME, gitSha);
         } else {
             request.setAttribute(ENVIRONMENT_PARAM_NAME, "development");
             //In development set the cache buster to a random UUID to avoid caching of static assets
             request.getSession().setAttribute(ASSETS_CACHE_BUSTER_PARAM_NAME, UUID.randomUUID());
         }
         return true;
     }
 }
