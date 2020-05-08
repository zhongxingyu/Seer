 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.security.simpleservices;
 
 import java.io.InputStream;
 
 import org.eclipse.riena.core.RienaActivator;
 import org.eclipse.riena.core.RienaConstants;
 import org.eclipse.riena.security.authorizationservice.IPermissionStore;
 import org.eclipse.riena.security.sessionservice.ISessionStore;
 import org.eclipse.riena.security.simpleservices.authorizationservice.store.FilePermissionStore;
 import org.eclipse.riena.security.simpleservices.sessionservice.store.MemoryStore;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 
 /**
  * The activator class controls the plug-in life cycle
  */
public final class Activator extends RienaActivator {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.eclipse.riena.security.simpleservices.simple.services"; //$NON-NLS-1$
 
 	private ServiceRegistration memoryStore;
 	private ServiceRegistration filepermissionstore;
 
 	// The shared instance
 	private static Activator plugin;
 
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		Activator.plugin = this;
 		// bring up a simple in memory session store
 		memoryStore = getContext().registerService(ISessionStore.class.getName(), new MemoryStore(),
 				RienaConstants.newDefaultServiceProperties());
 
 		// bring up a simple authorization store for permissions
 		InputStream inputStream = this.getClass().getResourceAsStream("policy-def.xml"); //$NON-NLS-1$
 		FilePermissionStore store = new FilePermissionStore(inputStream);
 		filepermissionstore = context.registerService(IPermissionStore.class.getName(), store, RienaConstants
 				.newDefaultServiceProperties());
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		memoryStore.unregister();
 		filepermissionstore.unregister();
 		Activator.plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Get the plugin instance.
 	 * 
 	 * @return
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 }
