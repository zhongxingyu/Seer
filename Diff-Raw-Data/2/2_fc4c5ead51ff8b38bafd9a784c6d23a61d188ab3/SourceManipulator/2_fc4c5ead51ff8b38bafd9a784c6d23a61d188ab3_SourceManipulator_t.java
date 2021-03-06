 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  ******************************************************************************/
 package org.eclipse.equinox.internal.p2.touchpoint.eclipse;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 import org.eclipse.equinox.frameworkadmin.BundleInfo;
 import org.eclipse.equinox.p2.engine.IProfile;
 import org.eclipse.equinox.simpleconfigurator.manipulator.internal.SimpleConfiguratorManipulatorImpl;
 import org.osgi.framework.Version;
 
 //This class deals with source bundles and how their addition to the source.bundles.txt
 public class SourceManipulator {
 	private List sourceBundles;
 	private IProfile profile;
 	boolean changed = false;
 
 	public SourceManipulator(IProfile profile) {
 		this.profile = profile;
 	}
 
 	public void addBundle(File bundleFile) throws IOException {
 		if (sourceBundles == null)
 			load();
 		sourceBundles.add(new BundleInfo(bundleFile.toURL().toString()));
 	}
 
 	public void addBundle(File bundleFile, String bundleId, Version bundleVersion) throws IOException {
 		if (sourceBundles == null)
 			load();
 		BundleInfo sourceInfo = new BundleInfo(bundleFile.toURL().toString());
 		sourceInfo.setSymbolicName(bundleId);
 		sourceInfo.setVersion(bundleVersion.toString());
 		sourceBundles.add(sourceInfo);
 	}
 
 	public void removeBundle(File bundleFile) throws MalformedURLException, IOException {
 		if (sourceBundles == null)
 			load();
 		sourceBundles.remove(new BundleInfo(bundleFile.toURL().toString()));
 	}
 
 	public void save() throws IOException {
 		if (sourceBundles != null)
 			SimpleConfiguratorManipulatorImpl.saveConfiguration(sourceBundles, getFileLocation(), getLauncherLocation(), false);
 	}
 
 	private void load() throws MalformedURLException, IOException {
 		if (getFileLocation().exists())
 			sourceBundles = SimpleConfiguratorManipulatorImpl.readConfiguration(getFileLocation().toURL(), getLauncherLocation());
 		else
 			sourceBundles = new ArrayList();
 	}
 
 	private File getFileLocation() {
		return new File(Util.getConfigurationFolder(profile), "org.eclipse.equinox.source/source.bundles.txt"); //$NON-NLS-1$
 	}
 
 	private File getLauncherLocation() {
 		return Util.getInstallFolder(profile);
 	}
 }
