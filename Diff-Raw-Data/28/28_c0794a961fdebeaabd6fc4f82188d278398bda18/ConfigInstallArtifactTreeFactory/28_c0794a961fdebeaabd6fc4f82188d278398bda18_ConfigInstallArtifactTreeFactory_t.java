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
 
 import java.util.Map;
 
 import org.eclipse.virgo.kernel.deployer.config.ConfigurationDeployer;
 import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
 import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
 import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
 import org.eclipse.virgo.kernel.install.artifact.ArtifactStorage;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifactTreeFactory;
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 import org.eclipse.virgo.util.common.ThreadSafeArrayListTree;
 import org.eclipse.virgo.util.common.Tree;
 import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
 
 /**
  * {@link ConfigInstallArtifactTreeFactory} is an {@link InstallArtifactTreeFactory} for configuration properties file
  * {@link InstallArtifact InstallArtifacts}.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * This class is thread safe.
  * 
  */
 final class ConfigInstallArtifactTreeFactory implements InstallArtifactTreeFactory {
 
     private static final String PROPERTIES_TYPE = ArtifactIdentityDeterminer.CONFIGURATION_TYPE;
 
     private final BundleContext bundleContext;
 
     private final ConfigLifecycleEngine lifecycleEngine;
 
     private final EventLogger eventLogger;
    
    private final Object monitor = new Object();
 
    private ConfigurationDeployer configurationDeployer;
 
    ConfigInstallArtifactTreeFactory(BundleContext bundleContext, EventLogger eventLogger) {
         this.bundleContext = bundleContext;
         this.lifecycleEngine = new ConfigLifecycleEngine(bundleContext);
         this.eventLogger = eventLogger;
     }
 
     /**
      * {@inheritDoc}
      */
     public Tree<InstallArtifact> constructInstallArtifactTree(ArtifactIdentity artifactIdentity, ArtifactStorage artifactStorage,
         Map<String, String> deploymentProperties, String repositoryName) throws DeploymentException {
         if (PROPERTIES_TYPE.equalsIgnoreCase(artifactIdentity.getType())) {
            ConfigurationDeployer configDeployer = obtainConfigurationDeployer();
             ArtifactStateMonitor artifactStateMonitor = new StandardArtifactStateMonitor(this.bundleContext);
             InstallArtifact configInstallArtifact = new StandardConfigInstallArtifact(artifactIdentity, artifactStorage, this.lifecycleEngine,
                this.lifecycleEngine, this.lifecycleEngine, artifactStateMonitor, repositoryName, eventLogger, configDeployer);
             return constructInstallTree(configInstallArtifact);
         } else {
             return null;
         }
     }
 
    private ConfigurationDeployer obtainConfigurationDeployer() {
        synchronized (this.monitor) {
            if (this.configurationDeployer == null) {
                ServiceReference<ConfigurationDeployer> serviceReference = this.bundleContext.getServiceReference(ConfigurationDeployer.class);
                this.configurationDeployer = this.bundleContext.getService(serviceReference);
            }
            return this.configurationDeployer;
        }
    }

     private Tree<InstallArtifact> constructInstallTree(InstallArtifact rootArtifact) {
         return new ThreadSafeArrayListTree<InstallArtifact>(rootArtifact);
     }
 }
