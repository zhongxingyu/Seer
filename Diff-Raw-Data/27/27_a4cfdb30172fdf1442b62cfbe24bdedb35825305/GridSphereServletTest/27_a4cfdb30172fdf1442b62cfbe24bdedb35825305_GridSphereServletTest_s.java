 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portletcontainer;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import org.apache.cactus.ServletTestCase;
 import org.gridlab.gridsphere.portlet.PortletContext;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletContext;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
 
 /**
  * This is the base fixture for service testing. Provides a service factory and the
  * properties file.
  */
 public class GridSphereServletTest extends ServletTestCase {
 
     protected static SportletServiceFactory factory = null;
     protected static PortletLog log = SportletLog.getInstance(GridSphereServletTest.class);
     protected PortletContext context = null;
     protected GridSphereServlet gsServlet = null;
 
     public GridSphereServletTest(String name) {
         super(name);
     }
 
     protected void setUp() {
 
     }
 
     public static void main(String[] args) {
         junit.textui.TestRunner.run(suite());
     }
 
    public void testCreateServlet() {
         gsServlet = new GridSphereServlet();
         assertNotNull(gsServlet);
         try {
             gsServlet.init(config);
         } catch (Exception e) {
            fail("Unable to initialize GridSphere Portlet services!");
         }
         this.context = new SportletContext(config);
         assertNotNull(context);
     }
 
     public static Test suite() {
         return new TestSuite(GridSphereServletTest.class);
     }
 
     protected void tearDown() {
     }
 }
