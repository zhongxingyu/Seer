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
 
 import javax.annotation.PostConstruct;
 
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
 import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
 import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
 import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
 
 import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
 import org.eclipse.virgo.kernel.deployer.model.RuntimeArtifactModel;
 import org.eclipse.virgo.kernel.serviceability.NonNull;
 import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
 
 /**
  * An initializer responsible for registering a {@link ModelInstallArtifactLifecycleListener} and enumerating any
  * existing {@link InstallArtifact} objects from the Kernel deployer
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Thread safe.
  * 
  * @see ModelInstallArtifactLifecycleListener
  */
 public final class ModelInstallArtifactLifecycleListenerInitializer {
 
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
     private final RuntimeArtifactRepository artifactRepository;
 
     private final BundleContext bundleContext;
 
     private final RuntimeArtifactModel runtimeArtifactModel;
 
     private final ServiceRegistrationTracker registrationTracker = new ServiceRegistrationTracker();
 
     private final RegionDigraph regionDigraph;
 
     public ModelInstallArtifactLifecycleListenerInitializer(@NonNull RuntimeArtifactRepository artifactRepository,
         @NonNull BundleContext bundleContext, @NonNull RuntimeArtifactModel runtimeArtifactModel, @NonNull RegionDigraph regionDigraph) {
         this.artifactRepository = artifactRepository;
         this.bundleContext = bundleContext;
         this.runtimeArtifactModel = runtimeArtifactModel;
         this.regionDigraph = regionDigraph;
     }
 
     /**
      * Registers a {@link ModelInstallArtifactLifecycleListener} with the service registry. Enumerates any existing
      * {@link InstallArtifact} objects that exist from the Kernel deployer
      */
     @PostConstruct
     public void initialize() {
        ModelInstallArtifactLifecycleListener listener = new ModelInstallArtifactLifecycleListener(this.bundleContext, this.artifactRepository,
            this.regionDigraph);
         this.registrationTracker.track(this.bundleContext.registerService(InstallArtifactLifecycleListener.class.getCanonicalName(), listener, null));
         for (DeploymentIdentity deploymentIdentity : this.runtimeArtifactModel.getDeploymentIdentities()) {
             InstallArtifact installArtifact = this.runtimeArtifactModel.get(deploymentIdentity);
             try {
                 if (installArtifact instanceof PlanInstallArtifact) {
                     this.artifactRepository.add(new DeployerCompositeArtifact(this.bundleContext, (PlanInstallArtifact) installArtifact));
                 } else if (installArtifact instanceof BundleInstallArtifact) {
                    this.artifactRepository.remove(installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion(), null);
                     BundleInstallArtifact bundleInstallArtifact = (BundleInstallArtifact) installArtifact;
                    this.artifactRepository.add(new DeployerBundleArtifact(this.bundleContext, bundleInstallArtifact,
                        this.regionDigraph.getRegion(bundleInstallArtifact.getBundle())));
                 } else {
                     this.artifactRepository.remove(installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion(), null);
                     this.artifactRepository.add(new DeployerArtifact(this.bundleContext, installArtifact));
                 }
             } catch (Exception e) {
                 logger.error(String.format("Exception adding deployer artifact '%s:%s' to the repository", installArtifact.getName(),
                     installArtifact.getVersion().toString()), e);
             }
         }
     }
 
     /**
      * Unregisters the listener from the service registry
      */
     public void destroy() {
         this.registrationTracker.unregisterAll();
     }
 }
