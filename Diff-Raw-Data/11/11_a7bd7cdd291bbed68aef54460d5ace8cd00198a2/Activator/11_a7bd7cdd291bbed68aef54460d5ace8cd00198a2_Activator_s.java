 /*******************************************************************************
  * Copyright (c) 2007 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.core;
 
 import java.util.Hashtable;
 
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.riena.core.RienaStartupStatus;
 import org.eclipse.riena.core.logging.LogUtil;
 import org.eclipse.riena.internal.core.config.ConfigFromExtensions;
 import org.eclipse.riena.internal.core.config.ConfigSymbolReplace;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.cm.ConfigurationPlugin;
 import org.osgi.service.cm.ManagedService;
 import org.osgi.service.log.LogService;
 
 public class Activator extends Plugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.eclipse.riena.core";
 
 	// The shared instance
 	private static Activator plugin;
 	private static BundleContext CONTEXT;
 	private LogUtil logUtil;
 	private ServiceRegistration configSymbolReplace;
 	private ServiceRegistration configurationPlugin;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 		CONTEXT = context;
 		Logger LOGGER = getLogger(Activator.class.getName());
 
 		Bundle[] bundles = context.getBundles();
 		for (Bundle bundle : bundles) {
			String forceStart = (String) bundle.getHeaders().get("Riena-ForceStart");
 			if (bundle.getState() != Bundle.ACTIVE
 					&& (bundle.getSymbolicName().equals("org.eclipse.equinox.cm") || bundle.getSymbolicName().equals(
 							"org.eclipse.equinox.log"))) {
				forceStart = "true";
 			}
			if (forceStart != null && forceStart.equals("true")) {
 				// TODO STARTING == LAZY, so start that also, STARTING is
 				// disabled, bundles with forceStart should not be LAZY
 				if (bundle.getState() == Bundle.RESOLVED/*
 														 * || bundle.getState() ==
 														 * Bundle.STARTING
 														 */) {
 					bundle.start();
 					LOGGER.log(LogService.LOG_INFO, bundle.getSymbolicName() + " forced autostart successfully");
 				} else {
 					if (bundle.getState() == Bundle.INSTALLED) {
 						System.err.println(bundle.getSymbolicName()
 								+ " has Riena-ForceStart but is only in state INSTALLED (not RESOLVED).");
 					} else {
 						if (bundle.getState() == Bundle.ACTIVE) {
 							LOGGER.log(LogService.LOG_INFO, bundle.getSymbolicName()
 									+ " no forced autostart. Bundle is already ACTIVE.");
 						}
 					}
 				}
 			}
 		}
 
 		// register ConfigSymbolReplace that replaces symbols in config strings
 		ConfigSymbolReplace csr = new ConfigSymbolReplace();
 		Hashtable<String, String> ht = new Hashtable<String, String>();
 		ht.put(Constants.SERVICE_PID, "org.eclipse.riena.config.symbols");
 		// register as configurable osgi service
 		configSymbolReplace = context.registerService(ManagedService.class.getName(), csr, ht);
 		// register as config admin configuration plugin
 		configurationPlugin = context.registerService(ConfigurationPlugin.class.getName(), csr, null);
 		// execute the class that reads through the extensions and executes them
 		// as config admin packages
 		new ConfigFromExtensions(context).doConfig();
 		RienaStartupStatus.getInstance().setStarted(true);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 		configSymbolReplace.unregister();
 		configurationPlugin.unregister();
 		plugin = null;
 		CONTEXT = null;
 	}
 
 	/**
 	 * Returns the shared instance
 	 * 
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	public static BundleContext getContext() {
 		return CONTEXT;
 	}
 
	public Logger getLogger(String name) {
 		if (logUtil == null) {
 			logUtil = new LogUtil(CONTEXT);
 		}
 		return logUtil.getLogger(name);
 	}
 }
