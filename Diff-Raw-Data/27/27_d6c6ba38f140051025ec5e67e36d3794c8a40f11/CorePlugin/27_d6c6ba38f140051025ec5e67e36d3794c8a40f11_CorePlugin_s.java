 // CorePlugin.java
 package org.eclipse.stem.core;
 
 /*******************************************************************************
  * Copyright (c) 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.stem.adapters.file.IdentifiableFileAdapterFactory;
 import org.eclipse.stem.core.graph.provider.GraphTimeProviderAdapterFactory;
 import org.osgi.framework.BundleContext;
 
 /**
  * The core STEM plugin.
  */
 public class CorePlugin extends Plugin {
 
 	/**
 	 * The plug-in ID
 	 */
 	public static final String PLUGIN_ID = "org.eclipse.stem.cSore";
 
 	// The shared instance.
 	private static CorePlugin plugin;
 
 	/**
 	 * The constructor.
 	 */
 	public CorePlugin() {
 		plugin = this;
 	} // CorePlugin
 
 	/**
 	 * This method is called upon plug-in activation
 	 * 
 	 * @param context
 	 * @throws Exception
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		new GraphTimeProviderAdapterFactory();
 		new IdentifiableFileAdapterFactory();
 		// The constructor will populate the plug-in specified resources
 		// stem = new STEM();
 	} // start
 
 	
 	/**
 	 * This method is called when the plug-in is stopped
 	 * 
 	 * @param context
 	 * @throws Exception
 	 */
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 		plugin = null;
 	} // stop
 
 	/**
 	 * Returns the shared instance.
 	 * 
 	 * @return the singleton instance of the plug-in class.
 	 */
 	public static CorePlugin getDefault() {
 		return plugin;
 	} // CorePlugin
 
 	/**
 	 * Log an error to the ILog for this plugin
 	 * 
 	 * @param message
 	 *            the localized error message text
 	 * @param exception
 	 *            the associated exception, or null
 	 */
 	public static void logError(String message, Throwable exception) {
 		// Do we have a plugin?
 		if (plugin != null) {
 			// Yes
 			plugin.getLog().log(
 					new Status(IStatus.ERROR, plugin.getBundle().getSymbolicName(),
 							0, message, exception));
 		}
 		else {
 			// No
 			System.out.println(message);
 			// Exception?
 			if (exception != null) {
 				// Yes
 				System.out.println(exception.getMessage());
 			} // if 
 		}
 	} // logError
 
 	/**
 	 * Log information to the ILog for this plugin
 	 * 
 	 * @param message
 	 *            the localized information message text
 	 * @param exception
 	 *            the associated exception, or null
 	 */
 	public static void logInformation(String message, Throwable exception) {
 		// Do we have a plugin?
 		if (plugin != null) {
 			// Yes
 			plugin.getLog().log(
 					new Status(IStatus.INFO, plugin.getBundle().getSymbolicName(),
 							0, message, exception));
 		}
 		else {
 			// No
 			System.out.println(message);
 			// Exception?
 			if (exception != null) {
 				// Yes
 				System.out.println(exception.getMessage());
 			} // if 
 		}
 	} // logInformation
 
 } // CorePlugin
