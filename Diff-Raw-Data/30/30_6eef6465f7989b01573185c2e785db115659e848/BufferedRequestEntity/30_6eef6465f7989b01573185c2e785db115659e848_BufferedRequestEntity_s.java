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
 package org.jets3t.service.impl.rest.httpclient;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jets3t.service.io.UnrecoverableIOException;
 
 /**
  * A request entity that is repeatable provided only a small amount of data has been transmitted. 
  * This class buffers a small amount of data, and if the request fails having transmitted less than
  * this buffer holds the request can be repeated.
  * 
  * @author James Murty
  */
 public class BufferedRequestEntity implements RequestEntity {
     private final Log log = LogFactory.getLog(BufferedRequestEntity.class);
 
    private int bufferSize = 16384;    
     private InputStream is = null;
     private String contentType = null;
     private long contentLength = 0;
     
     private int bytesWritten = 0;
    private byte[] buffer = new byte[bufferSize];
 
     /**
      * Constructs a buffered entity with a given buffer size.
      * 
      * @param bufferSize
      * @param is
      * @param contentType
      * @param contentLength
      */
     public BufferedRequestEntity(int bufferSize, InputStream is, String contentType, long contentLength) {
         if (is == null) {
             throw new IllegalArgumentException("InputStream cannot be null");
         }
         this.bufferSize = bufferSize;
         this.is = is;
         this.contentLength = contentLength;
         this.contentType = contentType;
     }
 
     /**
     * Constructs a buffered entity with the default buffer size of 16384 bytes.
      * 
      * @param is
      * @param contentType
      * @param contentLength
      */
     public BufferedRequestEntity(InputStream is, String contentType, long contentLength) {
        this(16384, is, contentType, contentLength);
     }
     
     public long getContentLength() {
       return contentLength;
     }
 
     public String getContentType() {
         return contentType;
     }
 
     /**
      * @return
      * true if fewer bytes have been written than are available in the buffer, and are therefore
      * available for replay. False otherwise.
      */
     public boolean isRepeatable() {
        log.info("Testing whether buffered request entity is repeatable. Bytes written: " // TODO 
            + bytesWritten + ", Buffer size: " + bufferSize + ", Is repeatable: " 
            + (bytesWritten < bufferSize));
        return (bytesWritten < bufferSize);
     }
 
     /**
      * Writes data from the wrapped input stream to the output, buffering a small amount of data
      * as it goes. If this method is repeated (ie is called when the buffer is not empty) the
      * buffer is written to the output first, then the remaining data is passed from the input stream
      * to the output.
      */
     public void writeRequest(OutputStream out) throws IOException {
         if (bytesWritten > 0) {
            log.info("Repeating buffered data of size: " + bytesWritten); // TODO
             out.write(buffer, 0, bytesWritten);
         }
         
         byte[] tmp = new byte[8192];
         int count = 0;
 
         while ((count = is.read(tmp)) >= 0) {
             if (bytesWritten + count < bufferSize) {
                 System.arraycopy(tmp, 0, buffer, bytesWritten, count);
             }
             bytesWritten += count;
             
             out.write(tmp, 0, count);
         }
         
         if (bytesWritten < contentLength) {
             throw new UnrecoverableIOException(
                 "Data underflow error as too little data was available. Expected " 
                 + contentLength + " bytes, but only " + bytesWritten + " bytes were available");
         }
         
     }
 
 }
