 package org.smartly.launcher;
 
 
 import org.smartly.IConstants;
 import org.smartly.Smartly;
 import org.smartly.commons.cmdline.CmdLineParser;
 import org.smartly.commons.lang.CharEncoding;
 import org.smartly.commons.logging.LoggingRepository;
 import org.smartly.commons.util.BeanUtils;
 import org.smartly.commons.util.FileUtils;
 import org.smartly.commons.util.PathUtils;
 import org.smartly.packages.SmartlyPackageLoader;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLDecoder;
 import java.util.HashMap;
 import java.util.Map;
 
 abstract class AbstractLauncher {
 
     private static final String DEFAULT_CLASSPATH = "lib/**";
     private static final String SYSPROP_CLASSPATH = "smartly.classpath";
 
     private static final String ENVPROP_HOME = "SMARTLY_HOME";
 
     private static final String CLASSPATH_SMARTLY = Smartly.class.getCanonicalName();
 
     private final String[] _args;
     private final Map<String, Object> _argsMap;
     private boolean _smartlyLauncher;
     private Class _runnerClass;
     private Object _runnerInstance;
     private boolean _initialized;
 
     protected AbstractLauncher(final String[] args) {
         _args = args;
         _argsMap = new HashMap<String, Object>();
         _initialized = false;
         try {
             final ClassLoader loader = this.init(args);
 
             _runnerClass = loader.loadClass(CLASSPATH_SMARTLY);
             _runnerInstance = _runnerClass.newInstance();
             // try inject args to runner
             BeanUtils.setValueIfAny(_runnerInstance, "launcherArgs", _argsMap);
         } catch (Exception x) {
             System.err.println("Uncaught exception: ");
             x.printStackTrace();
             System.exit(2);
         }
     }
 
     public final void run() {
         if (!_initialized) {
             runInternal();
         }
         _initialized = true;
     }
 
     public final String[] getArgs() {
         return _args;
     }
 
     public final Map<String, Object> getArgsMap(){
         return _argsMap;
     }
 
     public final boolean isSmartlyLauncher() {
         return _smartlyLauncher;
     }
 
     /**
      * Event emitter before Smartly started.
      * Here you can register programmatically your package.
      *
      * @param loader Package Loader
      */
     protected abstract void onLoadPackage(final SmartlyPackageLoader loader);
 
 
     // ------------------------------------------------------------------------
     //                      p r i v a t e
     // ------------------------------------------------------------------------
 
     @SuppressWarnings("unchecked")
     private void runInternal() {
         try {
             final SmartlyPackageLoader packageLoader = new SmartlyPackageLoader();
 
             // on-before start
             this.onLoadPackage(packageLoader);
 
             // run smartly runner
             _runnerClass.getMethod("run", SmartlyPackageLoader.class).invoke(_runnerInstance, packageLoader);
         } catch (Exception x) {
             System.err.println("Uncaught exception: ");
             x.printStackTrace();
             System.exit(2);
         }
     }
 
     private ClassLoader init(final String[] args) throws Exception {
         // init internal logger
         LoggingRepository.getInstance().setFilePath(IConstants.PATH_LOG.concat("/smartly.log"));
 
 
         // read args
         this.parseArgs(args);
 
         final File home = getSmartlyHome();
         final ClassLoader loader = createClassLoader(home);
 
         this.initDirs();
 
         return loader;
     }
 
     private void initDirs() throws Exception {
         FileUtils.mkdirs(PathUtils.getAbsolutePath(IConstants.PATH_LOG));
         FileUtils.mkdirs(PathUtils.getAbsolutePath(IConstants.PATH_CONFIGFILES));
         FileUtils.mkdirs(PathUtils.getAbsolutePath(IConstants.PATH_LIBRARIES));
         FileUtils.mkdirs(PathUtils.getAbsolutePath(IConstants.PATH_PACKAGES));
     }
 
     private void parseArgs(final String[] args) throws
             CmdLineParser.UnknownOptionException,
             CmdLineParser.IllegalOptionValueException {
         if (null != args && args.length > 0) {
             final CmdLineParser parser = new CmdLineParser();
 
             final CmdLineParser.Option workspaceOpt = parser.addStringOption('w', "workspace");
             final CmdLineParser.Option charsetOpt = parser.addStringOption('c', "charset");
             final CmdLineParser.Option testOpt = parser.addBooleanOption('t', "test"); // test mode (only for test unit)
             final CmdLineParser.Option proxyOpt = parser.addBooleanOption('p', "proxy"); // proxy (-p true, -p false) if true uses system proxy
 
             parser.parse(args);
 
             final String workspace = (String) parser.getOptionValue(workspaceOpt);
             if (null != workspace) {
                 System.setProperty(IConstants.SYSPROP_HOME, workspace);
                 _argsMap.put("w", workspace);
             }
 
             final String charset = (String) parser.getOptionValue(charsetOpt, CharEncoding.getDefault());
             if (null != charset) {
                 CharEncoding.setDefault(charset);
                 _argsMap.put("c", charset);
             }
 
             final boolean test = (Boolean)parser.getOptionValue(testOpt, false);
             _argsMap.put("t", test);
 
             // set default proxy for outgoing communications
             final boolean use_proxy = (Boolean)parser.getOptionValue(proxyOpt, false);
             _argsMap.put("p", use_proxy);
             System.setProperty("java.net.useSystemProxies", use_proxy+""); // java.net property
            System.setProperty(IConstants.SYSPROP_USE_PROXIES, use_proxy+""); // smartly property
         }
     }
     // ------------------------------------------------------------------------
     //                      S T A T I C
     // ------------------------------------------------------------------------
 
     /**
      * Create a server-wide ClassLoader from our install directory.
      * This will be used as parent ClassLoader for all application
      * ClassLoaders.
      *
      * @param home the ringo install directory
      * @return the main classloader we'll be using
      * @throws java.net.MalformedURLException
      */
     private static ClassLoader createClassLoader(final File home)
             throws IOException {
         final String classpath = System.getProperty(SYSPROP_CLASSPATH, DEFAULT_CLASSPATH);
         final String[] classes = classpath.split(",");
 
         // ensure classpath exists
         for (final String path : classes) {
             final String cleanPath = path.replaceAll("\\*", "");
             FileUtils.mkdirs(PathUtils.getAbsolutePath(cleanPath));
         }
 
         // creates and set the new class loader as context class loader
         final SmartlyClassLoader loader = new SmartlyClassLoader(home, classes);
         Thread.currentThread().setContextClassLoader(loader);
 
         return loader;
     }
 
     /**
      * Get the Smartly install directory.
      *
      * @return the base install directory we're running in
      * @throws IOException                    an I/O related exception occurred
      * @throws java.net.MalformedURLException the jar URL couldn't be parsed
      */
     private static File getSmartlyHome()
             throws IOException {
         // check if home directory is set via system property
         String smartlyHome = System.getProperty(IConstants.SYSPROP_HOME);
         if (smartlyHome == null) {
             smartlyHome = System.getenv(ENVPROP_HOME);
         }
 
         if (smartlyHome == null) {
 
             URL launcherUrl = findUrl(SmartlyLauncher.class.getClassLoader());
             if (launcherUrl == null) {
                 launcherUrl = findUrl(Thread.currentThread().getContextClassLoader());
             }
             if (launcherUrl == null) {
                 launcherUrl = findUrl(ClassLoader.getSystemClassLoader());
             }
 
             // this is a  JAR URL of the form
             //    jar:<url>!/{entry}
             // we strip away the jar: prefix and the !/{entry} suffix
             // to get the original jar file URL
 
             String jarUrl = launcherUrl.toString();
             // decode installDir in case it is URL-encoded
             try {
                 jarUrl = URLDecoder.decode(jarUrl, System.getProperty("file.encoding"));
             } catch (UnsupportedEncodingException x) {
                 System.err.println("Unable to decode jar URL: " + x);
             }
 
             if (!jarUrl.startsWith("jar:") || !jarUrl.contains("!")) {
                 smartlyHome = System.getProperty("user.dir");
                 System.err.println("Warning: smartly.home system property is not set ");
                 System.err.println("         and not started from launcher.jar. Using ");
                 System.err.println("         current working directory as install dir.");
             } else {
                 int excl = jarUrl.indexOf("!");
                 jarUrl = jarUrl.substring(4, excl);
                 launcherUrl = new URL(jarUrl);
                 smartlyHome = new File(launcherUrl.getPath()).getParent();
                 if (smartlyHome == null) {
                     smartlyHome = ".";
                 }
             }
         }
 
         final File home = new File(smartlyHome).getCanonicalFile();
         // set System property (this must be called into main)
         System.setProperty(IConstants.SYSPROP_HOME, home.getPath());
         return home;
     }
 
     private static URL findUrl(ClassLoader loader) {
         if (loader instanceof URLClassLoader) {
             return ((URLClassLoader) loader).findResource("org/smartly/launcher/SmartlyLauncher.class");
         }
         return null;
     }
 
 }
