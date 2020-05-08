 
 package org.webmacro;
 import java.io.*;
 import java.util.Hashtable;
 import org.webmacro.util.*;
 
 
 /**
   * FastWriter attempts to optimize output speed in a WebMacro template
   * through several specific optimizations: 
   * <ul>
   *   <li> FastWriter caches the output in a byte array until you 
   *        call reset(). You can access the output by one of several
   *        methods: toString(), toByteArray(), or writeTo(OutputStream)
   *   <li> you can turn off unicode conversion by calling setAsciiHack()
   *   <li> you can use a unicode conversion cache by calling writeStatic()
   *   <li> you can get the contents written to the FastWriter back
   *        as an array of bytes INSTEAD of writing to the output stream
   * </ul>
   * Note that if you turn on the asciiHack and then write non-ASCII 
   * data the output will be mangled. 
   * <p>
   * <b>Note that the FastWriter requires an explicit flush</b>
   * <p>
   * If you re-use a FastWriter you must re-use it in a context which
   * uses the SAME unicode conversion. The caches and internal data 
   * structures which the FastWriter allocates are tied to the 
   * encoding it was created with. 
   */
 
 final public class FastWriter extends Writer
 {
    final private String _encoding;      // what encoding we use
    final private BufferedWriter _bwriter;
    final private ByteArrayOutputStream _bstream;
    final private EncodingCache _cache;
    
    private OutputStream _out;
 
    private byte[] _buf = new byte[512];
    private boolean _encodeProperly;  // are we in fast mode?
    private boolean _buffered;
 
    final private static Hashtable _writerCache = new Hashtable();
 
    /**
     * Create a FastWriter to the target outputstream. You must specify
     * a character encoding. You can also call writeTo(), toString(), 
     * and toByteArray() to access any un-flush()ed contents.
     */
    public FastWriter(OutputStream out, String encoding)
       throws java.io.UnsupportedEncodingException
    {
       _encoding = encoding;
       _bstream = new ByteArrayOutputStream(4096);
       _bwriter = new BufferedWriter(new OutputStreamWriter(_bstream, encoding));
       _cache = EncodingCache.getInstance(encoding);
 
       _encodeProperly = true;
       _buffered = false;
 
       _out = out;
    }
 
    /**
      * Create a new FastWriter with no output stream target. You can
      * still call writeTo(), toString(), and toByteArray().
      */
    public FastWriter(String encoding) 
       throws java.io.UnsupportedEncodingException
    {
       this(null,encoding);
    }
 
 
    /**
      * Get the character encoding this FastWriter uses to convert
      * characters to byte[]
      */
    public String getEncoding() {
       return _encoding;
    }
 
    /**
      * Get the output stream this FastWriter sends output to. It 
      * may be null, in which case output is not sent anywhere.
      */
    public OutputStream getOutputStream() {
       return _out;
    }
 
    /**
     * Ordinarily an expensive char-to-byte routine is used to convert
     * strings and char[]'s to byte format. If you know that your data
     * is going to be ASCII only for some number of writes, turn on 
     * this AsciiHack and then write the ASCII data. It's much faster. 
     * Remember to turn the AsciiHack off before writing true Unicode
     * characters, otherwise they'll be mangled.
     */
    public void setAsciiHack(boolean on) 
    {
       bflush();
       _encodeProperly = !on;
    }
 
    /**
      * Returns true if we are mangling the unicode conversion in an
      * attempt to eek out a bit of extra efficiency.
      */
    public boolean getAsciiHack() 
    {
       return !_encodeProperly;
    }
 
    /**
      * Write characters to the output stream performing slow unicode
      * conversion unless AsciiHack is on.
      */
    public void write(char[] cbuf) 
    {
       write(cbuf, 0, cbuf.length);
    }
 
 
    /**
      * Write characters to to the output stream performing slow unicode
      * conversion unless the AsciiHack is on. 
      */
    public void write(char[] cbuf, int offset, int len) 
    {
       try {
          if (_encodeProperly) {
             _bwriter.write(cbuf,offset,len);
             _buffered = true;
          } else {
             if (_buf.length < len) _buf = new byte[len];
             int end = offset + len;
             for (int i = offset; i < end; i++) {
                _buf[i] = (byte) cbuf[i];
             }
             _bstream.write(_buf,0,cbuf.length);
          }
       } catch (IOException e) {
          e.printStackTrace(); // never happens
       }
    }
 
    /**
      * Write a single character, performing slow unicode conversion
      * unless AsciiHack is on.
      */
    public void write(int c) 
    {
       try {
          if (_encodeProperly ) {
             _bwriter.write(c);
             _buffered = true;
          } else {
             _bstream.write((byte) c);
          }
       } catch (IOException e) {
          e.printStackTrace(); // never happens
       }
    }
 
    /**
      * Write a string to the underlying output stream, performing
      * unicode conversion.
      */
    public void write(String s) 
    {
       write(s,0,s.length());
    }
 
    /*
     * Write a string to the underlying output stream, performing
     * unicode conversion.
     */
    public void write(String s, int off, int len) 
    {
       try {
          if (_encodeProperly) {
             _bwriter.write(s,off,len);
             _buffered = true;
          } else {
             if (_buf.length < len) _buf = new byte[len];
             s.getBytes(off,len,_buf,0);
             _bstream.write(_buf,0,len);
          }
       } catch (IOException e) {
          e.printStackTrace(); // never happens
       }
    }
 
    /**
      * Write a string tot he underlying output stream, performing
      * unicode conversion if necessary--try and read the encoding
      * from an encoding cache if possible.
      */
    public void writeStatic(String s) 
    {
       try {
          if (_encodeProperly) {
             bflush();
             _bstream.write(_cache.getEncoding(s));
          } else {
             write(s,0,s.length());
          }
       } catch (IOException e) {
          e.printStackTrace(); // never happens
       }
    }
 
    /**
      * Flush out all the buffers
      */
    private void bflush() 
    {
       try {
          if (_buffered) { 
             _bwriter.flush();
             _buffered = false;
          }
       } catch (IOException e) {
          e.printStackTrace(); // never happens
       }
    }
 
    /**
      * Flush all data out to the OutputStream, if any, clearing 
      * the internal buffers. Note that data is ONLY written to 
      * the output stream on a flush() operation, and never at 
      * any other time. Consequently this is one of the few places
      * that you may actually encounter an IOException when using
      * the FastWriter class.
      */
    public void flush() throws IOException
    {
       bflush();
       if (_out != null) {
          writeTo(_out);
          _out.flush();
       }
       _bstream.reset();
    }
 
    /**
      * Copy the contents written so far into a byte array.
      */
    public byte[] toByteArray() 
    {
       bflush();
       return _bstream.toByteArray();
    }
 
    /**
      * Copy the contents written so far into a String. 
      */
     public String toString() 
     {
        try {
           bflush();
           return _bstream.toString(_encoding); 
        } catch (UnsupportedEncodingException e) {
           e.printStackTrace(); // never happen: we already used it
           return null;
        }
     }
 
    /**
      * Copy the contents written so far to the suppiled output stream
      */
    public void writeTo(OutputStream out) throws IOException
    {
       bflush();
       _bstream.writeTo(out);
    }
 
    /**
      * Reset the fastwriter, clearing any contents that have 
      * been generated so far.
      */
    public void reset(OutputStream out) {
       bflush();
       _bstream.reset();
       _out = out;
    }
 
    /**
      * Get a new FastWriter. You must then call writeTo(..) before
      * attempting to write to the FastWriter.
      */
    public static FastWriter getInstance(OutputStream out, String encoding) 
       throws UnsupportedEncodingException
    {
       FastWriter fw = null;
       SimpleStack ss = (SimpleStack) _writerCache.get(encoding);
       if (ss != null) {
          fw = (FastWriter) ss.pop();
         fw.reset(out);
       }
      if (fw == null) {
         fw = new FastWriter(out,encoding);
      } 
      return fw;
    }
 
    /**
      * Return a FastWriter with the specified encoding and no output stream.
      */
    public static FastWriter getInstance(String encoding) 
       throws UnsupportedEncodingException
    {
       return getInstance(null,encoding);
    }
 
    /**
      * Return a FastWriter with default encoding and no output stream.
      */
    public static FastWriter getInstance() 
    {
       try {
          return getInstance(null,"UNICODE");
       } catch (UnsupportedEncodingException e) {
          e.printStackTrace(); // never gonna happen
          return null;
       }
    }
 
    /**
      * Return the FastWriter to the queue for later re-use. You must
      * not use the FastWriter after this call. Calling close() 
      * returns the FastWriter to the pool. If you don't want to
      * return it to the pool just discard it without a close().
      */
    public void close() throws IOException
    {
       flush();
       if (_out != null) {
          _out.close();
          _out = null;
       }
       SimpleStack ss = (SimpleStack) _writerCache.get(_encoding);
       if (ss == null) {
          ss = new SimpleStack();
          _writerCache.put(_encoding,ss);
       }
       ss.push(this);
    }
 
    public static void main(String arg[]) {
 
       System.out.println("----START----");
       try {
          FastWriter fw = new FastWriter("UTF8");
          fw.setAsciiHack(false);
          for (int i = 0; i < arg.length; i++) {
             fw.writeStatic("write: ");
             fw.write(arg[i]);
             fw.writeStatic("\n");
 
             fw.writeStatic("cache: ");
             fw.writeStatic(arg[i]);
             fw.writeStatic("\n");
          }
          fw.flush();
          fw.writeTo(System.out);
       } catch (Exception e) {
          e.printStackTrace();
       }
       System.out.println("----DONE----");
 
    }
 
 }
 
