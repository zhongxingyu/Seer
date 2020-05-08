 package com.centaurean.clmax.schema.contexts;
 
 import com.centaurean.clmax.cache.CLQueryCache;
 import com.centaurean.clmax.schema.CL;
 import com.centaurean.clmax.schema.CLObject;
 import com.centaurean.clmax.schema.exceptions.CLException;
 import com.centaurean.clmax.schema.exceptions.CLNativeException;
 import com.centaurean.clmax.schema.values.CLValue;
 import com.centaurean.commons.logs.Log;
 
 /*
  * Copyright (c) 2013, Centaurean software
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of Centaurean software nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL Centaurean software BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * jetFlow
  *
  * 24/03/13 17:37
  * @author gpnuma
  */
 public class CLContext extends CLObject {
     public CLContext(long pointer) {
         super(pointer);
     }
 
     private CLValue get(CLContextInfo contextInfo) {
         CLValue valueInCache = CLQueryCache.get(getPointer(), contextInfo);
         if (valueInCache == null) {
             switch (contextInfo.getReturnType()) {
                 case INT:
                     valueInCache = new CLValue(CL.getContextInfoLongNative(getPointer(), contextInfo.getKey()));
                     break;
                 case LONG_ARRAY:
                     valueInCache = new CLValue(CL.getContextInfoLongArrayNative(getPointer(), contextInfo.getKey()));
                     break;
             }
             CLQueryCache.add(getPointer(), contextInfo, valueInCache);
         }
         return valueInCache;
     }
 
     public void release() {
         CL.releaseContextNative(getPointer());
     }
 
     @Override
     public String toString() {
         StringBuilder stringBuilder = new StringBuilder();
         stringBuilder.append("{pointer='0x").append(Long.toHexString(getPointer())).append(" (").append(getPointer()).append(")'");
         for (CLContextInfo contextInfo : CLContextInfo.values())
             try {
                 stringBuilder.append(", ").append(contextInfo.name()).append("='").append(get(contextInfo)).append("'");
             } catch (CLNativeException exception) {
                Log.message(new CLException("Querying context info " + contextInfo.name() + " returned error " + exception.getMessage()));
             } finally {
                 stringBuilder.append("'");
             }
         return stringBuilder.append("}").toString();
     }
 }
