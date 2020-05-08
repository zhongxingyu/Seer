 /* Copyright (c) 2006 Jan S. Rellermeyer
  * Information and Communication Systems Research Group (IKS),
  * Institute for Pervasive Computing, ETH Zurich.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of ETH Zurich nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package ch.ethz.iks.concierge.framework;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.security.AccessController;
 import java.security.Permission;
 import java.security.PrivilegedAction;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Dictionary;
 import java.util.EventListener;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.net.URL;
 import org.osgi.framework.AdminPermission;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.BundleListener;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Filter;
 import org.osgi.framework.FrameworkEvent;
 import org.osgi.framework.FrameworkListener;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServicePermission;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.framework.SynchronousBundleListener;
 import org.osgi.service.log.LogService;
 import org.osgi.service.packageadmin.ExportedPackage;
 import org.osgi.service.packageadmin.PackageAdmin;
 import org.osgi.service.startlevel.StartLevel;
 
 /**
  * The core class of the Concierge OSGi framework. Maintains the central bundle
  * and service registry.
  * 
  * @author Jan S. Rellermeyer, ETH Zurich
  */
 public final class Framework {
 
 	// the runtime args
 
 	/**
 	 * framework basedir.
 	 */
 	private static String BASEDIR;
 
 	/**
 	 * bundle location.
 	 */
 	private static String BUNDLE_LOCATION = "file:.";
 
 	/**
 	 * the location where the storage resides.
 	 */
 	static String STORAGE_LOCATION;
 
 	/**
 	 * classloader buffer size.
 	 */
 	static int CLASSLOADER_BUFFER_SIZE;
 
 	/**
 	 * logging enabled.
 	 */
 	static boolean LOG_ENABLED = true;
 
 	/**
 	 * log service.
 	 */
 	static LogService logger;
 
 	/**
 	 * log buffer size.
 	 */
 	static int LOG_BUFFER_SIZE;
 
 	/**
 	 * log quiet ? (= no logging to System.out)
 	 */
 	static boolean LOG_QUIET;
 
 	/**
 	 * decompress bundles with embedded jars.
 	 */
 	static boolean DECOMPRESS_EMBEDDED;
 
 	/**
 	 * log level.
 	 */
 	static int LOG_LEVEL;
 
 	/**
 	 * perform strict startup, that means, stop if any bundle throws an
 	 * exception during startup ?
 	 */
 	private static boolean STRICT_STARTUP;
 
 	/**
 	 * security.
 	 */
 	static boolean SECURITY_ENABLED;
 
 	/**
 	 * enable deep equality check on service listeners when adding and removing
 	 * from runtime instance. This is sometimes nessary due to the OSGi R3
 	 * implementation of the ServiceTracker. Different ServiceTrackers evaluate
 	 * as equal because they subclass Hashtable. Different Hashtables evaluate
 	 * to equal if they are empty.
 	 */
 	static boolean DEEP_SERVICE_LISTENER_CHECK;
 
 	/**
 	 * debug outputs from bundles ?
 	 */
 	static boolean DEBUG_BUNDLES;
 
 	/**
 	 * debug outputs from packages ?
 	 */
 	static boolean DEBUG_PACKAGES;
 
 	/**
 	 * debug outputs from class loading ?
 	 */
 	static boolean DEBUG_CLASSLOADING;
 
 	/**
 	 * debug outputs from services ?
 	 */
 	static boolean DEBUG_SERVICES;
 
 	/**
 	 * the profile.
 	 */
 	private static String PROFILE;
 
 	/**
 	 * Version displayed upon startup and returned by System Bundle
 	 */
 	private static final String FRAMEWORK_VERSION = "1.0_RC1";
 
 	// registry data structures
 
 	/**
 	 * the bundles.
 	 */
 	static List bundles = new ArrayList(2);
 
 	/**
 	 * bundleID -> bundle.
 	 */
 	static Map bundleID_bundles = new HashMap(2);
 
 	/**
 	 * location -> bundle.
 	 */
 	static Map location_bundles = new HashMap(2);
 
 	/**
 	 * the services.
 	 */
 	static List services = new ArrayList(3);
 
 	/**
 	 * class name string -> service.
 	 */
 	private static Map classes_services = new HashMap(3);
 
 	/**
 	 * bundle listeners.
 	 */
 	private static List bundleListeners = new ArrayList(1);
 
 	/**
 	 * synchronous bundle listeners.
 	 */
 	private static List syncBundleListeners = new ArrayList(1);
 
 	/**
 	 * service listeners.
 	 */
 	private static List serviceListeners = new ArrayList(1);
 
 	/**
 	 * framework listeners.
 	 */
 	private static List frameworkListeners = new ArrayList(1);
 
 	/**
 	 * exported packages.
 	 */
 	static Map exportedPackages = new HashMap(1);
 
 	// the system bundle
 
 	/**
 	 * system bundle.
 	 */
 	private static SystemBundle systemBundle;
 
 	// the fields
 
 	/**
 	 * properties.
 	 */
 	static Properties properties;
 
 	/**
 	 * next bundle ID.
 	 */
 	private static long nextBundleID = 1;
 
 	/**
 	 * the framework start level.
 	 */
 	static int startlevel = 0;
 
 	/**
 	 * the initial startlevel for installed bundles.
 	 */
 	static int initStartlevel = 1;
 
 	/**
 	 * 
 	 */
 	static boolean frameworkStartupShutdown = false;
 
 	/**
 	 * framework thread.
 	 */
 	private static Thread frameworkThread;
 
 	/**
 	 * restart ?
 	 */
 	static boolean restart = false;
 
 	// constants
 
 	/**
 	 * the admin permission.
 	 */
 	private static final AdminPermission ADMIN_PERMISSION = new AdminPermission();
 
 	/**
 	 * start method.
 	 * 
 	 * @param args
 	 *            command line arguments.
 	 * @throws Throwable
 	 *             if something goes wrong.
 	 */
 	public static void main(final String[] args) throws BundleException {
 		final String profile;
 		if (args.length > 0) {
 			String p = args[0];
 			p.replace('\n', ' ').trim();
 			if ("".equals(p)) {
 				profile = null;
 			} else {
 				profile = p;
 			}
 		} else {
 			profile = null;
 		}
 		Framework.startup(profile);
 	}
 
 	/**
 	 * hidden defautlt constructor.
 	 */
 	private Framework() {
 	}
 
 	/*
 	 * startup and shutdown related methods
 	 */
 
 	/**
 	 * launch the framework.
 	 * 
 	 * @param profileName
 	 *            true is a restart is requested.
 	 * @throws Throwable
 	 */
 	private static void startup(final String profileName)
 			throws BundleException {
 		{
 			frameworkStartupShutdown = true;
 			frameworkThread = Thread.currentThread();
 			properties = System.getProperties();
 
 			System.out.println("------------------"
 					+ "---------------------------------------");
 			System.out.println("  Concierge OSGi " + FRAMEWORK_VERSION + " on "
 					+ properties.get("os.name") + " "
 					+ properties.get("os.version") + " starting ...");
 			System.out.println("-------------------"
 					+ "--------------------------------------");
 
 			int maxlevel = 1;
 			int target = 0;
 			long time = 0;
 
 			if (profileName != null && !"".equals(profileName)) {
 				time = System.currentTimeMillis();
 				PROFILE = profileName;
 				initialize();
 				launch();
 				target = restoreProfile();
 				if (target != -1) {
 					restart = true;
 				}
 			}
 
 			if (profileName == null || target == -1) {
 				// parse property file, if exists
 				final File propertyFile = new File(System.getProperty(
 						"properties", "." + File.separator
 								+ "system.properties"));
 				if (propertyFile.exists()) {
 					processPropertyFile(propertyFile);
 					initialize();
 				} else if (System.getProperty("properties") != null) {
 					warning("Property file " + System.getProperty("properties")
 							+ " not found");
 				}
 				
 				// parse init.xargs style file, if exists
 				final File startupFile = new File(System.getProperty("xargs",
 						"." + File.separatorChar + "init.xargs"));
 				if (startupFile.exists()) {
 					maxlevel = processXargsFile(startupFile);
 					initialize();
 				} else if (System.getProperty("xargs") != null) {
 					warning("xargs file " + System.getProperty("xargs")
 							+ " not found");
 				}
 				
 				if (!(startupFile.exists() || propertyFile.exists())) {
 					initialize();
 				}
 				
 				PROFILE = properties.getProperty("osgi.profile", "default");
 				launch();
				
				System.out.println("PROFILE IS " + PROFILE);
 
 				// if profile set, try to restart the profile
 				target = -1;
 				boolean init = getProperty("osgi.init", false);
 				if (!init) {
 					time = System.currentTimeMillis();
 					target = restoreProfile();
 					restart = true;
 				}
 
 				if (target == -1) {
 					restart = false;
 					File storage = new File(STORAGE_LOCATION);
 					if (init) {
 						if (storage.exists()) {
 							System.out.println("purging storage ...");
 							deleteDirectory(storage);
 						}
 					}
 
 					storage.mkdirs();
 
 					// TO THE ACTUAL WORK
 					time = System.currentTimeMillis();
 					properties.setProperty("osgi.auto.install.1", properties
 							.getProperty("osgi.auto.install.1", "")
 							+ " "
 							+ properties.getProperty("osgi.auto.install", ""));
 					properties.setProperty("osgi.auto.start.1", properties
 							.getProperty("osgi.auto.start.1", "")
 							+ " "
 							+ properties.getProperty("osgi.auto.start", ""));
 
 					int level = 1;
 					String install;
 					String start;
 					maxlevel = Integer.getInteger("osgi.maxLevel",
 							new Integer(maxlevel)).intValue();
 
 					do {
 						install = properties.getProperty("osgi.auto.install."
 								+ level);
 						start = properties.getProperty("osgi.auto.start."
 								+ level);
 
 						final String[] str = { install, start };
 						for (int i = 0; i < 2; i++) {
 							if (str[i] != null) {
 								StringTokenizer tokenizer = new StringTokenizer(
 										str[i], " ");
 								while (tokenizer.hasMoreTokens()) {
 									try {
 										final String location = tokenizer
 												.nextToken();
 
 										if (!isBundle(location)) {
 											System.out
 													.println("IGNORING NON-BUNDLE "
 															+ location);
 											continue;
 										}
 										System.out.println("INSTALLING "
 												+ location);
 										BundleImpl bundle = installNewBundle(location);
 										bundle.currentStartlevel = level;
 										if (i == 1) {
 											bundle.persistently = true;
 										}
 									} catch (BundleException be) {
 										if (STRICT_STARTUP) {
 											throw be;
 										} else {
 											be.printStackTrace();
 											be.getNestedException()
 													.printStackTrace();
 										}
 									}
 								}
 							}
 						}
 						level++;
 					} while (install != null || start != null
 							|| level < maxlevel);
 
 					initStartlevel = getProperty("osgi.startlevel.bundle", 1);
 					target = getProperty("osgi.startlevel.framework", 1);
 				}
 			}
 			// set startlevel and start all bundles that are marked to be
 			// started up to the intended startlevel
 			systemBundle.setLevel((Bundle[]) bundles.toArray(new Bundle[bundles
 					.size()]), target, false);
 			frameworkStartupShutdown = false;
 
 			// save the metadata
 			if (!restart) {
 				storeProfile();
 			}
 
 			final float timediff = (System.currentTimeMillis() - time)
 					/ (float) 1000.00;
 			System.out.println("-----------------------"
 					+ "----------------------------------");
 			System.out.println("  Framework "
 					+ (restart ? "restarted" : "started") + " in " + timediff
 					+ " seconds.");
 			System.out.println("---------------------------"
 					+ "------------------------------");
 			System.out.flush();
 
 			systemBundle.state = Bundle.ACTIVE;
 			notifyFrameworkListeners(FrameworkEvent.STARTED, systemBundle, null);
 
 		}
 
 		synchronized (frameworkThread) {
 			try {
 				frameworkThread.wait();
 			} catch (InterruptedException e) {
 				// we have been interrupted.
 			}
 		}
 	}
 
 	/**
 	 * Given a file path, determine if file is a valid OSGi bundle.
 	 * 
 	 * @param location
 	 * @return
 	 */
 	private static boolean isBundle(final String location) {
 		// TODO Perhaps do real validation here by looking in the jar and seeing
 		// if the Manifest is valid.
 		//
 		// #rjan: opening the bundle and checking the Manifest is quite costy.
 		// A fail-early strategy might significantly slow down the startup.
 		// If the case that invalid bundles occur is rather rare, I would not
 		// introduce too much checking.#
 
 		if (location.toUpperCase().endsWith(".JAR")
 				|| location.toUpperCase().endsWith(".ZIP")) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * restart the framework.
 	 * 
 	 * @param restart
 	 *            true is a restart is requested.
 	 */
 	static void shutdown(final boolean restart) {
 		System.out.println("----------------------------"
 				+ "-----------------------------");
 		System.out.println("  Concierge OSGi shutting down ...");
 		System.out.println("  Bye !");
 		System.out.println("----------------------------"
 				+ "-----------------------------");
 
 		systemBundle.state = Bundle.STOPPING;
 		systemBundle.setLevel((Bundle[]) bundles.toArray(new Bundle[bundles
 				.size()]), 0, true);
 
 		bundles.clear();
 		bundleID_bundles.clear();
 		systemBundle.state = Bundle.UNINSTALLED;
 
 		synchronized (frameworkThread) {
 			frameworkThread.notify();
 		}
 
 		if (!restart) {
 			System.exit(0);
 		} else {
 			try {
 				bundleID_bundles.put(new Long(0), systemBundle);
 				startup(PROFILE);
 			} catch (Throwable e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private static void initialize() {
 		BASEDIR = properties.getProperty("ch.ethz.iks.concierge.basedir", ".");
 		BUNDLE_LOCATION = properties.getProperty("ch.ethz.iks.concierge.jars",
 				properties.getProperty("org.knopflerfish.gosg.jars", "file:"
 						+ BASEDIR));
 		STORAGE_LOCATION = properties.getProperty(
 				"ch.ethz.iks.concierge.storage", BASEDIR + File.separatorChar
 						+ "storage")
 				+ File.separatorChar + PROFILE + File.separatorChar;
 		CLASSLOADER_BUFFER_SIZE = getProperty(
 				"ch.ethz.iks.concierge.classloader.buffersize", 2048);
 		LOG_ENABLED = getProperty("ch.ethz.iks.concierge.log.enabled", false);
 		LOG_QUIET = getProperty("ch.ethz.iks.concierge.log.quiet", false);
 		LOG_BUFFER_SIZE = getProperty("ch.ethz.iks.concierge.log.buffersize",
 				10);
 		LOG_LEVEL = getProperty("ch.ethz.iks.concierge.log.level",
 				LogService.LOG_ERROR);
 		DEBUG_BUNDLES = getProperty("ch.ethz.iks.concierge.debug.bundles",
 				false);
 		DEBUG_PACKAGES = getProperty("ch.ethz.iks.concierge.debug.packages",
 				false);
 		DEBUG_SERVICES = getProperty("ch.ethz.iks.concierge.debug.services",
 				false);
 		DEBUG_CLASSLOADING = getProperty(
 				"ch.ethz.iks.concierge.debug.classloading", false);
 		if (getProperty("ch.ethz.iks.concierge.debug", false)) {
 			System.out.println("SETTING ALL DEBUG FLAGS");
 			LOG_ENABLED = true;
 			LOG_LEVEL = LogService.LOG_DEBUG;
 			DEBUG_BUNDLES = true;
 			DEBUG_PACKAGES = true;
 			DEBUG_SERVICES = true;
 			DEBUG_CLASSLOADING = true;
 			LOG_LEVEL = 4;
 		}
 		STRICT_STARTUP = getProperty("ch.ethz.iks.concierge.strictStartup",
 				false);
 		DECOMPRESS_EMBEDDED = getProperty(
 				"ch.ethz.iks.concierge.decompressEmbedded", true);
 		SECURITY_ENABLED = getProperty(
 				"ch.ethz.iks.concierge.security.enabled", false);
 		DEEP_SERVICE_LISTENER_CHECK = getProperty(
 				"ch.ethz.iks.concierge.deepServiceListenerCheck", false);
 		
 		// sanity checks
 		if (!LOG_ENABLED) {
 			if (DEBUG_BUNDLES || DEBUG_PACKAGES || DEBUG_SERVICES
 					|| DEBUG_CLASSLOADING) {
 				System.err.println("Logger disabled, ignoring debug flags.");
 				DEBUG_BUNDLES = false;
 				DEBUG_PACKAGES = false;
 				DEBUG_SERVICES = false;
 				DEBUG_CLASSLOADING = false;
 			}
 		}
 		if (System.getSecurityManager() == null) {
 			if (SECURITY_ENABLED) {
 				warning("No security manager set, ignoring security flag.");
 				SECURITY_ENABLED = false;
 			}
 		}
 		// set framework properties
 		Object obj;
 		properties.put("org.osgi.framework.os.name", (obj = properties
 				.get("os.name")) != null ? obj : "undefined");
 		properties.put("org.osgi.framework.os.version", (obj = properties
 				.get("os.version")) != null ? obj : "undefined");
 		properties.put("org.osgi.framework.processor", (obj = properties
 				.get("os.arch")) != null ? obj : "undefined");
 		properties.put("org.osgi.framework.version", "1.2");
 		properties.put("org.osgi.framework.vendor", "concierge");
 		final String lang = java.util.Locale.getDefault().getLanguage();
 		properties.put("org.osgi.framework.language", lang != null ? lang
 				: "en");
 
 		// try to set the properties. Does not work on all platforms
 		try {
 			System.setProperties(properties);
 			properties = System.getProperties();
 		} catch (Throwable t) {
 			System.err
 					.println("VM does not support the setting of system properties.");
 		}
 	}
 
 	/**
 	 * create the setup with the properties and the internal framework flags.
 	 */
 	private static void launch() {
 		// create the system bundle
 		systemBundle = new SystemBundle();
 		systemBundle.state = Bundle.STARTING;
 	}
 
 	/**
 	 * get a boolean property.
 	 * 
 	 * @param key
 	 *            the key.
 	 * @param defaultVal
 	 *            the default.
 	 * @return the value.
 	 */
 	private static boolean getProperty(final String key,
 			final boolean defaultVal) {
 		final String val = (String) properties.get(key);
 		return val != null ? Boolean.valueOf(val).booleanValue() : defaultVal;
 	}
 
 	/**
 	 * get an int property.
 	 * 
 	 * @param key
 	 *            the key.
 	 * @param defaultVal
 	 *            the default.
 	 * @return the value.
 	 */
 	private static int getProperty(final String key, final int defaultVal) {
 		final String val = (String) properties.get(key);
 		return val != null ? Integer.parseInt(val) : defaultVal;
 	}
 
 	/**
 	 * process an init.xargs-style file.
 	 * 
 	 * @param file
 	 *            the file.
 	 * @return the startlevel.
 	 * @throws Throwable
 	 *             if something goes wrong. For example, if strict startup is
 	 *             set and the installation of a bundle fails.
 	 */
 	private static int processXargsFile(final File file) {
 		int maxLevel = 1;
 
 		try {
 			final BufferedReader reader = new BufferedReader(
 					new InputStreamReader(new FileInputStream(file)));
 
 			String token;
 			int initLevel = 1;
 
 			final HashMap startMap = new HashMap();
 			final HashMap installMap = new HashMap();
 			final HashMap memory = new HashMap();
 
 			while ((token = reader.readLine()) != null) {
 				token = token.trim();
 				if (token.equals("")) {
 					continue;
 				} else if (token.charAt(0) == '#') {
 					continue;
 				} else if (token.startsWith("-D")) {
 					token = getArg(token, 2);
 					// get key and value
 					int pos = token.indexOf("=");
 					if (pos > -1) {
 						String key = token.substring(0, pos);
 						String value = token.substring(pos + 1);
 						properties.put(key, value);
 					}
 					continue;
 				} else if (token.startsWith("-profile")) {
 					token = getArg(token, 8);
 					properties.setProperty("osgi.profile", token);
 					continue;
 				} else if (token.equals("-init")) {
 					properties.setProperty("osgi.init", "true");
 				} else if (token.startsWith("-initlevel")) {
 					token = getArg(token, 10);
 					initLevel = Integer.parseInt(token);
 					if (initLevel > maxLevel) {
 						maxLevel = initLevel;
 					}
 					continue;
 				} else if (token.startsWith("-all")) {
 					final File files[];
 					final File jardir = new File(new URL(BUNDLE_LOCATION)
 							.getFile());
 					files = jardir.listFiles(new FilenameFilter() {
 						public boolean accept(File arg0, String arg1) {
 							return arg1.toUpperCase().endsWith(".JAR")
 									|| arg1.toUpperCase().endsWith(".ZIP");
 						}
 					});
 					if (files == null) {
 						warning("NO FILES FOUND IN " + BUNDLE_LOCATION);
 						break;
 					}
 
 					final Integer level = new Integer(initLevel);
 					ArrayList list = (ArrayList) startMap.get(level);
 					if (list == null) {
 						list = new ArrayList();
 					}
 					for (int i = 0; i < files.length; i++) {
 						if (files[i].isDirectory()) {
 							continue;
 						}
 						list.add(files[i].getName());
 					}
 					startMap.put(level, list);
 					continue;
 				} else if (token.startsWith("-startlevel")) {
 					token = getArg(token, 11);
 					properties.setProperty("osgi.startlevel.framework", token);
 				} else if (token.startsWith("-istart")) {
 					token = getArg(token, 7);
 					addValue(startMap, new Integer(initLevel), token);
 				} else if (token.startsWith("-install")) {
 					token = getArg(token, 8);
 					// preliminarily add to install list. But it could be, that
 					// we
 					// find a -start command later on. Then, we have to move the
 					// entry to the start list. Processing the xargs is a real
 					// nightmare :-)
 					final Integer level = new Integer(initLevel);
 					addValue(installMap, level, token);
 					// keep track of the entry
 					memory.put(token, level);
 				} else if (token.startsWith("-start")) {
 					token = getArg(token, 6);
 					// okay, it happened. We should have already added the entry
 					// to
 					// the install list, so remove it there and add to start
 					// list
 					Integer level = (Integer) memory.remove(token);
 					if (level == null) {
 						System.err.println("Bundle " + token
 								+ " is marked to be started but has not been "
 								+ "installed before. Ignoring the command !");
 						removeValue(installMap, new Object[] { level }, token);
 						addValue(startMap, level, token);
 					}
 				}
 			}
 			reader.close();
 
 			// transform the gathered information into properties
 			StringBuffer buffer = new StringBuffer();
 
 			final String[] propName = { "osgi.auto.install.",
 					"osgi.auto.start." };
 			final Map[] maps = { installMap, startMap };
 
 			for (int i = 0; i < 2; i++) {
 				for (int j = 1; j <= maxLevel; j++) {
 					final ArrayList list = (ArrayList) maps[i].get(new Integer(
 							j));
 					if (list != null) {
 						String[] entries = (String[]) list
 								.toArray(new String[list.size()]);
 						buffer.setLength(0);
 						for (int k = 0; k < entries.length; k++) {
 							buffer.append(" ");
 							buffer.append(entries[k]);
 						}
 						final String existing = properties.getProperty(
 								propName[i] + j, "");
 						properties.setProperty(propName[i] + j, existing
 								+ buffer.toString());
 					}
 				}
 			}
 
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 
 		return maxLevel;
 	}
 
 	/**
 	 * write a warning or throw an Exception
 	 * 
 	 * @param message
 	 * @throws BundleException
 	 */
 	private static void warning(String message) throws RuntimeException {
 		if (getProperty("ch.ethz.iks.concierge.strictStartup", false)) {
 			throw new RuntimeException(message);
 		}
 		System.err.println("WARNING: " + message);
 	}
 
 	/**
 	 * process a property file.
 	 * 
 	 * @param file
 	 *            the file
 	 * @return the start level
 	 */
 	private static void processPropertyFile(final File file) {
 		try {
 			final BufferedReader reader = new BufferedReader(
 					new InputStreamReader(new FileInputStream(file)));
 			String token;
 			while ((token = reader.readLine()) != null) {
 				token = token.trim();
 				if (token.equals("") || token.startsWith("#")) {
 					continue;
 				}
 				final int pos = token.indexOf("=");
 				if (pos > -1) {
 					String key = token.substring(0, pos).trim();
 					String value = token.substring(pos + 1).trim();
 					while (value.endsWith("\\")) {
 						value = value.substring(0, value.length() - 1)
 								+ reader.readLine();
 					}
 					if (key.startsWith("oscar")) {
 						key = "osgi" + key.substring(5);
 					}
 					properties.put(key, value);
 				}
 			}
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 
 	/**
 	 * get the argument from a start list entry.
 	 * 
 	 * @param entry
 	 *            the entry.
 	 * @param cmdLength
 	 *            length of command.
 	 * @return the argument.
 	 */
 	private static String getArg(final String entry, final int cmdLength) {
 		// strip command
 		final String str = entry.substring(cmdLength);
 		// strip comments
 		int pos = str.indexOf("#");
 		return pos > -1 ? str.substring(0, pos).trim() : str.trim();
 	}
 
 	/**
 	 * store the profile.
 	 * 
 	 */
 	private static void storeProfile() {
 		final BundleImpl[] bundleArray = (BundleImpl[]) bundles
 				.toArray(new BundleImpl[bundles.size()]);
 		for (int i = 0; i < bundleArray.length; i++) {
 			bundleArray[i].updateMetadata();
 		}
 		storeMetadata();
 	}
 
 	/**
 	 * store the framework metadata.
 	 * 
 	 */
 	static void storeMetadata() {
 		try {
 			final DataOutputStream out = new DataOutputStream(
 					new FileOutputStream(new File(STORAGE_LOCATION, "meta")));
 			out.writeInt(startlevel);
 			out.writeLong(nextBundleID);
 			out.close();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 
 	/**
 	 * restore a profile.
 	 * 
 	 * @return the startlevel or -1 if the profile could not be restored.
 	 */
 	private static int restoreProfile() {
 		try {
 			System.out.println("restoring profile " + PROFILE);
 			final File file = new File(STORAGE_LOCATION, "meta");
 			if (!file.exists()) {
 				System.out.println("Profile " + PROFILE
 						+ " not found, performing clean start ...");
 				return -1;
 			}
 
 			final DataInputStream in = new DataInputStream(new FileInputStream(
 					file));
 			final int targetStartlevel = in.readInt();
 			nextBundleID = in.readLong();
 			in.close();
 
 			final File storageDir = new File(STORAGE_LOCATION);
 			final File[] bundleDirs = storageDir.listFiles();
 
 			for (int i = 0; i < bundleDirs.length; i++) {
 				if (bundleDirs[i].isDirectory()) {
 					final File meta = new File(bundleDirs[i], "meta");
 					if (meta.exists()) {
 						try {
 							final BundleImpl bundle = new BundleImpl(meta,
 									new BundleContextImpl());
 							System.out.println("RESTORED BUNDLE "
 									+ bundle.location);
 							bundles.add(bundle);
 							bundleID_bundles.put(new Long(bundle.bundleID),
 									bundle);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 			return targetStartlevel;
 
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 
 		return 0;
 	}
 
 	/**
 	 * delete a directory with all subdirs.
 	 * 
 	 * @param path
 	 *            the directory.
 	 */
 	static void deleteDirectory(final File path) {
 		final File[] files = path.listFiles();
 		for (int i = 0; i < files.length; i++) {
 			if (files[i].isDirectory()) {
 				deleteDirectory(files[i]);
 			} else {
 				files[i].delete();
 			}
 		}
 		path.delete();
 	}
 
 	/*
 	 * framework operations
 	 */
 
 	/**
 	 * check if the user has admin permissions.
 	 */
 	static void checkAdminPermission() {
 		AccessController.checkPermission(ADMIN_PERMISSION);
 	}
 
 	/**
 	 * install a bundle.
 	 * 
 	 * @param location
 	 *            the bundle location.
 	 * @return a Bundle object.
 	 * @throws BundleException
 	 *             if the installation failed.
 	 */
 	static BundleImpl installNewBundle(final String location)
 			throws BundleException {
 		try {
 			final String location2 = location.indexOf(":") > -1 ? location
 					: BUNDLE_LOCATION + File.separatorChar + location;
 			return installNewBundle(location2, new URL(location2)
 					.openConnection().getInputStream());
 		} catch (IOException e) {
 			throw new BundleException(
 					"Cannot retrieve bundle from " + location, e);
 		}
 	}
 
 	/**
 	 * install a bundle from input stream.
 	 * 
 	 * @param location
 	 *            the bundle location.
 	 * @param in
 	 *            the input stream.
 	 * @return a Bundle object.
 	 * @throws BundleException
 	 *             if the installation failed.
 	 */
 	static BundleImpl installNewBundle(final String location,
 			final InputStream in) throws BundleException {
 		/*
 		 * <specs page="58">Every bundle is uniquely identified by its location
 		 * string. If an installed bundle is using the specified location, the
 		 * installBundle method must return the Bundle object for that installed
 		 * bundle and not install a new bundle.</specs>
 		 */
 		final BundleImpl cached;
 		if ((cached = (BundleImpl) location_bundles.get(location)) != null) {
 			return cached;
 		}
 
 		final BundleImpl bundle = new BundleImpl(location, nextBundleID++,
 				new BundleContextImpl(), in);
 		bundles.add(bundle);
 		bundleID_bundles.put(new Long(bundle.getBundleId()), bundle);
 		location_bundles.put(location, bundle);
 		storeMetadata();
 		return bundle;
 	}
 
 	/**
 	 * unregister a service.
 	 * 
 	 * @param sref
 	 *            the service reference.
 	 */
 	static void unregisterService(final ServiceReference sref) {
 
 		services.remove(sref);
 
 		// remove all class entries
 		removeValue(classes_services, (String[]) sref
 				.getProperty(Constants.OBJECTCLASS), sref);
 
 		final BundleImpl bundle = (BundleImpl) sref.getBundle();
 		bundle.registeredServices.remove(sref);
 
 		// dispose list, if empty
 		if (bundle.registeredServices.isEmpty()) {
 			bundle.registeredServices = null;
 		}
 
 		notifyServiceListeners(ServiceEvent.UNREGISTERING, sref);
 
 		if (LOG_ENABLED && DEBUG_SERVICES) {
 			logger.log(LogService.LOG_INFO, "Framework: UNREGISTERED SERVICE "
 					+ sref);
 		}
 	}
 
 	/**
 	 * notify all bundle listeners.
 	 * 
 	 * @param state
 	 *            the new state.
 	 * @param bundle
 	 *            the bundle.
 	 */
 	static void notifyBundleListeners(final int state, final Bundle bundle) {
 		if (syncBundleListeners.isEmpty() && bundleListeners.isEmpty()) {
 			return;
 		}
 
 		final BundleEvent event = new BundleEvent(state, bundle);
 
 		// inform the synchrounous bundle listeners first ...
 		final BundleListener[] syncs = (BundleListener[]) syncBundleListeners
 				.toArray(new BundleListener[syncBundleListeners.size()]);
 
 		for (int i = 0; i < syncs.length; i++) {
 			syncs[i].bundleChanged(event);
 		}
 
 		if (bundleListeners.isEmpty()) {
 			return;
 		}
 
 		final BundleListener[] asyncs = (BundleListener[]) bundleListeners
 				.toArray(new BundleListener[bundleListeners.size()]);
 		for (int i = 0; i < asyncs.length; i++) {
 			asyncs[i].bundleChanged(event);
 		}
 	}
 
 	/**
 	 * notify all framework listeners.
 	 * 
 	 * @param state
 	 *            the new state.
 	 * @param bundle
 	 *            the bundle.
 	 * @param throwable
 	 *            a throwable.
 	 */
 	static void notifyFrameworkListeners(final int state, final Bundle bundle,
 			final Throwable throwable) {
 		if (frameworkListeners.isEmpty()) {
 			return;
 		}
 
 		final FrameworkEvent event = new FrameworkEvent(state, bundle,
 				throwable);
 
 		final FrameworkListener[] listeners = (FrameworkListener[]) frameworkListeners
 				.toArray(new FrameworkListener[frameworkListeners.size()]);
 
 		final boolean secure = SECURITY_ENABLED;
 		for (int i = 0; i < listeners.length; i++) {
 			final FrameworkListener listener = listeners[i];
 			if (secure) {
 				AccessController.doPrivileged(new PrivilegedAction() {
 					public Object run() {
 						listener.frameworkEvent(event);
 						return null;
 					}
 				});
 			} else {
 				listener.frameworkEvent(event);
 			}
 		}
 	}
 
 	/**
 	 * notify all service listeners.
 	 * 
 	 * @param state
 	 *            the new state.
 	 * @param reference
 	 *            the service reference.
 	 */
 	static void notifyServiceListeners(final int state,
 			final ServiceReference reference) {
 		if (serviceListeners.isEmpty()) {
 			return;
 		}
 
 		final ServiceEvent event = new ServiceEvent(state, reference);
 
 		final ServiceListenerEntry[] entries = (ServiceListenerEntry[]) serviceListeners
 				.toArray(new ServiceListenerEntry[serviceListeners.size()]);
 
 		final boolean secure = SECURITY_ENABLED;
 		for (int i = 0; i < entries.length; i++) {
 			if (entries[i].filter == null
 					|| entries[i].filter
 							.match(((ServiceReferenceImpl) reference).properties)) {
 				final ServiceListener listener = entries[i].listener;
 				if (secure) {
 					AccessController.doPrivileged(new PrivilegedAction() {
 						public Object run() {
 							listener.serviceChanged(event);
 							return null;
 						}
 					});
 				} else {
 					listener.serviceChanged(event);
 				}
 			}
 		}
 	}
 
 	/**
 	 * clear all traces of a bundle.
 	 * 
 	 * @param bundle
 	 *            the bundle.
 	 */
 	static void clearBundleTrace(final BundleImpl bundle) {
 		// remove all registered listeners
 		if (bundle.registeredFrameworkListeners != null) {
 			frameworkListeners.removeAll(bundle.registeredFrameworkListeners);
 			bundle.registeredFrameworkListeners = null;
 		}
 		if (bundle.registeredServiceListeners != null) {
 			serviceListeners.removeAll(bundle.registeredServiceListeners);
 			bundle.registeredServiceListeners = null;
 		}
 		if (bundle.registeredBundleListeners != null) {
 			bundleListeners.removeAll(bundle.registeredBundleListeners);
 			syncBundleListeners.removeAll(bundle.registeredBundleListeners);
 			bundle.registeredBundleListeners = null;
 		}
 
 		// unregister registered services
 		final ServiceReference[] regs = bundle.getRegisteredServices();
 		if (regs != null) {
 			for (int i = 0; i < regs.length; i++) {
 				Framework.unregisterService(regs[i]);
 			}
 			bundle.registeredServices = null;
 		}
 
 		// unget all using services
 		final ServiceReference[] refs = bundle.getServicesInUse();
 		for (int i = 0; i < refs.length; i++) {
 			((ServiceReferenceImpl) refs[i]).ungetService(bundle);
 		}
 	}
 
 	/**
 	 * add a value to a value list in a Map.
 	 * 
 	 * @param map
 	 *            the map.
 	 * @param key
 	 *            the key.
 	 * @param value
 	 *            the value to be added to the list.
 	 */
 	static void addValue(final Map map, final Object key, final Object value) {
 		List values;
 		if ((values = (List) map.get(key)) == null) {
 			values = new ArrayList();
 		}
 
 		values.add(value);
 		map.put(key, values);
 	}
 
 	/**
 	 * remove a value from a list in a Map.
 	 * 
 	 * @param map
 	 *            the map.
 	 * @param keys
 	 *            the keys that are affected.
 	 * @param value
 	 *            the value to be deleted in the lists.
 	 */
 	static void removeValue(final Map map, final Object[] keys,
 			final Object value) {
 		List values;
 		for (int i = 0; i < keys.length; i++) {
 			if ((values = (List) map.get(keys[i])) == null) {
 				continue;
 			}
 			values.remove(value);
 
 			if (values.isEmpty()) {
 				map.remove(keys[i]);
 			} else {
 				map.put(keys[i], values);
 			}
 		}
 	}
 
 	/**
 	 * export a package.
 	 * 
 	 * @param bundle
 	 *            the exporting bundle.
 	 * @param exports
 	 *            the exported packages.
 	 * @param resolved
 	 *            true is the bundle is already resolved.
 	 */
 	static void export(final BundleClassLoader classloader,
 			final String[] exports, final boolean resolved) {
 		/*
 		 * <specs page="61">A Framework must guarantee that only one version of
 		 * a bundle's classes is available at any time. If the updated bundle
 		 * had exported any packages that are used by other bundles, those
 		 * packages must not be updated; their old versions must remain until
 		 * the <code>org.osgi.service.admin.PackageAdmin.refreshPackages</code>
 		 * method has been called or the Framework is restarted</specs>
 		 */
 		synchronized (exportedPackages) {
 			if (DEBUG_PACKAGES) {
 				logger.log(LogService.LOG_DEBUG, "Bundle " + classloader.bundle
 						+ " registers "
 						+ (resolved ? "resolved" : "unresolved") + " packages "
 						+ java.util.Arrays.asList(exports));
 			}
 
 			for (int i = 0; i < exports.length; i++) {
 				final Package pkg = new Package(exports[i], classloader,
 						resolved);
 				final Package existing = (Package) exportedPackages.get(pkg);
 				if (existing == null) {
 					exportedPackages.put(pkg, pkg);
 					if (LOG_ENABLED && DEBUG_PACKAGES) {
 						logger.log(LogService.LOG_DEBUG, "REGISTERED PACKAGE "
 								+ pkg);
 					}
 				} else {
 					if (existing.importingBundles == null
 							&& pkg.updates(existing)) {
 						exportedPackages.remove(existing);
 						exportedPackages.put(pkg, pkg);
 						if (LOG_ENABLED && DEBUG_PACKAGES) {
 							logger.log(LogService.LOG_DEBUG,
 									"REPLACED PACKAGE " + existing + " WITH "
 											+ pkg);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * get an import for a package.
 	 * 
 	 * @param bundle
 	 *            the bundle.
 	 * @param importPkg
 	 *            the package name.
 	 * @param critical
 	 *            if the bundle is currently starting.
 	 * @return the ExportClassLoader.
 	 */
 	static BundleClassLoader getImport(final BundleImpl bundle,
 			final String importPkg, final boolean critical,
 			final HashSet pending) {
 		if (DEBUG_PACKAGES) {
 			logger.log(LogService.LOG_DEBUG, "Bundle " + bundle
 					+ " requests package " + importPkg);
 		}
 
 		synchronized (exportedPackages) {
 			final Package pkg = (Package) exportedPackages.get(new Package(
 					importPkg, null, false));
 
 			if (pkg == null || (!pkg.resolved && !critical)) {
 				return null;
 			}
 
 			final BundleClassLoader exporter = pkg.classloader;
 			if (exporter == bundle.classloader) {
 				return exporter;
 			}
 
 			// in case we really need the export and we have an unresolved
 			// bundle that provides this export, try to resolve it and all
 			// dependencies now to get the thing running. This allows lazy
 			// resolving and handling of cyclic dependencies ...
 			if (critical && !pkg.resolved && !pending.contains(pkg.classloader)) {
 				try {
 					pending.add(bundle.classloader);
 					pkg.classloader.resolveBundle(true, pending);
 				} catch (Exception e) {
 					e.printStackTrace();
 					return null;
 				}
 			}
 
 			if (pkg.importingBundles == null) {
 				pkg.importingBundles = new ArrayList(1);
 			}
 
 			if (!pkg.importingBundles.contains(bundle)) {
 				pkg.importingBundles.add(bundle);
 			}
 
 			if (LOG_ENABLED && DEBUG_PACKAGES) {
 				logger.log(LogService.LOG_DEBUG, "REQUESTED PACKAGE "
 						+ importPkg + ", RETURNED DELEGATION TO "
 						+ exporter.bundle);
 			}
 
 			return exporter;
 		}
 	}
 
 	/*
 	 * inner classes
 	 */
 
 	/**
 	 * The bundle context implementation.
 	 * 
 	 * @author Jan S. Rellermeyer, IKS, ETH Zurich
 	 * 
 	 */
 	final static class BundleContextImpl implements
 			org.osgi.framework.BundleContext {
 
 		/**
 		 * is the context valid ?
 		 */
 		boolean isValid = true;
 
 		/**
 		 * the bundle.
 		 */
 		BundleImpl bundle;
 
 		/**
 		 * check, if the context is valid.
 		 */
 		private void checkValid() {
 			if (!isValid) {
 				throw new IllegalStateException("BundleContext of bundle "
 						+ bundle
 						+ " used after bundle has been stopped or uninstalled.");
 			}
 		}
 
 		/**
 		 * add a bundle listener.
 		 * 
 		 * @param listener
 		 *            a bundle listener.
 		 * @see org.osgi.framework.BundleContext#addBundleListener(org.osgi.framework.BundleListener)
 		 */
 		public void addBundleListener(final BundleListener listener) {
 			checkValid();
 
 			// synchronous bundle listener ?
 			final List listeners = listener instanceof SynchronousBundleListener ? syncBundleListeners
 					: bundleListeners;
 
 			if (bundle.registeredBundleListeners == null) {
 				bundle.registeredBundleListeners = new ArrayList(1);
 			}
 			if (!bundle.registeredBundleListeners.contains(listener)) {
 				listeners.add(listener);
 				bundle.registeredBundleListeners.add(listener);
 			}
 		}
 
 		/**
 		 * add a framework listener.
 		 * 
 		 * @param listener
 		 *            a framework listener.
 		 * @see org.osgi.framework.BundleContext#addFrameworkListener(org.osgi.framework.FrameworkListener)
 		 * @category BundleContext
 		 */
 		public void addFrameworkListener(final FrameworkListener listener) {
 			checkValid();
 
 			if (bundle.registeredFrameworkListeners == null) {
 				bundle.registeredFrameworkListeners = new ArrayList(1);
 			}
 			if (!bundle.registeredFrameworkListeners.contains(listener)) {
 				frameworkListeners.add(listener);
 				bundle.registeredFrameworkListeners.add(listener);
 			}
 		}
 
 		/**
 		 * add a service listener.
 		 * 
 		 * @param listener
 		 *            the service listener.
 		 * @param filterExpr
 		 *            the filter String.
 		 * @throws InvalidSyntaxException
 		 *             if the filter string is invalid.
 		 * @see org.osgi.framework.BundleContext#addServiceListener(org.osgi.framework.ServiceListener,
 		 *      java.lang.String)
 		 * @category BundleContext
 		 */
 		public void addServiceListener(final ServiceListener listener,
 				final String filterExpr) throws InvalidSyntaxException {
 			checkValid();
 
 			ServiceListenerEntry entry = new ServiceListenerEntry(listener,
 					filterExpr);
 
 			if (bundle.registeredServiceListeners == null) {
 				bundle.registeredServiceListeners = new ArrayList(1);
 			}
 			if (isServiceListenerRegistered(listener)) {
 				serviceListeners.remove(entry);
 			} else {
 				bundle.registeredServiceListeners.add(listener);
 			}
 			serviceListeners.add(entry);
 		}
 		
 		/**
 		 * Determine if given service listener has been registered.
 		 * 
 		 * @param listener
 		 * @return <code>true</code> if the listener is registered.
 		 */
 		private boolean isServiceListenerRegistered(final ServiceListener listener) {
 		if (DEEP_SERVICE_LISTENER_CHECK) {
 			final Iterator iter = bundle.registeredServiceListeners.iterator();
 			while (iter.hasNext()) {
 				Object obj = iter.next();
 				if (listener == obj) {
 					return true;
 				}
 			}
 				return false;		
 			} else {
 				return bundle.registeredServiceListeners.contains(listener);
 			}
 		}		
 
 		/**
 		 * add a service listener.
 		 * 
 		 * @param listener
 		 *            the service listener.
 		 * @see org.osgi.framework.BundleContext#addServiceListener(org.osgi.framework.ServiceListener)
 		 * @category BundleContext
 		 */
 		public void addServiceListener(final ServiceListener listener) {
 			checkValid();
 			try {
 				addServiceListener(listener, null);
 			} catch (InvalidSyntaxException e) {
 				// does not happen
 			}
 		}
 
 		/**
 		 * create a filter.
 		 * 
 		 * @param filter
 		 *            the filter string.
 		 * @return a Filter object.
 		 * @throws InvalidSyntaxException
 		 *             if the filter string is invalid.
 		 * @see org.osgi.framework.BundleContext#createFilter(java.lang.String)
 		 * @category BundleContext
 		 */
 		public Filter createFilter(final String filter)
 				throws InvalidSyntaxException {
 			if (filter == null) {
 				throw new NullPointerException();
 			}
 			return RFC1960Filter.fromString(filter);
 		}
 
 		/**
 		 * get the bundle.
 		 * 
 		 * @return the bundle.
 		 * @see org.osgi.framework.BundleContext#getBundle()
 		 * @category BundleContext
 		 */
 		public Bundle getBundle() {
 			return bundle;
 		}
 
 		/**
 		 * get a bundle by id.
 		 * 
 		 * @param id
 		 *            the bundle id.
 		 * @return the bundle object.
 		 * @see org.osgi.framework.BundleContext#getBundle(long)
 		 * @category BundleContext
 		 */
 		public Bundle getBundle(final long id) {
 			checkValid();
 			return (Bundle) bundleID_bundles.get(new Long(id));
 		}
 
 		/**
 		 * get all bundles.
 		 * 
 		 * @return the array of bundles.
 		 * @see org.osgi.framework.BundleContext#getBundles()
 		 * @category BundleContext
 		 */
 		public Bundle[] getBundles() {
 			checkValid();
 
 			Bundle[] bundleArray = (Bundle[]) bundles
 					.toArray(new Bundle[bundles.size()]);
 			Bundle[] allBundles = new Bundle[bundleArray.length + 1];
 			allBundles[0] = systemBundle;
 			System.arraycopy(bundleArray, 0, allBundles, 1, bundleArray.length);
 			return allBundles;
 		}
 
 		/**
 		 * get a data file.
 		 * 
 		 * @param filename
 		 *            the name of the file
 		 * @return a File object.
 		 * @see org.osgi.framework.BundleContext#getDataFile(java.lang.String)
 		 * @category BundleContext
 		 */
 		public File getDataFile(final String filename) {
 			checkValid();
 			try {
 				final File file = new File(bundle.classloader.storageLocation
 						+ "/data", filename);
 				file.getParentFile().mkdirs();
 				return file;
 			} catch (Exception e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 
 		/**
 		 * get a system property.
 		 * 
 		 * @param key
 		 *            the key.
 		 * @return the value.
 		 * @see org.osgi.framework.BundleContext#getProperty(java.lang.String)
 		 * @category BundleContext
 		 */
 		public String getProperty(final String key) {
 			return (String) properties.get(key);
 		}
 
 		/**
 		 * get the service object.
 		 * 
 		 * @param reference
 		 *            the service reference.
 		 * @return the service object.
 		 * @see org.osgi.framework.BundleContext#getService(org.osgi.framework.ServiceReference)
 		 * @category BundleContext
 		 */
 		public Object getService(final ServiceReference reference) {
 			checkValid();
 			if (reference == null) {
 				throw new NullPointerException("Null service reference.");
 			}
 
 			if (SECURITY_ENABLED) {
 				final String[] clazzes = (String[]) reference
 						.getProperty(Constants.OBJECTCLASS);
 				for (int i = 0; i < clazzes.length; i++) {
 					try {
 						AccessController.checkPermission(new ServicePermission(
 								clazzes[i], ServicePermission.GET));
 						return ((ServiceReferenceImpl) reference)
 								.getService(bundle);
 					} catch (SecurityException se) {
 						continue;
 					}
 				}
 				throw new SecurityException(
 						"Caller does not have permissions for getting service from "
 								+ reference);
 			}
 
 			return ((ServiceReferenceImpl) reference).getService(bundle);
 		}
 
 		/**
 		 * get all service references matching a filter.
 		 * 
 		 * @param clazz
 		 *            the class name.
 		 * @param filter
 		 *            the filter.
 		 * @return the array of matching service references.
 		 * @throws InvalidSyntaxException
 		 *             if the filter string is invalid.
 		 * @see org.osgi.framework.BundleContext#getServiceReferences(java.lang.String,
 		 *      java.lang.String)
 		 * @category BundleContext
 		 */
 		public ServiceReference[] getServiceReferences(final String clazz,
 				final String filter) throws InvalidSyntaxException {
 			checkValid();
 
 			final Filter theFilter = RFC1960Filter.fromString(filter);
 			final Collection references;
 
 			if (clazz == null) {
 				references = services;
 			} else {
 				references = (List) classes_services.get(clazz);
 				if (references == null) {
 					return null;
 				}
 			}
 
 			final List result = new ArrayList();
 			final ServiceReferenceImpl[] refs = (ServiceReferenceImpl[]) references
 					.toArray(new ServiceReferenceImpl[references.size()]);
 
 			for (int i = 0; i < refs.length; i++) {
 				if (theFilter.match(refs[i])) {
 					result.add(refs[i]);
 				}
 			}
 
 			if (LOG_ENABLED && DEBUG_SERVICES) {
 				logger
 						.log(LogService.LOG_INFO,
 								"Framework: REQUESTED SERVICES " + clazz + " "
 										+ filter);
 				logger.log(LogService.LOG_INFO, "\tRETURNED " + result);
 			}
 			return result.size() == 0 ? null : (ServiceReference[]) result
 					.toArray(new ServiceReference[result.size()]);
 		}
 
 		/**
 		 * get a service reference.
 		 * 
 		 * @param clazz
 		 *            the class name.
 		 * @return the service reference or null if no such service is
 		 *         registered.
 		 * 
 		 * @see org.osgi.framework.BundleContext#getServiceReference(java.lang.String)
 		 * @category BundleContext
 		 */
 		public ServiceReference getServiceReference(final String clazz) {
 			checkValid();
 
 			ServiceReference winner = null;
 			int maxRanking = -1;
 			long lastServiceID = Long.MAX_VALUE;
 			final List list = ((List) classes_services.get(clazz));
 			if (list == null) {
 				return null;
 			}
 
 			final ServiceReference[] candidates = (ServiceReference[]) list
 					.toArray(new ServiceReference[list.size()]);
 
 			for (int i = 0; i < candidates.length; i++) {
 				Integer rankProp = (Integer) candidates[i]
 						.getProperty(Constants.SERVICE_RANKING);
 
 				int ranking = rankProp != null ? rankProp.intValue() : 0;
 				long serviceID = ((Long) candidates[i]
 						.getProperty(Constants.SERVICE_ID)).longValue();
 
 				if (ranking > maxRanking
 						|| (ranking == maxRanking && serviceID < lastServiceID)) {
 					winner = candidates[i];
 					maxRanking = ranking;
 					lastServiceID = serviceID;
 				}
 			}
 			if (LOG_ENABLED && DEBUG_SERVICES) {
 				logger.log(LogService.LOG_INFO, "Framework: REQUESTED SERVICE "
 						+ clazz);
 				logger.log(LogService.LOG_INFO, "\tRETURNED " + winner);
 			}
 			return winner;
 		}
 
 		/**
 		 * install a new bundle.
 		 * 
 		 * @param location
 		 *            the bundle location.
 		 * @return the bundle object.
 		 * @throws BundleException
 		 *             if something goes wrong.
 		 * @see org.osgi.framework.BundleContext#installBundle(java.lang.String)
 		 * @category BundleContext
 		 */
 		public Bundle installBundle(final String location)
 				throws BundleException {
 			if (location == null) {
 				throw new IllegalArgumentException("Location must not be null");
 			}
 			checkValid();
 			if (Framework.SECURITY_ENABLED) {
 				Framework.checkAdminPermission();
 			}
 			return installNewBundle(location);
 		}
 
 		/**
 		 * install a new bundle from input stream.
 		 * 
 		 * @param location
 		 *            the location.
 		 * @param in
 		 *            the input stream.
 		 * @return the bundle object.
 		 * @throws BundleException
 		 *             if something goes wrong.
 		 * @see org.osgi.framework.BundleContext#installBundle(java.lang.String,
 		 *      java.io.InputStream)
 		 * @category BundleContext
 		 */
 		public Bundle installBundle(final String location, final InputStream in)
 				throws BundleException {
 			if (location == null) {
 				throw new IllegalArgumentException("Location must not be null");
 			}
 			checkValid();
 			if (Framework.SECURITY_ENABLED) {
 				Framework.checkAdminPermission();
 			}
 			return installNewBundle(location, in);
 		}
 
 		/**
 		 * register a new service.
 		 * 
 		 * @param clazzes
 		 *            the classes under which the service is registered.
 		 * @param service
 		 *            the service object
 		 * @param properties
 		 *            the properties.
 		 * @return the service registration.
 		 * @see org.osgi.framework.BundleContext#registerService(java.lang.String[],
 		 *      java.lang.Object, java.util.Dictionary)
 		 * @context BundleContext
 		 */
 		public ServiceRegistration registerService(final String[] clazzes,
 				final Object service, final Dictionary serviceProperties) {
 			checkValid();
 
 			if (service == null) {
 				throw new IllegalArgumentException(
 						"Cannot register a null service");
 			}
 
 			if (SECURITY_ENABLED) {
 				for (int i = 0; i < clazzes.length; i++) {
 					AccessController.checkPermission(new ServicePermission(
 							clazzes[i], ServicePermission.REGISTER));
 				}
 			}
 
 			final ServiceReferenceImpl sref = new ServiceReferenceImpl(bundle,
 					service, serviceProperties, clazzes);
 
 			services.add(sref);
 
 			// lazy initialization
 			if (bundle.registeredServices == null) {
 				bundle.registeredServices = new ArrayList(1);
 			}
 			bundle.registeredServices.add(sref);
 
 			// and now register the service for all classes ...
 			for (int counter = 0; counter < clazzes.length; counter++) {
 				addValue(classes_services, clazzes[counter], sref);
 			}
 
 			if (LOG_ENABLED && DEBUG_SERVICES) {
 				logger.log(LogService.LOG_INFO,
 						"Framework: REGISTERED SERVICE " + clazzes[0]);
 			}
 
 			notifyServiceListeners(ServiceEvent.REGISTERED, sref);
 
 			return sref.registration;
 		}
 
 		/**
 		 * register a new service.
 		 * 
 		 * @param clazz
 		 *            the class under which the service is registered.
 		 * @param service
 		 *            the service object.
 		 * @param properties
 		 *            the properties.
 		 * @return the service registration.
 		 * @see org.osgi.framework.BundleContext#registerService(java.lang.String,
 		 *      java.lang.Object, java.util.Dictionary)
 		 * @category BundleContext
 		 */
 		public ServiceRegistration registerService(final String clazz,
 				final Object service, final Dictionary properties) {
 			return registerService(new String[] { clazz }, service, properties);
 		}
 
 		/**
 		 * remove a bundle listener.
 		 * 
 		 * @param listener
 		 *            a bundle listener.
 		 * @see org.osgi.framework.BundleContext#removeBundleListener(org.osgi.framework.BundleListener)
 		 * @category BundleContext
 		 */
 		public void removeBundleListener(final BundleListener listener) {
 			checkValid();
 			(listener instanceof SynchronousBundleListener ? syncBundleListeners
 					: bundleListeners).remove(listener);
 			bundle.registeredBundleListeners.remove(listener);
 			if (bundle.registeredBundleListeners.isEmpty()) {
 				bundle.registeredBundleListeners = null;
 			}
 		}
 
 		/**
 		 * remove a framework listener.
 		 * 
 		 * @param listener
 		 *            a framework listener.
 		 * @see org.osgi.framework.BundleContext#removeFrameworkListener(org.osgi.framework.FrameworkListener)
 		 * @category BundleContext
 		 */
 		public void removeFrameworkListener(final FrameworkListener listener) {
 			checkValid();
 			frameworkListeners.remove(listener);
 			bundle.registeredFrameworkListeners.remove(listener);
 			if (bundle.registeredFrameworkListeners.isEmpty()) {
 				bundle.registeredFrameworkListeners = null;
 			}
 		}
 
 		/**
 		 * remove a service listener.
 		 * 
 		 * @param listener
 		 *            the service listener.
 		 * @see org.osgi.framework.BundleContext#removeServiceListener(org.osgi.framework.ServiceListener)
 		 * @category BundleContext
 		 */
 		public void removeServiceListener(final ServiceListener listener) {
 			checkValid();
 			try {
 				serviceListeners
 						.remove(new ServiceListenerEntry(listener, null));
 				bundle.registeredServiceListeners.remove(listener);
 				if (bundle.registeredServiceListeners.isEmpty()) {
 					bundle.registeredServiceListeners = null;
 				}
 			} catch (InvalidSyntaxException i) {
 				// does not happen
 			}
 		}
 
 		/**
 		 * unget a service.
 		 * 
 		 * @param reference
 		 *            the service reference of the service
 		 * @return true is the service is still in use by other bundles, false
 		 *         otherwise.
 		 * @see org.osgi.framework.BundleContext#ungetService(org.osgi.framework.ServiceReference)
 		 * @category BundleContext
 		 */
 		public synchronized boolean ungetService(
 				final ServiceReference reference) {
 			checkValid();
 			return ((ServiceReferenceImpl) reference).ungetService(bundle);
 		}
 	}
 
 	/**
 	 * The systemBundle.
 	 * 
 	 * @author Jan S. Rellermeyer, IKS, ETH Zurich
 	 * 
 	 */
 	private static final class SystemBundle implements Bundle, StartLevel,
 			PackageAdmin {
 
 		/**
 		 * the state
 		 */
 		int state;
 
 		/**
 		 * the properties.
 		 */
 		private final Dictionary props = new Hashtable(3);
 
 		/**
 		 * the service reference.
 		 */
 		private final ServiceReference[] registeredServices;
 
 		/**
 		 * create the system bundle instance.
 		 * 
 		 */
 		SystemBundle() {
 			props.put(Constants.BUNDLE_NAME, "System Bundle");
 			props.put(Constants.BUNDLE_VERSION, FRAMEWORK_VERSION);
 			bundleID_bundles.put(new Long(0), this);
 
 			final ServiceReference ref = new ServiceReferenceImpl(this, this,
 					null, new String[] { StartLevel.class.getName(),
 							PackageAdmin.class.getName() });
 			addValue(classes_services, StartLevel.class.getName(), ref);
 			addValue(classes_services, PackageAdmin.class.getName(), ref);
 			services.add(ref);
 
 			// start the logger
 			if (LOG_ENABLED) {
 				logger = new LogServiceImpl(LOG_BUFFER_SIZE, LOG_LEVEL,
 						LOG_QUIET);
 				final ServiceReference logref = new ServiceReferenceImpl(
 						systemBundle, logger, null,
 						new String[] { LogService.class.getName() });
 				services.add(logref);
 				addValue(classes_services, LogService.class.getName(), logref);
 
 				registeredServices = new ServiceReference[] { ref, logref };
 			} else {
 				registeredServices = new ServiceReference[] { ref };
 			}
 		}
 
 		/**
 		 * get the bundle id.
 		 * 
 		 * @return 0.
 		 * @see org.osgi.framework.Bundle#getBundleId()
 		 * @category Bundle
 		 */
 		public long getBundleId() {
 			return 0;
 		}
 
 		/**
 		 * get the properties.
 		 * 
 		 * @return the properties.
 		 * @see org.osgi.framework.Bundle#getHeaders()
 		 * @category Bundle
 		 */
 		public Dictionary getHeaders() {
 			return props;
 		}
 
 		/**
 		 * get the location.
 		 * 
 		 * @return "System Bundle"
 		 * @see org.osgi.framework.Bundle#getLocation()
 		 * @category Bundle
 		 */
 		public String getLocation() {
 			return Constants.SYSTEM_BUNDLE_LOCATION;
 		}
 
 		/**
 		 * get the registered services.
 		 * 
 		 * @return the registered service.
 		 * @see org.osgi.framework.Bundle#getRegisteredServices()
 		 * @category Bundle
 		 */
 		public ServiceReference[] getRegisteredServices() {
 			return registeredServices;
 		}
 
 		/**
 		 * get resources.
 		 * 
 		 * @param name
 		 *            the name.
 		 * @return the URL or null.
 		 * @see org.osgi.framework.Bundle#getResource(java.lang.String)
 		 * @category Bundle
 		 */
 		public URL getResource(final String name) {
 			return getClass().getResource(name);
 		}
 
 		/**
 		 * get the services that are in use.
 		 * 
 		 * @return null.
 		 * @see org.osgi.framework.Bundle#getServicesInUse()
 		 * @category Bundle
 		 */
 		public ServiceReference[] getServicesInUse() {
 			return null;
 		}
 
 		/**
 		 * get the state.
 		 * 
 		 * @return the state.
 		 * @see org.osgi.framework.Bundle#getState()
 		 * @category Bundle
 		 */
 		public int getState() {
 			return state;
 		}
 
 		/**
 		 * check if some permissions are granted.
 		 * 
 		 * @param permission
 		 *            the permissions.
 		 * @return true, if the permissions hold.
 		 * @see org.osgi.framework.Bundle#hasPermission(java.lang.Object)
 		 * @category Bundle
 		 */
 		public boolean hasPermission(final Object permission) {
 			if (SECURITY_ENABLED) {
 				try {
 					AccessController.checkPermission((Permission) permission);
 				} catch (SecurityException se) {
 					return false;
 				}
 				return true;
 			} else {
 				return true;
 			}
 		}
 
 		/**
 		 * start the system bundle.
 		 * 
 		 * @throws BundleException
 		 *             never.
 		 * @see org.osgi.framework.Bundle#start()
 		 * @category Bundle
 		 */
 		public void start() throws BundleException {
 			// this method has no effect
 		}
 
 		/**
 		 * stopping the system bundle means shutting down the framework.
 		 * 
 		 * @throws BundleException
 		 *             never.
 		 * @see org.osgi.framework.Bundle#stop()
 		 * @category Bundle
 		 */
 		public void stop() throws BundleException {
 			if (SECURITY_ENABLED) {
 				checkAdminPermission();
 			}
 			shutdownThread(false);
 		}
 
 		/**
 		 * the system bundle cannot be uninstalled.
 		 * 
 		 * @throws BundleException
 		 *             always.
 		 * @see org.osgi.framework.Bundle#uninstall()
 		 * @category Bundle
 		 */
 		public void uninstall() throws BundleException {
 			throw new BundleException("Cannot uninstall the System Bundle");
 		}
 
 		/**
 		 * updating the system bundle means restarting the framework.
 		 * 
 		 * @throws BundleException
 		 *             never.
 		 * @see org.osgi.framework.Bundle#update()
 		 * @category Bundle
 		 */
 		public void update() throws BundleException {
 			if (SECURITY_ENABLED) {
 				checkAdminPermission();
 			}
 			shutdownThread(true);
 		}
 
 		/**
 		 * the shutdown thread.
 		 * 
 		 * @param restart
 		 *            perform a restart ?
 		 */
 		private void shutdownThread(final boolean restart) {
 			new Thread() {
 				public void run() {
 					shutdown(restart);
 				}
 			}.start();
 		}
 
 		/**
 		 * updating the system bundle means restarting the framework.
 		 * 
 		 * @param in
 		 *            the input stream (not used).
 		 * @throws BundleException
 		 *             never.
 		 * @see org.osgi.framework.Bundle#update(java.io.InputStream)
 		 * @category Bundle
 		 */
 		public void update(final InputStream in) throws BundleException {
 			if (SECURITY_ENABLED) {
 				checkAdminPermission();
 			}
 			shutdownThread(true);
 		}
 
 		// StartLevel methods
 
 		/**
 		 * get the startlevel for a specific bundle.
 		 * 
 		 * @param bundle
 		 *            the bundle.
 		 * @return the start level of the bundle.
 		 * @see org.osgi.service.startlevel.StartLevel#getBundleStartLevel(org.osgi.framework.Bundle)
 		 * @category StartLevel
 		 */
 		public int getBundleStartLevel(final Bundle bundle) {
 			if (bundle == this) {
 				return 0;
 			}
 			final BundleImpl theBundle = ((BundleImpl) bundle);
 			if (theBundle.state == Bundle.UNINSTALLED) {
 				throw new IllegalArgumentException("Bundle " + bundle
 						+ " has been uninstalled");
 			}
 			return theBundle.currentStartlevel;
 		}
 
 		/**
 		 * get the initial startlevel of the framework.
 		 * 
 		 * @return the initial startlevel.
 		 * @see org.osgi.service.startlevel.StartLevel#getInitialBundleStartLevel()
 		 * @category StartLevel
 		 */
 		public int getInitialBundleStartLevel() {
 			return initStartlevel;
 		}
 
 		/**
 		 * get the current startlevel.
 		 * 
 		 * @return the current startlevel.
 		 * @see org.osgi.service.startlevel.StartLevel#getStartLevel()
 		 * @category StartLevel
 		 */
 		public int getStartLevel() {
 			return startlevel;
 		}
 
 		/**
 		 * check, if a bundle has been started persistently.
 		 * 
 		 * @param bundle
 		 *            the bundle.
 		 * @return true or false.
 		 * @see org.osgi.service.startlevel.StartLevel#isBundlePersistentlyStarted(org.osgi.framework.Bundle)
 		 * @category StartLevel
 		 */
 		public boolean isBundlePersistentlyStarted(final Bundle bundle) {
 			if (bundle == this) {
 				return true;
 			}
 			final BundleImpl theBundle = ((BundleImpl) bundle);
 			if (theBundle.state == Bundle.UNINSTALLED) {
 				throw new IllegalArgumentException("Bundle " + bundle
 						+ " has been uninstalled");
 			}
 			return theBundle.persistently;
 		}
 
 		/**
 		 * set the startlevel for a specific bundle.
 		 * 
 		 * @param bundle
 		 *            the bundle.
 		 * @param startLevel
 		 *            the start level.
 		 * @see org.osgi.service.startlevel.StartLevel#setBundleStartLevel(org.osgi.framework.Bundle,
 		 *      int)
 		 * @category StartLevel
 		 */
 		public void setBundleStartLevel(final Bundle bundle,
 				final int startLevel) {
 			if (SECURITY_ENABLED) {
 				checkAdminPermission();
 			}
 
 			if (bundle == this) {
 				throw new IllegalArgumentException(
 						"Cannot set the start level for the system bundle.");
 			}
 			final BundleImpl theBundle = (BundleImpl) bundle;
 			if (theBundle.state == Bundle.UNINSTALLED) {
 				throw new IllegalArgumentException("Bundle " + bundle
 						+ " has been uninstalled");
 			} else if (startLevel <= 0) {
 				throw new IllegalArgumentException("Start level " + startLevel
 						+ " is not a valid level");
 			}
 
 			theBundle.currentStartlevel = startLevel;
 			theBundle.updateMetadata();
 			if (startLevel <= startlevel && bundle.getState() != Bundle.ACTIVE) {
 				try {
 					theBundle.startBundle();
 				} catch (BundleException be) {
 					notifyFrameworkListeners(FrameworkEvent.ERROR, bundle, be);
 				}
 			} else if (startLevel > startlevel
 					&& (bundle.getState() != Bundle.RESOLVED || bundle
 							.getState() != Bundle.INSTALLED)) {
 				try {
 					theBundle.stopBundle();
 				} catch (BundleException be) {
 					notifyFrameworkListeners(FrameworkEvent.ERROR, bundle, be);
 				}
 			}
 
 		}
 
 		/**
 		 * set the initial startlevel of the framework.
 		 * 
 		 * @param startLevel
 		 *            the startlevel.
 		 * @see org.osgi.service.startlevel.StartLevel#setInitialBundleStartLevel(int)
 		 * @category StartLevel
 		 */
 		public void setInitialBundleStartLevel(final int startLevel) {
 			if (SECURITY_ENABLED) {
 				checkAdminPermission();
 			}
 			if (startLevel <= 0) {
 				throw new IllegalArgumentException("Start level " + startLevel
 						+ " is not a valid level");
 			}
 			initStartlevel = startLevel;
 		}
 
 		/**
 		 * set the current startlevel.
 		 * 
 		 * @param targetLevel
 		 *            the target startlevel.
 		 * @see org.osgi.service.startlevel.StartLevel#setStartLevel(int)
 		 * @category StartLevel
 		 */
 		public void setStartLevel(final int targetLevel) {
 			if (SECURITY_ENABLED) {
 				checkAdminPermission();
 			}
 			if (targetLevel <= 0) {
 				throw new IllegalArgumentException("Start level " + targetLevel
 						+ " is not a valid level");
 			}
 			new Thread() {
 				public void run() {
 					setLevel((Bundle[]) bundles.toArray(new Bundle[bundles
 							.size()]), targetLevel, true);
 					notifyFrameworkListeners(FrameworkEvent.STARTLEVEL_CHANGED,
 							systemBundle, null);
 					storeMetadata();
 				}
 			}.start();
 		}
 
 		/**
 		 * set the current startlevel but does not update the metadata.
 		 * 
 		 * @param targetLevel
 		 *            the startlevel.
 		 */
 		private void setLevel(final Bundle[] bundleArray,
 				final int targetLevel, final boolean all) {
 			if (startlevel == targetLevel) {
 				return;
 			}
 			final boolean up = targetLevel > startlevel;
 
 			final int levels = up ? targetLevel - startlevel : startlevel
 					- targetLevel;
 			final Map startLevels = new HashMap(0);
 			// prepare startlevels
 			for (int i = 0; i < bundleArray.length; i++) {
 				if (bundleArray[i] == systemBundle) {
 					continue;
 				}
 				final BundleImpl bundle = (BundleImpl) bundleArray[i];
 				final int offset;
 				if (up) {					
 					offset = bundle.currentStartlevel - startlevel - 1;
 				} else {
 					offset = startlevel - bundle.currentStartlevel;
 				}
 				if (offset >= 0 && offset < levels
 						&& (all || bundle.persistently)) {
 					addValue(startLevels, new Integer(offset), bundle);
 				}
 			}
 
 			for (int i = 0; i < levels; i++) {
 				final List list = (List) startLevels.get(new Integer(i));
 				if (list == null) {
 					continue;
 				}
 				final BundleImpl[] toProcess = (BundleImpl[]) list
 						.toArray(new BundleImpl[list.size()]);
 				for (int j = 0; j < toProcess.length; j++) {
 					try {
 						if (up) {
 							System.out.println("STARTING "	+ toProcess[j].location);
 							toProcess[j].startBundle();
 						} else {
 							System.out.println("STOPPING " + toProcess[j].location);
 							toProcess[j].stopBundle();
 						}
 					} catch (BundleException be) {
 						be.getNestedException().printStackTrace();
 						be.printStackTrace();
 						notifyFrameworkListeners(FrameworkEvent.ERROR,
 								systemBundle, be);
 					} catch (Throwable t) {
 						t.printStackTrace();
 						notifyFrameworkListeners(FrameworkEvent.ERROR,
 								systemBundle, t);
 					}
 				}
 			}
 			startlevel = targetLevel;
 		}
 
 		// Package Admin methods
 
 		/**
 		 * get the exported packages of a bundle.
 		 * 
 		 * @param bundle
 		 *            the bundle.
 		 * @return the array of the exported packages.
 		 * @see org.osgi.service.packageadmin.PackageAdmin#getExportedPackage(org.osgi.framework.Bundle)
 		 * @category PackageAdmin
 		 */
 		public ExportedPackage[] getExportedPackages(final Bundle bundle) {
 			synchronized (exportedPackages) {
 				if (bundle == null || bundle == systemBundle) {
 					return (ExportedPackage[]) exportedPackages
 							.keySet()
 							.toArray(
 									new ExportedPackage[exportedPackages.size()]);
 				}
 
 				final BundleImpl theBundle = (BundleImpl) bundle;
 				if (theBundle.state == Bundle.UNINSTALLED) {
 					return null;
 				}
 
 				final String[] exports = ((BundleImpl) bundle).classloader.exports;
 				if (exports == null) {
 					return null;
 				}
 
 				final ArrayList result = new ArrayList();
 				final BundleClassLoader exporter = theBundle.classloader.originalExporter != null ? theBundle.classloader.originalExporter
 						: theBundle.classloader;
 				for (int i = 0; i < exports.length; i++) {
 					final Package pkg = (Package) exportedPackages
 							.get(new Package(exports[i], null, false));
 					if (pkg == null) {
 						continue;
 					}
 					if (pkg.classloader == exporter) {
 						if (!pkg.resolved) {
 							try {
 								pkg.classloader.resolveBundle(true,
 										new HashSet(0));
 								result.add(pkg);
 							} catch (BundleException e) {
 								continue;
 							}
 						} else {
 							result.add(pkg);
 						}
 					}
 				}
 				return result.isEmpty() ? null : (ExportedPackage[]) result
 						.toArray(new ExportedPackage[result.size()]);
 			}
 		}
 
 		/**
 		 * get the exported package by name.
 		 * 
 		 * @param name
 		 *            the name.
 		 * @return the exported package or null.
 		 * @see org.osgi.service.packageadmin.PackageAdmin#getExportedPackage(java.lang.String)
 		 * @category PackageAdmin
 		 */
 		public ExportedPackage getExportedPackage(final String name) {
 			synchronized (exportedPackages) {
 				final Package pkg = (Package) exportedPackages.get(new Package(
 						name, null, false));
 				if (pkg == null) {
 					return null;
 				}
 				if (!pkg.resolved) {
 					try {
 						pkg.classloader.resolveBundle(true, new HashSet(0));
 					} catch (BundleException e) {
 						return null;
 					}
 				}
 				return pkg;
 			}
 		}
 
 		/**
 		 * refresh all pending packages.
 		 * 
 		 * @param bundles
 		 *            the bundles which exported packages are to be refreshed or
 		 *            <code>null</code> for all packages.
 		 * @see org.osgi.service.packageadmin.PackageAdmin#refreshPackages(org.osgi.framework.Bundle[])
 		 * @category PackageAdmin
 		 */
 		public void refreshPackages(final Bundle[] bundleArray) {
 			if (SECURITY_ENABLED) {
 				checkAdminPermission();
 			}
 
 			new Thread() {
 				public void run() {
 					synchronized (exportedPackages) {
 						final List toProcess;
 
 						// build the initial set of bundles
 						if (bundleArray == null) {
 							toProcess = new ArrayList(bundles.size());
 							toProcess.addAll(bundles);
 							toProcess.remove(systemBundle);
 						} else {
 							toProcess = new ArrayList(bundleArray.length);
 							for (int i = 0; i < bundleArray.length; i++) {
 								if (((BundleImpl) bundleArray[i]).classloader != null) {
 									toProcess.add(bundleArray[i]);
 								}
 							}
 						}
 
 						// nothing to do ? fine, so we are done.
 						if (toProcess.isEmpty()) {
 							return;
 						}
 
 						if (LOG_ENABLED && DEBUG_PACKAGES) {
 							logger.log(LogService.LOG_DEBUG,
 									"REFRESHING PACKAGES FROM BUNDLES "
 											+ toProcess);
 						}
 
 						// build up the update graph. See specs for details.
 						final Set updateGraph = new HashSet();
 						while (!toProcess.isEmpty()) {
 							final BundleImpl bundle = (BundleImpl) toProcess
 									.remove(0);
 							if (updateGraph.contains(bundle)) {
 								continue;
 							}
 
 							ExportedPackage[] exported = getExportedPackages(bundle);
 							if (exported != null) {
 								for (int i = 0; i < exported.length; i++) {
 									final Bundle[] importers = exported[i]
 											.getImportingBundles();
 									if (importers == null) {
 										continue;
 									}
 									toProcess.addAll(java.util.Arrays
 											.asList(importers));
 								}
 							}
 							updateGraph.add(bundle);
 						}
 
 						if (LOG_ENABLED && DEBUG_PACKAGES) {
 							logger.log(LogService.LOG_DEBUG, "UPDATE GRAPH IS "
 									+ updateGraph);
 						}
 
 						// create a refresh array that is ordered by bundle IDs
 						final Bundle[] refreshArray = new Bundle[updateGraph
 								.size()];
 						int pos = -1;
 						final Bundle[] installedBundles = (Bundle[]) bundles
 								.toArray(new Bundle[bundles.size()]);
 						for (int i = 0; i < installedBundles.length; i++) {
 							if (updateGraph.contains(installedBundles[i])) {
 								refreshArray[++pos] = installedBundles[i];
 							}
 						}
 
 						// stop all bundles in the restart array regarding their
 						// startlevels
 						final int currentLevel = startlevel;
 						setLevel(refreshArray, 0, true);
 
 						// perform a cleanup for all bundles
 						for (int i = 0; i < refreshArray.length; i++) {
 							((BundleImpl) refreshArray[i]).classloader
 									.cleanup(false);
 						}
 
 						// register all their packages as unresolved exports
 						for (int i = 0; i < refreshArray.length; i++) {
 							final BundleClassLoader cl = ((BundleImpl) refreshArray[i]).classloader;
 							if (cl.exports.length > 0) {
 								Framework.export(cl, cl.exports, false);
 							}
 						}
 
 						// restart all bundles regarding their startlevels
 						for (int i = 0; i < refreshArray.length; i++) {
 							try {
 								((BundleImpl) refreshArray[i]).classloader
 										.resolveBundle(true, new HashSet());
 							} catch (BundleException e) {
 								e.printStackTrace();
 							}
 						}
 						setLevel(refreshArray, currentLevel, true);
 
 						Framework.notifyFrameworkListeners(
 								FrameworkEvent.PACKAGES_REFRESHED,
 								systemBundle, null);
 					}
 				}
 			}.start();
 
 		}
 
 		/**
 		 * get a string representation.
 		 * 
 		 * @return the string representation.
 		 * @see java.lang.Object#toString()
 		 * @category Object
 		 */
 		public String toString() {
 			return "SystemBundle";
 		}
 	}
 
 	/**
 	 * An entry consisting of service listener and filter.
 	 * 
 	 * @author Jan S. Rellermeyer, IKS, ETH Zurich
 	 */
 	private static final class ServiceListenerEntry implements EventListener {
 		/**
 		 * the listener.
 		 */
 		final ServiceListener listener;
 
 		/**
 		 * the filter.
 		 */
 		final Filter filter;
 
 		/**
 		 * create a new entry.
 		 * 
 		 * @param listener
 		 *            the listener.
 		 * @param filter
 		 *            the filter.
 		 * @throws InvalidSyntaxException
 		 *             if the filter cannot be parsed.
 		 */
 		private ServiceListenerEntry(final ServiceListener listener,
 				final String filter) throws InvalidSyntaxException {
 			this.listener = listener;
 			this.filter = filter == null ? null : RFC1960Filter
 					.fromString(filter);
 		}
 
 		/**
 		 * check for equality.
 		 * 
 		 * @param other
 		 *            the other object.
 		 * @return true, if the two objects are equal.
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		public boolean equals(final Object other) {
 			if (other instanceof ServiceListenerEntry) {
 				final ServiceListenerEntry entry = (ServiceListenerEntry) other;
 				return listener.equals(entry.listener);
 			}
 			return false;
 		}
 
 		/**
 		 * get the hash code.
 		 * 
 		 * @return the hash code.
 		 * @see java.lang.Object#hashCode()
 		 */
 		public int hashCode() {
 			return listener.hashCode()
 					+ (filter != null ? filter.hashCode() >> 8 : 0);
 		}
 
 		/**
 		 * get a string representation.
 		 * 
 		 * @return a string representation.
 		 * @see java.lang.Object#toString()
 		 */
 		public String toString() {
 			return listener + " " + filter;
 		}
 	}
 }
