 /*
  * $Id$
  */
 package org.xins.util.text;
 
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
  * Fast, unsynchronized string buffer implementation.
  *
  * @version $Revision$ $Date$
 * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public class FastStringBuffer extends Object {
 
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
     * Constructs a new <code>FastStringBuffer</code> object with the specified
     * initial capacity.
     *
     * @param capacity
     *    the initial capacity, must be &gt;= 0.
     *
     * @throws IllegalArgumentException
     *    if <code>capacity &lt; 0</code>.
     */
    public FastStringBuffer(int capacity)
    throws IllegalArgumentException {
       if (capacity < 0) {
          throw new IllegalArgumentException("capacity (" + capacity + ") < 0");
       }
 
       _buffer   = new char[capacity];
       _length   = 0;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The underlying character buffer. The size of this buffer is the capacity
     * of this string buffer object.
     */
    private char[] _buffer;
 
    /**
     * The actual length of the contained content. Is always less than or equal
     * to the capacity.
     */
    private int _length;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    private void ensureCapacity(int needed) {
       int current = _buffer.length;
       if (current < needed) {
          int newCapacity = needed + 16; // XXX: Is this okay?
          char[] newBuffer = new char[newCapacity];
          System.arraycopy(_buffer, 0, newBuffer, 0, current);
          _buffer = newBuffer;
       }
    }
 
    public void append(char c) {
       ensureCapacity(_length + 1);
       _buffer[_length++] = c;
    }
 
    public void append(char[] cbuf)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("cbuf", cbuf);
 
       ensureCapacity(_length + cbuf.length);
       System.arraycopy(cbuf, 0, _buffer, _length, cbuf.length);
    }
 
    public void append(char[] cbuf, int off, int len)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("cbuf", cbuf);
       if (off < 0) {
          throw new IllegalArgumentException("off (" + off + ") < 0");
       } else if (off >= cbuf.length) {
          throw new IllegalArgumentException("off (" + off + ") >= cbuf.length (" + cbuf.length + ')');
       } else if (len < 0) {
          throw new IllegalArgumentException("len (" + len + ") < 0");
       } else if (off + len > cbuf.length) {
          throw new IllegalArgumentException("off (" + off + ") + len (" + len + ") > cbuf.length (" + cbuf.length + ')');
       }
 
       ensureCapacity(_length + len);
       System.arraycopy(cbuf, off, _buffer, _length, len);
    }
 
    public void append(String str)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("str", str);
 
       int strLength = str.length();
       int newLength = _length + strLength;
       ensureCapacity(newLength);
 
       // Copy the string chars into the buffer
       str.getChars(0, strLength, _buffer, _length);
       _length = newLength;
    }
 
    public void clear() {
       _length = 0;
    }
 
    public String toString() {
       return new String(_buffer, 0, _length);
    }
 }
