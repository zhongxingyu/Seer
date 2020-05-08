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
 
 package org.eclipse.virgo.apps.admin.core.state;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.virgo.apps.admin.core.BundleHolder;
 import org.eclipse.virgo.apps.admin.core.DumpLocator;
 import org.eclipse.virgo.apps.admin.core.ExportedPackageHolder;
 import org.eclipse.virgo.apps.admin.core.FailedResolutionHolder;
 import org.eclipse.virgo.apps.admin.core.ImportedPackageHolder;
 import org.eclipse.virgo.apps.admin.core.PackagesCollection;
 import org.eclipse.virgo.apps.admin.core.ServiceHolder;
 import org.eclipse.virgo.apps.admin.core.StateHolder;
 import org.eclipse.virgo.kernel.module.ModuleContextAccessor;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
 import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;
 import org.eclipse.virgo.kernel.shell.state.StateService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * <p>
  * StandardStateInspectorService is the standard implementation of {@link StateHolder}.
  * </p>
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * StandardStateInspectorService is Thread-safe
  * 
  */
 final class StandardStateHolder implements StateHolder {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(StandardStateHolder.class);
 
     private static final String LIVE = "Live";
 
    private static final Object KERNEL_REGION_NAME = "org.eclipse.equinox.region.kernel";
 
     private final StateService stateService;
 
     private final DumpLocator dumpLocator;
 
     private final ModuleContextAccessor moduleContextAccessor;
 
     private final QuasiFrameworkFactory quasiFrameworkFactory;
 
     public StandardStateHolder(StateService stateService, DumpLocator dumpLocator, ModuleContextAccessor moduleContextAccessor,
         QuasiFrameworkFactory quasiFrameworkFactory) {
         this.stateService = stateService;
         this.dumpLocator = dumpLocator;
         this.moduleContextAccessor = moduleContextAccessor;
         this.quasiFrameworkFactory = quasiFrameworkFactory;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<BundleHolder> getAllBundles(String source) {
         File dumpDirectory = null;
         if (source != null && !LIVE.equals(source)) {
             dumpDirectory = this.getDumpDirectory(source);
         }
         List<QuasiBundle> allBundles = this.stateService.getAllBundles(dumpDirectory);
         List<BundleHolder> heldBundles = new ArrayList<BundleHolder>();
         for (QuasiBundle quasiBundle : allBundles) {
             if (quasiBundle != null && (!KERNEL_REGION_NAME.equals(this.stateService.getBundleRegionName(quasiBundle.getBundleId())) ||  0l == quasiBundle.getBundleId())) {
                 heldBundles.add(new StandardBundleHolder(quasiBundle, this.moduleContextAccessor, this.stateService));
             }
         }
         return heldBundles;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<ServiceHolder> getAllServices(String source) {
         File dumpDirectory = null;
         List<ServiceHolder> serviceHolders = new ArrayList<ServiceHolder>();
         if (source != null && !LIVE.equals(source)) {
             dumpDirectory = this.getDumpDirectory(source);
         }
         List<QuasiLiveService> allServices = this.stateService.getAllServices(dumpDirectory);
         for (QuasiLiveService quasiLiveService : allServices) {
             serviceHolders.add(new StandardServiceHolder(quasiLiveService, moduleContextAccessor, this.stateService));
         }
         return serviceHolders;
     }
 
     /**
      * {@inheritDoc}
      */
     public BundleHolder getBundle(String source, long bundleId) {
         File dumpDirectory = null;
         if (source != null && !LIVE.equals(source)) {
             dumpDirectory = this.getDumpDirectory(source);
         }
 
         QuasiBundle bundle = stateService.getBundle(dumpDirectory, bundleId);
         if (bundle != null) {
             return new StandardBundleHolder(bundle, this.moduleContextAccessor, this.stateService);
         }
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public BundleHolder getBundle(String source, String name, String version, String region) {
         File dumpDirectory = null;
         if (source != null && !LIVE.equals(source)) {
             dumpDirectory = this.getDumpDirectory(source);
         }
         QuasiBundle result = null;
         if (name != null && version != null) {
             List<QuasiBundle> allBundles = this.stateService.getAllBundles(dumpDirectory);
             for (QuasiBundle quasiBundle : allBundles) {
                 if (quasiBundle.getSymbolicName().equals(name) && quasiBundle.getVersion().toString().equals(version) && this.stateService.getBundleRegionName(quasiBundle.getBundleId()).equals(region)) {
                     result = quasiBundle;
                 }
             }
         }
         if (result != null) {
             return new StandardBundleHolder(result, this.moduleContextAccessor, this.stateService);
         }
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public ServiceHolder getService(String source, long serviceId) {
         File dumpDirectory = null;
         if (source != null && !LIVE.equals(source)) {
             dumpDirectory = this.getDumpDirectory(source);
         }
         QuasiLiveService service = this.stateService.getService(dumpDirectory, serviceId);
         if (service != null) {
             return new StandardServiceHolder(service, this.moduleContextAccessor, this.stateService);
         }
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public PackagesCollection getPackages(String source, String packageName) {
         QuasiFramework quasiFramework = this.getQuasiFramework(source);
         List<QuasiBundle> bundles = quasiFramework.getBundles();
 
         List<ImportedPackageHolder> importedPackageHolders = new ArrayList<ImportedPackageHolder>();
         List<ExportedPackageHolder> exportedPackageHolders = new ArrayList<ExportedPackageHolder>();
 
         for (QuasiBundle qBundle : bundles) {
             ImportedPackageHolder importPackage = processImporters(qBundle, packageName);
             if (importPackage != null) {
                 importedPackageHolders.add(importPackage);
             }
             ExportedPackageHolder exportPackage = processExporters(qBundle, packageName);
             if (exportPackage != null) {
                 exportedPackageHolders.add(exportPackage);
             }
         }
         return new StandardPackagesCollection(packageName, importedPackageHolders, exportedPackageHolders);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FailedResolutionHolder> getResolverReport(String source, long bundleId) {
         File dumpDirectory = null;
         if (source != null && !LIVE.equals(source)) {
             dumpDirectory = this.getDumpDirectory(source);
         }
         List<QuasiResolutionFailure> resolverReport = this.stateService.getResolverReport(dumpDirectory, bundleId);
         List<FailedResolutionHolder> failedResolutionHolders = new ArrayList<FailedResolutionHolder>();
         if (resolverReport != null) {
             for (QuasiResolutionFailure quasiResolutionFailure : resolverReport) {
                 failedResolutionHolders.add(new StandardFailedResolutionHolder(quasiResolutionFailure, this.moduleContextAccessor, this.stateService));
             }
         }
         return failedResolutionHolders;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<BundleHolder> search(String source, String term) {
         File dumpDirectory = null;
         if (source != null && !LIVE.equals(source)) {
             dumpDirectory = this.getDumpDirectory(source);
         }
         List<QuasiBundle> matchingBundles = this.stateService.search(dumpDirectory, term);
         List<BundleHolder> heldMatchingBundles = new ArrayList<BundleHolder>();
         for (QuasiBundle quasiBundle : matchingBundles) {
             if (quasiBundle != null) {
                 heldMatchingBundles.add(new StandardBundleHolder(quasiBundle, this.moduleContextAccessor, this.stateService));
             }
         }
         return heldMatchingBundles;
     }
 
     private File getDumpDirectory(String source) {
         File dumpDirectory = null;
         try {
             dumpDirectory = this.dumpLocator.getDumpDir(source);
         } catch (Exception e) {
             LOGGER.warn(String.format("Unable to obtain the dump directory '%s'", source), e);
         }
         return dumpDirectory;
     }
 
     private QuasiFramework getQuasiFramework(String source) {
         File dumpDirectory = getDumpDirectory(source);
         if (dumpDirectory != null) {
             try {
                 return this.quasiFrameworkFactory.create(dumpDirectory);
             } catch (Exception e) {
                 throw new RuntimeException("Failed to create quasi-framework", e);
             }
         } else {
             return this.quasiFrameworkFactory.create();
         }
     }
 
     private ImportedPackageHolder processImporters(QuasiBundle qBundle, String packageName) {
         for (QuasiImportPackage qImportPackage : qBundle.getImportPackages()) {
             if (qImportPackage.getPackageName().equals(packageName)) {
                 return new StandardImportedPackageHolder(qImportPackage, this.moduleContextAccessor, this.stateService);
             }
         }
         return null;
     }
 
     private ExportedPackageHolder processExporters(QuasiBundle qBundle, String packageName) {
         for (QuasiExportPackage qExportPackage : qBundle.getExportPackages()) {
             if (qExportPackage.getPackageName().equals(packageName)) {
                 return new StandardExportedPackageHolder(qExportPackage, this.moduleContextAccessor, this.stateService);
             }
         }
         return null;
     }
 
 }
