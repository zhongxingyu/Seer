 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.service;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.util.NoSuchElementException;
 import java.util.zip.CRC32;
 
 import org.apache.oro.text.regex.MalformedPatternException;
 import org.apache.oro.text.regex.Pattern;
 import org.apache.oro.text.regex.Perl5Compiler;
 import org.apache.oro.text.regex.Perl5Matcher;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 
 import org.xins.common.text.FastStringBuffer;
 
 /**
  * Descriptor for a single target service. A target descriptor defines a URL
  * that identifies the location of the service. Also, it may define 3 kinds of
  * time-outs:
  *
  * <dl>
  *    <dt><em>total time-out</em> ({@link #getTotalTimeOut()})</dt>
  *    <dd>the maximum duration of a call, including connection time, time used
  *    to send the request, time used to receive the response, etc.</dd>
  *
  *    <dt><em>connection time-out</em> ({@link #getConnectionTimeOut()})</dt>
  *    <dd>the maximum time for attempting to establish a connection.</dd>
  *
  *    <dt><em>socket time-out</em> ({@link #getSocketTimeOut()})</dt>
  *    <dd>the maximum time for attempting to receive data on a socket.</dd>
  * </dl>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class TargetDescriptor extends Descriptor {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The fully-qualified name of this class.
     */
    private static final String CLASSNAME = TargetDescriptor.class.getName();
 
    /**
     * The fully-qualified name of the inner class <code>Iterator</code>.
     */
    private static final String ITERATOR_CLASSNAME = TargetDescriptor.Iterator.class.getName();
 
    /**
     * The default time-out when no time-out is specified.
     */
    private static final int DEFAULT_TIMEOUT = 5000;
 
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
   private static final String PATTERN_STRING = "[a-z][a-z0-9]*:\\/\\/[a-zA-Z0-9\\-]+(\\.[a-zA-Z0-9\\-]+)*(:[1-9][0-9]*)?(\\/([a-zA-Z0-9\\-_~\\.]*))*";
 
    /**
     * The pattern for a URL.
     */
    private static final Pattern PATTERN;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Initializes this class. This function compiles {@link #PATTERN_STRING}
     * to a {@link Pattern} and then stores that in {@link #PATTERN}.
     */
    static {
       try {
          PATTERN = PATTERN_COMPILER.compile(PATTERN_STRING, Perl5Compiler.READ_ONLY_MASK);
       } catch (MalformedPatternException mpe) {
          String message = "The pattern \"" + PATTERN_STRING + "\" is malformed.";
          Log.log_1050(CLASSNAME, "<clinit>()", message);
          throw new Error(message);
       }
    }
 
    /**
     * Computes the CRC-32 checksum for the specified character string.
     *
     * @param s
     *    the string for which to compute the checksum, not <code>null</code>.
     *
     * @return
     *    the checksum for <code>s</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>s == null</code>.
     */
    private static int computeCRC32(String s)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("s", s);
 
       // Compute the CRC-32 checksum
       CRC32 checksum = new CRC32();
       byte[] bytes;
       final String ENCODING = "US-ASCII";
       try {
          bytes = s.getBytes(ENCODING);
       } catch (UnsupportedEncodingException exception) {
          String message = "Encoding \"" + ENCODING + "\" is not supported.";
          Log.log_1050(CLASSNAME, "computeCRC32(String)", message);
          throw new Error(message);
       }
       checksum.update(bytes, 0, bytes.length);
       return (int) (checksum.getValue() & 0x00000000ffffffffL);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>TargetDescriptor</code> for the specified URL.
     *
     * <p>Note: Both the connection time-out and the socket time-out will be
     * set to the default time-out: 5 seconds.
     *
     * @param url
     *    the URL of the service, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>url == null</code>.
     *
     * @throws MalformedURLException
     *    if the specified URL is malformed.
     */
    public TargetDescriptor(String url)
    throws IllegalArgumentException, MalformedURLException {
       this(url, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT);
    }
 
    /**
     * Constructs a new <code>TargetDescriptor</code> for the specified URL,
     * with the specifed total time-out.
     *
     * <p>Note: Both the connection time-out and the socket time-out will be
     * set to equal the total time-out.
     *
     * @param url
     *    the URL of the service, cannot be <code>null</code>.
     *
     * @param timeOut
     *    the total time-out for the service, in milliseconds; or a
     *    non-positive value for no total time-out.
     *
     * @throws IllegalArgumentException
     *    if <code>url == null</code>.
     *
     * @throws MalformedURLException
     *    if the specified URL is malformed.
     */
    public TargetDescriptor(String url, int timeOut)
    throws IllegalArgumentException, MalformedURLException {
       this(url, timeOut, timeOut, timeOut);
    }
 
    /**
     * Constructs a new <code>TargetDescriptor</code> for the specified URL,
     * with the specifed total time-out and connection time-out.
     *
     * <p>Note: If the passed connection time-out is smaller than 1 ms, or
     * greater than the total time-out, then it will be adjusted to equal the
     * total time-out.
     *
     * <p>Note: The socket time-out will be set to equal the total time-out.
     *
     * @param url
     *    the URL of the service, cannot be <code>null</code>.
     *
     * @param timeOut
     *    the total time-out for the service, in milliseconds; or a
     *    non-positive value for no total time-out.
     *
     * @param connectionTimeOut
     *    the connection time-out for the service, in milliseconds; or a
     *    non-positive value if the connection time-out should equal the total
     *    time-out.
     *
     * @throws IllegalArgumentException
     *    if <code>url == null</code>.
     *
     * @throws MalformedURLException
     *    if the specified URL is malformed.
     */
    public TargetDescriptor(String url, int timeOut, int connectionTimeOut)
    throws IllegalArgumentException, MalformedURLException {
       this(url, timeOut, connectionTimeOut, timeOut);
    }
 
    /**
     * Constructs a new <code>TargetDescriptor</code> for the specified URL,
     * with the specifed total time-out, connection time-out and socket
     * time-out.
     *
     * <p>Note: If the passed connection time-out is smaller than 1 ms, or
     * greater than the total time-out, then it will be adjusted to equal the
     * total time-out.
     *
     * <p>Note: If the passed socket time-out is smaller than 1 ms or greater
     * than the total time-out, then it will be adjusted to equal the total
     * time-out.
     *
     * @param url
     *    the URL of the service, cannot be <code>null</code>.
     *
     * @param timeOut
     *    the total time-out for the service, in milliseconds; or a
     *    non-positive value for no total time-out.
     *
     * @param connectionTimeOut
     *    the connection time-out for the service, in milliseconds; or a
     *    non-positive value if the connection time-out should equal the total
     *    time-out.
     *
     * @param socketTimeOut
     *    the socket time-out for the service, in milliseconds; or a
     *    non-positive value for no socket time-out.
     *
     * @throws IllegalArgumentException
     *    if <code>url == null</code>.
     *
     * @throws MalformedURLException
     *    if the specified URL is malformed.
     */
    public TargetDescriptor(String url,
                            int    timeOut,
                            int    connectionTimeOut,
                            int    socketTimeOut)
    throws IllegalArgumentException, MalformedURLException {
 
       // TRACE: Enter constructor
       Log.log_1000(CLASSNAME, null);
 
       // Check preconditions
       MandatoryArgumentChecker.check("url", url);
       if (! PATTERN_MATCHER.matches(url, PATTERN)) {
          throw new MalformedURLException(url);
       }
 
       // Convert negative total time-out to 0
       timeOut = (timeOut > 0) ? timeOut : 0;
 
       // If connection time-out or socket time-out is not set, then set it to
       // the total time-out
       connectionTimeOut = (connectionTimeOut > 0) ? connectionTimeOut : timeOut;
       socketTimeOut     = (socketTimeOut     > 0) ? socketTimeOut     : timeOut;
 
       // If either connection or socket time-out is greater than total
       // time-out, then limit it to the total time-out
       connectionTimeOut = (connectionTimeOut < timeOut) ? connectionTimeOut : timeOut;
       socketTimeOut     = (socketTimeOut     < timeOut) ? socketTimeOut     : timeOut;
 
       // Set fields
       _url               = url;
       _timeOut           = timeOut;
       _connectionTimeOut = connectionTimeOut;
       _socketTimeOut     = socketTimeOut;
       _crc               = computeCRC32(url);
 
       // Convert to a string
       // TODO: Include CRC in _asString
       FastStringBuffer buffer = new FastStringBuffer(290, "TargetDescriptor(url=\"");
       buffer.append(url);
       buffer.append("\"; total time-out is ");
       if (_timeOut < 1) {
          buffer.append("disabled; connection time-out is ");
       } else {
          buffer.append(_timeOut);
          buffer.append(" ms; connection time-out is ");
       }
       if (_connectionTimeOut < 1) {
          buffer.append("disabled; socket time-out is ");
       } else {
          buffer.append(_connectionTimeOut);
          buffer.append(" ms; socket time-out is ");
       }
       if (_socketTimeOut < 1) {
          buffer.append("disabled)");
       } else {
          buffer.append(_socketTimeOut);
          buffer.append(" ms)");
       }
       _asString = buffer.toString();
 
       // TRACE: Leave constructor
       Log.log_1002(CLASSNAME, _asString);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * A textual representation of this object. Cannot be <code>null</code>.
     * The value of this field is returned by {@link #toString()}.
     */
    private final String _asString;
 
    /**
     * The URL for the service. Cannot be <code>null</code>.
     */
    private final String _url;
 
    /**
     * The total time-out for the service. Is set to a 0 if no total time-out
     * should be applied.
     */
    private final int _timeOut;
 
    /**
     * The connection time-out for the service. Always greater than 0 and
     * smaller than or equal to the total time-out.
     */
    private final int _connectionTimeOut;
 
    /**
     * The socket time-out for the service. Always greater than 0 and smaller
     * than or equal to the total time-out.
     */
    private final int _socketTimeOut;
 
    /**
     * The CRC-32 checksum for the URL. See {@link #_url}.
     */
    private final int _crc;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Checks if this descriptor denotes a group of descriptors.
     *
     * @return
     *    <code>false</code>, since this descriptor does not denote a group.
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
     * Returns the total time-out for a call to the service. The value 0
     * is returned if there is no total time-out.
     *
     * @return
     *    the total time-out for the service, as a positive number, in
     *    milli-seconds, or 0 if there is no total time-out.
     */
    public int getTotalTimeOut() {
       return _timeOut;
    }
 
    /**
     * Returns the connection time-out for a call to the service.
     *
     * @return
     *    the connection time-out for the service; always greater than 0 and
     *    smaller than or equal to the total time-out.
     */
    public int getConnectionTimeOut() {
       return _connectionTimeOut;
    }
 
    /**
     * Returns the socket time-out for a call to the service.
     *
     * @return
     *    the socket time-out for the service; always greater than 0 and
     *    smaller than or equal to the total time-out.
     */
    public int getSocketTimeOut() {
       return _socketTimeOut;
    }
 
    /**
     * Returns the CRC-32 checksum for the URL of this function caller.
     *
     * @return
     *    the CRC-32 checksum.
     */
    public int getCRC() {
       return _crc;
    }
 
    /**
     * Iterates over all leaves, the target descriptors.
     *
     * <p>The returned {@link java.util.Iterator} will only return this target
     * descriptor.
     *
     * @return
     *    iterator that returns this target descriptor, never
     *    <code>null</code>.
     */
    public java.util.Iterator iterateTargets() {
       return new Iterator();
    }
 
    /**
     * Counts the total number of target descriptors in/under this descriptor.
     *
     * @return
     *    the total number of target descriptors, always 1.
     */
    public int getTargetCount() {
       return 1;
    }
 
    /**
     * Returns the <code>TargetDescriptor</code> that matches the specified
     * CRC-32 checksum.
     *
     * @param crc
     *    the CRC-32 checksum.
     *
     * @return
     *    the {@link TargetDescriptor} that matches the specified checksum, or
     *    <code>null</code>, if none could be found in this descriptor.
     */
    public TargetDescriptor getTargetByCRC(int crc) {
       return (_crc == crc) ? this : null;
    }
 
    /**
     * Textual description of this object. The string includes the URL and all
     * time-out values. For example:
     *
     * <blockquote><code>TargetDescriptor(url="http://api.google.com/some_api/"; total-time-out is 5300 ms; connection time-out is 1000 ms; socket time-out is disabled)</code></blockquote>
     *
     * @return
     *    this <code>TargetDescriptor</code> as a {@link String}, never
     *    <code>null</code>.
     */
    public String toString() {
       return _asString;
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Iterator over this (single) target descriptor. Needed for the
     * implementation of {@link #iterateTargets()}.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     *
     * @since XINS 1.0.0
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
          // TRACE: Enter constructor
          Log.log_1000(ITERATOR_CLASSNAME, null);
 
          // empty
 
          // TRACE: Leave constructor
          Log.log_1002(ITERATOR_CLASSNAME, null);
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
 
       /**
        * Checks if there is a next element.
        *
        * @return
        *    <code>true</code> if there is a next element, <code>false</code>
        *    if there is not.
        */
       public boolean hasNext() {
          return ! _done;
       }
 
       /**
        * Returns the next element.
        *
        * @return
        *    the next element, never <code>null</code>.
        *
        * @throws NoSuchElementException
        *    if there is no new element.
        */
       public Object next() throws NoSuchElementException {
          if (_done) {
             throw new NoSuchElementException();
          } else {
             _done = true;
             return TargetDescriptor.this;
          }
       }
 
       /**
        * Removes the element last returned by <code>next()</code> (unsupported
        * operation).
        *
        * @throws UnsupportedOperationException
        *    always thrown, since this operation is unsupported.
        */
       public void remove() throws UnsupportedOperationException {
          throw new UnsupportedOperationException();
       }
    }
 }
