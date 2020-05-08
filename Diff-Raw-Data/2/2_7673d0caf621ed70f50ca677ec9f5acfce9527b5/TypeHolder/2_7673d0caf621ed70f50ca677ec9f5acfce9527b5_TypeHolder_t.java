 /*
  * Copyright 2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.nebulae2us.electron.reflect;
 
 import static org.nebulae2us.electron.internal.util.ClassHolderType.COLLECTION;
 import static org.nebulae2us.electron.internal.util.ClassHolderType.MAP;
 import static org.nebulae2us.electron.internal.util.ClassHolderType.MULTI_COLLECTION;
 import static org.nebulae2us.electron.internal.util.ClassHolderType.MULTI_MAP;
 import static org.nebulae2us.electron.internal.util.ClassHolderType.SINGLE;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.lang.reflect.WildcardType;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.nebulae2us.electron.Pair;
 import org.nebulae2us.electron.internal.util.ClassHolderType;
 import org.nebulae2us.electron.util.ImmutableList;
 
 /**
  * @author Trung Phan
  *
  */
 public class TypeHolder {
 
 	private final String name;
 	
 	private final String packageName;
 
 	/**
 	 * can be null. Null when name is not class name, but type variable such as T, Q, E
 	 */
 	private final Class<?> rawClass;
 
 	private final WildcardBound wildcardBound;
 	
 	private final List<TypeHolder> typeParams;
 	
 	private TypeHolder(
 			String name,
 			String packageName, 
 			Class<?> rawClass,
 			WildcardBound wildcardBound,
 			List<TypeHolder> typeParams) {
 		
 		this.name = name;
 		this.packageName = packageName;
 		this.rawClass = rawClass;
 		this.wildcardBound = wildcardBound;
 		this.typeParams = typeParams == null ? new ImmutableList<TypeHolder>() : new ImmutableList<TypeHolder>(typeParams);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getPackageName() {
 		return packageName;
 	}
 
 	public Class<?> getRawClass() {
 		return rawClass;
 	}
 
 	public WildcardBound getWildcardBound() {
 		return wildcardBound;
 	}
 
 	public List<TypeHolder> getTypeParams() {
 		return typeParams;
 	}
 	
 	public ClassHolderType getClassHolderType() {
 		if (Collection.class.isAssignableFrom(rawClass)) {
 			if (typeParams.size() > 0) {
 				ClassHolderType innerType = typeParams.get(0).getClassHolderType();
 				return innerType == COLLECTION ? MULTI_COLLECTION :
 					   innerType == MULTI_COLLECTION ? MULTI_COLLECTION : COLLECTION;
 			}
 			return COLLECTION;
 		}
 		else if (Map.class.isAssignableFrom(rawClass)) {
 			if (typeParams.size() > 1) {
 				ClassHolderType innerType = typeParams.get(1).getClassHolderType();
 				return innerType == COLLECTION ? MULTI_MAP :
 					   innerType == MULTI_COLLECTION ? MULTI_MAP: MAP;
 			}
 			return MAP;
 		}
 		else{
 			return SINGLE;
 		}
 	}
 	
 	public TypeHolder eraseParams() {
		return new TypeHolder(name, packageName, rawClass, wildcardBound, null);
 	}
 	
 	public TypeHolder toBuilderTypeHolder(String builderSuffix, String parentBuilderVariableName, List<Class<?>> classesToBuild) {
 		
 		List<TypeHolder> newBuilderTypeParams = new ArrayList<TypeHolder>();
 		for (TypeHolder typeParam : typeParams) {
 			TypeHolder builderTypeParam = typeParam.toBuilderTypeHolder(builderSuffix, parentBuilderVariableName, classesToBuild);
 			newBuilderTypeParams.add(builderTypeParam);
 		}
 		
 		if (classesToBuild.contains(rawClass)) {
 			newBuilderTypeParams.add(0, new TypeHolder(parentBuilderVariableName, "", null, WildcardBound.NO_WILDCARD, null));
 			return new TypeHolder(name + builderSuffix, packageName, null, wildcardBound, newBuilderTypeParams);
 		}
 		
 		/**
 		 * Class is a special class where Class<? extends |x|> is the result of x.getClass(). |x| is the erasure of all static types
 		 */
 		if (rawClass == Class.class) {
 			for (int i = 0; i < newBuilderTypeParams.size(); i++) {
 				TypeHolder newBuilderTypeParam = newBuilderTypeParams.get(i);
 				newBuilderTypeParams.set(i, newBuilderTypeParam.eraseParams());
 			}
 		}
 		
 		return new TypeHolder(name, packageName, rawClass, wildcardBound, newBuilderTypeParams);
 	}
 	
 	@Override
 	public String toString() {
 
 		StringBuilder result = new StringBuilder();
 		switch (wildcardBound) {
 		case NO_WILDCARD:
 			result.append(name);
 			break;
 		case LOWER:
 			result.append("? super ").append(name);
 			break;
 		case UPPER:
 			if (rawClass == Object.class) {
 				result.append("?");
 			}
 			else {
 				result.append("? extends ").append(name);
 			}
 			break;
 		}
 
 		if (typeParams.size() > 0) {
 			result.append('<');
 			for (TypeHolder typeParam : typeParams) {
 				result.append(typeParam.toString()).append(", ");
 			}
 			result.replace(result.length() - 2, result.length(), ">");
 		}
 
 		return result.toString();
 	}
 
 	public static TypeHolder newInstance(Class<?> rawClass, Class<?> ... typeParams) {
 		List<TypeHolder> classHolders = new ArrayList<TypeHolder>();
 		
 		for (Class<?> typeParam : typeParams) {
 			TypeHolder classHolder = new TypeHolder(typeParam.getSimpleName(), typeParam.getPackage().getName(), typeParam, WildcardBound.NO_WILDCARD, Collections.EMPTY_LIST);
 			classHolders.add(classHolder);
 		}
 		
 		return new TypeHolder(rawClass.getSimpleName(), rawClass.getPackage().getName(), rawClass, WildcardBound.NO_WILDCARD, classHolders);
 	}
 
 	public static TypeHolder newInstance(Class<?> rawClass, TypeHolder ... argumentClasses) {
 		return new TypeHolder(rawClass.getSimpleName(), rawClass.getPackage().getName(), rawClass, WildcardBound.NO_WILDCARD, Arrays.asList(argumentClasses));
 	}
 	
 	public static TypeHolder newInstance(Field field) {
 		return newInstance(field.getGenericType());
 	}
 	
 	public static TypeHolder newInstance(Type type) {
 		Pair<Class<?>, String> _class = getClass(type);
 		
 		Class<?> rawClass = _class.getItem1();
 		String name = rawClass != null ? rawClass.getSimpleName() : _class.getItem2();
 		String packageName = rawClass != null && rawClass.getPackage() != null ? rawClass.getPackage().getName() : "";
 
 
 		WildcardBound wildcardBound = WildcardBound.NO_WILDCARD;
 		
 		List<TypeHolder> typeParams = new ArrayList<TypeHolder>();
 		
 		Type candidateParamType = type;
 		
 		if (type instanceof WildcardType) {
 			WildcardType wildcardType = (WildcardType)type;
 			if (wildcardType.getLowerBounds() != null && wildcardType.getLowerBounds().length > 0) {
 				wildcardBound = WildcardBound.LOWER;
 				candidateParamType = wildcardType.getLowerBounds()[0];
 			}
 			else if (wildcardType.getUpperBounds() != null && wildcardType.getUpperBounds().length > 0) {
 				wildcardBound = WildcardBound.UPPER;
 				candidateParamType = wildcardType.getUpperBounds()[0];
 			}
 		}
 		
 		if (candidateParamType instanceof ParameterizedType) {
 			ParameterizedType paramType = (ParameterizedType)candidateParamType;
 			Type[] subTypes = paramType.getActualTypeArguments();
 			if (subTypes != null) {
 				for (Type subType : subTypes) {
 					typeParams.add(TypeHolder.newInstance(subType));
 				}
 			}
 		}
 		
 
 		return new TypeHolder(name, packageName, rawClass, wildcardBound, typeParams);
 	}
 	
 	private static Pair<Class<?>, String> getClass(Type type) {
 		if (type instanceof Class) {
 			return new Pair<Class<?>, String>((Class<?>)type, ((Class<?>)type).getName());
 		}
 		else if (type instanceof ParameterizedType) {
 			ParameterizedType paramType = (ParameterizedType)type;
 			return getClass(paramType.getRawType());
 		}
 		else if (type instanceof WildcardType) {
 			WildcardType wildcardType = (WildcardType)type;
 			if (wildcardType.getLowerBounds() != null && wildcardType.getLowerBounds().length > 0) {
 				return getClass(wildcardType.getLowerBounds()[0]);
 			}
 			
 			if (wildcardType.getUpperBounds() != null && wildcardType.getUpperBounds().length > 0) {
 				return getClass(wildcardType.getUpperBounds()[0]);
 			}
 		}
 		else if (type instanceof TypeVariable) {
 			TypeVariable<?> typeVariable = (TypeVariable<?>)type;
 			return new Pair<Class<?>, String>(null, typeVariable.getName());
 		}
 		
 		return new Pair<Class<?>, String>(null, null);
 	}
 
 	public String getBuilderRawClassName(String suffix, List<Class<?>> classesToBuild) {
 		return classesToBuild.contains(rawClass) ? name + suffix : name;
 	}
 	
 }
