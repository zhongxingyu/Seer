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
 package org.eclipse.virgo.ide.manifest.core.editor.model;
 
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.pde.internal.core.ICoreConstants;
 import org.eclipse.pde.internal.core.ibundle.IBundle;
 import org.eclipse.pde.internal.core.ibundle.IBundleModel;
 import org.eclipse.pde.internal.core.ibundle.IBundleModelFactory;
 import org.eclipse.pde.internal.core.ibundle.IManifestHeader;
 import org.eclipse.pde.internal.core.text.bundle.BundleActivatorHeader;
 import org.eclipse.pde.internal.core.text.bundle.BundleClasspathHeader;
 import org.eclipse.pde.internal.core.text.bundle.BundleLocalizationHeader;
 import org.eclipse.pde.internal.core.text.bundle.BundleModel;
 import org.eclipse.pde.internal.core.text.bundle.BundleNameHeader;
 import org.eclipse.pde.internal.core.text.bundle.BundleSymbolicNameHeader;
 import org.eclipse.pde.internal.core.text.bundle.BundleVendorHeader;
 import org.eclipse.pde.internal.core.text.bundle.BundleVersionHeader;
 import org.eclipse.pde.internal.core.text.bundle.ExportPackageHeader;
 import org.eclipse.pde.internal.core.text.bundle.FragmentHostHeader;
 import org.eclipse.pde.internal.core.text.bundle.ImportPackageHeader;
 import org.eclipse.pde.internal.core.text.bundle.LazyStartHeader;
 import org.eclipse.pde.internal.core.text.bundle.ManifestHeader;
 import org.eclipse.pde.internal.core.text.bundle.RequireBundleHeader;
 import org.eclipse.pde.internal.core.text.bundle.RequiredExecutionEnvironmentHeader;
 import org.eclipse.virgo.ide.manifest.core.IHeaderConstants;
 import org.osgi.framework.Constants;
 
 /**
  * @author Leo Dos Santos
  */
 public class SpringBundleModelFactory implements IBundleModelFactory {
 
 	private final IBundleModel fModel;
 
 	public SpringBundleModelFactory(IBundleModel model) {
 		fModel = model;
 	}
 
 	public IManifestHeader createHeader() {
 		return null;
 	}
 
 	public IManifestHeader createHeader(String key, String value) {
 		ManifestHeader header = null;
 		IBundle bundle = null;
 		if (fModel != null) {
 			bundle = fModel.getBundle();
 		}
 		String newLine;
 		if (fModel instanceof BundleModel) {
 			newLine = TextUtilities.getDefaultLineDelimiter(((BundleModel) fModel).getDocument());
 		} else {
 			newLine = System.getProperty("line.separator"); //$NON-NLS-1$
 		}
 
 		if (key.equalsIgnoreCase(Constants.BUNDLE_ACTIVATOR)) {
 			header = new BundleActivatorHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(Constants.BUNDLE_LOCALIZATION)) {
 			header = new BundleLocalizationHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(Constants.BUNDLE_NAME)) {
 			header = new BundleNameHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT)) {
 			header = new RequiredExecutionEnvironmentHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(Constants.BUNDLE_SYMBOLICNAME)) {
 			header = new BundleSymbolicNameHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(Constants.BUNDLE_VENDOR)) {
 			header = new BundleVendorHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(Constants.BUNDLE_VERSION)) {
 			header = new BundleVersionHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(Constants.BUNDLE_CLASSPATH)) {
 			header = new BundleClasspathHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(ICoreConstants.ECLIPSE_LAZYSTART)
 				|| key.equalsIgnoreCase(ICoreConstants.ECLIPSE_AUTOSTART)) {
 			header = new LazyStartHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(Constants.EXPORT_PACKAGE)
 				|| key.equalsIgnoreCase(ICoreConstants.PROVIDE_PACKAGE)) {
 			header = new ExportPackageHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(Constants.FRAGMENT_HOST)) {
 			header = new FragmentHostHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(Constants.IMPORT_PACKAGE)) {
 			header = new ImportPackageHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(Constants.REQUIRE_BUNDLE)) {
 			header = new RequireBundleHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(IHeaderConstants.IMPORT_BUNDLE)) {
 			header = new ImportBundleHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(IHeaderConstants.IMPORT_LIBRARY)) {
 			header = new ImportLibraryHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(IHeaderConstants.IMPORT_TEMPLATE)) {
 			header = new ImportPackageHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(IHeaderConstants.EXPORT_TEMPLATE)) {
 			header = new ExportPackageHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(IHeaderConstants.EXCLUDED_IMPORTS)) {
 			header = new ImportPackageHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(IHeaderConstants.EXCLUDED_EXPORTS)) {
 			header = new ExportPackageHeader(key, value, bundle, newLine);
 		} else if (key.equalsIgnoreCase(IHeaderConstants.UNVERSIONED_IMPORTS)) {
 			header = new ImportPackageHeader(key, value, bundle, newLine);
 		} else {
 			header = new ManifestHeader(key, value, bundle, newLine);
 		}
 		return header;
 	}
 
 }
