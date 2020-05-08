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
 
       _buffer = new char[capacity];
       _length = 0;
    }
 
    /**
     * Constructs a new <code>FastStringBuffer</code> object with the specified
     * initial content. The capacity will be equal to the length of the
     * specified string.
     *
     * @param s
     *    the initial content, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>s == null</code>.
     */
    public FastStringBuffer(String s) {
       if (s == null) {
          throw new IllegalArgumentException("s == null");
       }
 
       _buffer = s.toCharArray();
       _length = _buffer.length;
    }
 
    /**
     * Constructs a new <code>FastStringBuffer</code> object with the specified
     * initial capacity and content.
     *
     * @param capacity
     *    the initial capacity, must be
     *    &gt;= <code>s.</code>{@link String#length()}.
     *
     * @param s
     *    the initial content, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>capacity &lt; <code>s.</code>{@link String#length()} || s == null</code>.
     */
    public FastStringBuffer(int capacity, String s) {
       if (s == null) {
          throw new IllegalArgumentException("s == null");
       } else if (capacity < s.length()) {
          throw new IllegalArgumentException("capacity (" + capacity + ") < s.length() (" + s.length() + ')');
       }
 
       _buffer = new char[capacity];
      _length = s.length;
       s.getChars(0, _length, _buffer, 0);
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
 
    /**
     * Ensures that the specified needed capacity is actually available. If it
     * is not, then the internal buffer will be expanded. The new capacity will
     * be larger than or equal to the needed capacity.
     *
     * @param needed
     *    the needed capacity.
     */
    private void ensureCapacity(int needed) {
       int current = _buffer.length;
       if (current < needed) {
          int newCapacity = needed + 16; // XXX: Is this okay?
          char[] newBuffer = new char[newCapacity];
          System.arraycopy(_buffer, 0, newBuffer, 0, current);
          _buffer = newBuffer;
       }
    }
 
    /**
     * Appends the specified character. If necessary, the capacity of this
     * string buffer will be increased.
     *
     * @param c
     *    the character to append.
     */
    public void append(char c) {
       ensureCapacity(_length + 1);
       _buffer[_length++] = c;
    }
 
    /**
     * Appends all characters in the specified character buffer. If necessary,
     * the capacity of this string buffer will be increased.
     *
     * @param cbuf
     *    the character array to copy characters from, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>cbuf == null</code>.
     */
    public void append(char[] cbuf)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("cbuf", cbuf);
 
       ensureCapacity(_length + cbuf.length);
       System.arraycopy(cbuf, 0, _buffer, _length, cbuf.length);
    }
 
    /**
     * Appends characters from the specified character buffer. If necessary,
     * the capacity of this string buffer will be increased.
     *
     * @param cbuf
     *    the character array to copy characters from, not <code>null</code>.
     *
     * @param off
     *    the offset in <code>cbuf</code>, must be &gt;= 0 and &lt;
     *    <code>cbuf.length</code>.
     *
     * @param len
     *    the number of characters to copy, must be &gt;= 0 and <code>(off +
     *    len)</code> must be &lt;= <code>cbuf.length</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>cbuf == null
     *          || off &lt; 0
     *          || off &gt;= cbuf.length
     *          || len &lt; 0
     *          || (off + len &gt; cbuf.length)</code>.
     */
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
 
    /**
     * Appends all characters in the specified character string. If necessary,
     * the capacity of this string buffer will be increased.
     *
     * @param str
     *    the character string to copy characters from, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>str == null</code>.
     */
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
 
    /**
     * Appends the string representation of the specified <code>byte</code>.
     * If necessary, the capacity of this string buffer will be increased.
     *
     * @param n
     *    the number of which the string representation should be added to this
     *    buffer.
     */
    public void append(byte n) {
       append(String.valueOf(n));
    }
 
    /**
     * Appends the string representation of the specified <code>short</code>.
     * If necessary, the capacity of this string buffer will be increased.
     *
     * @param n
     *    the number of which the string representation should be added to this
     *    buffer.
     */
    public void append(short n) {
       append(String.valueOf(n));
    }
 
    /**
     * Appends the string representation of the specified <code>int</code>.
     * If necessary, the capacity of this string buffer will be increased.
     *
     * @param n
     *    the number of which the string representation should be added to this
     *    buffer.
     */
    public void append(int n) {
       append(String.valueOf(n));
    }
 
    /**
     * Appends the string representation of the specified <code>long</code>.
     * If necessary, the capacity of this string buffer will be increased.
     *
     * @param n
     *    the number of which the string representation should be added to this
     *    buffer.
     */
    public void append(long n) {
       append(String.valueOf(n));
    }
 
    /**
     * Appends the string representation of the specified <code>float</code>.
     * If necessary, the capacity of this string buffer will be increased.
     *
     * @param n
     *    the number of which the string representation should be added to this
     *    buffer.
     */
    public void append(float n) {
       append(String.valueOf(n));
    }
 
    /**
     * Appends the string representation of the specified <code>double</code>.
     * If necessary, the capacity of this string buffer will be increased.
     *
     * @param n
     *    the number of which the string representation should be added to this
     *    buffer.
     */
    public void append(double n) {
       append(String.valueOf(n));
    }
 
    /**
     * Gets the length of this string buffer. This is always &lt;= the
     * capacity.
     *
     * @return
     *    the number of characters in this buffer, always
     *    &lt;= {@link #getCapacity()}.
     */
    public int getLength() {
       return _length;
    }
 
    /**
     * Gets the capacity of this string buffer. This is always &gt;= the
     * length.
     *
     * @return
     *    the number of characters that fits in this buffer without having to
     *    extend the internal data structure, always &gt;=
     *    {@link #getLength()}.
     */
    public int getCapacity() {
       return _buffer.length;
    }
 
    /**
     * Sets the character at the specified index.
     *
     * @param index
     *    the index at which to set the character, must be &lt;
     *    {@link #getLength()}.
     *
     * @param newChar
     *    the new character value.
     *
     * @return
     *    the old character value.
     *
     * @throws IndexOutOfBoundsException
     *    if <code>index &lt; 0 || index &gt;= </code>{@link #getLength()}.
     */
    public char setChar(int index, char newChar)
    throws IndexOutOfBoundsException {
 
       if (index >= _length) {
          throw new IndexOutOfBoundsException("index (" + index + ") >= getLength() (" + _length + ')');
       }
 
       char oldChar = _buffer[index];
       _buffer[index] = newChar;
       return oldChar;
    }
 
    /**
     * Clears this string buffer. The capacity will remain untouched, though.
     */
    public void clear() {
       _length = 0;
    }
 
    public String toString() {
       return new String(_buffer, 0, _length);
    }
 }
