 package com.zenika.dorm.maven.model.impl;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 
 import com.zenika.dorm.core.model.DormMetadataExtension;
 import com.zenika.dorm.core.util.DormStringUtils;
 import com.zenika.dorm.maven.exception.MavenException;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.builder.ToStringBuilder;
 
 import java.util.Map;
 
 /**
  * Maven immutable extension point to the dorm model
  * Add maven specific metadatas
  */
 public final class MavenMetadataExtension implements DormMetadataExtension {
 
     public static final transient String EXTENSION_NAME = "maven";
 
     /**
      * Metadata names
      */
     public static final transient String METADATA_GROUPID = "groupId";
     public static final transient String METADATA_ARTIFACTID = "artifactId";
     public static final transient String METADATA_VERSION = "version";
     public static final transient String METADATA_CLASSIFIER = "classifier";
     public static final transient String METADATA_PACKAGING = "packaging";
     public static final transient String METADATA_TIMESTAMP = "timestamp";
     public static final transient String METADATA_EXTENSION = "extension";
     public static final transient String METADATA_BUILDNUMBER = "buildNumber";
     public static final transient String METADATA_SNAPSHOT = "snapshot";
 
 
     private final String groupId;
     private final String artifactId;
     private final String version;
     private final String classifier;
     private final String packaging;
     private final String timestamp;
     private final String extension;
     private final String buildNumber;
     private final boolean snapshot;
 
     private final transient boolean mavenMetadata;
 
     /**
      * Only accessed by the builder in the same package
      */
     MavenMetadataExtension(String groupId, String artifactId, String version, String extension,
                            String packaging, String classifier, String timestamp, String buildNumber,
                            boolean mavenMetadata, boolean snapshot) {
 
         if (DormStringUtils.areBlanks(groupId, artifactId, version)) {
             throw new MavenException("Following metadatas are required : groupId, artifactId, versionId");
         }
 
         if (!mavenMetadata && !MavenConstant.FileExtension.isMavenExtension(extension)) {
             throw new MavenException("Extension is not allowed for a maven file : " + extension);
         }
 
         this.groupId = groupId;
         this.artifactId = artifactId;
         this.version = version;
 
         this.packaging = StringUtils.defaultIfBlank(packaging, MavenConstant.Packaging.JAR);
         this.extension = StringUtils.defaultIfBlank(extension, "");
         this.classifier = StringUtils.defaultIfBlank(classifier, "");
         this.timestamp = StringUtils.defaultIfBlank(timestamp, "");
         this.buildNumber = StringUtils.defaultIfBlank(buildNumber, "");
 
         this.mavenMetadata = mavenMetadata;
         this.snapshot = snapshot;
     }
 
     @Override
     public String getQualifier() {
         String separator = ":";
         StringBuilder qualifier = new StringBuilder()
                 .append(groupId).append(separator)
                 .append(artifactId).append(separator)
                 .append(packaging).append(separator);
 
         if (!classifier.isEmpty()) {
             qualifier.append(classifier).append(separator);
         }
 
         if (!timestamp.isEmpty()) {
             qualifier.append(timestamp).append(separator);
         }
 
         return qualifier.append(packaging).append(separator)
                 .append(version).toString();
     }
 
     @Override
     public String getExtensionName() {
         return EXTENSION_NAME;
     }
 
     public String getGroupId() {
         return groupId;
     }
 
     public String getArtifactId() {
         return artifactId;
     }
 
     public String getVersion() {
         return version;
     }
 
     public String getExtension() {
         return extension;
     }
 
     public String getPackaging() {
         return packaging;
     }
 
     public String getClassifier() {
         return classifier;
     }
 
     public String getTimestamp() {
         return timestamp;
     }
 
     public String getBuildNumber() {
 
         return buildNumber;
     }
 
     public boolean isMavenMetadata() {
         return mavenMetadata;
     }
 
     public boolean isSnapshot() {
         return snapshot;
     }
 
     @Override
     public DormMetadataExtension createFromMap(Map<String, String> properties) {
         return new MavenMetadataExtensionBuilder(properties.get(METADATA_GROUPID),
                 properties.get(METADATA_ARTIFACTID), properties.get(METADATA_VERSION))
                 .packaging(properties.get(METADATA_PACKAGING))
                 .classifier(properties.get(METADATA_CLASSIFIER))
                .timestamp(properties.get(METADATA_TIMESTAMP))
                 .extension(properties.get(METADATA_EXTENSION))
                 .snapshot(Boolean.valueOf(properties.get(METADATA_SNAPSHOT)))
                 .build();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         MavenMetadataExtension extension1 = (MavenMetadataExtension) o;
 
         if (mavenMetadata != extension1.mavenMetadata) return false;
         if (snapshot != extension1.snapshot) return false;
         if (artifactId != null ? !artifactId.equals(extension1.artifactId) : extension1.artifactId != null)
             return false;
         if (buildNumber != null ? !buildNumber.equals(extension1.buildNumber) : extension1.buildNumber != null)
             return false;
         if (classifier != null ? !classifier.equals(extension1.classifier) : extension1.classifier != null)
             return false;
         if (extension != null ? !extension.equals(extension1.extension) : extension1.extension != null)
             return false;
         if (groupId != null ? !groupId.equals(extension1.groupId) : extension1.groupId != null) return false;
         if (packaging != null ? !packaging.equals(extension1.packaging) : extension1.packaging != null)
             return false;
         if (timestamp != null ? !timestamp.equals(extension1.timestamp) : extension1.timestamp != null)
             return false;
         if (version != null ? !version.equals(extension1.version) : extension1.version != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = groupId != null ? groupId.hashCode() : 0;
         result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
         result = 31 * result + (version != null ? version.hashCode() : 0);
         result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
         result = 31 * result + (packaging != null ? packaging.hashCode() : 0);
         result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
         result = 31 * result + (extension != null ? extension.hashCode() : 0);
         result = 31 * result + (buildNumber != null ? buildNumber.hashCode() : 0);
         result = 31 * result + (snapshot ? 1 : 0);
         result = 31 * result + (mavenMetadata ? 1 : 0);
         return result;
     }
 
     @Override
     public String toString() {
         return new ToStringBuilder(this)
                 .append("groupId", groupId)
                 .append("artifactId", artifactId)
                 .append("version", version)
                 .append("classifier", classifier)
                 .append("packaging", packaging)
                 .append("timestamp", timestamp)
                 .append("extension", extension)
                 .append("buildNumber", buildNumber)
                 .append("snapshot", snapshot)
                 .append("mavenMetadata", mavenMetadata)
                 .appendSuper(super.toString())
                 .toString();
     }
 }
