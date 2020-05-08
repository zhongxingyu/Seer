 package com.zenika.dorm.maven.converter;
 
 import com.zenika.dorm.maven.exception.MavenException;
 import com.zenika.dorm.maven.model.MavenBuildInfo;
 import com.zenika.dorm.maven.model.MavenMetadata;
 import com.zenika.dorm.maven.model.builder.MavenBuildInfoBuilder;
 import com.zenika.dorm.maven.model.builder.MavenMetadataBuilder;
 import org.apache.maven.artifact.repository.metadata.Metadata;
 import org.apache.maven.artifact.repository.metadata.Snapshot;
 import org.apache.maven.artifact.repository.metadata.Versioning;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileInputStream;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public final class MavenDormMetadataConverter {
 
     private static final Logger LOG = LoggerFactory.getLogger(MavenDormMetadataConverter.class);
 
     public static Metadata dormToMaven(MavenMetadata dormMetadata) {
 
         Metadata metadata = new Metadata();
         metadata.setArtifactId(dormMetadata.getArtifactId());
         metadata.setGroupId(dormMetadata.getGroupId());
         metadata.setVersion(dormMetadata.getVersion());
 
         Versioning versioning = new Versioning();
         metadata.setVersioning(versioning);
 
         if (dormMetadata.isSnapshot()) {
             Snapshot snapshot = new Snapshot();
             snapshot.setBuildNumber(Integer.valueOf(dormMetadata.getBuildInfo().getBuildNumber()));
             snapshot.setTimestamp(dormMetadata.getBuildInfo().getTimestamp());
             versioning.setSnapshot(snapshot);
         }
 
         return metadata;
     }
 
     public MavenMetadata mavenModelToDorm(Model model) {
         return new MavenMetadataBuilder()
                 .artifactId(model.getArtifactId())
                 .groupId(model.getGroupId())
                 .version(model.getVersion())
                 .build();
     }
 }
