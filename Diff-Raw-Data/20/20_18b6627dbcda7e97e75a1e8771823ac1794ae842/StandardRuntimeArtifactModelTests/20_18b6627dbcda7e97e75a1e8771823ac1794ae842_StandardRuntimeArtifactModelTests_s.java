 /*******************************************************************************
  * Copyright (c) 2008, 2011 VMware Inc. and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.deployer.model.internal;
 
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
 import org.eclipse.virgo.nano.core.AbortableSignal;
 import org.eclipse.virgo.nano.deployer.api.core.DeployUriNormaliser;
 import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
 import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
 import org.eclipse.virgo.kernel.deployer.core.internal.StandardDeploymentIdentity;
 import org.eclipse.virgo.kernel.deployer.model.DuplicateDeploymentIdentityException;
 import org.eclipse.virgo.kernel.deployer.model.DuplicateFileNameException;
 import org.eclipse.virgo.kernel.deployer.model.DuplicateLocationException;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
 import org.eclipse.virgo.util.common.GraphNode;
 import org.junit.Before;
 import org.junit.Test;
 import org.osgi.framework.Version;
 
 /**
  * 
  * StandardRuntimeArtifactModelTests
  * <p />
  */
 public class StandardRuntimeArtifactModelTests {
 
     private final InstallArtifact stubInstallArtifact = new StubInstallArtifact();
     
     private final DeploymentIdentity deploymentIdentity = new StandardDeploymentIdentity(stubInstallArtifact.getType(), stubInstallArtifact.getName(), stubInstallArtifact.getVersion().toString());
     
     private StandardRuntimeArtifactModel standardRuntimeArtifactModel; 
     
     private URI testURI;
     
     @Before
     public void setUp() throws URISyntaxException, DuplicateFileNameException, DuplicateLocationException, DuplicateDeploymentIdentityException, DeploymentException{
         this.testURI =  new URI("file:/foo/test.bar");
         this.standardRuntimeArtifactModel = new StandardRuntimeArtifactModel(new DeployUriNormaliser(){
 
             @Override
             public URI normalise(URI uri) throws DeploymentException {
                 return uri;
             }
             
         });
 
         this.standardRuntimeArtifactModel.add(this.testURI, this.stubInstallArtifact);
     }
 
     @Test
     public void testGetInstallArtifactByDeploymentIdentity() {
         assertEquals(this.stubInstallArtifact, this.standardRuntimeArtifactModel.get(this.deploymentIdentity));
     }
     
     @Test
     public void testGetInstallArtifactByLocation() {
         assertEquals(this.stubInstallArtifact, this.standardRuntimeArtifactModel.get(this.testURI));
     }
     
     @Test
     public void testGetLocationByDeploymentIdentity() {
         URI location = this.standardRuntimeArtifactModel.getLocation(this.deploymentIdentity);
        assertEquals(this.testURI, location);
         assertEquals("file", location.getScheme());
        assertEquals("/foo/test.bar", location.getPath());
     }
     
     @Test
     public void testDelete() throws DeploymentException {
         assertEquals(this.stubInstallArtifact, this.standardRuntimeArtifactModel.get(this.deploymentIdentity));
         this.standardRuntimeArtifactModel.delete(this.deploymentIdentity);
         assertNull(this.standardRuntimeArtifactModel.get(this.deploymentIdentity));
     }
     
     @Test
     public void testisGCRoot() {
         assertTrue(this.standardRuntimeArtifactModel.isGCRoot(this.stubInstallArtifact));
         assertFalse(this.standardRuntimeArtifactModel.isGCRoot(new StubInstallArtifact()));
     }
     
 
     @Test
     public void testGCRootIterator() {
         Iterator<InstallArtifact> iterator = this.standardRuntimeArtifactModel.iterator();
         assertTrue(iterator.hasNext());
         assertEquals(this.stubInstallArtifact, iterator.next());
         assertFalse(iterator.hasNext());
     }
     
     @Test
     public void testDirectoryDeletion() throws DeploymentException, IOException, DuplicateFileNameException, DuplicateLocationException, DuplicateDeploymentIdentityException {
         // Reset state first.
         this.standardRuntimeArtifactModel.delete(this.deploymentIdentity);
         assertNull(this.standardRuntimeArtifactModel.get(this.deploymentIdentity));
         
         // Now deploy a file, delete it, and then get the deployed artifact.
         File dir = new File("target/StandardRuntimeArtifactModelTest.dir/");
         dir.mkdir();
         URI uri = dir.toURI();
         this.standardRuntimeArtifactModel.add(uri, this.stubInstallArtifact);
         assertEquals(this.stubInstallArtifact, this.standardRuntimeArtifactModel.get(uri));
         
         assertTrue(dir.delete());
         
         assertEquals(this.stubInstallArtifact, this.standardRuntimeArtifactModel.get(uri));
 
     }
     
     private static class StubInstallArtifact implements InstallArtifact {
 
         private volatile GraphNode<InstallArtifact> graph;
 
         public GraphNode<InstallArtifact> getGraph() {
             return this.graph;
         }
 
         public void stop() throws DeploymentException {
         }
 
         public void start() throws DeploymentException {
             start(null);
         }
 
         public void start(AbortableSignal signal) throws DeploymentException {
         }
 
         public void uninstall() throws DeploymentException {
         }
 
         public ArtifactFS getArtifactFS() {
             throw new UnsupportedOperationException();
         }
 
         public String getName() {
             return("test-name");
         }
 
         public String getRepositoryName() {
             return("test-repository");
         }
 
         public State getState() {
             throw new UnsupportedOperationException();
         }
 
         public String getType() {
             return("test-type");
         }
 
         public Version getVersion() {
             return Version.emptyVersion;
         }
 
         public boolean refresh() {
             throw new UnsupportedOperationException();
         }
 
         public String getProperty(String name) {
             throw new UnsupportedOperationException();
         }
 
         public Set<String> getPropertyNames() {
             throw new UnsupportedOperationException();
         }
 
         public String setProperty(String name, String value) {
             throw new UnsupportedOperationException();
         }
 
         public String getScopeName() {
             return null;
         }
     }
     
 }
