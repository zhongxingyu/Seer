 // See the COPYRIGHT file for copyright and license information
 package org.znerd.logdoc;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import org.znerd.logdoc.internal.LogCentral;
 import org.znerd.util.Preconditions;
 import org.znerd.util.log.Limb;
 import org.znerd.util.log.LogLevel;
 
 /**
  * Class that represents the Logdoc library.
  * <p>
  * When this class is run as a program, then it prints the name and version (if known) of this library to standard out.
  */
 public final class Library {
 
    private static final String LOG_LOCALE_PROPERTY;
    private static final String LOG_FILTER_PROPERTY;
     public static final String DEFAULT_LOCALE;
     private static final LogBridge DEFAULT_LOG_BRIDGE;
     private static final String VERSION;
     private static String CURRENT_LOCALE;
     private static LogBridge CURRENT_LOG_BRIDGE;
     private static boolean STACK_TRACE_AT_MESSAGE_LEVEL;
     private static LogFilter LOG_FILTER;
 
     private Library() {
     }

     static {
        LOG_LOCALE_PROPERTY = "org.znerd.logdoc.locale";
        LOG_FILTER_PROPERTY = "org.znerd.logdoc.filterClass";
         DEFAULT_LOCALE = "en_US";
         DEFAULT_LOG_BRIDGE = JulLogBridge.getInstance();
         VERSION = Library.class.getPackage().getImplementationVersion();
         CURRENT_LOCALE = determineStartupLocale();
         CURRENT_LOG_BRIDGE = DEFAULT_LOG_BRIDGE;
         LOG_FILTER = initLogFilter();
     }
 
     private static String determineStartupLocale() {
         String locale = System.getProperty(LOG_LOCALE_PROPERTY);
         if (locale == null || locale.trim().length() < 1) {
             return DEFAULT_LOCALE;
         } else {
             return locale;
         }
     }
 
     private static LogFilter initLogFilter() {
         try {
             return initLogFilterImpl();
         } catch (Exception cause) {
             throw new RuntimeException("Failed to initialize log filter.", cause);
         }
     }
 
     private static LogFilter initLogFilterImpl() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
         String s = System.getProperty(LOG_FILTER_PROPERTY);
         if (s == null || s.trim().length() < 1) {
             return new SimpleLogFilter();
         } else {
             return createLogFilterByClassName(s);
         }
     }
 
     private static LogFilter createLogFilterByClassName(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
         return (LogFilter) Class.forName(className).newInstance();
     }
 
     public static final void main(String[] args) {
         System.out.println(getNameAndVersion());
     }
 
     private static String getNameAndVersion() {
         String name = getName();
         String version = getVersion();
         return version == null ? name : name + " " + version;
     }
 
     /**
      * Returns the official human-readable name of this library.
      * 
      * @return the name, for example <code>"Logdoc"</code>, never <code>null</code>.
      */
     public static final String getName() {
         return "Logdoc";
     }
 
     /**
      * Returns the version of this library.
      * 
      * @return the version of this library, for example <code>"3.0"</code>, or <code>null</code> if unknown.
      */
     public static final String getVersion() {
         return VERSION;
     }
 
     /**
      * Sets the locale for the complete Logdoc library.
      * 
      * @param newLocale
      *            the new locale, cannot be <code>null</code>.
      * @throws UnsupportedLocaleException
      *             if the specified locale is not supported by <em>all</em> registered <code>Log</code> classes.
      */
     public static synchronized void setLocale(String newLocale) throws UnsupportedLocaleException {
         // TODO: Log?
         Preconditions.checkArgument(newLocale == null, "newLocale == null");
         if (!newLocale.equals(CURRENT_LOCALE)) {
             LogCentral.setLocale(newLocale);
             CURRENT_LOCALE = newLocale;
         }
     }
 
     /**
      * Sets the locale on all <em>logdoc</em> <code>Log</code> classes to the default locale.
      */
     public static synchronized void useDefaultLocale() {
         setLocale(DEFAULT_LOCALE);
     }
 
     /**
      * Get the current locale.
      * 
      * @return the locale, e.g. <code>"fr_FR"</code>; never <code>null</code>.
      */
     public static synchronized String getLocale() {
         return CURRENT_LOCALE;
     }
 
     public static void resetLogBridge() {
         CURRENT_LOG_BRIDGE = DEFAULT_LOG_BRIDGE;
     }
 
     /**
      * Get the current logging bridge.
      * 
      * @return the logging bridge, never <code>null</code>.
      */
     public static LogBridge getLogBridge() {
         return CURRENT_LOG_BRIDGE;
     }
 
     /**
      * Sets the logging bridge to be used.
      * 
      * @param logBridge
      *            the {@link LogBridge} to use, cannot be <code>null</code>.
      */
     public static synchronized void setLogBridge(LogBridge logBridge) {
         Preconditions.checkArgument(logBridge == null, "logBridge == null");
         CURRENT_LOG_BRIDGE = logBridge;
     }
 
     /**
      * Enables or disables the display of the stack trace at the same level as the message.
      * 
      * @param sameLevel
      *            <code>true</code> if the stack trace should be at the same level, <code>false</code> if the stack trace should be at LOG4J_DEBUG_LEVEL level.
      */
     public static synchronized void setStackTraceAtMessageLevel(boolean sameLevel) {
         STACK_TRACE_AT_MESSAGE_LEVEL = sameLevel;
     }
 
     /**
      * Indicates whether the stack trace should be displayed at the same level as the message.
      * 
      * @return <code>true</code> if the stack trace should be at the same level, <code>false</code> if the stack trace should be at LOG4J_DEBUG_LEVEL level.
      */
     public static synchronized boolean isStackTraceAtMessageLevel() {
         return STACK_TRACE_AT_MESSAGE_LEVEL;
     }
 
     /**
      * Sets the current log filter.
      * 
      * @param logFilter
      *            the new {@link LogFilter}, cannot be <code>null</code>.
      */
     public static synchronized void setLogFilter(LogFilter logFilter) {
         Preconditions.checkArgument(logFilter == null, "logFilter == null");
         Limb.log(LogLevel.INFO, "Set LogFilter to instance of class " + logFilter.getClass().getName() + '.');
         LOG_FILTER = logFilter;
     }
 
     /**
      * Set the active <code>LogFilter</code> by class name. If the parameter is <code>null</code> then an exception is thrown. Otherwise if an instance cannot be constructed, then an instance of class
      * {@link NullLogFilter} is used instead.
      * 
      * @param className
      *            the name of the {@link LogFilter} class to use, cannot be <code>null</code>.
      */
     public static void setLogFilterByClassName(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
         Preconditions.checkArgument(className == null, "className == null");
         LogFilter logFilter = createLogFilterByClassName(className);
         setLogFilter(logFilter);
     }
 
     /**
      * Returns the active <code>LogFilter</code>.
      * 
      * @return the current {@link LogFilter}, never <code>null</code>.
      */
     public static synchronized LogFilter getLogFilter() {
         return LOG_FILTER;
     }
 
     /**
      * Retrieves a meta resource and returns it as a <code>URL</code>.
      * 
      * @param path
      *            the path to the meta resource, cannot be <code>null</code>.
      * @return the resource as a {@link URL}, never <code>null</code>.
      * @throws NoSuchResourceException
      *             if the resource could not be found.
      */
     static URL getMetaResource(String path) throws NoSuchResourceException {
         Preconditions.checkArgument(path == null, "path == null");
         String absPath = "/META-INF/" + path;
         URL url = Library.class.getResource(absPath);
         if (url == null) {
             Limb.log(LogLevel.ERROR, "Failed to load resource \"" + absPath + "\".");
             throw new NoSuchResourceException("Failed to load resource \"" + absPath + "\".");
         } else {
             Limb.log(LogLevel.DEBUG, "Loaded \"" + absPath + "\".");
             return url;
         }
     }
 
     /**
      * Retrieves a meta resource and returns it as an <code>InputStream</code>. Calling this function will not trigger initialization of the library.
      * 
      * @param path
      *            the path to the meta resource, cannot be <code>null</code>.
      * @return the resource as an {@link InputStream}.
      * @throws NoSuchResourceException
      *             if the resource could not be found.
      * @throws IOException
      *             if the stream could not be opened.
      */
     public static InputStream getMetaResourceAsStream(String path) throws NoSuchResourceException, IOException {
         return getMetaResource(path).openStream();
     }
 }
