 package org.synyx.minos.core.web;
 
 import com.opensymphony.module.sitemesh.Config;
 import com.opensymphony.module.sitemesh.Decorator;
 import com.opensymphony.module.sitemesh.DecoratorMapper;
 import com.opensymphony.module.sitemesh.Page;
 import com.opensymphony.module.sitemesh.mapper.ConfigDecoratorMapper;
 import com.opensymphony.module.sitemesh.mapper.ConfigLoader;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import java.util.Properties;
 
 
 /**
  * Custom {@link ConfigDecoratorMapper} that resolves configured decorator-patterns for SpringMVCs Dispatcher-Servlet
  * and direct requests to JSPs.
  * 
  * @author Marc Kannegiesser - kannegiesser@synyx.de
  */
 public class SitemeshDecoratorMapper extends ConfigDecoratorMapper {
 
     private static final String DEFAULT_CONFIGURATION = "/WEB-INF/decorators.xml";
 
     private ConfigLoader configLoader = null;
 
 
     /** Create new ConfigLoader using the {@link #DEFAULT_CONFIGURATION} file. */
     public void init(Config config, Properties properties, DecoratorMapper parent) throws InstantiationException {
 
         super.init(config, properties, parent);
         try {
             String fileName = properties.getProperty("config", DEFAULT_CONFIGURATION);
             configLoader = new ConfigLoader(fileName, config);
         } catch (Exception e) {
            throw new InstantiationException(e.toString());
         }
     }
 
 
     /**
      * Retrieve {@link com.opensymphony.module.sitemesh.Decorator} based on 'pattern' tag.
      */
     public Decorator getDecorator(HttpServletRequest request, Page page) {
 
         String thisPath = getViewNameFromRequest(request);
 
         if (thisPath == null) {
             thisPath = request.getPathInfo();
             if (thisPath == null) {
                 thisPath = request.getRequestURI();
                 if (thisPath != null) {
                     String contextPath = request.getSession().getServletContext().getContextPath();
                     thisPath = thisPath.substring(contextPath.length(), thisPath.length());
                 }
             }
         }
 
         String name = null;
         try {
             name = configLoader.getMappedName(thisPath);
         } catch (ServletException e) {
            e.printStackTrace();
         }
 
         Decorator result = getNamedDecorator(request, name);
         return result == null ? super.getDecorator(request, page) : result;
     }
 
 
     protected String getViewNameFromRequest(HttpServletRequest request) {
 
         Object viewName = request.getAttribute(ViewWebRequestEnricher.VIEWNAME_ATTRIBUTE);
         if (viewName != null && (viewName instanceof String)) {
             return (String) viewName;
         }
 
         return null;
     }
 
 
     /**
      * Retrieve Decorator named in 'name' attribute. Checks the role if specified.
      */
     public Decorator getNamedDecorator(HttpServletRequest request, String name) {
 
         Decorator result = null;
         try {
             result = configLoader.getDecoratorByName(name);
         } catch (ServletException e) {
            e.printStackTrace();
         }
 
         if (result == null || (result.getRole() != null && !request.isUserInRole(result.getRole()))) {
             // if the result is null or the user is not in the role
             return super.getNamedDecorator(request, name);
         } else {
             return result;
         }
     }
 }
