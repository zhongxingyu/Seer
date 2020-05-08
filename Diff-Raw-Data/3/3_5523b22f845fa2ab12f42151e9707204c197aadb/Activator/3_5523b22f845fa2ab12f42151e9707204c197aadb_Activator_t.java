 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.core;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleListener;
 import org.osgi.service.log.LogService;
 
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.jobs.Job;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.RienaConstants;
 import org.eclipse.riena.core.RienaPlugin;
 import org.eclipse.riena.core.RienaStatus;
 import org.eclipse.riena.core.exception.IExceptionHandlerManager;
 import org.eclipse.riena.core.logging.ConsoleLogger;
 import org.eclipse.riena.core.wire.Wire;
 import org.eclipse.riena.internal.core.exceptionmanager.SimpleExceptionHandlerManager;
 import org.eclipse.riena.internal.core.ignore.IgnoreFindBugs;
 
 public class Activator extends RienaPlugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.eclipse.riena.core"; //$NON-NLS-1$
 
 	// startup status of Riena
 	private boolean active;
 	private boolean startupActionsExecuted;
 
 	// The shared instance
 	private static Activator plugin;
 
 	@Override
 	@IgnoreFindBugs(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "that is the eclipse way")
 	public void start(final BundleContext context) throws Exception {
 		super.start(context);
 		Activator.plugin = this;
 		startStartupListener();
 		startExceptionHandling();
 	}
 
 	private void startStartupListener() {
 		final BundleListener bundleListener = new StartupBundleListener();
 		getContext().addBundleListener(bundleListener);
 	}
 
 	private void startExceptionHandling() {
 		final SimpleExceptionHandlerManager handlerManager = new SimpleExceptionHandlerManager();
 		Wire.instance(handlerManager).andStart(getContext());
 		getContext().registerService(IExceptionHandlerManager.class.getName(), handlerManager,
 				RienaConstants.newDefaultServiceProperties());
 	}
 
 	@Override
 	@IgnoreFindBugs(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "that is the eclipse way")
 	public void stop(final BundleContext context) throws Exception {
		Job.getJobManager().cancel(getBundle().getSymbolicName());
 		active = false;
 		startupActionsExecuted = false;
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
 
 	/**
 	 * Riena core bundle status.
 	 * 
 	 * @return {@code true} if this bundle has been started; otherwise
 	 *         {@code false}
 	 */
 	public boolean isActive() {
 		return active;
 	}
 
 	/**
 	 * Are all startup actions executed?
 	 * 
 	 * @return {@code true} if all startup actions have been executed; otherwise
 	 *         {@code false}
 	 * 
 	 * @since 3.0
 	 */
 	public boolean areStartupActionsExecuted() {
 		return startupActionsExecuted;
 	}
 
 	/**
 	 * Listens for riena.core activation and performs startup actions.
 	 */
 	private class StartupBundleListener implements BundleListener {
 
 		public void bundleChanged(final BundleEvent event) {
 			if (Activator.getDefault() == null) {
 				new ConsoleLogger(StartupBundleListener.class.getName()).log(LogService.LOG_WARNING,
 						"Bundle already gone!"); //$NON-NLS-1$
 				return;
 			}
 			try {
 				if (event.getBundle() == getContextSave().getBundle() && event.getType() == BundleEvent.STARTED) {
 					getContextSave().removeBundleListener(this);
 					active = true;
 					logStage();
 					final ISafeRunnable safeRunnable = new StartupsSafeRunnable();
 					Wire.instance(safeRunnable).andStart(getContextSave());
 					SafeRunner.run(safeRunnable);
 					startupActionsExecuted = true;
 				}
 			} catch (final IllegalStateException e) {
 				new ConsoleLogger(StartupBundleListener.class.getName()).log(LogService.LOG_WARNING,
 						"BundleContext already gone!"); //$NON-NLS-1$
 			}
 		}
 
 		private void logStage() {
 			Log4r.getLogger(StartupBundleListener.class).log(LogService.LOG_INFO,
 					"Riena is running in stage '" + RienaStatus.getStage() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		private BundleContext getContextSave() {
 			final BundleContext context = getContext();
 			if (context == null) {
 				throw new IllegalStateException("BundleContext is <null>"); //$NON-NLS-1$
 			}
 			return context;
 		}
 	}
 
 }
