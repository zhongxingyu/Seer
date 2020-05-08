 /*******************************************************************************
  * Copyright (c) 2010 Neil Bartlett.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Neil Bartlett - initial API and implementation
  ******************************************************************************/
 package bndtools.runtime.junit.internal;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Vector;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import junit.framework.Test;
 import junit.framework.TestResult;
 import junit.framework.TestSuite;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.SynchronousBundleListener;
 
 public class Activator implements BundleActivator {
 
 	private static final String HEADER_TEST_SUITES = "Test-Suites";
     private static final String HEADER_TEST_CASES = "Test-Cases";
 
 	private static final int DEFAULT_THREAD_POOL_SIZE = 1;
 	private static final long DEFAULT_START_TIMEOUT = 45; // 45 seconds
 
 	private static final String REPORTER_PORT_PREFIX = "port:";
 	private static final String REPORTER_FILE_PREFIX = "file:";
 
 	private final Logger log = Logger.getLogger(Activator.class.getPackage().getName());
 
 	private BundleContext context;
 	private ScheduledExecutorService threadPool;
 	private TestReporter reporter;
 	private AtomicBoolean testRunnerLock = new AtomicBoolean(false);
 	private boolean keepAlive = false;
 
 	private final SynchronousBundleListener bundleListener = new SynchronousBundleListener() {
 		public void bundleChanged(BundleEvent event) {
 			if(BundleEvent.STARTED == event.getType()) {
 				maybeStartTesting();
 			}
 		}
 	};
 
 	public void start(final BundleContext context) {
 		this.context = context;
 		log.fine("Starting JUnit Activator");
 
 		// Read the keepalive setting
 		String keepAliveStr = context.getProperty(LauncherConstants.PROP_KEEP_ALIVE);
 		keepAlive = !"false".equalsIgnoreCase(keepAliveStr);
 
 		// Create the reporter setting
 		reporter = createTestReporter(context);
 		if(reporter == null) {
 			maybeKillFramework();
 			return;
 		}
 
 		// Create a testing thread pool
 		String threadPoolSizeStr = context.getProperty(LauncherConstants.PROP_THREADPOOL_SIZE);
 		int threadPoolSize = threadPoolSizeStr != null
 			? Integer.parseInt(threadPoolSizeStr)
 			: DEFAULT_THREAD_POOL_SIZE;
 		log.log(Level.FINE, "Creating testing thread pool with {0} thread(s).", threadPoolSize);
 		threadPool = Executors.newScheduledThreadPool(threadPoolSize);
 
 		// Add the bundle listener
 		context.addBundleListener(bundleListener);
 
 		// Perform an initial check
 		maybeStartTesting();
 
 		// Register a timeout to kill the framework if testing fails to start
 		if(!keepAlive) {
 			final long timeout = getStartTimeout(context);
 			Runnable killTask = new Runnable() {
 				public void run() {
 					// Attempt to acquire the test runner lock. If we succeed, the testing hasn't started. We
 					// also prevent the testing from starting after this point.
 					if(testRunnerLock.compareAndSet(false, true)) {
 						log.log(Level.SEVERE, "Unable to start testing after {0}s.", timeout);
 						reporter.aborted();
 						maybeKillFramework();
 					}
 				}
 			};
 			threadPool.schedule(killTask, timeout, TimeUnit.SECONDS);
 		}
 	}
 
 	protected TestReporter createTestReporter(final BundleContext context) {
 		TestReporter reporter = null;
 
 		String reporterSpec = context.getProperty(LauncherConstants.PROP_REPORTER);
 		if(reporterSpec == null) {
 			log.severe("No JUnit reporter was specified; aborting tests.");
 		}  else {
 			if(reporterSpec.startsWith(REPORTER_PORT_PREFIX)) {
 				String portStr = reporterSpec.substring(REPORTER_PORT_PREFIX.length());
 				try {
 					reporter = JUnitPortReporter.createReporter(Integer.parseInt(portStr));
 				} catch (NumberFormatException e) {
 					log.severe("The JUnit port was an invalid numeric string; aborting tests.");
 				}
 			} else if(reporterSpec.startsWith(REPORTER_FILE_PREFIX)) {
 				String fileName = reporterSpec.substring(REPORTER_FILE_PREFIX.length());
 				File outputFile = new File(fileName);
 				reporter = new SAXReporter(outputFile);
 			} else {
 				log.log(Level.SEVERE, "Unknown JUnit reporting specification: {0}", reporterSpec);
 			}
 		}
 		return reporter;
 	}
 
 	protected long getStartTimeout(BundleContext context) {
 		String timeoutStr = context.getProperty(LauncherConstants.PROP_START_TIMEOUT);
 		if(timeoutStr != null) {
 			try {
 				return Long.parseLong(timeoutStr);
 			} catch (NumberFormatException e) {
 			}
 		}
 		return DEFAULT_START_TIMEOUT;
 	}
 
 	void maybeStartTesting() {
 		// Any non-ACTIVE bundle (except for *this* bundle, which may be STARTING, and fragments, which are never ACTIVE)
 	    // should defer the test run
 		for (Bundle bundle : context.getBundles()) {
 			boolean active = Bundle.ACTIVE == bundle.getState();
            boolean thisBundle = bundle.getBundleId() != context.getBundle().getBundleId();
             if(!active && !thisBundle && !isFragment(bundle)) {
 				log.log(Level.INFO, "Deferring JUnit run, bundle \"{0}\" (and perhaps others) is not yet active.", bundle.getSymbolicName());
 				return;
 			}
 		}
 		log.fine("All bundles have reached ACTIVE state, triggering start of JUnit testing.");
 
 		// We can remove the listener and start testing
 		Runnable task = new Runnable() {
 			public void run() {
 				doTesting();
 			}
 		};
 		log.fine("Submitting JUnit test execution task");
 		threadPool.submit(task);
 		context.removeBundleListener(bundleListener);
 	}
 
 	boolean isFragment(Bundle bundle) {
 	    return bundle.getHeaders().get(Constants.FRAGMENT_HOST) != null;
     }
 
     void doTesting() {
 		if(!testRunnerLock.compareAndSet(false, true)) {
 			// Somebody beat us to it, or the test run timed out.
 			return;
 		}
 		log.info("Starting tests");
 
 		Bundle[] bundles = context.getBundles();
 		TestResult result = new TestResult();
 
 		try {
 			Vector<Test> tests = new Vector<Test>();
 			for (Bundle bundle : bundles) {
 				addBundleSuites(tests, bundle);
 			}
 			List<Test> flattenedTests = new ArrayList<Test>();
 
 			int realCount = flattenTests(flattenedTests, tests.elements());
 			reporter.begin(bundles, flattenedTests, realCount);
 			result.addListener(reporter);
 
 			// Run the bundle suites
 			for (Test test : tests) {
 				test.run(result);
 			}
 		} catch (Exception e) {
 			result.addError(null, e);
 		} finally {
 			reporter.end();
 			log.info("Testing complete.");
 			maybeKillFramework();
 		}
 	}
 
 	/**
 	 * Shuts down the OSGi framework unless keepAlive is enabled.
 	 */
 	void maybeKillFramework() {
 		if(!keepAlive) {
 			log.info("SHUTTING DOWN OSGi FRAMEWORK.");
 			try {
 				context.getBundle(0).stop();
 			} catch (BundleException e) {
 				log.log(Level.SEVERE, "Failed to shutdown OSGi Framework.", e);
 			}
 		}
 	}
 
 	public void stop(BundleContext context) {
 		if(threadPool != null)
 			threadPool.shutdown();
 		log.fine("Stopping JUnit activator");
 	}
 
     public static void addBundleSuites(Collection<? super Test> suites, Bundle bundle) {
         String suitesStr = (String) bundle.getHeaders().get(HEADER_TEST_SUITES);
         addBundleSuites(suites, bundle, suitesStr);
         String casesStr = (String) bundle.getHeaders().get(HEADER_TEST_CASES);
         addBundleSuites(suites, bundle, casesStr);
     }
 
     static void addBundleSuites(Collection<? super Test> suites, Bundle bundle, String suitesStr) {
         if (suitesStr != null && suitesStr.trim().length() > 0) {
             String[] names = suitesStr.split(",");
             for (String name : names) {
                 name = name.trim();
                 if (name.length() > 0) {
                     try {
                         Class<?> clazz = bundle.loadClass(name);
                         suites.add(new TestSuite(clazz));
                     } catch (ClassNotFoundException e) {
                         suites.add(new InvalidTest(name, e));
                     }
                 }
             }
         }
     }
 
 	static int flattenTests(Collection<? super Test> output, Enumeration<? extends Test> input) {
 		int count = 0;
 		while(input.hasMoreElements()) {
 			Test inputTest = input.nextElement();
 			output.add(inputTest);
 
 			if(inputTest instanceof TestSuite) {
 				Enumeration<? extends Test> children = ((TestSuite) inputTest).tests();
 				count += flattenTests(output, children);
 			} else {
 				count ++;
 			}
 		}
 		return count;
 	}
 }
