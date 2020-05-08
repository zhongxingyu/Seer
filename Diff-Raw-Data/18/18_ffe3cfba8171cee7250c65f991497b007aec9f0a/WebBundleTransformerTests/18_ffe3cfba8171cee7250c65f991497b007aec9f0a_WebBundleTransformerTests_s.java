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
 
 package org.eclipse.virgo.web.core.internal;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.eq;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.isA;
 import static org.easymock.EasyMock.isNull;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.eclipse.gemini.web.core.InstallationOptions;
 import org.eclipse.gemini.web.core.WebBundleManifestTransformer;
 import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
 import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 import org.eclipse.virgo.util.common.GraphNode;
 import org.eclipse.virgo.util.common.GraphNode.DirectedAcyclicGraphVisitor;
 import org.eclipse.virgo.util.common.GraphNode.ExceptionThrowingDirectedAcyclicGraphVisitor;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
 import org.eclipse.virgo.util.osgi.manifest.internal.StandardBundleManifest;
 import org.junit.Test;
 import org.osgi.framework.Version;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 
 public class WebBundleTransformerTests {
 
     private StubWebContainer webContainer = new StubWebContainer();
 
     private StubWebApplicationRegistry applicationRegistry = new StubWebApplicationRegistry();
 
     private WebBundleManifestTransformer manifestTransformer = createMock(WebBundleManifestTransformer.class);
 
     private ConfigurationAdmin configAdmin = createMock(ConfigurationAdmin.class);
 
     private EventLogger eventLogger = createMock(EventLogger.class);
 
     private WebDeploymentEnvironment environment = new WebDeploymentEnvironment(webContainer, applicationRegistry, manifestTransformer, configAdmin,
         eventLogger);
 
     private WebBundleTransformer webBundleTransformer = new WebBundleTransformer(environment);
 
     @Test
     public void transformation() throws IOException, DeploymentException {
         BundleManifest bundleManifest = new StandardBundleManifest(null);
         bundleManifest.getBundleSymbolicName().setSymbolicName("foo.war");
 
         File sourceFile = new File("/bar.war");
         URI sourceUri = sourceFile.toURI();
 
         BundleInstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(sourceUri, sourceFile, bundleManifest);
 
         manifestTransformer.transform(eq(bundleManifest), eq(sourceUri.toURL()), isA(InstallationOptions.class), eq(true));
 
         replay(manifestTransformer);
 
         GraphNode<InstallArtifact> installGraph = createGraphNode(installArtifact);
         webBundleTransformer.transform(installGraph, null);
 
         verify(manifestTransformer);
 
         assertManifestTransformations(bundleManifest, "web-bundle");
     }
 
     private GraphNode<InstallArtifact> createGraphNode(final InstallArtifact installArtifact) {
         return new GraphNode<InstallArtifact>() {
 
             /** 
              * {@inheritDoc}
              */
             @Override
             public void addChild(GraphNode<InstallArtifact> arg0) {
                 throw new UnsupportedOperationException();
             }
 
             /** 
              * {@inheritDoc}
              */
             @Override
             public List<GraphNode<InstallArtifact>> getChildren() {
                 throw new UnsupportedOperationException();
             }
 
             /** 
              * {@inheritDoc}
              */
             @Override
             public List<GraphNode<InstallArtifact>> getParents() {
                 throw new UnsupportedOperationException();
             }
 
             /** 
              * {@inheritDoc}
              */
             @Override
             public InstallArtifact getValue() {
                 return installArtifact;
             }
 
             /** 
              * {@inheritDoc}
              */
             @Override
             public boolean isRootNode() {
                 throw new UnsupportedOperationException();
             }
 
             /** 
              * {@inheritDoc}
              */
             @Override
             public boolean removeChild(GraphNode<InstallArtifact> arg0) {
                 throw new UnsupportedOperationException();
             }
 
             /** 
              * {@inheritDoc}
              */
             @Override
             public int size() {
                 throw new UnsupportedOperationException();
             }
 
             /** 
              * {@inheritDoc}
              */
             @Override
             public void visit(DirectedAcyclicGraphVisitor<InstallArtifact> visitor) {
                 visitor.visit(this);
             }
 
             /** 
              * {@inheritDoc}
              */
             @Override
             public <E extends Exception> void visit(ExceptionThrowingDirectedAcyclicGraphVisitor<InstallArtifact, E> visitor) throws E {
                 visitor.visit(this);
             }
             
         };
     }
 
     @Test
     public void nonBundleInstallArtifactTransformation() throws DeploymentException {
         InstallArtifact installArtifact = TestUtils.createInstallArtifact("foo", new Version(1, 0, 0), new File("location"), URI.create("file:/bar"));
 
         replay(manifestTransformer);
 
         GraphNode<InstallArtifact> installGraph = createGraphNode(installArtifact);
         webBundleTransformer.transform(installGraph, null);
 
         verify(manifestTransformer);
     }
 
     @Test
     public void nonWarBundleInstallArtifactTransformation() throws DeploymentException {
         BundleManifest bundleManifest = new StandardBundleManifest(null);
         bundleManifest.getBundleSymbolicName().setSymbolicName("foo.jar");
 
         URI sourceUri = URI.create("file:/bar");
         BundleInstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(sourceUri, new File("location"), bundleManifest);
 
         replay(manifestTransformer);
 
         GraphNode<InstallArtifact> installGraph = createGraphNode(installArtifact);
         webBundleTransformer.transform(installGraph, null);
 
         verify(manifestTransformer);
 
         assertManifestTransformations(bundleManifest, null);
     }
 
     @Test
     public void bundleWithWebContextPathTransformation() throws DeploymentException, IOException {
         BundleManifest bundleManifest = new StandardBundleManifest(null);
         bundleManifest.setHeader("Web-ContextPath", "/foo");
         bundleManifest.getBundleSymbolicName().setSymbolicName("foo.jar");
 
         File sourceFile = new File("/foo.jar");
         URI sourceUri = sourceFile.toURI();
         BundleInstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(sourceUri, sourceFile, bundleManifest);
 
         manifestTransformer.transform(eq(bundleManifest), eq(sourceUri.toURL()), isA(InstallationOptions.class), eq(true));
 
         replay(manifestTransformer);
 
         GraphNode<InstallArtifact> installGraph = createGraphNode(installArtifact);
         webBundleTransformer.transform(installGraph, null);
 
         verify(manifestTransformer);
 
         assertManifestTransformations(bundleManifest, "web-bundle");
     }
 
     @Test
     public void ignoreWebBundlesWithExistingModuleType() throws DeploymentException {
         BundleManifest bundleManifest = new StandardBundleManifest(null);
         bundleManifest.getBundleSymbolicName().setSymbolicName("foo.war");
         bundleManifest.setModuleType("web-slice");
 
         URI sourceUri = URI.create("file:/bar");
         BundleInstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(sourceUri, new File("location"), bundleManifest);
 
         replay(manifestTransformer);
 
         GraphNode<InstallArtifact> installGraph = createGraphNode(installArtifact);
         webBundleTransformer.transform(installGraph, null);
 
         verify(manifestTransformer);
 
         assertManifestTransformations(bundleManifest, "web-slice");
     }
 
     @Test
     public void testWABHeaderDefaulting() throws IOException, DeploymentException {
         BundleManifest bundleManifest = new StandardBundleManifest(null);
         bundleManifest.getBundleSymbolicName().setSymbolicName("foo.war");
 
         File sourceFile = new File("/bar.war");
         URI sourceUri = sourceFile.toURI();
 
         BundleInstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(sourceUri, sourceFile, bundleManifest);
 
         manifestTransformer.transform(eq(bundleManifest), eq(sourceUri.toURL()), isA(InstallationOptions.class), eq(true));
 
         replay(manifestTransformer);
 
         GraphNode<InstallArtifact> installGraph = createGraphNode(installArtifact);
         webBundleTransformer.transform(installGraph, null);
 
         verify(manifestTransformer);
 
         assertNull(bundleManifest.getHeader("org-eclipse-gemini-web-DefaultWABHeaders"));
     }
 
     @Test
     public void testWABHeaderStrictProcessing() throws IOException, DeploymentException {
         ConfigurationAdmin configAdmin = createMock(ConfigurationAdmin.class);
         Configuration config = createMock(Configuration.class);
         expect(configAdmin.getConfiguration(eq("org.eclipse.virgo.web"), (String) isNull())).andReturn(config);
        Dictionary<String, String> properties = new Hashtable<String, String>();
         properties.put("WABHeaders", "strict");
         expect(config.getProperties()).andReturn(properties);
         EventLogger eventLogger = createMock(EventLogger.class);
 
         WebDeploymentEnvironment environment = new WebDeploymentEnvironment(webContainer, applicationRegistry, manifestTransformer, configAdmin,
             eventLogger);
 
         BundleManifest bundleManifest = new StandardBundleManifest(null);
         bundleManifest.getBundleSymbolicName().setSymbolicName("foo.war");
 
         File sourceFile = new File("/bar.war");
         URI sourceUri = sourceFile.toURI();
 
         BundleInstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(sourceUri, sourceFile, bundleManifest);
 
         manifestTransformer.transform(eq(bundleManifest), eq(sourceUri.toURL()), isA(InstallationOptions.class), eq(true));
 
         replay(manifestTransformer, configAdmin, config);
 
         WebBundleTransformer webBundleTransformer = new WebBundleTransformer(environment);
 
         GraphNode<InstallArtifact> installGraph = createGraphNode(installArtifact);
         webBundleTransformer.transform(installGraph, null);
 
         verify(manifestTransformer, configAdmin, config);
 
         assertNull(bundleManifest.getHeader("org-eclipse-gemini-web-DefaultWABHeaders"));
     }
 
     @Test
     public void testWABHeaderExplicitDefaultProcessing() throws IOException, DeploymentException {
         ConfigurationAdmin configAdmin = createMock(ConfigurationAdmin.class);
         Configuration config = createMock(Configuration.class);
         expect(configAdmin.getConfiguration(eq("org.eclipse.virgo.web"), (String) isNull())).andReturn(config);
        Dictionary<String, String> properties = new Hashtable<String, String>();
         properties.put("WABHeaders", "defaulted");
         expect(config.getProperties()).andReturn(properties);
         EventLogger eventLogger = createMock(EventLogger.class);
 
         WebDeploymentEnvironment environment = new WebDeploymentEnvironment(webContainer, applicationRegistry, manifestTransformer, configAdmin,
             eventLogger);
 
         BundleManifest bundleManifest = new StandardBundleManifest(null);
         bundleManifest.getBundleSymbolicName().setSymbolicName("foo.war");
 
         File sourceFile = new File("/bar.war");
         URI sourceUri = sourceFile.toURI();
 
         BundleInstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(sourceUri, sourceFile, bundleManifest);
 
         manifestTransformer.transform(eq(bundleManifest), eq(sourceUri.toURL()), isA(InstallationOptions.class), eq(true));
 
         replay(manifestTransformer, configAdmin, config);
 
         WebBundleTransformer webBundleTransformer = new WebBundleTransformer(environment);
 
         GraphNode<InstallArtifact> installGraph = createGraphNode(installArtifact);
         webBundleTransformer.transform(installGraph, null);
 
         verify(manifestTransformer, configAdmin, config);
 
         assertEquals("true", bundleManifest.getHeader("org-eclipse-gemini-web-DefaultWABHeaders"));
     }
 
     public void assertManifestTransformations(BundleManifest bundleManifest, String expectedModuleType) {
         assertEquals(expectedModuleType, bundleManifest.getHeader("Module-Type"));
     }
 }
