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
 
 import java.io.File;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.osgi.framework.Version;
 
 import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;
 import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyDependenciesException;
 
 import org.eclipse.virgo.kernel.core.KernelException;
 import org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer;
 import org.eclipse.virgo.kernel.deployer.core.DeployUriNormaliser;
 import org.eclipse.virgo.kernel.deployer.core.DeployerConfiguration;
 import org.eclipse.virgo.kernel.deployer.core.DeployerLogEvents;
 import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
 import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
 import org.eclipse.virgo.kernel.deployer.core.internal.event.DeploymentListener;
 import org.eclipse.virgo.kernel.deployer.model.DuplicateDeploymentIdentityException;
 import org.eclipse.virgo.kernel.deployer.model.DuplicateFileNameException;
 import org.eclipse.virgo.kernel.deployer.model.DuplicateLocationException;
 import org.eclipse.virgo.kernel.deployer.model.RuntimeArtifactModel;
 import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifactTreeInclosure;
 import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
 import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
 import org.eclipse.virgo.kernel.install.environment.InstallEnvironmentFactory;
 import org.eclipse.virgo.kernel.install.pipeline.Pipeline;
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 import org.eclipse.virgo.util.common.Tree;
 import org.eclipse.virgo.util.io.PathReference;
 
 /**
  * {@link PipelinedApplicationDeployer} is an implementation of {@link ApplicationDeployer} which creates a {@link Tree}
  * of {@link InstallArtifact InstallArtifacts} and processes the tree by passing it through a {@link Pipeline} while
  * operating on an {@link InstallEnvironment}.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * This class is thread safe.
  * 
  */
 final class PipelinedApplicationDeployer implements ApplicationDeployer, ApplicationRecoverer {
 
     private static final String BUNDLE_TYPE = "bundle";
 
     private final EventLogger eventLogger;
 
     private final Object monitor = new Object();
 
     private final InstallEnvironmentFactory installEnvironmentFactory;
 
     private final InstallArtifactTreeInclosure installArtifactTreeInclosure;
 
     private final RuntimeArtifactModel ram;
 
     private final DeploymentListener deploymentListener;
 
     private final Map<DeploymentIdentity, DeploymentOptions> deploymentOptionsMap = new HashMap<DeploymentIdentity, DeploymentOptions>();
 
     private final Pipeline pipeline;
 
     private final DeployUriNormaliser deployUriNormaliser;
 
     private final int deployerConfiguredTimeoutInSeconds;
 
     public PipelinedApplicationDeployer(Pipeline pipeline, InstallArtifactTreeInclosure installArtifactTreeInclosure,
         InstallEnvironmentFactory installEnvironmentFactory, RuntimeArtifactModel ram, DeploymentListener deploymentListener,
         EventLogger eventLogger, DeployUriNormaliser normaliser, DeployerConfiguration deployerConfiguration) {
         this.eventLogger = eventLogger;
         this.installArtifactTreeInclosure = installArtifactTreeInclosure;
         this.installEnvironmentFactory = installEnvironmentFactory;
         this.ram = ram;
         this.deploymentListener = deploymentListener;
         this.deployUriNormaliser = normaliser;
 
         this.pipeline = pipeline;
         this.deployerConfiguredTimeoutInSeconds = deployerConfiguration.getDeploymentTimeoutSeconds();
     }
 
     /**
      * {@inheritDoc}
      */
     public DeploymentIdentity deploy(URI location) throws DeploymentException {
         synchronized (this.monitor) {
             return deploy(location, new DeploymentOptions());
         }
     }
 
     private URI normaliseDeploymentUri(URI uri) throws DeploymentException {
         URI normalisedLocation = this.deployUriNormaliser.normalise(uri);
 
         if (normalisedLocation == null) {
             this.eventLogger.log(DeployerLogEvents.UNSUPPORTED_URI_SCHEME, uri, uri.getScheme());
             throw new DeploymentException("PipelinedApplicationDeployer.deploy does not support '" + uri.getScheme() + "' scheme URIs");
         }
 
         return normalisedLocation;
     }
 
     public DeploymentIdentity install(URI location) throws DeploymentException {
         return install(location, new DeploymentOptions());
     }
 
     public DeploymentIdentity install(URI uri, DeploymentOptions deploymentOptions) throws DeploymentException {
         URI normalisedUri = normaliseDeploymentUri(uri);
 
         DeploymentIdentity deploymentIdentity = doInstall(normalisedUri, deploymentOptions);
         this.deploymentListener.deployed(normalisedUri, deploymentOptions);
 
         return deploymentIdentity;
     }
 
     private DeploymentIdentity doInstall(URI normalisedUri, DeploymentOptions deploymentOptions) throws DeploymentException {
         synchronized (this.monitor) {
             InstallArtifact existingArtifact = this.ram.get(normalisedUri);
 
             if (existingArtifact != null) {
                 DeploymentIdentity refreshedIdentity = updateAndRefreshExistingArtifact(normalisedUri, existingArtifact);
                 if (refreshedIdentity != null) {
                     return refreshedIdentity;
                 }
             }
 
             Tree<InstallArtifact> installTree = this.installArtifactTreeInclosure.createInstallTree(new File(normalisedUri));
             DeploymentIdentity deploymentIdentity;
 
             try {
                 deploymentIdentity = addTreeToModel(normalisedUri, installTree);
             } catch (KernelException ke) {
                 throw new DeploymentException(ke.getMessage(), ke);
             }
 
             this.deploymentOptionsMap.put(deploymentIdentity, deploymentOptions);
             try {
                 driveInstallPipeline(normalisedUri, installTree);
             } catch (DeploymentException de) {
                 this.ram.delete(deploymentIdentity);
                 throw de;
            } catch (RuntimeException re) {
                this.ram.delete(deploymentIdentity);
                throw re;
             }
 
             return deploymentIdentity;
         }
     }
 
     private DeploymentIdentity updateAndRefreshExistingArtifact(URI normalisedLocation, InstallArtifact existingArtifact) throws DeploymentException {
         String oldType = existingArtifact.getType();
         String oldName = existingArtifact.getName();
         Version oldVersion = existingArtifact.getVersion();
 
         DeploymentIdentity deploymentIdentity = updateAndRefresh(normalisedLocation, existingArtifact);
         if (deploymentIdentity != null) {
             return deploymentIdentity;
         }
 
         DeploymentIdentity oldDeploymentIdentity = new StandardDeploymentIdentity(oldType, oldName, oldVersion.toString());
         undeployInternal(oldDeploymentIdentity, true, false);
 
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public DeploymentIdentity deploy(URI location, DeploymentOptions deploymentOptions) throws DeploymentException {
         URI normalisedLocation = normaliseDeploymentUri(location);
 
         InstallArtifact installedArtifact;
         DeploymentIdentity deploymentIdentity;
 
         synchronized (this.monitor) {
             deploymentIdentity = install(location, deploymentOptions);
             installedArtifact = this.ram.get(normalisedLocation);
         }
 
         try {
             start(installedArtifact, deploymentOptions.getSynchronous());
         } catch (DeploymentException de) {
             synchronized (this.monitor) {
                 stopArtifact(installedArtifact);
                 uninstallArtifact(installedArtifact);
             }
             throw de;
         }
 
         this.deploymentListener.deployed(normalisedLocation, deploymentOptions);
 
         return deploymentIdentity;
     }
 
     private DeploymentIdentity updateAndRefresh(URI location, InstallArtifact installArtifact) throws DeploymentException {
         DeploymentIdentity deploymentIdentity = null;
         this.installArtifactTreeInclosure.updateStagingArea(new File(location), new ArtifactIdentity(installArtifact.getType(),
             installArtifact.getName(), installArtifact.getVersion(), installArtifact.getScopeName()));
         if (installArtifact.refresh()) {
             this.deploymentListener.refreshed(location);
 
             deploymentIdentity = new StandardDeploymentIdentity(installArtifact.getType(), installArtifact.getName(),
                 installArtifact.getVersion().toString());
         }
         return deploymentIdentity;
     }
 
     /**
      * {@inheritDoc}
      */
     public DeploymentIdentity deploy(String type, String name, Version version) throws DeploymentException {
         throw new UnsupportedOperationException(
             "PipelinedApplicationDeployer ApplicationDeployer does not support deployment by type, name, and version");
     }
 
     /**
      * {@inheritDoc}
      */
     public DeploymentIdentity deploy(String type, String name, Version version, DeploymentOptions options) throws DeploymentException {
         throw new UnsupportedOperationException(
             "PipelinedApplicationDeployer ApplicationDeployer does not support deployment by type, name, and version");
     }
 
     private DeploymentIdentity addTreeToModel(URI location, Tree<InstallArtifact> installTree) throws DuplicateFileNameException,
         DuplicateLocationException, DuplicateDeploymentIdentityException, DeploymentException {
         return this.ram.add(location, installTree.getValue());
     }
 
     /**
      * {@inheritDoc}
      */
     public void recoverDeployment(URI uri, DeploymentOptions options) throws DeploymentException {
 
         Tree<InstallArtifact> installTree = this.installArtifactTreeInclosure.recoverInstallTree(new File(uri), options);
 
         if (installTree == null) {
             // Remove the URI from the recovery log.
             this.deploymentListener.undeployed(uri);
         } else {
             driveInstallPipeline(uri, installTree);
 
             start(installTree.getValue(), options.getSynchronous());
 
             try {
                 addTreeToModel(uri, installTree);
             } catch (KernelException e) {
                 throw new DeploymentException(e.getMessage(), e);
             }
         }
     }
 
     private void driveInstallPipeline(URI uri, Tree<InstallArtifact> installTree) throws DeploymentException {
 
         InstallEnvironment installEnvironment = this.installEnvironmentFactory.createInstallEnvironment(installTree.getValue());
 
         try {
             this.pipeline.process(installTree, installEnvironment);
         } catch (UnableToSatisfyBundleDependenciesException utsbde) {
             logDependencySatisfactionException(uri, utsbde);
             throw new DeploymentException("Dependency satisfaction failed", utsbde);
         }
     }
 
     private void logDependencySatisfactionException(URI uri, UnableToSatisfyDependenciesException ex) {
         this.eventLogger.log(DeployerLogEvents.UNABLE_TO_SATISFY_CONSTRAINTS, ex, uri, ex.getSymbolicName(), ex.getVersion(),
             ex.getFailureDescription());
     }
 
     private void start(InstallArtifact installArtifact, boolean synchronous) throws DeploymentException {
         BlockingSignal blockingSignal = new BlockingSignal(synchronous);
         installArtifact.start(blockingSignal);
         if (synchronous && this.deployerConfiguredTimeoutInSeconds > 0) {
             boolean complete = blockingSignal.awaitCompletion(this.deployerConfiguredTimeoutInSeconds);
             if (!complete) {
                 this.eventLogger.log(DeployerLogEvents.START_TIMED_OUT, installArtifact.getType(), installArtifact.getName(),
                     installArtifact.getVersion(), this.deployerConfiguredTimeoutInSeconds);
             }
         } else {
             // Completion messages will have been issued if complete, so ignore return value.
             blockingSignal.checkComplete();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public DeploymentIdentity[] getDeploymentIdentities() {
         synchronized (this.monitor) {
             return this.ram.getDeploymentIdentities();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public DeploymentIdentity getDeploymentIdentity(URI location) {
         synchronized (this.monitor) {
             InstallArtifact installArtifact = this.ram.get(location);
             if (installArtifact != null) {
                 return getDeploymentIdentity(installArtifact);
             }
         }
         return null;
     }
 
     private DeploymentIdentity getDeploymentIdentity(InstallArtifact installArtifact) {
         return new StandardDeploymentIdentity(installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion().toString());
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isDeployed(URI location) {
         URI normalisedLocation;
         try {
             normalisedLocation = this.deployUriNormaliser.normalise(location);
         } catch (DeploymentException e) {
             return false;
         }
 
         if (normalisedLocation == null) {
             this.eventLogger.log(DeployerLogEvents.UNSUPPORTED_URI_SCHEME, location.toString(), location.getScheme());
             return false;
         }
         synchronized (this.monitor) {
             return this.ram.get(normalisedLocation) != null;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public DeploymentIdentity refresh(URI location, String symbolicName) throws DeploymentException {
         URI normalisedLocation = this.deployUriNormaliser.normalise(location);
 
         if (normalisedLocation == null) {
             this.eventLogger.log(DeployerLogEvents.UNSUPPORTED_URI_SCHEME, location.toString(), location.getScheme());
             throw new DeploymentException("PipelinedApplicationDeployer.refresh does not support '" + location.getScheme() + "' scheme URIs");
         }
 
         DeploymentIdentity deploymentIdentity;
         synchronized (this.monitor) {
             InstallArtifact installArtifact = this.ram.get(normalisedLocation);
             if (installArtifact == null) {
                 this.eventLogger.log(DeployerLogEvents.REFRESH_REQUEST_URI_NOT_FOUND, location.toString());
                 throw new DeploymentException("Refresh not possible as no application is deployed from URI " + location);
             } else {
                 DeploymentIdentity originalDeploymentIdentity = getDeploymentIdentity(installArtifact);
                 deploymentIdentity = originalDeploymentIdentity;
                 try {
                     this.installArtifactTreeInclosure.updateStagingArea(new File(normalisedLocation), new ArtifactIdentity(installArtifact.getType(),
                         installArtifact.getName(), installArtifact.getVersion(), installArtifact.getScopeName()));
                     // Attempt to refresh the artifact and escalate to redeploy if this fails.
                     if (refreshInternal(symbolicName, installArtifact)) {
                         this.deploymentListener.refreshed(normalisedLocation);
                     } else {
                         DeploymentOptions deploymentOptions = this.deploymentOptionsMap.get(deploymentIdentity);
                         if (deploymentOptions == null) {
                             deploymentOptions = DeploymentOptions.DEFAULT_DEPLOYMENT_OPTIONS;
                         }
                         deploymentIdentity = redeploy(originalDeploymentIdentity, normalisedLocation, deploymentOptions);
                     }
                     this.eventLogger.log(DeployerLogEvents.REFRESH_REQUEST_COMPLETED, symbolicName, originalDeploymentIdentity.getType(),
                         originalDeploymentIdentity.getSymbolicName(), originalDeploymentIdentity.getVersion());
                 } catch (RuntimeException e) {
                     this.eventLogger.log(DeployerLogEvents.REFRESH_REQUEST_FAILED, e, symbolicName, originalDeploymentIdentity.getType(),
                         originalDeploymentIdentity.getSymbolicName(), originalDeploymentIdentity.getVersion());
                     throw e;
                 } catch (Exception e) {
                     this.eventLogger.log(DeployerLogEvents.REFRESH_REQUEST_FAILED, e, symbolicName, originalDeploymentIdentity.getType(),
                         originalDeploymentIdentity.getSymbolicName(), originalDeploymentIdentity.getVersion());
                     throw new DeploymentException("refresh failed", e);
                 }
             }
         }
         return deploymentIdentity;
     }
 
     private boolean refreshInternal(String symbolicName, InstallArtifact installArtifact) throws DeploymentException {
         if (installArtifact instanceof PlanInstallArtifact) {
             return ((PlanInstallArtifact) installArtifact).refresh(symbolicName);
         } else {
             return installArtifact.refresh();
         }
     }
 
     private DeploymentIdentity redeploy(DeploymentIdentity toUndeploy, URI toDeploy, DeploymentOptions deploymentOptions) throws DeploymentException {
         synchronized (this.monitor) {
             undeployInternal(toUndeploy, true, false);
         }
         return deploy(toDeploy, deploymentOptions);
     }
 
     /**
      * {@inheritDoc}
      */
     public void refreshBundle(String bundleSymbolicName, String bundleVersion) throws DeploymentException {
         DeploymentIdentity deploymentIdentity = new StandardDeploymentIdentity(BUNDLE_TYPE, bundleSymbolicName, bundleVersion);
         InstallArtifact bundleInstallArtifact;
         synchronized (this.monitor) {
             bundleInstallArtifact = this.ram.get(deploymentIdentity);
         }
         if (bundleInstallArtifact == null) {
             this.eventLogger.log(DeployerLogEvents.REFRESH_ARTEFACT_NOT_FOUND, BUNDLE_TYPE, bundleSymbolicName, bundleVersion);
             throw new DeploymentException("Refresh not possible as no " + BUNDLE_TYPE + " with name " + bundleSymbolicName + " and version "
                 + bundleVersion + " is deployed");
         }
         bundleInstallArtifact.refresh();
     }
 
     /**
      * {@inheritDoc}
      */
     public void undeploy(String symbolicName, String version) throws DeploymentException {
         // This method is deprecated and should be deleted when it is no longer used. Meanwhile, just try undeploying
         // the possible types...
         DeploymentException de = null;
         try {
             undeploy(BUNDLE_TYPE, symbolicName, version);
             return;
         } catch (DeploymentException e) {
             de = e;
         }
 
         try {
             undeploy("par", symbolicName, version);
             return;
         } catch (DeploymentException e) {
             de = e;
         }
 
         try {
             undeploy("plan", symbolicName, version);
             return;
         } catch (DeploymentException e) {
             de = e;
         }
 
         try {
             undeploy("properties", symbolicName, version);
             return;
         } catch (DeploymentException e) {
             de = e;
         }
 
         throw de;
 
     }
 
     /**
      * {@inheritDoc}
      */
     public void undeploy(String type, String symbolicName, String version) throws DeploymentException {
         DeploymentIdentity deploymentIdentity = new StandardDeploymentIdentity(type, symbolicName, version);
         synchronized (this.monitor) {
             undeployInternal(deploymentIdentity, false, false);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void undeploy(DeploymentIdentity deploymentIdentity) throws DeploymentException {
         synchronized (this.monitor) {
             undeployInternal(deploymentIdentity, false, false);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void undeploy(DeploymentIdentity deploymentIdentity, boolean deleted) throws DeploymentException {
         synchronized (this.monitor) {
             undeployInternal(deploymentIdentity, false, true);
         }
     }
 
     /**
      * All the undeploy work goes on in here -- it is assumed that any required monitors are already held by the caller.
      * <p>
      * The deleted parameter indicates whether the undeployment is a consequence of the artifact having been deleted.
      * This affects the processing of "deployer owned" artifacts which undeploy would normally delete automatically. If
      * the undeploy is a consequence of the artifact having been deleted, then undeploy must not delete the artifact
      * automatically since this may actually delete a "new" artifact which has arrived shortly after the "old" artifact
      * was deleted.
      * 
      * @param deploymentIdentity identity of artifact to undeploy
      * @param redeploying flag to indicate if we are performing a re-deploy
      * @param deleted <code>true</code> if and only if undeploy is being driven as a consequence of the artifact having
      *        been deleted
      * @throws DeploymentException
      */
     private void undeployInternal(DeploymentIdentity deploymentIdentity, boolean redeploying, boolean deleted) throws DeploymentException {
         DeploymentOptions options = this.deploymentOptionsMap.remove(deploymentIdentity);
         URI location = doUndeploy(deploymentIdentity);
         if (!redeploying) {
             deleteArtifactIfNecessary(location, options, deleted);
         }
     }
 
     private void deleteArtifactIfNecessary(URI location, DeploymentOptions options, boolean deleted) {
         if (options != null && options.getDeployerOwned() && !deleted) {
             new PathReference(location).delete(true);
         }
     }
 
     private URI doUndeploy(DeploymentIdentity deploymentIdentity) throws DeploymentException {
         synchronized (this.monitor) {
             InstallArtifact installArtifact = this.ram.get(deploymentIdentity);
             if (installArtifact == null) {
                 String type = deploymentIdentity.getType();
                 String symbolicName = deploymentIdentity.getSymbolicName();
                 String version = deploymentIdentity.getVersion();
                 this.eventLogger.log(DeployerLogEvents.UNDEPLOY_ARTEFACT_NOT_FOUND, type, symbolicName, version);
                 throw new DeploymentException("Undeploy not possible as no " + type + " with name " + symbolicName + " and version " + version
                     + " is deployed");
             } else {
                 URI location = this.ram.getLocation(deploymentIdentity);
 
                 stopArtifact(installArtifact);
                 uninstallArtifact(installArtifact);
 
                 return location;
             }
         }
     }
 
     private void stopArtifact(InstallArtifact installArtifact) throws DeploymentException {
 
         installArtifact.stop();
 
     }
 
     private void uninstallArtifact(InstallArtifact installArtifact) throws DeploymentException {
         installArtifact.uninstall();
     }
 
 }
