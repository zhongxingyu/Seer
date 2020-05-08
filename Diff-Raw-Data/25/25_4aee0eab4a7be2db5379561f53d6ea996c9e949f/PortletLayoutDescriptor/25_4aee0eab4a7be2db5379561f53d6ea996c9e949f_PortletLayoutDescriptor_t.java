 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.core.persistence.castor.descriptor.DescriptorException;
 
 import java.io.IOException;
 
public class PortletLayoutDescriptor extends org.gridlab.gridsphere.core.persistence.castor.descriptor.Descriptor {
 
     private static PortletLayoutDescriptor instance = new PortletLayoutDescriptor();
 
     /**
      * Constructs a PortletLayoutDescriptor from an xml descriptor and a mapping file
      *
      * @param layoutDescriptorPath location of the layout.xml
      * @param layoutMappingPath location of the mapping file
      * @throws DescriptorException if the PortletLayoutDescriptor cannot be created
      */
     private PortletLayoutDescriptor() {
     }
 
     /**
      * Loads the portlet container associated with this descriptor
      *
      * @param layoutDescriptorPath location of the layout.xml
      * @param layoutMappingPath location of the mapping file
      *
      * @return the portlet container
      */
     public static PortletContainer loadPortletContainer(String layoutDescriptorPath, String layoutMappingPath) throws IOException, DescriptorException {
         return (PortletContainer) load(layoutDescriptorPath, layoutMappingPath);
     }
 
     public static PortletTab loadPortletTab(String layoutDescriptorPath, String layoutMappingPath) throws IOException, DescriptorException {
         return (PortletTab) load(layoutDescriptorPath, layoutMappingPath);
     }
 
     /**
      *
      */
     public static void savePortletContainer(PortletContainer pc, String descriptorPath, String mappingPath) throws IOException, DescriptorException {
         save(descriptorPath, mappingPath, pc);
     }
 
     /**
      *
      */
     public static void savePortletContainer(PortletTab tab, String descriptorPath, String mappingPath) throws IOException, DescriptorException {
         save(descriptorPath, mappingPath, tab);
     }
 }
