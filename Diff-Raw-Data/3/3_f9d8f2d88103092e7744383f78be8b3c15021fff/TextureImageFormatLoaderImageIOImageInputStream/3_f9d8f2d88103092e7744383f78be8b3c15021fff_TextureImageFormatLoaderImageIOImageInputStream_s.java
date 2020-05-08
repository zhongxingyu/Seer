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
 package org.jagatoo.loaders.textures.formats;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.util.Iterator;
 
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReadParam;
 import javax.imageio.ImageReader;
 import javax.imageio.ImageTypeSpecifier;
 import javax.imageio.stream.ImageInputStream;
 
 import org.jagatoo.image.SharedBufferedImage;
 import org.jagatoo.loaders.textures.AbstractTextureImage;
 import org.jagatoo.loaders.textures.TextureFactory;
 import org.jagatoo.util.image.ImageUtility;
 
 /**
  * This {@link TextureImageFormatLoader} is used as a fallback-loader,
  * if not other loader was capable of loading the requested image format.
  * 
  * @author Matthias Mann
  * @author Marvin Froehlich (aka Qudus)
  */
 public class TextureImageFormatLoaderImageIOImageInputStream implements TextureImageFormatLoader
 {
     private static SharedBufferedImage loadFromStream( InputStream in, boolean acceptAlpha ) throws IOException
     {
         SharedBufferedImage bi = null;
         
         ImageInputStream stream = ImageIO.createImageInputStream( in );
         Iterator< ImageReader > iter = ImageIO.getImageReaders( stream );
         
         if ( iter.hasNext() )
         {
             ImageReader reader = iter.next();
             
             ImageReadParam param = reader.getDefaultReadParam();
             reader.setInput( stream, true, true );
             int iw = reader.getWidth( 0 );
             int ih = reader.getHeight( 0 );
             
             SharedBufferedImage dst = null;
             
             ImageTypeSpecifier its = reader.getImageTypes( 0 ).next();
             
             //int numBands = its.next().getNumBands();
             int numChannels = its.getColorModel().getNumComponents();
             //int pixelSize = its.getColorModel().getPixelSize();
             //int colorSpaceType = its.getColorModel().getColorSpace().getType();
             boolean hasAlpha = its.getColorModel().hasAlpha() && acceptAlpha;
             
             dst = SharedBufferedImage.create( iw, ih, numChannels, hasAlpha, null, null );
             
             param.setDestination( dst );
             bi = (SharedBufferedImage)reader.read( 0, param );
             
             stream.close();
             reader.dispose();
         }
         else
         {
             stream.close();
         }
         
         return( bi );
     }
     
     private static AbstractTextureImage createTextureImage( SharedBufferedImage img, boolean acceptAlpha, boolean flipVertically, boolean allowStreching, TextureFactory texFactory ) throws IOException
     {
         final int orgWidth = img.getWidth();
         final int orgHeight = img.getHeight();
         
         final int width;
         final int height;
         
         if ( allowStreching )
         {
             width = ImageUtility.roundUpPower2( img.getWidth() );
             height = ImageUtility.roundUpPower2( img.getHeight() );
         }
         else
         {
             width = img.getWidth();
             height = img.getHeight();
         }
         
         final boolean alpha = img.getColorModel().hasAlpha() && acceptAlpha;
         
         if ( ( orgWidth != width ) || ( orgHeight != height ) )
         {
             img = ImageUtility.scaleImage( img, width, height, alpha );
         }
         
         AbstractTextureImage ti = texFactory.createTextureImage( width, height, orgWidth, orgHeight, img.getPixelSize() );
         
         ByteBuffer bb = ti.getDataBuffer();
         bb.limit( bb.capacity() );
         
         byte[] imageData = img.getSharedData();
         
         switch ( img.getPixelSize() )
         {
             case 4:
                 if ( flipVertically )
                 {
                     int lineSize = width * 4;
                     for ( int y = height - 1; y >= 0; y-- )
                     {
                         for ( int x = 0; x < lineSize; x += 4 )
                         {
                             int i = ( y * lineSize ) + x;
                             
                             bb.put( imageData[i + 3] );
                             bb.put( imageData[i + 2] );
                             bb.put( imageData[i + 1] );
                             bb.put( imageData[i + 0] );
                         }
                     }
                 }
                 else
                 {
                     for ( int i = 0; i < imageData.length; i += 4 )
                     {
                         bb.put( imageData[i + 3] );
                         bb.put( imageData[i + 2] );
                         bb.put( imageData[i + 1] );
                         bb.put( imageData[i + 0] );
                     }
                 }
                 break;
             case 3:
                 if ( flipVertically )
                 {
                     int lineSize = width * 3;
                     for ( int y = height - 1; y >= 0; y-- )
                     {
                         for ( int x = 0; x < lineSize; x += 3 )
                         {
                             int i = ( y * lineSize ) + x;
                             
                             bb.put( imageData[i + 2] );
                             bb.put( imageData[i + 1] );
                             bb.put( imageData[i + 0] );
                         }
                     }
                 }
                 else
                 {
                     for ( int i = 0; i < imageData.length; i += 3 )
                     {
                         bb.put( imageData[i + 2] );
                         bb.put( imageData[i + 1] );
                         bb.put( imageData[i + 0] );
                     }
                 }
                 break;
             case 2:
                 if ( flipVertically )
                 {
                     int lineSize = width * 2;
                     for ( int y = height - 1; y >= 0; y-- )
                     {
                         for ( int x = 0; x < lineSize; x += 2 )
                         {
                             int i = ( y * lineSize ) + x;
                             
                             bb.put( imageData[i + 1] );
                             bb.put( imageData[i + 0] );
                         }
                     }
                 }
                 else
                 {
                     for ( int i = 0; i < imageData.length; i += 2 )
                     {
                         bb.put( imageData[i + 1] );
                         bb.put( imageData[i + 0] );
                     }
                 }
                 break;
             case 1:
                 if ( flipVertically )
                 {
                     int lineSize = width * 1;
                     for ( int y = height - 1; y >= 0; y-- )
                     {
                         for ( int x = 0; x < lineSize; x += 1 )
                         {
                             int i = ( y * lineSize ) + x;
                             
                             bb.put( imageData[i + 0] );
                         }
                     }
                 }
                 else
                 {
                     for ( int i = 0; i < imageData.length; i += 1 )
                     {
                         bb.put( imageData[i + 0] );
                     }
                 }
                 break;
         }
         
         bb.flip();
         
         return( ti );
     }
     
     /**
      * {@inheritDoc}
      */
     public AbstractTextureImage loadTextureImage( BufferedInputStream in, boolean acceptAlpha, boolean flipVertically, boolean allowStreching, TextureFactory texFactory ) throws IOException
     {
         SharedBufferedImage img = loadFromStream( in, acceptAlpha );
         
         AbstractTextureImage ti = createTextureImage( img, acceptAlpha, flipVertically, allowStreching, texFactory );
         
         return( ti );
     }
     
 }
