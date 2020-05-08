 package server;
 
 import java.io.File;
import java.lang.reflect.Field;
 
 import javax.servlet.ServletContextEvent;
 
 import org.apache.commons.lang.StringUtils;
 
 import play.Logger;
 import play.Play;
 import play.exceptions.UnexpectedException;
import play.mvc.Router;
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

 		try {
	        Play.ctxPath = contextPath;
	        Router.load(contextPath);
	        Field field = ServletWrapper.class.getDeclaredField("routerInitializedWithContext");
	        field.setAccessible(true);
	        field.set(null, Boolean.TRUE);
 		} 
 		catch (Exception e) {
			Logger.error(e,"Cannot invoke 'Router' method");
			throw new RuntimeException("Cannot invoke 'Router' method", e);
 		}
     }
  
 	
 }
