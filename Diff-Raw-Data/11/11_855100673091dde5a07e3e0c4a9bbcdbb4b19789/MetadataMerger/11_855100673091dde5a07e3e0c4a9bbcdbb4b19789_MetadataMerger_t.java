 /*
  * Copyright 2012 Codehaus.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.codehaus.mojo.stage2;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.Writer;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import org.apache.maven.artifact.repository.metadata.Metadata;
 import org.apache.maven.artifact.repository.metadata.Versioning;
 import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
 import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.Parent;
 import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
 import org.apache.maven.project.validation.DefaultModelValidator;
 import org.apache.maven.project.validation.ModelValidationResult;
 import org.apache.maven.project.validation.ModelValidator;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.ReaderFactory;
 import org.codehaus.plexus.util.xml.XmlStreamReader;
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 
 /**
  * @author Mirko Friedenhagen
  */
 class MetadataMerger {
 
     private final File existingMetadataFile;
 
     private final MetadataXpp3Reader reader = new MetadataXpp3Reader();
 
     private final MetadataXpp3Writer writer = new MetadataXpp3Writer();
 
     /**
      * @param existingMetadataFile
      */
     MetadataMerger(final File existingMetadataFile) {
         this.existingMetadataFile = existingMetadataFile;
     }
 
     void writeNewMetadata(File pomFile) throws IOException {
         final Metadata metadataFromPom = createMetadataFromPom(pomFile);
         writeMetadata(metadataFromPom);
     }
 
     void mergeMetadata(File pomFile) throws IOException {
         final Metadata metadataFromPom = createMetadataFromPom(pomFile);
         final Metadata existingMetadata = readFromFile(existingMetadataFile);
         existingMetadata.merge(metadataFromPom);
         writeMetadata(existingMetadata);
 
     }
 
     /**
      * @param existingMetadata
      *
      s IOException
      */
     private void writeMetadata(final Metadata existingMetadata) throws IOException {
         // Write back the merged data.
         final Writer metadataFileWriter = new FileWriter(existingMetadataFile);
         try {
             writer.write(metadataFileWriter, existingMetadata);
         } finally {
             metadataFileWriter.close();
         }
         // Regenerate the checksums as they will be different after the merger
         try {
             final File parentFile = existingMetadataFile.getParentFile();
             final String name = existingMetadataFile.getName();
             final File md5 = new File(parentFile, name + "." + Constants.MD5);
             FileUtils.fileWrite(md5.getAbsolutePath(), checksum(existingMetadataFile, Constants.MD5));
             final File sha1 = new File(parentFile, name + "." + Constants.SHA1);
             FileUtils.fileWrite(sha1.getAbsolutePath(), checksum(existingMetadataFile, Constants.SHA1));
         } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
         }
     }
 
     /**
      * @param pomFile
      *
      * @return
      *
      * @throws IOException
      */
     private Metadata createMetadataFromPom(File pomFile) throws IOException {
         final Model pom = fromPomFile(pomFile);
         final Metadata metadataFromPom = new Metadata();
         metadataFromPom.setGroupId(pom.getGroupId());
         metadataFromPom.setArtifactId(pom.getArtifactId());
         metadataFromPom.setVersion(pom.getVersion());
         final Versioning versioning = new Versioning();
         versioning.addVersion(pom.getVersion());
         versioning.setLastUpdatedTimestamp(new Date());
         metadataFromPom.setVersioning(versioning);
         // TODO: how to set maven-plugin information?
 //        if (pom.getPackaging().equals("maven-plugin")) {
 //            final Plugin plugin = new Plugin();
 //            plugin.setArtifactId(pom.getArtifactId());
 //            metadataFromPom.addPlugin(plugin);
 //        }
         return metadataFromPom;
     }
 
     private Model fromPomFile(final File pom) throws IOException {
         final XmlStreamReader reader = ReaderFactory.newXmlReader(pom);
         final Model model;
         try {
             model = new MavenXpp3Reader().read(reader);
         } catch (XmlPullParserException e) {
             throw new IOException("Could not create model from " + pom, e);
         } finally {
             reader.close();
         }
         final Parent parent = model.getParent();
         String groupId = model.getGroupId() == null ? parent.getGroupId() : model.getGroupId();
         String artifactId = model.getArtifactId();
         String version = model.getVersion() == null ? parent.getVersion() : model.getVersion();
         String packaging = model.getPackaging();
         final Model newModel = generateModel(groupId, artifactId, version, packaging);
         System.err.println("XXXXX" + newModel);
         ModelValidator validator = new DefaultModelValidator();
         System.err.println("YYYY" + validator);
         ModelValidationResult validationResult = validator.validate(newModel);
         if (validationResult.getMessageCount() > 0) {
             throw new IOException(validationResult.toString());
         }
         return newModel;
     }
 
     /**
      * Generates a minimal model from the user-supplied artifact information.
      *
      * @return The generated model, never
      * <code>null</code>.
      */
     private Model generateModel(String groupId, String artifactId, String version, String packaging) {
         Model model = new Model();
         model.setModelVersion("4.0.0");
         model.setGroupId(groupId);
         model.setArtifactId(artifactId);
         model.setVersion(version);
         model.setPackaging(packaging);
         return model;
     }
 
     private Metadata readFromFile(File metadataFile) throws IOException {
         final Reader fileReader = new FileReader(metadataFile);
         try {
             return reader.read(fileReader);
         } catch (XmlPullParserException e) {
            throw new IOException("Metadata file is corrupt " + metadataFile, e);
         } finally {
             fileReader.close();
         }
     }
 
     private String checksum(File file, String type) throws IOException, NoSuchAlgorithmException {
         final MessageDigest digest = MessageDigest.getInstance(type);
 
         final InputStream is = new FileInputStream(file);
 
         final byte[] buf = new byte[8192];
 
         int i;
         try {
             while ((i = is.read(buf)) > 0) {
                 digest.update(buf, 0, i);
             }
         } finally {
             is.close();
         }
 
         return encode(digest.digest());
     }
 
     private String encode(byte[] binaryData) {
         if (binaryData.length != 16 && binaryData.length != 20) {
             int bitLength = binaryData.length * 8;
             throw new IllegalArgumentException("Unrecognised length for binary data: " + bitLength + " bits");
         }
 
         final StringBuilder retValue = new StringBuilder();
 
         for (int i = 0; i < binaryData.length; i++) {
             final String t = Integer.toHexString(binaryData[i] & 0xff);
 
             if (t.length() == 1) {
                 retValue.append("0" + t);
             } else {
                 retValue.append(t);
             }
         }
 
         return retValue.toString().trim();
     }
 }
