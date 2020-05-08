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
 package org.eclipse.riena.internal.core;
 
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.riena.core.util.PropertiesUtils;
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.internal.core.ignore.IgnoreFindBugs;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.service.log.LogService;
 
 /**
  * Execute the startup actions
  */
 public class StartupsSafeRunnable implements ISafeRunnable {
 
 	private IRienaStartupExtension[] startups;
 
 	private final static Logger LOGGER = Activator.getDefault().getLogger(StartupsSafeRunnable.class);
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.
 	 * Throwable)
 	 */
 	public void handleException(Throwable exception) {
		// TODO use our own exception handling!
 		LOGGER.log(LogService.LOG_ERROR, "Error activating bundels.", exception); //$NON-NLS-1$
 	}
 
 	/*
 	 * Execute all startup actions
 	 * 
 	 * @see org.eclipse.core.runtime.ISafeRunnable#run()
 	 */
 	public void run() throws Exception {
 		if (startups == null) {
 			return;
 		}
 		// handle required bundles first!
 		for (IRienaStartupExtension startup : startups) {
 			String[] bundleNames = PropertiesUtils.asArray(startup.getRequiredBundles());
 			for (String bundleName : bundleNames) {
				// special bundle that we need! ->> TODO extension point for foreign bundles?!
 				if (StringUtils.isEmpty(bundleName)) {
 					continue;
 				}
 				Bundle bundle = Platform.getBundle(bundleName);
 				if (bundle != null) {
 					if (bundle.getState() != Bundle.ACTIVE) {
 						start(bundle);
 					} else {
 						LOGGER.log(LogService.LOG_INFO, "Startup required bundle: '" + bundleName //$NON-NLS-1$
 								+ "' already started."); //$NON-NLS-1$
 					}
 				} else {
 					LOGGER.log(LogService.LOG_WARNING, "Startup required bundle: '" + bundleName //$NON-NLS-1$
 							+ "' not found."); //$NON-NLS-1$
 				}
 			}
 		}
 		// handle bundle self activation either by creating and executing the load class or by activating the bundle 
 		for (IRienaStartupExtension startup : startups) {
 			Bundle bundle = startup.getContributingBundle();
 			String runClassName = startup.getRunClassName();
 			if (StringUtils.isGiven(runClassName)) {
 				// try to load and execute the starter class
 				try {
 					startup.createRunner().run();
 					LOGGER.log(LogService.LOG_INFO, "Startup: '" + bundle.getSymbolicName() + "' with starter '" //$NON-NLS-1$ //$NON-NLS-2$
 							+ runClassName + "' succesful."); //$NON-NLS-1$
 				} catch (Exception e) {
 					LOGGER.log(LogService.LOG_ERROR, "Startup: '" + bundle.getSymbolicName() + "' with starter '" //$NON-NLS-1$ //$NON-NLS-2$
 							+ runClassName + "' failed.", e); //$NON-NLS-1$
 				}
 			}
 			if (!startup.isActivateSelf()) {
 				continue;
 			}
 			if (bundle.getState() == Bundle.RESOLVED) {
 				start(bundle);
 			} else if (bundle.getState() == Bundle.STARTING
 					&& Constants.ACTIVATION_LAZY.equals(bundle.getHeaders().get(Constants.BUNDLE_ACTIVATIONPOLICY))) {
 				try {
 					bundle.start();
 					LOGGER.log(LogService.LOG_INFO, "Startup <<lazy>>: '" + bundle.getSymbolicName() + "' succesful."); //$NON-NLS-1$ //$NON-NLS-2$
 				} catch (BundleException be) {
 					LOGGER.log(LogService.LOG_WARNING, "Startup <<lazy>>: '" + bundle.getSymbolicName() //$NON-NLS-1$
 							+ "' failed but may succeed (bundle state is in transition):\n\t\t" + be.getMessage() //$NON-NLS-1$
 							+ (be.getCause() != null ? " cause: " + be.getCause() : "")); //$NON-NLS-1$ //$NON-NLS-2$
 				} catch (RuntimeException rte) {
 					LOGGER.log(LogService.LOG_ERROR, "Startup <<lazy>>:: '" + bundle.getSymbolicName() //$NON-NLS-1$
 							+ "' failed with exception.", rte); //$NON-NLS-1$
 				}
 
 			} else if (bundle.getState() == Bundle.INSTALLED) {
 				LOGGER.log(LogService.LOG_ERROR, "Startup: '" + bundle.getSymbolicName() //$NON-NLS-1$
 						+ "' failed. Startup extension is set but is only in state INSTALLED (not RESOLVED)."); //$NON-NLS-1$
 			} else if (bundle.getState() == Bundle.ACTIVE) {
 				LOGGER.log(LogService.LOG_DEBUG, "Startup: '" + bundle.getSymbolicName() + "' is already ACTIVE."); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 	}
 
 	private void start(Bundle bundle) throws BundleException {
 		if (bundle == null) {
 			return;
 		}
 		try {
 			bundle.start();
 			LOGGER.log(LogService.LOG_INFO, "Startup: '" + bundle.getSymbolicName() + "' succesful."); //$NON-NLS-1$ //$NON-NLS-2$
 		} catch (RuntimeException rte) {
 			LOGGER.log(LogService.LOG_ERROR, "Startup: '" + bundle.getSymbolicName() //$NON-NLS-1$
 					+ "' failed with exception.", rte); //$NON-NLS-1$
 		}
 	}
 
 	@IgnoreFindBugs(value = "EI_EXPOSE_REP2", justification = "deep cloning the startups is too expensive")
 	public void update(IRienaStartupExtension[] startups) {
 		this.startups = startups;
 	}
 
 }
