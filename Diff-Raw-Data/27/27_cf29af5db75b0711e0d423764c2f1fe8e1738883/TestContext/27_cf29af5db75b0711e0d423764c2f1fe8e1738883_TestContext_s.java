 package com.adito.testcontainer;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Properties;
 import java.util.prefs.Preferences;
 
 import javax.net.ssl.TrustManager;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.meterware.servletunit.ServletRunner;
 import com.adito.boot.BootProgressMonitor;
 import com.adito.boot.Context;
 import com.adito.boot.ContextHolder;
 import com.adito.boot.ContextListener;
 import com.adito.boot.LogBootProgressMonitor;
 import com.adito.boot.PropertyClass;
 import com.adito.boot.PropertyPreferences;
 import com.adito.boot.RequestHandler;
 import com.adito.boot.RequestHandlerRequest;
 import com.adito.boot.RequestHandlerResponse;
 import com.adito.boot.StopContextListenerThread;
 import com.adito.boot.SystemProperties;
 import com.adito.boot.VersionInfo;
 import com.adito.boot.VersionInfo.Version;
 
 /**
  */
 public class TestContext implements Context {
 
     private final static Log LOG = LogFactory.getLog(TestContext.class);
 
     static {
         File f = new File("target/tmp/db");
         f.mkdirs();
     }
     final static File TMP_DIR = new File("target/tmp");
     final static File DB_DIR = new File("target/tmp/db");
     final static File LOG_DIR = new File("target/logs");
     private File appDir = null;
     private List<ContextListener> listeners = new ArrayList<ContextListener>();
     private File confDir;
     private String hostname;
     private Thread mainThread;
     private Preferences pref;
     private boolean setup;
     private ServletRunner runner;
     private BootProgressMonitor bootProgressMonitor = new LogBootProgressMonitor();
 
     ;
 
     /**
      * @param confDir
      * @param setup
      * @throws Exception
      */
     public TestContext(File confDir, boolean setup) throws Exception {
         ContextHolder.setContext(this);
         this.confDir = confDir;
         loadSystemProperties();
         if (!"".equals(SystemProperties.get("adito.extensions", ""))) {
             appDir = new File(SystemProperties.get("adito.extensions"));
         }
         hostname = InetAddress.getLocalHost().getCanonicalHostName();
         mainThread = Thread.currentThread();
         this.setup = setup;
         pref = PropertyPreferences.SYSTEM_ROOT;
         LOG.info("Creating runner");
         runner = new ServletRunner(new File("src/main/webapp/WEB-INF/web.xml"), "/webapp");
     }
 
     private void loadSystemProperties() {
         /*
          * Read in system properties from a resource, more a debugging aid than
          * anything else
          */
         InputStream in = null;
         try {
             File f = new File(getConfDirectory(), "system.properties");
             in = new FileInputStream(f);
             Properties p = new Properties();
             p.load(in);
             for (Enumeration e = p.keys(); e.hasMoreElements();) {
                 String k = (String) e.nextElement();
 
                 // The test may have already specified devExtensions
                 if (!k.equals("adito.devExtensions") || SystemProperties.get("adito.devExtensions") == null) {
                     System.getProperties().setProperty(k, p.getProperty(k).trim());
                 }
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
     }
 
     public void addContextListener(ContextListener contextListener) {
         listeners.add(contextListener);
 
     }
 
     public void addContextLoaderURL(URL u) {
         try {
             URLClassLoader sysloader = (URLClassLoader) getClass().getClassLoader();
             Class sysclass = URLClassLoader.class;
             Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
             method.setAccessible(true);
             method.invoke(sysloader, new Object[]{u});
             if (LOG.isInfoEnabled()) {
                 LOG.info(u.toExternalForm() + " added to context classloader");
             }
         } catch (Exception e) {
             LOG.error("Failed to add to classpath.", e);
         }
     }
 
     public void addResourceBase(URL url) {
         // Don't care
     }
 
     public void addWebApp(String contextPathSpec, String webApp) throws Exception {
         // Don't care
     }
 
     public Collection<URL> getResourceBases() {
         return new ArrayList<URL>();
     }
 
     public String deobfuscatePassword(String s) {
         if (s.startsWith("OBF:")) {
             s = s.substring(4);
         }
 
         byte[] b = new byte[s.length() / 2];
         int l = 0;
         for (int i = 0; i < s.length(); i += 4) {
             String x = s.substring(i, i + 4);
             int i0 = Integer.parseInt(x, 36);
             int i1 = (i0 / 256);
             int i2 = (i0 % 256);
             b[l++] = (byte) ((i1 + i2 - 254) / 2);
         }
 
         return new String(b, 0, l);
     }
 
     public void deregisterRequestHandler(RequestHandler requestHandler) {
         // Don't care
     }
 
     public File getApplicationDirectory() {
         return appDir == null ? new File(getConfDirectory(), "extensions") : appDir;
     }
 
     public File getConfDirectory() {
         return confDir;
     }
 
     public File getDBDirectory() {
         return DB_DIR;
     }
 
     public String getHostname() {
         return hostname;
     }
 
     public File getLogDirectory() {
         return LOG_DIR;
     }
 
     public Thread getMainThread() {
         return mainThread;
     }
 
     public int getPort() {
         // No port
         return 0;
     }
 
     public Preferences getPreferences() {
         return pref;
     }
 
     public File getTempDirectory() {
         return TMP_DIR;
     }
 
     public Version getVersion() {
         return VersionInfo.getVersion();
     }
 
     public boolean isRestartAvailableMode() {
         return false;
     }
 
     public boolean isSetupMode() {
         return setup;
     }
 
     public String obfuscatePassword(String s) {
         StringBuffer buf = new StringBuffer();
         byte[] b = s.getBytes();
 
         synchronized (buf) {
             buf.append("OBF:");
             for (int i = 0; i < b.length; i++) {
                 byte b1 = b[i];
                 byte b2 = b[s.length() - (i + 1)];
                 int i1 = (int) b1 + (int) b2 + 127;
                 int i2 = (int) b1 - (int) b2 + 127;
                 int i0 = i1 * 256 + i2;
                 String x = Integer.toString(i0, 36);
 
                 switch (x.length()) {
                     case 1:
                         buf.append('0');
                     case 2:
                         buf.append('0');
                     case 3:
                         buf.append('0');
                     default:
                         buf.append(x);
                 }
             }
             return buf.toString();
         }
     }
 
     public void registerRequestHandler(RequestHandler requestHandler) {
         // Don't care
     }
 
     public void removeContextListener(ContextListener contextListener) {
         listeners.remove(contextListener);
     }
 
     public void removeResourceBase(URL url) {
         // Don't care
     }
 
     /**
      * @param key
      * @param value
      * @return String
      */
     public String setContextProperty(String key, String value) {
         // No context properties
         return null;
     }
 
    public void setTrustMananger(TrustManager trustManager, boolean require) {
         // No SSL
     }
 
     public void shutdown(boolean restart) {
         // Inform all context listeners of what is happening
         for (ContextListener l : listeners) {
             new StopContextListenerThread(l).waitForStop();
         }
         runner.shutDown();
     }
 
     public URL[] getContextLoaderClassPath() {
         return ((URLClassLoader) getClass().getClassLoader()).getURLs();
     }
 
     public ClassLoader getContextLoader() {
         return getClass().getClassLoader();
     }
 
     /**
      * @return Context
      * @throws Exception
      */
     public static Context getTestContext() throws Exception {
         return new TestContext(new File("devconf"), false);
     }
 
     public void setResourceAlias(String uri, String location) {
         // Not supported
     }
 
     public PropertyClass getConfig() {
         return null;
     }
 
     public void removeResourceAlias(String uri) {
         // Not supported
     }
 
     public BootProgressMonitor getBootProgressMonitor() {
         return bootProgressMonitor;
     }
 
     public void registerRequestHandler(RequestHandler requestHandler, HandlerProtocol protocol) {
         // DONT CARE
     }
 
     public HttpServletRequest createServletRequest(RequestHandlerRequest request) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public HttpServletResponse createServletResponse(RequestHandlerResponse response, HttpServletRequest request) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public void access(HttpSession session) {
     }
}
