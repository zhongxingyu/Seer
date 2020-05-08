 /**
  * Copyright (c) 2007-2008, JAGaToo Project Group all rights reserved.
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
 package org.jagatoo.loaders.textures.pixelprocessing;
 
 import java.awt.image.BufferedImage;
 import java.nio.ByteBuffer;
 
 import org.jagatoo.loaders.textures.AbstractTextureImage;
 import org.jagatoo.loaders.textures.TextureFactory;
 import org.jagatoo.opengl.enums.TextureFormat;
 import org.jagatoo.opengl.enums.TextureImageFormat;
 
 /**
  * Pixel Processor.
  * 
  * @author Matthias Mann
  * @author Marvin Froehlich (aka Qudus);
  */
 public abstract class PixelProcessor
 {
     private final int pixelSize;
     
     public final int getPixelSize()
     {
         return( pixelSize );
     }
     
     public final int getLineSize( int width )
     {
         return( getPixelSize() * width );
     }
     
     public final int getStartOffset( int x, int y, int width )
     {
         return( ( x * getPixelSize() ) + ( y * getLineSize( width ) ) );
     }
     
     public abstract TextureImageFormat getTextureImageFormat();
     
     public abstract int readImageData( BufferedImage img, int startX, int startY, int width, int height, ByteBuffer trg, int trgOffset, boolean flipVertically );
     
     public AbstractTextureImage createTextureImage( BufferedImage img, int orgWidth, int orgHeight, TextureFormat format, boolean flipVertically, TextureFactory texFactory )
     {
         int width = img.getWidth();
         int height = img.getHeight();
         
         AbstractTextureImage ti = texFactory.createTextureImage( width, height, orgWidth, orgHeight, pixelSize, format.getDefaultTextureImageFormat() );
         
         ByteBuffer bb = ti.getDataBuffer();
         
         readImageData( img, 0, 0, width, height, bb, 0, flipVertically );
         
         return( ti );
     }
     
     /**
      * Note:
      * This can work inplace, if srcOff == trgOff and srcLineSize >= trgLineSize.
      */
     public AbstractTextureImage calcMipMap( AbstractTextureImage srcImg, int trgOffset, int trgWidth, int trgHeight, int trgLineSize, TextureFactory texFactory )
     {
         // substract one to prevent ArrayIndexOutOfBoundsException for odd
         // widths and heights.
         final int srcWidth = srcImg.getWidth() - 1;
         final int srcHeight = srcImg.getHeight() - 1;
         final int pixelSize = srcImg.getPixelSize();
         final int srcLineSize = srcImg.getWidth() * srcImg.getPixelSize();
         int srcOffset = 0;
         
         ByteBuffer src = srcImg.getDataBuffer();
         
         AbstractTextureImage trgImg = texFactory.createTextureImage( trgWidth, trgHeight, trgWidth, trgHeight, pixelSize );
         ByteBuffer trg = trgImg.getDataBuffer();
         final int trgOffset0 = trgOffset;
         trg.position( trgOffset0 );
         trg.limit( trg.capacity() );
         
         for ( int y = 0; y < srcHeight; y += 2 )
         {
             int trgOffsetX = trgOffset;
             int srcOffsetX = srcOffset;
             
             for ( int x = 0; x < srcWidth; x += 2, srcOffsetX += pixelSize )
             {
                 for ( int i = 0; i < pixelSize; ++i, srcOffsetX++ )
                 {
                     byte value = (byte)( ( ( src.get( srcOffsetX ) & 0xFF ) +
                                            ( src.get( srcOffsetX + pixelSize ) & 0xFF ) +
                                            ( src.get( srcOffsetX + srcLineSize ) & 0xFF ) +
                                            ( src.get( srcOffsetX + srcLineSize + pixelSize ) & 0xFF )
                                          ) >> 2
                                        );
                     trg.put( trgOffsetX++, value );
                 }
             }
             
             trgOffset += trgLineSize;
             srcOffset += srcLineSize << 1;
         }
         
         int dataSize = trgHeight * trgLineSize;
         
         trg.position( 0 );
         trg.limit( trgOffset0 + dataSize );
         
         return( trgImg );
     }
     
     protected PixelProcessor( int pixelSize )
     {
         if ( ( pixelSize < 1 ) || ( pixelSize > 4 ) )
         {
             throw( new IllegalArgumentException( "Unsupported pixelSize: " + pixelSize ) );
         }
         
         this.pixelSize = pixelSize;
     }
     
     public static final PixelProcessor selectPixelProcessor( TextureImageFormat tiFormat )
     {
         switch ( tiFormat )
         {
             case INTENSITY:
             case LUMINANCE:
             case ALPHA:
                 return( PixelProcessorLUM.getInstance() );
                 
             case RGB:
                 return( PixelProcessorRGB.getInstance() );
                 
             case RGBA:
                 return( PixelProcessorRGBA.getInstance() );
                 
             default:
                 throw new Error( "PixelProcessor for format " + tiFormat + " not (yet) available" );
         }
     }
     
     public static final PixelProcessor selectPixelProcessor( TextureFormat texFormat )
     {
         switch ( texFormat )
         {
             case LUMINANCE:
             case DEPTH:
                 return( PixelProcessorLUM.getInstance() );
                 
             case RGB:
                 return( PixelProcessorRGB.getInstance() );
                 
             case RGBA:
                 return( PixelProcessorRGBA.getInstance() );
                 
             default:
                 throw new Error( "PixelProcessor for format not (yet) available" );
         }
     }
     
     /**
      * @return the best fitting PixelProcessor based on the BufferedImage format
      *         and the given format.
      * 
      * @param img A BufferedImage on which the decision should be based.
      * @param texFormat The desired format.
      * 
      * @return A PixelProcessor that can work with the given BufferedImage.
      * 
      * @throws IllegalArgumentException if the format is unknown.
      */
     public static final PixelProcessor selectPixelProcessor( BufferedImage img, TextureFormat texFormat )
     {
         boolean imgHasAlpha = img.getColorModel().hasAlpha();
         boolean imgIsGrey = img.getColorModel().getNumColorComponents() == 1;
         
         switch ( texFormat )
         {
             case LUMINANCE:
             case DEPTH:
                 return( PixelProcessorLUM.getInstance() );
                 
             case RGB:
                 if ( imgIsGrey )
                 {
                     return( PixelProcessorLUM.getInstance() );
                 }
                 else
                 {
                     return( PixelProcessorRGB.getInstance() );
                 }
                 
             case RGBA:
                 if ( imgHasAlpha )
                 {
                     return( PixelProcessorRGBA.getInstance() );
                 }
                 else if ( imgIsGrey )
                 {
                     return( PixelProcessorLUM.getInstance() );
                 }
                 else
                 {
                     return( PixelProcessorRGB.getInstance() );
                 }
                 
             default:
                 throw new IllegalArgumentException( "Unsupported format: " + texFormat );
         }
     }
     
     /**
      * @return the best fitting PixelProcessor based on the BufferedImage format
      *         and the given format.
      * 
      * @param img A BufferedImage on which the decision should be based.
      * @param acceptAlpha is an alpha channel accepted
      * 
      * @return A PixelProcessor that can work with the given BufferedImage.
      * 
      * @throws IllegalArgumentException if the format is unknown.
      */
     public static final PixelProcessor selectPixelProcessor( BufferedImage img, boolean acceptAlpha )
     {
         boolean imgHasAlpha = img.getColorModel().hasAlpha();
         boolean imgIsGrey = img.getColorModel().getNumColorComponents() == 1;
         
         if ( acceptAlpha )
         {
             if ( imgHasAlpha )
             {
                 return( PixelProcessorRGBA.getInstance() );
             }
             else if ( imgIsGrey )
             {
                 return( PixelProcessorLUM.getInstance() );
             }
             
             return( PixelProcessorRGB.getInstance() );
         }
         
         if ( imgIsGrey )
         {
             return( PixelProcessorLUM.getInstance() );
         }
         
         return( PixelProcessorRGB.getInstance() );
     }
 }
