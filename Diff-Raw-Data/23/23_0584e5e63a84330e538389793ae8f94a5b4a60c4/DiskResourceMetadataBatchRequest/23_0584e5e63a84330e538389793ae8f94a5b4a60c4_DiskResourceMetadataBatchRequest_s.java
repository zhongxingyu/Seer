 package org.iplantc.de.client.services.impl.models;
 
 import org.iplantc.de.client.models.diskResources.DiskResourceMetadata;
 
 import java.util.Set;
 
 public interface DiskResourceMetadataBatchRequest {
 
     Set<DiskResourceMetadata> getAdd();
 
     void setAdd(Set<DiskResourceMetadata> add);
 
    Set<String> getDelete();
 
    void setDelete(Set<String> delete);
 
 }
