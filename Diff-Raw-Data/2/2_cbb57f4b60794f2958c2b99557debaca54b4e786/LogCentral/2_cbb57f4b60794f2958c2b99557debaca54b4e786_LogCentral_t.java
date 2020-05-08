 /*
  * $Id$
  */
 package org.xins.logdoc;
 
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
  * Central class for <em>logdoc</em> logging.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public final class LogCentral
 extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * All registered <code>LogController</code> instances.
     *
     * @see #registerLog(LogController)
     */
    private static AbstractLog.LogController[] CONTROLLERS;
 
 
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
     * @throws IllegalArgumentException
     *    if <code>controller == null</code>.
     */
    static final void registerLog(AbstractLog.LogController controller)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("controller", controller);
 
       // Add the controller to the List
       if (CONTROLLERS == null) {
          CONTROLLERS = new AbstractLog.LogController[] { controller };
       } else {
          int size = CONTROLLERS.length;
          AbstractLog.LogController[] a = new AbstractLog.LogController[size + 1];
          System.arraycopy(CONTROLLERS, 0, a, 0, size);
          a[size] = controller;
          CONTROLLERS = a;
       }
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
    public static final void setLocale(String newLocale)
    throws IllegalArgumentException, UnsupportedLocaleException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("newLocale", newLocale);
 
       // Make sure the locale is supported by all controllers
       int size = CONTROLLERS.length;
       for (int i = 0; i < size; i++) {
          if (CONTROLLERS[i].isLocaleSupported(newLocale) == false) {
             throw new UnsupportedLocaleException(newLocale);
          }
       }
 
       // Change the locale on all controllers
       for (int i = 0; i < size; i++) {
          CONTROLLERS[i].setLocale(newLocale);
       }
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
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
