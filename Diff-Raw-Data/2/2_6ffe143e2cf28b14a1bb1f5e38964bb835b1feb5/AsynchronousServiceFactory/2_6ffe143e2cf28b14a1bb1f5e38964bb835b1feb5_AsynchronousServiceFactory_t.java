 /*
  * Copyright (c) 2009, Julian Gosnell
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *     * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above
  *     copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided
  *     with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.dada.core;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.Arrays;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.locks.Lock;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A ServiceFactory that returns a proxy for its target, invocations upon which are queued and executed on another thread.
  * It may be sensible to execute invocations returning values directly on the calling Thread.
  *  
  * @author jules
  *
  * @param <T>
  */
 public class AsynchronousServiceFactory<T> implements ServiceFactory<T> {
 
 	private final Logger log = LoggerFactory.getLogger(AsynchronousServiceFactory.class);
 	
 	private final ExecutorService executorService;
 	private final Class<?>[] interfaces;
 	private final Lock lock;
 	
 	public AsynchronousServiceFactory(Class<?>[] interfaces, ExecutorService executorService, Lock lock) {
		this.interfaces = Arrays.copyOf(interfaces, interfaces.length);
 		this.executorService = executorService;
 		this.lock = lock;
 	}
 
 	protected class Invocation implements Runnable {
 
 		private final T target;
 		private final Method method;
 		private final Object[] args;
 		
 		protected Invocation(T target, Method method, Object[] args) {
 			lock.lock();
 			this.target = target;
 			this.method = method;
 			this.args = args;
 		}
 		
 		@Override
 		public void run() {
 			try {
 				method.invoke(target, args);
 			} catch (Throwable t) {
 				log.error("problem during async invocation ({})", this, t);
 			} finally {
 				lock.unlock();
 			}
 		}
 		
 		@Override
 		public String toString() {
 			return "<" + getClass().getSimpleName() + ":" + target + "." + method + ": " + Arrays.toString(args) + ">";
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public T decouple(final T target) {
 		InvocationHandler handler = new InvocationHandler() {
 			@Override
 			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 				if (method.getReturnType() == Void.TYPE) {
 					// async invocation
 					executorService.execute(new Invocation(target, method, args));
 					return null;
 				} else {
 					// sync invocation
 					return method.invoke(method.getDeclaringClass().equals(Object.class) ? this : target, args);
 				}
 			}
 			
 			@Override
 			public String toString() {
 				return "<" + getClass().getEnclosingClass().getSimpleName() + "." + getClass().getInterfaces()[0].getSimpleName() + ">";
 			}
 		};
 		return (T)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, handler);
 	}
 
 	@Override
 	public T client(String endPoint) throws Exception {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("NYI");
 	}
 	
 	@Override
 	public void server(T target, String endPoint) throws Exception {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("NYI");
 	}
 
 	// used to expose Invocation to test harness
 	protected Invocation createInvocation(T target, Method method, Object[] args) {
 		return new Invocation(target, method, args);
 	}
 
 }
