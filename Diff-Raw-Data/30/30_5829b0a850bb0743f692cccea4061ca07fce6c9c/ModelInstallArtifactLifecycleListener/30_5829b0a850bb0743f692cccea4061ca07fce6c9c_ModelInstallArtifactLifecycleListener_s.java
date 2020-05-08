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
 
 package org.eclipse.virgo.kernel.model.internal.deployer;
 
 import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
 import org.eclipse.virgo.kernel.serviceability.NonNull;
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListenerSupport;
 import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
 import org.eclipse.virgo.kernel.model.Artifact;
 import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
 import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
 
 /**
  * Implementation of {@link InstallArtifactLifecycleListener} that notices
  * {@link InstallArtifactLifecycleListener#onInstalling(InstallArtifact)} and
  * {@link InstallArtifactLifecycleListener#onUninstalled(InstallArtifact)} calls to add and remove respectively
  * {@link Artifact}s from the {@link RuntimeArtifactRepository}
  * <p />
  * An existing artifact with the same type, name, and version as the incoming artifact is not replaced except in the
  * case of a bundle. An existing {@link BundleArtifact} is replaced by an incoming {@link DeployerBundleArtifact}. This
  * prevents the RAM being corrupted if an attempt it made to install an artifact which belongs to an existing install
  * tree. See {@link TreeRestrictingInstallArtifactLifecycleListener}.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Threadsafe
  * 
  */
 class ModelInstallArtifactLifecycleListener extends InstallArtifactLifecycleListenerSupport {
 
     private static final String USER_REGION_NAME = "org.eclipse.virgo.region.user";
 
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
     private final RuntimeArtifactRepository artifactRepository;
 
     private final BundleContext bundleContext;
 
     private final RegionDigraph regionDigraph;
 
     public ModelInstallArtifactLifecycleListener(@NonNull BundleContext bundleContext, @NonNull RuntimeArtifactRepository artifactRepository,
         @NonNull RegionDigraph regionDigraph) {
         this.bundleContext = bundleContext;
         this.artifactRepository = artifactRepository;
         this.regionDigraph = regionDigraph;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void onInstalling(InstallArtifact installArtifact) throws DeploymentException {
         logEvent("installing", installArtifact);
         if (installArtifact instanceof PlanInstallArtifact) {
             addPlan((PlanInstallArtifact) installArtifact);
         } else if (installArtifact instanceof BundleInstallArtifact) {
             addOrReplaceBundle((BundleInstallArtifact) installArtifact);
         } else {
             addArtifact(installArtifact);
         }
     }
 
     private void addPlan(PlanInstallArtifact planInstallArtifact) {
         this.artifactRepository.add(new DeployerCompositeArtifact(this.bundleContext, planInstallArtifact));
     }
 
     private void addOrReplaceBundle(BundleInstallArtifact bundleInstallArtifact) {
         Artifact existingBundleArtifact = this.artifactRepository.getArtifact(bundleInstallArtifact.getType(), bundleInstallArtifact.getName(), bundleInstallArtifact.getVersion(), this.regionDigraph.getRegion(USER_REGION_NAME));
         if (!(existingBundleArtifact instanceof DeployerBundleArtifact)) {
             remove(bundleInstallArtifact);
             this.artifactRepository.add(new DeployerBundleArtifact(this.bundleContext, bundleInstallArtifact, this.regionDigraph.getRegion(USER_REGION_NAME)));
         }
     }
 
     private void addArtifact(InstallArtifact installArtifact) {
         this.artifactRepository.add(new DeployerArtifact(this.bundleContext, installArtifact));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void onInstallFailed(InstallArtifact installArtifact) throws DeploymentException {
         logEvent("install failed", installArtifact);
         remove(installArtifact);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void onUninstalled(InstallArtifact installArtifact) throws DeploymentException {
         logEvent("uninstalled", installArtifact);
         remove(installArtifact);
     }
 
     private void logEvent(String event, InstallArtifact installArtifact) {
         this.logger.info("Processing " + event + " event for {} '{}' version '{}'",
             new String[] { installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion().toString() });
     }
 
     private void remove(InstallArtifact installArtifact) {
         if(installArtifact instanceof BundleInstallArtifact){
            this.artifactRepository.remove(installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion(), this.regionDigraph.getRegion(((BundleInstallArtifact) installArtifact).getBundle()));
         } else {
             this.artifactRepository.remove(installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion(), null);
         }
     }
 
 }
