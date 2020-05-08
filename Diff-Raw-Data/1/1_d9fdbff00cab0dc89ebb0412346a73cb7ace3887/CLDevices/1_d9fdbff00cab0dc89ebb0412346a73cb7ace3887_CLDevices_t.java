 package com.centaurean.clmax.schema.devices;
 
 import com.centaurean.commons.utilities.Transform;
 
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
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
  * CLmax
  *
  * 23/03/13 22:35
  * @author gpnuma
  */
 public class CLDevices extends Hashtable<Long, CLDevice> {
     private CLDeviceType type;
     private HashSet<CLDevice> ignored;
 
     public CLDevices(CLDeviceType type) {
         super();
         ignored = new HashSet<CLDevice>();
        this.type = type;
     }
 
     public boolean add(CLDevice device) {
         CLDevice found = put(device.getPointer(), device);
         return found == null;
     }
 
     public long[] getPointers() {
         return Transform.toLongArray(keySet());
     }
 
     public CLDeviceType getType() {
         return type;
     }
 
     public void ignore(CLDevice device) {
         if(remove(device.getPointer()) == null)
             throw new NoSuchElementException();
         ignored.add(device);
     }
 
     public void reinstate(CLDevice device) {
         if(ignored.remove(device))
             add(device);
     }
 
     public Set<CLDevice> getIgnored() {
         return ignored;
     }
 }
