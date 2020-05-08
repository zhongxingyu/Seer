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
 package com.forty9.fluent.proxy;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.GenericArrayType;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.lang.reflect.WildcardType;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * <p>Specifies the raw class and the parameterized types corresponding to its 
  * type variables of a fluent proxy. Because of Java 
  * <a href="http://en.wikipedia.org/wiki/Type_erasure" target="_blank">Type Erasure</a>, the 
  * type parameter of a generic type might be lost at runtime and the fluent
  * proxy factory might not know which class to proxy in response of an 
  * invocation on an existing fluent proxy.</p>
  * 
  * <p>By example, a <code>Map&lt;Integer, List&lt;String&gt;&gt;</code> proxy is
  * going to have behind it the {@link FluentType}:</p>
  * 
  * <pre>
  * rawClass = Map&lt;K, V&gt;
  * parameterizations = {
  *    &lt;K&gt; = Integer,
  *    &lt;V&gt; = List&lt;String&gt;
  * }</pre>
  * 
  * <p>The <code>values()</code> method of the map class is defined to return the
  * parameterized type <code>Collection&lt;V&gt;</code>. When this method is 
  * invoked on this fluent proxy, this parameterized type will be passed to this
  * fluent type; which will then infer it as the new following fluent type by 
  * associating the type variable <code>&lt;E&gt;</code> of the 
  * <code>Collection</code> class to the parameterized type 
  * <code>List&lt;String&gt;</code> associated to the <code>&lt;V&gt</code> type
  * variable of the <code>Map</code> class:</p>
  * 
  * <pre>
  * rawClass = Collection&lt;E&gt;
  * parameterizations = {
  *    &lt;E&gt; = List&lt;String&gt;
  * }</pre>
  * 
  * <p>Without the map of parameterization that was stored along the raw
  * <code>Map</code> class, we would not be able to determine that the expected
  * to proxy when <code>values()</code> is invoked is a list of strings.</p>
  * 
  * <p>As you cannot defined a parameterized type in Java by simply writing 
  * something like <code>List&lt;String&gt;.class</code>, the 
  * {@link FluentProxyFactory} offer two approach to specify the type to proxy:</p>
  * 
  * <ul>
  *    <li>{@link FluentProxyFactory#create(Class, java.lang.reflect.Type[])} or
  *        {@link FluentProxyFactory#create(Class, InvocationDelegate, java.lang.reflect.Type[])} to
  *        simply specify the raw type and the types to associate to its type
  *        variable, if any. This simple approach works only for non-generic 
  *        classes or for one depth generic types (i.e. 
  *        <code>List&lt;String&gt;</code>, but not 
  *        <code>List&lt;List&lt;String&gt;&gt;</code>).</li>
  *    <li>{@link FluentProxyFactory#create(Parameterizer)} or
 *        {@link FluentProxyFactory#create(Parameterizer, InvocationDelegate))} 
  *        to specify more complex types by passing an instance of an anonymous 
  *        class implementing the {@link Parameterizer} interface (see the later 
  *        for more details).</li>
  * </ul>
  * 
  * @author pjaton
  */
 public class FluentType {
 	private static final Logger LOGGER = LoggerFactory.getLogger(FluentType.class);
 	private Class<?> rawClass;
 	private Map<TypeVariable<?>, Type> parameterizations;
 
 	/**
 	 * Constructor with the specification of the raw class and the parameterized
 	 * types to associate to its type variables, if any 
 	 * @param rawClass The raw class
 	 * @param parameterizations The parameterized types to associate to the 
 	 * raw class's type variables in the order the later are defined. These 
 	 * parameterizations are optional, even if the raw type has type variables,
 	 * but their number must match the number and order of the type variables if
 	 * they are set.
 	 */
 	public FluentType(Class<?> rawClass, Type...parameterizations) {
 		this.rawClass = rawClass;
 		this.parameterizations = new HashMap<TypeVariable<?>, Type>();
 		if ((parameterizations != null) && (parameterizations.length > 0)) {
 			TypeVariable<?>[] typeParameters = rawClass.getTypeParameters();
 			if (typeParameters.length != parameterizations.length) {
 				throw new IllegalArgumentException(String.format(
 						"Number of parameterized types (%,d) does not match declared number of type variables (%,d) on %s!", 
 						parameterizations.length,
 						typeParameters.length,
 						rawClass));
 			}
 			for (int i=0; i<typeParameters.length; i++) {
 				this.parameterizations.put(typeParameters[i], parameterizations[i]);
 			}
 		}
 	}
 	
 	/**
 	 * Constructor with the specification of the raw class and the 
 	 * parameterizations mapping
 	 * @param rawClass The raw class  
 	 * @param parameterizations The map of parameterized types associated to 
 	 * type variables 
 	 */
 	public FluentType(Class<?> rawClass, Map<TypeVariable<?>, Type> parameterizations) {
 		this.rawClass = rawClass;
 		this.parameterizations = parameterizations;
 	}
 	
 	/**
 	 * Static builder of a new fluent type describing the given type
 	 * @param type The type to build a fluent type for
 	 * @return The corresponding fluent type or null
 	 */
 	public static FluentType build(Type type) {
 		FluentType fluent = new FluentType(null);
 		Map<TypeVariable<?>, Type> parameterizations = new HashMap<TypeVariable<?>, Type>();
 		Class<?> rawClass = fluent.interfer(type, parameterizations);
 		fluent.rawClass = rawClass;
 		fluent.parameterizations = parameterizations;
 		return fluent;
 	}
 	
 	/**
 	 * Resolves the new fluent type by taking the provided type and resolving
 	 * which parameterized type to associate to its type variables or resolving
 	 * its upper or lower bound all this based on the parameterizations defined
 	 * in the current fluent type. By example, if the current fluent type 
 	 * specifies a <code>Map&lt;Integer, String&gt;</code>, the generic return
 	 * type <code>Collection&lt;V&gt;</code> of the <code>Map#values()</code>
 	 *  method will be resolved as a <code>Collection&lt;String&gt;</code>.  
 	 * @param type The type to resolve
 	 * @return The new fluent type or null if it cannot be resolved (e.g. if the
 	 * type is a type variable that does not match any parameterization)
 	 * @see <a href="http://en.wikipedia.org/wiki/Type_inference" target="_blank">Type Inference</a> 
 	 * definition on wikipedia
 	 */
 	public FluentType interfer(Type type) {
 		Map<TypeVariable<?>, Type> newParameterizations = new HashMap<TypeVariable<?>, Type>();
 		Class<?> raw = interfer(type, newParameterizations);
 		if (raw != null) {
 			return new FluentType(raw, newParameterizations);
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Resolves the concrete class from the given type and populate the map
 	 * of parameterizations with the parameterized type associate to type 
 	 * variables this concrete class owns or referenced 
 	 * @param type The type to resolve
 	 * @param newParameterizations The map of parameterizations to populate
 	 * @return The corresponding type or null if it cannot be resolved
 	 */
 	private Class<?> interfer(Type type, Map<TypeVariable<?>, Type> newParameterizations) {
 		
 		// the type is a class
 		if (type instanceof Class<?>) {
 			return (Class<?>) type;
 		} 
 
 		// the type is a type variable (<T>)
 		// try to find the corresponding type in the map of existing 
 		// parameterizations or to resolve its upper bound (<T extends ...>), 
 		// and "interfer" it further until a concrete class is found (populating
 		// the new parameterizations along the way)
 		else if (type instanceof TypeVariable<?>) {
 			TypeVariable<?> tv = (TypeVariable<?>) type;
 			
 			if (parameterizations.containsKey(tv)) {
 				return interfer(parameterizations.get(tv), newParameterizations);
 			} 
 
 			Type[] bounds = tv.getBounds();
 			if ((bounds != null) && (bounds.length > 0)) {
 				return interfer(bounds[0], newParameterizations);
 			} 
 
 			// unknown type variable
 			LOGGER.debug("Cannot resolve <{}> as it is neither specified or bound!", type);
 		}
 		
 		// the type is a wildcard (<?>)
 		// try to resolve its upper (<? extends ...>) or lower (<? super ...>)
 		// bound and "interfer" it further until a concrete class is found 
 		// (populating the new parameterizations along the way)
 		else if (type instanceof WildcardType) {
 			WildcardType wt = (WildcardType) type;
 			Type[] bounds = wt.getUpperBounds();
 			if ((bounds != null) && (bounds.length > 0)) {
 				return interfer(bounds[0], newParameterizations);
 			} 
 			bounds = wt.getLowerBounds();
 			if ((bounds != null) && (bounds.length > 0)) {
 				return interfer(bounds[0], newParameterizations);
 			}
 
 			// unknown wildcard
 			LOGGER.debug("Cannot resolve <{}> as it is unbound!", type);
 		}
 		
 		// the type is a parameterized type
 		// returns its raw type (should be a class) after having populating the
 		// parameterizations for its type variables in the new map
 		else if (type instanceof ParameterizedType) {
 			ParameterizedType pt = (ParameterizedType) type;
 			Type raw = pt.getRawType();
 			if (raw instanceof Class<?>) {
 				TypeVariable<?>[] typeParameters = ((Class<?>) raw).getTypeParameters();
 				Type[] actualTypes = pt.getActualTypeArguments();
 				if (typeParameters.length != actualTypes.length) {
 					LOGGER.warn("{} defines {} type variables, but has {} actual ones", 
 							pt, typeParameters.length, actualTypes.length);
 					return null;
 				}
 				for (int i=0; i<typeParameters.length; i++) {
 					if (parameterizations.containsKey(actualTypes[i])) {
 						newParameterizations.put(typeParameters[i], parameterizations.get(actualTypes[i]));
 					} else {
 						newParameterizations.put(typeParameters[i], actualTypes[i]);
 					}
 				}
 				return (Class<?>)raw;
 			} else {
 				LOGGER.warn("Cannot resolved {}. Its raw type {} is not a Class!", pt, raw);
 			}
 		} 
 		
 		// the type is a generic array type
 		// try to find its generic component type in the map of existing 
 		// parameterizations and "interfer" the result further until a concrete
 		// class is found (populating the new parameterizations along the way)
 		else if (type instanceof GenericArrayType) {
 			GenericArrayType gat = (GenericArrayType) type;
 			if (parameterizations.containsKey(gat.getGenericComponentType())) {
 				Class<?> arrayType = interfer(parameterizations.get(gat.getGenericComponentType()), newParameterizations);
 				return Array.newInstance(arrayType, 0).getClass();
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * The raw class this fluent type is build on
 	 * @return The raw class
 	 */
 	public Class<?> getRawClass() {
 		return rawClass;
 	}
 	
 	/**
 	 * Returns the parameterized type for the given type variable, if any, from 
 	 * the map of parameterization
 	 * @param variable The type variable to retrieve
 	 * @return The corresponding parameterized type or null
 	 */
 	public Type getParameterizedType(TypeVariable<?> variable) {
 		return parameterizations.get(variable);
 	}
 	
 	/**
 	 * Returns the map of parameterizations; i.e. the parameterized types 
 	 * associated to the type varibles of the raw class
 	 * @return The map of parameterizations
 	 */
 	public Map<TypeVariable<?>, Type> getParameterizations() {
 		return parameterizations;
 	}
 
 }
