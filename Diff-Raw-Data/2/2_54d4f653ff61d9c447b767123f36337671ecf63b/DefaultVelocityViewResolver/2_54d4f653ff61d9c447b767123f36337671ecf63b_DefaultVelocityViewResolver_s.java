 package com.expressmvc.view.impl;
 
 import com.expressioc.annotation.Singleton;
 import com.expressmvc.AppInitializer;
 import com.expressmvc.view.View;
 import com.expressmvc.view.ViewResolver;
 import com.google.common.base.Strings;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.exception.VelocityException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.http.HttpServletRequest;
 import java.util.Properties;
 
 @Singleton
 public class DefaultVelocityViewResolver implements ViewResolver, AppInitializer {
     public static final String PRE_FIX = "/WEB-INF/views";
    public static final String DEFAULT_VIEW = "/show.vm";
     public static final String TEMPLATE_POSTFIX = ".vm";
 
     @Override
     public View findView(HttpServletRequest request, String viewName) {
         return new VelocityView(findViewTemplatePath(request, viewName));
     }
 
     private String findViewTemplatePath(HttpServletRequest req, String viewName) {
         String pathInContext = req.getRequestURI().substring(req.getContextPath().length());
         String viewTemplateName = Strings.isNullOrEmpty(viewName) ? DEFAULT_VIEW : viewName + TEMPLATE_POSTFIX;
         return pathInContext + "/" + viewTemplateName;
     }
 
     @Override
     public void init(ServletConfig config) {
         Properties properties = new Properties();
         String basePath = System.getProperty("user.dir") + config.getServletContext().getContextPath() + PRE_FIX;
         properties.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, basePath);
         try {
             Velocity.init(properties);
         } catch (Exception e) {
             throw new VelocityException(e);
         }
     }
 }
