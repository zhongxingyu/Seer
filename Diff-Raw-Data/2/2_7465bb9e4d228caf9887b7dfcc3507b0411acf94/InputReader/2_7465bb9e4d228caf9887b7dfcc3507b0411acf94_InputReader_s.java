 /*
  * Copyright (C) 2012 Timo Vesalainen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.vesalainen.parser.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import org.vesalainen.regex.Range.BoundaryType;
 import org.vesalainen.regex.SyntaxErrorException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PushbackReader;
 import java.io.Reader;
 import java.io.Writer;
 import java.lang.reflect.Member;
 import java.lang.reflect.Type;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 import java.util.ArrayDeque;
 import java.util.Deque;
 import org.vesalainen.bcc.type.Generics;
 import org.vesalainen.grammar.GTerminal;
 import org.xml.sax.InputSource;
 
 /**
  * Reader that stores input in ring buffer. Ring buffer size must be big enough
  * to enable rewind and access to captured data
  * @author tkv
  */
 public class InputReader extends Reader implements AutoCloseable
 {
     private char[] array;       // backing array
     private int size;           // size of ring buffer (=buffer.length)
     private int end;            // position of last actual read char
     private int cursor;         // position of current input
     private IncludeLevel includeLevel = new IncludeLevel();
     private Deque<IncludeLevel> includeStack;
     private int length;         // length of current input
     private int findSkip;       // number of characters the find can skip after unsucces
     private int findMark = -1;  // position where find could have last accessed the string
     private boolean useOffsetLocatorException;
 
     public InputReader(File file, int size) throws FileNotFoundException
     {
         this(new FileInputStream(file), size);
     }
     public InputReader(File file, int size, String cs) throws FileNotFoundException
     {
         this(new FileInputStream(file), size, cs);
     }
     public InputReader(File file, int size, String cs, boolean upper) throws FileNotFoundException
     {
         this(new FileInputStream(file), size, cs, upper);
     }
     public InputReader(File file, int size, Charset cs) throws FileNotFoundException
     {
         this(new FileInputStream(file), size, cs);
     }
     public InputReader(File file, int size, Charset cs, boolean upper) throws FileNotFoundException
     {
         this(new FileInputStream(file), size, cs, upper);
     }
     /**
      * Constructs an InputReader with default charset
      * @param is
      * @param size size of inner ring buffer
      */
     public InputReader(InputStream is, int size)
     {
         this(new StreamReader(is), size);
     }
     public InputReader(InputStream is, int size, boolean upper)
     {
         this(new StreamReader(is), size, upper);
     }
     /**
      * Constructs an InputReader
      * @param is
      * @param size size of inner ring buffer
      * @param cs Character set
      */
     public InputReader(InputStream is, int size, String cs)
     {
         this(new StreamReader(is, cs), size);
     }
     public InputReader(InputStream is, int size, String cs, boolean upper)
     {
         this(new StreamReader(is, cs), size, upper);
     }
     /**
      * Constructs an InputReader
      * @param is
      * @param size
      * @param cs 
      */
     public InputReader(InputStream is, int size, Charset cs)
     {
         this(new StreamReader(is, cs), size);
     }
     public InputReader(InputStream is, int size, Charset cs, boolean upper)
     {
         this(new StreamReader(is, cs), size, upper);
     }
     /**
      * Constructs an InputReader
      * @param sr
      * @param size 
      */
     public InputReader(StreamReader sr, int size)
     {
         this(new PushbackReader(sr), size);
         includeLevel.setStreamReader(sr);
     }
     /**
      * Constructs an InputReader
      * @param in
      * @param size
      * @param upper If true input is converted upper-case, if false input is converted lower-case
      */
     public InputReader(Reader in, int size, boolean upper)
     {
         this(new CaseChangeReader(in, upper), size);
     }
     /**
      * Constructs an InputReader
      * @param in
      * @param size 
      */
     public InputReader(Reader in, int size)
     {
         this(new PushbackReader(in), size);
     }
     /**
      * Constructs an InputReader
      * @param in
      * @param size 
      */
     public InputReader(PushbackReader in, int size)
     {
         this(in, new char[size]);
     }
     /**
      * Constructs an InputReader
      * @param shared Shared ringbuffer.
      */
     public InputReader(PushbackReader in, char[] shared)
     {
         size = shared.length;
         includeLevel.setIn(in);
         array = shared;
     }
     /**
      * Constructs an InputReader
      * @param text
      */
     public InputReader(CharSequence text)
     {
         size = text.length();
         end = size;
         array = text.toString().toCharArray();
     }
     public void reuse(CharSequence text)
     {
         size = text.length();
         end = size;
         array = text.toString().toCharArray();
         cursor = 0;
         includeLevel.reset();
         includeStack = null;
         length = 0;
         findSkip = 0;
         findMark = -1;  // position where find could have last accessed the string
     }
     /**
      * Constructs an InputReader
      * @param text
      * @param size 
      */
     public InputReader(CharSequence text, int size)
     {
         if (size < text.length())
         {
             throw new IllegalArgumentException("buffer size "+size+" < text length "+text.length());
         }
         this.size = size;
         array = new char[size];
         for (int ii=0;ii<text.length();ii++)
         {
             array[ii] = text.charAt(ii);
         }
         end = text.length();
     }
     /**
      * Constructs an InputReader
      * @param array
      */
     public InputReader(char[] array)
     {
         size = array.length;
         this.array = array;
         end = size;
     }
     /**
      * Constructs an InputReader
      * @param input
      * @param size Ringbuffer size
      * @return
      * @throws IOException 
      */
     public static InputReader getInstance(InputSource input, int size) throws IOException
     {
         InputReader inputReader = null;
         Reader reader = input.getCharacterStream();
         if (reader != null)
         {
             inputReader = new InputReader(reader, size);
         }
         else
         {
             InputStream is = input.getByteStream();
             String encoding = input.getEncoding();
             if (is != null)
             {
                 if (encoding != null)
                 {
                     inputReader = new InputReader(is, size, encoding);
                 }
                 else
                 {
                     inputReader = new InputReader(is, size, "US-ASCII");
                 }
             }
             else
             {
                 String sysId = input.getSystemId();
                 try
                 {
                     URI uri = new URI(sysId);
                     InputStream uis = uri.toURL().openStream();
                     if (encoding != null)
                     {
                         inputReader = new InputReader(uis, size, encoding);
                     }
                     else
                     {
                         inputReader = new InputReader(uis, size, "US-ASCII");
                     }
                 }
                 catch (URISyntaxException ex)
                 {
                     throw new IOException(ex);
                 }
             }
         }
         inputReader.setSource(input.getSystemId());
         return inputReader;
     }
     public void useOffsetLocatorException(boolean useOffsetLocatorException)
     {
         this.useOffsetLocatorException = useOffsetLocatorException;
     }
     /**
      * Set current character set. Only supported with InputStreams!
      * @param cs 
      */
     public void setEncoding(String cs)
     {
         setEncoding(Charset.forName(cs));
     }
     /**
      * Set current character set. Only supported with InputStreams!
      * @param cs 
      */
     public void setEncoding(Charset cs)
     {
         if (includeLevel.getStreamReader() == null)
         {
             throw new UnsupportedOperationException("setting charset not supported with current input");
         }
         includeLevel.getStreamReader().setCharset(cs);
     }
     /**
      * Set's the source of current input
      * @param source 
      */
     public void setSource(String source)
     {
         includeLevel.setSource(source);
     }
     /**
      * Get's the source of current input
      * @param source 
      */
     public String getSource()
     {
         return includeLevel.getSource();
     }
     
     public void throwSyntaxErrorException() throws SyntaxErrorException
     {
         throwSyntaxErrorException(null);
     }
    public void throwSyntaxErrorException(Throwable thr) throws SyntaxErrorException
     {
         String source = includeLevel.getSource();
         if (useOffsetLocatorException)
         {
             throw new OffsetLocatorException("syntax error", source, getStart(), getEnd(), thr);
         }
         else
         {
             int line = getLineNumber();
             int column = getColumnNumber();
             throw new LineLocatorException("source: "+source+"\n"+
                     "syntax error at line "+line+": pos "+column+
                     "\n"+
                     getLine()+
                     "\n"+
                     pointer(getColumnNumber()),
                     source,
                     line,
                     column,
                     thr
                     );
         }
     }
 
     public void throwSyntaxErrorException(String expecting, String token) throws SyntaxErrorException
     {
         String source = includeLevel.getSource();
         if (useOffsetLocatorException)
         {
             throw new OffsetLocatorException("Expected: '"+expecting+"' got "+token+"='"+getString()+"'", source, getStart(), getEnd());
         }
         else
         {
             int line = getLineNumber();
             int column = getColumnNumber();
             throw new LineLocatorException("source: "+source+"\n"+
                     "Expected: '"+expecting+"' at line "+line+": pos "+column+
                     "\n"+
                     getLine()+
                     "\n"+
                     pointer(getColumnNumber())+
                     "\n got "+token+"='"+getString()+"'",
                     source,
                     line,
                     column
                     );
         }
     }
 
     private String pointer(int p)
     {
         StringBuilder sb = new StringBuilder();
         for (int ii=1;ii<p;ii++)
         {
             sb.append(" ");
         }
         sb.append("^^^");
         return sb.toString();
     }
     /**
      * Return true if next input id eof
      * @return
      * @throws IOException 
      */
     public boolean isEof() throws IOException
     {
         return peek(1) == -1;
     }
     /**
      * Inserts text at cursor position
      * @param text 
      */
     public void insert(CharSequence text) throws IOException
     {
         int ln = text.length();
         if (ln == 0)
         {
             return;
         }
         if (ln >= size - (end-cursor))
         {
             throw new IOException(text+" doesn't fit in the buffer");
         }
         if (cursor != end)
         {
             makeRoom(ln);
         }
         for (int ii=0;ii<ln;ii++)
         {
             array[(cursor+ii) % size] = text.charAt(ii);
         }
         end += ln;
     }
     /**
      * Inserts text at cursor position
      * @param text 
      */
     public void insert(char[] text) throws IOException
     {
         int ln = text.length;
         if (ln == 0)
         {
             return;
         }
         if (ln >= size - (end-cursor))
         {
             throw new IOException(text+" doesn't fit in the buffer");
         }
         if (cursor != end)
         {
             makeRoom(ln);
         }
         int cms = cursor % size;
         if (size - cms < text.length)
         {
             System.arraycopy(text, 0, array, cms, size - cms);
             System.arraycopy(text, size - cms, array, 0, text.length - (size - cms));
         }
         else
         {
             System.arraycopy(text, 0, array, cms, text.length);
         }
         end += ln;
     }
     private void makeRoom(int ln)
     {
         int src = 0;
         int dst = 0;
         int len = 0;
         int ems = end % size;
         int cms = cursor % size;
         if (ems < cms)
         {
             src = 0;
             dst = ln;
             len = ems;
             System.arraycopy(array, src, array, dst, len);
         }
         int spaceAtEndOfBuffer = 0;
         if (ems >= cms)
         {
             spaceAtEndOfBuffer = size - ems;
         }
         int needToWrap = Math.min(ln - spaceAtEndOfBuffer, size - cms);
         if (needToWrap > 0)
         {
             src = size - spaceAtEndOfBuffer - needToWrap;
             dst = ln-needToWrap - spaceAtEndOfBuffer;
             len = needToWrap;
             System.arraycopy(array, src, array, dst, len);
         }
         src = cms;
         if (ems < cms)
         {
             len = (size - cms) - needToWrap;
         }
         else
         {
             len = (ems - cms) - needToWrap;
         }
         dst = Math.min(cms + ln, size-1);
         System.arraycopy(array, src, array, dst, len);
     }
     /**
      * Synchronizes actual reader to current cursor position
      * @throws IOException
      */
     public void release() throws IOException
     {
         if (includeLevel.in != null)
         {
             for (int ii=cursor;ii<end;ii++)
             {
                 includeLevel.in.unread(array[ii % size]);
             }
             end = cursor;
         }
     }
     /**
      * Returns the length of current input
      * @return
      */
     public int getLength()
     {
         return length;
     }
     /**
      * Returns the start position of current input
      * @return
      */
     public int getStart()
     {
         return cursor-length;
     }
     /**
      * Returns the end position of current input
      * @return
      */
     public int getEnd()
     {
         return cursor;
     }
     /**
      * Returns a reference to current field. Field start and length are decoded
      * in int value;
      * @return
      */
     public int getFieldRef()
     {
         if (size > 0xffff)
         {
             throw new IllegalArgumentException("fieldref not supported when buffer size is >65535");
         }
         return (cursor-length) % size + length * 0x10000;
     }
 
     public int concat(int fieldRef1, int fieldRef2)
     {
         int l1 = fieldRef1>>16;
         int s1 = fieldRef1 & 0xffff;
         int l2 = fieldRef2>>16;
         int s2 = fieldRef2 & 0xffff;
         return (s1) % size + (l1+l2) * 0x10000;
     }
 
     public boolean equals(int fieldRef, char[] buf)
     {
         int l = fieldRef>>16;
         int s = fieldRef & 0xffff;
         for (int ii=0;ii<l;ii++)
         {
             if (buf[ii] != array[(s+ii) % size])
             {
                 return false;
             }
         }
         return true;
     }
     /**
      * Returns the last matched input
      * @return 
      */
     public String getString()
     {
         return getString(cursor-length, length);
     }
 
     public String getString(int fieldRef)
     {
         return getString(fieldRef & 0xffff, fieldRef>>16);
     }
 
     public boolean getBoolean()
     {
         return parseBoolean();
     }
 
     public byte getByte()
     {
         return parseByte();
     }
 
     public char getChar()
     {
         return parseChar();
     }
 
     public short getShort()
     {
         return parseShort();
     }
 
     public int getInt()
     {
         return parseInt();
     }
 
     public long getLong()
     {
         return parseLong();
     }
 
     public float getFloat()
     {
         return parseFloat();
     }
 
     public double getDouble()
     {
         return parseDouble();
     }
 
     public void write(Writer writer) throws IOException
     {
         write(cursor-length, length, writer);
     }
 
     public void write(int s, int l, Writer writer) throws IOException
     {
         if (s < end-size)
         {
             throw new IllegalArgumentException("buffer too small");
         }
         int ps = s % size;
         int es = (s+l) % size;
         if (ps <= es)
         {
             writer.write(array, ps, l);
         }
         else
         {
             writer.write(array, ps, size-ps);
             writer.write(array, 0, es);
         }
     }
 
     public char[] getArray()
     {
         return array;
     }
 
     public String buffered()
     {
         return getString(cursor, end-cursor);
     }
     public String getString(int s, int l)
     {
         if (s < end-size)
         {
             throw new IllegalArgumentException("buffer too small");
         }
         int ps = s % size;
         int es = (s+l) % size;
         if (ps <= es)
         {
             return new String(array, ps, l);
         }
         else
         {
             StringBuilder sb = new StringBuilder();
             sb.append(array, ps, size-ps);
             sb.append(array, 0, es);
             return sb.toString();
         }
     }
 
     public String getLine()
     {
         int c = includeLevel.getColumn();
         if (cursor-c < end-size)
         {
             int len = size / 2;
             return "... "+getString(end-len, len);
         }
         else
         {
             return getString(cursor-c, end-(cursor-c));
         }
     }
     /**
      * Returns the input data after last release call
      * @return 
      */
     public String getInput()
     {
         return getString(cursor-length, length);
     }
     
     @Override
     public String toString()
     {
         return getInput();
     }
     /**
      * get a char from input buffer.
      * @param offset 0 is last read char.
      * @return
      * @throws IOException
      */
     public int peek(int offset) throws IOException
     {
         int target = cursor + offset - 1;
         if (target - end > size || target < end - size || target < 0)
         {
             throw new IllegalArgumentException("offset "+offset+" out of buffer");
         }
         if (target >= end)
         {
             int la = 0;
             while (target >= end)
             {
                 int cc = read();
                 if (cc == -1)
                 {
                     if (target+la == end)
                     {
                         return -1;
                     }
                     else
                     {
                         throw new IOException("eof");
                     }
                 }
                 la++;
             }
             rewind(la);
         }
         return array[target % size];
     }
     /**
      * Set how many characters we can skip after failed find.
      * @param acceptStart 
      */
     public void setAcceptStart(int acceptStart)
     {
         findSkip = acceptStart;
     }
     /**
      * Marks to position where find could accept the input.
      */
     public void findAccept()
     {
         findMark = cursor;
     }
     /**
      * Unread to the last findMark. Used after succesfull find.
      */
     public void findPushback() throws IOException
     {
         assert findMark >= 0;
         rewind(cursor-findMark);
     }
     /**
      * Resets positions suitable for next find. Used after failed find to continue at next
      * character.
      * @throws IOException
      */
     public void findRecover() throws IOException
     {
         assert findSkip >= 0;
         if (findSkip > 0)
         {
             rewind(length-findSkip);
         }
         length = 0;
     }
     /**
      * Rewinds cursor position count characters. Used for unread.
      * @param count
      * @throws IOException
      */
     public void rewind(int count) throws IOException
     {
         if (count < 0)
         {
             throw new IllegalArgumentException("negative rewind "+count);
         }
         cursor -= count;
         if (cursor < end - size || cursor < 0)
         {
             throw new IOException("insufficient room in the pushback buffer");
         }
         length -= count;
         if (length < 0)
         {
             throw new IOException("rewinding past input");
         }
         int ld = 0;
         for (int ii=0;ii<count;ii++)
         {
             if (array[(cursor+ii) % size] == '\n')
             {
                 ld++;
             }
         }
         if (ld > 0)
         {
             int l = includeLevel.getLine();
             includeLevel.setLine(l - ld);
             int c = 0;
             int start = Math.max(0, end-size);
             for (int ii=cursor;ii>=start;ii--)
             {
                 if (array[ii % size] == '\n')
                 {
                     break;
                 }
                 c++;
             }
             includeLevel.setColumn(c);
         }
         else
         {
             int c = includeLevel.getColumn();
             includeLevel.setColumn(c - count);
         }
     }
     public void unread() throws IOException
     {
         rewind(length);
     }
     public void unreadLa(int len) throws IOException
     {
         length += len;
         rewind(length);
     }
 /*
     public void unread(char[] cbuf) throws IOException
     {
         rewind(cbuf.length);
     }
 
     public void unread(char[] cbuf, int off, int len) throws IOException
     {
         rewind(len);
     }
  *
  */
     public void unread(int c) throws IOException
     {
         rewind(1);
     }
     /**
      * Reads from ring buffer or from actual reader.
      * @return
      * @throws IOException
      */
     @Override
     public int read() throws IOException
     {
         assert cursor <= end;
         if (cursor >= end)
         {
             if (includeLevel.in == null)
             {
                 return -1;
             }
             int cc = includeLevel.in.read();
             if (cc == -1)
             {
                 if (includeStack != null)
                 {
                     while (!includeStack.isEmpty() && cc == -1) // TODO how to close those
                     {
                         includeLevel = includeStack.pop();
                         cc = includeLevel.in.read();
                     }
                     if (cc == -1)
                     {
                         return -1;
                     }
                 }
                 else
                 {
                     return -1;
                 }
             }
             array[cursor % size] = (char) cc;
             end++;
         }
         int rc = array[cursor++ % size];
         includeLevel.forward(rc);
         length++;
         if (length > size)
         {
             throw new IOException("input size "+length+" exceeds buffer size "+size);
         }
         return rc;
     }
 
     public void include(InputStream is, String source) throws IOException
     {
         include(is, Charset.defaultCharset(), source);
     }
     
     public void include(InputStream is, String cs, String source) throws IOException
     {
         include(is, Charset.forName(cs), source);
     }
     
     public void include(InputStream is, Charset cs, String source) throws IOException
     {
         if (cursor != end)
         {
             throw new IOException("not allowed to include when buffer is not empty");
         }
         if (includeStack == null)
         {
             includeStack = new ArrayDeque<>();
         }
         includeStack.push(includeLevel);
         StreamReader sr = new StreamReader(is, cs);
         PushbackReader pr = new PushbackReader(sr);
         includeLevel = new IncludeLevel(pr, sr, source);
     }
     
     public void include(PushbackReader nin, String source) throws IOException
     {
         if (cursor != end)
         {
             throw new IOException("not allowed to include when buffer is not empty");
         }
         if (includeStack == null)
         {
             includeStack = new ArrayDeque<>();
         }
         includeStack.push(includeLevel);
         includeLevel = new IncludeLevel(nin, source);
     }
     
     public void reRead(int count) throws IOException
     {
         if (count < 0)
         {
             throw new IOException("count="+count);
         }
         assert cursor <= end;
         for (int ii=0;ii<count;ii++)
         {
             if (cursor >= end)
             {
                 throw new IOException("reRead's unread data");
             }
             int rc = array[cursor++ % size];
             includeLevel.forward(rc);
             length++;
             if (length > size)
             {
                 throw new IOException("input size "+length+" exceeds buffer size "+size);
             }
         }
     }
 
     public int read(char[] cbuf, int off, int len) throws IOException
     {
         for (int ii=0;ii<len;ii++)
         {
             int cc = read();
             if (cc == -1)
             {
                 if (ii == 0)
                 {
                     return -1;
                 }
                 else
                 {
                     return ii;
                 }
             }
             cbuf[ii+off] = (char) cc;
         }
         return len;
     }
 
     @Override
     public int read(char[] cbuf) throws IOException
     {
         for (int ii=0;ii<cbuf.length;ii++)
         {
             int cc = read();
             if (cc == -1)
             {
                 if (ii == 0)
                 {
                     return -1;
                 }
                 else
                 {
                     return ii;
                 }
             }
             cbuf[ii] = (char) cc;
         }
         return cbuf.length;
     }
 
     @Override
     public int read(CharBuffer target) throws IOException
     {
         int count = 0;
         while (target.hasRemaining())
         {
             int cc = read();
             if (cc == -1)
             {
                 if (count == 0)
                 {
                     return -1;
                 }
                 else
                 {
                     return count;
                 }
             }
             target.put((char)cc);
             count++;
         }
         return count;
     }
 
     @Override
     public long skip(long n) throws IOException
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void reset() throws IOException
     {
         throw new UnsupportedOperationException();
     }
     /**
      * Clears input. After that continues to next input token.
      * @throws IOException
      */
     public void clear()
     {
         length = 0;
         findSkip = 0;
         findMark = -1;
     }
 
     @Override
     public boolean ready() throws IOException
     {
         if (includeLevel.in != null)
         {
             return includeLevel.in.ready();
         }
         else
         {
             return true;
         }
     }
 
     @Override
     public boolean markSupported()
     {
         return false;
     }
 
     @Override
     public void mark(int readAheadLimit) throws IOException
     {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void close() throws IOException
     {
         if (includeLevel.in != null)
         {
             includeLevel.in.close();
         }
     }
 
     public static Member getParseMethod(Type type, GTerminal terminal) throws NoSuchMethodException
     {
         if (Generics.isPrimitive(type))
         {
             String name = Generics.getName(type);
             int radix = terminal.getBase();
             if (radix != 10)
             {
                 if (radix > 0)
                 {
                     name = name+"Radix"+radix;
                 }
                 else
                 {
                     radix = -radix;
                     name = name+"Radix2C"+radix;
                 }
             }
             return InputReader.class.getMethod("parse"+name.toUpperCase().substring(0, 1)+name.substring(1));
         }
         else
         {
             if (String.class.equals(type))
             {
                 return InputReader.class.getMethod("getString");
             }
             throw new IllegalArgumentException("no parse method for non primitive type "+type+" at "+terminal);
         }
     }
     /**
      * Returns true if content is string 'true' ignoring case
      * @return
      */
     public boolean parseBoolean()
     {
         return parseBoolean(cursor-length, length);
     }
     /**
      * Converts part of input
      * @param s Start position starting at 0
      * @param l Length
      * @return
      */
     private boolean parseBoolean(int s, int l)
     {
         if (
             l == 4 &&
             (array[s % size] == 'T' || array[s % size] == 't') &&
             (array[(s+1) % size] == 'R' || array[(s+1) % size] == 'r') &&
             (array[(s+2) % size] == 'U' || array[(s+2) % size] == 'u') &&
             (array[(s+3) % size] == 'E' || array[(s+3) % size] == 'e')
                 )
         {
             return true;
         }
         else
         {
             return false;
         }
     }
     /**
      * Returns the only character of string
      * @return
      */
     public char parseChar()
     {
         return parseChar(cursor-length, length);
     }
     /**
      * Converts part of input
      * @param s Start position starting at 0
      * @param l Length
      * @return
      */
     private char parseChar(int s, int l)
     {
         if (l != 1)
         {
             throw new IllegalArgumentException("cannot convert "+this+" to char");
         }
         return array[s % size];
     }
     /**
      * Parses string content to byte "6" -&gt; 6
      * Minus is allowed as first character
      * @return
      */
     public byte parseByte()
     {
         return parseByte(cursor-length, length);
     }
     /**
      * Converts part of input
      * @param s Start position starting at 0
      * @param l Length
      * @return
      */
     private byte parseByte(int s, int l)
     {
         int i = parseInt(s, l);
         if (i < Byte.MIN_VALUE || i > 0xff)
         {
             throw new IllegalArgumentException("cannot convert "+this+" to byte");
         }
         return (byte) i;
     }
     /**
      * Parses string content to short "123" -&gt; 123
      * Minus is allowed as first character
      * @return
      */
     public short parseShort()
     {
         return parseShort(cursor-length, length);
     }
     /**
      * Converts part of input
      * @param s Start position starting at 0
      * @param l Length
      * @return
      */
     private short parseShort(int s, int l)
     {
         int i = parseInt(s, l);
         if (i < Short.MIN_VALUE || i > 0xffff)
         {
             throw new IllegalArgumentException("cannot convert "+this+" to short");
         }
         return (short) i;
     }
     /**
      * Parses string content to int "123" -&gt; 123
      * Minus is allowed as first character
      * @return
      */
     public int parseInt()
     {
         return parseInt(cursor-length, length);
     }
     /**
      * Parses string content to int "011" -&gt; 3
      * @return
      */
     public int parseIntRadix2()
     {
         return parseInt(cursor-length, length, 2);
     }
     public int parseIntRadix2C2()
     {
         return parseInt(cursor-length, length, -2);
     }
     public long parseLongRadix2()
     {
         return parseLong(cursor-length, length, 2);
     }
     public long parseLongRadix2C2()
     {
         return parseLong(cursor-length, length, -2);
     }
     /**
      * Converts part of input
      * @param s Start position starting at 0
      * @param l Length
      * @return
      */
     private int parseInt(int s, int l)
     {
         int sign = 1;
         int result = 0;
         int start = 0;
         if (l == 0)
         {
             throw new IllegalArgumentException("cannot convert "+this+" to int");
         }
         if (array[s % size] == '-')
         {
             sign = -1;
             start = 1;
         }
         for (int j=start;j<l;j++)
         {
             int ii=s+j;
             switch (array[ii % size])
             {
                 case '0':
                 case '1':
                 case '2':
                 case '3':
                 case '4':
                 case '5':
                 case '6':
                 case '7':
                 case '8':
                 case '9':
                     result = 10*result + array[ii % size] - '0';
                     break;
                 default:
                     throw new IllegalArgumentException("cannot convert "+this+" to int");
             }
             if (result < 0)
             {
                 throw new IllegalArgumentException("cannot convert "+this+" to int");
             }
         }
         return sign*result;
     }
     /**
      * Converts binary to int
      * @param s
      * @param l
      * @param radix
      * @return 
      */
     private int parseInt(int s, int l, int radix)
     {
         assert radix == 2 || radix == -2;
         if (l > 32)
         {
             throw new IllegalArgumentException("bit number "+l+" is too much for int");
         }
         int result = 0;
         int start = 0;
         if (l == 0)
         {
             throw new IllegalArgumentException("cannot convert "+this+" to int");
         }
         for (int j=start;j<l;j++)
         {
             int ii=s+j;
             result <<= 1;
             switch (array[ii % size])
             {
                 case '0':
                     break;
                 case '1':
                     result++;
                     break;
                 default:
                     throw new IllegalArgumentException("cannot convert "+this+" to int");
             }
         }
         if (radix > 0 || result < (1<<(l-1)))
         {
             return result;
         }
         else
         {
             return result - (Integer.MAX_VALUE>>(Integer.SIZE-l));
         }
     }
     private long parseLong(int s, int l, int radix)
     {
         assert radix == 2 || radix == -2;
         if (l > 64)
         {
             throw new IllegalArgumentException("bit number "+l+" is too much for long");
         }
         long result = 0;
         int start = 0;
         if (l == 0)
         {
             throw new IllegalArgumentException("cannot convert "+this+" to long");
         }
         for (int j=start;j<l;j++)
         {
             int ii=s+j;
             result <<= 1;
             switch (array[ii % size])
             {
                 case '0':
                     break;
                 case '1':
                     result++;
                     break;
                 default:
                     throw new IllegalArgumentException("cannot convert "+this+" to long");
             }
         }
         if (radix > 0 || result < (1<<(l-1)))
         {
             return result;
         }
         else
         {
             return result - (Long.MAX_VALUE>>(Long.SIZE-l));
         }
     }
     /**
      * Parses string content to long "123" -&gt; 123
      * Minus is allowed as first character
      * @return
      */
     public long parseLong()
     {
         return parseLong(cursor-length, length);
     }
     /**
      * Converts part of input
      * @param s Start position starting at 0
      * @param l Length
      * @return
      */
     private long parseLong(int s, int l)
     {
         int sign = 1;
         long result = 0;
         int start = 0;
         if (l == 0)
         {
             throw new IllegalArgumentException("cannot convert "+this+" to int");
         }
         if (array[s % size] == '-')
         {
             sign = -1;
             start = 1;
         }
         for (int j=start;j<l;j++)
         {
             int ii=s+j;
             switch (array[ii % size])
             {
                 case '0':
                 case '1':
                 case '2':
                 case '3':
                 case '4':
                 case '5':
                 case '6':
                 case '7':
                 case '8':
                 case '9':
                     result = 10*result + array[ii % size] - '0';
                     break;
                 default:
                     throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to long");
             }
             if (result < 0)
             {
                 throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to long");
             }
         }
         return sign*result;
     }
 
     /**
      * Parses string content to float "123.456" -&gt; 123.456
      * Minus is allowed as first character.
      * Decimal separator is dot (.)
      * Scientific notation is supported. E.g -1.23456E-9
      * @return
      */
     public float parseFloat()
     {
         return parseFloat(cursor-length, length);
     }
     /**
      * Converts part of input
      * @param s Start position starting at 0
      * @param l Length
      * @return
      */
     private float parseFloat(int s, int l)
     {
         int sign = 1;
         float result = 0;
         int start = 0;
         int decimal = -1;
         boolean decimalPart = false;
         int mantissa = 0;
         int mantissaSign = 1;
         boolean mantissaPart = false;
         if (length == 0)
         {
             throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to float");
         }
         if (array[s % size] == '-')
         {
             sign = -1;
             start = 1;
         }
         for (int j=start;j<l;j++)
         {
             int ii=s+j;
             switch (array[ii % size])
             {
                 case '0':
                 case '1':
                 case '2':
                 case '3':
                 case '4':
                 case '5':
                 case '6':
                 case '7':
                 case '8':
                 case '9':
                     if (mantissaPart)
                     {
                         mantissa = 10*mantissa + array[ii % size] - '0';
                     }
                     else
                     {
                         if (decimalPart)
                         {
                             result += (array[ii % size] - '0')*Math.pow(10, decimal);
                             decimal--;
                         }
                         else
                         {
                             result = 10*result + array[ii % size] - '0';
                         }
                     }
                     break;
                 case '.':
                     decimalPart = true;
                     break;
                 case 'E':
                     mantissaPart = true;
                     break;
                 case '-':
                     if (!mantissaPart)
                     {
                         throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to float");
                     }
                     mantissaSign = -1;
                     break;
                 case '+':
                     if (!mantissaPart)
                     {
                         throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to float");
                     }
                     break;
                 default:
                     throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to float");
             }
             if (result < 0)
             {
                 throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to float");
             }
         }
         return (float) (sign * result * Math.pow(10, mantissa*mantissaSign));
     }
 
     /**
      * Parses string content to double "123.456" -&gt; 123.456
      * Minus is allowed as first character.
      * Decimal separator is dot (.)
      * Scientific notation is supported. E.g -1.23456E-9
      * @return
      */
     public double parseDouble()
     {
         return parseDouble(cursor-length, length);
     }
     /**
      * Converts part of input
      * @param s Start position starting at 0
      * @param l Length
      * @return
      */
     private double parseDouble(int s, int l)
     {
         int sign = 1;
         double result = 0;
         int start = 0;
         int decimal = -1;
         boolean decimalPart = false;
         int mantissa = 0;
         int mantissaSign = 1;
         boolean mantissaPart = false;
         if (length == 0)
         {
             throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to double");
         }
         if (array[s % size] == '-')
         {
             sign = -1;
             start = 1;
         }
         for (int j=start;j<l;j++)
         {
             int ii=s+j;
             switch (array[ii % size])
             {
                 case '0':
                 case '1':
                 case '2':
                 case '3':
                 case '4':
                 case '5':
                 case '6':
                 case '7':
                 case '8':
                 case '9':
                     if (mantissaPart)
                     {
                         mantissa = 10*mantissa + array[ii % size] - '0';
                     }
                     else
                     {
                         if (decimalPart)
                         {
                             result += (array[ii % size] - '0')*Math.pow(10, decimal);
                             decimal--;
                         }
                         else
                         {
                             result = 10*result + array[ii % size] - '0';
                         }
                     }
                     break;
                 case '.':
                     decimalPart = true;
                     break;
                 case 'E':
                     mantissaPart = true;
                     break;
                 case '-':
                     if (!mantissaPart)
                     {
                         throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to double");
                     }
                     mantissaSign = -1;
                     break;
                 case '+':
                     if (!mantissaPart)
                     {
                         throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to double");
                     }
                     break;
                 default:
                     throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to double");
             }
             if (result < 0)
             {
                 throw new IllegalArgumentException("cannot convert "+getString(s, l)+" to double");
             }
         }
         return (sign * result * Math.pow(10, mantissa*mantissaSign));
     }
 
     public boolean isAtBoundary(int t) throws IOException
     {
         BoundaryType type = BoundaryType.values()[t];
         switch (type)
         {
             case BOL:
                 return includeLevel.startOfLine();
             case EOL:
                 return ((includeLevel.startOfLine() || !isLineSeparator(peek(0))) && isLineSeparator(peek(1)));
             case WB:
                 return ((includeLevel.startOfLine() || !Character.isLetter(peek(0))) && Character.isLetter(peek(1)));
             case NWB:
                 return ((!includeLevel.startOfLine() && Character.isLetter(peek(0))) && !Character.isLetter(peek(1)));
             case BOI:
                 return end == 0;
             case EOPM:
                 throw new UnsupportedOperationException();
             case EOIL:
                 int cc = peek(1);
                 return isLineSeparator(cc) || cc == -1;
             case EOI:
                 return peek(1) == -1;
             default:
                 throw new IllegalArgumentException("unknown boundary "+type);
         }
     }
 
     private boolean isLineSeparator(int cc)
     {
         return cc == '\r' || cc == '\n';
     }
 
     public int getLineNumber()
     {
         return includeLevel.getLine();
     }
 
     public int getColumnNumber()
     {
         return includeLevel.getColumn();
     }
 
     public String getEncoding()
     {
         return includeLevel.getStreamReader().getCharset().name();
     }
 
     private class IncludeLevel
     {
         private PushbackReader in;
         private StreamReader streamReader;
         private int line = 1;
         private int column;
         private String source = "";
 
         public IncludeLevel()
         {
         }
 
         public IncludeLevel(PushbackReader in, String source)
         {
             this.in = in;
             this.source = source;
         }
 
         public IncludeLevel(PushbackReader in, StreamReader streamReader, String source)
         {
             this.in = in;
             this.streamReader = streamReader;
             this.source = source;
         }
 
         public void reset()
         {
             in = null;
             streamReader = null;
             line = 1;
             column = 0;
             source = "";
         }
         
         public void setIn(PushbackReader in)
         {
             this.in = in;
         }
 
         public void setStreamReader(StreamReader streamReader)
         {
             this.streamReader = streamReader;
         }
 
         public PushbackReader getIn()
         {
             return in;
         }
 
         public StreamReader getStreamReader()
         {
             return streamReader;
         }
 
         public String getSource()
         {
             return source;
         }
 
         public int getColumn()
         {
             return column;
         }
 
         public int getLine()
         {
             return line;
         }
         
         private boolean startOfLine()
         {
             return column == 0;
         }
 
         private void forward(int rc)
         {
             if (rc == '\n')
             {
                 line++;
                 column = 0;
             }
             else
             {
                 column++;
             }
         }
 
         public void setColumn(int column)
         {
             this.column = column;
         }
 
         public void setLine(int line)
         {
             this.line = line;
         }
 
         private void setSource(String source)
         {
             this.source = source;
         }
 
     }
     public static void main(String[] args)
     {
         try
         {
             InputReader input = new InputReader("abcdefg");
             input.read();
             for (int count=0;count < 1000;count++)
             {
                 input.read();
                 input.read();
                 input.read();
                 input.clear();
                 if (!"efg".equals(input.buffered()))
                 {
                     throw new IOException(input.buffered());
                 }
                 input.insert("1".toCharArray());
                 if (!"1efg".equals(input.buffered()))
                 {
                     throw new IOException(input.buffered());
                 }
                 input.insert("23".toCharArray());
                 if (!"231efg".equals(input.buffered()))
                 {
                     throw new IOException(input.buffered());
                 }
                 input.insert("".toCharArray());
                 if (!"231efg".equals(input.buffered()))
                 {
                     throw new IOException(input.buffered());
                 }
             }
         }
         catch (Exception ex)
         {
             ex.printStackTrace();
         }
     }
 
 }
