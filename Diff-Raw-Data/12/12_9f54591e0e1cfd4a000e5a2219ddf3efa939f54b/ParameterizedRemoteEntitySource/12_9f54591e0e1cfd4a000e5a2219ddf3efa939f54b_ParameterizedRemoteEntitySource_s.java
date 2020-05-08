 package org.nuxeo.ecm.platform.semanticentities.service;
 
 import java.net.URI;
 
 import org.nuxeo.ecm.platform.semanticentities.RemoteEntitySource;
 
 /**
  * Abstract base class to be used by all contributions to the
  * RemoteEntityServiceImpl service.
  *
  * Factorize common mapping logic and offer public methods to help the service
  * set parameters from the descriptor.
  */
 public abstract class ParameterizedRemoteEntitySource implements
         RemoteEntitySource {
 
     protected RemoteEntitySourceDescriptor descriptor;
 
     public void setDescriptor(RemoteEntitySourceDescriptor descriptor) {
         this.descriptor = descriptor;
     }
 
     @Override
     public boolean canDereference(URI remoteEntity) {
        return descriptor.getUriPrefix().startsWith(remoteEntity.toString());
     }
 
 }
