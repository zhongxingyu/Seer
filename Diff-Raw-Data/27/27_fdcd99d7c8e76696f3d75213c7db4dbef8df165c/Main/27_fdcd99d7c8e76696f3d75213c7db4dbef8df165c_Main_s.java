 
 				/*
  *  Adito
  *
  *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 			
 package com.adito.server;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Method;
 import java.net.Authenticator;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.NetworkInterface;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.TreeMap;
 import java.util.prefs.BackingStoreException;
 import java.util.prefs.Preferences;
 
 import javax.net.ssl.TrustManager;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.mortbay.http.HttpContext;
 import org.mortbay.http.NCSARequestLog;
 import org.mortbay.http.ResourceCache;
 import org.mortbay.http.SocketListener;
 import org.mortbay.http.handler.MsieSslHandler;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.servlet.ServletHandler;
 import org.mortbay.jetty.servlet.ServletHttpRequest;
 import org.mortbay.jetty.servlet.ServletHttpResponse;
 import org.mortbay.jetty.servlet.SessionManager;
 import org.mortbay.util.LifeCycleEvent;
 import org.mortbay.util.LifeCycleListener;
 import org.mortbay.util.Password;
 import org.tanukisoftware.wrapper.WrapperListener;
 import org.tanukisoftware.wrapper.WrapperManager;
 
 import com.adito.boot.BootProgressMonitor;
 import com.adito.boot.Branding;
 import com.adito.boot.BrowserLauncher;
 import com.adito.boot.Context;
 import com.adito.boot.ContextConfig;
 import com.adito.boot.ContextHolder;
 import com.adito.boot.ContextKey;
 import com.adito.boot.ContextListener;
 import com.adito.boot.DefaultPropertyDefinition;
 import com.adito.boot.KeyStoreManager;
 import com.adito.boot.LogBootProgressMonitor;
 import com.adito.boot.PropertyClass;
 import com.adito.boot.PropertyClassManager;
 import com.adito.boot.PropertyDefinition;
 import com.adito.boot.PropertyList;
 import com.adito.boot.PropertyPreferences;
 import com.adito.boot.RequestHandler;
 import com.adito.boot.RequestHandlerRequest;
 import com.adito.boot.RequestHandlerResponse;
 import com.adito.boot.StopContextListenerThread;
 import com.adito.boot.SystemProperties;
 import com.adito.boot.Util;
 import com.adito.boot.VersionInfo;
 import com.adito.boot.XMLPropertyDefinition;
 import com.adito.boot.VersionInfo.Version;
 import com.adito.server.jetty.CustomHttpContext;
 import com.adito.server.jetty.CustomJsseListener;
 import com.adito.server.jetty.CustomWebApplicationContext;
 import com.adito.server.jetty.HTTPRedirectHandler;
 
 /**
  * <p>
  * Provides an entry point and a default environment for starting the
  * Adito service.
  * 
  * <p>
  * Adito is primarily a standard Java web application. However, it
  * requires a few additional services from the container that it is running in.
  * This environment is called the {@link com.adito.boot.Context} (see this
  * interfaces Javadoc for more information about this environment) and this
  * class implements that interface.
  * 
  * <p>
  * This class currently provides an implementation that uses Jetty for the
  * servlet / JSP container.
  * 
  * <p>
  * The <i>Context Properties</b> are stored using the Java Preferences API so
  * will likely end up in the Windows register on Win32 platforms or XML files
  * everywhere else.
  * 
  * @see com.adito.boot.Context
  */
 public class Main implements WrapperListener, Context {
     // Private statics
 
     private static File DB_DIR = new File("db");
     private static File CONF_DIR = new File("conf");
     private static File TMP_DIR = new File("tmp");
     private static File LOG_DIR = new File("logs");
     private static File appDir = null;
 
     static Log log;
     static Preferences PREF;
     static ClassLoader bootLoader;
 
     // Private instance variables
     private Server server;
     private long startupStarted;
     private HashMap<URL, ResourceCache> resourceCaches;
     private String hostAddress;
     private static boolean useWrapper = false;
     private boolean install;
     private boolean gui;
     private Throwable startupException;
     // a list of listeners holding the http and https socket listeners.
     private List<SocketListener> listeners;
     private CustomWebApplicationContext webappContext;
     private CustomHttpContext httpContext;
     private String jettyLog;
     private int defaultPort;
     private int actualPort;
     private boolean useDevConfig;
     private String hostname;
     private ServerLock serverLock;
     private Thread mainThread;
     private Thread insecureThread;
     private ThreadGroup threadGroup;
     private TreeMap<String, PropertyDefinition> contextPropertyDefinitions;
     private List<ContextListener> contextListeners;
     private boolean shuttingDown;
     private ContextConfig contextConfiguration;
     private BootProgressMonitor bootProgressMonitor;
     private boolean logToConsole;
     private boolean restarting;
     private Server insecureServer;
     private ServletHandler servletHandler;
 
     public static void setBootLoader(ClassLoader bootLoader) {
         Main.bootLoader = bootLoader;
     }
 
     /**
      * Entry point
      * 
      * @param args
      * @throws Throwable
      */
     public static void main(String[] args) throws Throwable {
 
         // This is a hack to allow the Install4J installer to get the java
         // runtime that will be used
         if (args.length > 0 && args[0].equals("--jvmdir")) {
             System.out.println(SystemProperties.get("java.home"));
             System.exit(0);
         }
         useWrapper = System.getProperty("wrapper.key") != null;
         final Main main = new Main();
         ContextHolder.setContext(main);
 
         if (useWrapper) {
             WrapperManager.start(main, args);
         } else {
             Integer returnCode = main.start(args);
             if (returnCode != null) {
                 if (main.gui) {
                     if (main.startupException == null) {
                         main.startupException = new Exception("An exit code of " + returnCode + " was returned.");
                     }
                     try {
                         if (SystemProperties.get("os.name").toLowerCase().startsWith("windows")) {
                             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                         }
                     } catch (Exception e) {
                     }
                     String mesg = main.startupException.getMessage() == null ? "No message supplied." : main.startupException
                                     .getMessage();
                     StringBuffer buf = new StringBuffer();
                     int l = 0;
                     char ch = ' ';
                     for (int i = 0; i < mesg.length(); i++) {
                         ch = mesg.charAt(i);
                         if (l > 50 && ch == ' ') {
                             buf.append("\n");
                             l = 0;
                         } else {
                             if (ch == '\n') {
                                 l = 0;
                             } else {
                                 l++;
                             }
                             buf.append(ch);
                         }
                     }
                     mesg = buf.toString();
                     final String fMesg = mesg;
                     SwingUtilities.invokeAndWait(new Runnable() {
                         public void run() {
                             JOptionPane.showMessageDialog(null, fMesg, "Startup Error", JOptionPane.ERROR_MESSAGE);
                         }
                     });
                 }
                 System.exit(returnCode.intValue());
             } else {
                 Runtime.getRuntime().addShutdownHook(new Thread() {
                     public void run() {
                         if (!main.shuttingDown) {
                             main.stop(0);
                         }
                     }
                 });
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.tanukisoftware.wrapper.WrapperListener#start(java.lang.String[])
      */
     public Integer start(String[] args) {
         startupStarted = System.currentTimeMillis();
 
         // Inform the wrapper the startup process may take a while
         if (useWrapper) {
             WrapperManager.signalStarting(60000);
         }
 
         // Parse the command line
         Integer returnCode = parseCommandLine(args);
         if (returnCode != null) {
             if (returnCode.intValue() == 999) {
                 return null;
             }
             return returnCode;
         }
 
         // Create the boot progress monitor
         if (gui) {
             try {
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             } catch (Exception e) {
             }
             bootProgressMonitor = new SwingBootProgressMonitor();
         } else {
             bootProgressMonitor = new LogBootProgressMonitor();
         }
 
         //
         resourceCaches = new HashMap<URL, ResourceCache>();
         contextListeners = new ArrayList<ContextListener>();
 
         loadSystemProperties();
         initialiseLogging();
 
         /*
          * Migrate preferences.
          */
         File newPrefDir = new File(ContextHolder.getContext().getConfDirectory(), "prefs");
         PREF = PropertyPreferences.SYSTEM_ROOT;
         try {
             if (!newPrefDir.exists() && Preferences.systemRoot().node("/com").nodeExists("adito")) {
                 Preferences from = Preferences.systemRoot().node("/com/adito");
                 log.warn("Migrating preferences");
                 try {
                     copyNode(from.node("core"), PREF.node("core"));
                     from.node("core").removeNode();
                     copyNode(from.node("plugin"), PREF.node("plugin"));
                     from.node("plugin").removeNode();
                     copyNode(from.node("extensions"), PREF.node("extensions"));
                     from.node("extensions").removeNode();
                     copyNode(from.node("dbupgrader"), PREF.node("dbupgrader"));
                     from.node("dbupgrader").removeNode();
                 } catch (Exception e) {
                     log.error("Failed to migrate preferences.", e);
                 }
                 try {
                     from.flush();
                 } catch (BackingStoreException bse) {
                     log.error("Failed to flush old preferences");
                 }
                 try {
                     PREF.flush();
                 } catch (BackingStoreException bse) {
                     log.error("Failed to flush new preferences");
                 }
                 if (log.isInfoEnabled()) {
                     log.info("Flushing preferences");
                 }
 
             }
         } catch (BackingStoreException bse) {
             log.error("Failed to migrate preferences.", bse);
         }
 
         // Inform the wrapper the startup process is going ok
         if (useWrapper) {
             WrapperManager.signalStarting(60000);
         }
 
         try {
             clearTemp();
             try {
                 hostname = Inet4Address.getLocalHost().getCanonicalHostName();
                 hostAddress = Inet4Address.getLocalHost().getHostAddress();
             } catch (Exception ex) {
                 // This should be fatal, we now rely on the hostname being
                 // available
                 throw new Exception("The host name or address on which this service is running could not "
                                 + "be determined. Check you network configuration. One possible cause is "
                                 + "a misconfigured 'hosts' file (e.g. on UNIX-like systems this would be "
                                 + "/etc/hosts, on Windows XP it would be " + "C:\\Windows\\System32\\Drivers\\Etc\\Hosts).");
             }
 
             PropertyClassManager.getInstance().registerPropertyClass(contextConfiguration = new ContextConfig(getClass().getClassLoader()));
 
             // Display some information about the system we are running on
             displaySystemInfo();
 
             // Load the context property definitions
             loadContextProperties();
 
             // Inform the wrapper the startup process is going ok
             if (useWrapper) {
                 WrapperManager.signalStarting(60000);
             }
 
             // Configure any HTTP / HTTPS / SOCKS proxy servers
             configureProxyServers();
 
             PropertyList l = contextConfiguration.retrievePropertyList(new ContextKey("webServer.bindAddress"));
             getBootProgressMonitor().updateMessage("Creating server lock");
             getBootProgressMonitor().updateProgress(6);
             serverLock = new ServerLock((String) l.get(0));
             if (serverLock.isLocked()) {
                 if (!isSetupMode()) {
                     if (serverLock.isSetup()) {
                         throw new Exception("The installation wizard is currently running. "
                                         + "Please shut this down by pointing your browser " + "to http://" + getHostname() + ":"
                                         + serverLock.getPort() + "/showShutdown.do before attempting to start the server again.");
                     } else {
                         throw new Exception("The server is already running.");
                     }
                 } else {
                     if (!serverLock.isSetup()) {
                         throw new Exception("The server is currently already running. "
                                         + "Please shut this down by pointing your browser " + "to https://" + getHostname() + ":"
                                         + serverLock.getPort() + "/showShutdown.do before attempting to start the server again.");
                     } else {
                         throw new Exception("The installation wizard is running..");
                     }
 
                 }
 
             }
 
             // Inform the wrapper the startup process is going ok
             if (useWrapper) {
                 WrapperManager.signalStarting(60000);
             }
 
             Runtime.getRuntime().addShutdownHook(new Thread() {
                 public void run() {
                     serverLock.stop();
                 }
             });
 
             //
             registerKeyStores();
 
             //
             threadGroup = new ThreadGroup("MainThreadGroup");
 
             if (install) {
                 setupMode();
 
             } else {
                 normalMode();
                 startHttpServer();
             }
         } catch (Throwable t) {
             startupException = t;
             log.error("Failed to start the server. " + t.getMessage(), t);
             return new Integer(1);
         }
 
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#getConfig()
      */
     public PropertyClass getConfig() {
         return contextConfiguration;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.tanukisoftware.wrapper.WrapperListener#controlEvent(int)
      */
     public void controlEvent(int evt) {
         if (evt == WrapperManager.WRAPPER_CTRL_C_EVENT) {
             if (log.isInfoEnabled())
                 log.info("Got CTRL+C event");
             WrapperManager.stop(0);
         } else if (evt == WrapperManager.WRAPPER_CTRL_CLOSE_EVENT) {
             if (log.isInfoEnabled())
                 log.info("Got windows close event, ignoring.");
         } else if (evt == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT) {
             if (log.isInfoEnabled())
                 log.info("Got windows logoff event, ignoring.");
         } else if (evt == WrapperManager.WRAPPER_CTRL_SHUTDOWN_EVENT) {
             if (log.isInfoEnabled())
                 log.info("Got shutdown event");
             WrapperManager.stop(0);
         } else {
             if (log.isInfoEnabled())
                 log.info("Got unknown control event " + evt + ", ignoring.");
         }
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.tanukisoftware.wrapper.WrapperListener#stop(int)
      */
     public int stop(int exitCode) {
         if (log != null) {
             if (log.isInfoEnabled()) {
                 if (restarting)
                     log.info("Restarting the server.");
                 else
                     log.info("Shutting down the server.");
             }
         }
         if (useWrapper) {
             WrapperManager.signalStopping(20000);
         }
 
         // TODO This really screws up wrapper on windows - no idea why
         if (server != null && server.isStarted()) {
             try {
                 server.stop(false);
             } catch (InterruptedException e) {
                 if (log != null) {
                     if (log.isInfoEnabled())
                         log.info("Failed to stop server.", e);
                 }
             }
         }
 
         // Inform all context listeners of what is happening
         for (ContextListener l : contextListeners) {
             new StopContextListenerThread(l).waitForStop();
         }
 
         // 
         if (log.isInfoEnabled()) {
             log.info("Flushing preferences");
         }
         try {
             ContextHolder.getContext().getPreferences().flush();
         } catch (BackingStoreException bse) {
             log.error("Failed to flush context preferences.", bse);
         }
         try {
             Preferences.systemRoot().flush();
         } catch(IllegalStateException ise) {
         } catch (BackingStoreException bse) {
             log.error("Failed to flush system preferences");
         }
 
         return exitCode;
     }
 
     void loadContextProperties() throws IOException, JDOMException {
         getBootProgressMonitor().updateMessage("Loading context properties");
         getBootProgressMonitor().updateProgress(4);
         for (Enumeration<URL> e = getClass().getClassLoader().getResources("META-INF/contextConfig-definitions.xml"); e
                         .hasMoreElements();) {
             URL u = e.nextElement();
             log.info("Loading context property definitions from " + u);
             SAXBuilder build = new SAXBuilder();
             Element root = build.build(u).getRootElement();
             if (!root.getName().equals("definitions")) {
                 throw new JDOMException("Root element in " + u + " should be <definitions>");
             }
             for (Iterator i = root.getChildren().iterator(); i.hasNext();) {
                 Element c = (Element) i.next();
                 if (c.getName().equals("definition")) {
                     DefaultPropertyDefinition def = new XMLPropertyDefinition(c);
                     contextConfiguration.registerPropertyDefinition(def);
                 } else {
                     throw new JDOMException("Expect root element of <definitions> with child elements of <definition>. Got <"
                                     + c.getName() + ">.");
                 }
             }
         }
 
     }
 
     void copyNode(Preferences from, Preferences to) throws BackingStoreException {
         String[] keys = from.keys();
         for (int i = 0; i < keys.length; i++) {
             to.put(keys[i], from.get(keys[i], ""));
         }
         String childNodes[] = from.childrenNames();
         for (int i = 0; i < childNodes.length; i++) {
             Preferences cn = from.node(childNodes[i]);
             Preferences tn = to.node(childNodes[i]);
             copyNode(cn, tn);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#isSetupMode()
      */
     public boolean isSetupMode() {
         return install;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#isRestartAvailableMode()
      */
     public boolean isRestartAvailableMode() {
         return (useDevConfig || useWrapper) && !isSetupMode();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#shutdown(boolean)
      */
     public void shutdown(boolean restart) {
         shuttingDown = true;
         restarting = restart;
         if (useWrapper) {
             if (restart) {
                 WrapperManager.restart();
             } else {
                 WrapperManager.stop(0);
             }
         } else {
             stop(0);
             System.exit(0);
         }
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#getVersion()
      */
     public Version getVersion() {
         return VersionInfo.getVersion();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#getConfDirectory()
      */
     public File getConfDirectory() {
         return CONF_DIR;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#getTempDirectory()
      */
     public File getTempDirectory() {
         return TMP_DIR;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#getLogDirectory()
      */
     public File getLogDirectory() {
         return LOG_DIR;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#getApplicationDirectory()
      */
     public File getApplicationDirectory() {
         return appDir == null ? new File(getTempDirectory(), "extensions") : appDir;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#getDBDirectory()
      */
     public File getDBDirectory() {
         return DB_DIR;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#getMainThread()
      */
     public Thread getMainThread() {
         return mainThread;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#setResourceAlias(java.lang.String,
      *      java.lang.String)
      */
     public void setResourceAlias(String uri, String location) {
         webappContext.setResourceAlias(uri, location);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#addResourceBase(java.net.URL)
      */
     public void addResourceBase(URL base) {
         if (log.isInfoEnabled())
             log.info("Adding new resource base " + base.toExternalForm());
         ResourceCache cache = new ResourceCache();
         cache.setMimeMap(webappContext.getMimeMap());
         cache.setEncodingMap(webappContext.getEncodingMap());
         cache.setResourceBase(base.toExternalForm());
         try {
             cache.start();
             webappContext.addResourceCache(cache);
             if (httpContext != null) {
                 httpContext.addResourceCache(cache);
             }
             resourceCaches.put(base, cache);
         } catch (Exception e) {
             log.error("Failed to add new resource base " + base.toExternalForm() + ".", e);
         }
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#addResourceBase(java.net.URL)
      */
     public void removeResourceBase(URL base) {
         if (log.isInfoEnabled())
             log.info("Removing resource base " + base.toExternalForm());
         ResourceCache cache = (ResourceCache) resourceCaches.get(base);
         webappContext.removeResourceCache(cache);
         if (httpContext != null) {
             httpContext.removeResourceCache(cache);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#getHostname()
      */
     public String getHostname() {
         return hostname;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#getPort()
      */
     public int getPort() {
         return actualPort;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#addContextLoaderURL(java.net.URL)
      */
     public void addContextLoaderURL(URL url) {
         doAddContextLoaderURL(url);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#registerRequestHandler(com.adito.boot.RequestHandler)
      */
     public void registerRequestHandler(RequestHandler requestHandler) {
         registerRequestHandler(requestHandler, HandlerProtocol.HTTPS_PROTOCOL);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#deregisterRequestHandler(com.adito.boot.RequestHandler)
      */
     public void deregisterRequestHandler(RequestHandler requestHandler) {
         if (httpContext != null) {
             httpContext.deregisterRequestHandler(requestHandler);
         }
         HTTPRedirectHandler.registerHandler(requestHandler);
     }
 
     private void registerKeyStores() throws Exception {
         getBootProgressMonitor().updateMessage("Registering key stores");
         getBootProgressMonitor().updateProgress(7);
         String defaultKeyStorePassword = contextConfiguration.retrieveProperty(new ContextKey(
                         "webServer.keystore.sslCertificate.password"));
         KeyStoreManager.registerKeyStore(KeyStoreManager.DEFAULT_KEY_STORE, "keystore", true, defaultKeyStorePassword,
             KeyStoreManager.getKeyStoreType(contextConfiguration.retrieveProperty(new ContextKey("webServer.keyStoreType"))));
 
         KeyStoreManager.registerKeyStore(KeyStoreManager.SERVER_AUTHENTICATION_CERTIFICATES_KEY_STORE, "keystore", true,
             "adito", KeyStoreManager.TYPE_JKS);
         KeyStoreManager.registerKeyStore(KeyStoreManager.TRUSTED_SERVER_CERTIFICATES_KEY_STORE, "keystore", true, "adito",
             KeyStoreManager.TYPE_JKS);
 
     }
 
     private void clearTemp() {
         String currVer = PREF.get("lastTempClear", "");
         if (currVer.equals("") || !currVer.equals(getVersion().toString())
                         || "true".equalsIgnoreCase(SystemProperties.get("adito.clearTemp"))) {
             getBootProgressMonitor().updateMessage("Clearing temporary files");
             getBootProgressMonitor().updateProgress(3);
             if (log.isInfoEnabled())
                 log.info("Clearing temporary directory");
             
             /* We have to leave the server.run and server.pid files alone as these are
              * used by external components to determine service state 
              */ 
             
             File[] files = getTempDirectory().listFiles();
             if(files != null) {
                 for(File file : files) {
                     if(!file.getName().equals(ServerLock.LOCK_NAME) &&
                                     !file.getName().equals(Branding.SERVICE_NAME + ".pid")) {
                         Util.delTree(file);
                     }
                 }
             }
             else {
                 if (!getTempDirectory().mkdirs()) {
                     log.error("CRITICAL. Failed to create the temporary directory " + getTempDirectory() + ".");
                 }
             }
         }
         PREF.put("lastTempClear", getVersion().toString());
     }
 
     private void initialiseLogging() {
         URL resource =  bootLoader.getResource("log4j.properties");
         getBootProgressMonitor().updateMessage("Intialising logging");
         getBootProgressMonitor().updateProgress(2);
         LOG_DIR.mkdirs();
         InputStream in = null;
         try {
             if (resource == null) {
                 throw new IOException("Could not locate log4j.properties");
             }
             in = resource.openStream();
             Properties p = new Properties();
             p.load(in);
             p.setProperty("log4j.rootCategory", p.getProperty("log4j.rootCategory", "WARN,logfile") + ( logToConsole ? ",stdout" : "" ));
             Class.forName("org.apache.log4j.PropertyConfigurator", true, bootLoader).getMethod("configure", new Class[] { Properties.class })
                             .invoke(null, new Object[] { p });
         } catch (Exception e) {
         } finally {
             Util.closeStream(in);
         }
         log = LogFactory.getLog(Main.class);
     }
 
     private void startHttpServer() throws Exception {
 
         int port = contextConfiguration.retrievePropertyInt(new ContextKey("webServer.httpRedirectPort"));
         if (port <= 0) {
             if (log.isInfoEnabled())
                 log.info("HTTP redirect port " + port + " is invalid");
             return;
         }
         
         String bind = contextConfiguration.retrieveProperty(new ContextKey("webServer.bindAddress"));
         PropertyList l = new PropertyList(bind.equals("") ? "0.0.0.0" : bind);
         insecureServer = new Server();
         for (Iterator<String> i = l.iterator(); i.hasNext();) {
             String address = i.next();
             if (log.isInfoEnabled())
                 log.info("Adding listener on " + address + ":" + port);
             SocketListener listener = new SocketListener();
             listener.setHost(address);
             listener.setPort(port);
             listener.setMinThreads(contextConfiguration.retrievePropertyInt(new ContextKey("webServer.minThreads")));
             listener.setMaxThreads(contextConfiguration.retrievePropertyInt(new ContextKey("webServer.maxThreads")));
             listener.setMaxIdleTimeMs(0);
             listener.setLowResourcePersistTimeMs(2000);
             listener.setAcceptQueueSize(0);
             listener.setPoolName("P2");
             insecureServer.addListener(listener);
         }
         
         // Create the webapp
         HttpContext context = new HttpContext();
         context.setContextPath("/");
         context.setResourceBase("./dummy/");
         context.addHandler(new HTTPRedirectHandler());
         insecureServer.addContext(context);
 
         // Configure the server
         insecureServer.setRequestsPerGC(2000);
 
         insecureThread = new Thread(threadGroup, "InsecureWebServer") {
             public void run() {
                 // Start the server
                 try {
                     insecureServer.start();
                 } catch (Exception e) {
                     log.warn("Failed to start HTTP Jetty. " + e.getMessage(), e);
                 }
             }
         };
 
         if (log.isInfoEnabled())
             log.info("Starting HTTP redirect server");
         insecureThread.start();
 
     }
 
     private void normalMode() throws Exception {
 
         getBootProgressMonitor().updateMessage("Creating server");
         getBootProgressMonitor().updateProgress(8);
 
         if (log.isInfoEnabled())
             log.info("Starting Jetty Web Server");
 
         server = createServer();
 
         // SunJsseListener listener = new SunJsseListener();
         String keystorePassword = contextConfiguration
                         .retrieveProperty(new ContextKey("webServer.keystore.sslCertificate.password"));
         if (keystorePassword.equals("")) {
             throw new Exception(
                             "Private key / certificate password has not been set. Please run the Installation Wizard.");
         }
 
         actualPort = defaultPort == -1 ? contextConfiguration.retrievePropertyInt(new ContextKey("webServer.port")) : defaultPort;
         String bind = contextConfiguration.retrieveProperty(new ContextKey("webServer.bindAddress"));
         listeners = new ArrayList<SocketListener>();
         PropertyList l = new PropertyList(bind.equals("") ? "0.0.0.0" : bind);
         for (Iterator<String> i = l.iterator(); i.hasNext();) {
             String address = i.next();
             if (log.isInfoEnabled())
                 log.info("Adding listener on " + address + ":" + actualPort);
             if (!serverLock.isStarted()) {
                 serverLock.start(actualPort);
             }
             SocketListener listener = null;
             if (contextConfiguration.retrieveProperty(new ContextKey("webServer.protocol")).equals("http")) {
                 listener = new SocketListener();
                 log.warn("The server is configured to listen for plain HTTP connections.");
             } else {
                 listener = new CustomJsseListener(keystorePassword);
                 MsieSslHandler sslHandler = new MsieSslHandler();
                 sslHandler.setUserAgentSubString("MSIE 5");
                 listener.setHttpHandler(sslHandler);
             }
             listener.setPort(actualPort);
             listener.setMinThreads(contextConfiguration.retrievePropertyInt(new ContextKey("webServer.minThreads")));
             listener.setMaxThreads(contextConfiguration.retrievePropertyInt(new ContextKey("webServer.maxThreads")));
             listener.setMaxIdleTimeMs(contextConfiguration.retrievePropertyInt(new ContextKey("webServer.maxIdleTimeMs")));
             listener.setHost(address);
             listener.setBufferSize(contextConfiguration.retrievePropertyInt(new ContextKey("webServer.bufferSize")));
             listener.setBufferReserve(contextConfiguration.retrievePropertyInt(new ContextKey("webServer.bufferReserve")));
             listener.setTcpNoDelay(contextConfiguration.retrievePropertyBoolean(new ContextKey("webServer.tcpNoDelay")));
             listener.setThreadsPriority(contextConfiguration.retrievePropertyInt(new ContextKey("webServer.threadPriority")));
             listeners.add(listener);
 
             listener.setLowResourcePersistTimeMs(contextConfiguration.retrievePropertyInt(new ContextKey(
                             "webServer.lowResourcePersistTimeMs")));
             listener.setPoolName("main");
             server.addListener(listener);
         }
         
         // Add the context
         getBootProgressMonitor().updateMessage("Creating web application");
         getBootProgressMonitor().updateProgress(9);
         httpContext = new CustomHttpContext(server, "/", useDevConfig, bootLoader);
         httpContext.setRedirectNullPath(false);
         server.addContext(httpContext);
 
         // Dunny servlet handler for faking HttpServletRequest,
         // HttpServletResponse
         servletHandler = new ServletHandler();
         servletHandler.initialize(httpContext);
         servletHandler.start();
 
         // Add the webapp
         webappContext = new CustomWebApplicationContext(useDevConfig, bootLoader);
         addLifecycleListener(webappContext);
 
         server.addContext(webappContext);
         webappContext.setRedirectNullPath(false);
 
         // Configure the server
         server.setRequestsPerGC(contextConfiguration.retrievePropertyInt(new ContextKey("webServer.requestsPerGC")));
         server.setTrace(false);
 
         // Set the request log
         if (contextConfiguration.retrievePropertyBoolean(new ContextKey("webServer.requestLog"))) {
             NCSARequestLog requestLog = new NCSARequestLog(jettyLog);
             requestLog.setRetainDays(90);
             requestLog.setAppend(true);
             requestLog.setExtended(false);
             requestLog.setBuffered(false);
             requestLog.setLogTimeZone("GMT");
             server.setRequestLog(requestLog);
         }
 
         // Inform the wrapper the startup process is going ok
         if (useWrapper) {
             WrapperManager.signalStarting(60000);
         }
 
         mainThread = new Thread(threadGroup, "WebServer") {
             public void run() {
                 // Start the server
                 try {
                     server.start();
                     if (useDevConfig) {
                         log.warn("Server startup took " + ((System.currentTimeMillis() - startupStarted) / 1000) + " seconds");
                     }
                     getBootProgressMonitor().updateMessage("Server is now running");
                     getBootProgressMonitor().updateProgress(100);
                     Thread.sleep(2000);
                 } catch (Exception e) {
                     log.error("Failed to start Jetty. " + e.getMessage(), e);
                     if (useWrapper) {
                         WrapperManager.stop(1);
                     } else {
                         System.exit(1);
                     }
                 } finally {
                     getBootProgressMonitor().dispose();
                 }
             }
         };
         mainThread.start();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#addWebApp(java.lang.String,
      *      java.lang.String)
      */
     public void addWebApp(String path, String warFile) throws Exception {
         log.info("Adding webapp '" + path + "' using path / war '" + warFile + "'");
         HttpContext context = server.addWebApplication(path, warFile);
         context.start();
     }
 
     private void setupMode() throws Exception {
 
     	// Ensure that https redirect is not turned on in jetty
     	System.setProperty("jetty.force.HTTPSRedirect", "false");
     	
         getBootProgressMonitor().updateMessage("Creating server");
         getBootProgressMonitor().updateProgress(8);
 
         actualPort = defaultPort == -1 ? 28080 : defaultPort;
         serverLock.start(actualPort);
 
         server = createServer();
 
         SocketListener socketListener = new SocketListener();
         socketListener.setPort(actualPort);
         socketListener.setMinThreads(10);
         socketListener.setMaxThreads(200);
         socketListener.setMaxIdleTimeMs(0);
         socketListener.setLowResourcePersistTimeMs(2000);
         socketListener.setAcceptQueueSize(0);
         socketListener.setPoolName("P1");
         server.addListener(socketListener);
 
         // // Add the context
         // HttpContext context = new CustomHttpContext(server, "/",
         // useDevConfig);
         // server.addContext(context);
 
         // Create the webapp
 
         getBootProgressMonitor().updateMessage("Creating web application");
         getBootProgressMonitor().updateProgress(9);
 
         webappContext = new CustomWebApplicationContext(useDevConfig, bootLoader);
         webappContext.setRedirectNullPath(false);
         addLifecycleListener(webappContext);
         server.addContext(webappContext);
 
         // Configure the server
         server.setRequestsPerGC(2000);
 
         String realHostname = hostname == null ? InetAddress.getLocalHost().getHostName() : hostname;
 
         /*
          * If the 'Active DNS' feature is enabled, the DNS server may return the
          * wild-card name. This will probably fail. As a work-around, if the
          * hostname looks like a wildcard, then it is simply changed to
          * 'localhost'.
          */
         if (realHostname.startsWith("*.")) {
             realHostname = "localhost";
         }
 
         //
         final String fRealHostname = realHostname;
         final int realPort = defaultPort == -1 ? 28080 : defaultPort;
 
         // Inform the wrapper the startup process is going ok
         if (useWrapper) {
             WrapperManager.signalStarting(60000);
         }
 
         mainThread = new Thread(threadGroup, "WebServer") {
             public void run() {
                 // Start the server
                 try {
                     server.start();
 
                     if (!useWrapper && !"true".equals(SystemProperties.get("adito.noBrowserLaunch"))) {
                         try {
                             BrowserLauncher.openURL("http://" + fRealHostname + ":" + realPort);
                             System.out.println("A browser has been opened and pointed to http://" + fRealHostname + ":" + realPort
                                             + ". ");
                         } catch (Exception ex) {
                             System.out.println("Point your browser to http://" + fRealHostname + ":" + realPort + ". ");
                         }
                     } else {
                         System.out.println("Point your browser to http://" + fRealHostname + ":" + realPort + ". ");
                     }
                     System.out
                                     .println("\nPress CTRL+C or use the 'Shutdown' option from the web interface to leave the installation wizard.");
                     getBootProgressMonitor().updateMessage("Server is now running");
                     getBootProgressMonitor().updateProgress(100);
                     Thread.sleep(2000);
                 } catch (Exception e) {
                     log.error("Failed to start Jetty. " + e.getMessage(), e);
                     if (useWrapper) {
                         WrapperManager.stop(1);
                     } else {
                         System.exit(1);
                     }
                 } finally {
                     getBootProgressMonitor().dispose();
                 }
             }
         };
 
         System.out.print("Starting installation wizard");
         mainThread.start();
 
         /*
          * Wait for up to 5 minutes for the server to become available we need
          * to wait this long because precompilation can take a while!
          */
 
         int waitFor = 60 * 5;
 
         boolean running = false;
 
         if (!"true".equals(SystemProperties.get("adito.disableStartupCheck", "false"))) {
             int i = 0;
             for (; i < waitFor && !running; i++) {
                 try {
                     System.out.print(".");
                     Socket s = new Socket(realHostname, realPort);
                     s.close();
                     running = true;
                 } catch (Exception ex) {
                     try {
                         Thread.sleep(1000);
                     } catch (Exception ex2) {
                     }
                 }
             }
             System.out.println();
         } else {
             running = true;
         }
         if (!running) {
             System.out.println("Failed to start installation wizard. Check the logs for more detail.");
             if (useWrapper) {
                 WrapperManager.stop(1);
             } else {
                 System.exit(1);
             }
         }
     }
 
     private void configureProxyServers() throws Exception {
         getBootProgressMonitor().updateMessage("Configuring proxy servers");
         getBootProgressMonitor().updateProgress(5);
 
         String httpProxyHost = contextConfiguration.retrieveProperty(new ContextKey("proxies.http.proxyHost"));
         if (!httpProxyHost.equals("")) {
             if (log.isInfoEnabled())
                 log.info("Configuring outgoing HTTP connections to use a proxy server.");
             System.setProperty("http.proxyHost", httpProxyHost);
             System.setProperty("com.maverick.ssl.https.HTTPProxyHostname", httpProxyHost);
             String httpProxyPort = contextConfiguration.retrieveProperty(new ContextKey("proxies.http.proxyPort"));
 
             String httpProxyUsername = contextConfiguration.retrieveProperty(new ContextKey("proxies.http.proxyUser"));
             String httpProxyPassword = contextConfiguration.retrieveProperty(new ContextKey("proxies.http.proxyPassword"));
 
             System.setProperty("http.proxyPort", httpProxyPort);
             System.setProperty("com.maverick.ssl.https.HTTPProxyPort", httpProxyPort);
 
             if (!httpProxyUsername.trim().equals(""))
                 System.setProperty("com.maverick.ssl.https.HTTPProxyUsername", httpProxyUsername.trim());
 
             if (!httpProxyPassword.trim().equals(""))
                 System.setProperty("com.maverick.ssl.https.HTTPProxyPassword", httpProxyPassword.trim());
 
             System.setProperty("com.maverick.ssl.https.HTTPProxySecure", "false");
 
             PropertyList list = contextConfiguration.retrievePropertyList(new ContextKey("proxies.http.nonProxyHosts"));
             StringBuffer hosts = new StringBuffer();
             for (Iterator i = list.iterator(); i.hasNext();) {
                 if (hosts.length() != 0) {
                     hosts.append("|");
                 }
                 hosts.append(i.next());
             }
             System.setProperty("http.nonProxyHosts", hosts.toString());
             System.setProperty("com.maverick.ssl.https.HTTPProxyNonProxyHosts", hosts.toString());
         }
         String socksProxyHost = contextConfiguration.retrieveProperty(new ContextKey("proxies.socksProxyHost"));
         if (!socksProxyHost.equals("")) {
             if (log.isInfoEnabled())
                 log.info("Configuring outgoing TCP/IP connections to use a SOCKS proxy server.");
             System.setProperty("socksProxyHost", httpProxyHost);
             System.setProperty("socksProxyPort", contextConfiguration.retrieveProperty(new ContextKey("proxies.socksProxyPort")));
         }
         if (!socksProxyHost.equals("") || !httpProxyHost.equals("")) {
             Authenticator.setDefault(new ProxyAuthenticator());
         }
     }
 
     private Server createServer() throws MalformedURLException {
         Server server = new Server();
         if (contextConfiguration.retrievePropertyBoolean(new ContextKey("webServer.stats"))) {
             new StatsLogger(server, contextConfiguration.retrievePropertyInt(new ContextKey("webServer.statsUpdate")));
         }
         return server;
     }
 
     private void addLifecycleListener(final CustomWebApplicationContext context) {
         context.addEventListener(new LifeCycleListener() {
             public void lifeCycleFailure(LifeCycleEvent arg0) {
             }
 
             public void lifeCycleStarted(LifeCycleEvent arg0) {
                 getBootProgressMonitor().updateMessage("Server is now running");
                 getBootProgressMonitor().updateProgress(100);
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException e) {
                 }
             }
 
             public void lifeCycleStarting(LifeCycleEvent arg0) {
             }
 
             public void lifeCycleStopped(LifeCycleEvent arg0) {
             }
 
             public void lifeCycleStopping(LifeCycleEvent arg0) {
             }
         });
     }
 
     private void displaySystemInfo() throws SocketException {
 
         //
 
         if (useDevConfig) {
             log.warn("Development environment enabled. Do not use this on a production server.");
         }
 
         if (log.isInfoEnabled()) {
            	log.info("Version is " + ContextHolder.getContext().getVersion());
             log.info("Java version is " + SystemProperties.get("java.version"));
             log.info("Server is installed on " + hostname + "/" + hostAddress);
             log.info("Configuration: " + CONF_DIR.getAbsolutePath());
         }
 
         if(SystemProperties.get("java.vm.name", "").indexOf("GNU") > -1
         		|| SystemProperties.get("java.vm.name", "").indexOf("libgcj") > -1) 
         {
         	System.out.println("********** WARNING **********");
         	System.out.println("The system has detected that the Java runtime is GNU/GCJ");
         	System.out.println("Adito does not work correctly with this Java runtime");
         	System.out.println("you should reconfigure with a different runtime");
         	System.out.println("*****************************");
         	
         	
         	log.warn("********** WARNING **********");
         	log.warn("The system has detected that the Java runtime is GNU/GCJ");
         	log.warn("Adito may not work correctly with this Java runtime");
         	log.warn("you should reconfigure with a different runtime");
         	log.warn("*****************************");
 
         }
         
         Enumeration e = NetworkInterface.getNetworkInterfaces();
 
         while (e.hasMoreElements()) {
             NetworkInterface netface = (NetworkInterface) e.nextElement();
             if (log.isInfoEnabled())
                 log.info("Net interface: " + netface.getName());
 
             Enumeration e2 = netface.getInetAddresses();
 
             while (e2.hasMoreElements()) {
                 InetAddress ip = (InetAddress) e2.nextElement();
                 if (log.isInfoEnabled())
                     log.info("IP address: " + ip.toString());
             }
         }
 
         if (log.isInfoEnabled())
             log.info("System properties follow:");
         Properties sysProps = System.getProperties();
         for (Iterator i = sysProps.entrySet().iterator(); i.hasNext();) {
             Map.Entry entry = (Map.Entry) i.next();
             int idx = 0;
             String val = (String) entry.getValue();
             while (true) {
                 if (entry.getKey().equals("java.class.path")) {
                     StringTokenizer t = new StringTokenizer(entry.getValue().toString(), SystemProperties.get("path.separator", ","));
                     while (t.hasMoreTokens()) {
                         String s = t.nextToken();
                         if (log.isInfoEnabled())
                             log.info("java.class.path=" + s);
                     }
                     break;
                 } else {
                     if ((val.length() - idx) > 256) {
                         if (log.isInfoEnabled())
                             log.info("  " + entry.getKey() + "=" + val.substring(idx, idx + 256));
                         idx += 256;
                     } else {
                         if (log.isInfoEnabled())
                             log.info("  " + entry.getKey() + "=" + val.substring(idx));
                         break;
                     }
                 }
             }
         }
     }
 
     private void loadSystemProperties() {
         getBootProgressMonitor().updateMessage("Loading system properties");
         getBootProgressMonitor().updateProgress(1);
 
         /*
          * Read in system properties from a resource, more a debugging aid than
          * anything else
          */
         InputStream in = null;
         try {
             File f = new File(CONF_DIR, "system.properties");
             in = new FileInputStream(f);
             Properties p = new Properties();
             p.load(in);
             for (Enumeration e = p.keys(); e.hasMoreElements();) {
                 String k = (String) e.nextElement();
                 System.getProperties().setProperty(k, p.getProperty(k).trim());
             }
         } catch (IOException e) {
             // Dont care
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException ioe) {
 
                 }
             }
         }
         
         /**
          * Set the prefix if any. 
          */
         SystemProperties.setPrefix(System.getProperty("boot.propertyPrefix"));
 
         /**
          * Are we in development mode?
          */
         useDevConfig = "true".equalsIgnoreCase(SystemProperties.get("adito.useDevConfig"));
 
         if (!"".equals(SystemProperties.get("adito.extensions", ""))) {
             appDir = new File(SystemProperties.get("adito.extensions"));
         }
 
         //
         System.setProperty("org.mortbay.jetty.servlet.SessionCookie", SystemProperties.get("adito.cookie", "JSESSIONID"));
         System.setProperty("org.mortbay.jetty.servlet.SessionURL", SystemProperties.get("adito.cookie", "JSESSIONID").toLowerCase());
     }
 
     private Integer parseCommandLine(String[] args) {
         defaultPort = -1;
         logToConsole = false;
         jettyLog = "logs/yyyy_mm_dd.request.log";
         boolean fullReset = false;
         String os = System.getProperty("os.name").toLowerCase();
         gui = "true".equals(System.getProperty("adito.useDevConfig")) && os.startsWith("windows");
         try {
             for (int i = 0; i < args.length; i++) {
                 if (args[i].equals("--manager")) {
                     System.err
                                     .println("The database manager can no longer be started via the server command line. Run it directly from hsqldb.jar ensuring you have the webapp classes included in your class path.");
                     return new Integer(1);
                 } else if (args[i].equals("--setup")) {
                     System.err.println("WARNING: --setup is deprecated, please use --install");
                     install = true;
                 } else if (args[i].equals("--install")) {
                     install = true;
                 } else if (args[i].equals("--logToConsole")) {
                     logToConsole = true;
                 } else if (args[i].equals("--gui")) {
                     if (os.startsWith("windows")) {
                         gui = true;
                     } else if (os.equals("linux") || os.equals("solaris") || os.endsWith("aix")) {
                         String displaySysProp = SystemProperties.get("adito.display", "");
                         String display = null;
                         try {
                             display = displaySysProp.equals("") ? System.getenv("DISPLAY") : displaySysProp;
                         } catch (Throwable t) {
                         }
                         gui = display != null && display.length() > 0;
                     }
                 } else if (args[i].startsWith("--db")) {
                     DB_DIR = new File(args[i].substring(5));
                     if (DB_DIR.exists() && !DB_DIR.isDirectory()) {
                         throw new Exception("--db option specifies an existing file, must either not exist or be a directory");
                     }
                 } else if (args[i].startsWith("--applications")) {
                     appDir = new File(args[i].substring(15));
                     if (appDir.exists() && !appDir.isDirectory()) {
                         throw new Exception("--db option specifies an existing file, must either not exist or be a directory");
                     }
                 } else if (args[i].startsWith("--temp")) {
                     TMP_DIR = new File(args[i].substring(7));
                     if (TMP_DIR.exists() && !TMP_DIR.isDirectory()) {
                         throw new Exception("--temp option specifies an existing file, must either not exist or be a directory");
                     }
                 } else if (args[i].startsWith("--conf")) {
                     CONF_DIR = new File(args[i].substring(7));
                     if (!CONF_DIR.exists() || !CONF_DIR.isDirectory()) {
                         throw new Exception("--conf option does not specify a valid directory");
                     }
                 } else if (args[i].startsWith("--port")) {
                     defaultPort = Integer.parseInt(args[i].substring(7));
                 } else if (args[i].startsWith("--jettyLog")) {
                     jettyLog = args[i].substring(11);
                 } else if (args[i].equals("--full-reset")) {
                     fullReset = true;
                 } else if (args[i].startsWith("start")) {
                     // For compatibility with the install4j launcher
                 } else {
                     System.err.println("Starts / configures the server.\n");
                     System.err.println("Usage: adito [OPTION]...");
                     System.err.println("\nThe server may be started in setup or normal mode. When setup.\n");
                     System.err.println("mode is enabled a plain http server will be started on port 28080\n");
                     System.err.println("allowing you configure using a browser.\n\n");
                     System.out.println("Options:\n");
                     System.out.println(" --install         Start the server in installation mode.");
                     System.out.println(" --full-reset      Deletes *all* configuration data and resets");
                     System.out.println("                   the server to its initial state. Use with");
                     System.out.println("                   greate caution.");
                     System.out.println(" --db=DIR          Set the directory where the configuration");
                     System.out.println("                   database is stored.");
                     System.out.println(" --conf=DIR        Set the directory where the configuration");
                     System.out.println("                   files are stored.");
                     System.out.println(" --temp=DIR        Set the directory where the temporary");
                     System.out.println("                   files are stored.");
                     System.out.println(" --port=NUMBER     The port on which the server will start.");
                     System.out.println("                   Note that this applies to both setup and");
                     System.out.println("                   normal mode and will overide whatever port");
                     System.out.println("                   been configured.");
                     System.out.println(" --jettyLog=LOG    The location of the Jetty NCSA request log.");
                     System.out.println("\nInvalid option: " + args[i] + ".\n");
                     return new Integer(2);
                 }
             }
 
             // Create the temporary directory
             if (!TMP_DIR.exists()) {
                 if (!TMP_DIR.mkdirs()) {
                     throw new Exception("Could not create temporary directory " + TMP_DIR.getAbsolutePath() + ".");
                 }
             }
         } catch (Exception e) {
             System.err.println(e.getMessage());
             return new Integer(2);
         }
 
         // Perform a full reset
         if (fullReset) {
             if (fullReset()) {
                 System.err.println("Configuration has been fully reset");
                 return new Integer(0);
             } else {
                 System.err.println("Aborted full reset.");
                 return new Integer(1);
             }
         }
 
         // Another way for external processes to force starting installation
         // wizard
         if (new File(TMP_DIR, "setup.run").exists()) {
             install = true;
         }
 
         return null;
 
     }
 
     /*
      * Perform a full reset
      */
     private boolean fullReset() {
         if (gui) {
             if (JOptionPane.showConfirmDialog(null, "The embedded configuration database will be\n"
                             + "completely deleted and re-created the next\ntime you run the server. Are you absolutely\n"
                             + "sure you wish to do this?", "Full Reset", JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION) {
                 return false;
             }
         } else {
             System.out.println("The embedded configuration database will be");
             System.out.println("completely deleted and re-created the next");
             System.out.println("time you run the server. Are you absolutely");
             System.out.println("sure you wish to do this?");
             System.out.println();
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
             while (true) {
                 System.out.print("(Y)es or (N)o: ");
                 String s;
                 try {
                     s = br.readLine();
                     if (s == null) {
                         return false;
                     }
                     if (s.toLowerCase().equals("y") || s.toLowerCase().equals("yes")) {
                         break;
                     }
                     if (s.toLowerCase().equals("n") || s.toLowerCase().equals("no")) {
                         return false;
                     }
                     System.out.println("\nPlease answer 'y' or 'yes' to perform the reset, or 'n' or 'no' to abort the reset.");
                 } catch (IOException e) {
                     return false;
                 }
             }
         }
 
         // Start the reset
         System.out.println("Resetting all configuration");
         File[] f = getDBDirectory().listFiles();
         if (f != null) {
             for (int i = 0; i < f.length; i++) {
                 if (!f[i].getName().equals("CVS") && !f[i].equals(".cvsignore")) {
                     System.out.println("    Deleting " + f[i].getPath());
                     if (!f[i].delete()) {
                         System.out.println("        Failed to remove");
                     }
                 }
             }
         }
 
         return true;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#deobfuscatePassword(java.lang.String)
      */
     public String deobfuscatePassword(String val) {
         try {
             return Password.deobfuscate(val);
         } catch (Exception e) {
             return "";
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#obfuscatePassword(java.lang.String)
      */
     public String obfuscatePassword(String val) {
         return Password.obfuscate(val);
     }
 
     /*
      * (non-Javadoc)
      * 
     * @see com.adito.boot.Context#setTrustMananger(javax.net.ssl.TrustManager)
      */
    public void setTrustMananger(TrustManager trustManager, boolean require) {
         if (listeners == null || listeners.size() == 0) {
             log.warn("Not setting trust managers there are no SSL listeners configured.");
         } else {
             if (log.isInfoEnabled())
                 log.info("Set trust managers");
             for (Iterator i = listeners.iterator(); i.hasNext();) {
                 SocketListener l = (SocketListener) i.next();
                 if (l instanceof CustomJsseListener) {
                     ((CustomJsseListener) l).setNeedClientAuth(trustManager != null);
                     ((CustomJsseListener) l).setTrustManager(trustManager, require);
                 }
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#addContextListener(com.adito.boot.ContextListener)
      */
     public void addContextListener(ContextListener contextListener) {
         contextListeners.add(contextListener);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#removeContextListener(com.adito.boot.ContextListener)
      */
     public void removeContextListener(ContextListener contextListener) {
         contextListeners.remove(contextListener);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#getPreferences()
      */
     public Preferences getPreferences() {
         return PREF;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#getContextLoaderClassPath()
      */
     public URL[] getContextLoaderClassPath() {
         List<URL> urlList = new ArrayList<URL>();
         ClassLoader webappContextClassLoader = webappContext.getClassLoader();
         while (webappContextClassLoader != null) {
             if (webappContextClassLoader != null && webappContextClassLoader instanceof URLClassLoader) {
                 urlList.addAll(Arrays.asList(((URLClassLoader) webappContextClassLoader).getURLs()));
             }
             webappContextClassLoader = webappContextClassLoader.getParent();
         }
         URL[] urls = (URL[]) urlList.toArray(new URL[urlList.size()]);
         return urls;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#getContextLoader()
      */
     public ClassLoader getContextLoader() {
         return webappContext.getClassLoader();
     }
 
     public void removeResourceAlias(String uri) {
         webappContext.removeResourceAlias(uri);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#getResourceBases()
      */
     public Collection<URL> getResourceBases() {
         return resourceCaches.keySet();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#getBootProgressMonitor()
      */
     public BootProgressMonitor getBootProgressMonitor() {
         return bootProgressMonitor;
     }
 
     public void registerRequestHandler(RequestHandler requestHandler, HandlerProtocol protocol) {
         if (httpContext != null) {
             if (protocol == HandlerProtocol.HTTPS_PROTOCOL || protocol == HandlerProtocol.BOTH_PROTOCOLS)
                 httpContext.registerRequestHandler(requestHandler);
         }
 
         if (protocol == HandlerProtocol.HTTP_PROTOCOL || protocol == HandlerProtocol.BOTH_PROTOCOLS) {
             HTTPRedirectHandler.registerHandler(requestHandler);
         }
 
     }
 
     public HttpServletRequest createServletRequest(RequestHandlerRequest request) {
 
         if (request instanceof com.adito.server.jetty.RequestAdapter) {
             ServletHttpRequest req = new ServletHttpRequest(servletHandler, request.getPath(),
                             ((com.adito.server.jetty.RequestAdapter) request).getHttpRequest());
             return req;
         } else
             throw new IllegalArgumentException("Request must be RequestAdapter");
 
     }
 
     public HttpServletResponse createServletResponse(RequestHandlerResponse response, HttpServletRequest request) {
         if (response instanceof com.adito.server.jetty.ResponseAdapter) {
             ServletHttpResponse res = new ServletHttpResponse((ServletHttpRequest) request,
                             ((com.adito.server.jetty.ResponseAdapter) response).getHttpResponse());
             ((ServletHttpRequest) request).getSession(true);
             return res;
         } else
             throw new IllegalArgumentException("Response must be ResponseAdapter");
     }
 
     private void doAddContextLoaderURL(URL u) {
         try {
             Class sysclass = URLClassLoader.class;
             Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
             method.setAccessible(true);
             method.invoke(webappContext.getClassLoader(), new Object[] { u });
             if (log.isInfoEnabled())
                 log.info(u.toExternalForm() + " added to context classloader");
         } catch (Exception e) {
             log.error("Failed to add to classpath.", e);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.adito.boot.Context#access(javax.servlet.http.HttpSession)
      */
     public void access(HttpSession session) {
         ((SessionManager.Session) session).access();
 
     }
 
 }
