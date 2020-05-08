 //  ImmutableNamedFlags.java
 
 /*
  * Copyright (c) 2008, Gennady & Michael Kushnir
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  * 
  * 	•	Redistributions of source code must retain the above copyright notice, this
  * 		list of conditions and the following disclaimer.
  * 	•	Redistributions in binary form must reproduce the above copyright notice,
  * 		this list of conditions and the following disclaimer in the documentation
  * 		and/or other materials provided with the distribution.
  * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
  * 		to endorse or promote products derived from this software without specific 
  * 		prior written permission.
  * 		
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
  * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.rujel.reusables;
 
 import com.webobjects.foundation.*;
 
 public class ImmutableNamedFlags extends NamedFlags {
 	protected static final String immutable = " is Immutable";
 	
 	public ImmutableNamedFlags() {
 		super();
 	}
 	
 	public ImmutableNamedFlags(int source) {
 		super(source);
 	}
 	
 	public ImmutableNamedFlags (NSArray initKeys) {
 		super(initKeys);
 	}
 	
 	public ImmutableNamedFlags (Numerator initNum) {
 		super(initNum);
 	}
 	
 	public ImmutableNamedFlags (int source, NSArray initKeys) {
 		super(source,initKeys);
 	}
 	
 	public ImmutableNamedFlags (int source, Numerator initNum) {
 		super(source,initNum);
 	}
 
 	public void setFlagForKey(boolean flag, Object key) {
 		throw new UnsupportedOperationException(this.getClass().getName() + immutable);
 	}
 	
 	public void takeValueForKey(Object value, String key) {
 		throw new NSKeyValueCoding.UnknownKeyException(this.getClass().getName() + immutable,value,key);
 	}
 
 	public void setSyncParams(Object syncWith, Object syncMethod) {
 		throw new UnsupportedOperationException(this.getClass().getName() + immutable);
 	}
 	
 	public void setFlag(int reg, boolean value) {
 		throw new UnsupportedOperationException(this.getClass().getName() + immutable);
 	}
 	
 	public void sync() {
 		;
 	}
 	
 	public NamedFlags mutableClone() {
		return super.mutableClone();
 	}
 	
 	public ImmutableNamedFlags immutableClone() {
 		return this;
 	}
 	public Object clone () {
 		return this;
 	}
 	public ImmutableNamedFlags and(int otherFlags) {
 		return new ImmutableNamedFlags (flags & otherFlags,numerator);
 	}
 	
 	public ImmutableNamedFlags and(Number otherFlags) {
 		return and(otherFlags.intValue());
 	}
 	
 	public ImmutableNamedFlags or(int otherFlags) {
 		return new ImmutableNamedFlags (flags | otherFlags,numerator);
 	}
 	
 	public ImmutableNamedFlags or(Number otherFlags) {
 		return or(otherFlags.intValue());
 	}
 	
 	public ImmutableNamedFlags xor(int otherFlags) {
 		return new ImmutableNamedFlags (flags ^ otherFlags,numerator);
 	}
 	
 	public ImmutableNamedFlags xor(Number otherFlags) {
 		return xor(otherFlags.intValue());
 	}	
 }
