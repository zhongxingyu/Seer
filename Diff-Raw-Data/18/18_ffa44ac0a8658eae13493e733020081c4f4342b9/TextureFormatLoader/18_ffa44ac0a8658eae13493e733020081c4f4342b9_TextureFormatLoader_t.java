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
 import java.io.IOException;
 
 import org.jagatoo.loaders.textures.AbstractTexture;
 import org.jagatoo.loaders.textures.TextureFactory;
 
 /**
  * Loads a Texture stream.<br>
  * <br>
  * <b>Important</b>: No implementation of TextureStreamLoader may ever call
  * mark() on the InputStream.
  * 
  * @author Matthias Mann
  */
 public interface TextureFormatLoader
 {
     /**
      * Tries to load the Texture from the InputStream.<br>
      * <br>
      * <b>Important</b>: No implementation of TextureStreamLoader may ever call
      * mark() on the InputStream <i>in</i>.
      * 
      * @param in the InputStream to load the Texture from
      * @param acceptAlpha
     * @param flipVertically
      * @param loadMipmaps
      * @param allowStreching If true, the image is streched to power-of-two width and height, if necessary. 
      * @param texFactory
      * 
      * @return the Texture, loaded from the InputStream or <i>null</i>, if the
      *         loader wasn't able to decode the stream.
      * 
      * @throws IOException
      */
    public AbstractTexture loadTexture( BufferedInputStream in, boolean acceptAlpha, boolean flipVertically, boolean loadMipmaps, boolean allowStreching, TextureFactory texFactory ) throws IOException;
 }
