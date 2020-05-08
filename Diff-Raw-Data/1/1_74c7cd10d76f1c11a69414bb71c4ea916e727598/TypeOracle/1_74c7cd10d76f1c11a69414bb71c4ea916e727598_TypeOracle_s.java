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
 
 package com.google.code.nanorm.internal.introspect;
 
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.lang.reflect.WildcardType;
 
import com.google.code.nanorm.exceptions.TypeOracleException;
 import com.google.code.nanorm.internal.introspect.asm.ResolvedParameterizedType;
 
 /**
  * Utilities to resolve generic types using the reflection. Used for resolving
  * concrete types while generating the property accessors for nested properties.
  * 
  * <code>
  * // Generic type:
  * public class Wrapper&lt;T&gt; {
  *     private T value;
  *     
  *     public T getValue() {
  *         return value;
  *     }
  *     
  *     public void setValue(T value) {
  *         this.value = value;
  *     }
  * }
  * 
  * // Type that uses wrapper:
  * public class Bean {
  *     private Wrapper&lt;String&gt item;
  *     
  *     public Wrapper&lt;String&gt getItem() {
  *         return item;
  *     }
  *     
  *     public void setItem(Wrapper&lt;String&gt item) {
  *         this.item = item;
  *     }
  * }
  * 
  * // Code to resolve getValue actual type
  * Class&lt;?&gt; clazz = Bean.class;
  * Type returnType = clazz.getMethod("getItem").getGenericReturnType();
  * ParameterizedType pt = new ResolvedParameterizedType(clazz);
  * 
  * // Resolve return type of getItem method
  * pt = resolve(returnType, pt);
  * 
  * // Resolve return type of getValue method 
  * returnType = Wrapper.class.getMethod("getValue").getGenericReturnType();
  * pt = resolve(returnType, pt);
  * 
  * // Now pt.getRawType() will be String.class. 
  * </code>
  * 
  * @author Ivan Dubrov
  * @version 1.0 20.06.2008
  */
 public class TypeOracle {
 
     /**
      * Resolve type in given context. Replaces all {@link TypeVariable}
      * instances using the actual parameters of the context type.
      * 
      * Propagates as much type information as possible from the context
      * parameter (which is usually the specialization of the declaring class) to
      * the given type parameter (which is any type used inside the generic
      * declaring class) and returns it as {@link ParameterizedType}.
      * 
      * @param type type to resolve
      * @param context context type (specialized generic or concrete class)
      * @return resolved type
      */
     public static Type resolve(Type type, Type context) {
         // First, we wrap context into ParameterizedType for simplicity
         // In fact, it should be only concrete type ({@link Class}) or
         // parameterized type ({@link ParameterizedType}).
         Type resolved;
         if (context instanceof Class<?>) {
             Class<?> clazz = (Class<?>) context;
             resolved = resolveImpl(type, new ResolvedParameterizedType(clazz));
         } else if (context instanceof ParameterizedType) {
             resolved = resolveImpl(type, (ParameterizedType) context);
         } else {
             throw new IllegalArgumentException("Illegal context argument: " + context
                     + ". Context type could be only java.lang.Class or "
                     + "java.lang.reflect.ParameterizedType instance.");
         }
         return resolved;
     }
 
     /**
      * Resolve {@link Class} instance from given {@link Type}, which could be
      * either {@link Class} instance of {@link ParameterizedType} instance.
      * 
      * @param type type to resolve
      * @return resolved {@link Class} instance
      */
     public static Class<?> resolveClass(Type type) {
         if (type instanceof Class<?>) {
             return (Class<?>) type;
         } else if (type instanceof ParameterizedType) {
             ParameterizedType pt = (ParameterizedType) type;
             if (pt.getRawType() instanceof Class<?>) {
                 return (Class<?>) pt.getRawType();
             }
             throw new IllegalArgumentException("Illegal raw type of parameterized type: "
                     + pt.getRawType() + ". Raw type could be only java.lang.Class instance.");
         } else {
             throw new IllegalArgumentException("Illegal type argument: " + type
                     + ". Type could be only java.lang.Class instance or "
                     + "java.lang.reflect.ParameterizedType instance.");
         }
     }
 
     /**
      * Resolve type using the given context.
      * 
      * Returns {@link Class} instances as is. Resolves {@link TypeVariable}
      * using {@link #resolveTypeVariable(TypeVariable, ParameterizedType)}.
      * Resolves {@link ParameterizedType} recursively.
      * 
      * TODO: Implement support for wildcards.
      * 
      * @see #resolve(Type, Type)
      * @see #resolveTypeVariable(TypeVariable, ParameterizedType)
      * @param type type to resolve.
      * @param context context type
      * @return resolved type
      */
     private static Type resolveImpl(Type type, ParameterizedType context) {
         if (type instanceof Class<?>) {
             // If type is concrete class -- nothing to resolve, all type info is
             // known
             return type;
         } else if (type instanceof TypeVariable<?>) {
             // If type is type variable -- resolve it
             TypeVariable<?> tv = (TypeVariable<?>) type;
 
             return resolveTypeVariable(tv, context);
         } else if (type instanceof ParameterizedType) {
             ParameterizedType pt = (ParameterizedType) type;
 
             // First resolve raw type
             // TODO: Pass context?
             Class<?> resolvedRawType = resolveClass(pt.getRawType());
 
             // Recursively resolve actual type arguments
             Type[] arguments = pt.getActualTypeArguments();
             Type[] res = new Type[arguments.length];
             for (int i = 0; i < res.length; ++i) {
                 res[i] = resolveImpl(arguments[i], context);
             }
             return new ResolvedParameterizedType(resolvedRawType, res);
         } else if (type instanceof WildcardType) {
             WildcardType wildcard = (WildcardType) type;
 
             Type[] bounds = wildcard.getUpperBounds();
             if (bounds.length == 0) {
                 // TODO: Log!
                 return Object.class;
             }
 
             // Currently only one bound is possible
             return bounds[0];
         } else {
             throw new RuntimeException("Not supported!");
         }
     }
 
     /**
      * Resolve {@link TypeVariable} using given context. Finds the actual
      * argument for given {@link TypeVariable} and returns it.
      * 
      * TODO: Try to get type information from bounds in corner cases.
      * 
      * @param tv
      * @param context
      * @return resolved type variable
      */
     private static Type resolveTypeVariable(TypeVariable<?> tv, ParameterizedType context) {
         Class<?> ownerRaw = resolveClass(context.getRawType());
         TypeVariable<?>[] params = ownerRaw.getTypeParameters();
         for (int i = 0; i < params.length; ++i) {
             if (params[i].equals(tv)) {
                 Type argument = context.getActualTypeArguments()[i];
                 // TODO: This could be TypeVariable, in that case we probably
                 // need to get
                 // information from bounds.
                 return argument;
             }
         }
         throw new RuntimeException("Could not resolve!");
     }
 }
