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
 
import org.funcish.core.util.Mappings;
import org.funcish.core.util.Predicates;
import org.funcish.core.util.Reducers;
 import org.funcish.core.fn.Function;
 import org.funcish.core.fn.Mapper;
 import org.funcish.core.fn.Mapping;
 import org.funcish.core.fn.ParaMapper;
 import org.funcish.core.fn.ParaPredicator;
 import org.funcish.core.fn.ParaReducer;
 import org.funcish.core.fn.Predicate;
 import org.funcish.core.fn.Reducer;
 import org.funcish.core.fn.Reduction;
 
 import clojure.lang.IFn;
 
 public class FromClojure {
 	
 	public static Function<Object> function(IFn fn) {
 		return function(fn, null);
 	}
 	
 	public static Function<Object> function(IFn fn, Long arity) {
 		return new IFnProxyFunction(fn, arity == null ? null : arity.intValue());
 	}
 	
 	public static Function<Boolean> bfunction(IFn fn) {
 		return bfunction(fn, null);
 	}
 	
 	public static Function<Boolean> bfunction(IFn fn, Long arity) {
 		return new IFnProxyBooleanFunction(fn, arity == null ? null : arity.intValue());
 	}
 
 	public static Function<Boolean> bfunction(IFn fn, long arity) {
 		return new IFnProxyBooleanFunction(fn, (int) arity);
 	}
 
 	public static Mapping<Object, Object> mapping(IFn fn) {
 		return Mappings.mapping(Object.class, function(fn, 1L));
 	}
 	
 	public static Mapper<Object, Object> mappicator(IFn fn) {
 		return Mappings.mapper(mapping(fn));
 	}
 	
 	public static ParaMapper<Object, Object> paraMappicator(IFn fn) {
 		return Mappings.paraMapper(mapping(fn));
 	}
 	
 	public static Reduction<Object, Object> reducer(IFn fn) {
 		return Reducers.reduction(Object.class, function(fn, 2L));
 	}
 	
 	public static Reduction<Object, Object> reducer(IFn fn, Object memoStart) {
 		return Reducers.reduction(Object.class, memoStart, function(fn, 2L));
 	}
 	
 	public static Reducer<Object, Object> reducator(IFn fn) {
 		return Reducers.reducer(reducer(fn));
 	}
 
 	public static Reducer<Object, Object> reducator(IFn fn, Object memoStart) {
 		return Reducers.reducer(reducer(fn, memoStart));
 	}
 	
 	public static ParaReducer<Object, Object> paraReducator(IFn fn, IFn collator) {
 		return Reducers.paraReducer(reducer(fn), reducer(collator));
 	}
 
 	public static Predicate<Object> predicate(IFn fn) {
 		return Predicates.predicate(Object.class, bfunction(fn, 1));
 	}
 	
 	public static Predicate<Object> predicator(IFn fn) {
		return Predicates.predicator(Object.class, predicate(fn));
 	}
 	
 	public static ParaPredicator<Object> paraPredicator(IFn fn) {
		return Predicates.paraPredicator(Object.class, predicate(fn));
 	}
 	
 	private FromClojure() {}
 }
