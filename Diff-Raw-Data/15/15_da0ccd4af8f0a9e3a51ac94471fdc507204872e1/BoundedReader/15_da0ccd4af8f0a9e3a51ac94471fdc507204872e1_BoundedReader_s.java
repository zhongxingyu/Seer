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
 import java.io.Reader;
 import java.nio.CharBuffer;
 
 /**
  * <P>
  * A <code>BoundedReader</code> allows to specify maximum of characters to be read from wrapped reader.
  * If the specified maximum of characters is read, the reader is in EOF condition.
  * </P>
  * <p>
  * This class is not thread safe. 
  * </p>
  * 
  * @author <a href="mailto:opalka dot richard at gmail dot com">Richard Opalka</a>
  */
 public final class BoundedReader extends DelegatingReader {
 
     private boolean closed;
 
     private long position;
 
     private long limit;
 
     private long mark;
 
     /**
      * Creates a <code>BoundedReader</code>.
      *
      * @param delegate reader to be shortened
      * @param limit reader bound
      */
     public BoundedReader( final Reader delegate, final long limit ) {
         // ensure preconditions
         super( delegate );
         if ( limit <= 0 ) {
             throw new IllegalArgumentException( "Limit must be positive" );
         }
         // initialize
         this.limit = limit;
     }
 
     /**
      * See {@link java.io.Reader#read()} javadoc.
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
      * See {@link java.io.Reader#read(char[])} javadoc.
      */
     @Override
     public int read( final char[] buffer ) throws IOException {
         // ensure preconditions
         ensureOpen();
         if ( buffer == null ) {
             throw new IllegalArgumentException( "buffer cannot be null" );
         }
         // the implementation
         return read( buffer, 0, buffer.length );
     }
 
     /**
      * See {@link java.io.Reader#read(char[], int, int)} javadoc.
      */
     @Override
     public int read( final char[] buffer, final int offset, final int length ) throws IOException {
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
         final int readCharsCount = super.read( buffer, offset, len );
         if ( readCharsCount > 0 ) {
             position += readCharsCount;
         }
         return readCharsCount;
     }
 
     /**
      * See {@link java.io.Reader#skip(long)} javadoc.
      */
     @Override
    public long skip( long count ) throws IOException {
         // ensure preconditions
         ensureOpen();
         // method implementation
         if ( count <= 0 ) {
             return 0;
         }
         final long skippedCharsCount = super.skip( Math.min( count, remaining() ) );
        count += skippedCharsCount;
         return skippedCharsCount;
     }
 
     /**
      * See {@link java.io.Reader#read(CharBuffer)} javadoc.
      */
     @Override
     public int read( final CharBuffer buffer ) throws IOException {
         // ensure preconditions
         ensureOpen();
         // method implementation
         final int readCharsCount = super.read( buffer );
         if ( readCharsCount > 0 ) {
             position += readCharsCount;
         }
         return readCharsCount;
     }
 
     /**
      * See {@link java.io.Reader#ready()} javadoc.
      */
     @Override
     public boolean ready() throws IOException {
         // ensure preconditions
         ensureOpen();
         // method implementation
         return super.ready();
     }
 
     /**
      * See {@link java.io.Reader#mark(int)} javadoc.
      */
     @Override
     public void mark( final int readLimit ) throws IOException {
         // ensure preconditions
         ensureOpen();
         // method implementation
         super.mark( readLimit );
         mark = position;
     }
 
     /**
      * See {@link java.io.Reader#reset()} javadoc.
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
      * See {@link java.io.Reader#markSupported()} javadoc.
      */
     @Override
     public boolean markSupported() {
         // ensure preconditions
         ensureOpen();
         // method implementation
         return super.markSupported();
     }
 
     /**
      * See {@link java.io.Reader#close()} javadoc.
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
             throw new IllegalStateException( "Reader is closed" );
         }
     }
 
     private long remaining() {
         return limit - position;
     }
 }
