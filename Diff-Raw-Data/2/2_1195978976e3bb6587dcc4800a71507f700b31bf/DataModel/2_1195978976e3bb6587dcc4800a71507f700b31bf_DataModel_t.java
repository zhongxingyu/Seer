 /*
  * Copyright 2013 Gerrit Meinders
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
 package hexedit;
 
 import java.io.*;
 import java.net.*;
 import java.nio.*;
 import java.nio.channels.*;
 
 /**
  * Provides access to data from a {@link FileChannel}. The entire file can be
  * accessed, but only a very small piece of the file is kept in memory at any
  * given time.
  *
  * @author Gerrit Meinders
  */
 public class DataModel
 {
 	private final FileChannel _channel;
 
 	private long _offset = 0L;
 
 	private byte[] _bytes;
 
 	private int _windowSize;
 
 	private URI _dataSource;
 
 	private ByteBuffer _buffer;
 
 	/**
 	 * Constructs a new instance.
 	 *
 	 * @param dataSource URI of the data source.
 	 * @param channel    Channel that provides access to the data.
 	 */
 	public DataModel( final URI dataSource, final FileChannel channel )
 	{
 		_channel = channel;
 		_dataSource = dataSource;
 		_windowSize = 0x10000; // 64k
 	}
 
 	public long getOffset()
 	{
 		return _offset;
 	}
 
 	public void setOffset( final long offset )
 	{
 		if ( _offset != offset )
 		{
 			_offset = offset;
 			_bytes = null;
 		}
 	}
 
 	public int getWindowSize()
 	{
 		return _windowSize;
 	}
 
 	public void setWindowSize( final int windowSize )
 	{
 		if ( _windowSize != windowSize )
 		{
 			_windowSize = windowSize;
 			_bytes = null;
 			_buffer = null;
 		}
 	}
 
 	public ByteBuffer getBuffer()
 	{
 		if ( _buffer == null )
 		{
 			_buffer = ByteBuffer.allocateDirect( _windowSize );
 		}
 		return _buffer;
 	}
 
 	public byte getByte( final long address )
 	throws IOException
 	{
 		final long relativeAddress = address - _offset;
 		if ( relativeAddress < 0L || relativeAddress >= (long)_windowSize )
 		{
 			setOffset( Math.max( 0L, address - (long)( _windowSize / 2 ) ) );
 		}
 		final int index = (int)( address - _offset );
 		final byte[] bytes = getBytes();
 		return index >= bytes.length ? (byte)0 : bytes[ index ];
 	}
 
 	private byte[] getBytes()
 	throws IOException
 	{
 		if ( _bytes == null )
 		{
 			_channel.position( _offset );
 
 			final ByteBuffer buffer = getBuffer();
 			buffer.clear();
 			while ( buffer.hasRemaining() && _channel.read( buffer ) != -1 )
 			{
 			}
 
 			buffer.flip();
 			final byte[] bytes = new byte[ buffer.limit() ];
 			buffer.get( bytes );
 			_bytes = bytes;
 		}
 
 		return _bytes;
 	}
 
 	public URI getDataSource()
 	{
 		return _dataSource;
 	}
 
 	public void setDataSource( final URI dataSource )
 	{
 		_dataSource = dataSource;
 	}
 
 	public long getBigEndian( final long start, final int length )
 	throws IOException
 	{
 		long result = 0L;
		for ( long offset = start, end = start + (long)length; offset < end; offset++ )
 		{
 			result = ( result << 8 ) | ( (long)getByte( offset ) & 0xffL );
 		}
 		return result;
 	}
 
 	public long getLittleEndian( final long start, final int length )
 	throws IOException
 	{
 		long result = 0L;
 		for ( long offset = start + (long)length - 1L; offset >= start; offset-- )
 		{
 			result = ( result << 8 ) | ( (long)getByte( offset ) & 0xffL );
 		}
 		return result;
 	}
 }
