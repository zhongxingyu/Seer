 package com.centaurean.clmax.schema.devices;
 
 import com.centaurean.clmax.cache.CLQueryCache;
 import com.centaurean.clmax.schema.CL;
 import com.centaurean.clmax.schema.CLObject;
 import com.centaurean.clmax.schema.exceptions.CLException;
 import com.centaurean.clmax.schema.exceptions.CLNativeException;
 import com.centaurean.clmax.schema.values.CLValue;
 import com.centaurean.clmax.schema.versions.CLVersion;
 import com.centaurean.clmax.schema.versions.exceptions.CLVersionException;
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
  * 23/03/13 22:57
  * @author gpnuma
  */
 public class CLDevice extends CLObject {
     private CLVersion version;
 
     public CLDevice(long pointer) {
         super(pointer);
     }
 
     private CLValue get(CLDeviceInfo deviceInfo) {
         if (deviceInfo != CLDeviceInfo.CL_DEVICE_VERSION)
             if (getVersion().compareTo(deviceInfo.getMinimumCLVersion()) < 0)
                 throw new CLVersionException(deviceInfo.name() + " (" + deviceInfo.getMinimumCLVersion().majorMinor() + " function) not supported by this " + getVersion().majorMinor() + " device.");
         CLValue valueInCache = CLQueryCache.get(getPointer(), deviceInfo);
         if (valueInCache == null) {
             switch (deviceInfo.getReturnType()) {
                 case BOOLEAN:
                 case INT:
                 case LONG:
                 case SIZE_T:
                 case BIT_FIELD:
                     valueInCache = new CLValue(CL.getDeviceInfoLongNative(getPointer(), deviceInfo.getKey()));
                     break;
                 case CHAR_ARRAY:
                     valueInCache = new CLValue(CL.getDeviceInfoStringNative(getPointer(), deviceInfo.getKey()));
                     break;
                 case LONG_ARRAY:
                 case SIZE_T_ARRAY:
                     valueInCache = new CLValue(CL.getDeviceInfoLongArrayNative(getPointer(), deviceInfo.getKey()));
                     break;
             }
             CLQueryCache.add(getPointer(), deviceInfo, valueInCache);
         }
         return valueInCache;
     }
 
     public CLVersion getVersion() {
         if (version == null)
             version = CLVersion.parse(get(CLDeviceInfo.CL_DEVICE_VERSION).getString());
         return version;
     }
 
     @Override
     public int hashCode() {
         return (int) getPointer();
     }
 
     @Override
     public boolean equals(Object object) {
         if (object == null)
             return false;
         if (object == this)
             return true;
         if (!(object instanceof CLDevice))
             return false;
         CLDevice device = (CLDevice) object;
         return (getPointer() == device.getPointer());
     }
 
     @Override
     public String toString() {
         StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{pointer='0x").append(Long.toHexString(getPointer())).append(" (").append(getPointer()).append(")'");
         for (CLDeviceInfo deviceInfo : CLDeviceInfo.values()) {
             if (getVersion().compareTo(deviceInfo.getMinimumCLVersion()) > 0)
                 try {
                     stringBuilder.append(", ").append(deviceInfo.name()).append("='").append(get(deviceInfo));
                 } catch (CLNativeException exception) {
                    Log.message(new CLException("Querying device info " + deviceInfo.name() + " returned error " + exception.getMessage()));
                 } finally {
                     stringBuilder.append("'");
                 }
         }
         return stringBuilder.append("}").toString();
     }
 }
