 package com.ikkerens.worldedit.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.nio.ShortBuffer;
 import java.util.zip.DataFormatException;
 import java.util.zip.Deflater;
 import java.util.zip.Inflater;
 
 public class Clipboard {
     // Relative towards player
     private final int         rX, rY, rZ;
     private final short[][][] blocks;
 
     public Clipboard( final int rX, final int rY, final int rZ, final short[][][] blocks ) {
         this.rX = rX;
         this.rY = rY;
         this.rZ = rZ;
         this.blocks = blocks;
     }
 
     public static Clipboard loadFromFile( final String name ) {
         RandomAccessFile file = null;
 
         try {
             final File schemFile = new File( String.format( "plugins/MBWorldEdit/schematics/%s.mbschem", name ) );
             schemFile.getParentFile().mkdirs();
 
             if ( !schemFile.exists() )
                 return null;
 
             file = new RandomAccessFile( schemFile.getAbsolutePath(), "r" );
             final int oSize = file.readInt();
             final int cSize = file.readInt();
             final byte[] compressed = new byte[ cSize ];
             final byte[] decompressed = new byte[ oSize ];
 
             int loaded = 0;
             while ( loaded != cSize )
                 loaded += file.read( compressed, loaded, cSize - loaded );
 
             final Inflater decompressor = new Inflater();
             decompressor.setInput( compressed );
             decompressor.inflate( decompressed );
             decompressor.end();
 
             final ByteBuffer schem = ByteBuffer.wrap( decompressed );
 
             final int rX = schem.getInt();
             final int rY = schem.getInt();
             final int rZ = schem.getInt();
             final short[][][] clp = new short[ schem.getInt() ][ schem.getInt() ][ schem.getInt() ];
 
             final ShortBuffer sBuf = schem.asShortBuffer();
 
             for ( final short[][] element : clp )
                 for ( int y = 0; y < clp[ 0 ].length; y++ )
                     sBuf.get( element[ y ], 0, element[ y ].length );
 
             return new Clipboard( rX, rY, rZ, clp );
         } catch ( final IOException e ) {
         } catch ( final DataFormatException e ) {
         } finally {
             if ( file != null )
                 try {
                     file.close();
                 } catch ( final IOException e ) {
                 }
         }
 
         return null;
     }
 
     public int getRelativeX() {
         return this.rX;
     }
 
     public int getRelativeY() {
         return this.rY;
     }
 
     public int getRelativeZ() {
         return this.rZ;
     }
 
     public short[][][] getBlocks() {
         return this.blocks;
     }
 
     public boolean save( final String name ) {
         RandomAccessFile file = null;
 
         try {
             final File schemFile = new File( String.format( "plugins/MBWorldEdit/schematics/%s.mbschem", name ) );
             schemFile.getParentFile().mkdirs();
 
             if ( schemFile.exists() )
                 schemFile.delete();
 
             final ByteBuffer byteBuf = ByteBuffer.allocate( 24 + ( this.blocks.length * this.blocks[ 0 ].length * this.blocks[ 0 ][ 0 ].length * 2 ) );
 
             byteBuf.putInt( this.rX );
             byteBuf.putInt( this.rY );
             byteBuf.putInt( this.rZ );
             byteBuf.putInt( this.blocks.length );
             byteBuf.putInt( this.blocks[ 0 ].length );
             byteBuf.putInt( this.blocks[ 0 ][ 0 ].length );
 
             final ShortBuffer sBuf = byteBuf.asShortBuffer();
             for ( final short[][] block : this.blocks )
                for ( int y = 0; y < block.length; y++ )
                    sBuf.put( block[ y ] );
 
             final byte[] before = byteBuf.array();
             final byte[] result = new byte[ before.length ];
 
             final Deflater compressor = new Deflater( Deflater.BEST_COMPRESSION );
             compressor.setInput( before );
             compressor.finish();
             final int cSize = compressor.deflate( result );
             compressor.end();
 
             file = new RandomAccessFile( schemFile.getAbsolutePath(), "rw" );
             file.writeInt( before.length );
             file.writeInt( cSize );
             file.write( result, 0, cSize );
 
             return true;
         } catch ( final IOException e ) {
         } finally {
             if ( file != null )
                 try {
                     file.close();
                 } catch ( final IOException e ) {
                 }
         }
 
         return false;
     }
 }
