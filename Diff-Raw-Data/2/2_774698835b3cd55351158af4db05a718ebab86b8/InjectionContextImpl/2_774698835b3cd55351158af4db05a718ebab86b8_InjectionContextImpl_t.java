 /*
  * This file is a component of thundr, a software library from 3wks.
  * Read more: http://www.3wks.com.au/thundr
  * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.threewks.thundr.injection;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.atomicleopard.expressive.collection.Triplets;
 import com.threewks.thundr.configuration.Environment;
 import com.threewks.thundr.exception.BaseException;
 import com.threewks.thundr.introspection.ClassIntrospector;
 import com.threewks.thundr.introspection.MethodIntrospector;
 import com.threewks.thundr.introspection.ParameterDescription;
 
 public class InjectionContextImpl implements UpdatableInjectionContext {
 	private static final String ENVIRONMENT_SEPARATOR = "%";
 	private Triplets<Class<?>, String, Class<?>> types = map();
 	private Triplets<Class<?>, String, Object> instances = map();
 
 	private MethodIntrospector methodIntrospector = new MethodIntrospector();
 	private ClassIntrospector classIntrospector = new ClassIntrospector();
 
 	@Override
 	public <T> InjectorBuilder<T> inject(Class<T> type) {
 		return new InjectorBuilder<T>(this, type);
 	}
 
 	@Override
 	public <T> InjectorBuilder<T> inject(T instance) {
 		return new InjectorBuilder<T>(this, instance);
 	}
 
 	public <T> T get(Class<T> type) {
 		return get(type, null);
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> T get(Class<T> type, String name) {
 		T instance = getExistingNamedInstance(type, name);
 		if (instance == null) {
 			T newInstance = instantiate((Class<T>) types.get(type, name));
 			if (newInstance != null) {
 				synchronized (instances) {
 					if (!instances.containsKey(type, name)) {
 						instances.put(type, name, newInstance);
 					}
 				}
 				instance = (T) instances.get(type, name);
 			} else {
 				if (name != null) {
 					instance = get(type, null);
 				}
 			}
 		}
 		if (instance == null) {
 			throw new NullPointerException(String.format("Could not inject '%s' - no dependency configured for injection", type.getName()));
 		}
 		return instance;
 	}
 
 	protected <T> void addType(Class<T> type, String name, Class<? extends T> as) {
 		types.put(type, name, as);
 	}
 
 	protected <T> void addInstance(Class<T> type, String name, T as) {
 		instances.put(type, name, as);
 	}
 
 	private <T> T instantiate(Class<T> type) {
 		if (type == null) {
 			return null;
 		}
 		List<Constructor<T>> ctors = classIntrospector.listConstructors(type);
 		for (int i = ctors.size() - 1; i >= 0; i--) {
 			Constructor<T> constructor = ctors.get(i);
 			List<ParameterDescription> parameterDescriptions = methodIntrospector.getParameterDescriptions(constructor);
 			if (canSatisfy(parameterDescriptions)) {
 				Object[] args = getAll(parameterDescriptions);
 				T instance = invokeConstructor(constructor, args);
 				instance = invokeSetters(type, instance);
 				return setFields(type, instance);
 			}
 		}
 
 		throw new InjectionException("Could not create a %s - cannot match parameters of any available constructors", type.getName());
 	}
 
 	private <T> T invokeSetters(Class<T> type, T instance) {
 		List<Method> setters = classIntrospector.listSetters(type);
 		for (Method method : setters) {
 			String name = getPropertyNameFromSetMethod(method);
 			try {
 				Class<?> argumentType = method.getParameterTypes()[0];
 				if (contains(argumentType, name)) {
 					method.invoke(instance, get(argumentType, name));
 				}
 			} catch (Exception e) {
 				throw new InjectionException(e, "Failed to inject into %s.%s: %s", type.getName(), method.getName(), e.getMessage());
 			}
 		}
 		return instance;
 	}
 
 	// TODO - Stack Overflow - A thread local storing types being created could bail
 	// out early in the case of stack overflow
 	private <T> T setFields(Class<T> type, T instance) {
 		List<Field> fields = classIntrospector.listInjectionFields(type);
 		for (Field field : fields) {
 			try {
 				Object beanProperty = get(field.getType(), field.getName());
 				boolean accessible = field.isAccessible();
 				field.setAccessible(true);
 				field.set(instance, beanProperty);
 				field.setAccessible(accessible);
 			} catch (Exception e) {
 				throw new InjectionException(e, "Failed to inject into %s.%s: %s", type.getName(), field.getName(), e.getMessage());
 			}
 		}
 
 		return instance;
 	}
 
 	private Object[] getAll(List<ParameterDescription> parameterDescriptions) {
 		List<Object> args = new ArrayList<Object>(parameterDescriptions.size());
 		for (ParameterDescription parameterDescription : parameterDescriptions) {
 			Object arg = get(parameterDescription.classType(), parameterDescription.name());
 			args.add(arg);
 		}
 		return args.toArray();
 	}
 
 	@Override
 	public <T> boolean contains(Class<T> type) {
 		return contains(type, null);
 	}
 
 	@Override
 	public <T> boolean contains(Class<T> type, String name) {
 		String envName = environmentSpecificName(name);
 		boolean contains = instances.containsKey(type, envName) || types.containsKey(type, envName) || instances.containsKey(type, name) || types.containsKey(type, name);
 		return contains || (name != null && contains(type));
 	}
 
 	@Override
 	public String toString() {
		return String.format("Injection context (%s instances, %s classes)", instances.size(), types.size());
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T> T getExistingNamedInstance(Class<T> type, String name) {
 		String environmentSpecificName = environmentSpecificName(name);
 		T instance = (T) instances.get(type, environmentSpecificName);
 		if (instance == null) {
 			instance = (T) instances.get(type, name);
 		}
 		return instance;
 	}
 
 	private String environmentSpecificName(String name) {
 		return name + ENVIRONMENT_SEPARATOR + Environment.get();
 	}
 
 	private boolean canSatisfy(List<ParameterDescription> parameterDescriptions) {
 		for (ParameterDescription parameterDescription : parameterDescriptions) {
 			if (!contains(parameterDescription.classType(), parameterDescription.name())) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private <T> T invokeConstructor(Constructor<T> constructor, Object[] args) {
 		try {
 			return constructor.newInstance(args);
 		} catch (Exception e) {
 			throw new BaseException(e, "Failed to create a new instance using the constructor %s: %s", constructor.toString(), e.getMessage());
 		}
 	}
 
 	private String getPropertyNameFromSetMethod(Method method) {
 		String nameWithUpperCaseFirstLetter = method.getName().replace("set", "");
 		return nameWithUpperCaseFirstLetter.substring(0, 1).toLowerCase() + nameWithUpperCaseFirstLetter.substring(1);
 	}
 
 	private <K1, K2, V> Triplets<K1, K2, V> map() {
 		return new Triplets<K1, K2, V>();
 	}
 
 }
