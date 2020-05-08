 package server;
 
 import java.io.File;
import java.lang.reflect.Method;
 
 import javax.servlet.ServletContextEvent;
 
 import org.apache.commons.lang.StringUtils;
 
 import play.Logger;
 import play.Play;
 import play.exceptions.UnexpectedException;
 import play.server.ServletWrapper;
 
 /**
  * Subclass the Play servlet wrapper to make the {#link Play.usePrecompiled} field configurable
  * 
  * @author Paolo Di Tommaso
  *
  */
 public class TServerServlet extends ServletWrapper {
 
 	
     public void contextInitialized(ServletContextEvent e) {
         String appDir = e.getServletContext().getRealPath("/WEB-INF/application");
         File root = new File(appDir);
         final String playId = e.getServletContext().getInitParameter("play.id");
         if (StringUtils.isEmpty(playId)) {
             throw new UnexpectedException("Please define a play.id parameter in your web.xml file. Without that parameter, play! cannot start your application. Please add a context-param into the WEB-INF/web.xml file.");
         }
         // This is really important as we know this parameter already (we are running in a servlet container)
         Play.frameworkPath = root.getParentFile();
         Play.usePrecompiled = "true".equals(e.getServletContext().getInitParameter("use.precompiled"));
         Play.init(root, playId);
         Play.Mode mode = Play.Mode.valueOf(Play.configuration.getProperty("application.mode", "DEV").toUpperCase());
         if (mode.isDev()) {
             Logger.info("Forcing PROD mode because deploying as a war file.");
         }
 
         // Servlet 2.4 does not allow you to get the context path from the servletcontext...
         if (isGreaterThan(e.getServletContext(), 2, 4)) {
             loadRouter(e.getServletContext().getContextPath());
         }
     }
     
     private void loadRouter(String contextPath) {
    	Method method;
 		try {
			method = ServletWrapper.class.getDeclaredMethod("loadRouter", String.class);
	    	method.setAccessible(true);
	    	method.invoke(null, contextPath);
 		} 
 		catch (Exception e) {
			throw new RuntimeException("Cannot invoke 'laodRoter' method", e);
 		}
     }
  
 	
 }
