 /*******************************************************************************
  * Copyright (c) 2011 Petri Tuononen and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Petri Tuononen - Initial implementation
  *******************************************************************************/
 package org.eclipse.cdt.managedbuilder.pkgconfig;
 
 import java.io.IOException;
 import java.util.PropertyResourceBundle;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends AbstractUIPlugin {
 
 	//Plug-in ID
	public static final String PLUGIN_ID = "pkg-config-support"; //$NON-NLS-1$
 
 	//Shared instance
 	private static Activator plugin;
 	
 	//Name for the properties file
 	private final static String PROPERTIES = "plugin.properties"; //$NON-NLS-1$
 	
 	//Property Resource bundle
 	private PropertyResourceBundle properties;
 	
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 		super();
 		plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
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
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns an image descriptor for the image file at the given
 	 * plug-in relative path
 	 *
 	 * @param path the path
 	 * @return the image descriptor
 	 */
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(PLUGIN_ID, path);
 	}
 	
 	/**
 	 * Get plugin.properties
 	 * 
 	 * @return PropertyResourceBundle
 	 */
 	public PropertyResourceBundle getProperties(){
 		if (this.properties == null){
 			try {
 				this.properties = new PropertyResourceBundle(
 						FileLocator.openStream(this.getBundle(),
 								new Path(PROPERTIES),false));
 			} catch (IOException e) {
 				//log error
 				log(e);
 			}
 		}
 		return this.properties;
 	}	  
 	
 	/**
 	 * Log error.
 	 * 
 	 * @param e
 	 */
 	public void log(Throwable e) {
 		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e)); //$NON-NLS-1$
 	}
 
 	/**
 	 * Log status.
 	 * 
 	 * @param status
 	 */
 	public void log(IStatus status) {
 		getLog().log(status);
 	}
 	
 }
