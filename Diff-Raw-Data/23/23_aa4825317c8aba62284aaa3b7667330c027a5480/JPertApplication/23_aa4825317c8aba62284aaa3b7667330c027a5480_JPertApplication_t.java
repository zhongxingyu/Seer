 package de.elendis.jpert.webapp;
 
 import com.vaadin.annotations.Title;
 import de.elendis.jpert.service.Registry;
 import de.elendis.jpert.service.TaskService;
 import de.elendis.jpert.webapp.view.JPertMainWindow;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.jar.Attributes;
 import java.util.jar.Manifest;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.server.VaadinServlet;
 import com.vaadin.ui.UI;
 
 /**
  *
  * @author skywalker
  */
 @Title("jPERT")
 public class JPertApplication extends UI {
     /* Prefix for JNDI Lookups */
 
     public static final String JNDI_PREFIX = "java:global/";
     private JPertMainWindow mainWindow;
 
     @Override
     public void init(VaadinRequest vaadinRequest) {
         lookupAndRegisterTaskService();
         mainWindow = new JPertMainWindow();
         mainWindow.init();
         mainWindow.setSizeFull();
         addWindow(mainWindow);
     }
 
     public void lookupAndRegisterTaskService() {
         try {
             VaadinServlet ctx = VaadinServlet.getCurrent();
 
             InputStream inputStream = ctx.getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF");
             Manifest manifest = new Manifest(inputStream);
             Attributes attributes = manifest.getMainAttributes();
 
             String title = attributes.getValue("Implementation-Title");
             String version = attributes.getValue("Implementation-Version");
 
             String jndiName = JNDI_PREFIX + title + "-" + version + "/TaskServiceImpl";
 
             Registry.register(Registry.RegistryKeys.TASK_SERVICE, (TaskService) InitialContext.doLookup(jndiName));
         } catch (NamingException | IOException e) {
             System.err.println(e);
         }
     }
 }
