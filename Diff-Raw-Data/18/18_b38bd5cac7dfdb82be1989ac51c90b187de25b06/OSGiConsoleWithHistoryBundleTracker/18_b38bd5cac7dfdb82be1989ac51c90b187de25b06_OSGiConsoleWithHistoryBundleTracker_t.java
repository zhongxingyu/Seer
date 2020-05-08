 package de.jw.equinox.osgi.console.withhistory.activator;
 
 import java.util.logging.Logger;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.util.tracker.BundleTracker;
 import org.osgi.util.tracker.BundleTrackerCustomizer;
 
 public class OSGiConsoleWithHistoryBundleTracker extends BundleTracker {
 	private final Logger log = Logger
 			.getLogger(OSGiConsoleWithHistoryBundleTracker.class.getName());
 
 	public OSGiConsoleWithHistoryBundleTracker(BundleContext context,
 			int stateMask, BundleTrackerCustomizer customizer) {
 		super(context, stateMask, customizer);
 	}
 
 	public Object addingBundle(Bundle bundle, BundleEvent event) {
 		log.fine("OSGiConsoleWithHistoryBundleTracker addingBundle: "
 				+ bundle.getSymbolicName() + ", event: " + event);
		/*
		 * if (bundle.getSymbolicName().equals("org.eclipse.pde.ui")) {
		 * log.info(
		 * "OSGiConsoleWithHistoryBundleTracker about extensions-registration."
		 * );
		 * 
		 * OSGiConsoleWithHistoryPluginXmlRegistration.register();
		 * 
		 * log.info("OSGiConsoleWithHistoryBundleTracker registered extensions.")
		 * ; } else
		 */if (bundle.getSymbolicName().equals("org.eclipse.core.runtime")) {
 			log.info("OSGiConsoleWithHistoryBundleTracker about extensions-registration.");

 			OSGiConsoleWithHistoryPluginXmlRegistration.register();

 			log.info("OSGiConsoleWithHistoryBundleTracker registered extensions.");
 		}
 		return bundle;
 	}
 
 	public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
 		log.fine("OSGiConsoleWithHistoryBundleTracker modifyingBundle: "
 				+ bundle.getSymbolicName() + ", event: " + event);
 		if (bundle.getSymbolicName().equals("org.eclipse.pde.ui")) {
 			OSGiConsoleWithHistoryPluginXmlRegistration.register();
 		}
 	}
 
 }
