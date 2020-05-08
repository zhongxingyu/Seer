 // See the COPYRIGHT file for copyright and license information
 package org.znerd.logdoc.internal.log4j;
 
 import org.apache.log4j.Level;
 import org.znerd.logdoc.LogCentral;
 import org.znerd.logdoc.UnsupportedLocaleException;
 
 /**
  * Abstract base class for <em>logdoc</em> <code>Log</code> classes.
  *
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public abstract class AbstractLog {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
    
    /**
     * The <em>debug</em> log level.
     */
    public static final Level DEBUG;
 
    /**
     * The <em>info</em> log level.
     */
    public static final Level INFO;
 
    /**
     * The <em>notice</em> log level.
     */
    public static final Level NOTICE;
 
    /**
     * The <em>warning</em> log level.
     */
    public static final Level WARNING;
 
    /**
     * The <em>error</em> log level.
     */
    public static final Level ERROR;
 
    /**
     * The <em>fatal</em> log level.
     */
    public static final Level FATAL;
    
    
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Initializes this class.
     */
    static {
 
       // Determine the int value for the NOTICE level
       int noticeInt = (Level.INFO_INT + Level.WARN_INT) / 2;
 
       // Initialize all the log levels
       DEBUG   = Level.DEBUG;
       INFO    = Level.INFO;
       NOTICE  = new CustomLevel(noticeInt, "NOTICE", 5);
       WARNING = Level.WARN;
       ERROR   = Level.ERROR;
       FATAL   = Level.FATAL;
    }
    
    
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
    
    /**
     * Constructs a new <code>AbstractLog</code> instance.
     */
    protected AbstractLog() {
       // empty
    }
 
    
    //-------------------------------------------------------------------------
    // Inner classes   
    //-------------------------------------------------------------------------
    
    /**
     * Log controller. Can be used by the <code>LogCentral</code> class to set
     * the locale on a specific <code>Log</code> class. Each <code>Log</code>
     * class should create exactly one <code>LogController</code> object, in a
     * class initializer.
     *
     * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
     */
   public static abstract class LogController {
       
       //----------------------------------------------------------------------
       // Constructors
       //-------------------------------------------------------------------------
 
       /**
        * Constructs a new <code>LogController</code> object.
        *
        * @throws UnsupportedLocaleException
        *    if this <code>LogController</code> does not support the current Locale.
        */
       protected LogController() throws UnsupportedLocaleException {
 
          // Register this Log with the LogCentral, so that
          // LogCentral.setLocale(String) may call setLocale(String) on this
          // instance
          LogCentral.registerLog(this);
       }
       
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
       
       /**
        * Checks if the specified locale is supported.
        *
        * @param locale
        *    the locale, not <code>null</code>.
        *
        * @return
        *    <code>true</code> if the locale is supported, <code>false</code>
        *    if it is not.
        */
      public abstract boolean isLocaleSupported(String locale);
 
       /**
        * Activates the specified locale.
        *
        * <p>This method should only be called with locales that are supported,
        * according to {@link #isLocaleSupported(String)}. Otherwise the
        * behaviour of this method is unspecified.
        *
        * @param newLocale
        *    the new locale, not <code>null</code>.
        */
      public abstract void setLocale(String newLocale);
    }
 
    /**
     * Custom log level.
     *
     * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
     */
    private static final class CustomLevel extends Level {
       
       //----------------------------------------------------------------------
       // Class fields
       //----------------------------------------------------------------------
       
       /**
        * Unique ID used to determine serialization compatibility.
        */
       private static final long serialVersionUID = 1909887126346631322L;
 
       
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
       
       /**
        * Constructs a new <code>CustomLevel</code> object.
        *
        * @param value
        *    the <code>int</code> value for this level.
        *
        * @param name
        *    the name for this level, should not be <code>null</code>.
        *
        * @param syslogEquivalent
        *    the syslog equivalent.
        */
       private CustomLevel(int value, String name, int syslogEquivalent) {
          super(value, name, syslogEquivalent);
       }
    }
 }
