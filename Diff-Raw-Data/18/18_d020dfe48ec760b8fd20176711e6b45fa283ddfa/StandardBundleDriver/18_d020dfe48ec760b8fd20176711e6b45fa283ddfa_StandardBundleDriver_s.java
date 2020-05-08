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
 
 package org.eclipse.virgo.kernel.install.artifact.internal.bundle;
 
 import org.eclipse.virgo.kernel.core.AbortableSignal;
 import org.eclipse.virgo.kernel.core.BundleStarter;
 import org.eclipse.virgo.kernel.core.BundleUtils;
 import org.eclipse.virgo.kernel.core.KernelException;
 import org.eclipse.virgo.kernel.core.Signal;
 import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
 import org.eclipse.virgo.kernel.install.artifact.ArtifactState;
 import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.internal.ArtifactStateMonitor;
 import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;
 import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
 import org.eclipse.virgo.kernel.serviceability.Assert;
 import org.eclipse.virgo.kernel.shim.serviceability.TracingService;
 import org.eclipse.virgo.util.common.Tree;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.BundleListener;
 
 /**
  * {@link StandardBundleDriver} monitors the state of a bundle and keeps the associated {@link ArtifactState} up to
  * date.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * This class is thread safe.
  * 
  */
 final class StandardBundleDriver implements BundleDriver {
 
     private static final String SYNTHETIC_CONTEXT_SUFFIX = "-synthetic.context";
 
     private final Object monitor = new Object();
 
     private final BundleStarter bundleStarter;
 
     private final TracingService tracingService;
 
     private final PackageAdminUtil packageAdminUtil;
 
     private final BundleContext bundleContext;
 
     private final OsgiFramework osgi;
 
     private final ArtifactStateMonitor artifactStateMonitor;
 
     private volatile BundleThreadContextManager threadContextManager;
 
     private volatile StandardBundleInstallArtifact installArtifact;
 
     private volatile BundleDriverBundleListener bundleListener;
 
     private Bundle bundle;
 
     private final String applicationTraceName;
 
     /**
      * Creates a {@link StandardBundleDriver} for the given {@link Bundle} and {@link ArtifactState}.
      * 
      * @param osgiFramework framework
      * @param bundleContext context
      * @param bundleStarter to start bundles
      * @param tracingService to trace bundle operations
      * @param packageAdminUtil utilities for package administration
      */
     StandardBundleDriver(OsgiFramework osgiFramework, BundleContext bundleContext, BundleStarter bundleStarter, TracingService tracingService,
         PackageAdminUtil packageAdminUtil, String scopeName, ArtifactStateMonitor artifactStateMonitor) {
         this.osgi = osgiFramework;
         this.bundleContext = bundleContext;
         this.tracingService = tracingService;
         this.packageAdminUtil = packageAdminUtil;
         this.bundleStarter = bundleStarter;
         this.applicationTraceName = scopeName;
         this.artifactStateMonitor = artifactStateMonitor;
     }
 
     public void setInstallArtifact(StandardBundleInstallArtifact installArtifact) {
         this.installArtifact = installArtifact;
     }
 
     public void setBundle(Bundle bundle) {
         BundleListener bundleListener = null;
 
         synchronized (this.monitor) {
            if (getThreadContextBundle() == null) {
                 this.bundle = bundle;
                if (getThreadContextBundle() != null) {
                    this.bundleListener = new BundleDriverBundleListener(this.installArtifact, getThreadContextBundle(), this.artifactStateMonitor);
                     bundleListener = this.bundleListener;
                 }
             }
         }
 
         if (bundleListener != null) {
             this.bundleContext.addBundleListener(bundleListener);
         }
     }
 
     public void syncStart() throws KernelException {
         pushThreadContext();
         try {
             this.bundleStarter.start(obtainLocalBundle(), null);
         } catch (BundleException be) {
             throw new KernelException("BundleException", be);
         } finally {
             popThreadContext();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void pushThreadContext() {
         ensureThreadContextManager();
         this.threadContextManager.pushThreadContext();
     }
 
     public void popThreadContext() {
         this.threadContextManager.popThreadContext();
     }
 
     private void ensureThreadContextManager() {
         synchronized (this.monitor) {
             if (this.threadContextManager == null) {
                 this.threadContextManager = new BundleThreadContextManager(this.osgi, getThreadContextBundle(), this.applicationTraceName,
                     this.tracingService);
             }
         }
     }
 
     private Bundle getThreadContextBundle() {
         if (this.installArtifact != null) {
             Tree<InstallArtifact> tree = this.installArtifact.getTree();
             Tree<InstallArtifact> parent = tree.getParent();
             while (parent != null) {
                 InstallArtifact parentArtifact = parent.getValue();
                 if (parentArtifact instanceof PlanInstallArtifact) {
                     PlanInstallArtifact parentPlan = (PlanInstallArtifact) (parentArtifact);
                     if (parentPlan.isScoped()) {
                        String syntheticContextBundleSymbolicName = parentPlan.getScopeName() + SYNTHETIC_CONTEXT_SUFFIX;
                         for (Tree<InstallArtifact> scopedPlanChild : parent.getChildren()) {
                             InstallArtifact scopedPlanChildArtifact = scopedPlanChild.getValue();
                             if (scopedPlanChildArtifact instanceof BundleInstallArtifact
                                 && syntheticContextBundleSymbolicName.equals(scopedPlanChildArtifact.getName())) {
                                 BundleInstallArtifact syntheticContextBundleArtifact = (BundleInstallArtifact) scopedPlanChildArtifact;
                                 return syntheticContextBundleArtifact.getBundle();
                             }
                         }
                     }
                 }
                 parent = parent.getParent();
             }
         }
         return this.bundle;
     }
 
     /**
      * {@inheritDoc}
      */
     public void start(AbortableSignal signal) throws DeploymentException {
         Bundle bundle = obtainLocalBundle();
 
         if (!BundleUtils.isFragmentBundle(bundle)) {
             pushThreadContext();
             try {
                 startBundle(bundle, signal);
             } catch (DeploymentException e) {
                 signalFailure(signal, e);
                 throw e;
             } catch (RuntimeException e) {
                 signalFailure(signal, e);
                 throw e;
             } finally {
                 popThreadContext();
             }
         } else {
             signalSuccessfulCompletion(signal);
         }
 
     }
 
     private void startBundle(Bundle bundle, AbortableSignal signal) throws DeploymentException {
         this.bundleListener.addSolicitedStart(bundle);
         try {
             this.bundleStarter.start(bundle, signal);
         } catch (BundleException e) {
             throw new DeploymentException("BundleException", e);
         } finally {
             this.bundleListener.removeSolicitedStart(bundle);
         }
     }
 
     protected static void signalFailure(Signal signal, Throwable e) {
         if (signal != null) {
             signal.signalFailure(e);
         }
     }
 
     private static void signalSuccessfulCompletion(Signal signal) {
         if (signal != null) {
             signal.signalSuccessfulCompletion();
         }
     }
 
     public void syncStart(int options) throws KernelException {
         Bundle bundle = obtainLocalBundle();
 
         if (!BundleUtils.isFragmentBundle(bundle)) {
             pushThreadContext();
             try {
                 this.bundleStarter.start(obtainLocalBundle(), options, null);
             } catch (BundleException be) {
                 throw new KernelException("BundleException", be);
             } finally {
                 popThreadContext();
             }
         }
     }
 
     private Bundle obtainLocalBundle() {
         synchronized (this.monitor) {
            if (getThreadContextBundle() == null) {
                 throw new IllegalStateException("bundle not set");
             }
            return getThreadContextBundle();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean update(BundleManifest bundleManifest) throws DeploymentException {
         updateBundle(bundleManifest);
         refreshBundle();
         return true;
     }
 
     private void updateBundle(BundleManifest bundleManifest) throws DeploymentException {
         if (!isFragment(bundleManifest)) {
             Bundle bundle = obtainLocalBundle();
             Assert.isTrue(bundle.getState() == Bundle.INSTALLED || bundle.getState() == Bundle.RESOLVED,
                 "A bundle cannot be updated unless is in INSTALLED or RESOLVED state");
             try {
                 this.osgi.update(bundle, new BundleDriverManifestTransformer(bundleManifest));
             } catch (BundleException e) {
                 throw new DeploymentException("Failed to update bundle '" + bundle + "'.", e);
             }
         }
     }
 
     private static boolean isFragment(BundleManifest bundleManifest) {
         return bundleManifest.getFragmentHost().getBundleSymbolicName() != null;
     }
 
     /**
      * {@inheritDoc}
      */
     public void refreshBundle() throws DeploymentException {
         Bundle bundle = obtainLocalBundle();
         this.packageAdminUtil.synchronouslyRefreshPackages(new Bundle[] { bundle });
     }
 
     /**
      * {@inheritDoc}
      */
     public void stop() throws DeploymentException {
         pushThreadContext();
         try {
             obtainLocalBundle().stop();
         } catch (BundleException e) {
             throw new DeploymentException("stop failed", e);
         } finally {
             popThreadContext();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void uninstall() throws DeploymentException {
         Bundle bundle = obtainLocalBundle();
 
         pushThreadContext();
         try {
             bundle.uninstall();
         } catch (BundleException e) {
             throw new DeploymentException("uninstall failed", e);
         } finally {
             popThreadContext();
         }
 
         BundleListener localBundleListener = this.bundleListener;
         this.bundleListener = null;
 
         if (localBundleListener != null) {
             this.bundleContext.removeBundleListener(localBundleListener);
         }
 
         this.packageAdminUtil.synchronouslyRefreshPackages(new Bundle[] { bundle });
     }
 
     /**
      * {@inheritDoc}
      */
     public void trackStart(AbortableSignal signal) {
         this.bundleStarter.trackStart(obtainLocalBundle(), signal);
     }
 }
