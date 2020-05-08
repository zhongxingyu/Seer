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
 package org.jagatoo.loaders.models.md3;
 
 import java.io.IOException;
 
 import org.jagatoo.loaders.IncorrectFormatException;
 import org.jagatoo.util.streams.LittleEndianDataInputStream;
 
 /**
  * Insert type comment here.
  * 
  * @author Marvin Froehlich (aka Qudus)
  */
 public class MD3Header
 {
     public static final int MAGIC_NUMBER = 0x33504449;
     
     public final int version;
     public final String name;
     public final int flags;
     public final int numFrames;
     public final int numTags;
     public final int numSurfaces;
     public final int numSkins;
     public final int frameOffset;
     public final int tagOffset;
     public final int surfaceOffset;
     public final int fileOffset;
     
     private MD3Header( LittleEndianDataInputStream in ) throws IOException, IncorrectFormatException
     {
         int ident = in.readInt();
         if ( ident != MAGIC_NUMBER )
             throw new IncorrectFormatException( "This is not an MD3 file!" );
         
         version = in.readInt();
         if ( version != 15 )
             throw new IncorrectFormatException( "Unsupported MD3 version " + version + ". Currently ony version 15 is supported." );
         
        name = MD3File.fixPath( in.readCString( 64, true ) );
         flags = in.readInt();
         numFrames = in.readInt();
         numTags = in.readInt();
         numSurfaces = in.readInt();
         numSkins = in.readInt();
         frameOffset = in.readInt();
         tagOffset = in.readInt();
         surfaceOffset = in.readInt();
         fileOffset = in.readInt();
     }
     
     public static final MD3Header readHeader( LittleEndianDataInputStream in ) throws IOException, IncorrectFormatException
     {
         return( new MD3Header( in ) );
     }
 }
