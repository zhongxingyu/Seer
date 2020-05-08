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
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.jar.JarFile;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Version;
 
 
 import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
 import org.eclipse.virgo.kernel.artifact.fs.ArtifactFSEntry;
 import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
 import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
 import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
 import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
 import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.internal.ArtifactStateMonitor;
 import org.eclipse.virgo.kernel.install.artifact.internal.StandardArtifactStateMonitor;
 import org.eclipse.virgo.kernel.install.artifact.internal.InstallArtifactRefreshHandler;
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 import org.eclipse.virgo.util.io.IOUtils;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
 
 /**
  * A factory for creating {@link BundleInstallArtifact} instances.
  * 
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Thread-safe.
  * 
  */
 final class BundleInstallArtifactFactory {
 
     private static final Version DEFAULT_BUNDLE_VERSION = Version.emptyVersion;
 
     private final BundleContext kernelBundleContext;
 
     private final InstallArtifactRefreshHandler refreshHandler;
 
     private final BundleDriverFactory bundleDriverFactory;
     
     private final EventLogger eventLogger;
     
     private final ArtifactIdentityDeterminer identityDeterminer;
 
     BundleInstallArtifactFactory(BundleContext kernelBundleContext, InstallArtifactRefreshHandler refreshHandler,
         BundleDriverFactory bundleDriverFactory, EventLogger eventLogger, ArtifactIdentityDeterminer identityDeterminer) {
         this.kernelBundleContext = kernelBundleContext;
         this.refreshHandler = refreshHandler;
         this.bundleDriverFactory = bundleDriverFactory;
         this.eventLogger = eventLogger;
         this.identityDeterminer = identityDeterminer;
     }
 
     BundleInstallArtifact createBundleInstallArtifact(ArtifactIdentity identity, ArtifactStorage artifactStorage, String repositoryName) throws DeploymentException {
 
         ArtifactStateMonitor artifactStateMonitor = new StandardArtifactStateMonitor(this.kernelBundleContext);
 
         StandardBundleDriver bundleDriver = this.bundleDriverFactory.createBundleDriver(identity, artifactStateMonitor);
 
         BundleManifest bundleManifest = retrieveArtifactFSManifest(artifactStorage.getArtifactFS());        
 
         StandardBundleInstallArtifact bundleInstallArtifact = new StandardBundleInstallArtifact(identity, bundleManifest, artifactStorage, bundleDriver, artifactStateMonitor, this.refreshHandler, repositoryName, this.eventLogger, this.identityDeterminer);
 
         bundleDriver.setInstallArtifact(bundleInstallArtifact);
 
         return bundleInstallArtifact;
     }
 
     private BundleManifest retrieveArtifactFSManifest(ArtifactFS artifactFS) throws DeploymentException {
         ArtifactFSEntry manifestEntry = artifactFS.getEntry(JarFile.MANIFEST_NAME);
         if (manifestEntry != null && manifestEntry.exists()) {
             try {
                 Reader manifestReader = new InputStreamReader(manifestEntry.getInputStream());
                 try {
                     return BundleManifestFactory.createBundleManifest(manifestReader);
                 } finally {
                     IOUtils.closeQuietly(manifestReader);
                 }
             } catch (IOException ioe) {
                 throw new DeploymentException("Failed to read manifest for bundle from " + artifactFS, ioe);
             }
         } else {
             return BundleManifestFactory.createBundleManifest();
         }
     }
 
     static Version getVersionFromManifest(BundleManifest bundleManifest) {
         Version version = bundleManifest.getBundleVersion();
         return (version == null ? DEFAULT_BUNDLE_VERSION : version);
     }
 
     private static String getNameFromManifest(BundleManifest bundleManifest) {
         return bundleManifest.getBundleSymbolicName().getSymbolicName();
     }
 
     static String determineName(BundleManifest bundleManifest, ArtifactFS artifactFS) {
         String name = getNameFromManifest(bundleManifest);
         if (name == null) {
             name = artifactFS.getFile().getName();
         }
         return name;
     }
 }
