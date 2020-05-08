 /*
  * Created on 24 Nov 2006
  */
 package uk.org.ponder.springutil;
 
 import javax.servlet.ServletContext;
 
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.core.io.DefaultResourceLoader;
 import org.springframework.core.io.Resource;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.ServletContextResource;
 
 import uk.org.ponder.servletutil.ServletContextLocator;
 
public class ExtraContextResourceLoader extends DefaultResourceLoader {
 
   private ApplicationContext applicationContext;
   private WebApplicationContext wac;
  
   private ServletContextLocator servletContextLocator;
  
  public void setServletContextLocator(ServletContextLocator servletContextLocator) {
     this.servletContextLocator = servletContextLocator;
   }
 
   public Resource getResource(String location) {
     if (wac != null && location.startsWith("/..")) {
       int slashpos = location.indexOf('/', 4);
       String uripath = location.substring(3, slashpos); // include leading /
       String relpath = location.substring(slashpos + 1);
       ServletContext extracontext = null;
       if (servletContextLocator != null) {
         extracontext = servletContextLocator.locateContext(uripath);
       }
       if (extracontext == null) {
         extracontext = wac.getServletContext().getContext(uripath);
       }
       if (extracontext == null) {
         return null;
       }
       else {
         return new ServletContextResource(extracontext, relpath);
       }
     }
    else return applicationContext.getResource(location);
   }
 
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
     this.applicationContext = applicationContext;
     if (applicationContext instanceof WebApplicationContext) {
       this.wac = (WebApplicationContext) applicationContext;
     }
   }
 
 }
