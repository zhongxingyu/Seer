 /**
  * org.funcish: functional utilities for Java
  * 
  * Copyright 2013 Robin Kirkman.
  * 
  * Released under a BSD license.
  * 
  * Copyright (c) 2013, Robin Kirkman
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted 
  * provided that the following conditions are met:
  * 
  *  o  Redistributions of source code must retain the above copyright notice, this list of conditions 
  *     and the following disclaimer.
  *  o  Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
  *     and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *  o  Neither the name of org.funcish nor the names of its contributors may be used to endorse 
  *     or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.funcish.clj;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.funcish.core.fn.Function;
 import org.funcish.core.impl.AbstractReducer;
 import org.funcish.core.impl.Proxied;
 
 public class ClojurelikeProxyReducer extends AbstractReducer<Object, Object> implements Proxied<Function<?>> {
 
 	private Function<?> fn;
 	
 	public ClojurelikeProxyReducer(Function<?> fn) {
 		super(Object.class, Object.class, null);
 		this.fn = fn;
 	}
 
 	@Override
 	public Object reduce0(Object memo, Object obj, Integer index)
 			throws Exception {
 		return fn.call(fn.args(new Object[] {memo, obj, index}));
 	}
 	
 	@Override
	public Object over(Collection<?> c) {
 		Iterator<?> i = c.iterator();
 		Object first = i.next();
 		Collection<Object> rest = new ArrayList<Object>();
 		while(i.hasNext())
 			rest.add(i.next());
 		return innerOver(first, rest);
 	}
 	
 	@Override
 	public Function<?> proxiedTarget() {
 		return fn;
 	}
 
 }
