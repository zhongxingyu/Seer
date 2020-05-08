 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.core.test.util;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.swordfish.internal.core.test.util.Activator;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
 import org.osgi.framework.ServiceRegistration;
 
 /**
  * A test case class for integration testing that will start up required Swordfish bundles
  * upon creation and clean up service registrations after each test.
  */
 public class SwordfishTestCase extends TestCase {
     protected static final Log LOG = LogFactory.getLog(SwordfishTestCase.class);
     protected List<ServiceRegistration> registrationsToCancel = new ArrayList<ServiceRegistration>();
 
 	protected BundleContext bundleContext = null;
 
 	/**
 	 * Create a test case instance
 	 */
 	public SwordfishTestCase() {
 		bundleContext = Activator.getDefault().getContext();
		bundleContext.addBundleListener(new BundleListener() {
 
			@Override
			public void bundleChanged(BundleEvent event) {
				onBundleChanged(event);

			}
		});
 		// Give the framework time to start if one or more bundles had to be started
 		if (startRequiredBundles(bundleContext)) {
 			try {
 				LOG.info("Giving framework time to start up.");
 				Thread.sleep(10000);
 
 			} catch (InterruptedException e) {
 				LOG.error("Startup interrupted", e);
 			}
 		}
 	}
 
 	protected void preStartBundleCallBack(Bundle bundle) {
 
 	}
 
	protected void onBundleChanged(BundleEvent event) {

	}
 
 	/**
 	 * Start not active bundles needed for testing and print state of bundles.
 	 * @param context - a bundle context
 	 * @return true if at least one bundle had to be started, otherwise false
 	 */
 	private boolean startRequiredBundles(BundleContext context) {
 		boolean oneOrMoreBundlesWereStarted = false;
 		List<Pattern> startBundles = getStartBundlePatterns();
 		List<Pattern> excludedBundles = getExcludeBundlePatterns();
 
 		for (Bundle b : context.getBundles()) {
 			printBundleState(b);
 
 			if (!TestCollector.isFragment(b)
 					&& b.getState() != Bundle.ACTIVE) {
 
 				if (bundleNeedsToBeStarted(b, startBundles, excludedBundles)) {
 					try {
 						LOG.info("Start bundle " + b.getSymbolicName());
 						preStartBundleCallBack(b);
 						b.start();
 						LOG.info("Bundle " + b.getSymbolicName() + " is starting!");
 						oneOrMoreBundlesWereStarted = true;
 
 					} catch(Exception ex) {
 						LOG.error("Bundle "+ b.getSymbolicName() + " - not started", ex);
 					}
 				}
 			}
    		}
 
 		return oneOrMoreBundlesWereStarted;
 	}
 
 
 	/**
 	 * Check if a bundle has to be started
 	 * @param b - a bundle
 	 * @return true - if startup is necessary
 	 */
 	protected boolean bundleNeedsToBeStarted(Bundle b, List<Pattern> startBundles, List<Pattern> excludedBundles) {
 		boolean doesNeedStartup = false;
 		String bundleName = b.getSymbolicName();
 
 		for (Iterator<Pattern> patIter = startBundles.iterator();
 				!doesNeedStartup && patIter.hasNext();) {
 			Pattern pat = patIter.next();
 			doesNeedStartup = pat.matcher(bundleName).matches();
 		}
 
 		for (Iterator<Pattern> patIter = excludedBundles.iterator();
 				doesNeedStartup && patIter.hasNext();) {
 			Pattern pat = patIter.next();
 
 			if (pat.matcher(bundleName).matches()) {
 				doesNeedStartup = false;
 			}
 		}
 
 		return doesNeedStartup;
 	}
 
 
 	/**
 	 * Prints the state of a bundle human readable to the log
 	 * @param b - a bundle instance
 	 */
 	private static void printBundleState(Bundle b) {
 		int state = b.getState();
 		String stateStr;
 
 		switch(state) {
 		case Bundle.ACTIVE:
 			stateStr = "Active";
 			break;
 		case Bundle.INSTALLED:
 			stateStr = "Installed";
 			break;
 		case Bundle.UNINSTALLED:
 			stateStr = "Uninstalled";
 			break;
 		case Bundle.STARTING:
 			stateStr = "Starting";
 			break;
 		case Bundle.STOPPING:
 			stateStr = "Stopping";
 			break;
 		case Bundle.RESOLVED:
 			stateStr = "Resolved";
 			break;
 		default:
 			stateStr = "???";
 		}
 
    		LOG.info("Bundle " + b.getSymbolicName() + " - " + stateStr);
 	}
 
 
     /**
      * Add a registration to the internal list to be released during tearDown
      * @param serviceRegistration - the registration to be released
      */
     protected void addRegistrationToCancel(ServiceRegistration serviceRegistration) {
     	registrationsToCancel.add(serviceRegistration);
     }
 
 
     /**
      * Return a list of bundle name patterns that selects bundles to be excluded
      * from being started before test.
      * @return
      */
     protected List<Pattern> getExcludeBundlePatterns() {
         return Arrays.asList(
         	Pattern.compile("org\\.eclipse\\.osgi_.*"),
         	Pattern.compile("org\\.eclipse\\.swordfish\\.samples.*"),
         	Pattern.compile("org\\.eclipse\\.swordfish\\.registry.*"),
         	Pattern.compile("org\\.eclipse\\.swordfish\\.plugins\\.resolver\\.backend\\.remote.*"),
         	Pattern.compile("org\\.eclipse\\.swordfish\\.plugins\\.ws\\.wsdlgenerator\\.t.*"));
     }
 
 
     /**
      * Return a list of bundle name patterns that selects bundles to be started before test.
      * Note that bundles will not be started if an exclude pattern matches.
      * @return
      */
     protected List<Pattern> getStartBundlePatterns() {
     	return Arrays.asList(
            	Pattern.compile("com.sun.xml.bind.*"),
         	Pattern.compile("javax\\.(activation|annotation|jbi|jws|mail|servlet|transaction|wsdl|xml).*"),
         	Pattern.compile("javax\\.xml\\.(bind|soap|stream|ws).*"),
         	Pattern.compile("jaxp\\.ri.*"),
         	Pattern.compile("org\\.aopalliance.*"),
         	Pattern.compile("org\\.apache\\.(ant|axiom|commons|cxf|neethi|servicemix|ws|xbeans).*"),
         	Pattern.compile("org\\.codehaus\\.stax2.*"),
         	Pattern.compile("org\\.eclipse\\.equinox\\.(cm|event).*"),
         	Pattern.compile("org\\.eclipse\\.swordfish.*"),
         	Pattern.compile("org\\.jvnet\\.staxex.*"),
         	Pattern.compile("org\\.mortbay.*"),
         	Pattern.compile("org\\.objectweb.*"),
         	Pattern.compile("org\\.springframework.*"),
         	Pattern.compile("servicemix.*"),
         	Pattern.compile("net\\.sf\\.cglib.*"));
     }
 
 
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@Override
 	protected void tearDown() throws Exception {
 		for (ServiceRegistration sr : registrationsToCancel) {
 			try {
 				sr.unregister();
 			} catch (Exception ex) {
 				LOG.error("Unable to unregister ", ex);
 			}
 		}
 
 		registrationsToCancel.clear();
 	}
 }
