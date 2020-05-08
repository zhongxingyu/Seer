 /*
  * Copyright (c) 2012, FOSS Nova Software foundation (FNSF),
  * and individual contributors as indicated by the @author tags.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.fossnova.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * <P>
  * A <code>BoundedInputStream</code> allows to specify maximum of bytes to be read from wrapped stream.
  * If the specified maximum of bytes is read, the stream is in EOF condition.
  * </P>
  * <p>
  * This class is not thread safe. 
  * </p>
  * 
  * @author <a href="mailto:opalka dot richard at gmail dot com">Richard Opalka</a>
  */
 public final class BoundedInputStream extends DelegatingInputStream {
 
     private boolean closed;
 
     private long position;
 
     private long limit;
 
     private long mark;
 
     /**
      * Creates a <code>BoundedInputStream</code>.
      *
      * @param delegate input stream to be shortened
      * @param limit input stream bound
      */
     public BoundedInputStream( final InputStream delegate, final long limit ) {
         // ensure preconditions
         super( delegate );
         if ( limit <= 0 ) {
             throw new IllegalArgumentException( "Limit must be positive" );
         }
         // initialize
         this.limit = limit;
     }
 
     /**
      * See {@link java.io.InputStream#read()} javadoc.
      */
     @Override
     public int read() throws IOException {
         // ensure preconditions
         ensureOpen();
         // method implementation
         if ( remaining() == 0 ) {
             return -1;
         }
         final int retVal = super.read();
         if ( retVal != -1 ) {
             position++;
         }
         return retVal;
     }
 
     /**
      * See {@link java.io.InputStream#read(byte[])} javadoc.
      */
     @Override
     public int read( final byte[] buffer ) throws IOException {
         // ensure preconditions
         ensureOpen();
         if ( buffer == null ) {
             throw new IllegalArgumentException( "buffer cannot be null" );
         }
         // the implementation
         return read( buffer, 0, buffer.length );
     }
 
     /**
      * See {@link java.io.InputStream#read(byte[], int, int)} javadoc.
      */
     @Override
     public int read( final byte[] buffer, final int offset, final int length ) throws IOException {
         // ensure preconditions
         ensureOpen();
         if ( buffer == null ) {
             throw new IllegalArgumentException( "buffer cannot be null" );
         }
         if ( offset < 0 ) {
             throw new IllegalArgumentException( "offset must be positive" );
         }
         if ( length < 0 ) {
             throw new IllegalArgumentException( "length must be positive" );
         }
         if ( length > ( buffer.length - offset ) ) {
             throw new IllegalArgumentException( "length must be less or equal to free space available in the buffer" );
         }
         // method implementation
         if ( length == 0 ) {
             return 0;
         }
         final int len = remaining() > length ? length : ( int ) remaining();
         final int readBytesCount = super.read( buffer, offset, len );
         if ( readBytesCount > 0 ) {
             position += readBytesCount;
         }
         return readBytesCount;
     }
 
     /**
      * See {@link java.io.InputStream#skip(long)} javadoc.
      */
     @Override
    public long skip( long count ) throws IOException {
         // ensure preconditions
         ensureOpen();
         // method implementation
         if ( count <= 0 ) {
             return 0;
         }
         final long skippedBytesCount = super.skip( Math.min( count, remaining() ) );
        count += skippedBytesCount;
         return skippedBytesCount;
     }
 
     /**
      * See {@link java.io.InputStream#available()} javadoc.
      */
     @Override
     public int available() throws IOException {
         // ensure preconditions
         ensureOpen();
         // method implementation
         final int available = super.available();
         final long remaining = remaining();
         return remaining > available ? available : ( int ) remaining;
     }
 
     /**
      * See {@link java.io.InputStream#mark(int)} javadoc.
      */
     @Override
     public void mark( final int readLimit ) {
         // ensure preconditions
         ensureOpen();
         // method implementation
         super.mark( readLimit );
         mark = position;
     }
 
     /**
      * See {@link java.io.InputStream#reset()} javadoc.
      */
     @Override
     public void reset() throws IOException {
         // ensure preconditions
         ensureOpen();
         // method implementation
         super.reset();
         position = mark;
     }
 
     /**
      * See {@link java.io.InputStream#markSupported()} javadoc.
      */
     @Override
     public boolean markSupported() {
         // ensure preconditions
         ensureOpen();
         // method implementation
         return super.markSupported();
     }
 
     /**
      * See {@link java.io.InputStream#close()} javadoc.
      */
     @Override
     public void close() throws IOException {
         if ( !closed ) {
             closed = true;
             super.close();
         }
     }
 
     private void ensureOpen() {
         if ( closed ) {
             throw new IllegalStateException( "Stream is closed" );
         }
     }
 
     private long remaining() {
         return limit - position;
     }
 }
