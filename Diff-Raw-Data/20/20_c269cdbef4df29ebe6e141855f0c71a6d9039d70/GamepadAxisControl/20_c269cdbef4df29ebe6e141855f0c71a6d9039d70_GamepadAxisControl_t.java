 /*
  * Copyright (c) 2013 devnewton <devnewton@bci.im>
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * * Redistributions of source code must retain the above copyright
  *   notice, this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright
  *   notice, this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  *
  * * Neither the name of 'devnewton <devnewton@bci.im>' nor the names of
  *   its contributors may be used to endorse or promote products derived
  *   from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package im.bci.lwjgl.nuit.controls;
 
 import org.lwjgl.input.Controller;
 
 public class GamepadAxisControl implements Control {
     private final Controller pad;
     private final int axis;
     private float scale;
 
     public GamepadAxisControl(Controller pad, int axis, boolean positive) {
         this.pad = pad;
         this.axis = axis;
         this.scale = positive ? 1.0f : -1.0f;
     }
 
     @Override
     public String getName() {
        return pad.getAxisName(axis)+ (scale > 0.0f ? '+' : '-');
     }
 
     @Override
     public float getDeadZone() {
         return pad.getDeadZone(axis);
     }
 
     @Override
     public float getValue() {
         return Math.max(0.0f, pad.getAxisValue(axis) * scale);
     }
 
     @Override
     public String getControllerName() {
        return pad.getName();
     }
 
 }
