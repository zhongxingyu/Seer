 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.artifact.bundle;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Dictionary;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 
 import org.eclipse.virgo.kernel.artifact.StubHashGenerator;
 import org.eclipse.virgo.repository.ArtifactDescriptor;
 import org.eclipse.virgo.repository.ArtifactGenerationException;
 import org.eclipse.virgo.repository.Attribute;
 import org.junit.Before;
 import org.junit.Test;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Version;
 
 /**
  * <p>
  * Unit tests for {@link org.eclipse.virgo.kernel.artifact.bundle.BundleBridge BundleBridge}. Uses a combination of real
  * bundle files and static test data.
  * </p>
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Threadsafe test case
  * 
  */
 public class BundleBridgeTests {
 
     // Test Data
 
     private final static String ARTEFACT_ATTRIBUTE_NAME = "name";
 
     private final static String ARTEFACT_ATTRIBUTE_VERSION = "version";
 
     // End Test Data
 
     private static BundleBridge BUNDLE_BRIDGE;
 
     private static final StubBundleArtefactBridge STUB_ARTEFACT_DEFINITION = new StubBundleArtefactBridge();
 
     private static final String BUNDLE_MANIFEST_VERSION_HEADER_NAME = "Bundle-ManifestVersion";
 
     private static final String BUNDLE_NAME_HEADER_NAME = "Bundle-Name";
 
     @Before
     public void setUp() throws Exception {
         BUNDLE_BRIDGE = new BundleBridge(new StubHashGenerator());
     }
 
     @Test
     public void testFictionalURI() {
         File file = new File("/foo/bar.jar");
         try {
             BUNDLE_BRIDGE.generateArtifactDescriptor(file);
             assertTrue("Should throw exception", false);
         } catch (ArtifactGenerationException age) {
             assertEquals("ArtifactType in exception is incorrect", age.getArtifactType(), BundleBridge.BRIDGE_TYPE);
         }
     }
 
     @Test
     public void testBadManifest01() {
         File file = new File("./src/test/resources/wars/testbad01.war"); // contains Erroneous-Data: Bundle-Version
         try {
             BUNDLE_BRIDGE.generateArtifactDescriptor(file);
             assertTrue("Should throw exception", false);
         } catch (ArtifactGenerationException age) {
             assertEquals("ArtifactType in exception is incorrect", age.getArtifactType(), BundleBridge.BRIDGE_TYPE);
         }
     }
 
     @Test
     public void testGenerateArtefact() throws ArtifactGenerationException {
         File jarsDirectory = new File(
            System.getProperty("user.home") + "/virgo-build-cache/ivy-cache/repository/org.apache.commons/com.springsource.org.apache.commons.dbcp/1.2.2.osgi/com.springsource.org.apache.commons.dbcp-1.2.2.osgi.jar");
         File directoriesDirectory = new File("./src/test/resources/directories");
 
         Set<ArtifactDescriptor> artefacts = new HashSet<ArtifactDescriptor>();
 
         artefacts.add(BUNDLE_BRIDGE.generateArtifactDescriptor(jarsDirectory));
 
         assertEquals("Wrong number of artefacts have been parsed", 1, artefacts.size());
 
         artefacts.addAll(generateArtefacts(directoriesDirectory));
 
         assertEquals("Wrong number of artefacts have been parsed", 2, artefacts.size());
 
         ArtifactDescriptor stubArtefact;
         Set<Attribute> stubAttributes;
         Set<Attribute> testAttributes;
 
         for (ArtifactDescriptor testArtefact : artefacts) {
             stubArtefact = STUB_ARTEFACT_DEFINITION.generateArtifactDescriptor(new File(testArtefact.getUri()));
 
             stubAttributes = stubArtefact.getAttribute(ARTEFACT_ATTRIBUTE_NAME);
             testAttributes = testArtefact.getAttribute(Constants.BUNDLE_SYMBOLICNAME);
 
             assertEquals("Error on: " + testArtefact.toString(), stubAttributes.iterator().next().getValue(),
                 testAttributes.iterator().next().getValue());
 
             stubAttributes = stubArtefact.getAttribute(ARTEFACT_ATTRIBUTE_VERSION);
             testAttributes = testArtefact.getAttribute(Constants.BUNDLE_VERSION);
 
             assertEquals("Error on: " + testArtefact.toString(), stubAttributes.iterator().next().getValue(),
                 testAttributes.iterator().next().getValue());
         }
     }
 
     @Test
     public void testBuildDictionary() throws ArtifactGenerationException, IOException {
         File testFile = new File(
             System.getProperty("user.home") + "/virgo-build-cache/ivy-cache/repository/org.eclipse.virgo.mirrored/javax.servlet/3.0.0.v201112011016/javax.servlet-3.0.0.v201112011016.jar");
 
         ArtifactDescriptor inputArtefact = BUNDLE_BRIDGE.generateArtifactDescriptor(testFile);
 
         Dictionary<String, String> dictionary = BundleBridge.convertToDictionary(inputArtefact);
 
         JarFile testJar = new JarFile(testFile);
         Attributes attributes = testJar.getManifest().getMainAttributes();
 
         testJar.close();
 
         assertEquals("Failed to match regenerated " + Constants.BUNDLE_SYMBOLICNAME, dictionary.get(Constants.BUNDLE_SYMBOLICNAME),
             attributes.getValue(Constants.BUNDLE_SYMBOLICNAME));
         assertEquals("Failed to match regenerated " + Constants.BUNDLE_VERSION, dictionary.get(Constants.BUNDLE_VERSION),
             attributes.getValue(Constants.BUNDLE_VERSION));
         assertEquals("Failed to match regenerated " + BUNDLE_MANIFEST_VERSION_HEADER_NAME, dictionary.get(BUNDLE_MANIFEST_VERSION_HEADER_NAME),
             attributes.getValue(BUNDLE_MANIFEST_VERSION_HEADER_NAME));
         assertEquals("Failed to match regenerated " + BUNDLE_NAME_HEADER_NAME, dictionary.get(BUNDLE_NAME_HEADER_NAME),
             attributes.getValue(BUNDLE_NAME_HEADER_NAME));
 
     }
 
     @Test
     public void webBundleWar() throws ArtifactGenerationException {
         ArtifactDescriptor descriptor = BUNDLE_BRIDGE.generateArtifactDescriptor(new File("src/test/resources/wars/test.war"));
         assertNotNull(descriptor);
         assertEquals(BundleBridge.BRIDGE_TYPE, descriptor.getType());
         assertEquals("com.springsource.server.admin.web", descriptor.getName());
         assertEquals(new Version(2, 0, 0), descriptor.getVersion());
     }
 
     @Test
     public void explodedBundle() throws ArtifactGenerationException {
         ArtifactDescriptor descriptor = BUNDLE_BRIDGE.generateArtifactDescriptor(new File("src/test/resources/bundle.jar"));
         assertNotNull(descriptor);
         assertEquals(BundleBridge.BRIDGE_TYPE, descriptor.getType());
         assertEquals("exploded.bundle", descriptor.getName());
         assertEquals(new Version(1, 0, 0), descriptor.getVersion());
     }
 
     @Test
     public void noSymbolicName() throws ArtifactGenerationException {
         ArtifactDescriptor descriptor = BUNDLE_BRIDGE.generateArtifactDescriptor(new File("src/test/resources/jars/no-symbolic-name.jar"));
         assertNotNull(descriptor);
         assertEquals(BundleBridge.BRIDGE_TYPE, descriptor.getType());
         assertEquals("no-symbolic-name", descriptor.getName());
         assertEquals(new Version(0, 0, 0), descriptor.getVersion());
     }
 
     @Test
     public void noManifest() throws ArtifactGenerationException {
         ArtifactDescriptor descriptor = BUNDLE_BRIDGE.generateArtifactDescriptor(new File("src/test/resources/jars/no-manifest.jar"));
         assertNotNull(descriptor);
         assertEquals(BundleBridge.BRIDGE_TYPE, descriptor.getType());
         assertEquals("no-manifest", descriptor.getName());
         assertEquals(Version.emptyVersion, descriptor.getVersion());
     }
 
     private Set<ArtifactDescriptor> generateArtefacts(File directory) throws ArtifactGenerationException {
         Set<ArtifactDescriptor> artefacts = new HashSet<ArtifactDescriptor>();
 
         for (File fileInDir : directory.listFiles()) {
             if (!fileInDir.getName().endsWith(".jar") && !fileInDir.getName().contains("sources")) {
                 ArtifactDescriptor artefact = BUNDLE_BRIDGE.generateArtifactDescriptor(fileInDir);
                 if (artefact != null) {
                     artefacts.add(BUNDLE_BRIDGE.generateArtifactDescriptor(fileInDir));
                 }
             }
         }
         return artefacts;
     }
 }
