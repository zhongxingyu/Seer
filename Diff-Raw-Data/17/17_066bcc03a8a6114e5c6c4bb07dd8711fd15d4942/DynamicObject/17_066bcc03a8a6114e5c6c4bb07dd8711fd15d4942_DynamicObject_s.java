 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.util;
 
 import com.google.common.base.Throwables;
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 
 /**
  * Utility class to access (R/W) field in super class hierarchy.
  * <p>
  * It has decent performance as it LRU-caches reflective call.
  * 
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class DynamicObject {
 
 	private final Cache<String, Field> fFieldCache;
 
 	private final Object fTarget;
 
 	private final Class<?> fTargetClass;
 
 	/**
 	 * 
 	 */
 	public DynamicObject(Object target) {
 		fTarget = target;
 		fTargetClass = target.getClass();
 		fFieldCache = CacheBuilder.newBuilder().maximumSize(16).build(new CacheLoader<String, Field>() {
 			@Override
 			public Field load(String key) throws Exception {
 				return getField(fTargetClass, key);
 			}
 		});
 	}
 
 	public Object get(String fieldName) {
 		Object ret = null;
 		try {
 			Field field = fFieldCache.get(fieldName);
 			ret = field.get(fTarget);
		} catch (Throwable e) {
 			handleException(e);
 		}
 		return ret;
 	}
 
 	public void set(String fieldName, Object value) {
 		try {
 			Field field = fFieldCache.get(fieldName);
 			field.set(fTarget, value);
		} catch (Throwable e) {
 			handleException(e);
 		}
 	}
 
 	public double getDouble(String fieldName) {
 		double ret = Double.NaN;
 		try {
 			Field field = fFieldCache.get(fieldName);
 			ret = field.getDouble(fTarget);
		} catch (Throwable e) {
 			handleException(e);
 		}
 		return ret;
 	}
 
 	public void setDouble(String fieldName, double value) {
 		try {
 			Field field = fFieldCache.get(fieldName);
 			field.setDouble(fTarget, value);
		} catch (Throwable e) {
 			handleException(e);
 		}
 	}
 
 	public int getInt(String fieldName) {
 		int ret = 0;
 		try {
 			Field field = fFieldCache.get(fieldName);
 			ret = field.getInt(fTarget);
		} catch (Throwable e) {
 			handleException(e);
 		}
 		return ret;
 	}
 
 	public void setInt(String fieldName, int value) {
 		try {
 			Field field = fFieldCache.get(fieldName);
 			field.setInt(fTarget, value);
		} catch (Throwable e) {
 			handleException(e);
 		}
 	}
 
 	/**
 	 * @param cause
 	 */
 	protected void handleException(Throwable cause) {
 		Throwables.propagate(cause);
 	}
 
 	private static Field getField(Class<?> clazz, String fieldName) {
 		try {
 			Field declaredField = clazz.getDeclaredField(fieldName);
 			makeAccessible(declaredField);
 			return declaredField;
 		} catch (NoSuchFieldException e) {
 			Class<?> superClass = clazz.getSuperclass();
 			if (superClass == null) {
 				e.printStackTrace();
 			} else {
 				return getField(superClass, fieldName);
 			}
 		}
 		return null;
 	}
 
 	private static void makeAccessible(Field field) {
 		if (!Modifier.isPublic(field.getModifiers())
 				|| !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
 			field.setAccessible(true);
 		}
 	}
 
 }
