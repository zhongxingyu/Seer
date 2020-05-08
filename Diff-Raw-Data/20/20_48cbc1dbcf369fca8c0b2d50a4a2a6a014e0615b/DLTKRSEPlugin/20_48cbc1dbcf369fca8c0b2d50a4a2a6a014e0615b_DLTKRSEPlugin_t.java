 package org.eclipse.dltk.core.internal.rse;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Status;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class DLTKRSEPlugin extends Plugin {
 
	static final String NAMESPACE = "org.eclipse.dltk.rse"; //$NON-NLS-1$

 	// The plug-in ID
	public static final String PLUGIN_ID = NAMESPACE + ".core"; //$NON-NLS-1$
 
 	// The shared instance
 	private static DLTKRSEPlugin plugin;
 
 	/**
 	 * The constructor
 	 */
 	public DLTKRSEPlugin() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
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
 	public static DLTKRSEPlugin getDefault() {
 		return plugin;
 	}
 
 	public static void log(IStatus status) {
 		getDefault().getLog().log(status);
 	}
 
 	public static void log(String message) {
 		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, null));
 	}
 
 	public static void logWarning(String message) {
 		log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.ERROR, message, null));
 	}
 
 	public static void logWarning(Throwable t) {
 		log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.ERROR, t
 				.getMessage(), t));
 	}
 
 	public static void logWarning(String message, Throwable t) {
 		log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.ERROR, message, t));
 	}
 
 	public static void log(Throwable e) {
 		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, e.getMessage(),
 				e));
 	}
 
 	public static void log(String message, Throwable e) {
 		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, e));
 	}
 
 }
