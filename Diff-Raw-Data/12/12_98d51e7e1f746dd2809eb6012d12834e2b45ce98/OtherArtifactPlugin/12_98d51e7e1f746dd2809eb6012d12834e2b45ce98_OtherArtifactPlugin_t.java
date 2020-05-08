 package org.mule.galaxy.impl.artifact;
 
 import org.mule.galaxy.ArtifactType;
 
 /**
  * Does some initialization for other artifact types.
  */
 public class OtherArtifactPlugin extends AbstractArtifactPlugin
 {
 
    public void doInstall() throws Exception {
         artifactTypeDao.save(new ArtifactType("Other Artifacts", "*/*"));
     }
 
     public void initialize() throws Exception {
     }
 
     public int getVersion() {
         return 1;
     }
     
 }
