 /*
  * Copyright 2010 CodeGist.org
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
  *
  * ===================================================================
  *
  * More information at http://www.codegist.org.
  */
 
 package org.codegist.common.reflect;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
import java.util.*;
 import java.util.regex.Pattern;
 
 /**
  * @author Laurent Gilles (laurent.gilles@codegist.org)
  */
 public final class Methods {
     private Methods() {
     }
 
    public static Map<Class<? extends Annotation>, Annotation>[] getParamsAnnotation(Method method) {
         int paramsCount = method.getParameterTypes().length;
        Map<Class<? extends Annotation>, Annotation>[] paramAnnotations = new Map[paramsCount];
         for (int i = 0; i < paramsCount; i++) {
             paramAnnotations[i] = getParamsAnnotation(method, i);
         }
         return paramAnnotations;
     }
    public static Map<Class<? extends Annotation>, Annotation> getParamsAnnotation(Method method, int index) {
         Annotation[] annotations = method.getParameterAnnotations()[index];
        Map<Class<? extends Annotation>, Annotation> paramAnnotations = new LinkedHashMap<Class<? extends Annotation>, Annotation>();
         for (Annotation anno : annotations) {
            paramAnnotations.put(anno.annotationType(), anno);
         }
         return paramAnnotations;
     }
     /**
      * Returns true if the given method is either equals()/hashCode()/toString() method.
      *
      * @param method Method to check
      * @return true if the given method is either equals()/hashCode()/toString() method
      */
     public static boolean isFromObject(Method method) {
         return isEquals(method) || isHashCode(method) || isToString(method);
     }
 
     /**
      * Checks if the given method is the Object.equals() method.
      *
      * @param method method to check
      * @return true if the given method is the Object.equals() method
      */
     public static boolean isEquals(Method method) {
         if (method == null || !method.getName().equals("equals")) {
             return false;
         }
         Class<?>[] paramTypes = method.getParameterTypes();
         return (paramTypes.length == 1 && paramTypes[0] == Object.class);
     }
 
     /**
      * Checks if the given method is the Object.hashCode() method.
      *
      * @param method method to check
      * @return true if the given method is the Object.hashCode() method
      */
     public static boolean isHashCode(Method method) {
         return (method != null && method.getName().equals("hashCode") && method.getParameterTypes().length == 0);
     }
 
     /**
      * Checks if the given method is the Object.toString() method.
      *
      * @param method method to check
      * @return true if the given method is the Object.toString() method
      */
     public static boolean isToString(Method method) {
         return (method != null && method.getName().equals("toString") && method.getParameterTypes().length == 0);
     }
 
     /**
      * Returns the declared method array of the given class that matches the given name regex pattern.
      *
      * @param clazz   Class to check
      * @param pattern name pattern
      * @return method array matching the given pattern
      */
     public static Method[] getDeclaredMethodsThatMatches(Class clazz, String pattern) {
         return getDeclaredMethodsThatMatches(clazz, Pattern.compile(pattern));
     }
 
     /**
      * Returns the declared method array of the given class that matches the given name regex pattern.
      *
      * @param clazz   Class to check
      * @param pattern name pattern
      * @return method array matching the given pattern
      */
     public static Method[] getDeclaredMethodsThatMatches(Class clazz, Pattern pattern) {
         return getDeclaredMethodsThatMatches(clazz, pattern, false);
     }
 
     /**
      * Returns the declared method array of the given class that matches the given regex pattern.
      *
      * @param clazz               Class to check
      * @param pattern             pattern
      * @param includeParamInMatch If true, pattern can contains method parameter types
      * @return method array matching the given pattern
      */
     public static Method[] getDeclaredMethodsThatMatches(Class clazz, String pattern, boolean includeParamInMatch) {
         return getDeclaredMethodsThatMatches(clazz, Pattern.compile(pattern), includeParamInMatch);
     }
 
     /**
      * Returns the declared method array of the given class that matches the given regex pattern.
      *
      * @param clazz               Class to check
      * @param pattern             pattern
      * @param includeParamInMatch If true, pattern can contains method parameter types, eg my.*meth\(java\.lang\.String,int(,int[])?\)
      * @return method array matching the given pattern
      */
     public static Method[] getDeclaredMethodsThatMatches(Class clazz, Pattern pattern, boolean includeParamInMatch) {
         List<Method> methodsList = new ArrayList<Method>();
         Method[] methods = clazz.getDeclaredMethods();
         for (Method method : methods) {
             StringBuilder name = new StringBuilder(method.getName());
             if (includeParamInMatch) {
                 name.append("(");
                 int i = 0, max = method.getParameterTypes().length;
                 for (Class<?> type : method.getParameterTypes()) {
                     name.append(Types.getTypeName(type)).append(++i < max ? "," : "");
                 }
                 name.append(")");
             }
             if (pattern.matcher(name).matches()) {
                 methodsList.add(method);
             }
         }
         return methodsList.toArray(new Method[methodsList.size()]);
     }
 }
