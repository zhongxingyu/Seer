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
 
 package org.eclipse.virgo.kernel.userregion.internal.quasi;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.equinox.region.Region;
 import org.eclipse.equinox.region.RegionDigraph;
 import org.eclipse.equinox.region.RegionFilter;
 import org.eclipse.osgi.service.resolver.BundleDescription;
 import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
 import org.eclipse.osgi.service.resolver.PlatformAdmin;
 import org.eclipse.osgi.service.resolver.ResolverError;
 import org.eclipse.osgi.service.resolver.State;
 import org.eclipse.osgi.service.resolver.StateHelper;
 import org.eclipse.osgi.service.resolver.StateObjectFactory;
 import org.eclipse.osgi.service.resolver.VersionConstraint;
 import org.eclipse.virgo.kernel.core.FatalKernelException;
 import org.eclipse.virgo.kernel.osgi.framework.ManifestTransformer;
 import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyDependenciesException;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
 import org.eclipse.virgo.kernel.userregion.internal.equinox.TransformedManifestProvidingBundleFileWrapper;
 import org.eclipse.virgo.kernel.userregion.internal.quasi.ResolutionFailureDetective.ResolverErrorsHolder;
 import org.eclipse.virgo.repository.Repository;
 import org.eclipse.virgo.util.common.StringUtils;
 import org.eclipse.virgo.util.osgi.manifest.VersionRange;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Version;
 import org.osgi.framework.hooks.resolver.ResolverHookFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * {@link StandardQuasiFramework} is the default implementation of {@link QuasiFramework}.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * This class is thread safe.
  * 
  */
 final class StandardQuasiFramework implements QuasiFramework {
 
     private final RegionFilter TOP;
 
     private static final String REGION_LOCATION_DELIMITER = "@";
 
     private static final String COREGION_SUFFIX = ".coregion";
 
     private static final String REFERENCE_SCHEME = "reference:";
 
     private static final String FILE_SCHEME = "file:";
 
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
     private final Object monitor = new Object();
 
     private final State state;
 
     private final StateObjectFactory stateObjectFactory;
 
     /*
      * Track the bundles which are explicitly installed. These are input to the resolve method.
      */
     private final List<StandardQuasiBundle> installedQuasiBundles = new ArrayList<StandardQuasiBundle>();
 
     private volatile BundleDescription[] otherBundles;
 
     private final ResolutionFailureDetective detective;
 
     private final BundleContext bundleContext;
 
     private final DependencyCalculator dependencyCalculator;
 
     private final StateHelper stateHelper;
 
     private final TransformedManifestProvidingBundleFileWrapper bundleTransformationHandler;
 
     private final RegionDigraph regionDigraph;
 
     private Region coregion;
 
     private final Region userRegion;
 
     StandardQuasiFramework(BundleContext bundleContext, State state, PlatformAdmin platformAdmin, ResolutionFailureDetective detective,
         Repository repository, TransformedManifestProvidingBundleFileWrapper bundleTransformationHandler, RegionDigraph regionDigraph) {
         TOP = regionDigraph.createRegionFilterBuilder().allowAll(RegionFilter.VISIBLE_ALL_NAMESPACE).build();
         this.bundleContext = bundleContext;
         this.state = state;
         this.stateObjectFactory = platformAdmin.getFactory();
         this.detective = detective;
         this.stateHelper = platformAdmin.getStateHelper();
         this.bundleTransformationHandler = bundleTransformationHandler;
         this.regionDigraph = regionDigraph;
         this.userRegion = regionDigraph.getRegion("org.eclipse.virgo.region.user");
         this.coregion = regionDigraph.getRegion(this.userRegion.getName() + COREGION_SUFFIX);
         setResolverHookFactory();
 
         this.dependencyCalculator = new DependencyCalculator(platformAdmin.getFactory(), this.detective, repository, this.bundleContext);
     }
 
     private void setResolverHookFactory() {
         /*
          * Create a resolver hook factory for the region digraph. If the region digraph is live, this will create a hook
          * factory equivalent to the live hook factory. If the region digraph is disconnected (a reconstituted copy of a
          * live region digraph), this will produce a hook factory independent of the live hook factory.
          */
         ResolverHookFactory resolverHookFactory = this.regionDigraph.getResolverHookFactory();
         this.state.setResolverHookFactory(resolverHookFactory);
     }
 
     /**
      * {@inheritDoc}
      */
     public QuasiBundle install(URI location, BundleManifest bundleManifest) throws BundleException {
         synchronized (this.monitor) {
             createCoregionIfNecessary();
             StandardQuasiBundle qb = doInstall(location, bundleManifest);
             this.installedQuasiBundles.add(qb);
             return qb;
         }
     }
 
     private void createCoregionIfNecessary() {
         synchronized (this.monitor) {
             if (this.coregion == null) {
                 try {
                     this.coregion = this.regionDigraph.createRegion(this.userRegion.getName() + COREGION_SUFFIX);
                     this.userRegion.connectRegion(this.coregion, TOP);
                     this.coregion.connectRegion(this.userRegion, TOP);
                 } catch (BundleException e) {
                     // should never happen
                     throw new FatalKernelException("Failed to create coregion", e);
                 }
             }
         }
     }
 
     private StandardQuasiBundle doInstall(URI location, BundleManifest bundleManifest) throws BundleException {
         try {
             Dictionary<String, String> manifest = bundleManifest.toDictionary();
             String installLocation = "file".equals(location.getScheme()) ? new File(location).getAbsolutePath() : location.toString();
             BundleDescription bundleDescription = this.stateObjectFactory.createBundleDescription(this.state, manifest, this.coregion.getName()
                 + REGION_LOCATION_DELIMITER + installLocation, nextBundleId());
             this.state.addBundle(bundleDescription);
             this.coregion.addBundle(bundleDescription.getBundleId());
             return new StandardQuasiBundle(bundleDescription, bundleManifest, this.stateHelper);
         } catch (RuntimeException e) {
             throw new BundleException("Unable to read bundle at '" + location + "'", e);
         }
     }
 
     /**
      * @return
      */
     private long nextBundleId() {
         return this.dependencyCalculator.getNextBundleId();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<QuasiBundle> getBundles() {
         BundleDescription[] bundleDescriptions = this.state.getBundles();
         List<QuasiBundle> result = new ArrayList<QuasiBundle>();
         QuasiBundle quasiBundle;
         for (BundleDescription bundleDescription : bundleDescriptions) {
             quasiBundle = new StandardQuasiBundle(bundleDescription, null, this.stateHelper);
             result.add(quasiBundle);
         }
         return Collections.unmodifiableList(result);
     }
 
     /**
      * {@inheritDoc}
      */
     public QuasiBundle getBundle(long bundleId) {
         QuasiBundle quasiBundle = null;
         BundleDescription bundleDescription = this.state.getBundle(bundleId);
         if (bundleDescription != null) {
             quasiBundle = new StandardQuasiBundle(bundleDescription, null, this.stateHelper);
         }
         return quasiBundle;
     }
 
     /**
      * {@inheritDoc}
      */
     public QuasiBundle getBundle(String name, Version version) {
         QuasiBundle quasiBundle = null;
         BundleDescription bundleDescription = this.state.getBundle(name, version);
         if (bundleDescription != null) {
             quasiBundle = new StandardQuasiBundle(bundleDescription, null, this.stateHelper);
         }
         return quasiBundle;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<QuasiResolutionFailure> resolve() {
         synchronized (this.monitor) {
             BundleDescription[] bundles = getBundleDescriptionArray();
             BundleDescription[] dependencies = getDependencies(bundles);
 
             this.otherBundles = dependencies;
 
             List<QuasiResolutionFailure> failures = getFailures();
             if (!failures.isEmpty()) {
                 this.otherBundles = null;
             }
 
             return failures;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<QuasiResolutionFailure> diagnose(long bundleId) {
         BundleDescription bundleDescription = this.state.getBundle(bundleId);
         ResolverErrorsHolder reh = new ResolverErrorsHolder();
         String failureDescription = this.detective.generateFailureDescription(this.state, bundleDescription, reh);
         return this.processResolverErrors(reh.getResolverErrors(), new StandardQuasiBundle(bundleDescription, null, this.stateHelper),
             failureDescription);
     }
 
     private BundleDescription[] getDependencies(BundleDescription[] bundles) {
         createCoregionIfNecessary();
         try {
             return this.dependencyCalculator.calculateDependencies(this.state, this.coregion, bundles);
         } catch (BundleException e) {
             return new BundleDescription[0];
         } catch (UnableToSatisfyDependenciesException utsde) {
             return new BundleDescription[0];
         }
     }
 
     private List<QuasiResolutionFailure> getFailures() {
         List<QuasiResolutionFailure> failures = new ArrayList<QuasiResolutionFailure>();
         ResolverErrorsHolder reh;
         String failureDescription;
         for (StandardQuasiBundle quasiBundle : this.installedQuasiBundles) {
             if (!quasiBundle.isResolved()) {
                 reh = new ResolverErrorsHolder();
                 failureDescription = this.detective.generateFailureDescription(this.state, quasiBundle.getBundleDescription(), reh);
                 failures.addAll(this.processResolverErrors(reh.getResolverErrors(), quasiBundle, failureDescription));
             }
         }
         return failures;
     }
 
     private List<QuasiResolutionFailure> processResolverErrors(ResolverError[] resolverErrors, QuasiBundle quasiBundle, String failureDescription) {
         List<QuasiResolutionFailure> processedResolverErrors = new ArrayList<QuasiResolutionFailure>();
         boolean added = false;
         if (resolverErrors != null) {
             for (ResolverError resolverError : resolverErrors) {
                 if (resolverError.getType() == ResolverError.IMPORT_PACKAGE_USES_CONFLICT) {
                     VersionConstraint unsatisfiedConstraint = resolverError.getUnsatisfiedConstraint();
                     if (unsatisfiedConstraint instanceof ImportPackageSpecification) {
                         processedResolverErrors.add(createPackagesUsesResolutionFailure(quasiBundle, failureDescription, unsatisfiedConstraint));
                         added = true;
                     }
                 } else if (resolverError.getType() == ResolverError.MISSING_IMPORT_PACKAGE) {
                     VersionConstraint unsatisfiedConstraint = resolverError.getUnsatisfiedConstraint();
                     if (unsatisfiedConstraint instanceof ImportPackageSpecification) {
                         processedResolverErrors.add(createPackageResolutionFailure(quasiBundle, failureDescription, unsatisfiedConstraint));
                         added = true;
                     }
                 }
             }
         }
         if (!added) {
             processedResolverErrors.add(new GenericQuasiResolutionFailure(quasiBundle, failureDescription));
         }
         return processedResolverErrors;
     }
 
     private PackageQuasiResolutionFailure createPackageResolutionFailure(QuasiBundle quasiBundle, String failureDescription,
         VersionConstraint unsatisfiedConstraint) {
         ImportPackageSpecification importPackageSpecification = (ImportPackageSpecification) unsatisfiedConstraint;
         String pkgName = importPackageSpecification.getName();
         VersionRange pkgVersionRange = convertVersionRange(importPackageSpecification.getVersionRange());
         String bundleSymbolicName = importPackageSpecification.getBundleSymbolicName();
         VersionRange bundleVersionRange = convertVersionRange(importPackageSpecification.getBundleVersionRange());
         long bundleId = importPackageSpecification.getBundle().getBundleId();
         this.logger.debug("Missing import: package '{}' version '{}' bundle '{}' version '{}' id '{}'", new Object[] { pkgName, pkgVersionRange,
             bundleSymbolicName, bundleVersionRange, bundleId });
         return new PackageQuasiResolutionFailure(failureDescription, quasiBundle, pkgName, pkgVersionRange, bundleSymbolicName, bundleVersionRange);
     }
 
     private PackageUsesQuasiResolutionFailure createPackagesUsesResolutionFailure(QuasiBundle quasiBundle, String failureDescription,
         VersionConstraint unsatisfiedConstraint) {
         ImportPackageSpecification importPackageSpecification = (ImportPackageSpecification) unsatisfiedConstraint;
         String pkgName = importPackageSpecification.getName();
         VersionRange pkgVersionRange = convertVersionRange(importPackageSpecification.getVersionRange());
         String bundleSymbolicName = importPackageSpecification.getBundleSymbolicName();
         VersionRange bundleVersionRange = convertVersionRange(importPackageSpecification.getBundleVersionRange());
         long bundleId = importPackageSpecification.getBundle().getBundleId();
         this.logger.debug("Uses conflict: package '{}' version '{}' bundle '{}' version '{}' id '{}'", new Object[] { pkgName, pkgVersionRange,
             bundleSymbolicName, bundleVersionRange, bundleId });
         return new PackageUsesQuasiResolutionFailure(failureDescription, quasiBundle, pkgName, pkgVersionRange, bundleSymbolicName,
             bundleVersionRange);
     }
 
     private static VersionRange convertVersionRange(org.eclipse.osgi.service.resolver.VersionRange versionRange) {
         return new VersionRange(versionRange.toString());
     }
 
     private BundleDescription[] getBundleDescriptionArray() {
         BundleDescription[] bd;
         int n = this.installedQuasiBundles.size();
         bd = new BundleDescription[n];
         for (int i = 0; i < n; i++) {
             bd[i] = this.installedQuasiBundles.get(i).getBundleDescription();
         }
         return bd;
     }
 
     /**
      * {@inheritDoc}
      */
     public void commit() throws BundleException {
         synchronized (this.monitor) {
             if (this.otherBundles == null) {
                 List<QuasiResolutionFailure> failures = resolve();
                 if (!failures.isEmpty()) {
                     throw new BundleException("Commit resolution failed: '" + failures.toString() + "'");
                 }
             } else {
                 try {
                     Set<Long> installedQuasiBundles = installQuasiBundles();
                     List<Bundle> installedDependencies = installOtherBundles(installedQuasiBundles);
                     startBundles(installedDependencies);
                 } catch (BundleException e) {
                     uninstallQuasiBundles();
                     throw e;
                 }
             }
         }
     }
 
     private void startBundles(List<Bundle> bundles) throws BundleException {
         for (Bundle bundle : bundles) {
             startBundle(bundle);
         }
     }
 
     private void startBundle(Bundle bundle) throws BundleException {
         if (!isFragmentBundle(bundle)) {
            String bundleActivationPolicy = (String) bundle.getHeaders().get(Constants.BUNDLE_ACTIVATIONPOLICY);
            if (bundleActivationPolicy == null) {
                try {
                    bundle.start();
                } catch (BundleException be) {
                    throw new BundleException("Failed to start bundle '" + bundle.getSymbolicName() + "' version '" + bundle.getVersion() + "'", be);
                }
             }
         }
     }
 
     // TODO Move this method into utils project
     private static boolean isFragmentBundle(Bundle bundle) {
         String fragmentHostHeader = (String) bundle.getHeaders().get(Constants.FRAGMENT_HOST);
         return StringUtils.hasText(fragmentHostHeader);
     }
 
     private List<Bundle> installOtherBundles(Set<Long> installedQuasiBundles) throws BundleException {
         List<Bundle> installedBundles = new ArrayList<Bundle>();
         for (BundleDescription otherBundle : otherBundles) {
             if (!installedQuasiBundles.contains(otherBundle.getBundleId())) {
                 try {
                     Bundle bundle = installBundleDescription(otherBundle);
                     installedBundles.add(bundle);
                 } catch (BundleException e) {
                     for (Bundle bundle : installedBundles) {
                         try {
                             bundle.uninstall();
                         } catch (BundleException be) {
                             this.logger.error("Uninstall of '{}' failed", be, bundle);
                         }
                     }
                     throw e;
                 }
             }
         }
         return installedBundles;
     }
 
     private Set<Long> installQuasiBundles() throws BundleException {
         Set<Long> installed = new HashSet<Long>();
         for (StandardQuasiBundle quasiBundle : this.installedQuasiBundles) {
             BundleDescription description = quasiBundle.getBundleDescription();
             String location = description.getLocation();
             ManifestTransformer manifestTransformer = new QuasiManifestTransformer(quasiBundle.getBundleManifest());
             this.bundleTransformationHandler.pushManifestTransformer(manifestTransformer);
 
             try {
                 URI locationUri = new File(stripRegionTag(location)).toURI();
                 Bundle bundle = doInstallBundleInternal(locationUri.toString());
                 quasiBundle.setBundle(bundle);
                 installed.add(description.getBundleId());
             } finally {
                 this.bundleTransformationHandler.popManifestTransformer();
             }
 
         }
         return installed;
     }
 
     private String stripRegionTag(String location) {
         int atPos = location.indexOf(REGION_LOCATION_DELIMITER);
         if (atPos != -1) {
             return location.substring(atPos + 1);
         }
         return location;
     }
 
     private static final class QuasiManifestTransformer implements ManifestTransformer {
 
         private final BundleManifest bundleManifest;
 
         public QuasiManifestTransformer(BundleManifest bundleManifest) {
             this.bundleManifest = bundleManifest;
         }
 
         /**
          * {@inheritDoc}
          */
         public BundleManifest transform(BundleManifest bundleManifest) {
             return this.bundleManifest;
         }
     }
 
     private Bundle installBundleDescription(BundleDescription description) throws BundleException {
         String location = stripRegionTag(description.getLocation());
         String installLocation = location.startsWith("http:") ? location : new File(location).toURI().toString();
         return doInstallBundleInternal(installLocation);
     }
 
     private Bundle doInstallBundleInternal(String location) throws BundleException {
         return this.userRegion.installBundle(location, openBundleStream(location));
     }
 
     private InputStream openBundleStream(String location) throws BundleException {
         String absoluteBundleUriString = getAbsoluteUriString(location);
 
         try {
             // Use the reference: scheme to obtain an InputStream for either a file or a directory.
             return new URL(REFERENCE_SCHEME + absoluteBundleUriString).openStream();
 
         } catch (MalformedURLException e) {
             throw new BundleException("Invalid bundle URI '" + absoluteBundleUriString + "'", e);
         } catch (IOException e) {
             throw new BundleException("Invalid bundle at URI '" + absoluteBundleUriString + "'", e);
         }
     }
 
     private String getAbsoluteUriString(String bundleUriString) throws BundleException {
 
         if (!bundleUriString.startsWith(FILE_SCHEME)) {
             throw new BundleException("'" + bundleUriString + "' which did not start with '" + FILE_SCHEME + "'");
         }
 
         String filePath = bundleUriString.substring(FILE_SCHEME.length());
 
         return FILE_SCHEME + new File(filePath).getAbsolutePath();
     }
 
     private void uninstallQuasiBundles() {
         for (StandardQuasiBundle quasiBundle : this.installedQuasiBundles) {
             Bundle bundle = quasiBundle.getBundle();
             if (bundle != null) {
                 try {
                     bundle.uninstall();
                 } catch (BundleException e) {
                     this.logger.error("Uninstall of '{}' failed", e, quasiBundle);
                 }
                 quasiBundle.setBundle(null);
             }
         }
     }
 
     @Override
     public void destroy() {
         Region coregionCopy;
         synchronized (this.monitor) {
             coregionCopy = this.coregion;
             this.coregion = null;
         }
         if (coregionCopy != null) {
             this.regionDigraph.removeRegion(coregionCopy);
         }
     }
 }
