 package org.lunifera.runtime.web.jetty.tests.context;
 
 import static org.knowhowlab.osgi.testing.assertions.BundleAssert.assertBundleAvailable;
 import static org.knowhowlab.osgi.testing.assertions.ServiceAssert.assertServiceAvailable;
 
 import org.knowhowlab.osgi.testing.utils.BundleUtils;
 import org.lunifera.runtime.web.jetty.tests.Activator;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 
 /**
  * Helps checking required bundles
  * 
  * @author admin
  * 
  */
 public class BundleHelper {
 
 	public static void ensureSetup() throws BundleException {
 		ensureNeededBundlesAvailable();
 		ensureNeededServicesAvailable();
 	}
 
 	public static void ensureNeededBundlesAvailable() throws BundleException {
 
 		// check bundles available
 		assertBundleAvailable("org.lunifera.runtime.web.jetty");
		assertBundleAvailable("org.eclipse.equinox.ds");
 		assertBundleAvailable("org.eclipse.equinox.util");
 		assertBundleAvailable("org.eclipse.equinox.cm");
 
 		// stop jetty
 		Bundle jetty = BundleUtils.findBundle(Activator.context,
 				"org.eclipse.equinox.http.jetty");
 		if (jetty != null) {
 			jetty.stop();
 		}
 
 		// start ds
 		Bundle ds = BundleUtils.findBundle(Activator.context,
 				"org.eclipse.equinox.ds");
 		if (ds == null) {
 			throw new IllegalStateException(
 					"Bundle org.eclipse.equinox.ds is missing!");
 		}
 		if (ds.getState() != Bundle.STARTING && ds.getState() != Bundle.ACTIVE) {
 			ds.start();
 		}
 
 		// start cm
 		Bundle cm = BundleUtils.findBundle(Activator.context,
 				"org.eclipse.equinox.cm");
 		if (cm == null) {
 			throw new IllegalStateException(
 					"Bundle org.eclipse.equinox.cm is missing!");
 		}
 		if (cm.getState() != Bundle.STARTING && cm.getState() != Bundle.ACTIVE) {
 			cm.start();
 		}
 	}
 
 	public static void ensureNeededServicesAvailable() throws BundleException {
 		assertServiceAvailable("org.osgi.service.cm.ConfigurationAdmin");
 	}
 
 }
