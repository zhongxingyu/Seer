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
 package org.jagatoo.loaders.textures;
 
 import org.jagatoo.loaders.textures.pixelprocessing.PixelProcessor;
 
 /**
  * Generates Mipmaps for a Texture.
  * 
  * @author Marvin Froehlich (aka Qudus)
  */
 public class MipmapGenerator
 {
     /**
      * Creates mipmaps from the given {@link AbstractTextureImage}.<br>
      * 
      * @param ti0 the base-image
      * @param tex the target-texture.
      */
     public static void createMipMaps( AbstractTextureImage ti0, AbstractTexture tex, TextureFactory texFactory )
     {
         int width = ti0.getWidth();
         int height = ti0.getHeight();
         
        int numLevels = Integer.numberOfTrailingZeros( Math.max( width, height ) ) + 1;
        
        /*
         * For some ugly texture sizes the above algorithm will compute a wrong level-count.
         */
        if ( ( ( width >> numLevels ) > 0 ) || ( ( height >> numLevels ) > 0 ) )
        {
            numLevels++;
        }
         
         final PixelProcessor pp = PixelProcessor.selectPixelProcessor( ti0.getFormat() );
         
         int lineSize = pp.getLineSize( width );
         
         AbstractTextureImage ti = ti0;
         
         for ( int level = 1; level < numLevels; level++ )
         {
             /*
             // Limit to 8 to avoid unecessarily small mipmaps!
             if ( ( width <= 8 ) || ( height <= 8 ) )
             {
                 break;
             }
             */
             
             // rounding based on OpenGL rule.
             width = Math.max( 1, width >> 1 );
             height = Math.max( 1, height >> 1 );
             lineSize = pp.getLineSize( width );
             
             ti = pp.calcMipMap( ti, 0, width, height, lineSize, texFactory );
             tex.setImage( level, ti );
         }
     }
 }
