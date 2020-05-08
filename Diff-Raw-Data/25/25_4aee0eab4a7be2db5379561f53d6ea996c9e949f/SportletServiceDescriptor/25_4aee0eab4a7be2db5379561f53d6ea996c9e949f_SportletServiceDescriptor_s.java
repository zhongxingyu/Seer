 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet.service.spi.impl.descriptor;
 
 import org.gridlab.gridsphere.core.persistence.castor.descriptor.DescriptorException;
 
 import java.io.IOException;
 
 public class SportletServiceDescriptor extends Descriptor {
 
     private SportletServiceCollection services = null;
 
     private String descriptorPath, mappingPath;
 
     public SportletServiceDescriptor(String descriptorFile, String mappingFile) throws IOException, DescriptorException {
         services = (SportletServiceCollection)load(descriptorFile, mappingFile);
     }
 
     public SportletServiceCollection getServiceCollection() {
         return services;
     }
 
     public void save() throws IOException, DescriptorException  {
         save(descriptorPath, mappingPath, services);
     }
 
 }
