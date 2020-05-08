 
 /*
 * DataStream.java            $Revision: 1.4 $ $Date: 2001/05/16 05:05:32 $
  *
  * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
  *
  * The contents of this file are subject to the Blocks Public License (the
  * "License"); You may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.invisible.net/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  */
 package org.beepcore.beep.core;
 
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.lang.UnsupportedOperationException;
 
 import java.util.Hashtable;
 import java.util.Enumeration;
 
 
 /**
  * <code>DataStream</code> represents a BEEP message's payload as a stream.
  * <p>
  * <b>Note that this implementation
  * is not synchronized.</b> If multiple threads access a
  * <code>DataStream</code> concurrently, data may be inconsistent or lost.
  *
  * @see org.beepcore.beep.core.DataStream
  *
  *
  * @author Eric Dixon
  * @author Huston Franklin
  * @author Jay Kint
  * @author Scott Pead
 * @version $Revision: 1.4 $, $Date: 2001/05/16 05:05:32 $
  */
 public abstract class DataStream {
 
     static final String CONTENT_TYPE = "Content-Type";
     static final String CONTENT_TRANSFER_ENCODING =
         "Content-Transfer-Encoding";
     private static final String COLON_SPACE = ": ";
     private static final String HEADER_SUFFIX = "\r\n";
     private static final int LEN_PADDING = COLON_SPACE.length()
                                            + HEADER_SUFFIX.length();
 
     // guessing most hashtables will only contain the 2 default values
     private static final int DEFAULT_HEADER_TABLE_SIZE = 3;
 
     // length of data portion of data stream where aplicable
     private int lenHeaders = 0;    // length of header portion of data stream
     private int bytesRead = 0;
     private int headersBytesRead = 0;
     private byte[] headersBytes;
     private boolean headersCached = true;
     private boolean headersRead = false;
     private Hashtable mimeHeadersTable =
         new Hashtable(DEFAULT_HEADER_TABLE_SIZE);
 
     /**
      * The default <code>DataStream</code> content type
      * ("application/octet-stream").
      */
     public static final String DEFAULT_CONTENT_TYPE =
         "application/octet-stream";
 
     /**
      * The default <code>DataStream</code> content transfer encoding
      * ("binary").
      */
     public static final String DEFAULT_CONTENT_TRANSFER_ENCODING = "binary";
 
     /**
      * <code>DataStream</code> content type ("application/beep+xml");
      */
     public static final String BEEP_XML_CONTENT_TYPE = "application/beep+xml";
     private final int MAX_BUFFER_SIZE = 128;
     private InputStream stream = null;
 
     /**
      * Creates a <code>DataStream</code> using the default content type
      * <code>DEFAULT_CONTENT_TYPE</code> and default content transfre encoding
      * <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
      */
     protected DataStream()
     {
         this.mimeHeadersTable.put(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
         this.mimeHeadersTable.put(CONTENT_TRANSFER_ENCODING,
                                   DEFAULT_CONTENT_TRANSFER_ENCODING);
 
         // neither of these values are passed on as bytes since they are default
         this.lenHeaders = this.HEADER_SUFFIX.length();
         this.headersCached = false;
     }
 
     /**
      * Creates a <code>DataStream</code> using the specified content type and
      * the <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code> content transfer
      * encoding.
      *
      * @param contentType <code>DataStream</code> content type.
      */
     protected DataStream(String contentType)
     {
         this(contentType, DEFAULT_CONTENT_TRANSFER_ENCODING);
     }
 
     /**
      * Creates a <code>DataStream</code> using the specified content type and
      * content transfer encoding.
      *
      * @param contentType <code>DataStream</code> content type.
      * @param transferEncoding <code>DataStream</code> content transfer encoding.
      */
     protected DataStream(String contentType, String transferEncoding)
     {
         this.mimeHeadersTable.put(CONTENT_TYPE, contentType);
         this.mimeHeadersTable.put(CONTENT_TRANSFER_ENCODING,
                                   transferEncoding);
 
         this.lenHeaders = this.HEADER_SUFFIX.length();
 
         // check if these are default values
         if (!contentType.equals(DEFAULT_CONTENT_TYPE)) {
 
             // not default, add to len
             this.lenHeaders += contentType.length() + CONTENT_TYPE.length()
                                + this.LEN_PADDING;
         }
 
         if (!transferEncoding.equals(DEFAULT_CONTENT_TRANSFER_ENCODING)) {
 
             // not default, add to len
             this.lenHeaders += transferEncoding.length()
                                + CONTENT_TRANSFER_ENCODING.length()
                                + this.LEN_PADDING;
         }
 
         this.headersCached = false;
     }
 
     /**
      * Sets the content type of a <code>DataStream</code>.
      *
      * @param contentType
      */
     public void setContentType(String contentType)
     {
         if (this.mimeHeadersTable.containsKey(CONTENT_TYPE)) {
             removeHeader(CONTENT_TYPE);
         }
 
         this.mimeHeadersTable.put(CONTENT_TYPE, contentType);
 
         if (!contentType.equals(DEFAULT_CONTENT_TYPE)) {
             this.lenHeaders += CONTENT_TYPE.length() + contentType.length()
                                + this.LEN_PADDING;
         }
 
         this.headersCached = false;
     }
 
     /**
      * Returns the content type of the <code>DataStream</code>.
      *
      * @return Data stream's content type.
      *
      * @throws BEEPException
      */
     public String getContentType() throws BEEPException
     {
         return (String) this.mimeHeadersTable.get(CONTENT_TYPE);
     }
 
     /**
      * Sets the content transfer encoding of a <code>DataStream</code>
      *
      * @param transferEncoding
      */
     public void setTransferEncoding(String transferEncoding)
     {
         if (this.mimeHeadersTable.containsKey(CONTENT_TRANSFER_ENCODING)) {
             removeHeader(CONTENT_TRANSFER_ENCODING);
         }
 
         this.mimeHeadersTable.put(CONTENT_TRANSFER_ENCODING,
                                   transferEncoding);
 
         if (!transferEncoding.equals(DEFAULT_CONTENT_TRANSFER_ENCODING)) {
             this.lenHeaders += CONTENT_TRANSFER_ENCODING.length()
                                + transferEncoding.length() + this.LEN_PADDING;
         }
 
         this.headersCached = false;
     }
 
     /**
      * Returns the content transfer encoding of the <code>DataStream</code>.
      *
      * @return Content transfer encoding.
      * @exception BEEPException Thrown.
      */
     public String getTransferEncoding() throws BEEPException
     {
         return (String) this.mimeHeadersTable.get(CONTENT_TRANSFER_ENCODING);
     }
 
     /**
      * Adds a MIME entity header to this data stream.
      *
      * @param name  Name of the MIME enitity header.
      * @param value Value of the MIME entity header.
      */
     public void setHeader(String name, String value)
     {
         if (this.mimeHeadersTable.containsKey(name)) {
             removeHeader(name);
         }
 
         this.mimeHeadersTable.put(name, value);
 
         if ((name.equals(CONTENT_TYPE) && value.equals(DEFAULT_CONTENT_TYPE))
                 || (name.equals(CONTENT_TRANSFER_ENCODING)
                     && value.equals(DEFAULT_CONTENT_TRANSFER_ENCODING))) {
 
             // these are default values, don't add to len
         } else {
             this.lenHeaders += name.length() + value.length()
                                + this.LEN_PADDING;
         }
 
         this.headersCached = false;
     }
 
     /**
      * Retrieves the correspoding <code>value</code> to a given a MIME entity
      * header <code>name</code>.
      *
      * @param name Name of the MIME entity header.
      * @return The <code>value</code> of the MIME entity header.
      *
      * @throws BEEPException
      */
     public String getHeaderValue(String name) throws BEEPException
     {
         return (String) this.mimeHeadersTable.get(name);
     }
 
     /**
      * Returns an <code>Enumeration</code> of all the names of the MIME entity
      * headers in this data stream.
      * Use this call in conjunction with <code>getHeaderValue</code> to iterate
      * through all the corresponding MIME entity header <code>value</code>(s)
      * in this data stream.
      *
      * @return An <code>Enumeration</code> of all the MIME entity header
      * names.
      *
      * @throws BEEPException
      */
     public Enumeration getHeaderNames() throws BEEPException
     {
         return this.mimeHeadersTable.keys();
     }
 
     /**
      * Removes the <code>name</code> and <code>value</code> of a MIME entity
      * header from the data stream.  Returns <code>true</code> if the
      * <code>name</code> was successfully removed.
      *
      * @param name Name of the header to be removed from the data stream.
      *
      * @return Returns </code>true<code> if header was removed.  Otherwise,
      * returns <code>false</code>.
      */
     public boolean removeHeader(String name)
     {
         String value = (String) mimeHeadersTable.get(name);
 
         if (value != null) {
             if (this.mimeHeadersTable.remove(name) != null) {
                 if ((name.equals(CONTENT_TYPE) && value.equals(DEFAULT_CONTENT_TYPE))
                         || (name.equals(CONTENT_TRANSFER_ENCODING)
                             && value.equals(DEFAULT_CONTENT_TRANSFER_ENCODING))) {
 
                     // don't decrement the length, these are default values
                 } else {
                     this.lenHeaders -= name.length() + value.length()
                                        + this.LEN_PADDING;
                 }
 
                 this.headersCached = false;
 
                 return true;
             }
         }
 
         return false;
     }
 
     abstract int available() throws BEEPException;
 
     abstract int read() throws BEEPException;
 
     abstract int read(byte buf[]) throws BEEPException;
 
     abstract int read(byte buf[], int off, int len) throws BEEPException;
 
     abstract long skip(long n) throws BEEPException;
 
     abstract void close() throws BEEPException;
 
     /**
      *  Returns the number of bytes that can be read (or skipped over) from
      *  the MIME entity headers and the data portion of the
      *  data stream without blocking by the next caller of a method for this
      *  input stream.
      *
      *  @return Number of bytes available.
      *
      * @throws BEEPException
      */
     protected int availableHeadersAndData() throws BEEPException
     {
         return this.lenHeaders + available() - this.headersBytesRead;
     }
 
     /**
      * Reads up to <code>len</code> bytes from the data stream's MIME entity
      * headers and data starting at <code>off</code>. Reads as if they were one
      * stream and stores them into the buffer array buf.
      * Note that this implementation is not synchronized.
      *
      * @param buf The buffer into which the data is read.
      * @param off The start offset of the data.
      * @param len The maximum number of bytes read.
      * @return The number of bytes read.  Returns -1 if no more can be
      *    read.
      *
      * @throws BEEPException
      */
     protected int readHeadersAndData(byte buf[], int off, int len)
             throws BEEPException
     {
         int headerBytes = 0;
         int dataBytes = 0;
 
         if (!this.headersRead) {
             headerBytes = readHeaders(buf, off, len);
 
             if (this.headersBytesRead == this.lenHeaders) {
                 this.headersRead = true;    // read all bytes, continue reading
             } else {
                 return headerBytes;    // there are more bytes to read later
             }
         }
 
         // read the data portion
         if (headerBytes < len) {
             dataBytes = read(buf, off + headerBytes, len - headerBytes);
 
             if (dataBytes == -1) {
                 return -1;
             }
         }
 
         return dataBytes + headerBytes;
     }
 
     private int readHeaders(byte[] buf, int off, int len)
     {
         if (!this.headersCached) {
             this.headersBytes = new byte[this.lenHeaders];
 
             int offsetHeaders = 0;
 
             // read the headers
             Enumeration headers = mimeHeadersTable.keys();
 
             while (headers.hasMoreElements()) {
                 String name = (String) headers.nextElement();
                 String value = (String) mimeHeadersTable.get(name);
 
                 if ((name.equals(CONTENT_TYPE) && value.equals(DEFAULT_CONTENT_TYPE))
                         || (name.equals(CONTENT_TRANSFER_ENCODING)
                             && value.equals(DEFAULT_CONTENT_TRANSFER_ENCODING))) {
 
                     // these are default values that don't need to be added
                     // to the payload
                     continue;
                 }
 
                 // copy name
                 System.arraycopy(name.getBytes(), 0, this.headersBytes,
                                  offsetHeaders, name.length());
 
                 offsetHeaders += name.length();
 
                 // ": "
                 System.arraycopy(COLON_SPACE.getBytes(), 0,
                                  this.headersBytes, offsetHeaders,
                                  COLON_SPACE.length());
 
                 offsetHeaders += COLON_SPACE.length();
 
                 // copy value
                 System.arraycopy(value.getBytes(), 0, this.headersBytes,
                                  offsetHeaders, value.length());
 
                 offsetHeaders += value.length();
 
                 // CRLF
                 System.arraycopy(this.HEADER_SUFFIX.getBytes(), 0,
                                  this.headersBytes, offsetHeaders,
                                  this.HEADER_SUFFIX.length());
 
                 offsetHeaders += this.HEADER_SUFFIX.length();
             }
 
             // read the CRLF that separates the headers and the data
             System.arraycopy(this.HEADER_SUFFIX.getBytes(), 0,
                              this.headersBytes, offsetHeaders,
                              this.HEADER_SUFFIX.length());
 
             offsetHeaders += this.HEADER_SUFFIX.length();
             this.headersCached = true;
         }
 
        len = Math.min(len, this.headersBytes.length - this.headersBytesRead);
        System.arraycopy(this.headersBytes, this.headersBytesRead, buf,
                         off, len);
 
        this.headersBytesRead += len;
        this.bytesRead += len;
 
        return len;
     }
 
     /**
      * Returns <code>true</code> if no more bytes will be added to those
      * currently available, if any, on this stream.  Returns
      * <code>false</code> if more bytes are expected.
      */
     abstract public boolean isComplete();
 
     /**
      * Returns this data stream as an <code>InputStream</code>.
      */
     public InputStream getInputStream()
     {
         if (stream == null) {
             stream = new BufferedInputStream(new InnerInputStream(),
                                              MAX_BUFFER_SIZE);
         }
 
         return stream;
     }
 
     private class InnerInputStream extends InputStream {
 
         public int available() throws IOException
         {
             try {
                 return DataStream.this.available();
             } catch (BEEPException e) {
                 throw new IOException(e.getMessage());
             }
         }
 
         public int read() throws IOException
         {
             try {
                 return DataStream.this.read();
             } catch (BEEPException e) {
                 throw new IOException(e.getMessage());
             }
         }
 
         public int read(byte[] dest) throws IOException
         {
             try {
                 return DataStream.this.read(dest);
             } catch (BEEPException e) {
                 throw new IOException(e.getMessage());
             }
         }
 
         public int read(byte[] dest, int destOffset, int destLen)
                 throws IOException
         {
             try {
                 return DataStream.this.read(dest, destOffset, destLen);
             } catch (BEEPException e) {
                 throw new IOException(e.getMessage());
             }
         }
 
         public void close() throws IOException
         {
             try {
                 DataStream.this.close();
             } catch (BEEPException e) {
                 throw new IOException(e.getMessage());
             }
         }
 
         public long skip(long n) throws IOException
         {
             try {
                 return DataStream.this.skip(n);
             } catch (BEEPException e) {
                 throw new IOException(e.getMessage());
             }
         }
     }
 }
