 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.core.persistence.ConfigurationException;
 import org.gridlab.gridsphere.core.persistence.RestoreException;
 import org.gridlab.gridsphere.core.persistence.PersistenceException;
 import org.gridlab.gridsphere.core.persistence.castor.PersistenceManagerXml;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.PortletConfig;
 
 import javax.servlet.ServletConfig;
 import java.util.Iterator;
 import java.util.Vector;
 import java.util.List;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 
 public class PortletLayoutDescriptor {
 
     private static PortletLog log = org.gridlab.gridsphere.portlet.impl.SportletLog.getInstance(PortletLayoutDescriptor.class);
     private PortletContainer pc = null;
     private String layoutPath, layoutMappingPath;
 
    public PortletLayoutDescriptor(PortletConfig config) throws PortletLayoutDescriptorException {
         // load in layout.xml file
         String appRoot = config.getServletContext().getRealPath("") + "/";
         String portletConfigFile = config.getInitParameter("layout.xml");
         String portletMappingFile = config.getInitParameter("layout-mapping.xml");
         if (portletConfigFile == null) {
             portletConfigFile = "WEB-INF/conf/layout.xml";
         }
         if (portletMappingFile == null) {
             portletMappingFile = "WEB-INF/conf/layout-mapping.xml";
         }
         layoutPath = appRoot + portletConfigFile;
         layoutMappingPath = appRoot + portletMappingFile;
         load(layoutPath, layoutMappingPath);
     }
 
     /**
      * Constructs a PortletDeploymentDescriptor from a portlet.xml and mapping file
      *
      * @param portletFilePath location of the layout.xml
      * @param mappingFilePath location of the mapping file
      * @throws PortletLayoutDescriptorException if the PortletLayoutDescriptor cannot be created
      */
    public PortletLayoutDescriptor(String layoutPath, String layoutMappingPath) throws PortletLayoutDescriptorException  {
         load(layoutPath, layoutMappingPath);
     }
 
     /**
      * Returns the portlet container associated with this descriptor
      *
      * @return the portlet container
      */
     public PortletContainer getPortletContainer() {
         return pc;
     }
 
    public void reload() throws PortletLayoutDescriptorException {
         load(layoutPath, layoutMappingPath);
     }
 
     /**
      * Loads the PortletLayoutDescriptor from the layout.xml
      *
      * @param url the location of the portlet.xml file
      * @param mapping the location of the portlet xml mapping file
      * @throws PortletDeploymentDescriptorException if the PortletDeploymentDescriptor cannot be created
      */
    public void load(String url, String mapping) throws PortletLayoutDescriptorException  {
         PersistenceManagerXml pmx = PersistenceManagerXml.getInstance();
 
         // where is the portlet.xml ?
         pmx.setConnectionURL(url);
 
         // set the path to the mapping file
         pmx.setMappingFile(mapping);
 
         // try to get it
         try {
              pc = (PortletContainer)pmx.restoreObject();
         } catch (PersistenceException e) {
             log.error("PersistenceException ("+pmx.getMappingFile()+", "+pmx.getConnectionURL()+") ", e);
             throw new PortletLayoutDescriptorException("Unable to load portlet: "+e.getMessage());
         }
     }
 
     /**
      * Save the layout deployment descriptor to layout.xml
      * <b>not implemented yet</b>
      */
    public void save(String url, String mapping) throws PortletLayoutDescriptorException {
         PersistenceManagerXml pmx = PersistenceManagerXml.getInstance();
 
         // where is the portlet.xml ?
         pmx.setConnectionURL(url);
 
         // set the path to the mapping file
         pmx.setMappingFile(mapping);
 
 
         // try to get it
         try {
              pmx.update(pc);
         } catch (PersistenceException e) {
             log.error("PersistenceException: ("+pmx.getMappingFile()+", "+pmx.getConnectionURL()+") ", e);
             throw new PortletLayoutDescriptorException("Unable to save: "+e.getMessage());
         }
     }
 
 }
