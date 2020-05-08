 /*******************************************************************************
  * Copyright (c) 2010, 2012 SAP AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tycho.nexus.internal.plugin.cache;
 
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 
 import junit.framework.AssertionFailedError;
 
 import org.easymock.Capture;
 import org.easymock.EasyMock;
 import org.easymock.IAnswer;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.sonatype.nexus.proxy.IllegalRequestException;
 import org.sonatype.nexus.proxy.ResourceStoreRequest;
 import org.sonatype.nexus.proxy.item.StorageFileItem;
 import org.sonatype.nexus.proxy.maven.MavenRepository;
 
 @SuppressWarnings({ "nls" })
 public class LatestVersionConverterTest {
 
     private String outerMetadataXml = "./src/test/resources/outer-maven-metadata.xml";
 
     @Test
     public void testConvertToLatestVersion() throws Exception {
         assertPathIsConvertedTo(
             "org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/SNAPSHOT/org.eclipse.tycho.example.target-SNAPSHOT.zip-unzip",
             "org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/0.7.1-SNAPSHOT/org.eclipse.tycho.example.target-0.7.1-20110718.111322-2.zip-unzip");
     }
 
     @Test
     public void testConvertToLatestReleaseVersion() throws Exception {
         assertPathIsConvertedTo(
             "org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/RELEASE/org.eclipse.tycho.example.target-RELEASE.zip-unzip",
             "org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/0.7.0/org.eclipse.tycho.example.target-0.7.0.zip-unzip");
     }
 
     @Test
     public void testConvertToLatestVersionNotSnapshot() throws Exception {
         outerMetadataXml = "./src/test/resources/maven-metadata-latest-is-not-a-snapshot.xml";
         assertPathIsConvertedTo(
             "org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/SNAPSHOT/org.eclipse.tycho.example.target-SNAPSHOT.zip-unzip",
             "org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/0.7.0/org.eclipse.tycho.example.target-0.7.0.zip-unzip");
     }
 
     @Test
     public void testConvertToReleaseVersionNoReleaseAvailable() throws Exception {
         outerMetadataXml = "./src/test/resources/outer-maven-metadata-without-release.xml";
         assertPathIsNotConverted("org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/RELEASE/org.eclipse.tycho.example.target-RELEASE.zip-unzip");
     }
 
     @Test(expected = IllegalRequestException.class)
     @Ignore
     public void testInvalidRange() throws Exception {
         // TODO 
         // requires specification how version ranges are defined in url.
 
 //        assertPathIsConvertedWithinRange(
 //            "org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/SNAPSHOT/org.eclipse.tycho.example.target-SNAPSHOT.zip-unzip", "",
 //            "[2.0.0,1.0.0)");       
     }
 
     @Test
     @Ignore
     public void testConvertToLatestVersionWithinRange() throws Exception {
         // TODO 
         // requires specification how version ranges are defined in url.
 
 //        assertPathIsConvertedWithinRange(
 //            "org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/SNAPSHOT/org.eclipse.tycho.example.target-SNAPSHOT.zip-unzip",
 //            "org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/0.6.1-SNAPSHOT/org.eclipse.tycho.example.target-0.6.1-20110718.111322-2.zip-unzip",
 //            "[0.5.0,0.7.0-SNAPSHOT)");
     }
 
     @Test
     @Ignore
     public void testNoLatestVersionWithinRange() throws Exception {
         // TODO 
         // requires specification how version ranges are defined in url.
 
 //        assertPathIsNotConvertedWithinRange(
 //            "org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/SNAPSHOT/org.eclipse.tycho.example.target-SNAPSHOT.zip-unzip",
 //            "[1.0.0,2.0.0)");
     }
 
     @Test
     @Ignore
     public void testConvertToLatestReleasedVersionWithinRange() throws Exception {
         // TODO 
         // requires specification how version ranges are defined in url.
 
 //        assertPathIsConvertedWithinRange(
 //            "org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/RELEASE/org.eclipse.tycho.example.target-RELEASE.zip-unzip",
 //            "org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/0.6.0/org.eclipse.tycho.example.target-0.6.0.zip-unzip",
 //            "[0.5.0,0.7.0)");
     }
 
     private void assertPathIsConvertedTo(final String requestPath, final String convertedPath) throws Exception {
         assertPathConvertion(new ResourceStoreRequest(requestPath), convertedPath, true);
     }
 
     private void assertPathIsNotConverted(final String requestPath) throws Exception {
         assertPathConvertion(new ResourceStoreRequest(requestPath), requestPath, false);
     }
 
     private void assertPathConvertion(final ResourceStoreRequest request, final String convertedPath,
            final boolean pathConversionExpected) throws Exception {
         final MavenRepository repositoryMock =
             createRepositoryMock("org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/",
                 "org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/0.7.1-SNAPSHOT/",
                 "org/eclipse/tycho/nexus/org.eclipse.tycho.example.target/0.6.1-SNAPSHOT/");
         final ConversionResult conversionResult = RequestPathConverter.convert(repositoryMock, request, true);
        assertEquals(pathConversionExpected, conversionResult.isPathConverted());
         assertEquals(convertedPath, conversionResult.getConvertedPath());
        if (pathConversionExpected) {
            assertTrue(convertedPath.contains(conversionResult.getLatestVersion()));
        }
     }
 
     @SuppressWarnings("unchecked")
     private MavenRepository createRepositoryMock(final String pathToOuterMetadata, final String pathToArtifact,
             final String pathToArtifact2) {
         final String pathToOuterMetadataXML = pathToOuterMetadata + "maven-metadata.xml";
         final String pathToInnerMetadataXML = pathToArtifact + "maven-metadata.xml";
         final String pathToArtifact2MetadataXML = pathToArtifact2 + "maven-metadata.xml";
         final MavenRepository repositoryMock = EasyMock.createMock(MavenRepository.class);
         final Capture<ResourceStoreRequest>[] captures = new Capture[] { new Capture<ResourceStoreRequest>() };
         try {
             EasyMock.expect(repositoryMock.getId()).andStubReturn("");
             EasyMock.expect(repositoryMock.retrieveItem(EasyMock.capture(captures[0])))
                 .andAnswer(new IAnswer<StorageFileItem>() {
                     @Override
                     public StorageFileItem answer() throws Throwable {
                         final StorageFileItem mavenMetaDataXml = EasyMock.createMock(StorageFileItem.class);
                         EasyMock.expect(mavenMetaDataXml.getInputStream()).andAnswer(new IAnswer<InputStream>() {
 
                             @Override
                             public InputStream answer() throws Throwable {
                                 final File metaDataFile;
                                 final String requestPath = captures[0].getValue().getRequestPath();
                                 if (pathToInnerMetadataXML.equals(requestPath)) {
                                     metaDataFile = new File("./src/test/resources/0.7.1-SNAPSHOT/maven-metadata.xml");
                                 } else if (pathToArtifact2MetadataXML.equals(requestPath)) {
                                     metaDataFile = new File("./src/test/resources/0.6.1-SNAPSHOT/maven-metadata.xml");
                                 } else if (pathToOuterMetadataXML.equals(captures[0].getValue().getRequestPath())) {
                                     metaDataFile = new File(outerMetadataXml);
                                 } else {
                                     throw new AssertionFailedError("wrong request for maven-metadata.xml: "
                                             + requestPath);
                                 }
                                 return new FileInputStream(metaDataFile);
                             }
                         });
                         EasyMock.replay(mavenMetaDataXml);
                         return mavenMetaDataXml;
                     }
                 })
                 .anyTimes();
         } catch (final Exception e) {
             throw new RuntimeException("Unable to create Maven Repo Mock", e);
         }
         EasyMock.replay(repositoryMock);
         return repositoryMock;
     }
 }
