 /**
  * Copyright (C) 2008 Ivan S. Dubrov
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
 
 package com.google.code.nanorm.internal.introspect.asm;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Type;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import com.google.code.nanorm.exceptions.IntrospectionException;
 import com.google.code.nanorm.internal.QueryDelegate;
 import com.google.code.nanorm.internal.config.InternalConfiguration;
 import com.google.code.nanorm.internal.config.StatementConfig;
 import com.google.code.nanorm.internal.introspect.AbstractIntrospectionFactory;
 import com.google.code.nanorm.internal.introspect.Getter;
 import com.google.code.nanorm.internal.introspect.IntrospectUtils;
 import com.google.code.nanorm.internal.introspect.Setter;
 
 /**
  * ASM based
  * {@link com.google.code.nanorm.internal.introspect.IntrospectionFactory}
  * implementation. Uses runtime code generation to create getters and setters.
  * 
  * TODO: Debugging.
  * 
  * @author Ivan Dubrov
  * @version 1.0 19.06.2008
  */
 public class ASMIntrospectionFactory extends AbstractIntrospectionFactory {
 
 	private final ASMClassLoader classLoader;
 
 	private final Map<AccessorKey, Getter> getters;
 
 	private final Map<AccessorKey, Setter> setters;
 
 	private final AtomicInteger counter = new AtomicInteger(0);
 
 	private final Object lock = new Object();
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param parentLoader parent classloader for classloader which will load
 	 *            generated clasess
 	 */
 	public ASMIntrospectionFactory(final ClassLoader parentLoader) {
 		classLoader = AccessController.doPrivileged(new PrivilegedAction<ASMClassLoader>() {
 			public ASMClassLoader run() {
 				return new ASMClassLoader(parentLoader);
 			}
 		});
 		getters = new ConcurrentHashMap<AccessorKey, Getter>();
 		setters = new ConcurrentHashMap<AccessorKey, Setter>();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Getter buildGetter(final Class<?> beanClass, final String path) {
 		return buildGetterImpl(beanClass, null, path);
 	}
 
 	/**
 	 * Create regular getter or parameter getter instance.
 	 * 
 	 * @param beanClass bean class; should be null if types is not null
 	 * @param types parameter types; should be null if beanClass is not null
 	 * @param path property path
 	 * @return
 	 */
 	private Getter buildGetterImpl(final Class<?> beanClass, final Type[] types, final String path) {
 		AccessorKey key = types == null ? new AccessorKey(beanClass, path) : new AccessorKey(types,
 				path);
 		Getter instance = getters.get(key);
 		if (instance == null) {
 			String name = "com/google/code/nanorm/generated/Getter" + counter.incrementAndGet();
 
 			AccessorBuilder builder = new AccessorBuilder(name, false);
 			Type[] finalType = new Type[1];
 
 			byte[] code = types == null ? IntrospectUtils.visitPath(path, beanClass, builder,
 					finalType) : IntrospectUtils.visitPath(path, types, builder, finalType);
 
 			// Re-check we didn't created other instance while we were
 			// generating
 			synchronized (lock) {
 				instance = getters.get(key);
 
 				if (instance == null) {
					Class<?> clazz = classLoader.defineClass(name.replace('/', '.'), code);
 					try {
 						Constructor<?> ct = clazz.getConstructor(java.lang.reflect.Type.class);
 						instance = (Getter) ct.newInstance(finalType[0]);
 					} catch (Exception e) {
 						throw new IntrospectionException("Failed to create getter instance!", e);
 					}
 					getters.put(key, instance);
 				}
 			}
 		}
 		return instance;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Getter buildParameterGetter(final java.lang.reflect.Type[] types, final String path) {
 		return buildGetterImpl(null, types, path);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Setter buildSetter(final Class<?> beanClass, final String path) {
 		return buildSetterImpl(beanClass, null, path);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Setter buildParameterSetter(final Type[] types, final String path) {
 		return buildSetterImpl(null, types, path);
 	}
 
 	/**
 	 * Create regular setter or parameter getter instance.
 	 * 
 	 * @param beanClass bean class; should be null if types is not null
 	 * @param types parameter types; should be null if beanClass is not null
 	 * @param path property path
 	 * @return setter instance
 	 */
 	public Setter buildSetterImpl(final Class<?> beanClass, final Type[] types, final String path) {
 		AccessorKey key = new AccessorKey(beanClass, path);
 		Setter instance = setters.get(key);
 		if (instance == null) {
 			String name = "com/google/code/nanorm/generated/Setter" + counter.incrementAndGet();
 
 			AccessorBuilder builder = new AccessorBuilder(name, true);
 			Type[] finalType = new Type[1];
 
 			byte[] code = types == null ? IntrospectUtils.visitPath(path, beanClass, builder,
 					finalType) : IntrospectUtils.visitPath(path, types, builder, finalType);
 
 			// Re-check we didn't created other instance while we were
 			// generating
 			synchronized (lock) {
 				instance = setters.get(key);
 
 				if (instance == null) {
					Class<?> clazz = classLoader.defineClass(name.replace('/', '.'), code);
 					try {
 						instance = (Setter) clazz.newInstance();
 					} catch (Exception e) {
 						throw new IntrospectionException("Failed to create setter instance!", e);
 					}
 					setters.put(key, instance);
 				}
 			}
 		}
 		return instance;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public <T> T createMapper(Class<T> interfaze, InternalConfiguration config,
 			QueryDelegate delegate){
 
 		// TODO: Cache!
 		List<MethodConfig> methods = new ArrayList<MethodConfig>();
 		List<StatementConfig> configs = new ArrayList<StatementConfig>();
 		for (java.lang.reflect.Method m : interfaze.getMethods()) {
 			StatementConfig stConfig = config.getStatementConfig(interfaze, m.getName(), m
 					.getGenericParameterTypes());
 			if (stConfig != null) {
 				MethodConfig cfg = new MethodConfig(m, methods.size());
 				methods.add(cfg);
 				configs.add(stConfig);
 			}
 		}
 
 		String name = "com/google/code/nanorm/generated/Mapper" + counter.incrementAndGet();
 		byte[] code = MapperBuilder.buildMapper(name, interfaze, methods
 				.toArray(new MethodConfig[methods.size()]));
 
		Class<?> clazz = classLoader.defineClass(name.replace('/', '.'), code);
 		Object instance;
 		try {
 			Constructor<?> ctor = clazz
 					.getConstructor(QueryDelegate.class, StatementConfig[].class);
 			instance = ctor.newInstance(delegate, configs.toArray(new StatementConfig[configs
 					.size()]));
 		} catch (Exception e) {
 			throw new IntrospectionException("Failed to create mapper instance!", e);
 		}
 		return interfaze.cast(instance);
 	}
 
 	/**
 	 * Classloader used for accessors.
 	 * 
 	 * @author Ivan Dubrov
 	 * @version 1.0 21.06.2008
 	 */
 	private static class ASMClassLoader extends ClassLoader {
 		/**
 		 * Constructor.
 		 * 
 		 * @param parent parent classloader
 		 */
 		ASMClassLoader(ClassLoader parent) {
 			super(parent);
 		}
 
 		/**
 		 * Define class in the classloader.
 		 * 
 		 * @param name class name
 		 * @param b code
 		 * @return {@link Class} instance.
 		 */
		public Class<?> defineClass(String name, byte[] b) {
 			return defineClass(name, b, 0, b.length);
 		}
 	}
 }
