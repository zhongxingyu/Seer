 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portletcontainer.jsrimpl;
 
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
 import org.gridlab.gridsphere.portlet.PortletException;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
 import org.gridlab.gridsphere.portletcontainer.ApplicationPortlet;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;
 import org.gridlab.gridsphere.portletcontainer.PortletWebApplication;
 import org.gridlab.gridsphere.portletcontainer.impl.BasePortletWebApplicationImpl;
 import org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.*;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import java.io.File;
 import java.util.Hashtable;
 import java.util.Map;
 
 /**
  * The <code>PortletWebApplicationImpl</code> is an implementation of a <code>PortletWebApplication</code> that
  * represents a collection of portlets contained in a packaged WAR file. Currently
  * under development is the notion of dynamically managing portlet web applications.
  */
 public class JSRPortletWebApplicationImpl extends BasePortletWebApplicationImpl implements PortletWebApplication {
 
     private PortletLog log = SportletLog.getInstance(JSRPortletWebApplicationImpl.class);
     private PortletApp portletWebApp = null;
 
     protected Map portletDefinitions = new Hashtable();
     protected RequestDispatcher rd = null;
 
     // PortletLayout engine handles layout.xml
     //private PortletLayoutEngine layoutEngine = PortletLayoutEngine.getInstance();
 
     public JSRPortletWebApplicationImpl(ServletContext context, ClassLoader loader) throws PortletException {
         super(context);
         String realPath = context.getRealPath("");
         int l = realPath.lastIndexOf(File.separator);
         String appName = realPath.substring(l + 1);
 
         // Make all jsr portlets have only one concrete instance
         webApplicationName = appName;
         //webApplicationName = appName + ".1";
 
         this.webAppDescription = context.getServletContextName();
 
         // load portlet.xml
         loadPortlets(context);
 
         // load services.xml
        loadServices(context);
 
         //load roles.xml
         loadRoles(context);
 
         // load group.xml
         loadGroup(context);
     }
 
 
     /**
      * Loads collection of portlets from portlet descriptor file using the associated <code>ServletContext</code>
      *
      * @param ctx the <code>ServletContext</code>
      */
     protected void loadPortlets(ServletContext ctx) throws PortletException {
         // load in the portlet.xml file
         String portletXMLfile = ctx.getRealPath("/WEB-INF/portlet.xml");
         //String portletMappingFile = GridSphereConfig.getProperty(GridSphereConfigProperties.PORTLET_MAPPING);
 
         String portletMappingFile = GridSphereConfig.getServletContext().getRealPath("/WEB-INF/mapping/portlet-jsr-mapping.xml");
 
         PortletDeploymentDescriptor2 pdd = null;
         try {
             pdd = new PortletDeploymentDescriptor2(portletXMLfile, portletMappingFile);
         } catch (Exception e) {
             log.error("Mapping Error! " + webApplicationName, e);
             throw new PortletException("Unable to load portlets from: " + webApplicationName + " + due to mapping error", e);
         }
 
         this.portletWebApp = pdd.getPortletWebApplication();
         // Every SportletDefinition has a PortletApplication and possibly multiple ConcretePortletConfig's
         PortletDefinition[] portletDefs = pdd.getPortletDefinitionList();
 
 
         // Iterate thru portlet definitions for portlet applications
         for (int i = 0; i < portletDefs.length; i++) {
             ApplicationPortlet portletApp = new JSRApplicationPortletImpl(pdd, portletDefs[i], webApplicationName, ctx);
 
             String portletClass = portletApp.getApplicationPortletID();
             String portletName = portletApp.getApplicationPortletName();
             portletDefinitions.put(portletName, portletDefs[i]);
             appPortlets.put(portletName, portletApp);
 
             log.debug("sticking " + portletName + " class: " + portletClass + " in hash");
         }
 
     }
 
     public PortletDefinition getPortletDefinition(String portletName) {
         return (PortletDefinition) portletDefinitions.get(portletName);
     }
 
     public void init() {}
 
     public void destroy() {
         //log.debug("removing application tab :" + webApplicationName);
         //PortletTabRegistry.removeGroupTab(webApplicationName);
         log.debug("unloading portlet services: ");
         SportletServiceFactory factory = SportletServiceFactory.getInstance();
         factory.shutdownServices(webApplicationName);
         PersistenceManagerFactory.destroyPersistenceManagerRdbms(webApplicationName);
         portletWebApp = null;
         appPortlets = null;
         portletDefinitions = null;
         rd = null;
     }
 
     public CustomPortletMode[] getCustomPortletModes() {
         return portletWebApp.getCustomPortletMode();
     }
 
     public CustomWindowState[] getCustomWindowStates() {
         return portletWebApp.getCustomWindowState();
     }
 
     public UserAttribute[] getUserAttributes() {
         return portletWebApp.getUserAttribute();
     }
 
     public SecurityConstraint[] getSecurityConstraints() {
         return portletWebApp.getSecurityConstraint();
     }
 }
