 /*
  * Copyright (c) 2009 Stephan Schloepke and innoQ Deutschland GmbH
  *
  * Stephan Schloepke: http://www.schloepke.de/
  * innoQ Deutschland GmbH: http://www.innoq.com/
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.jbasics.discover;
 
 import java.io.Serializable;
 
 import org.jbasics.checker.ContractCheck;
 import org.jbasics.pattern.delegation.ReleasableDelegate;
 
 public class DiscoverableImplementationDelegate<T> implements ReleasableDelegate<T>, Serializable {
 	private final Class<T> abstractClass;
 	private final Class<? extends T> defaultImpl;
 	private transient T instance;
 
 	public static <T> DiscoverableImplementationDelegate<T> newInstance(Class<T> abstractClass) {
 		return new DiscoverableImplementationDelegate<T>(abstractClass, null);
 	}
 
 	public static <T> DiscoverableImplementationDelegate<T> newInstance(Class<T> abstractClass, Class<? extends T> defaultImpl) {
 		return new DiscoverableImplementationDelegate<T>(abstractClass, defaultImpl);
 	}
 	
 	protected DiscoverableImplementationDelegate(Class<T> abstractClass, Class<? extends T> defaultImpl) {
 		this.abstractClass = ContractCheck.mustNotBeNull(abstractClass, "abstractClass");
 		this.defaultImpl = defaultImpl;
 	}
 
 	public T delegate() {
		if (this.instance != null) {
 			Class<? extends T> temp = ServiceClassDiscovery.discoverImplementation(this.abstractClass, this.defaultImpl);
 			if (temp == null) {
 				throw new RuntimeException("Cannot find an implementation for the abstract class " + this.abstractClass.getName());
 			}
 			try {
 				this.instance = temp.newInstance();
 			} catch (InstantiationException e) {
 				throw new RuntimeException("Cannot instantiate implmentation " + temp.getName(), e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("Cannot instantiate implmentation " + temp.getName(), e);
 			}
 		}
 		return this.instance;
 	}
 
 	public boolean release() {
 		this.instance = null;
 		return false;
 	}
 
 }
