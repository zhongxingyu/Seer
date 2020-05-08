 package com.dannycrafts.myTitles.database;
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 
 public class Database
 {
 	private File databaseFile;
 	private short version;
 	private Header header;
 	private ArrayList<Long> sockets;
 	private long dataOffset;
 	private RandomAccessFile stream;
 	
 	public Database( File databaseFile, short version, Header header ) throws IOException, Database.DifferentVersionException
 	{
 		this.databaseFile = databaseFile;
 		this.version = version;
 		this.sockets = new ArrayList<Long>();
 		this.header = header;
 		
 		if ( !databaseFile.exists() )
 			createFile();
 		else
 			openFile();
 	}
 	
 	public long addRow( Cell... cells ) throws IOException
 	{
 		long socketIndex = findSocket();
 		seekRow( socketIndex );
 		
 		stream.writeByte( 1 ); // Flag indicating the row exists, if set to 0 it is flagged as empty.
 		short i = 0;
 		for ( Cell cell : cells )
 		{
 			byte[] cellBuffer = cell.read();
 			byte[] buffer = new byte[header.getCellLength( i )];
 			System.arraycopy( cellBuffer, 0, buffer, 0, cellBuffer.length );
 			stream.write( buffer );
 			i++;
 		}
 		
 		return socketIndex;
 	}
 	
 	public long addUniqueRow( SearchCriteria searchCriteria, Cell... cells ) throws IOException
 	{
 		long index = findRow( searchCriteria );
 		if ( index == -1 )
 			return addRow( cells );
 		
 		return index;
 	}
 	
 	public void close() throws IOException
 	{
 		stream.getFD().sync();
 		stream.close();
 	}
 	
 	private void createFile() throws IOException
 	{
 		databaseFile.createNewFile();
 		open();
 		
 		stream.writeShort( version );
 		
 		close();
 		
 		this.dataOffset = 2;
 	}
 	
 	public long findRow( int cellIndex, Cell cellValue ) throws IOException
 	{
 		return findRow( new SearchCriteria( (short)cellIndex, cellValue ) );
 	}
 	
 	public long findRow( int cellIndex, String cellValue ) throws IOException
 	{
 		return findRow( new SearchCriteria( (short)cellIndex, new StringCell( cellValue ) ) );
 	}
 	
 	public long findRow( int cellIndex, int cellValue ) throws IOException
 	{
 		return findRow( new SearchCriteria( (short)cellIndex, new Int32Cell( cellValue ) ) );
 	}
 	
 	public long findRow( int cellIndex, long cellValue ) throws IOException
 	{
 		return findRow( new SearchCriteria( (short)cellIndex, new Int64Cell( cellValue ) ) );
 	}
 	
 	public long findRow( SearchCriteria... searchCriteria ) throws IOException
 	{
 		return findRow( 0, searchCriteria );
 	}
 	
 	public long findRow( long startIndex, SearchCriteria... searchCriteria ) throws IOException
 	{
 		try
 		{
			for ( long i = startIndex; ; i++ )
 			{
 				seekRow( i );
 				byte[] buffer = new byte[this.header.getHeaderLength()];
 				
 				if ( stream.readBoolean() == true ) // If row exists
 				{
 					boolean matches = true;
 					for ( short j = 0; j < searchCriteria.length; j++ )
 					{
 						seekCell( i, searchCriteria[j].cellIndex );
 						stream.read( buffer, 0, this.header.getCellLength( searchCriteria[j].cellIndex ) );
 						
 						if ( !searchCriteria[j].cellData.matches( buffer ) )
 						{
 							matches = false;
 							break;
 						}
 					}
 					
 					if ( matches ) return i;
 				}
 			}
 		}
 		catch (EOFException e) {}
 		
 		return -1;
 	}
 	
 	public long[] findRows( short cellIndex, long cellValue ) throws IOException
 	{
 		return findRows( cellIndex, new Int64Cell( cellValue ) );
 	}
 	
 	public long[] findRows( short cellIndex, Cell cellValue ) throws IOException
 	{
 		return findRows( new SearchCriteria( cellIndex, cellValue ) );
 	}
 	
 	public long[] findRows( SearchCriteria... searchCriteria ) throws IOException
 	{
 		ArrayList<Long> rowList = new ArrayList<Long>();
 		long row = findRow( searchCriteria );
 		while ( row != -1 )
 		{
 			rowList.add( row );
 			row = findRow( row, searchCriteria );
 		}
 
 		long[] rows = new long[rowList.size()];
 		for ( int i = 0; i < rowList.size(); i++ )
 			rows[i] = rowList.get( i );
 		
 		return rows;
 	}
 	
 	public long findSocket() throws IOException
 	{
 		if ( sockets.size() != 0 )
 		{
 			long socketIndex = sockets.get( 0 );
 			sockets.remove( 0 );
 			return socketIndex;
 		}
 		
 		stream.seek( dataOffset );
 		long index = 0;
 		try
 		{
 			while ( stream.readBoolean() == true )
 			{
 				short headerLength = header.getHeaderLength();
 				int skipped = stream.skipBytes( headerLength );
 				if ( skipped < headerLength )
 					return index;
 				index++;
 			}
 		}
 		catch ( EOFException e )
 		{
 			return index;
 		}
 		
 		return -1;
 	}
 	
 	public void flush() throws IOException
 	{
 		stream.getFD().sync();
 	}
 	
 	public Row getRow( long index ) throws IOException
 	{
 		boolean exists = false;
 		try
 		{
 			seekRow( index );
 			exists = stream.readBoolean();
 		}
 		catch ( EOFException e ) { return null; }
 		if ( !exists ) return null;
 		
 		byte[] buffer = new byte[header.getHeaderLength()];
 		stream.read( buffer );
 		
 		return new Row( index, this.header, buffer );
 	}
 	
 	public void open() throws IOException
 	{
 		stream = new RandomAccessFile( databaseFile, "rw" );
 	}
 	
 	private void openFile() throws IOException, Database.DifferentVersionException
 	{
 		open();
 		
 		short dbVersion = stream.readShort();
 		if ( dbVersion != this.version )
 		{
 			close();
 			throw new Database.DifferentVersionException( dbVersion );
 		}
 		
 		close();
 		
 		this.dataOffset = 2;
 	}
 	
 	public boolean removeRow( long index ) throws IOException
 	{
 		try
 		{
 			seekRow( index );
 			stream.writeByte( 0 );
 		}
 		catch ( EOFException e ) { return false; }
 		
 		return true;
 	}
 	
 	public int removeRows( SearchCriteria searchCriteria ) throws IOException
 	{
 		long row = findRow( searchCriteria );
 		int removed = 0;
 		while ( row != -1 )
 		{
 			removeRow( row );
 			removed++;
 			
 			row = findRow( row, searchCriteria );
 		}
 
 		return removed;
 	}
 	
 	private void seekCell( long rowIndex, short cellIndex ) throws IOException
 	{
 		stream.seek( dataOffset + rowIndex * ( header.getHeaderLength() + 1 ) + 1 + header.getCellOffset( cellIndex ) );
 	}
 	
 	private void seekRow( long index ) throws IOException
 	{
 		stream.seek( dataOffset + index * ( header.getHeaderLength() + 1 ) );
 	}
 	
 	public boolean updateCell( long rowIndex, short cellIndex, int cellData ) throws IOException
 	{
 		return updateCell( rowIndex, cellIndex, new Int32Cell( cellData ) );
 	}
 	
 	public boolean updateCell( long rowIndex, short cellIndex, long cellData ) throws IOException
 	{
 		return updateCell( rowIndex, cellIndex, new Int64Cell( cellData ) );
 	}
 	
 	public boolean updateCell( long rowIndex, short cellIndex, String cellData ) throws IOException
 	{
 		return updateCell( rowIndex, cellIndex, new StringCell( cellData ) );
 	}
 	
 	public boolean updateCell( long rowIndex, short cellIndex, Cell cell ) throws IOException
 	{
 		try
 		{
 			seekRow( rowIndex );
 			if ( stream.readBoolean() == false ) return false;
 			stream.skipBytes( header.getCellOffset( cellIndex ) );
 			stream.write( cell.read() );
 		}
 		catch ( EOFException e ) { return false; }
 		
 		return true;
 	}
 	
 	public boolean updateRow( long index, Cell... cells ) throws IOException
 	{
 		try
 		{
 			seekRow( index );
 			if ( stream.readBoolean() == false ) return false;
 		}
 		catch ( EOFException e ) { return false; }
 		
 		short i = 0;
 		for ( Cell cell : cells )
 		{
 			byte[] cellBuffer = cell.read();
 			byte[] buffer = new byte[header.getCellLength( i )];
 			System.arraycopy( cellBuffer, 0, buffer, 0, cellBuffer.length );
 			stream.write( buffer );
 			
 			i++;
 		}
 		
 		return true;
 	}
 
 	public static class DifferentVersionException extends Exception
 	{
 		public short actualVersion;
 		
 		public DifferentVersionException( short actualVersion )
 		{
 			this.actualVersion = actualVersion;
 		}
 	}
 	
 }
