 /*
  *  Copyright 2013 Lukas Karas, Avast a.s. <karas@avast.com>
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package com.avast;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 
 public class ByteBufferBackedInputStream extends InputStream {
 
     private final ByteBuffer buff;
 
     public ByteBufferBackedInputStream(ByteBuffer buff){
         this.buff = buff;
     }
 
     @Override
     public synchronized int read() throws IOException {
       if (!buff.hasRemaining()) {
         return -1;
       }
       return ((int)buff.get()) & 0x000000ff; // default conversion from byt to integer expand highest bit, returned integer can be negative...
     }
 
     @Override
     public synchronized int read(byte[] b, int off, int len) throws IOException {
         int rem = buff.remaining();
         if (rem == 0)
             return -1;
         int toRead = Math.min(len, rem);
        buff.get(b, off, toRead);
         return toRead;
     }
 }
