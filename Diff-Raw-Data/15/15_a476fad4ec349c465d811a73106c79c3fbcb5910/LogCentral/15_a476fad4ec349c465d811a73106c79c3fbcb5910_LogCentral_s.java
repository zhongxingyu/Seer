 // See the COPYRIGHT file for copyright and license information
 package org.znerd.logdoc;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.NDC;
 import org.znerd.logdoc.internal.log4j.AbstractLog;
 
 /**
  * Central class for <em>logdoc</em> logging.
  *
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public final class LogCentral {
    
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
    
    /**
     * The name of the property that specifies which locale should be used.
     */
    public static final String LOG_LOCALE_PROPERTY = "org.znerd.logdoc.locale";
 
    /**
     * The name of the property that specifies if all stack traces should be
     * displayed at the message level. By default, stack traces are displayed
     * at the <em>DEBUG</em> level.
     */
    public static final String LOG_STACK_TRACE_AT_MESSAGE_LEVEL = "org.znerd.logdoc.stackTraceAtMessageLevel";
 
    /**
     * The name of the property that specifies the name of the
     * <code>LogFilter</code> class to use.
     */
    public static final String LOG_FILTER_PROPERTY = "org.znerd.logdoc.filterClass";
 
    /**
     * The default locale used at start-up, if no locale is specified in a
     * system property.
     */
    public static final String DEFAULT_LOCALE = "en_US";
 
    /**
     * All registered <code>LogController</code> instances.
     *
     * @see #registerLog(AbstractLog.LogController)
     */
    private static AbstractLog.LogController[] CONTROLLERS;
 
    /**
     * The locale for the logdoc.
     */
    private static String LOCALE = null;
 
    /**
     * Flag indicating whether the stack trace should be displayed at the same
     * level of the message or not. Default is <code>false</code>.
     */
    private static boolean STACK_TRACE_AT_MESSAGE_LEVEL = false;
 
    /**
     * The active <code>LogFilter</code> instance.
     */
    private static LogFilter LOG_FILTER;
    
    
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
    
    /**
     * Registers the specified <code>LogController</code>, which represents a
     * <em>logdoc</em> <code>Log</code> class.
     *
     * @param controller
     *    the {@link AbstractLog.LogController}, cannot be <code>null</code>.
     *
     * @throws UnsupportedLocaleException
     *    if {@link AbstractLog.LogController} does not support the current Locale.
     */
   static void registerLog(AbstractLog.LogController controller)
    throws UnsupportedLocaleException {
 
       // When the first LogController registers, do one-time initialization
       if (LOCALE == null) {
          LOCALE = determineStartupLocale();
          initLogFilter();
       }
 
       // Set the locale on the controller
       if (controller.isLocaleSupported(LOCALE)) {
          controller.setLocale(LOCALE);
 
       // Fail if the controller does not support this locale
       } else {
          System.err.println("Locale \"" + LOCALE + "\" is not supported by log controller: " + controller);
          throw new UnsupportedLocaleException(LOCALE);
       }
 
       // Add it to the list of registered controllers
       if (CONTROLLERS == null) {
          CONTROLLERS = new AbstractLog.LogController[] { controller };
       } else {
          int size = CONTROLLERS.length;
          AbstractLog.LogController[] temp = new AbstractLog.LogController[size + 1];
          System.arraycopy(CONTROLLERS, 0, temp, 0, size);
          temp[size] = controller;
          CONTROLLERS = temp;
       }
    }
 
    /**
     * Determines the start-up locale. If the system property
     * {@link #LOG_LOCALE_PROPERTY} is set to a non-empty value, then this
     * will be returned, otherwise {@link #DEFAULT_LOCALE} is returned.
     *
     * <p>This method is called from
     * {@link #registerLog(AbstractLog.LogController)} as soon as the first
     * {@link AbstractLog.LogController} is registered.
     *
     * @return
     *    the locale to use initially, at start-up.
     */
    private static String determineStartupLocale() {
 
       // Use the value of the system property, if set...
       String locale = System.getProperty(LOG_LOCALE_PROPERTY);
       if (locale != null && locale.trim().length() > 0) {
          return locale;
 
       // ...or otherwise fallback to the default locale
       } else {
          return DEFAULT_LOCALE;
       }
    }
 
    /**
     * Determines which <code>LogFilter</code> class to use, constructs an
     * instance and stores it inside this class
     *
     * <p>If no filter is configured in the system properties (see
     * {@link #LOG_FILTER_PROPERTY}), then a {@link SimpleLogFilter} is
     * used.
     *
     * <p>If a filter is configured in the system properties, but could not be
     * constructed, then a {@link NullLogFilter} is used, to avoid any
     * security issues.
     */
    private static void initLogFilter() {
 
       // Get the system property
       String s = System.getProperty(LOG_FILTER_PROPERTY);
 
       // Property is not set, use a SimpleLogFilter
       if (s == null || s.trim().length() < 1) {
          setLogFilter(new SimpleLogFilter());
 
       // Property is set, use it
       } else {
          setLogFilterByClass(s);
       }
    }
 
    /**
     * Returns the current diagnostic context identifier.
     *
     * @return
     *    the current diagnostic context identifier, or <code>null</code> if
     *    there is none.
     */
    public static String getContext() {
       return NDC.peek();
    }
 
    /**
     * Sets the locale on all <em>logdoc</em> <code>Log</code> classes.
     *
     * @param newLocale
     *    the new locale, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>newLocale == null</code>.
     *
     * @throws UnsupportedLocaleException
     *    if the specified locale is not supported by all registered
     *    <em>logdoc</em> <code>Log</code> classes.
     */
    public static void setLocale(String newLocale)
    throws IllegalArgumentException, UnsupportedLocaleException {
 
       // Check preconditions
       if (newLocale == null) {
          throw new IllegalArgumentException("newLocale == null");
       }
 
       // Short-circuit if the new locale equals the current one
       if (newLocale.equals(LOCALE)) {
          return;
       }
 
       // Make sure the locale is supported by all controllers
       int size = (CONTROLLERS == null) ? 0 : CONTROLLERS.length;
       for (int i = 0; i < size; i++) {
          if (!CONTROLLERS[i].isLocaleSupported(newLocale)) {
             throw new UnsupportedLocaleException(newLocale);
          }
       }
 
       // Change the locale on all controllers
       // XXX This should be removed and the controller should invoke LogCentral.getLocale()
       for (int i = 0; i < size; i++) {
          CONTROLLERS[i].setLocale(newLocale);
       }
 
       LOCALE = newLocale;
    }
 
    /**
     * Sets the locale on all <em>logdoc</em> <code>Log</code> classes to the
     * default locale.
     */
    public static void useDefaultLocale() {
       setLocale(DEFAULT_LOCALE);
    }
 
    /**
     * Get the locale set in this LogCentral.
     *
     * @return
     *    the locale, e.g. <code>"en_US"</code>.
     */
    public static String getLocale() {
       return LOCALE;
    }
 
    /**
     * Enables or disables the display of the stack trace at the same level as
     * the message.
     *
     * @param sameLevel
     *    <code>true</code> if the stack trace should be at the same level,
     *    <code>false</code> if the stack trace should be at DEBUG level.
     */
    public static void setStackTraceAtMessageLevel(boolean sameLevel) {
        STACK_TRACE_AT_MESSAGE_LEVEL = sameLevel;
    }
 
    /**
     * Indicates whether the stack trace should be displayed at the same level
     * as the message.
     *
     * @return
     *    <code>true</code> if the stack trace should be at the same level,
     *    <code>false</code> if the stack trace should be at DEBUG level.
     */
    public static boolean isStackTraceAtMessageLevel() {
        return STACK_TRACE_AT_MESSAGE_LEVEL;
    }
 
    /**
     * Set the active <code>LogFilter</code>.
     *
     * @param logFilter
     *    the new {@link LogFilter} to use,
     *    cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>logFilter == null</code>.
     */
    public static synchronized void setLogFilter(LogFilter logFilter)
    throws IllegalArgumentException {
 
       // Check preconditions
       if (logFilter == null) {
          throw new IllegalArgumentException("logFilter == null");
       }
 
       Logger.getLogger("org.znerd.logdoc.LogCentral.0000").debug("Set LogFilter to instance of class " + logFilter.getClass().getName() + '.');
 
       // Store the filter in this class
       LOG_FILTER = logFilter;
    }
 
    /**
     * Set the active <code>LogFilter</code> by class name. If the parameter is
     * <code>null</code> then an exception is thrown. Otherwise if an instance
     * cannot be constructed, then an instance of class {@link NullLogFilter}
     * is used instead.
     *
     * @param className
     *    the name of the {@link LogFilter} class to use,
     *    cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>className == null</code>.
     */
    public static void setLogFilterByClass(String className)
    throws IllegalArgumentException {
 
       // Check preconditions
       if (className == null) {
          throw new IllegalArgumentException("className == null");
       }
 
       // Construct an instance
       LogFilter logFilter;
       try {
          logFilter = (LogFilter) Class.forName(className).newInstance();
 
       // Instance construction failed, eclipse potential issues using a
       // NullLogFilter
       } catch (Throwable cause) {
          Logger.getLogger("org.znerd.logdoc.LogCentral.0001").error("Failed to construct LogFilter of class: " + className + ". Using NullLogFilter.", cause);
          logFilter = new NullLogFilter();
       }
 
       // Delegate to synchronized setter
       setLogFilter(logFilter);
    }
 
    /**
     * Returns the active <code>LogFilter</code>.
     *
     * @return
     *    the current {@link LogFilter}, never <code>null</code>.
     */
    public static synchronized LogFilter getLogFilter() {
       return LOG_FILTER;
    }
 
    
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
    
    /**
     * Constructs a new <code>LogCentral</code> instance. This constructor is
     * intentionally made <code>private</code>, since no instances should be
     * constructed of this class.
     */
    private LogCentral() {
       // empty
    }
 }
