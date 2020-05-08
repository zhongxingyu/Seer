 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.server;

 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.emf.emfstore.server.exceptions.FatalEmfStoreException;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the emfstore life cycle.
  */
 public class Activator extends Plugin {
 
 	/**
 	 * The plug-in ID.
 	 */
 	public static final String PLUGIN_ID = "org.eclipse.emf.emfstore.server";
 
 	// The shared instance
 	private static Activator plugin;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void start(BundleContext context) throws FatalEmfStoreException {
 		try {
 			super.start(context);
 			// BEGIN SUPRESS CATCH EXCEPTION
 		} catch (Exception e) {
 			throw new FatalEmfStoreException("Plugin Bundle start failed!", e);
 		}
 		// END SUPRESS CATCH EXCEPTION
 		plugin = this;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void stop(BundleContext context) throws FatalEmfStoreException {
 		plugin = null;
 		try {
 			super.stop(context);
 			// BEGIN SUPRESS CATCH EXCEPTION
 		} catch (Exception e) {
 			throw new FatalEmfStoreException("Plugin Bundle stop failed!", e);
 		}
 		// END SUPRESS CATCH EXCEPTION
 	}
 
 	/**
 	 * Returns the shared instance.
 	 * 
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 }
