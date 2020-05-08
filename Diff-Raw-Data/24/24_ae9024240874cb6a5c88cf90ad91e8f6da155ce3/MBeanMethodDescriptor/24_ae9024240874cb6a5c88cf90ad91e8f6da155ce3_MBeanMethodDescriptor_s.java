 /*
  * Copyright 2009 buschmais GbR
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.
  * See the License for the specific language governing permissions and
  * limitations under the License
  */
 package com.buschmais.maexo.framework.commons.mbean.dynamic;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Describes a method which is provided by an MBean. The parameter types are
  * normalized which means that native types are treated to be equal to their
  * wrapper types.
  */
 public class MBeanMethodDescriptor {
 
 	private final static Map<String, String> NATIVE_TYPE_TRANSLATIONS;
 
 	static {
 		Map<String, String> nativeTypeTranslations = new HashMap<String, String>();
		nativeTypeTranslations = new HashMap<String, String>();
 		nativeTypeTranslations.put(Boolean.TYPE.getName(), Boolean.class
 				.getName());
 		nativeTypeTranslations.put(Byte.TYPE.getName(), Byte.class.getName());
 		nativeTypeTranslations.put(Character.TYPE.getName(), Character.class
 				.getName());
 		nativeTypeTranslations.put(Double.TYPE.getName(), Double.class
 				.getName());
 		nativeTypeTranslations.put(Float.TYPE.getName(), Float.class.getName());
 		nativeTypeTranslations.put(Integer.TYPE.getName(), Integer.class
 				.getName());
 		nativeTypeTranslations.put(Long.TYPE.getName(), Long.class.getName());
 		nativeTypeTranslations.put(Short.TYPE.getName(), Short.class.getName());
 		NATIVE_TYPE_TRANSLATIONS = Collections
 				.unmodifiableMap(nativeTypeTranslations);
 	}
 
 	/**
 	 * The name of the method.
 	 */
 	private String name;
 
 	/**
 	 * The list of parameter types.
 	 */
 	private List<String> parameterTypes = new LinkedList<String>();
 
 	/**
 	 * Constructs the descriptor using a string array describing the method's
 	 * parameters.
 	 *
 	 * @param name
 	 *            The name of the method.
 	 * @param parameterTypes
 	 *            The parameter types.
 	 */
 	public MBeanMethodDescriptor(String name, String[] parameterTypes) {
 		this.name = name;
 		for (String parameterType : parameterTypes) {
 			this.parameterTypes.add(this.normalizeType(parameterType));
 		}
 	}
 
 	/**
 	 * Performs type normalization.
 	 * <p>
 	 * Currently all native types are converted to their corresponding wrapper
 	 * types.
 	 *
 	 * @param typeClass
 	 *            The name of the type.
 	 * @return The name of the normalized type.
 	 */
 	private String normalizeType(String typeClass) {
 		String translated = NATIVE_TYPE_TRANSLATIONS.get(typeClass);
 		return translated != null ? translated : typeClass;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result
 				+ ((parameterTypes == null) ? 0 : parameterTypes.hashCode());
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		MBeanMethodDescriptor other = (MBeanMethodDescriptor) obj;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (parameterTypes == null) {
 			if (other.parameterTypes != null)
 				return false;
 		} else if (!parameterTypes.equals(other.parameterTypes))
 			return false;
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String toString() {
 		return String.format("%s(%s)", this.name, this.parameterTypes);
 	}
 
 }
