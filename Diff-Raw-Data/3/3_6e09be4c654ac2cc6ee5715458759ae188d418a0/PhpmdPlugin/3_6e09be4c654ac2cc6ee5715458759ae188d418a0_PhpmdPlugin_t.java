 /*******************************************************************************
  * Copyright (c) 2009, 2010 Dejan Spasic
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.phpsrc.eclipse.pti.tools.phpmd;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.osgi.framework.BundleContext;
 import org.phpsrc.eclipse.pti.core.AbstractPHPToolPlugin;
 import org.phpsrc.eclipse.pti.library.pear.PHPLibraryPEARPlugin;
 import org.phpsrc.eclipse.pti.tools.phpmd.core.Phpmd;
import org.phpsrc.eclipse.pti.tools.phpmd.model.ViolationManager;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class PhpmdPlugin extends AbstractPHPToolPlugin {
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.phpsrc.eclipse.pti.tools.phpmd";
 
 	// The shared instance
 	private static PhpmdPlugin plugin;
 
 	private Phpmd phpmd;
 
 	/**
 	 * The constructor
 	 */
 	public PhpmdPlugin() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
 	 * )
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
 	 * )
 	 */
 	public void stop(BundleContext context) throws Exception {
		ViolationManager.getManager().saveViolations();
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 * 
 	 * @return the shared instance
 	 */
 	public static PhpmdPlugin getDefault() {
 		return plugin;
 	}
 
 	public Phpmd getPhpmd() {
 		if (null == phpmd) {
 			phpmd = new Phpmd();
 		}
 
 		return phpmd;
 	}
 
 	/**
 	 * Provides all include paths for the plugin
 	 * 
 	 * @return the include paths
 	 */
 	public IPath[] getPluginIncludePaths(IProject project) {
 		IPath[] pearPaths = PHPLibraryPEARPlugin.getDefault().getPluginIncludePaths(project);
 
 		IPath[] includePaths = new IPath[pearPaths.length + 1];
 		includePaths[0] = resolvePluginResource("/php/tools");
 		System.arraycopy(pearPaths, 0, includePaths, 1, pearPaths.length);
 
 		return includePaths;
 	}
 }
