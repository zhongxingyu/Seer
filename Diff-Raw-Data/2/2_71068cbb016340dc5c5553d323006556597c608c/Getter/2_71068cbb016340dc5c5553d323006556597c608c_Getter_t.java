 /*
  * Copyright 2013-present the original author or authors.
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.forty9.fluent.util;
 
 import static com.forty9.fluent.proxy.FluentProxyFactory.factory;
 import static com.forty9.fluent.proxy.FluentProxyFactory.getInvocationHandler;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.forty9.fluent.proxy.ChainingInvocationDelegate;
 import com.forty9.fluent.proxy.FluentInvocationHandler;
 import com.forty9.fluent.proxy.FluentProxy;
 import com.forty9.fluent.proxy.Invocation;
 import com.forty9.fluent.proxy.InvocationDelegate;
 import com.forty9.fluent.proxy.Parameterizer;
 
 /**
  * <p>Tool to retrieve a value from a given source. The particularity of these
  * getters is that they are built using a {@link FluentProxy} chain of 
  * invocations. As the <q>path</q> a getter will follow to resolve a property
  * from an instance is defined using a simulation of invocation, it is validated
  * at compile time.</p>
  * 
  * <p>First, a static <code>build</code> methods is called to get the starting 
 * fluent proxy. A method is invoked on this proxy and, optionally, on the
  * result of this invocation, etc. This create a chain of invocations that is
  * recorded. Once the desired <q>path</q> has been reached, the original
  * proxy is passed to the {@link Getter#create(Object)} method and the 
  * corresponding {@link Getter} is returned. Note that the creation of a getter
  * will reset the recording of the invocation and, thus, the proxy can be
  * directly re-used to create another getter.</p>
  * 
  * <p>Example (assuming a <code>Person</code> and <code>Address</code> classes):</p>
  * <pre>
  * Person person = Getter.build(Person.class);
  * person.getAddress().getStreet();
  * Getter&lt;Person, String&gt; getter = Getter.create(person);</pre>
  * 
  * <p>Having created this getter, it is now possible to get the street address 
  * of any person directly. Note that this address can be retrieved either in a
  * lenient way where not exception would be thrown if the person has no address
  * of the <code>getAddress()</code> is not accessible where the code is run, or
  * in a non-lenient way:</p>
  * 
  * <pre>
  * String street = getter.getLenient(johnDoe); // does not throw any exception, even-though John Doe's address is most likely unknown
  * String infiniteLoop = getter.get(apple); // will throw a RuntimeException if the address is null</pre>
  * 
  * <p>It is possible to pass argument to a method as long as these are not the
  * result of an invocation on a proxy that will have to be evaluated later:</p>
  * 
  * <pre>
  * person.getAddresses("home").getStreet(); // valid
  * person.getAddresses(person.getAddresses().size() - 1).getStreet(); // invalid</pre>
  * 
  * <p>Note that, as a final class cannot be proxied, the chain of invocation 
  * cannot go beyond any final result. By example, it is not possible to create
  * a getter to get the length of a string as the later is final. However, it
  * might be possible to create a getter to access that string.</p>
  * 
  * @author Patrice Jaton
  */
 public class Getter<S, R> {
 	private List<Pair<Method, Object[]>> calls;
 	private Class<S> sourceClass;
 	private Class<R> resultClass;
 	
 	/**
 	 * Gets the result value from the given source in a lenient way; i.e. if
 	 * any of the encountered object is null, then, instead of throwing an
 	 * exception, returns null if the expected type is not a primitive. If it
 	 * is a primitive, then return false for boolean or zero for any other 
 	 * primitive
 	 * @param source The source to retrieve the value of
 	 * @return The resulting value or null or the <q>default</q> primitive value
 	 */
 	public R getLenient(S source) {
 		return getLenient(source, TypeUtils.nullZeroOrFalse(resultClass));
 	}
 	
 	/**
 	 * Gets the result value from the given source in a lenient way; i.e. if
 	 * any of the encountered object is null, then, instead of throwing an
 	 * exception, returns the provided default value
 	 * @param source The source to retrieve the value of
 	 * @param defaultValue The default value to return when the result is null
 	 * at any level
 	 * @return The resulting value or the default value
 	 */
 	@SuppressWarnings("unchecked")
 	public R getLenient(S source, R defaultValue) {
 		if (source == null) {
 			return defaultValue;
 		}
 		
 		Object result = source;
 		for (Pair<Method, Object[]> call : calls) {
 			Method method = call.getFirst();
 			Object[] args = call.getSecond();
 			try {
 				result = method.invoke(result, args);
 			} catch (Exception e) {
 				result = null;
 			}
 			if (result == null) {
 				break;
 			}
 		}
 
 		if (result == null) {
 			return defaultValue;
 		}
 		
 		return (R) result;
 	}
 
 	/**
 	 * Gets the result value from the given source and throws a runtime 
 	 * exception if the result if null at any intermediate level.
 	 * @param source The source to retrieve the value of
 	 * @return The resulting value
 	 * @throws RuntimeException if an error occurred while retrieving the
 	 * value 
 	 */
 	public R get(S source) {
 		return get(source, null);
 	}
 
 	/**
 	 * Gets the result value from the given source and throws a runtime 
 	 * exception if the result if null at any intermediate level.
 	 * @param source The source to retrieve the value of
 	 * @param defaultValue The default value to return when the end result 
 	 * is null
 	 * @return The resulting value or the default value
 	 * @throws RuntimeException if an error occurred while retrieving the
 	 * value 
 	 */
 	@SuppressWarnings("unchecked")
 	public R get(S source, R defaultValue) {
 		
 		Object result = source;
 		StringBuilder path = new StringBuilder(sourceClass.getName());
 		for (Pair<Method, Object[]> call : calls) {
 			Method method = call.getFirst();
 			Object[] args = call.getSecond();
 			path.append(".").append(method.getName()).append("(");
 			if (args != null) {
 				for (int i=0; i<args.length; i++) {
 					path.append(args[i]);
 					if (i+1<args.length) {
 						path.append(", ");
 					}
 				}
 			}
 			path.append(")");
 			try {
 				result = method.invoke(result, args);
 			} catch (Exception e) {
 				throw new RuntimeException(String.format("Unable to retrieve %s", path.toString()), e);
 			}
 		}
 
 		if (result == null) {
 			return defaultValue;
 		}
 		
 		return (R) result;
 	}
 
 	/**
 	 * <p>Builds a new {@link Getter} by creating and returning a fluent proxy. 
 	 * This proxy will be of the expected source type.</p>
 	 * 
 	 * <p>To <q>build</q> the path the getter will have to follow, simply call 
 	 * the method on the proxy or the result of a method call, unless the 
 	 * return type of the later is final (final classes, like string, arrays,
 	 * enumeration and primitive cannot be proxied). Note that parameters
 	 * can be passed to a method invocation as long as they are static:</p>
 	 * 
 	 * <ul>
 	 *  <li><code>person.getAddresses("home").getStreet()</code> is valid</li>
 	 *  <li><code>person.getAddresses(person.getAddresses().size()-1).getStreet()</code>
 	 *      is invalid</li>
 	 * </ul>
 	 * 
 	 * <p>Once the desire path has been <q>built</q>, pass the original proxy
 	 * to the {@link Getter#create(Object)} method to get the corresponding 
 	 * {@link Getter}</p>
 
 	 * @param rawClass The raw class to proxy
 	 * @param parameterizations The parameterized types to associate to the 
 	 * raw class's type variables in the order the later are defined. These 
 	 * parameterizations are optional, even if the raw type has type variables,
 	 * but their number must match the number and order of the type variables if
 	 * they are set.
 	 * @param <S> The expected type of the proxy
 	 * @return The corresponding proxy to use to define the path of the getter
 	 */
 	@SuppressWarnings("unchecked")
 	public static <S> S build(Class<?> rawClass, Type...parameterizations) {
 		if (Modifier.isFinal(rawClass.getModifiers())) {
 			throw new IllegalArgumentException(String.format(
 					"Unable to build getter for final %s", 
 					rawClass));
 		}
 		return (S) factory().create(rawClass, new ChainingInvocationDelegate(), parameterizations);
 	}
 
 	/**
 	 * <p>Builds a new {@link Getter} by creating and returning a fluent proxy. 
 	 * This proxy will be of the expected source type.</p>
 	 * 
 	 * <p>To <q>build</q> the path the getter will have to follow, simply call 
 	 * the method on the proxy or the result of a method call, unless the 
 	 * return type of the later is final (final classes, like string, arrays,
 	 * enumeration and primitive cannot be proxied). Note that parameters
 	 * can be passed to a method invocation as long as they are static:</p>
 	 * 
 	 * <ul>
 	 *  <li><code>person.getAddresses("home").getStreet()</code> is valid</li>
 	 *  <li><code>person.getAddresses(person.getAddresses().size()-1).getStreet()</code>
 	 *      is invalid</li>
 	 * </ul>
 	 * 
 	 * <p>Once the desire path has been <q>built</q>, pass the original proxy
 	 * to the {@link Getter#create(Object)} method to get the corresponding 
 	 * {@link Getter}</p>
 
 	 * @param parameterizer The parameterizer instance to determine the type of
 	 * the proxy (see {@link Parameterizer})
 	 * @param <S> The expected type of the proxy
 	 * @return The corresponding proxy to use to define the path of the getter
 	 */
 	public static <S> S build(Parameterizer<S> parameterizer) {
 		if (Modifier.isFinal(parameterizer.getClass().getModifiers())) {
 			throw new IllegalArgumentException(String.format(
 					"Unable to build getter for final %s", 
 					parameterizer.getClass()));
 		}
 		return factory().create(parameterizer, new ChainingInvocationDelegate());
 	}
 	
 	/**
 	 * Creates a new getter that will <q>replay</q> the methods invocation chain
 	 * that has been recorded on the given fluent proxy instance
 	 * @param proxy The fluent proxy instance to build the getter from
 	 * @param <S> The type of the source that will be passed to the getter
 	 * @param <R> The type of the result
 	 * @return The corresponding getter
 	 */
 	@SuppressWarnings("unchecked")
 	public static <S, R> Getter<S, R> create(S proxy) {
 		FluentInvocationHandler handler = getInvocationHandler(proxy);
 		if (handler == null) {
 			throw new IllegalArgumentException(String.format("[%s] is not a fluent proxy!", proxy));
 		}
 		InvocationDelegate delegate = handler.getDelegate();
 		if (delegate instanceof ChainingInvocationDelegate) {
 			LinkedList<Invocation> chain = ((ChainingInvocationDelegate) delegate).chainAndReset();
 			if (chain.isEmpty()) {
 				throw new IllegalArgumentException(String.format(
 						"At least one method must be invoked on [%s] to create a getter from it!", 
 						proxy));
 			}
 			Getter<S, R> getter = new Getter<S, R>();
 			getter.calls = new ArrayList<Pair<Method,Object[]>>(chain.size());
 			getter.sourceClass = (Class<S>) handler.getFluentType().getRawClass();
 			getter.resultClass = (Class<R>) chain.getLast().getResultClass();
 			for (Invocation invocation : chain) {
 				getter.calls.add(new Pair<Method, Object[]>(invocation.getMethod(), invocation.getArgs()));
 			}
 			return getter;
 		}
 		throw new IllegalArgumentException(String.format(
 				"[%s] is not a fluent proxy created by the one of the Getter#build() method!", 
 				proxy));
 	}
 }
