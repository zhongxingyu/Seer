/*******************************************************************************
 * Copyright (c) 2012 EclipseSource Muenchen GmbH.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Max Hohenegger (initial implementation)
 ******************************************************************************/
 package org.eclipse.emf.emfstore.branding;
 
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle.
  */
 public class Activator extends AbstractUIPlugin {
 
 	
 	/**
 	 * The plugin id.
 	 */
 	public static final String PLUGIN_ID = "org.unicase.emfstore.branding";
 
 	// The shared instance
 	private static Activator plugin;
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	// BEGIN SUPRESS CATCH EXCEPTION
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 	// END SUPRESS CATCH EXCEPTION
 	/**
 	 * Returns the shared instance.
 	 * 
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 }
