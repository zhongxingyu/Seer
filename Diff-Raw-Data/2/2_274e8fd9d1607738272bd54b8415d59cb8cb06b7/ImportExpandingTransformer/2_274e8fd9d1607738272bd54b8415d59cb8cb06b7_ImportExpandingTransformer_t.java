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
 
 package org.eclipse.virgo.kernel.deployer.core.internal;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.virgo.kernel.osgi.framework.ImportExpander;
 import org.eclipse.virgo.kernel.osgi.framework.ImportMergeException;
 import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyDependenciesException;
 
 import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
 import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
 import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
 import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
 import org.eclipse.virgo.kernel.install.pipeline.stage.transform.internal.BundleInstallArtifactGatheringTreeVisitor;
 import org.eclipse.virgo.util.common.Tree;
 import org.eclipse.virgo.util.common.Tree.ExceptionThrowingTreeVisitor;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
 
 /**
  * {@link ImportExpandingTransformer} is a {@link Transformer} that expands Import-Library and Import-Bundle into package imports.
  * Expansion of imports in bundles that are part of a scope is performed against all of the bundles at the same time.
  * Expansion of bundles that are not part of a scope is performed on a bundle-by-bundle basis.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Thread-safe.
  * 
  */
 final class ImportExpandingTransformer implements Transformer {
 
     private final ImportExpander importExpander;
 
     ImportExpandingTransformer(ImportExpander importExpander) {
         this.importExpander = importExpander;
     }
 
     /**
      * {@inheritDoc}
      */
     public void transform(Tree<InstallArtifact> installTree, final InstallEnvironment installEnvironment) throws DeploymentException {
         installTree.visit(new ImportExpandingTreeVisitor(installEnvironment));
     }
 
     private final class ImportExpandingTreeVisitor implements ExceptionThrowingTreeVisitor<InstallArtifact, DeploymentException> {
 
         private final InstallEnvironment installEnvironment;
 
         ImportExpandingTreeVisitor(InstallEnvironment installEnvironment) {
             this.installEnvironment = installEnvironment;
         }
 
         /**
          * {@inheritDoc}
          */
         public boolean visit(Tree<InstallArtifact> tree) throws DeploymentException {
             if (tree.getValue() instanceof PlanInstallArtifact) {
                 PlanInstallArtifact planInstallArtifact = (PlanInstallArtifact) tree.getValue();
                 if (planInstallArtifact.isScoped()) {
                     expandImportsOfBundlesInScopedPlan(tree, this.installEnvironment);
                     return false;
                 }
             } else if (tree.getValue() instanceof BundleInstallArtifact) {
                 expandImports(Collections.singleton((BundleInstallArtifact) tree.getValue()), this.installEnvironment);
             }
             return true;
         }
     }
 
     void expandImportsOfBundlesInScopedPlan(Tree<InstallArtifact> planTree, InstallEnvironment installEnvironment) throws DeploymentException {
         BundleInstallArtifactGatheringTreeVisitor visitor = new BundleInstallArtifactGatheringTreeVisitor();
         planTree.visit(visitor);
         expandImports(visitor.getChildBundles(), installEnvironment);
     }
 
     void expandImports(Set<BundleInstallArtifact> bundleInstallArtifacts, InstallEnvironment installEnvironment) throws DeploymentException {
 
         List<BundleManifest> bundleManifestList = new ArrayList<BundleManifest>(bundleInstallArtifacts.size());
 
         for (BundleInstallArtifact bundleInstallArtifact : bundleInstallArtifacts) {
             try {
                 BundleManifest bundleManifest = bundleInstallArtifact.getBundleManifest();
                 bundleManifestList.add(bundleManifest);
             } catch (IOException e) {
                 installEnvironment.getInstallLog().log(this, "I/O error getting bundle manifest for  %s", bundleInstallArtifact.toString());
                 throw new DeploymentException("I/O error getting bundle manifest for " + bundleInstallArtifact, e);
             }
         }
 
         try {
             this.importExpander.expandImports(bundleManifestList);
             installEnvironment.getInstallLog().log(this, "Expanded imports of %s", bundleInstallArtifacts.toString());
         } catch (ImportMergeException e) {
             installEnvironment.getInstallLog().log(this, "Error in %s merging expanded imports for package %s from %s",
                 bundleInstallArtifacts.toString(), e.getConflictingPackageName(), e.getSources());
             throw new DeploymentException("Error merging expanded imports for " + bundleInstallArtifacts, e);
         } catch (UnableToSatisfyDependenciesException e) {
             installEnvironment.getInstallLog().log(this, "Unsatisfied dependencies in %s: %s", bundleInstallArtifacts.toString(),
                 e.getFailureDescription());
            throw new DeploymentException(e.getMessage(), e);
         }
     }
 }
