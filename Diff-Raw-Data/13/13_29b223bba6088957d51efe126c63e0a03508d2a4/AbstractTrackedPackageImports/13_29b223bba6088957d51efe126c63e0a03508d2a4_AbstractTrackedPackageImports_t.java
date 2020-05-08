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
 
 package org.eclipse.virgo.kernel.userregion.internal.importexpansion;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.virgo.kernel.osgi.framework.ImportMergeException;
 
 import org.eclipse.virgo.kernel.serviceability.Assert;
 import org.eclipse.virgo.util.osgi.VersionRange;
 import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
 import org.eclipse.virgo.util.osgi.manifest.Resolution;
 
 /**
  * {@link AbstractTrackedPackageImports} provides the general implementations of {@link TrackedPackageImports}.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * This class is thread safe.
  * 
  */
 abstract class AbstractTrackedPackageImports implements TrackedPackageImports {
 
     protected static final String SOURCE_SEPARATOR = ", ";
 
     private static final String VERSION_ATTRIBUTE_NAME = "version";
 
     private static final String VERSION_ALTERNATE_ATTRIBUTE_NAME = "specification-version";
 
     private static final String BUNDLE_VERSION_ATTRIBUTE_NAME = "bundle-version";
 
     private static final Object tieMonitor = new Object(); // locking for hash clashes in isEquivalent
 
     private final Object monitor = new Object();
 
     /**
      * The current merged package imports are held as a map of package name to {@link ImportedPackage}. Each package
      * import contains one and only one imported package name which corresponds to the package name used to index the
      * package import in the map. The map is valid if and only if mergeException is <code>null</code>.
      */
     protected final Map<String, ImportedPackage> mergedPackageImports = new HashMap<String, ImportedPackage>();
 
     private ImportMergeException mergeException = null;
 
     protected final List<TrackedPackageImports> sources = new ArrayList<TrackedPackageImports>();
 
     /**
      * Construct an {@link AbstractTrackedPackageImports} from the given package imports.
      * 
      * @param initialPackageImports a map of package name to {@link ImportedPackage}
      */
     AbstractTrackedPackageImports(Map<String, ImportedPackage> packageImports) {
         this.mergedPackageImports.putAll(packageImports);
     }
 
     /**
      * {@inheritDoc}
      */
     public void merge(TrackedPackageImports importsToMerge) throws ImportMergeException {
         synchronized (this.monitor) {
             checkMergeException();
             try {
                 // Add the new imports before merging so they are included in any diagnostics.
                 sources.add(importsToMerge);
                 doMerge(importsToMerge);
             } catch (ImportMergeException e) {
                 this.mergeException = e;
                 throw e;
             }
         }
     }
 
     /**
      * Merge the given package imports into this collection of package imports. If there is a conflict, issue
      * diagnostics and throw {@link ImportMergeException}.
      * <p />
      * Pre-condition: the monitor is held and the current merged imports have no conflicts.
      * 
      * @param importsToMerge
      * @throws ImportMergeException
      */
     private void doMerge(TrackedPackageImports importsToMerge) throws ImportMergeException {
         List<ImportedPackage> mergedImportsToMerge = importsToMerge.getMergedImports();
         for (ImportedPackage packageImportToMerge : mergedImportsToMerge) {
             String pkg = packageImportToMerge.getPackageName();
             ImportedPackage mergedPackageImport = this.mergedPackageImports.get(pkg);
             if (mergedPackageImport == null) {
                 this.mergedPackageImports.put(pkg, packageImportToMerge);
             } else {
                 mergePackageImport(mergedPackageImport, packageImportToMerge);
             }
         }
 
     }
 
     /**
      * Merge the given source package import into the given target package import. Throw {@link ImportMergeException} if
      * and only if there is a merge clash.
      * 
      * @param targetPackageImport the package import to be merged and updated
      * @param sourceImportToMerge the package import to be merged in
      * @throws ImportMergeException thrown if there is a merge clash
      */
     private void mergePackageImport(ImportedPackage targetPackageImport, ImportedPackage sourceImportToMerge) throws ImportMergeException {
         mergeAttributes(targetPackageImport, sourceImportToMerge);
         mergeDirectives(targetPackageImport, sourceImportToMerge);
 
     }
 
     /**
      * Merge the attributes of the source package import into those of the target package import. Throw
      * {@link ImportMergeException} if and only if there is a merge clash.
      * 
      * @param targetPackageImport the package import to be merged and updated
      * @param sourceImportToMerge the package import to be merged in
      * @throws ImportMergeException thrown if there is a merge clash
      */
     private void mergeAttributes(ImportedPackage targetPackageImport, ImportedPackage sourceImportToMerge) throws ImportMergeException {
         Map<String, String> targetAttributes = targetPackageImport.getAttributes();
         Map<String, String> sourceAttributes = sourceImportToMerge.getAttributes();
 
        // Merge attributes before versions so, for example, conflicting bundle symbolic names take precedence over disjoint bundle version ranges.
         for (Entry<String, String> sourceAttributeEntry : sourceAttributes.entrySet()) {
             String sourceAttributeName = sourceAttributeEntry.getKey();
             if (!isVersionAttribute(sourceAttributeName)) {
                 String sourceAttributeValue = sourceAttributeEntry.getValue();
                 String targetAttributeValue = targetAttributes.get(sourceAttributeName);
                 if (targetAttributeValue != null) {
                     if (!targetAttributeValue.equals(sourceAttributeValue)) {
                         throw new ImportMergeException(targetPackageImport.getPackageName(), getPackageSources(targetPackageImport),
                             "conflicting values '" + sourceAttributeValue + "', '" + targetAttributeValue + "' of attribute '" + sourceAttributeName
                            + "'");
                     }
                 } else {
                     targetAttributes.put(sourceAttributeName, sourceAttributeValue);
                 }
             }
         }
        
        mergeVersionRanges(targetPackageImport, sourceImportToMerge);

        mergeBundleVersionRanges(targetPackageImport, sourceImportToMerge);
     }
 
     /**
      * Merge the version ranges of the given source and target attributes and update the target attributes if necessary
      * with the merged range. If the version ranges are disjoint, throw {@link ImportMergeException}.
      * 
      * @param targetPackageImport the package import to be merged and updated
      * @param sourceAttributes
      * @throws ImportMergeException
      */
     private void mergeVersionRanges(ImportedPackage targetPackageImport, ImportedPackage sourceImportToMerge) throws ImportMergeException {
         Map<String, String> sourceAttributes = sourceImportToMerge.getAttributes();
         VersionRange sourceVersionRange = getVersionRange(sourceAttributes);
         if (sourceVersionRange != null) {
             Map<String, String> targetAttributes = targetPackageImport.getAttributes();
             VersionRange targetVersionRange = getVersionRange(targetAttributes);
             VersionRange mergedVersionRange;
             if (targetVersionRange == null) {
                 mergedVersionRange = sourceVersionRange;
             } else {
                 mergedVersionRange = VersionRange.intersection(sourceVersionRange, targetVersionRange);
                 if (mergedVersionRange.isEmpty()) {
                     throw new ImportMergeException(targetPackageImport.getPackageName(), getPackageSources(targetPackageImport),
                         "disjoint package version ranges");
                 }
             }
             targetAttributes.put(VERSION_ATTRIBUTE_NAME, mergedVersionRange.toParseString());
         }
     }
 
     /**
      * Get the version range for the given attributes.
      * 
      * @param attributes the attributes which may specify a version range
      * @return the version range or <code>null</code> if no version range is specified
      */
     private VersionRange getVersionRange(Map<String, String> attributes) {
         String versionRangeString = attributes.get(VERSION_ATTRIBUTE_NAME);
         if (versionRangeString == null) {
             versionRangeString = attributes.get(VERSION_ALTERNATE_ATTRIBUTE_NAME);
         }
         return versionRangeString == null ? null : new VersionRange(versionRangeString);
     }
 
     /**
      * Merge the bundle version ranges of the given source and target attributes and update the target attributes if
      * necessary with the merged range. If the bundle version ranges are disjoint, throw {@link ImportMergeException}.
      * 
      * @param targetPackageImport the package import to be merged and updated
      * @param sourceAttributes
      * @throws ImportMergeException
      */
     private void mergeBundleVersionRanges(ImportedPackage targetPackageImport, ImportedPackage sourceImportToMerge) throws ImportMergeException {
         VersionRange sourceVersionRange = sourceImportToMerge.getBundleVersion();
 
         // Map<String, String> targetAttributes = targetPackageImport.getAttributes();
         VersionRange targetVersionRange = targetPackageImport.getBundleVersion();
         VersionRange mergedVersionRange;
         if (targetVersionRange == null) {
             mergedVersionRange = sourceVersionRange;
         } else {
             mergedVersionRange = VersionRange.intersection(sourceVersionRange, targetVersionRange);
             if (mergedVersionRange.isEmpty()) {
                 throw new ImportMergeException(targetPackageImport.getPackageName(), getPackageSources(targetPackageImport),
                     "disjoint bundle version ranges " + sourceVersionRange.toString() + " and " + targetVersionRange.toString());
             }
         }
         targetPackageImport.setBundleVersion(mergedVersionRange);
     }
 
     /**
      * Return <code>true</code> if and only if the given attribute name is that of a version or bundle version
      * attribute.
      * 
      * @param attributeName the attribute name
      * @return whether or not the given attribute name is that of a version or bundle version attribute
      */
     private boolean isVersionAttribute(String attributeName) {
         return VERSION_ATTRIBUTE_NAME.equals(attributeName) || VERSION_ALTERNATE_ATTRIBUTE_NAME.equals(attributeName)
             || BUNDLE_VERSION_ATTRIBUTE_NAME.equals(attributeName);
     }
 
     /**
      * Merge the directives of the source package import into those of the target package import.
      * 
      * @param targetPackageImport
      * @param sourceImportToMerge
      */
     private void mergeDirectives(ImportedPackage targetPackageImport, ImportedPackage sourceImportToMerge) {
         if (targetPackageImport.getResolution() == Resolution.OPTIONAL && sourceImportToMerge.getResolution() == Resolution.MANDATORY) {
             targetPackageImport.setResolution(Resolution.MANDATORY);
         }
     }
 
     /**
      * Get a string describing the sources of the package of the given package import. This should help in diagnosing
      * the root cause of a conflicting merge.
      * 
      * @param pkg the package whose sources are required
      * @return a string describing the given package's sources
      */
     private String getPackageSources(ImportedPackage packageImport) {
         return getSources(packageImport.getPackageName());
     }
 
     /**
      * {@inheritDoc}
      */
     public String getSources(String pkg) {
         synchronized (this.monitor) {
             StringBuilder sourcesDescription = new StringBuilder();
             boolean first = true;
             String source = getSource(pkg);
             if (source != null) {
                 sourcesDescription.append(source);
                 first = false;
             }
             for (TrackedPackageImports trackedPackageImports : this.sources) {
                 String sources = trackedPackageImports.getSources(pkg);
                 if (sources != null) {
                     if (!first) {
                         sourcesDescription.append(SOURCE_SEPARATOR);
                     }
                     sourcesDescription.append(sources);
                     first = false;
                 }
             }
             return first ? null : sourcesDescription.toString();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public final List<ImportedPackage> getMergedImports() throws ImportMergeException {
         synchronized (this.monitor) {
             checkMergeException();
             List<ImportedPackage> mergedImports = new ArrayList<ImportedPackage>();
             for (ImportedPackage packageImport : this.mergedPackageImports.values()) {
                 mergedImports.add(packageImport);
             }
             return mergedImports;
         }
     }
 
     /**
      * If a merge failure has occurred, re-throw the {@link ImportMergeException}.
      * 
      * @throws ImportMergeException
      */
     private void checkMergeException() throws ImportMergeException {
         if (this.mergeException != null) {
             throw this.mergeException;
         }
     }
 
     /**
      * Convert a given list of package imports with no duplicate package names to a map of package name to
      * {@link PackageImport}.
      * 
      * @param importedPackages a list of <code>PackageImport</code>
      * @return a map of package name to <code>PackageImport</code>
      */
     protected static Map<String, ImportedPackage> convertImportedPackageListToMap(List<ImportedPackage> importedPackages) {
         Map<String, ImportedPackage> initialPackageImports = new HashMap<String, ImportedPackage>();
         for (ImportedPackage importedPackage : importedPackages) {
             Assert.isNull(initialPackageImports.put(importedPackage.getPackageName(), importedPackage),
                 "input packageImports must not contain duplicate items");
         }
 
         return initialPackageImports;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isEmpty() {
         synchronized (this.monitor) {
             return this.mergedPackageImports.isEmpty();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isEquivalent(TrackedPackageImports otherTrackedPackageImports) {
         if (otherTrackedPackageImports == null) {
             return isEmpty();
         }
 
         Assert.isInstanceOf(AbstractTrackedPackageImports.class, otherTrackedPackageImports,
             "otherTrackedPackageImports must be of type AbstractTrackedPackageImports");
         AbstractTrackedPackageImports otherAbstractTrackedPackageImports = (AbstractTrackedPackageImports) otherTrackedPackageImports;
         // Lock the object monitors in a predictable order to avoid deadlocks.
         // Use hash to determine ordering, and tieMonitor (static monitor) when hashes coincide.
         int thisHash = System.identityHashCode(this);
         int otherHash = System.identityHashCode(otherTrackedPackageImports);
         if (thisHash > otherHash) {
             synchronized (this.monitor) {
                 synchronized (otherAbstractTrackedPackageImports.monitor) {
                     return this.mergedPackageImports.equals(otherAbstractTrackedPackageImports.mergedPackageImports);
                 }
             }
         } else if (thisHash < otherHash) {
             synchronized (otherAbstractTrackedPackageImports.monitor) {
                 synchronized (this.monitor) {
                     return this.mergedPackageImports.equals(otherAbstractTrackedPackageImports.mergedPackageImports);
                 }
             }
         } else {
             synchronized (tieMonitor) {
                 synchronized (otherAbstractTrackedPackageImports.monitor) {
                     synchronized (this.monitor) {
                         return this.mergedPackageImports.equals(otherAbstractTrackedPackageImports.mergedPackageImports);
                     }
                 }
             }
         }
     }
 
 }
