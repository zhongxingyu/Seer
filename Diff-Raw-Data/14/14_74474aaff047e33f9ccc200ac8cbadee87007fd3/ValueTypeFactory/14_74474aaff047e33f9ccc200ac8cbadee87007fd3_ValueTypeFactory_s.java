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
 package org.jbasics.configuration.properties;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jbasics.checker.ContractCheck;
import org.jbasics.discover.ServiceClassDiscovery;
 import org.jbasics.exception.DelegatedException;
 import org.jbasics.pattern.factory.ParameterFactory;
import org.jbasics.types.sequences.Sequence;
 
 @SuppressWarnings("unchecked")
 public final class ValueTypeFactory {
 	private final static Map<Class<?>, ParameterFactory<?, String>> FACTORIES = new HashMap<Class<?>, ParameterFactory<?, String>>();
 	static {
 		try {
			for (final Map.Entry<Sequence<Class<?>>, ParameterFactory> entry : ServiceClassDiscovery
					.discoverGenericsMappedImplementations(ValueTypeFactory.class, ParameterFactory.class, null).entrySet()) {
				ValueTypeFactory.FACTORIES.put(entry.getKey().first(), entry.getValue());

			}
 		} catch (final Exception e) {
 			throw DelegatedException.delegate(e);
 		}
 	}
 
 	public static <T> ParameterFactory<T, String> registerFactory(final Class<T> type, final ParameterFactory<T, String> factory) {
 		final ParameterFactory<T, String> temp = (ParameterFactory<T, String>) ValueTypeFactory.FACTORIES.get(ContractCheck.mustNotBeNull(type,
 				"type"));
 		ValueTypeFactory.FACTORIES.put(type, ContractCheck.mustNotBeNull(factory, "factory"));
 		return temp;
 	}
 
 	@SuppressWarnings("rawtypes")
 	public static <T> ParameterFactory<T, String> getFactory(final Class<T> type) {
 		ParameterFactory<T, String> temp = (ParameterFactory<T, String>) ValueTypeFactory.FACTORIES.get(ContractCheck.mustNotBeNull(type, "type"));
 		if (temp == null && Enum.class.isAssignableFrom(type)) {
 			temp = EnumValueTypeFactory.newInstance((Class<? extends Enum>) type);
 			ValueTypeFactory.registerFactory(type, temp);
 		}
 		return temp;
 	}
 
 	public static boolean hasFactory(final Class<?> type) {
 		return ValueTypeFactory.FACTORIES.containsKey(type);
 	}
 
 	public static <T> T create(final String value, final Class<T> type) {
 		final ParameterFactory<T, String> temp = ValueTypeFactory.getFactory(type);
 		if (temp == null) {
 			throw new RuntimeException("No value type factory registered for " + type);
 		}
 		return temp.create(value);
 	}
 }
