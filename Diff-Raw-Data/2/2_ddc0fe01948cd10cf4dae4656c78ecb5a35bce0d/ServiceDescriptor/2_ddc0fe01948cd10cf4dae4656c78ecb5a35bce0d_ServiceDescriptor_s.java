 /*
  * $Id$
  */
 package org.xins.util.service;
 
 import java.net.MalformedURLException;
 import java.util.NoSuchElementException;
 import org.apache.oro.text.regex.MalformedPatternException;
 import org.apache.oro.text.regex.Pattern;
 import org.apache.oro.text.regex.Perl5Compiler;
 import org.apache.oro.text.regex.Perl5Matcher;
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
  * Descriptor for a single target service.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.105
  */
 public final class ServiceDescriptor extends Descriptor {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Perl 5 pattern compiler.
     */
    private static final Perl5Compiler PATTERN_COMPILER = new Perl5Compiler();
 
    /**
     * Pattern matcher.
     */
    private static final Perl5Matcher PATTERN_MATCHER = new Perl5Matcher();
 
    /**
     * The pattern for a URL, as a character string.
     */
   private static final String PATTERN_STRING = "^[a-z][a-z0-9]*:\\/\\/[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)*(:[1-9][0-9]*)?(\\/([a-zA-Z0-9]*))*$"
 
    /**
     * The pattern for a URL.
     */
    private static final Pattern PATTERN;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    static {
       try {
          PATTERN = PATTERN_COMPILER.compile(PATTERN_STRING, Perl5Compiler.READ_ONLY_MASK);
       } catch (MalformedPatternException mpe) {
          throw new Error("Unable to compile pattern: " + PATTERN_STRING);
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>ServiceDescriptor</code>.
     *
     * @param url
     *    the URL of the service, cannot be <code>null</code>.
     *
     * @param timeOut
     *    the time-out for the service, in milliseconds; if a negative value is
     *    passed then the service should be waited for forever.
     *
     * @throws IllegalArgumentException
     *    if <code>url == null</code>.
     *
     * @throws MalformedURLException
     *    if the specified URL is malformed.
     */
    public ServiceDescriptor(String url, int timeOut)
    throws IllegalArgumentException, MalformedURLException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("url", url);
       if (! PATTERN_MATCHER.matches(url, PATTERN)) {
          throw new MalformedURLException(url);
       }
 
       // Set fields
       _url     = url;
       _timeOut = timeOut;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The URL for the service. Cannot be <code>null</code>.
     */
    private final String _url;
 
    /**
     * The time-out for the service. Is set to a negative value if the service
     * should be waited for forever.
     */
    private final int _timeOut;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Checks if this service descriptor denotes a group.
     *
     * @return
     *    <code>false</code> since this descriptor does not denote a group.
     */
    public boolean isGroup() {
       return false;
    }
 
    /**
     * Returns the URL for the service.
     *
     * @return
     *    the URL for the service, not <code>null</code>.
     */
    public String getURL() {
       return _url;
    }
 
    /**
     * Returns the time-out for the service. A negative value is returned if the service
     * should be waited for forever.
     *
     * @return
     *    the time-out for the service, or a negative value if the service
     *    should be waited for forever.
     */
    public int getTimeOut() {
       return _timeOut;
    }
 
    public java.util.Iterator iterateServices() {
       return new Iterator();
    }
 
    public String toString() {
       return "ServiceDescriptor(url=\"" + _url + "\"; timeOut=" + _timeOut + ')';
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Iterator over this (single) service descriptor. Needed for the
     * implementation of {@link #iterateServices()}.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
     *
     * @since XINS 0.105
     */
    private final class Iterator
    extends Object
    implements java.util.Iterator {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>Iterator</code>.
        */
       private Iterator() {
          // empty
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * Flag that indicates if this iterator is already done iterating over
        * the single element.
        */
       private boolean _done;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       public boolean hasNext() {
          return ! _done;
       }
 
       public Object next() {
          if (_done) {
             throw new NoSuchElementException();
          } else {
             _done = true;
             return ServiceDescriptor.this;
          }
       }
 
       public void remove() throws UnsupportedOperationException {
          throw new UnsupportedOperationException();
       }
    }
 }
