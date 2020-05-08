 
 /*
 * InputStreamDataStream.java            $Revision: 1.3 $ $Date: 2001/04/24 22:52:02 $
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
 
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.FileNotFoundException;
 
 /**
  * <code>FileDataStream</code> represents a BEEP message's payload.
  * Allows the implementor to treat a <code>File</code> or
  * <code>FileDescriptor</code> as a <code>DataSream</code>.
  * <p>
  * <b>Note that this implementation
  * is not synchronized.</b> If multiple threads access a
  * <code>FileDataStream</code> concurrently, data may be inconsistent or lost.
  *
  * @see org.beepcore.beep.core.DataStream
  *
  * @author Eric Dixon
  * @author Huston Franklin
  * @author Jay Kint
  * @author Scott Pead
 * @version $Revision, $Date: 2001/04/24 22:52:02 $
  */
 public class InputStreamDataStream extends DataStream {
 
     protected InputStream data = null;
     private boolean complete = false;
     private long length = -1;
     private long bytesRead = 0;
     private long markBytesRead = 0;
 
     /**
      * Creates a <code>InputStreamDataStream</code> with a content type of
      * <code>DEFAULT_CONTENT_TYPE</code> and a transfer encoding
      * of <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
      * Once <code>length</code> bytes are read from the stream,
      * <code>isComplete</code> will return <code>true</code>.
      * Subsequent reads from the stream will return -1.
      *
      * @param data  the stream to be opened for reading.
      * @param length the length of the stream.
     *
     * @see isComplete
      */
     public InputStreamDataStream(InputStream data, long length)
     {
         super();
 
         this.data = data;
         if( length <= 0 ) {
             throw new IllegalArgumentException("Length must be greater than zero");
         }
         this.length = length;
     }
 
     /**
      * Creates a <code>InputStreamDataStream</code> with a specified content
      * type and a transfer encoding
      * <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
      * Once <code>length</code> bytes are read from the stream,
      * <code>isComplete</code> will return <code>true</code>.
      * Subsequent reads from the stream will return -1.
      *
      * @param contentType Content type of <code>stream</code>.
      * @param data  the stream to be opened for reading.
      * @param length the length of the stream.
     *
     * @see isComplete
      */
     public InputStreamDataStream(String contentType, InputStream data,
                                  long length)
     {
         super(contentType, DataStream.DEFAULT_CONTENT_TRANSFER_ENCODING);
 
         this.data = data;
         if( length <= 0 ) {
             throw new IllegalArgumentException("Length must be greater than zero");
         }
         this.length = length;
     }
 
     /**
      * Creates a <code>InputStreamDataStream</code> with a specified content
      * type and a transfer encoding
      * <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
      * Once <code>length</code> bytes are read from the stream,
      * <code>isComplete</code> will return <code>true</code>.
      * Subsequent reads from the stream will return -1.
      *
      * @param contentType Content type of <code>stream</code>.
      * @param transferEncoding Encoding Transfer encoding type of
      * <code>stream</code>.
      * @param data  the stream to be opened for reading.
      * @param length the length of the stream.
     *
     * @see isComplete
      */
     public InputStreamDataStream(String contentType, String transferEncoding,
                                  InputStream data, long length)
     {
         super(contentType, transferEncoding);
 
         this.data = data;
         if( length <= 0 ) {
             throw new IllegalArgumentException("Length must be greater than zero");
         }
         this.length = length;
     }
 
 
     /**
      * Creates a <code>InputStreamDataStream</code> with a content type of
      * <code>DEFAULT_CONTENT_TYPE</code> and a transfer encoding
      * of <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
      * Depending on the particular type of the input stream and how it was
      * created, <code>isComplete</code> may never return <code>true</code>.
      * If this constructor is used in a sub-class, you must implement the
      * <code>isComplete</code> method.
      *
      * @param data  the stream to be opened for reading.
      */
     protected InputStreamDataStream(InputStream data)
     {
         super();
 
         this.data = data;
     }
 
     /**
      * Creates a <code>InputStreamDataStream</code> with a specified content
      * type and a transfer encoding
      * <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
      * Depending on the particular type of the input stream and how it was
      * created, <code>isComplete</code> may never return <code>true</code>.
      * If this constructor is used in a sub-class, you must implement the
      * <code>isComplete</code> method.
      *
      * @param contentType Content type of <code>stream</code>.
      * @param data  the stream to be opened for reading.
      */
     protected InputStreamDataStream(String contentType, InputStream data)
     {
         super(contentType, DataStream.DEFAULT_CONTENT_TRANSFER_ENCODING);
 
         this.data = data;
     }
 
     /**
      * Creates a <code>InputStreamDataStream</code> with a specified content
      * type and a transfer encoding <code>DEFAULT_CONTENT_TRANSFER_ENCODING</code>.
      * Depending on the particular type of the input stream and how it was
      * created, <code>isComplete</code> may never return <code>true</code>.
      * If this constructor is used in a sub-class, you must implement the
      * <code>isComplete</code> method.
      *
      * @param contentType Content type of <code>stream</code>.
      * @param transferEncoding Encoding Transfer encoding type of
      * <code>stream</code>.
      * @param data  the stream to be opened for reading.
      */
     protected InputStreamDataStream(String contentType, String transferEncoding,
                                  InputStream data)
     {
         super(contentType, transferEncoding);
 
         this.data = data;
     }
 
 
     /**
      * Returns this data stream as an <code>InputStream</code>
      */
     public InputStream getInputStream()
     {
         return this.data;
     }
 
     /**
      * An <code>InputStreamDataStream</code> will always return false.
      * To detect the end of stream look for <code>read</code> to return a -1.
      *
      * @return false
      */
     public boolean isComplete()
     {
         return this.complete;
     }
 
     /**
      * Returns the number of bytes that can be read (or skipped over) from this
      * data stream without blocking by the next caller of a method for this
      * input stream.  See <code>isComplete</code> to detect end of stream
      * without blocking.
      *
      * @return Number of bytes available.
      *
      * @throws BEEPException
      */
     int available() throws BEEPException
     {
         try {
             return data.available();
         } catch (IOException e) {
             throw new BEEPException(e.getMessage());
         }
     }
 
     /**
      * Reads a byte of data from this data stream.
      * This method blocks if no input is yet available..
      * Note that this implementation is not synchronized.
      *
      * @return The next byte.
      *
      * @exception BEEPException Throws <code>BEEPException</code>,
      * if an I/O error occurs.
      */
     int read() throws BEEPException
     {
         try {
             if( this.length != -1 && this.bytesRead >= this.length )
             {
               return -1;
             }
             int b = this.data.read();
             this.bytesRead++;
             if( this.length != -1 && this.bytesRead >= this.length )
             {
               this.complete = true;
             }
             return b;
         } catch (IOException e) {
             throw new BEEPException(e.getMessage());
         }
     }
 
     /**
      * Reads some number of bytes from the input stream and stores them into
      * the buffer array <code>buf</code>.
      * This method blocks until some input is available.
      * Note that this implementation is not synchronized.
      *
      * @param buf The buffer into which the data is read.
      * @return The total number of bytes read into the buffer, or -1 if
      *    there is no more data because the end of the file has been reached.
      *
      * @exception BEEPException Throws <code>BEEPException</code>,
      * if an I/O error occurs.
      */
     int read(byte[] buf) throws BEEPException
     {
         return read( buf, 0, buf.length );
     }
 
     /**
      * Reads up to len bytes of data from the input stream into an array of
      * bytes. This method blocks until some input is available.
      * Note that this implementation is not synchronized.
      *
      * @param buf The buffer into which the data is read.
      * @param off The start offset of the data.
      * @param len The maximum number of bytes read.
      * @return The number of bytes read.  Returns -1 if no more can be
      *    read.
      * @exception BEEPException Throws <code>BEEPException</code>,
      * if an I/O error occurs.
      */
     int read(byte[] buf, int off, int len) throws BEEPException
     {
         try {
             if( this.length != -1 && this.bytesRead >= this.length )
             {
               return -1;
             }
             int count = this.data.read( buf, off, len );
             if( count != -1 )
             {
               this.bytesRead += count;
               if( this.length != -1 && this.bytesRead >= this.length )
               {
                 this.complete = true;
               }
             }
             return count;
         } catch (IOException e) {
             throw new BEEPException(e.getMessage());
         }
     }
 
     /**
      * Skips over and discards n bytes of data from this input stream.
      *
      * @param n Number of bytes to skip.
      * @return Number of bytes actually skipped.
      *
      * @exception BEEPException Throws <code>BEEPException</code>,
      * if an I/O error occurs.
      */
     long skip(long n) throws BEEPException
     {
         try {
             if(this.length != -1 && (n + this.bytesRead > this.length)) {
                 return 0;
             }
             this.bytesRead += n;
             return this.data.skip(n);
         } catch (IOException e) {
             throw new BEEPException(e.getMessage());
         }
     }
 
     void reset() throws BEEPException
     {
         try {
             this.data.reset();
         } catch (IOException e) {
             throw new BEEPException(e.getMessage());
         }
         this.bytesRead = this.markBytesRead;
     }
 
     boolean markSupported()
     {
         return this.data.markSupported();
     }
 
     void mark(int readlimit)
     {
         this.markBytesRead = this.bytesRead;
         this.data.mark(readlimit);
     }
 
     void close() throws BEEPException
     {
         try {
             this.data.close();
         } catch (IOException e) {
             throw new BEEPException(e.getMessage());
         }
     }
 }
