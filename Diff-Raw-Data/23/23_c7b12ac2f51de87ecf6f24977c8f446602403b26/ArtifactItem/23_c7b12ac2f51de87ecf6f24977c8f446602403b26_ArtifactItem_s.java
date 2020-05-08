 package com.bazaarvoice.maven.plugin.s3repo.create;
 
 import org.apache.maven.plugins.annotations.Parameter;
 import org.sonatype.aether.resolution.ArtifactResult;
 import org.sonatype.aether.util.StringUtils;
 
 public final class ArtifactItem {
 
     @Parameter(required = true)
     private String groupId;
 
     @Parameter(required = true)
     private String artifactId;
 
     @Parameter(required = true)
     private String version = null;
 
     @Parameter
     private String type = "jar";
 
     @Parameter
     private String classifier = "";
 
     @Parameter
     private String targetSubfolder = "";
 
     @Parameter /** The target file name (excluding extension); optional - otherwise the name will be inferred. */
     private String targetBaseName = null;
 
     @Parameter
     private String targetExtension = "noarch.rpm";
 
     /** Will be set by resolution code. */
     private ArtifactResult resolvedArtifact;
 
     public boolean isSnapshot() {
         return version.endsWith("-SNAPSHOT");
     }
 
     public String getGroupId() {
         return groupId;
     }
 
     public void setGroupId(String groupId) {
         this.groupId = groupId;
     }
 
     public String getArtifactId() {
         return artifactId;
     }
 
     public void setArtifactId(String artifactId) {
         this.artifactId = artifactId;
     }
 
     public String getVersion() {
         return version;
     }
 
     public void setVersion(String version) {
         this.version = version;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public String getClassifier() {
         return classifier;
     }
 
     public void setClassifier(String classifier) {
         this.classifier = classifier;
     }
 
     public boolean hasTargetBaseName() {
         return !StringUtils.isEmpty(targetBaseName);
     }
 
     public String getTargetBaseName() {
         return targetBaseName;
     }
 
     public void setTargetBaseName(String targetBaseName) {
         this.targetBaseName = targetBaseName;
     }
 
     public String getTargetExtension() {
         return targetExtension;
     }
 
     public void setTargetExtension(String targetExtension) {
         this.targetExtension = targetExtension;
     }
 
     public String getTargetSubfolder() {
         return targetSubfolder;
     }
 
     public boolean hasTargetSubfolder() {
         return !StringUtils.isEmpty(targetSubfolder);
     }
 
     public void setTargetSubfolder(String targetSubfolder) {
         this.targetSubfolder = targetSubfolder;
     }
 
     /*package*/ ArtifactResult getResolvedArtifact() {
         return resolvedArtifact;
     }
 
     /*package*/ void setResolvedArtifact(ArtifactResult resolvedArtifact) {
         this.resolvedArtifact = resolvedArtifact;
     }
 
 }
