 /*
  * Orika - simpler, better and faster Java bean mapping
  * 
  * Copyright (C) 2011 Orika authors
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
 package ma.glasnost.orika.metadata;
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.TypeVariable;
 import java.lang.reflect.WildcardType;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * TypeFactory contains various methods for obtaining a Type instance to
  * represent various type situations.
  * 
  * @author matt.deboer@gmail.com
  * 
  *  
  */
 public abstract class TypeFactory {
     
     /**
      * Should not be extended
      */
     private TypeFactory() { }
     
     /**
      * Use a weak-valued concurrent map to avoid keeping static references to Types
      * (classes) which may belong to descendant class-loaders
      */
     private static final ConcurrentHashMap<TypeKey, WeakReference<Type<?>>> typeCache = new ConcurrentHashMap<TypeKey, WeakReference<Type<?>>>();
     
     public static final Type<Object> TYPE_OF_OBJECT = valueOf(Object.class);
         
     /**
      * Store the combination of rawType and type arguments as a Type within the
      * type cache.<br>
      * Use the existing type if already available; we try to enforce that Type
      * should be immutable.
      * 
      * @param rawType
      * @param typeArguments
      * @return
      */
     @SuppressWarnings("unchecked")
     private static <T> Type<T> intern(Class<T> rawType, java.lang.reflect.Type[] typeArguments) {
         
         Type<?>[] convertedArguments = TypeUtil.convertTypeArguments(rawType, typeArguments);
         TypeKey key = TypeKey.valueOf(rawType, convertedArguments);
         
         WeakReference<Type<?>> mapped = typeCache.get(key);
         if (mapped == null || mapped.get() == null) {
        	synchronized(rawType) {
         		mapped = typeCache.get(key);
         		if (mapped == null || mapped.get() == null) {
 		            mapped = new WeakReference<Type<?>>(createType(key, rawType, convertedArguments));
 		            WeakReference<Type<?>> existing = typeCache.putIfAbsent(key, mapped);
 		            if (existing != null) {
 		                if (existing.get() == null) {
 		                    // Should not be possible, since the references are based on Class objects,
 		                    // which cannot be GC'd until their respective class loader is GC'd,
 		                    // in which case, such a Class could not be passed into this method as
 		                    // an argument, or embedded within an argument
 		                    typeCache.put(key, mapped);
 		                } else {
 		                    mapped = existing;
 		                }
 		            }
         		}
         	}
         }
         return (Type<T>) mapped.get();
     }
     
     private static <T> Type<T> createType(TypeKey key, Class<T> rawType, Type<?>[] typeArguments) {
         Map<String, Type<?>> typesByVariable = null;
         if (typeArguments.length > 0) {
             typesByVariable = new HashMap<String, Type<?>>(typeArguments.length);
             for (int i = 0, len = typeArguments.length; i < len; ++i) {
                 typesByVariable.put(rawType.getTypeParameters()[i].getName(), typeArguments[i]);
             }
         }
         return new Type<T>(key, rawType, typesByVariable, typeArguments);
     }
     
     /**
      * @param rawType
      * @return
      */
     @SuppressWarnings("unchecked")
     public static <E> Type<E> valueOf(final Class<?> rawType) {
         if (rawType == null) {
             return null;
         } else if (rawType.isAnonymousClass() && rawType.getGenericSuperclass() instanceof ParameterizedType) {
             ParameterizedType genericSuper = (ParameterizedType) rawType.getGenericSuperclass();
             return valueOf(genericSuper);
         } else {
             return (Type<E>) intern(rawType, new java.lang.reflect.Type[0]);
         }
     }
     
     /**
      * @param rawType
      * @param actualTypeArguments
      * @return
      */
     @SuppressWarnings("unchecked")
     public static <E> Type<E> valueOf(final Class<?> rawType, final java.lang.reflect.Type... actualTypeArguments) {
         if (rawType == null) {
             return null;
         } else {
             return (Type<E>) intern((Class<E>) rawType, actualTypeArguments);
         }
     }
     
     /**
      * This method declaration helps to shortcut the other methods for
      * ParameterizedType which it extends; we just return it.
      * 
      * @param type
      * @return
      */
     public static <T> Type<T> valueOf(final Type<T> type) {
         return type;
     }
     
     /**
      * Return the Type for the given ParameterizedType, resolving actual type
      * arguments where possible.
      * 
      * @param type
      * @return
      */
     @SuppressWarnings("unchecked")
     public static <T> Type<T> valueOf(final ParameterizedType type) {
         if (Enum.class.equals(type.getRawType())) {
             // Enum is a special recursively-defined type which causes
             // StackOverflowError; this doesn't seem to occur with other
             // recursively-defined types...
             return (Type<T>) valueOf(Enum.class, new Type<?>[0]);
         } else {
             return valueOf((Class<? extends T>) type.getRawType(), type.getActualTypeArguments());
         }
     }
     
     
     
     @SuppressWarnings("unchecked")
     public static <T> Type<T> valueOf(final TypeVariable<?> var) {
 
         if (var.getBounds().length > 0) {
             Set<Type<?>> bounds = new HashSet<Type<?>>(var.getBounds().length);
             for (int i=0, len=var.getBounds().length; i < len; ++i) {
                 bounds.add(valueOf(var.getBounds()[i]));
             }
             return (Type<T>) refineBounds(bounds);
         } else {
             return (Type<T>) TYPE_OF_OBJECT;
         }
     }
     
     @SuppressWarnings("unchecked")
     public static <T> Type<T> valueOf(final WildcardType var) {
 
         Set<Type<?>> bounds = new HashSet<Type<?>>(var.getUpperBounds().length + var.getLowerBounds().length);
         for (int i=0, len=var.getUpperBounds().length; i < len; ++i) {
             bounds.add(valueOf(var.getUpperBounds()[i]));
         }
         for (int i=0, len=var.getLowerBounds().length; i < len; ++i) {
             bounds.add(valueOf(var.getLowerBounds()[i]));
         }
         return (Type<T>) refineBounds(bounds); 
     }
     
     /**
      * Returns the most specific type from the set of provided bounds.
      * 
      * @param bounds
      * @return
      */
     private static Type<?> refineBounds(Set<Type<?>> bounds) {
         if (bounds.size() > 1) {
             // Consolidate bounds to most significant
             Iterator<Type<?>> currentBoundIter = bounds.iterator();
             while (currentBoundIter.hasNext()) {
                 Type<?> currentBound = currentBoundIter.next();
                 Iterator<Type<?>> boundIter = bounds.iterator();
                 while (boundIter.hasNext()) {
                     Type<?> nextType = boundIter.next();
                     if (nextType.equals(currentBound)) {
                         continue;
                     } else{
                         Type<?> mostSpecific = TypeUtil.getMostSpecificType(currentBound, nextType);
                         if (nextType.equals(mostSpecific)) {
                             boundIter.remove();
                         }
                     }
                     
                 }
             }
             if (bounds.size() != 1) {
                 throw new IllegalArgumentException(bounds + " is not refinable");
             }
         }
         
         return bounds.iterator().next(); 
     }
     
     
     /**
      * Return the Type for the given java.lang.reflect.Type, either for a ParameterizedType
      * or a Class instance
      * 
      * @param type
      * @return
      */
     @SuppressWarnings("unchecked")
     public static <T> Type<T> valueOf(final java.lang.reflect.Type type) {
         if (type instanceof Type) {
             return (Type<T>)type;
         } else if (type instanceof ParameterizedType) {
             return valueOf((ParameterizedType)type);
         } else if (type instanceof Class) {
             return valueOf((Class<T>)type);
         } else if (type instanceof TypeVariable) {
             return valueOf((TypeVariable<?>)type);
         } else if (type instanceof WildcardType) {
             return valueOf((WildcardType)type);
         } else {
             throw new IllegalArgumentException(type + " is an unsupported type");
         }
     }
     
     
     /**
      * Resolve the Type for the given ParameterizedType, using the provided
      * referenceType to resolve any unresolved actual type arguments.
      * 
      * @param type
      * @param referenceType
      * @return
      */
     @SuppressWarnings("unchecked")
     public static <T> Type<T> resolveValueOf(final ParameterizedType type, final Type<?> referenceType) {
         if (type == null) {
             return null;
         } else {
             java.lang.reflect.Type[] actualTypeArguments = TypeUtil.resolveActualTypeArguments(type, referenceType);
             Type<T> result = intern((Class<T>) type.getRawType(), actualTypeArguments);
             
             return result;
         }
     }
     
     /**
      * Resolve the Type for the given Class, using the provided referenceType to
      * resolve the actual type arguments.
      * 
      * @param type
      * @param referenceType
      * @return
      */
     public static <T> Type<T> resolveValueOf(final Class<T> type, final Type<?> referenceType) {
         if (type == null) {
             return null;
         } else {
             if (type.getTypeParameters() != null && type.getTypeParameters().length > 0) {
                 java.lang.reflect.Type[] actualTypeArguments = TypeUtil.resolveActualTypeArguments(type.getTypeParameters(), type.getTypeParameters(), referenceType);
                 return valueOf(type, actualTypeArguments);
             } else {
                 return valueOf(type);
             }
         }
     }
     
     /**
      * Return the Type for the given object.
      * 
      * @param object
      * @return
      */
     @SuppressWarnings("unchecked")
     public static <T> Type<T> typeOf(final T object) {
         return (Type<T>) (object == null ? null : valueOf((Class<T>) object.getClass()));
     }
     
     /**
      * Resolve the Type for the given object, using the provided referenceType
      * to resolve the actual type arguments.
      * 
      * @param object
      * @param referenceType
      * @return
      */
     @SuppressWarnings("unchecked")
     public static <T> Type<T> resolveTypeOf(final T object, Type<?> referenceType) {
         return object == null ? null : resolveValueOf((Class<T>) object.getClass(), referenceType);
     }
     
     /**
      * Resolve the (element) component type for the given array.
      * 
      * @param object
      * @return
      */
     @SuppressWarnings("unchecked")
     public static <T> Type<T> componentTypeOf(final T[] object) {
         return (Type<T>) (object == null ? null : valueOf((Class<T>) object.getClass().getComponentType()));
     }
     
     /**
      * Resolve the nested element type for the given Iterable.
      * 
      * @param object
      * @return
      */
     @SuppressWarnings("unchecked")
     public static <T> Type<T> elementTypeOf(final Iterable<T> object) {
         return valueOf((Class<T>) (object == null || !object.iterator().hasNext() ? null : object.iterator().next().getClass()));
     }
     
 }
