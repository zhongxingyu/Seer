 /*******************************************************************************
  * Copyright (c) 2007, 2008 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Actuate Corporation  - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.birt.core.archive.compound;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 public class ArchiveView implements IArchiveFile
 {
 	private boolean sharedArchive = false;
 	private IArchiveFile view = null;
 	private IArchiveFile archive = null;
 
 	public ArchiveView( IArchiveFile view, IArchiveFile archive,
 			boolean sharedArchive )
 	{
 		this.view = view;
 		this.archive = archive;
 		this.sharedArchive = sharedArchive;
 	}
 
 	public ArchiveView( String viewName, String archiveName, String viewMode )
 			throws IOException
 	{
 		this.view = new ArchiveFileV2( viewName, viewMode );
 		this.archive = new ArchiveFile( archiveName, "r" );
 		sharedArchive = false;
 	}
 
 	public ArchiveView( String viewName, IArchiveFile archive, String viewMode )
 			throws IOException
 	{
 		this.view = new ArchiveFileV2( viewName, viewMode );
 		this.archive = archive;
 		this.sharedArchive = true;
 	}
 
 	public void close( ) throws IOException
 	{
 		try
 		{
 			if ( !sharedArchive )
 			{
 				archive.close( );
 			}
 		}
 		finally
 		{
 			view.close( );
 		}
 	}
 
 	synchronized public boolean exists( String name )
 	{
 		if ( view.exists( name ) || archive.exists( name ) )
 		{
 			return true;
 		}
 		return false;
 	}
 
 	synchronized public ArchiveEntry getEntry( String name ) throws IOException
 	{
 		ArchiveEntry result = view.getEntry( name );
 		if ( result != null )
 		{
 			return result;
 		}
 
 		result = archive.getEntry( name );
 		if ( result != null )
 		{
 			return new ViewEntry( this, name, result );
 		}
 		return null;
 	}
 
 	synchronized public List listEntries( String namePattern )
 	{
 		List viewList = view.listEntries( namePattern );
 		List archiveList = archive.listEntries( namePattern );
 
 		Iterator iter = archiveList.iterator( );
 		while ( iter.hasNext( ) )
 		{
 			String entryName = (String) iter.next( );
 			if ( !viewList.contains( entryName ) )
 			{
 				viewList.add( entryName );
 			}
 		}
 
 		return viewList;
 	}
 
 	public Object lockEntry( ArchiveEntry entry ) throws IOException
 	{
 		return entry;
 	}
 
 	public void refresh( ) throws IOException
 	{
 		//archive.refresh( ); donot need to refresh archive, because archive in ONLY in r mode
 		view.refresh( );
 	}
 
 	public String getSystemId( )
 	{
 		return view.getSystemId( );
 	}
 
 	public String getDependId()
 	{
 		return archive.getSystemId();
 	}
 
 	class ViewEntry extends ArchiveEntry
 	{
 		IArchiveFile view;
 		boolean writable;
 		ArchiveEntry entry;
 		String name;
 
 		ViewEntry( IArchiveFile view, String name, ArchiveEntry entry )
 		{
 			writable = false;
 			this.view = view;
 			this.name = name;
 			this.entry = entry;
 		}
 
 		public long getLength( ) throws IOException
 		{
 			return entry.getLength( );
 		}
 
 		public void setLength( long length ) throws IOException
 		{
 			ensureWritable( );
 			entry.setLength( length );
 		}
 
 		public void flush( ) throws IOException
 		{
 			ensureWritable( );
 			entry.flush( );
 		}
 
 		public void refresh( ) throws IOException
 		{
 			entry.refresh( );
 		}
 
 		public int read( long pos, byte[] b, int off, int len )
 				throws IOException
 		{
 			return entry.read( pos, b, off, len );
 		}
 
 		public void write( long pos, byte[] b, int off, int len )
 				throws IOException
 		{
 			ensureWritable( );
 			entry.write( pos, b, off, len );
 		}
 
 		void ensureWritable( ) throws IOException
 		{
 			if ( !writable )
 			{
 				ArchiveEntry viewEntry = view.createEntry( name );
 				copyEntry( entry, viewEntry );
 				entry = viewEntry;
 				writable = true;
 			}
 		}
 
 		private void copyEntry( ArchiveEntry src, ArchiveEntry tgt )
 				throws IOException
 		{
 			byte[] b = new byte[4096];
 			long length = src.getLength( );
 			long pos = 0;
 			while ( pos < length )
 			{
 				int size = src.read( pos, b, 0, 4096 );
 				tgt.write( pos, b, 0, size );
 				pos += size;
 			}
 		}
 	}
 
 	public ArchiveEntry createEntry( String name ) throws IOException
 	{
 		return view.createEntry( name );
 	}
 
 	public void flush( ) throws IOException
 	{
 		view.flush( );
 	}
 
	public void save( ) throws IOException
	{
		view.save( );
	}

 	public String getName( )
 	{
 		return view.getName( );
 	}
 
 	public int getUsedCache( )
 	{
 		return view.getUsedCache( );
 	}
 
 	public boolean removeEntry( String name ) throws IOException
 	{
 		return view.removeEntry( name );
 	}
 
 	public void setCacheSize( int cacheSize )
 	{
 		view.setCacheSize( cacheSize );
 	}
 
 	public void unlockEntry( Object locker ) throws IOException
 	{
 		view.unlockEntry( locker );
 	}
 
 	public IArchiveFile getArchive( )
 	{
 		return this.archive;
 	}
 
 	public IArchiveFile getView( )
 	{
 		return this.view;
 	}
 }
