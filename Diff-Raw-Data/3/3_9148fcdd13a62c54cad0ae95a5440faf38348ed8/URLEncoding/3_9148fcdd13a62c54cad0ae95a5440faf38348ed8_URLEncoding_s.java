 /*
  * $Id$
  */
 package org.xins.util.net;
 
 import java.net.URLDecoder;
import org.xins.text.NonASCIIException;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.text.FastStringBuffer;
 
 /**
  * URL encoding utility functions. This class supports both encoding and
  * decoding. Only 7-bit ASCII characters are supported. All characters higher
  * than 127 (0x7f) will cause the encode or decode operation to fail.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.106
  */
 public final class URLEncoding extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Mappings from unencoded (array index) to encoded values (array
     * elements). The size of this array is 127.
     */
    private static final String[] UNENCODED_TO_ENCODED;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    static {
       UNENCODED_TO_ENCODED = new String[127];
       for (int i = 0; i < 127; i++) {
          char c = (char) i;
          if ((c >= 'a' && c <= 'z')   || (c >= 'A' && c <= 'Z')   || (c >= '0' && c <= '9')
           || (c == '-') || (c == '_') || (c == '.') || (c == '*')) {
             UNENCODED_TO_ENCODED[i] = String.valueOf(c);
          } else if (c == ' ') {
             UNENCODED_TO_ENCODED[i] = "+";
          } else {
             char[] data = new char[3];
             data[0] = '%';
             data[1] = Character.toUpperCase(Character.forDigit((i >> 4) & 0xF, 16));
             data[2] = Character.toUpperCase(Character.forDigit( i       & 0xF, 16));
             UNENCODED_TO_ENCODED[i] = new String(data);
          }
       }
    }
 
    /**
     * URL encodes the specified character string.
     *
     * @param s
     *    the string to URL encode, not <code>null</code>.
     *
     * @return
     *    URL encoded version of the specified character string, never
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>s == null</code>
     *
     * @throws NonASCIIException
     *    if <code>s.charAt(<em>n</em>) &gt; 127</code>,
     *    where <code>0 &lt;= <em>n</em> &lt; s.length</code>.
     */
    public static String encode(String s)
    throws IllegalArgumentException, NonASCIIException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("s", s);
 
       // Construct a buffer
       int length = s.length();
       FastStringBuffer buffer = new FastStringBuffer(length * 2);
 
       // Loop through the string and just append whatever we find
       // in UNENCODED_TO_ENCODED
       int c = -99;
       try {
          for (int i = 0; i < length; i++) {
             c = (int) s.charAt(i);
             buffer.append(UNENCODED_TO_ENCODED[c]);
          }
       } catch (IndexOutOfBoundsException exception) {
          throw new NonASCIIException(c);
       }
       
       return buffer.toString();
    }
 
    /**
     * Decodes the specified URL encoded character string.
     *
     * @param s
     *    the URL encoded string to decode, not <code>null</code>.
     *
     * @return
     *    unencoded version of the specified URL encoded character string,
     *    never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>s == null || s.charAt(<em>n</em>) &gt; 127</code>
     *    (where <code>0 &lt;= <em>n</em> &lt; s.length</code>).
     */
    public static final String decode(String s)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("s", s);
 
       // TODO: Use own method, throw exception if not 7-bit ASCII
       return URLDecoder.decode(s);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>URLEncoding</code> object.
     */
    private URLEncoding() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
