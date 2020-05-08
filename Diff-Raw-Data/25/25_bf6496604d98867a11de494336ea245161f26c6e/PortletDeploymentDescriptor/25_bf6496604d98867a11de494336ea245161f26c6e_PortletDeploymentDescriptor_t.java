 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portletcontainer.descriptor;
 
 import org.gridlab.gridsphere.core.persistence.ConfigurationException;
 import org.gridlab.gridsphere.core.persistence.RestoreException;
 import org.gridlab.gridsphere.core.persistence.castor.PersistenceManagerXml;
 
 import java.util.Iterator;
 import java.util.Vector;
 
 public class PortletDeploymentDescriptor {
 
 
     static org.apache.log4j.Category cat = org.apache.log4j.Category.getInstance(PortletDeploymentDescriptor.class.getName());
 
    private Vector PortletApp;
 
     /**
     * construcs PortletDeploymentDescriptor
      * @param url localtion of the portlet.xml
      * @param mapping localtion of the mapping file
      */
     public PortletDeploymentDescriptor(String url, String mapping) throws PortletDeploymentDescriptorException  {
         load(url, mapping);
     }
 
     /**
      *  returns a vector of portletapps
      */
     public Vector getPortletApp() {
         return PortletApp;
     };
 
     /**
      * sets the PortletApp Vector
      */
     private void setPortletApp(Vector v) {
         PortletApp = v;
     }
 
     /**
      * creates a new PortletApp (which is a vector)
      */
  /*  public PortletApp createPortletApp() {
         return new PortletApp();
     }
  */
     /**
      *  <b>not implemented yet</b>
      */
    public void addPortletApp() {};
 
 
     public void load(String url, String mapping) throws PortletDeploymentDescriptorException  {
         PersistenceManagerXml pmx = new PersistenceManagerXml();
 
         // where is the portlet.xml ?
         pmx.setConnectionURL(url);
 
         // set the path to the mapping file
         pmx.setMappingFile(mapping);
 
         PortletDefinition pd = null;
 
         // try to get it
         try {
              pd = (PortletDefinition)pmx.restoreObject();
         } catch (RestoreException e) {
            cat.error("RestoreError "+e.getMessage());
            throw new PortletDeploymentDescriptorException("Unable to restore: "+e.getMessage());
         } catch (ConfigurationException e) {
             cat.error("ConfigurationError "+e);
            throw new PortletDeploymentDescriptorException("Configuration error: "+e.getMessage());
         }
 
         this.setPortletApp(pd.getPortletAppList());
     }
 
     /**
      * <b>not implemented yet</b>
      */
    public void save() {};
 
 }
