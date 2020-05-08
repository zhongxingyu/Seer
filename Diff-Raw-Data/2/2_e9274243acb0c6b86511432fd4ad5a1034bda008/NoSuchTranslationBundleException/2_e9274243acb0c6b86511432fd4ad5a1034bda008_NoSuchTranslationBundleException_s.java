 /*
  * $Id$
  */
 package org.xins.logdoc;
 
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
  * Exception thrown if a specified translation bundle does not exist.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public final class NoSuchTranslationBundleException extends Exception {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructor a new <code>NoSuchTranslationBundleException</code>.
     *
     * @param locale
     *    the locale, which does not denote an existing translation bundle,
     *    cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>locale == null</code>.
     */
    public NoSuchTranslationBundleException(String locale)
    throws IllegalArgumentException {
 
       // Call superconstructor first
      super("Translation bundle \"" + name + "\" does not exist.");
 
       // Check preconditions
       MandatoryArgumentChecker.check("locale", locale);
 
       // XXX: Store the locale ?
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
