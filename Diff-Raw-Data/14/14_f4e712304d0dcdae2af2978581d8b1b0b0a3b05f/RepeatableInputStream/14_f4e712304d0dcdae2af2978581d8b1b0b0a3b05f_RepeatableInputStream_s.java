 /*
  * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
  * This is a java.net project, see https://jets3t.dev.java.net/
  * 
  * Copyright 2006 James Murty
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package org.jets3t.service.io;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jets3t.service.Constants;
 import org.jets3t.service.Jets3tProperties;
 
 /**
  * A repeatable input stream wrapper for any input stream. This input stream relies on buffered
  * data to repeat, and can therefore only be repeated when less data has been read than this
  * buffer can hold.
  * <p>
  * <b>Note:</b> Always use a {@link RepeatableFileInputStream} instead of this class if you are
  * sourcing data from a file, as the file-based repeatable input stream can be repeated without
  * any limitations. 
  * 
  * <p>
  * This class uses properties obtained through {@link Jets3tProperties}. For more information on 
  * these properties please refer to 
  * <a href="http://jets3t.s3.amazonaws.com/toolkit/configuration.html">JetS3t Configuration</a>
  * </p>
  * 
  * @author James Murty
  */
 public class RepeatableInputStream extends InputStream implements IRepeatableInputStream, InputStreamWrapper {
     private final Log log = LogFactory.getLog(RepeatableInputStream.class);
 
     private InputStream is = null;
     private int bufferOffset = 0;
     private int bufferSize = 0;    
    private int bytesReadTotal = 0;
     private byte[] buffer = null;
     
     /**
      * Creates a repeatable input stream based on another input stream.
      * 
      * @param inputStream
      * an input stream to wrap. The data read from the wrapped input stream is buffered as it is
      * read, up to the buffer limit specified.
      * @param bufferSize
      * the number of bytes buffered by this class. 
      * 
      * @throws FileNotFoundException
      */
     public RepeatableInputStream(InputStream inputStream, int bufferSize) {
         if (inputStream == null) {
             throw new IllegalArgumentException("InputStream cannot be null");
         }
         this.is = inputStream;        
 
         this.bufferSize = bufferSize;
         this.buffer = new byte[this.bufferSize];            
         
         log.debug("Underlying input stream will be repeatable up to " + this.buffer.length + " bytes");            
     }
 
     /**
      * Creates a repeatable input stream based on another input stream.
      * 
      * @param inputStream
      * an input stream to wrap. The data read from the wrapped input stream is buffered as it is
      * read, up to the buffer limit as set by the JetS3t property 
      * <tt>s3service.stream-retry-buffer-size</tt>.
      * 
      * @throws FileNotFoundException
      */
     public RepeatableInputStream(InputStream inputStream) {
         this(inputStream, Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME)
             .getIntProperty("s3service.stream-retry-buffer-size", 131072));
     }
 
     /**
      * Resets the input stream to the beginning by pointing the buffer offset to the beginning of the
      * available data buffer. 
      * 
      * @throws UnrecoverableIOException
      * when the available buffer size has been exceeded, in which case the input stream data cannot
      * be repeated.
      */
     public void repeatInputStream() throws IOException {
         if (bytesReadTotal <= bufferSize) {
             log.debug("Reset after reading " + bytesReadTotal + " bytes.");            
             bufferOffset = 0;
         } else {
             throw new UnrecoverableIOException(
                 "Input stream is not repeatable as " + this.bytesReadTotal 
                 + " bytes have been written, exceeding the available buffer size of " + this.bufferSize);
         }
     }
 
     public int available() throws IOException {
         return is.available();
     }
 
     public void close() throws IOException {
         is.close();
     }
 
     public int read(byte[] out, int outOffset, int outLength) throws IOException {
         byte[] tmp = new byte[outLength];
 
         // Check whether we already have buffered data.
         if (bufferOffset < bytesReadTotal && bufferOffset < bufferSize) {
             // Data is being repeated, read from buffer instead of wrapped input stream.            
             // Write the buffered data in chunks so the progress monitor is only updated a
             // little as a time as the output stream actually pushes through data.
             int bytesFromBuffer = tmp.length;
             if (bufferOffset + bytesFromBuffer > bytesReadTotal) {
                bytesFromBuffer = bytesReadTotal - bufferOffset;
             }
 
             // Write to output.
             System.arraycopy(buffer, bufferOffset, out, outOffset, bytesFromBuffer);
             bufferOffset += bytesFromBuffer;  
             return bytesFromBuffer;
         }
         
         // Read data from input stream.
         int count = is.read(tmp);
         
         if (count <= 0) {
             return count;
         }
         
         // Fill the buffer with data until it is full.
        int length = (bytesReadTotal + count < bufferSize
             ? count
             : bufferSize - bytesReadTotal);
         if (length > 0) {
            System.arraycopy(tmp, 0, buffer, bytesReadTotal, length);
             bufferOffset += length;
         } else if (length < 0 && buffer != null) {
             // We have exceeded the buffer size, after which point it is of no use. Free the memory.
             log.debug("Buffer size " + bufferSize + 
                " has been exceed and input stream is no longer repeatable, freeing buffer memory");
             buffer = null;
         }
         
         // Write to output.
         System.arraycopy(tmp, 0, out, outOffset, count);
         bytesReadTotal += count;
 
         return count;
     }
 
     public int read() throws IOException {
         byte[] tmp = new byte[1];
         int count = read(tmp);
         if (count != -1) {
             return tmp[0];
         } else {
             return count;
         }
     }
 
     public InputStream getWrappedInputStream() {
         return is;
     }
 
 }
