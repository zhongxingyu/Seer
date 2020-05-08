 
 package org.paxle.desktop.impl;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.Comparator;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.paxle.core.io.IOTools;
 
 /**
  * To get this to work you need to set the variable
  * org.osgi.framework.system.packages=sun.awt,javax.swing,javax.swing.event,sun.awt.motif,sun.awt.X11,javax.swing.plaf.metal,javax.swing.plaf.basic
  *
  */
 public class Activator implements BundleActivator {
 	
 	public static final float NATIVE_JRE_SUPPORT = 1.6f;
 	public static final String BACKEND_IMPL_ROOT_PACKAGE = "org.paxle.desktop.backend.impl";
 	
 	private static final String IMPL_JDIC = "jdic";
 	private static final String IMPL_JRE6 = "jre6";
 	
 	private Log logger = null;
 	
 	/**
 	 * Contains all currently known implementations of the desktop integration backend descendingly
 	 * sorted by the first supported Java version number
 	 */
 	// XXX: maybe this can work with the execution environments the respective bundle manifest specifies,
 	// all I currently know is that J2SE-1.5 or something like that seems not to be a legal value here
 	private static final TreeMap<Float,String> KNOWN_IMPLS = new TreeMap<Float,String>(
 			// negative comparator
 			new Comparator<Float>() {
 				public int compare(Float o1, Float o2) {
 					return -o1.compareTo(o2);
 				}
 			});
 	
 	static {
 		KNOWN_IMPLS.put(Float.valueOf(1.6f), IMPL_JRE6);
 		KNOWN_IMPLS.put(Float.valueOf(1.4f), IMPL_JDIC);
 	}
 	
 	private static float getJavaVersion() {
 		String version = System.getProperty("java.version");
 		int dot = version.indexOf('.');
 		if (dot != -1) {
 			dot = version.indexOf('.', dot + 1);
 			if (dot != -1)
 				version = version.substring(0, dot);
 		}
 		return Float.parseFloat(version);
 	}
 	
 	public static ClassLoader uiClassLoader = Activator.class.getClassLoader();
 	public static BundleContext bc = null;
 	public static String libPath = null;
 	
 	public Method initMethod = null;
 	public Method shutdownMethod = null;
 	public Object initObject = null;
 	
 	public Object dibackend = null;
 	public Object trayIcon = null;
 	
 	public void start(final BundleContext context) throws Exception {
 		bc = context;
 		this.logger = LogFactory.getLog(this.getClass());
 		
 		final boolean thelisMethod = false;
 		final float javaVersion = getJavaVersion();
		this.logger.debug(String.format("Detected java version: %f", javaVersion));
 		
 		final Iterator<Map.Entry<Float,String>> implIt = KNOWN_IMPLS.entrySet().iterator();
 		while (implIt.hasNext()) {
 			final Map.Entry<Float,String> impl = implIt.next();
 			
 			// discard if not supported by JRE
			if (impl.getKey().floatValue() > javaVersion) {
				this.logger.info(String.format(
						"Implementation %s skipped because of missing java version %f.",
						impl.getValue(),
						impl.getKey()
				));
 				continue;
			}
 			
 			try {
				// display icon				
 				initUI(context, impl.getValue(), false);
 				this.logger.info("Successfully started bundle using backend '" + impl + "' and " + ((thelisMethod) ? "theli's" : "KoH's") + " method");
 				break;
 			} catch (Exception e) {
 				this.logger.error("Error starting bundle using backend '" + impl + "' and " + ((thelisMethod) ? "theli's" : "KoH's") + " method: " + e + " (" + e.getCause() + ")",e);
 			}
 		}
 	}
 	
 	public void stop(BundleContext context) throws Exception {
 		if (shutdownMethod != null) {
 			shutdownMethod.invoke(initObject);
 			shutdownMethod = null;
 		}
 		initMethod = null;
 		initObject = null;
 		bc = null;
 	}
 	
 	private void initUI(BundleContext context, String impl, final boolean thelisMethod) throws Exception {
		String bundleName = BACKEND_IMPL_ROOT_PACKAGE + '.' + impl;
		Bundle bundle = findBundle(context, bundleName);
		if (bundle != null) {
			this.logger.info(String.format("Using implementation %s ...",impl));
		} else {
			this.logger.warn(String.format("Unable to find bundle '%s' for implementation %s. Skipping implementation ...",bundleName, impl));
			return;
		}
 		
 		if (impl == IMPL_JDIC) {
 			// copy natives into bundle data folder
 			copyNatives(context);
 			
 			// JdicManager.getManager().initShareNative();
 			final Class<?> jdicManagerC = uiClassLoader.loadClass("org.jdesktop.jdic.init.JdicManager");
 			final Object jdicManager = jdicManagerC.getMethod("getManager").invoke(null);
 			jdicManagerC.getMethod("initShareNative").invoke(jdicManager);
 			
 			if (!(uiClassLoader instanceof HelperClassLoader)) {
 				final File jdicStubJarFile = (File)jdicManagerC.getField("jdicStubJarFile").get(jdicManager);
 				final URL[] helperClassloaderURLs = { jdicStubJarFile.toURI().toURL() };
 				uiClassLoader = new HelperClassLoader(helperClassloaderURLs, uiClassLoader);
 			}
 		}
 		
 		if (thelisMethod) {
 			// use DesktopInit object
 			Class<?> init = uiClassLoader.loadClass("org.paxle.desktop.impl.DesktopInit");
 			Constructor<?> initC = init.getConstructor(BundleContext.class, String.class);
 			initObject = initC.newInstance(context, impl);
 			initMethod = init.getMethod("init");
 			shutdownMethod = init.getMethod("shutdown");
 			initMethod.invoke(initObject);
 			
 		} else {
 			// use org.paxle.desktop.backend.*-tree and dialogues.Menu
 			final String diBackendCName = String.format("%s.%s.%s", BACKEND_IMPL_ROOT_PACKAGE, impl, "DIBackend");
 			this.dibackend = uiClassLoader.loadClass(diBackendCName).newInstance();
 			final Class<?> smC = uiClassLoader.loadClass("org.paxle.desktop.impl.ServiceManager");
 			final Object sm = smC.getConstructor(uiClassLoader.loadClass("org.osgi.framework.BundleContext")).newInstance(context);
 			
 			final Class<?> menuC = uiClassLoader.loadClass("org.paxle.desktop.impl.SystrayMenu2");
 			this.shutdownMethod = menuC.getMethod("shutdown");
 			
 			final Class<?> idibackendC = uiClassLoader.loadClass("org.paxle.desktop.backend.IDIBackend");
 			final Class<?> urlC = uiClassLoader.loadClass("java.net.URL");
 			final Object iconUrl = context.getBundle().getResource("/resources/trayIcon.png");
 			this.initObject = menuC.getConstructor(smC, idibackendC, urlC).newInstance(sm, this.dibackend, iconUrl);
 		}
 	}
 	
 	private static Bundle findBundle(BundleContext context, String symbolicName) {
 		for (Bundle b : context.getBundles())
 			if (b.getHeaders().get(Constants.BUNDLE_SYMBOLICNAME).equals(symbolicName))
 				return b;
 		return null;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static void copyNatives(BundleContext context) throws IOException {
 		Activator.libPath = context.getDataFile("/").getCanonicalPath();
 
 		File libFile = null;
 		
 		Enumeration<URL> libs = findBundle(context, BACKEND_IMPL_ROOT_PACKAGE + '.' + IMPL_JDIC).findEntries("/binaries/libs/","*",true);
 		while (libs.hasMoreElements()) {
 			
 			// open the URL
 			URL lib = libs.nextElement();
 			
 			InputStream libIn = lib.openStream();
 			
 			// open a file
 			String fileName = lib.getFile();
 			int idx = fileName.lastIndexOf("/binaries/libs/");
 			fileName = fileName.substring(idx+"/binaries/libs/".length());			
 			libFile = context.getDataFile(fileName);			
 			
 			if (!libFile.exists() && !fileName.endsWith("/")) {
 				File parent = libFile.getParentFile();
 				if (!parent.exists()) parent.mkdirs();
 				
 				FileOutputStream out = new FileOutputStream(libFile);
 				IOTools.copy(libIn,out);
 				out.close();
 			}
 		}		
 	}
 }
