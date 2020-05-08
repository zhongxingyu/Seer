 package org.sonatype.spice.zapper.fs;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.nio.channels.Channels;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.sonatype.spice.zapper.IOSourceListable;
 import org.sonatype.spice.zapper.Path;
 import org.sonatype.spice.zapper.Range;
 import org.sonatype.spice.zapper.ZFile;
 import org.sonatype.spice.zapper.hash.Hash;
 import org.sonatype.spice.zapper.hash.HashAlgorithm;
 import org.sonatype.spice.zapper.internal.Check;
 import org.sonatype.spice.zapper.internal.PathImpl;
 import org.sonatype.spice.zapper.internal.RangeImpl;
 import org.sonatype.spice.zapper.internal.RangeInputStream;
 import org.sonatype.spice.zapper.internal.ZFileImpl;
 
 public class DirectoryIOSource
     extends AbstractDirectory
     implements IOSourceListable
 {
     private final HashStrategy hashStrategy;
 
     /**
      * Creates source that will source ZFiles without hashes.
      * 
      * @param root
      * @throws IOException
      */
     public DirectoryIOSource( final File root )
         throws IOException
     {
         this( root, (HashStrategy) null );
     }
 
     /**
      * Creates source that will calculate hashes before creating ZFiles.
      * 
      * @param root
      * @param hashAlgorithm
      * @throws IOException
      */
     public DirectoryIOSource( final File root, final HashAlgorithm hashAlgorithm )
         throws IOException
     {
         this( root, new CalculateHashStrategy( hashAlgorithm ) );
     }
 
     /**
      * Creates source that will use given strategy to get hashes when creating ZFiles.
      * 
      * @param root
      * @param hashStrategy
      * @throws IOException
      */
     public DirectoryIOSource( final File root, final HashStrategy hashStrategy )
         throws IOException
     {
         super( root );
         this.hashStrategy = hashStrategy;
     }
 
     @Override
     public ZFile createZFile( final Path path )
         throws IOException
     {
         final File file = getFile( Check.notNull( path, Path.class ) );
         return createZFile( path, file );
     }
 
     @Override
     public InputStream readSegment( final Path path, final Range range )
         throws IOException
     {
         final File file = getFile( path );
         final Range fileRange = new RangeImpl( 0, file.length() );
 
         if ( fileRange.matches( range ) )
         {
             return Channels.newInputStream( new RandomAccessFile( file, "r" ).getChannel() );
         }
         else if ( fileRange.contains( range ) )
         {
             final RandomAccessFile raf = new RandomAccessFile( file, "r" );
             raf.seek( range.getOffset() );
             return new RangeInputStream( Channels.newInputStream( raf.getChannel() ), range, false, true );
         }
         else
         {
             throw new IOException( "Range falls out of the supplied ZFile!" );
         }
     }
 
     @Override
     public void close( final boolean successful )
         throws IOException
     {
         // nothing
     }
 
     @Override
     public List<ZFile> listFiles()
         throws IOException
     {
         final ArrayList<ZFile> zfiles = new ArrayList<ZFile>();
         scanDirectory( getRoot(), zfiles );
         return Collections.unmodifiableList( zfiles );
     }
 
     // ==
 
     public ZFile createZFile( final Path path, final File file )
         throws IOException
     {
         Hash hash = null;
         if ( hashStrategy != null )
         {
             hash = hashStrategy.getHashFor( file );
         }
         return createZFile( path, file, hash );
     }
 
     public ZFile createZFile( final Path path, final File file, final Hash hash )
         throws IOException
     {
         Check.notNull( path, Path.class );
         Check.notNull( file, File.class );
         // this is "source", we expect to create ZFiles based on existing files
         if ( file.isFile() )
         {
             if ( hash != null )
             {
                 return new ZFileImpl( path, file.length(), file.lastModified(), hash );
             }
             else
             {
                 return new ZFileImpl( path, file.length(), file.lastModified() );
             }
         }
         else
         {
             throw new FileNotFoundException( String.format( "File on path %s not found!", path ) );
         }
     }
 
     // ==
 
     protected int scanDirectory( final File dir, final List<ZFile> zfiles )
         throws IOException
     {
         if ( dir == null )
         {
             return 0;
         }
         int i = 0;
         File[] fileArray = dir.listFiles();
         if ( fileArray != null )
         {
             for ( File file : fileArray )
             {
                 if ( file.isDirectory() )
                 {
                     i += scanDirectory( file, zfiles );
                 }
                 else if ( file.isFile() )
                 {
                    // win paths
                    final String pathString =
                        file.getAbsolutePath().substring( getRoot().getAbsolutePath().length() + 1 ).replace( '\\', '/' );
                    final Path path = new PathImpl( pathString );
                     final ZFile zfile = createZFile( path, file );
                     zfiles.add( zfile );
                     i++;
                 }
             }
         }
         return i;
     }
 }
