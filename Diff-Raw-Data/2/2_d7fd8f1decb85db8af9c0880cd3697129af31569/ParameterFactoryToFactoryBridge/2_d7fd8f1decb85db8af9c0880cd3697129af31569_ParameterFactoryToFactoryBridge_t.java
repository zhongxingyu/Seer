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
 package org.jbasics.pattern.factory;
 
 import org.jbasics.checker.ContractCheck;
 
 public class ParameterFactoryToFactoryBridge<InstanceType, ParameterType> implements Factory<InstanceType> {
 	private final ParameterFactory<InstanceType, ParameterType> factory;
 	private final ParameterType paramerter;
 
 	public static <T, P> Factory<T> create(final ParameterFactory<T, P> factory, final P param) {
 		return new ParameterFactoryToFactoryBridge<T, P>(factory, param);
 	}
 
 	public ParameterFactoryToFactoryBridge(final ParameterFactory<InstanceType, ParameterType> factory, final ParameterType parameter) {
		this.factory = ContractCheck.mustNotBeNull(factory, "factory");
 		this.paramerter = parameter;
 	}
 
 	@Override
 	public InstanceType newInstance() {
 		return this.factory.create(this.paramerter);
 	}
 
 }
