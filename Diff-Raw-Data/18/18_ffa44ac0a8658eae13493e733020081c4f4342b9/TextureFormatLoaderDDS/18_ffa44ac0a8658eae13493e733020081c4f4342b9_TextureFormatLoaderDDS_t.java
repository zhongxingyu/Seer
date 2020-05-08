 /**
  * Copyright (c) 2007-2009, JAGaToo Project Group all rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * 
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * 
  * Neither the name of the 'Xith3D Project Group' nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
  * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE
  */
 package org.jagatoo.loaders.textures.formats;
 
 import java.io.BufferedInputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import org.jagatoo.loaders.textures.AbstractTexture;
 import org.jagatoo.loaders.textures.AbstractTextureImage;
 import org.jagatoo.loaders.textures.TextureFactory;
 import org.jagatoo.opengl.enums.TextureImageFormat;
 import org.jagatoo.opengl.enums.TextureImageInternalFormat;
 
 /**
  * Reads compressed DDS files.
  * Uncompressed file are currently not supported.
  * 
  * @author Matthias Mann
  * @author Marvin Froehlich (aka Qudus)
  */
 public class TextureFormatLoaderDDS implements TextureFormatLoader
 {
     /*
     private static final int DDSD_CAPS         = 0x00000001;
     private static final int DDSD_HEIGHT       = 0x00000002;
     private static final int DDSD_WIDTH        = 0x00000004;
     private static final int DDSD_PITCH        = 0x00000008;
     private static final int DDSD_PIXELFORMAT  = 0x00001000;
     private static final int DDSD_MIPMAPCOUNT  = 0x00020000;
     private static final int DDSD_LINEARSIZE   = 0x00080000;
     private static final int DDSD_DEPTH        = 0x00800000;
     
     private static final int DDPF_ALPHAPIXELS  = 0x00000001;
     private static final int DDPF_FOURCC       = 0x00000004;
     private static final int DDPF_RGB          = 0x00000040;
     
     private static final int DDSCAPS_COMPLEX  = 0x00000008;
     private static final int DDSCAPS_TEXTURE  = 0x00001000;
     private static final int DDSCAPS_MIPMAP   = 0x00400000;
     
     private static final int DDSCAPS2_CUBEMAP            = 0x00000200;
     private static final int DDSCAPS2_CUBEMAP_POSITIVEX  = 0x00000400;
     private static final int DDSCAPS2_CUBEMAP_NEGATIVEX  = 0x00000800;
     private static final int DDSCAPS2_CUBEMAP_POSITIVEY  = 0x00001000;
     private static final int DDSCAPS2_CUBEMAP_NEGATIVEY  = 0x00002000;
     private static final int DDSCAPS2_CUBEMAP_POSITIVEZ  = 0x00004000;
     private static final int DDSCAPS2_CUBEMAP_NEGATIVEZ  = 0x00008000;
     private static final int DDSCAPS2_VOLUME             = 0x00200000;
     */
     
     //private static final int HEADER_FIELD_SIZE                 = 0;
     //private static final int HEADER_FIELD_FLAGS                = 1;
     private static final int HEADER_FIELD_HEIGHT = 2;
     private static final int HEADER_FIELD_WIDTH = 3;
     //private static final int HEADER_FIELD_PITCH_OR_LINEAR_SIZE = 4;
     //private static final int HEADER_FIELD_DEPTH                = 5;
     private static final int HEADER_FIELD_MIPMAP_COUNT = 6;
     
     //private static final int HEADER_FIELD_PIXEL_FORMAT_SIZE               = 18;
     //private static final int HEADER_FIELD_PIXEL_FORMAT_FLAGS              = 19;
     private static final int HEADER_FIELD_PIXEL_FORMAT_FOUR_CC = 20;
     private static final int HEADER_FIELD_PIXEL_FORMAT_RGB_BIT_COUNT = 21;
     private static final int HEADER_FIELD_PIXEL_FORMAT_R_BIT_MASK = 22;
     private static final int HEADER_FIELD_PIXEL_FORMAT_G_BIT_MASK = 23;
     private static final int HEADER_FIELD_PIXEL_FORMAT_B_BIT_MASK = 24;
     private static final int HEADER_FIELD_PIXEL_FORMAT_RGB_ALPHA_BIT_MASK = 25;
     
     //private static final int HEADER_FIELD_CAPS_1 = 26;
     //private static final int HEADER_FIELD_CAPS_2 = 27;
     
     /**
      * Creates a new instance of TextureStreamLoaderDDS
      */
     public TextureFormatLoaderDDS()
     {
     }
     
    public AbstractTexture loadTexture( BufferedInputStream in, boolean acceptAlpha, boolean flipVertically, boolean loadMipmaps, boolean allowStreching, TextureFactory texFactory ) throws IOException
     {
         if ( in.available() < 2 * 4 )
         {
             return ( null );
         }
         
         if ( readInt( in ) != 0x20534444 ) // "DDS "
         {
             return ( null );
         }
         
         final int headerByteSize = readInt( in ); // size of the header
         if ( headerByteSize != 124 )
         {
             return ( null );
         }
         final int headerIntSize = headerByteSize / 4;
         
         final int[] header = new int[ headerIntSize ];
         header[ 0 ] = headerByteSize;
         for ( int i = 1; i < headerIntSize; i++ )
         {
             header[ i ] = readInt( in );
         }
         
         /*
         System.out.println( header[ HEADER_FIELD_HEIGHT ] );
         System.out.println( header[ HEADER_FIELD_WIDTH ] );
         System.out.println( header[ HEADER_FIELD_MIPMAP_COUNT ] );
         System.out.println( header[ HEADER_FIELD_PIXEL_FORMAT_SIZE ] );
         System.out.println( header[ HEADER_FIELD_PIXEL_FORMAT_FLAGS ] );
         System.out.println( header[ HEADER_FIELD_PIXEL_FORMAT_FOUR_CC ] );
         System.out.println( header[ HEADER_FIELD_PIXEL_FORMAT_RGB_BIT_COUNT ] );
         System.out.println( header[ HEADER_FIELD_CAPS_1 ] );
         System.out.println( header[ HEADER_FIELD_CAPS_2 ] );
         */
 
         LinePostProcessing lpp = null;
         TextureImageFormat texImgFormat;
         TextureImageInternalFormat internalFormat = TextureImageInternalFormat.RGBA;
         int blockSize = 0;
         int ddsPixelSize = 0;
         int icPixelSize = 0;
         
         final boolean readAlpha;
         
         switch ( header[ HEADER_FIELD_PIXEL_FORMAT_FOUR_CC ] )
         {
             case 0x31545844: // FOURCC_DXT1
                 // DXT1's compression ratio is 8:1
                 readAlpha = acceptAlpha;
                 texImgFormat = TextureImageFormat.RGBA_DXT1;
                 blockSize = 8;
                 break;
             
             case 0x33545844: // FOURCC_DXT3
                 // DXT3's compression ratio is 4:1
                 readAlpha = acceptAlpha;
                 texImgFormat = TextureImageFormat.RGBA_DXT3;
                 blockSize = 16;
                 break;
             
             case 0x34545844: // FOURCC_DXT5
                 // DXT5's compression ratio is 4:1
                 readAlpha = acceptAlpha;
                 texImgFormat = TextureImageFormat.RGBA_DXT5;
                 blockSize = 16;
                 break;
             
             case 0:
                 switch ( header[ HEADER_FIELD_PIXEL_FORMAT_RGB_BIT_COUNT ] )
                 {
                     case 8:
                         ddsPixelSize = 1;
                         
                         if ( ( header[ HEADER_FIELD_PIXEL_FORMAT_R_BIT_MASK ] == 0xe0 ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_G_BIT_MASK ] == 0x1c ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_B_BIT_MASK ] == 0x03 ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_RGB_ALPHA_BIT_MASK ] == 0x00 ) )
                         {
                             readAlpha = false;
                             texImgFormat = TextureImageFormat.RGB;
                             internalFormat = TextureImageInternalFormat.R3_G3_B2;
                             icPixelSize = 3;
                             lpp = LinePostProcessing_R3G3B2.instance;
                             break;
                         }
                         
                         throw new IOException( "Unsupported 8 bit color format: red = " + Integer.toHexString( header[ HEADER_FIELD_PIXEL_FORMAT_R_BIT_MASK ] ) + ", green" + Integer.toHexString( header[ HEADER_FIELD_PIXEL_FORMAT_G_BIT_MASK ] ) + ", blue = " + Integer.toHexString( header[ HEADER_FIELD_PIXEL_FORMAT_B_BIT_MASK ] ) + ", alpha = " + Integer.toHexString( header[ HEADER_FIELD_PIXEL_FORMAT_RGB_ALPHA_BIT_MASK ] ) );
                         
                     case 16:
                         ddsPixelSize = 2;
                         
                         if ( ( header[ HEADER_FIELD_PIXEL_FORMAT_R_BIT_MASK ] == 0x0f00 ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_G_BIT_MASK ] == 0x00f0 ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_B_BIT_MASK ] == 0x000f ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_RGB_ALPHA_BIT_MASK ] == 0x0000 ) )
                         {
                             readAlpha = false;
                             texImgFormat = TextureImageFormat.RGB;
                             internalFormat = TextureImageInternalFormat.RGB4;
                             icPixelSize = 3;
                             lpp = LinePostProcessing_X4R4G4B4.instance;
                             break;
                         }
                         
                         if ( ( header[ HEADER_FIELD_PIXEL_FORMAT_R_BIT_MASK ] == 0xf800 ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_G_BIT_MASK ] == 0x07e0 ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_B_BIT_MASK ] == 0x001f ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_RGB_ALPHA_BIT_MASK ] == 0x0000 ) )
                         {
                             readAlpha = false;
                             texImgFormat = TextureImageFormat.RGB;
                             internalFormat = TextureImageInternalFormat.RGB5;
                             icPixelSize = 3;
                             lpp = LinePostProcessing_R5G6B5.instance;
                             break;
                         }
                         
                         if ( ( header[ HEADER_FIELD_PIXEL_FORMAT_R_BIT_MASK ] == 0x7c00 ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_G_BIT_MASK ] == 0x03e0 ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_B_BIT_MASK ] == 0x001f ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_RGB_ALPHA_BIT_MASK ] == 0x0000 ) )
                         {
                             readAlpha = false;
                             texImgFormat = TextureImageFormat.RGB;
                             internalFormat = TextureImageInternalFormat.RGB5;
                             icPixelSize = 3;
                             lpp = LinePostProcessing_X1R5G5B5.instance;
                             break;
                         }
                         
                         if ( ( header[ HEADER_FIELD_PIXEL_FORMAT_R_BIT_MASK ] == 0x7c00 ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_G_BIT_MASK ] == 0x03e0 ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_B_BIT_MASK ] == 0x001f ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_RGB_ALPHA_BIT_MASK ] == 0x8000 ) )
                         {
                             readAlpha = false;
                             texImgFormat = TextureImageFormat.RGBA;
                             internalFormat = TextureImageInternalFormat.RGB5_A1;
                             icPixelSize = 4;
                             lpp = LinePostProcessing_A1R5G5B5.instance;
                             break;
                         }
                         
                         if ( ( header[ HEADER_FIELD_PIXEL_FORMAT_R_BIT_MASK ] == 0x0f00 ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_G_BIT_MASK ] == 0x00f0 ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_B_BIT_MASK ] == 0x000f ) && ( header[ HEADER_FIELD_PIXEL_FORMAT_RGB_ALPHA_BIT_MASK ] == 0xf000 ) )
                         {
                             readAlpha = acceptAlpha;
                             texImgFormat = TextureImageFormat.RGBA;
                             internalFormat = TextureImageInternalFormat.RGBA4;
                             icPixelSize = 4;
                             lpp = LinePostProcessing_A4R4G4B4.instance;
                             break;
                         }
                         
                         throw new IOException( "Unsupported 16 bit color format: red = " + Integer.toHexString( header[ HEADER_FIELD_PIXEL_FORMAT_R_BIT_MASK ] ) + ", green = " + Integer.toHexString( header[ HEADER_FIELD_PIXEL_FORMAT_G_BIT_MASK ] ) + ", blue = " + Integer.toHexString( header[ HEADER_FIELD_PIXEL_FORMAT_B_BIT_MASK ] ) + ", alpha = " + Integer.toHexString( header[ HEADER_FIELD_PIXEL_FORMAT_RGB_ALPHA_BIT_MASK ] ) );
                         
                     default:
                         throw new IOException( "Unsupported color depth: " + header[ HEADER_FIELD_PIXEL_FORMAT_RGB_BIT_COUNT ] );
                 }
                 break;
             
             default:
                 throw new IOException( "Unknown compression format: " + Integer.toHexString( header[ HEADER_FIELD_PIXEL_FORMAT_FOUR_CC ] ) );
         }
         
         AbstractTexture tex = texFactory.createTexture( readAlpha );
         
         int width = header[ HEADER_FIELD_WIDTH ];
         int height = header[ HEADER_FIELD_HEIGHT ];
         final int mipmapCount = loadMipmaps ? header[ HEADER_FIELD_MIPMAP_COUNT ] : 1;
         
         if ( blockSize > 0 )
         {
             int size = ( ( width + 3 ) / 4 ) * ( ( height + 3 ) / 4 ) * blockSize;
             byte[] tmp = new byte[ size ];
             
             for ( int i = 0; i < mipmapCount; ++i )
             {
                 readFully( in, tmp, 0, size );
                 
                 AbstractTextureImage ti = texFactory.createTextureImage( width, height, width, height, -1, size, internalFormat, texImgFormat );
                 
                 ByteBuffer bb = ti.getDataBuffer();
                 //bb.position( 0 );
                 bb.limit( bb.capacity() );
                 bb.put( tmp, 0, size );
                 bb.flip();
                 
                 tex.setImage( i, ti );
                 
                 width = ( width + 1 ) >> 1;
                 height = ( height + 1 ) >> 1;
                 size = ( ( width + 3 ) / 4 ) * ( ( height + 3 ) / 4 ) * blockSize;
             }
         }
         else
         {
             int ddsLineSize = width * ddsPixelSize;
             int icLineSize = width * icPixelSize;
             byte[] tmpBuf = new byte[ icLineSize * height + 4 ];
             byte[] ddsBuf = ( lpp != null ) ? new byte[ ddsLineSize + 4 ] : null;
             
             for ( int i = 0; i < mipmapCount; ++i )
             {
                 int ddsLinePitch = ddsLineSize;// (lineSize+3) & ~3;
                 
                 if ( lpp != null )
                 {
                     for ( int y = 0, off = 0; y < height; ++y )
                     {
                         readFully( in, ddsBuf, 0, ddsLinePitch );
                         lpp.postProcess( tmpBuf, off, ddsBuf, 0, width );
                         off += icLineSize;
                     }
                 }
                 else if ( icLineSize == ddsLinePitch )
                 {
                     readFully( in, tmpBuf, 0, icLineSize * height );
                 }
                 else
                 {
                     for ( int y = 0, off = 0; y < height; ++y )
                     {
                         readFully( in, tmpBuf, off, ddsLinePitch );
                         off += icLineSize;
                     }
                 }
                 
                 AbstractTextureImage ti = texFactory.createTextureImage( width, height, width, height, readAlpha ? 4 : 3, internalFormat, texImgFormat );
                 
                 ByteBuffer bb = ti.getDataBuffer();
                 //bb.position( 0 );
                 bb.limit( bb.capacity() );
                 bb.put( tmpBuf );
                 bb.flip();
                 
                 tex.setImage( i, ti );
                 
                 width = Math.max( 1, width >> 1 );
                 height = Math.max( 1, height >> 1 );
                 ddsLineSize = width * ddsPixelSize;
                 icLineSize = width * icPixelSize;
             }
         }
         
         return ( tex );
     }
     
     protected byte[] tmpReadBuffer = new byte[ 64 ];
     
     private void readFully( BufferedInputStream in, byte[] data, int off, int len ) throws IOException
     {
         while ( len > 0 )
         {
             int read = in.read( data, off, len );
             if ( read <= 0 )
             {
                 throw new EOFException();
             }
             off += read;
             len -= read;
         }
     }
     
     /*
     private void skipBytes( BufferedInputStream in, int numBytes ) throws IOException
     {
         readFully( in, tmpReadBuffer, 0, numBytes );
     }
     */
 
     private int readInt( BufferedInputStream in ) throws IOException
     {
         byte[] tmp = tmpReadBuffer;
         readFully( in, tmp, 0, 4 );
         
         return ( ( ( tmp[ 3 ] & 255 ) << 24 ) | ( ( tmp[ 2 ] & 255 ) << 16 ) | ( ( tmp[ 1 ] & 255 ) << 8 ) | ( ( tmp[ 0 ] & 255 ) ) );
     }
     
     protected static final byte[] tab2 = new byte[ 4 ];
     protected static final byte[] tab3 = new byte[ 8 ];
     protected static final byte[] tab4 = new byte[ 16 ];
     protected static final byte[] tab5 = new byte[ 32 ];
     protected static final byte[] tab6 = new byte[ 64 ];
     
     static
     {
         for ( int i = 0; i < 4; ++i )
         {
             tab2[ i ] = (byte)( ( i * 255 ) / 3 );
         }
         for ( int i = 0; i < 8; ++i )
         {
             tab3[ i ] = (byte)( ( i * 255 ) / 7 );
         }
         for ( int i = 0; i < 16; ++i )
         {
             tab4[ i ] = (byte)( ( i * 255 ) / 15 );
         }
         for ( int i = 0; i < 32; ++i )
         {
             tab5[ i ] = (byte)( ( i * 255 ) / 31 );
         }
         for ( int i = 0; i < 64; ++i )
         {
             tab6[ i ] = (byte)( ( i * 255 ) / 63 );
         }
     }
     
     private interface LinePostProcessing
     {
         public void postProcess( byte[] dst, int dstOff, byte[] src, int srcOff, int width );
     }
     
     /**
      * Converts A4R4G4B4 to RGBA
      */
     private static class LinePostProcessing_A4R4G4B4 implements LinePostProcessing
     {
         public static final LinePostProcessing_A4R4G4B4 instance = new LinePostProcessing_A4R4G4B4();
         
         public void postProcess( byte[] dst, int dstOff, byte[] src, int srcOff, int width )
         {
             for ( int x = 0; x < width; ++x, srcOff += 2 )
             {
                 int v = ( ( src[ srcOff ] & 255 ) ) | ( ( src[ srcOff + 1 ] & 255 ) << 8 );
                 dst[ dstOff++ ] = tab4[ ( v >> 8 ) & 15 ];
                 dst[ dstOff++ ] = tab4[ ( v >> 4 ) & 15 ];
                 dst[ dstOff++ ] = tab4[ ( v ) & 15 ];
                 dst[ dstOff++ ] = tab4[ ( v >> 12 ) & 15 ];
             }
         }
     }
     
     /**
      * Converts X4R4G4B4 to RGB
      */
     private static class LinePostProcessing_X4R4G4B4 implements LinePostProcessing
     {
         public static final LinePostProcessing_X4R4G4B4 instance = new LinePostProcessing_X4R4G4B4();
         
         public void postProcess( byte[] dst, int dstOff, byte[] src, int srcOff, int width )
         {
             for ( int x = 0; x < width; ++x, srcOff += 2 )
             {
                 int v = ( ( src[ srcOff ] & 255 ) ) | ( ( src[ srcOff + 1 ] & 255 ) << 8 );
                 dst[ dstOff++ ] = tab4[ ( v >> 8 ) & 15 ];
                 dst[ dstOff++ ] = tab4[ ( v >> 4 ) & 15 ];
                 dst[ dstOff++ ] = tab4[ ( v ) & 15 ];
             }
         }
     }
     
     /**
      * Converts R5G6B5 to RGB
      */
     private static class LinePostProcessing_R5G6B5 implements LinePostProcessing
     {
         public static final LinePostProcessing_R5G6B5 instance = new LinePostProcessing_R5G6B5();
         
         public void postProcess( byte[] dst, int dstOff, byte[] src, int srcOff, int width )
         {
             for ( int x = 0; x < width; ++x, srcOff += 2 )
             {
                 int v = ( ( src[ srcOff ] & 255 ) ) | ( ( src[ srcOff + 1 ] & 255 ) << 8 );
                 dst[ dstOff++ ] = tab5[ ( v >> 11 ) & 31 ];
                 dst[ dstOff++ ] = tab6[ ( v >> 5 ) & 63 ];
                 dst[ dstOff++ ] = tab5[ ( v ) & 31 ];
             }
         }
     }
     
     /**
      * Converts X1R5G5B5 to RGB
      */
     private static class LinePostProcessing_X1R5G5B5 implements LinePostProcessing
     {
         public static final LinePostProcessing_X1R5G5B5 instance = new LinePostProcessing_X1R5G5B5();
         
         public void postProcess( byte[] dst, int dstOff, byte[] src, int srcOff, int width )
         {
             for ( int x = 0; x < width; ++x, srcOff += 2 )
             {
                 int v = ( ( src[ srcOff ] & 255 ) ) | ( ( src[ srcOff + 1 ] & 255 ) << 8 );
                 dst[ dstOff++ ] = tab5[ ( v >> 10 ) & 31 ];
                 dst[ dstOff++ ] = tab5[ ( v >> 5 ) & 31 ];
                 dst[ dstOff++ ] = tab5[ ( v ) & 31 ];
             }
         }
     }
     
     /**
      * Converts A1R5G5B5 to RGBA
      */
     private static class LinePostProcessing_A1R5G5B5 implements LinePostProcessing
     {
         public static final LinePostProcessing_A1R5G5B5 instance = new LinePostProcessing_A1R5G5B5();
         
         public void postProcess( byte[] dst, int dstOff, byte[] src, int srcOff, int width )
         {
             for ( int x = 0; x < width; ++x, srcOff += 2 )
             {
                 int v = ( ( src[ srcOff ] & 255 ) ) | ( ( src[ srcOff + 1 ] & 255 ) << 8 );
                 dst[ dstOff++ ] = tab5[ ( v >> 10 ) & 31 ];
                 dst[ dstOff++ ] = tab5[ ( v >> 5 ) & 31 ];
                 dst[ dstOff++ ] = tab5[ ( v ) & 31 ];
                 dst[ dstOff++ ] = (byte)( (short)v >>> 15 );
             }
         }
     }
     
     /**
      * Converts R3G3B2 to RGB
      */
     private static class LinePostProcessing_R3G3B2 implements LinePostProcessing
     {
         public static final LinePostProcessing_R3G3B2 instance = new LinePostProcessing_R3G3B2();
         
         public void postProcess( byte[] dst, int dstOff, byte[] src, int srcOff, int width )
         {
             for ( int x = 0; x < width; ++x )
             {
                 int v = src[ srcOff++ ] & 255;
                 dst[ dstOff++ ] = tab3[ ( v >> 5 ) & 7 ];
                 dst[ dstOff++ ] = tab3[ ( v >> 2 ) & 7 ];
                 dst[ dstOff++ ] = tab2[ ( v ) & 3 ];
             }
         }
     }
 }
