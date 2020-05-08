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
 
 package org.eclipse.virgo.kernel.install.artifact.internal;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.osgi.framework.Version;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 

 import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
 import org.eclipse.virgo.kernel.core.Signal;
 import org.eclipse.virgo.kernel.deployer.core.DeployerLogEvents;
 import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
 import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
 import org.eclipse.virgo.kernel.install.artifact.ArtifactState;
 import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
 import org.eclipse.virgo.kernel.serviceability.NonNull;
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 import org.eclipse.virgo.util.common.Tree;
 
 /**
  * {@link AbstractInstallArtifact} is a base class for implementations of {@link InstallArtifact}.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * This class is thread safe.
  * 
  */
 public abstract class AbstractInstallArtifact implements InstallArtifact {
 
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
     private final Object monitor = new Object();
 
     private final ArtifactIdentity identity;
 
     protected final ArtifactStorage artifactStorage;
 
     private final Map<String, String> properties = new ConcurrentHashMap<String, String>();
 
     private final Map<String, String> deploymentProperties = new ConcurrentHashMap<String, String>();
 
     private final ArtifactStateMonitor artifactStateMonitor;
 
     private final String repositoryName;
 
     protected final EventLogger eventLogger;
 
     private Tree<InstallArtifact> tree;
 
     private volatile boolean isRefreshing;
 
     /**
      * Construct an {@link AbstractInstallArtifact} from the given type, name, version, {@link ArtifactFS}, and
      * {@link ArtifactState}, none of which may be null.
      * 
      * @param type a non-<code>null</code> artifact type
      * @param name a non-<code>null</code> artifact name
      * @param version a non-<code>null</code> artifact {@link Version}
      * @param artifactFS a non-<code>null</code> <code>ArtifactFS</code>
      * @param repositoryName the name of the source repository, or <code>null</code> if the artifact is not from a
      *        repository
      */
     protected AbstractInstallArtifact(@NonNull ArtifactIdentity identity, @NonNull ArtifactStorage artifactStorage,
         @NonNull ArtifactStateMonitor artifactStateMonitor, String repositoryName, EventLogger eventLogger) {
         this.identity = identity;
         this.artifactStorage = artifactStorage;
         this.artifactStateMonitor = artifactStateMonitor;
         this.repositoryName = repositoryName;
         this.eventLogger = eventLogger;
         this.isRefreshing = false;
     }
 
     final ArtifactIdentity getIdentity() {
         return this.identity;
     }
 
     public final boolean isRefreshing() {
         return this.isRefreshing;
     }
 
     public void beginInstall() throws DeploymentException {
         try {
             this.artifactStateMonitor.onInstalling(this);
         } catch (DeploymentException de) {
             failInstall();
             throw de;
         }
        
     }
 
     public void failInstall() throws DeploymentException {
         this.artifactStateMonitor.onInstallFailed(this);
     }
 
     public void endInstall() throws DeploymentException {
         this.artifactStateMonitor.onInstalled(this);
     }
 
     public void beginResolve() throws DeploymentException {
         pushThreadContext();
         try {
             this.artifactStateMonitor.onResolving(this);
         } finally {
             popThreadContext();
         }
     }
 
     public void failResolve() throws DeploymentException {
         pushThreadContext();
         try {
             this.artifactStateMonitor.onResolveFailed(this);
         } finally {
             popThreadContext();
         }
     }
 
     public void endResolve() throws DeploymentException {
         pushThreadContext();
         try {
             this.artifactStateMonitor.onResolved(this);
         } finally {
             popThreadContext();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public final String getType() {
         return this.identity.getType();
     }
 
     /**
      * {@inheritDoc}
      */
     public final String getName() {
         return this.identity.getName();
     }
 
     /**
      * {@inheritDoc}
      */
     public final Version getVersion() {
         return this.identity.getVersion();
     }
 
     /**
      * {@inheritDoc}
      */
     public final String getScopeName() {
         return this.identity.getScopeName();
     }
 
     /**
      * {@inheritDoc}
      */
     public State getState() {
         return this.artifactStateMonitor.getState();
     }
 
     /**
      * {@inheritDoc}
      */
     public void start() throws DeploymentException {
         start(null);
     }
 
     /**
      * {@inheritDoc}
      */
     public void start(Signal signal) throws DeploymentException {
         pushThreadContext();
         try {
             boolean stateChanged = this.artifactStateMonitor.onStarting(this);
             if (stateChanged || signal != null) {
                 driveDoStart(signal);
             }
         } finally {
             popThreadContext();
         }
     }
 
     protected final void driveDoStart(Signal signal) throws DeploymentException {
         Signal stateMonitorSignal = createStateMonitorSignal(signal);
         doStart(stateMonitorSignal);
     }
 
     protected Signal createStateMonitorSignal(Signal signal) {
         return new StateMonitorSignal(signal);
     }
 
     private final class StateMonitorSignal implements Signal {
 
         private final Signal signal;
 
         public StateMonitorSignal(Signal signal) {
             this.signal = signal;
         }
 
         /**
          * {@inheritDoc}
          */
         public void signalSuccessfulCompletion() {
             try {
                 asyncStartSucceeded();
                 AbstractInstallArtifact.signalSuccessfulCompletion(this.signal);
             } catch (DeploymentException de) {
               signalFailure(de);
             }
         }
 
         /**
          * {@inheritDoc}
          */
         public void signalFailure(Throwable cause) {
             asyncStartFailed(cause);
             handleFailure(cause);
         }
 
         private void handleFailure(Throwable cause) {
             try {
                 stop();
             } catch (DeploymentException de) {
                 AbstractInstallArtifact.this.logger.error("Stop failed", de);
             }
             AbstractInstallArtifact.signalFailure(this.signal, cause);
         }
 
     }
 
     protected static void signalSuccessfulCompletion(Signal signal) {
         if (signal != null) {
             signal.signalSuccessfulCompletion();
         }
     }
 
     protected static void signalFailure(Signal signal, Throwable e) {
         if (signal != null) {
             signal.signalFailure(e);
         }
     }
 
     /**
      * Perform the actual start of this {@link InstallArtifact} and drive the given {@link Signal} on successful or
      * unsuccessful completion.
      * 
      * @param signal the <code>Signal</code> to be driven
      * @throws DeploymentException if the start fails synchronously
      */
     protected abstract void doStart(Signal signal) throws DeploymentException;
 
     private final void asyncStartSucceeded() throws DeploymentException {
         pushThreadContext();
         try {
             this.artifactStateMonitor.onStarted(this);
         } finally {
             popThreadContext();
         }
     }
 
     private final void asyncStartFailed(Throwable cause) {
         pushThreadContext();
         try {
             this.artifactStateMonitor.onStartFailed(this, cause);
         } catch (DeploymentException e) {
             logger.error(String.format("listener for %s threw DeploymentException", this), e);
         } finally {
             popThreadContext();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void stop() throws DeploymentException {
         if (this.getState().equals(State.ACTIVE)) {
             pushThreadContext();
             try {
                 this.artifactStateMonitor.onStopping(this);
                 try {
                     doStop();
                     this.artifactStateMonitor.onStopped(this);
                 } catch (DeploymentException e) {
                     this.artifactStateMonitor.onStopFailed(this, e);
                 }
             } finally {
                 popThreadContext();
             }
         }
     }
 
     /**
      * @see stop
      */
     protected abstract void doStop() throws DeploymentException;
 
     /**
      * {@inheritDoc}
      */
     public void uninstall() throws DeploymentException {
         if (getState().equals(State.STARTING) || getState().equals(State.ACTIVE) || getState().equals(State.RESOLVED)
             || getState().equals(State.INSTALLED)) {
             pushThreadContext();
             try {
                 if (getState().equals(State.ACTIVE) || getState().equals(State.STARTING)) {
                     stop();
                 }
                 this.artifactStateMonitor.onUninstalling(this);
                 try {
                     doUninstall();
                     this.artifactStateMonitor.onUninstalled(this);
                 } catch (DeploymentException e) {
                     this.artifactStateMonitor.onUninstallFailed(this, e);
                 }
             } finally {
                 this.artifactStorage.delete();
                 popThreadContext();
             }
         }
     }
 
     /**
      * @see uninstall
      */
     protected abstract void doUninstall() throws DeploymentException;
 
     /**
      * {@inheritDoc}
      */
     public final ArtifactFS getArtifactFS() {
         return this.artifactStorage.getArtifactFS();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         return this.identity.toString();
     }
 
     /**
      * Push the thread context including any application trace name and thread context class loader. The caller is
      * responsible for calling <code>popThreadContext</code>.
      */
     public void pushThreadContext() {
         // There is no default thread context. Subclasses must override to provide one.
     }
 
     /**
      * Pop a previously pushed thread context.
      */
     public void popThreadContext() {
         // There is no default thread context. Subclasses must override to provide one.
     }
 
     protected final ArtifactStateMonitor getStateMonitor() {
         return this.artifactStateMonitor;
     }
 
     /**
      * @return false
      */
     public boolean refresh() throws DeploymentException {
         try {
             this.isRefreshing = true;
             this.eventLogger.log(DeployerLogEvents.REFRESHING, getType(), getName(), getVersion());
             this.artifactStorage.synchronize();
 
             boolean refreshed = doRefresh();
 
             if (refreshed) {
                 this.eventLogger.log(DeployerLogEvents.REFRESHED, getType(), getName(), getVersion());
             } else {
                this.artifactStorage.rollBack();
                this.eventLogger.log(DeployerLogEvents.REFRESH_FAILED, getType(), getName(), getVersion());
             }
 
             return refreshed;
         } catch (DeploymentException de) {
            this.eventLogger.log(DeployerLogEvents.REFRESH_FAILED, de, getType(), getName(), getVersion());
             throw de;
         } finally {
             this.isRefreshing = false;
         }
     }
 
     protected abstract boolean doRefresh() throws DeploymentException;
 
     /**
      * {@inheritDoc}
      */
     public final String getProperty(@NonNull String name) {
         return this.properties.get(name);
     }
 
     /**
      * {@inheritDoc}
      */
     public final Set<String> getPropertyNames() {
         HashSet<String> propertyNames = new HashSet<String>(this.properties.keySet());
         return Collections.unmodifiableSet(propertyNames);
     }
 
     /**
      * {@inheritDoc}
      */
     public final String setProperty(String name, String value) {
         return this.properties.put(name, value);
     }
 
     public Map<String, String> getDeploymentProperties() {
         return this.deploymentProperties;
     }
 
     /**
      * {@inheritDoc}
      */
     public final String getRepositoryName() {
         return this.repositoryName;
     }
 
     /**
      * @param tree to set
      * @throws DeploymentException possible from overriding methods
      */
     public void setTree(Tree<InstallArtifact> tree) throws DeploymentException {
         synchronized (this.monitor) {
             this.tree = tree;
         }
     }
 
     public final Tree<InstallArtifact> getTree() {
         synchronized (this.monitor) {
             return this.tree;
         }
     }
 
 }
