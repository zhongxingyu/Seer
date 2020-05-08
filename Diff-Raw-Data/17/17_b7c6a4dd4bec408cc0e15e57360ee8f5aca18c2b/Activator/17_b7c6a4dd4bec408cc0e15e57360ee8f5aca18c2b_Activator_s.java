 package org.eclipse.ecf.internal.provider.jms.activemq;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.ecf.core.util.LogHelper;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator implements BundleActivator {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.eclipse.ecf.provider.jms.activemq"; //$NON-NLS-1$
 
 	// The shared instance
 	private static Activator plugin;
 
 	private BundleContext context = null;
 
	private ServiceTracker logServiceTracker = null;
 
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 	}
 
 	protected LogService getLogService() {
 		if (logServiceTracker == null) {
			logServiceTracker = new ServiceTracker(this.context,
					LogService.class.getName(), null);
 			logServiceTracker.open();
 		}
 		return (LogService) logServiceTracker.getService();
 	}
 
 	public void log(IStatus status) {
 		LogService logService = getLogService();
 		if (logService != null) {
 			logService.log(LogHelper.getLogCode(status), LogHelper
 					.getLogMessage(status), status.getException());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		plugin = this;
 		this.context = context;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		if (logServiceTracker != null) {
 			logServiceTracker.close();
 			logServiceTracker = null;
 		}
 		plugin = null;
 		this.context = null;
 	}
 
 	/**
 	 * Returns the shared instance
 	 * 
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 }
