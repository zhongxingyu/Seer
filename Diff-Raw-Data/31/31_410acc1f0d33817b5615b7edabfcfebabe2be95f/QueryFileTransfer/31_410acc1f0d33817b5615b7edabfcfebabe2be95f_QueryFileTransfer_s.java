 /*
  * Copyright 2011 Gregory P. Moyer
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
 package org.syphr.mythtv.protocol;
 
 import java.io.IOException;
 
 import org.syphr.mythtv.types.SeekOrigin;
 
 /**
  * This interface is a sub-protocol to {@link Protocol} and represents the
  * combined file transfer API of all MythTV protocols that are supported.
  * However, any functionality that is not part of the protocol present in the
  * most current stable release of MythTV will be marked as deprecated.
  *
  * @author Gregory P. Moyer
  */
 public interface QueryFileTransfer
 {
     /**
      * Determine whether or not the associated file transfer socket is open.
      *
      * @return <code>true</code> if the socket is open; <code>false</code>
      *         otherwise
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public boolean isOpen() throws IOException;
 
     /**
     * Inform the backend that the client has finished with this file transfer.
      *
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public void done() throws IOException;
 
     /**
      * Request data be sent over this file transfer socket.
      *
      * @param bytes
      *            the number of bytes requested by the client
      * @return the number of bytes actually being transferred by the server
      *         (this does not have to match the request) or <code>-1</code> if
      *         there was an error
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public long requestBlock(long bytes) throws IOException;
 
     /**
      * Notify the backend that data will be sent over this file transfer socket.
      *
      * @param bytes
      *            the number of bytes to be transferred from the client
      * @return the number of bytes actually being transferred by the server
      *         (this does not have to match the request) or <code>-1</code> if
      *         there was an error
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public long writeBlock(long bytes) throws IOException;
 
     /**
      * Request that the backend seek to another location in the file being
      * transferred.
      *
      * @param position
      *            the desired position relative to the given origin
      * @param origin
      *            the base location from which to seek (the position offset will
      *            be applied to this location)
      * @param curPosition
      *            the current position (this is used with
      *            {@link SeekOrigin#CURRENT} because the backend has likely read
      *            beyond to some unknown point in the file, but it will use this
      *            value as the basis for determining the seek destination)
      * @return the new location as an offset from the beginning of the file or
      *         <code>-1</code> if the request failed
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public long seek(long position, SeekOrigin origin, long curPosition) throws IOException;
 
     /**
      * Set the timeout to fast or slow. Fast timeouts are used for static files
      * and slow timeouts are used for live data streams.
      *
      * @param fast
      *            if <code>true</code>, the timeout will be set to fast;
      *            otherwise the timeout will be set to slow
      * @throws IOException
      *             if there is a communication or protocol error
      *
      * @since 63
      */
     public void setTimeout(boolean fast) throws IOException;
 
     /**
      * Retrieve the size of the file being transferred.
      *
      * @return the file size
      */
     public long getSize();
 }
