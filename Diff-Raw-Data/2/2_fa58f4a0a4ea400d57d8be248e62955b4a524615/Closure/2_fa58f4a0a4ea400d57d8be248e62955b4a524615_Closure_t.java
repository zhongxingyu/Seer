 //Copyright 2012-2013 Joshua Scoggins. All rights reserved.
 //
 //Redistribution and use in source and binary forms, with or without modification, are
 //permitted provided that the following conditions are met:
 //
 //   1. Redistributions of source code must retain the above copyright notice, this list of
 //      conditions and the following disclaimer.
 //
 //   2. Redistributions in binary form must reproduce the above copyright notice, this list
 //      of conditions and the following disclaimer in the documentation and/or other materials
 //      provided with the distribution.
 //
 //THIS SOFTWARE IS PROVIDED BY Joshua Scoggins ``AS IS'' AND ANY EXPRESS OR IMPLIED
 //WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 //FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Joshua Scoggins OR
 //CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 //CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 //SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 //ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 //NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 //ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 //The views and conclusions contained in the software and documentation are those of the
 //authors and should not be interpreted as representing official policies, either expressed
 //or implied, of Joshua Scoggins. 
 package com.dritanium.delegates;
 /**
 * Represents a layer of indirection that is used to provide access to variables 
  * outside a given instance. 
  * @author Joshua Scoggins 
  * @param <T>  The type of the value stored in the Closure.
  */
 public class Closure<T> implements Cloneable{
 
 	private T value;
 
 	public T getValue() {
 		return value;
 	}
 
 	public void setValue(T value) {
 		this.value = value;
 	}
 
 	public Closure(T value) {
 		setValue(value);
 	}
 	/**
 	 * Does a shallow copy of the current closure. The value stored in the
 	 * closure is not cloned. 
 	 * @return A shallow copy of the Closure.
 	 */
 	public Object clone() {
 		return new Closure(this.getValue());
 	}
 
 	@Override
 	public String toString() {
 	   return value == null ? super.toString() : value.toString();
 	}
 }
