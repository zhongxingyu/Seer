 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.core;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IShutdownListener;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.search.IDLTKSearchConstants;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.SearchPattern;
import org.eclipse.dltk.core.search.TypeNameRequestor;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class RubyPlugin extends Plugin {
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.eclipse.dltk.ruby.core"; //$NON-NLS-1$
 
 	public static final boolean DUMP_EXCEPTIONS_TO_CONSOLE = Boolean
 			.valueOf(
 					Platform
 							.getDebugOption("org.eclipse.dltk.ruby.core/dumpErrorsToConsole")) //$NON-NLS-1$
 			.booleanValue();
 
 	// The shared instance
 	private static RubyPlugin plugin;
 
 	/**
 	 * The constructor
 	 */
 	public RubyPlugin() {
 		plugin = this;
 	}
 
 	/*
 	 * @see Plugins#start(BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 	}
 
 	private final ListenerList shutdownListeners = new ListenerList();
 
 	public void addShutdownListener(IShutdownListener listener) {
 		shutdownListeners.add(listener);
 	}
 
 	/*
 	 * @see Plugin#stop(BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		Object[] listeners = shutdownListeners.getListeners();
 		for (int i = 0; i < listeners.length; ++i) {
 			((IShutdownListener) listeners[i]).shutdown();
 		}
 		shutdownListeners.clear();
 		plugin = null;
 		savePluginPreferences();
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 * 
 	 * @return the shared instance
 	 */
 	public static RubyPlugin getDefault() {
 		return plugin;
 	}
 
 	public static void log(Exception ex) {
 		if (DLTKCore.DEBUG || DUMP_EXCEPTIONS_TO_CONSOLE)
 			ex.printStackTrace();
 		String message = ex.getMessage();
 		if (message == null)
 			message = "(no message)"; //$NON-NLS-1$
 		getDefault().getLog().log(
 				new Status(IStatus.ERROR, PLUGIN_ID, 0, message, ex));
 	}
 
 	public static void log(String message) {
 		if (DLTKCore.DEBUG || DUMP_EXCEPTIONS_TO_CONSOLE)
 			System.out.println(message);
 		getDefault().getLog().log(
 				new Status(IStatus.WARNING, PLUGIN_ID, 0, message, null));
 	}
 
 	/**
 	 * Initializes DLTKCore internal structures to allow subsequent operations
 	 * (such as the ones that need a resolved classpath) to run full speed. A
 	 * client may choose to call this method in a background thread early after
 	 * the workspace has started so that the initialization is transparent to
 	 * the user.
 	 * <p>
 	 * However calling this method is optional. Services will lazily perform
 	 * initialization when invoked. This is only a way to reduce initialization
 	 * overhead on user actions, if it can be performed before at some
 	 * appropriate moment.
 	 * </p>
 	 * <p>
 	 * This initialization runs accross all Java projects in the workspace. Thus
 	 * the workspace root scheduling rule is used during this operation.
 	 * </p>
 	 * <p>
 	 * This method may return before the initialization is complete. The
 	 * initialization will then continue in a background thread.
 	 * </p>
 	 * <p>
 	 * This method can be called concurrently.
 	 * </p>
 	 * 
 	 * @param monitor
 	 *            a progress monitor, or <code>null</code> if progress reporting
 	 *            and cancellation are not desired
 	 * @exception CoreException
 	 *                if the initialization fails, the status of the exception
 	 *                indicates the reason of the failure
 	 * @since 3.1
 	 */
 	public static void initializeAfterLoad(IProgressMonitor monitor)
 			throws CoreException {
 		try {
 			if (monitor != null)
 				monitor
 						.beginTask(Messages.RubyPlugin_initializingDltkRuby,
 								100);
 
 			// dummy query for waiting until the indexes are ready
 			SearchEngine engine = new SearchEngine();
 			IDLTKSearchScope scope = SearchEngine
 					.createWorkspaceScope(RubyLanguageToolkit.getDefault());
 			try {
 				if (monitor != null)
 					monitor
 							.subTask(Messages.RubyPlugin_initializingSearchEngine);
 				engine.searchAllTypeNames(
 						null,
 						SearchPattern.R_EXACT_MATCH,
 						"!@$#!@".toCharArray(), //$NON-NLS-1$
 						SearchPattern.R_PATTERN_MATCH
 								| SearchPattern.R_CASE_SENSITIVE,
 						IDLTKSearchConstants.TYPE,
 						scope,
						new TypeNameRequestor() {
							public void acceptType(int modifiers,
									char[] packageName, char[] simpleTypeName,
									char[][] enclosingTypeNames, String path) {
								// no type to accept
							}
						},
 						// will not activate index query caches if indexes are
 						// not ready, since it would take to long
 						// to wait until indexes are fully rebuild
 						IDLTKSearchConstants.CANCEL_IF_NOT_READY_TO_SEARCH,
 						monitor == null ? null : new SubProgressMonitor(
 								monitor, 49) // 49% of the time is spent in the
 						// dummy search
 						);
 
 				// String[] mainClasses = new String[] {
 				//	"Object", "String", "Fixnum", "Array", "Regexp", "Class", "Kernel" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
 				// for (int i = 0; i < mainClasses.length; i++) {
 				// RubyMixinModel.getInstance().createRubyElement(
 				// mainClasses[i]);
 				// RubyMixinModel.getInstance().createRubyElement(
 				//	mainClasses[i] + "%"); //$NON-NLS-1$
 				// monitor.worked(10);
 				// }
 				// RubyMixinModel.getRawInstance().find("$*"); //$NON-NLS-1$
 				// monitor.worked(10);
 			} catch (ModelException e) {
 				// /search failed: ignore
 			} catch (OperationCanceledException e) {
 				if (monitor != null && monitor.isCanceled())
 					throw e;
 				// else indexes were not ready: catch the exception so that jars
 				// are still refreshed
 			}
 
 		} finally {
 			if (monitor != null)
 				monitor.done();
 		}
 	}
 
 }
