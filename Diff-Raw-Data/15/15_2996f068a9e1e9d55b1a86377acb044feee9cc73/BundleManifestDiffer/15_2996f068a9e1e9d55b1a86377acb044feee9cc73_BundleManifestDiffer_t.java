 /*******************************************************************************
  * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.manifest.internal.core;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.eclipse.virgo.ide.manifest.core.IBundleManifestChangeListener;
 import org.eclipse.virgo.ide.manifest.core.IBundleManifestChangeListener.Type;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
 import org.eclipse.virgo.util.osgi.manifest.ExportPackage;
 import org.eclipse.virgo.util.osgi.manifest.ImportBundle;
 import org.eclipse.virgo.util.osgi.manifest.ImportLibrary;
 import org.eclipse.virgo.util.osgi.manifest.ImportPackage;
 import org.eclipse.virgo.util.osgi.manifest.RequireBundle;
 import org.osgi.framework.Constants;
 
 /**
  * Utility that checks two {@link BundleManifest} instances for equality.
  * 
  * @author Christian Dupuis
  * @since 1.0.1
  */
 class BundleManifestDiffer {
 
 	/**
 	 * Diffs the two given bundles and provides a set of changes.
 	 * 
 	 * @param bundleManifest1
 	 *            the first manifest to check
 	 * @param bundleManifest2
 	 *            the second manifest to check
 	 * @return {@link Set} of {@link IBundleManifestChangeListener.Type} expressing the actual change.
 	 */
 	static Set<Type> diff(BundleManifest bundleManifest1, BundleManifest bundleManifest2) {
 		if (bundleManifest1 == null && bundleManifest2 == null) {
 			return Collections.emptySet();
 		} else if ((bundleManifest1 == null && bundleManifest2 != null)
 				|| (bundleManifest1 != null && bundleManifest2 == null)) {
 			return BundleManifestManager.IMPORTS_CHANGED;
 		}
 
 		ImportPackage importPackageHeader1 = bundleManifest1.getImportPackage();
 		ImportPackage importPackageHeader2 = bundleManifest2.getImportPackage();
 
 		ExportPackage exportPackageHeader1 = bundleManifest1.getExportPackage();
 		ExportPackage exportPackageHeader2 = bundleManifest2.getExportPackage();
 
 		ImportLibrary importLibraryHeader1 = bundleManifest1.getImportLibrary();
 		ImportLibrary importLibraryHeader2 = bundleManifest2.getImportLibrary();
 
 		ImportBundle importBundleHeader1 = bundleManifest1.getImportBundle();
 		ImportBundle importBundleHeader2 = bundleManifest2.getImportBundle();
 
 		RequireBundle requireBundleHeader1 = bundleManifest1.getRequireBundle();
 		RequireBundle requireBundleHeader2 = bundleManifest2.getRequireBundle();
 
 		String execEnvironment1 = bundleManifest1.toDictionary().get(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT);
 		String execEnvironment2 = bundleManifest2.toDictionary().get(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT);
 
 		Set<Type> differences = new HashSet<Type>();
 
		if (!ObjectUtils.equals(importPackageHeader1.getImportedPackages(), importPackageHeader2.getImportedPackages())) {
 			differences.add(Type.IMPORT_PACKAGE);
 		}
 		if (!ObjectUtils.equals(execEnvironment1, execEnvironment2)) {
 			differences.add(Type.IMPORT_PACKAGE);
 		}
		if (!ObjectUtils.equals(exportPackageHeader1.getExportedPackages(), exportPackageHeader2.getExportedPackages())) {
 			differences.add(Type.EXPORT_PACKAGE);
 		}
		if (!ObjectUtils.equals(importLibraryHeader1.getImportedLibraries(),
				importLibraryHeader2.getImportedLibraries())) {
 			differences.add(Type.IMPORT_LIBRARY);
 		}
		if (!ObjectUtils.equals(importBundleHeader1.getImportedBundles(), importBundleHeader2.getImportedBundles())) {
 			differences.add(Type.IMPORT_BUNDLE);
 		}
		if (!ObjectUtils.equals(requireBundleHeader1.getRequiredBundles(), requireBundleHeader2.getRequiredBundles())) {
 			differences.add(Type.REQUIRE_BUNDLE);
 		}
 
 		return differences;
 	}
 
 }
