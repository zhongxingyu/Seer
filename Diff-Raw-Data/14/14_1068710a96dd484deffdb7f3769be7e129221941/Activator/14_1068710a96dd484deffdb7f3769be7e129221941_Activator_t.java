 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.core.tests;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import org.eclipse.core.runtime.Plugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends Plugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.eclipse.dltk.ruby.core.tests";
 
 	// The shared instance
 	private static Activator plugin;
 	
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 		plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 	public static InputStream openResource(String path) throws IOException {
		URL url = getDefault().getBundle().getEntry(path);
 		return new BufferedInputStream(url.openStream());		
 	}
 }
