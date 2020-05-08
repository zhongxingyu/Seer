 package com.centaurean.clmax.schema.mem.buffers;
 
 import com.centaurean.clmax.schema.CL;
 import com.centaurean.clmax.schema.exceptions.CLException;
 import com.centaurean.clmax.schema.mem.CLMapType;
 import com.centaurean.clmax.schema.mem.CLMem;
 import com.centaurean.clmax.schema.queues.CLCommandQueue;
 
 import java.nio.ByteBuffer;
 
 /*
  * Copyright (c) 2013, Centaurean
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of Centaurean nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL Centaurean BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
 * jetFlow
  *
  * 14/04/13 02:53
  * @author gpnuma
  */
 public class CLBuffer extends CLMem {
     private ByteBuffer hostBuffer;
 
     public CLBuffer(long pointerBuffer, ByteBuffer hostBuffer) {
         super(pointerBuffer);
         this.hostBuffer = hostBuffer;
     }
 
     public void map(CLCommandQueue commandQueue, CLMapType mapType) {
         if(commandQueue.getDevice().getVersion().compareTo(mapType.getMinimumCLVersion()) < 0)
             throw new CLException("Map type " + mapType.name() + " requires an " + mapType.getMinimumCLVersion().majorMinor() + " compatible device !");
         CL.mapBufferNative(commandQueue.getPointer(), getPointer(), mapType.getValue(), getHostBuffer().capacity());
     }
 
     public void unmap(CLCommandQueue commandQueue) {
         CL.unmapMemObjectNative(commandQueue.getPointer(), getPointer(), getHostBuffer());
     }
 
     public void release() {
         CL.releaseMemObjectNative(getPointer());
     }
 
     public ByteBuffer getHostBuffer() {
         return hostBuffer;
     }
 
     @Override
     public String toString() {
         StringBuilder stringBuilder = new StringBuilder();
         stringBuilder.append(super.toString()).append(" {").append(getHostBuffer()).append("}");
         return stringBuilder.toString();
     }
 }
