 /*
  * Copyright (c) 2000
  *      Jon Schewe.  All rights reserved
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  *
  * I'd appreciate comments/suggestions on the code jpschewe@mtu.net
  */
 package net.mtu.eggplant.util;
 
 /**
  * class to put multiple objects in one.
  * 
  * @version $Revision$
 * @param <TYPE_ONE> the type of the first object 
 * @param <TYPE_TWO> the type of the second object
  */
 public class Pair<TYPE_ONE, TYPE_TWO> extends Object {
 
   public Pair(final TYPE_ONE one,
               final TYPE_TWO two) {
     _one = one;
     _two = two;
   }
 
   private TYPE_ONE _one = null;
 
   public TYPE_ONE getOne() {
     return _one;
   }
 
   private TYPE_TWO _two = null;
 
   public TYPE_TWO getTwo() {
     return _two;
   }
 
   @Override
   public String toString() {
     return "[Pair one:" + getOne() + " two: " + getTwo() + "]";
   }
 
   /**
    * Equality is defined by the equality of the objects in the Pair.
    **/
   @Override
   public boolean equals(final Object o) {
     if (o == this) {
       return true;
     } else if (o instanceof Pair) {
       final Pair<?, ?> other = (Pair<?, ?>) o;
       return Functions.safeEquals(other.getOne(), getOne())
       && Functions.safeEquals(other.getTwo(), getTwo());
     }
     return false;
   }
 
   @Override
   public int hashCode() {
     if (getOne() == null) {
       return -1;
     } else {
       return getOne().hashCode();
     }
   }
 
 }
