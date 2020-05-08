 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portletcontainer.descriptor;
 
 import org.gridlab.gridsphere.core.persistence.castor.PersistenceManagerXml;
 import org.gridlab.gridsphere.core.persistence.castor.descriptor.DescriptorException;
import org.gridlab.gridsphere.core.persistence.castor.descriptor.Descriptor;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 public class PortletDeploymentDescriptor extends Descriptor {
 
     private static PortletLog log = SportletLog.getInstance(PortletDeploymentDescriptor.class);
     private PersistenceManagerXml pmx = PersistenceManagerXml.getInstance();
     private List PortletDef = new ArrayList();
     private PortletCollection pc = null;
     private String mappingFilePath = null;
     private String portletFilePath = null;
 
     /**
      * Constructs a PortletDeploymentDescriptor from a portlet.xml and mapping file
      *
      * @param portletFilePath location of the portlet.xml
      * @param mappingFilePath location of the mapping file
      * @throws PortletDeploymentDescriptorException if the PortletDeploymentDescriptor cannot be created
      */
     public PortletDeploymentDescriptor(String portletFilePath, String mappingFilePath) throws IOException, DescriptorException {
         this.mappingFilePath = mappingFilePath;
         pc = (PortletCollection) load(portletFilePath, mappingFilePath);
     }
 
     /**
      * Returns a list of PortletApps
      *
      * @return a list of PortletApps
      */
     public List getPortletDef() {
         return pc.getPortletDefList();
     }
 
     /**
      * Returns a list of PortletApps
      *
      * @return a list of PortletApps
      */
     public void setPortletDef(ArrayList defs) {
         pc.setPortletDefList(defs);
     }
 
     /**
      * Return the portlet application associated with the
      *
      * @param concretePortletID the concrete portlet ID
      * @return the corresponding ConcretePortletDescriptor or null if none exists
      */
     public ConcretePortletDescriptor getConcretePortletDescriptor(String concretePortletID) {
         Iterator it = PortletDef.iterator();
         while (it.hasNext()) {
             PortletDefinition pd = (PortletDefinition) it.next();
             List apps = pd.getConcreteApps();
             Iterator appsIt = apps.iterator();
             while (appsIt.hasNext()) {
                 ConcretePortletDescriptor capp = (ConcretePortletDescriptor) appsIt.next();
                 String uid = capp.getID();
                 if (concretePortletID.equals(uid)) {
                     return capp;
                 }
             }
         }
         return null;
     }
 
     public void setConcretePortletDescriptor(ConcretePortletDescriptor concApp) {
         if (getConcretePortletDescriptor(concApp.getID()) == null) {
             // see if an application portlet exists
             List defList = pc.getPortletDefList();
             Iterator it = defList.iterator();
             while (it.hasNext()) {
                 PortletDefinition pd = (PortletDefinition) it.next();
                 List apps = pd.getConcreteApps();
                 ApplicationPortletDescriptor pApp = pd.getApplicationPortletDescriptor();
                 if (concApp.getID().startsWith(pApp.getID())) {
                     apps.add(concApp);
                     defList.remove(pd);
                     pd.setConcreteApps((ArrayList) apps);
                     defList.add(pd);
                     pc.setPortletDefList((ArrayList) defList);
                 }
             }
         }
     }
 
     /**
      * Return the portlet application associated with the
      *
      * @param concretePortletID the concrete portlet ID
      * @return the corresponding PortletApplication or null if none exists
      */
     public ApplicationPortletDescriptor getApplicationPortletDescriptor(String concretePortletID) {
         Iterator it = PortletDef.iterator();
         while (it.hasNext()) {
             PortletDefinition pd = (PortletDefinition) it.next();
             ApplicationPortletDescriptor app = pd.getApplicationPortletDescriptor();
             String uid = app.getID();
             if (concretePortletID.startsWith(uid)) {
                 return app;
             }
         }
         return null;
     }
 
     /**
      * Return the portlet application associated with the
      *
      * @param concretePortletID the concrete portlet ID
      * @return the corresponding PortletApplication or null if none exists
      */
     public void setApplicationPortletDescriptor(ApplicationPortletDescriptor portletApp) {
         if (getApplicationPortletDescriptor(portletApp.getID()) == null) {
             PortletDefinition pd = new PortletDefinition();
             List defList = pc.getPortletDefList();
             defList.add(pd);
             pc.setPortletDefList((ArrayList) defList);
         }
     }
 
     /**
      *
      */
     public void save(String portletFilePath) throws IOException, DescriptorException {
         save(portletFilePath, mappingFilePath, pc);
     }
 
     public void save() throws IOException, DescriptorException {
         save(portletFilePath, mappingFilePath, pc);
     }
 }
