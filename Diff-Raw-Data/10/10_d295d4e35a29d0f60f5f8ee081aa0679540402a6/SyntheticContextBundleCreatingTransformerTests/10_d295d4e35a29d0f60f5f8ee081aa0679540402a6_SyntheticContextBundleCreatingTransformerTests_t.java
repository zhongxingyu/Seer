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
 
 package org.eclipse.virgo.kernel.install.pipeline.stage.transform.internal;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.eq;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.isA;
 import static org.easymock.EasyMock.isNull;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.JarFile;
 
 import org.eclipse.virgo.kernel.artifact.fs.StandardArtifactFSFactory;
 import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
 import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
 import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
 import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifactGraphFactory;
 import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.internal.ArtifactStorageFactory;
 import org.eclipse.virgo.kernel.install.artifact.internal.StandardArtifactStorageFactory;
 import org.eclipse.virgo.kernel.install.artifact.internal.scoping.ScopeNameFactory;
 import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
 import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
 import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
 import org.eclipse.virgo.util.common.DirectedAcyclicGraph;
 import org.eclipse.virgo.util.common.GraphNode;
 import org.eclipse.virgo.util.common.ThreadSafeDirectedAcyclicGraph;
 import org.eclipse.virgo.util.io.PathReference;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
 import org.eclipse.virgo.util.osgi.manifest.ImportedBundle;
 import org.junit.Before;
 import org.junit.Test;
 import org.osgi.framework.Version;
 
 /**
  */
 public final class SyntheticContextBundleCreatingTransformerTests {
 
     private final InstallArtifactGraphFactory installArtifactGraphFactory = createMock(InstallArtifactGraphFactory.class);
 
     private final InstallEnvironment installEnvironment = createMock(InstallEnvironment.class);
 
     private final ArtifactStorageFactory artifactStorageFactory = new StandardArtifactStorageFactory(new PathReference("target/work"),
         new StandardArtifactFSFactory(), new MockEventLogger(), "true");
 
     private final Transformer transformer = new SyntheticContextBundleCreatingTransformer(this.installArtifactGraphFactory,
         this.artifactStorageFactory);
 
     private DirectedAcyclicGraph<InstallArtifact> dag;
 
     @Before
     public void createGraph() {
     		this.dag = new ThreadSafeDirectedAcyclicGraph<InstallArtifact>();
    		new PathReference("target/work/staging/plan-name-1").delete(true);
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void basicSyntheticContextCreation() throws DeploymentException, FileNotFoundException, IOException {
         GraphNode<InstallArtifact> planInstallGraph = createMockPlan(true, new Version(1, 0, 0), "plan-name", "bundle1", "bundle2", "bundle3");
         InstallArtifact syntheticContextInstallArtifact = createMock(InstallArtifact.class);
 
        File syntheticBundleDir = new File("target/work/staging/plan-name-1/0/0/plan-name-1-synthetic.context.jar").getAbsoluteFile();
         expect(
             this.installArtifactGraphFactory.constructInstallArtifactGraph(eq(new ArtifactIdentity("bundle", "plan-name-1-synthetic.context",
                 new Version(1, 0, 0), ScopeNameFactory.createScopeName("plan-name", new Version(1, 0, 0)))), isA(ArtifactStorage.class),
                 (Map<String, String>) isNull(), (String) isNull())).andReturn(this.dag.createRootNode(syntheticContextInstallArtifact));
 
         replay(this.installEnvironment, this.installArtifactGraphFactory);
 
         this.transformer.transform(planInstallGraph, this.installEnvironment);
 
         verify(this.installEnvironment, this.installArtifactGraphFactory);
 
         File manifest = new File(syntheticBundleDir, JarFile.MANIFEST_NAME);
         assertTrue(manifest.exists());
 
         assertBundlesImported(manifest, "bundle1", "bundle2", "bundle3");
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void nestedPlanSyntheticContextCreation() throws DeploymentException, FileNotFoundException, IOException {
         GraphNode<InstallArtifact> rootPlanInstallGraph = createMockPlan(true, new Version(1, 0, 0), "plan-name", "bundle1");
         rootPlanInstallGraph.addChild(createMockPlan(true, new Version(1, 0, 0), "nested-plan", "bundle2", "bundle3"));
 
         InstallArtifact syntheticContextInstallArtifact = createMock(InstallArtifact.class);
 
        File syntheticBundleDir = new File("target/work/staging/plan-name-1/0/0/plan-name-1-synthetic.context.jar").getAbsoluteFile();
         expect(
             this.installArtifactGraphFactory.constructInstallArtifactGraph(eq(new ArtifactIdentity("bundle", "plan-name-1-synthetic.context",
                 new Version(1, 0, 0), ScopeNameFactory.createScopeName("plan-name", new Version(1, 0, 0)))), isA(ArtifactStorage.class),
                 (Map<String, String>) isNull(), (String) isNull())).andReturn(this.dag.createRootNode(syntheticContextInstallArtifact));
 
         replay(this.installEnvironment, this.installArtifactGraphFactory);
 
         this.transformer.transform(rootPlanInstallGraph, this.installEnvironment);
 
         verify(this.installEnvironment, this.installArtifactGraphFactory);
 
         File manifest = new File(syntheticBundleDir, JarFile.MANIFEST_NAME);
         assertTrue(manifest.exists());
 
         assertBundlesImported(manifest, "bundle1", "bundle2", "bundle3");
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void syntheticContextOnlyCreatedForScopedPlans() throws DeploymentException, FileNotFoundException, IOException {
         GraphNode<InstallArtifact> rootPlanInstallGraph = createMockPlan(false, new Version(1, 0, 0), "plan-name", "bundle1");
         rootPlanInstallGraph.addChild(createMockPlan(true, new Version(1, 0, 0), "nested-plan", "bundle2", "bundle3"));
 
         InstallArtifact syntheticContextInstallArtifact = createMock(InstallArtifact.class);
 
         File syntheticBundleDir = new File(
            "target/work/staging/nested-plan-1/0/0/nested-plan-1-synthetic.context.jar").getAbsoluteFile();
         expect(
             this.installArtifactGraphFactory.constructInstallArtifactGraph(eq(new ArtifactIdentity("bundle", "nested-plan-1-synthetic.context",
                 new Version(1, 0, 0), ScopeNameFactory.createScopeName("nested-plan", new Version(1, 0, 0)))), isA(ArtifactStorage.class),
                 (Map<String, String>) isNull(), (String) isNull())).andReturn(this.dag.createRootNode(syntheticContextInstallArtifact));
 
         replay(this.installEnvironment, this.installArtifactGraphFactory);
 
         this.transformer.transform(rootPlanInstallGraph, this.installEnvironment);
 
         verify(this.installEnvironment, this.installArtifactGraphFactory);
 
         File manifest = new File(syntheticBundleDir, JarFile.MANIFEST_NAME);
         assertTrue(manifest.exists());
 
         assertBundlesImported(manifest, "bundle2", "bundle3");
     }
 
     private void assertBundlesImported(File manifestFile, String... symbolicNames) throws FileNotFoundException, IOException {
         BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest(new FileReader(manifestFile));
         List<ImportedBundle> importedBundles = bundleManifest.getImportBundle().getImportedBundles();
         assertEquals(symbolicNames.length, importedBundles.size());
 
         for (String symbolicName : symbolicNames) {
             assertBundleImported(importedBundles, symbolicName);
         }
     }
 
     private void assertBundleImported(List<ImportedBundle> importedBundles, String symbolicName) {
         for (ImportedBundle importedBundle : importedBundles) {
             if (symbolicName.equals(importedBundle.getBundleSymbolicName())) {
                 return;
             }
         }
         fail("No import for symbolic name '" + symbolicName + "' was found among imported bundles " + importedBundles);
     }
 
     private InstallArtifact createMockBundleInstallArtifact(String symbolicName) {
         InstallArtifact bundle = createMock(BundleInstallArtifact.class);
         expect(bundle.getName()).andReturn(symbolicName).anyTimes();
         replay(bundle);
         return bundle;
     }
 
     private GraphNode<InstallArtifact> createMockPlan(boolean scoped, Version version, String name, String... bundleSymbolicNames) {
         PlanInstallArtifact plan = createMock(PlanInstallArtifact.class);
 
         expect(plan.isScoped()).andReturn(scoped).anyTimes();
         expect(plan.getVersion()).andReturn(version).anyTimes();
         expect(plan.getName()).andReturn(name).anyTimes();
 
         replay(plan);
 
         GraphNode<InstallArtifact> installTree = this.dag.createRootNode(plan);
 
         for (String bundleSymbolicName : bundleSymbolicNames) {
             InstallArtifact bundle = createMockBundleInstallArtifact(bundleSymbolicName);
             installTree.addChild(this.dag.createRootNode(bundle));
         }
         return installTree;
     }
 }
