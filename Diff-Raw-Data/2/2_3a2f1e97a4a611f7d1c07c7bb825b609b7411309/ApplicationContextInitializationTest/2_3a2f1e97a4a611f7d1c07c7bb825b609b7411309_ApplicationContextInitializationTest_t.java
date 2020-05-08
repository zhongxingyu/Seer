 package edu.northwestern.bioinformatics.studycalendar;
 
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import org.springframework.context.ApplicationContext;
 import org.springframework.core.io.FileSystemResourceLoader;
 import org.springframework.mock.web.MockServletContext;
 import org.springframework.web.context.support.XmlWebApplicationContext;
 
 /**
 * These tests are intended to verify that the various application contexts will all load when
  * the application is deployed.
  *
  * @author Rhett Sutphin
  */
 public class ApplicationContextInitializationTest extends StudyCalendarTestCase {
 
     private MockServletContext servletContext;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         servletContext = new MockServletContext("src/main/webapp", new FileSystemResourceLoader());
     }
 
     public void testApplicationContextItself() throws Exception {
         getDeployedApplicationContext();
         // no exceptions
     }
 
     public void testSpringServletContext() throws Exception {
         assertDispatcherServletConfigLoads("spring");
     }
 
     public void testPublicServletContext() throws Exception {
         assertDispatcherServletConfigLoads("public");
     }
 
     private void assertDispatcherServletConfigLoads(String servletName) throws Exception {
         try {
             createWebApplicationContextForServlet(servletName);
         } catch (Exception e) {
             throw new StudyCalendarError("Loading the configuration for MVC servlet '" + servletName
                 + "' failed:  " + e.getMessage(), e);
         }
     }
 
     private XmlWebApplicationContext createWebApplicationContextForServlet(String servletName) {
         ApplicationContext parent = getDeployedApplicationContext();
         XmlWebApplicationContext context = new XmlWebApplicationContext();
         context.setParent(parent);
         context.setServletContext(servletContext);
         context.setConfigLocations(new String[] { "WEB-INF/" + servletName + "-servlet.xml" });
         context.refresh();
         return context;
     }
 }
