 /*
  * Copyright 2003-2007 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package groovy.util;
 
 import groovy.lang.*;
 import org.codehaus.groovy.control.MultipleCompilationErrorsException;
 import org.codehaus.groovy.runtime.DefaultGroovyMethods;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.*;
 
 /**
  * Classes to generate 'Proxy' objects which implement interfaces
  * and/or extend classes.
  *
  * @author Paul King
  * @author Guillaume Laforge
  */
 public class ProxyGenerator {
     public static boolean debug = false;
 
     public static Object instantiateAggregateFromBaseClass(Class clazz) {
         return instantiateAggregateFromBaseClass(null, clazz);
     }
 
     public static Object instantiateAggregateFromBaseClass(Map map, Class clazz) {
         return instantiateAggregateFromBaseClass(map, clazz, null);
     }
 
     public static Object instantiateAggregateFromBaseClass(Map map, Class clazz, Object[] constructorArgs) {
         return instantiateAggregate(map, null, clazz, constructorArgs);
     }
 
     public static Object instantiateAggregateFromInterface(Class clazz) {
         return instantiateAggregateFromInterface(null, clazz);
     }
 
     public static Object instantiateAggregateFromInterface(Map map, Class clazz) {
         List interfaces = new ArrayList();
         interfaces.add(clazz);
         return instantiateAggregate(map, interfaces);
     }
 
     public static Object instantiateAggregate(List interfaces) {
         return instantiateAggregate(null, interfaces);
     }
 
     public static Object instantiateAggregate(Map closureMap, List interfaces) {
         return instantiateAggregate(closureMap, interfaces, null);
     }
 
     public static Object instantiateAggregate(Map closureMap, List interfaces, Class clazz) {
         return instantiateAggregate(closureMap, interfaces, clazz, null);
     }
 
     public static Object instantiateAggregate(Map closureMap, List interfaces, Class clazz, Object[] constructorArgs) {
         Map map = new HashMap();
         if (closureMap != null) {
             map = closureMap;
         }
         List interfacesToImplement = new ArrayList();
         if (interfaces != null) {
             interfacesToImplement = interfaces;
         }
         Class baseClass = GroovyObjectSupport.class;
         if (clazz != null) {
             baseClass = clazz;
         }
         boolean hasArgs = constructorArgs != null && constructorArgs.length > 0;
         String name = shortName(baseClass.getName()) + "_groovyProxy";
         StringBuffer buffer = new StringBuffer();
 
         // add class header and fields
         buffer.append("class ").append(name);
         if (clazz != null) {
             buffer.append(" extends ").append(baseClass.getName());
         }
         for (int i = 0; i < interfacesToImplement.size(); i++) {
             Class thisInterface = (Class) interfacesToImplement.get(i);
             if (i == 0) {
                 buffer.append(" implements ");
             } else {
                 buffer.append(", ");
             }
             buffer.append(thisInterface.getName());
         }
         buffer.append(" {\n").append("    private closureMap\n    ");
 
         // add constructor
         buffer.append(name).append("(map");
         if (hasArgs) {
             buffer.append(", args");
         }
         buffer.append(") {\n");
         buffer.append("        super(");
         if (hasArgs) {
             buffer.append("*args");
         }
         buffer.append(")\n");
         buffer.append("        this.closureMap = map\n");
         buffer.append("    }\n");
 
         // add overwriting methods
         List selectedMethods = new ArrayList();
         List publicAndProtectedMethods = DefaultGroovyMethods.toList(baseClass.getMethods());
         publicAndProtectedMethods.addAll(getInheritedMethods(baseClass));
         for (int i = 0; i < publicAndProtectedMethods.size(); i++) {
             Method method = (Method) publicAndProtectedMethods.get(i);
             if (method.getName().indexOf('$') != -1)
               continue;
             if (map.containsKey(method.getName())) {
                 selectedMethods.add(method.getName());
                 addOverridingMapCall(buffer, method);
             }
         }
 
         // add interface methods
         List interfaceMethods = new ArrayList();
         for (int i = 0; i < interfacesToImplement.size(); i++) {
             Class thisInterface = (Class) interfacesToImplement.get(i);
             interfaceMethods.addAll(DefaultGroovyMethods.toList(thisInterface.getMethods()));
             interfaceMethods.addAll(getInheritedMethods(thisInterface));
         }
         for (int i = 0; i < interfaceMethods.size(); i++) {
             Method method = (Method) interfaceMethods.get(i);
             if (!containsEquivalentMethod(publicAndProtectedMethods, method)) {
                 selectedMethods.add(method.getName());
                 addMapOrDummyCall(map, buffer, method);
             }
         }
 
         // add leftover methods from the map
         for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
             String methodName = (String) iterator.next();
             if (methodName.indexOf('$') != -1)
               continue;
             if (selectedMethods.contains(methodName)) continue;
             addNewMapCall(buffer, methodName);
         }
 
         // end class
 
         buffer.append("}\n").append("new ").append(name);
         buffer.append("(map");
         if (hasArgs) {
             buffer.append(", constructorArgs");
         }
         buffer.append(")");
 
         Binding binding = new Binding();
         binding.setVariable("map", map);
         binding.setVariable("constructorArgs", constructorArgs);
         ClassLoader cl = baseClass.getClassLoader();
         if (clazz == null && interfacesToImplement.size() > 0) {
             Class c = (Class) interfacesToImplement.get(0);
             cl = c.getClassLoader();
         }
         GroovyShell shell = new GroovyShell(cl, binding);
         if (debug)
             System.out.println("proxy source:\n------------------\n" + buffer.toString() + "\n------------------");
         try {
             return shell.evaluate(buffer.toString());
         } catch (MultipleCompilationErrorsException err) {
             throw new GroovyRuntimeException("Error creating proxy: " + err.getMessage());
         }
     }
 
     public static Object instantiateDelegate(Object delegate) {
         return instantiateDelegate(null, delegate);
     }
 
     public static Object instantiateDelegate(List interfaces, Object delegate) {
         return instantiateDelegate(null, interfaces, delegate);
     }
 
     public static Object instantiateDelegate(Map closureMap, List interfaces, Object delegate) {
         return instantiateDelegateWithBaseClass(closureMap, interfaces, delegate, null);
     }
 
     public static Object instantiateDelegateWithBaseClass(Map closureMap, List interfaces, Object delegate) {
         return instantiateDelegateWithBaseClass(closureMap, interfaces, delegate, delegate.getClass());
     }
 
     public static Object instantiateDelegateWithBaseClass(Map closureMap, List interfaces, Object delegate, Class baseClass) {
         String name = shortName(delegate.getClass().getName()) + "_delegateProxy";
         return instantiateDelegateWithBaseClass(closureMap, interfaces, delegate, baseClass, name);
     }
 
     public static Object instantiateDelegateWithBaseClass(Map closureMap, List interfaces, Object delegate, Class baseClass, String name) {
         Map map = new HashMap();
         if (closureMap != null) {
             map = closureMap;
         }
         List selectedMethods = new ArrayList();
         List interfacesToImplement = new ArrayList();
         if (interfaces != null) {
             interfacesToImplement = interfaces;
         }
         StringBuffer buffer = new StringBuffer();
 
         // add class header and fields
         buffer.append("import org.codehaus.groovy.runtime.InvokerHelper\nclass ").append(name);
         if (baseClass != null) {
             buffer.append(" extends ").append(baseClass.getName());
         }
         
         for (int i = 0; i < interfacesToImplement.size(); i++) {
             Class thisInterface = (Class) interfacesToImplement.get(i);
             if (i == 0) {
                 buffer.append(" implements ");
             } else {
                 buffer.append(", ");
             }
             buffer.append(thisInterface.getName());
         }
         buffer.append(" {\n").append("    private delegate\n").append("    private closureMap\n    ");
 
         // add constructor
         buffer.append(name).append("(map, delegate) {\n");
         buffer.append("        this.closureMap = map\n");
         buffer.append("        this.delegate = delegate\n");
         buffer.append("    }\n");
 
         List objectMethods = DefaultGroovyMethods.toList(Object.class.getMethods());
         objectMethods.addAll(getInheritedMethods(Object.class));
 
         List groovyObjectMethods = DefaultGroovyMethods.toList(GroovyObject.class.getMethods());
         groovyObjectMethods.addAll(getInheritedMethods(GroovyObject.class));
 
         // add interface methods
         List interfaceMethods = new ArrayList();
         for (int i = 0; i < interfacesToImplement.size(); i++) {
             Class thisInterface = (Class) interfacesToImplement.get(i);
             interfaceMethods.addAll(DefaultGroovyMethods.toList(thisInterface.getMethods()));
             interfaceMethods.addAll(getInheritedMethods(thisInterface));
         }
         for (int i = 0; i < interfaceMethods.size(); i++) {
             Method method = (Method) interfaceMethods.get(i);
             if (!containsEquivalentMethod(objectMethods, method) &&
                     !containsEquivalentMethod(groovyObjectMethods, method)) {
                 selectedMethods.add(method.getName());
                 addWrappedCall(buffer, method, map);
             }
         }
         List additionalMethods = new ArrayList();
         additionalMethods.addAll(DefaultGroovyMethods.toList(delegate.getClass().getMethods()));
         additionalMethods.addAll(getInheritedMethods(delegate.getClass()));
         for (int i = 0; i < additionalMethods.size(); i++) {
             Method method = (Method) additionalMethods.get(i);
             if (method.getName().indexOf('$') != -1)
               continue;
             if (!containsEquivalentMethod(interfaceMethods, method) &&
                     !containsEquivalentMethod(objectMethods, method) &&
                     !containsEquivalentMethod(groovyObjectMethods, method)) {
                 selectedMethods.add(method.getName());
                 addWrappedCall(buffer, method, map);
             }
         }
 
         // add leftover methods from the map
         for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
             String methodName = (String) iterator.next();
             if (selectedMethods.contains(methodName)) continue;
             addNewMapCall(buffer, methodName);
         }
 
         // end class
 
         buffer.append("}\n").append("new ").append(name);
         buffer.append("(map, delegate)");
 
         Binding binding = new Binding();
         binding.setVariable("map", map);
         binding.setVariable("delegate", delegate);
         ClassLoader cl = delegate.getClass().getClassLoader();
         GroovyShell shell = new GroovyShell(cl, binding);
         if (debug)
             System.out.println("proxy source:\n------------------\n" + buffer.toString() + "\n------------------");
         try {
             return shell.evaluate(buffer.toString());
         } catch (MultipleCompilationErrorsException err) {
             throw new GroovyRuntimeException("Error creating proxy: " + err.getMessage());
         }
     }
 
     private static void addWrappedCall(StringBuffer buffer, Method method, Map map) {
         if (map.containsKey(method.getName())) {
             addOverridingMapCall(buffer, method);
         } else {
             Class[] parameterTypes = addMethodPrefix(buffer, method);
             addWrappedMethodBody(buffer, method, parameterTypes);
             addMethodSuffix(buffer);
         }
     }
 
     private static boolean containsEquivalentMethod(List publicAndProtectedMethods, Method candidate) {
         for (int i = 0; i < publicAndProtectedMethods.size(); i++) {
             Method method = (Method) publicAndProtectedMethods.get(i);
             if (candidate.getName().equals(method.getName()) &&
                     candidate.getReturnType().equals(method.getReturnType()) &&
                     hasMatchingParameterTypes(candidate, method)) {
                 return true;
             }
         }
         return false;
     }
 
     private static boolean hasMatchingParameterTypes(Method method, Method candidate) {
         Class[] candidateParamTypes = candidate.getParameterTypes();
         Class[] methodParamTypes = method.getParameterTypes();
         if (candidateParamTypes.length != methodParamTypes.length) return false;
         for (int i = 0; i < methodParamTypes.length; i++) {
             if (!candidateParamTypes[i].equals(methodParamTypes[i])) return false;
         }
         return true;
     }
 
     private static List getInheritedMethods(Class baseClass) {
         List protectedMethodList = new ArrayList();
         Class currentClass = baseClass;
         while (currentClass != null) {
             Method[] protectedMethods = currentClass.getDeclaredMethods();
             for (int i = 0; i < protectedMethods.length; i++) {
                 Method method = protectedMethods[i];
                 if (method.getName().indexOf('$') != -1)
                   continue;
                if (Modifier.isProtected(method.getModifiers()))
                     protectedMethodList.add(method);
             }
             currentClass = currentClass.getSuperclass();
         }
         return protectedMethodList;
     }
 
     private static void addNewMapCall(StringBuffer buffer, String methodName) {
         buffer.append("    def ").append(methodName).append("(Object[] args) {\n")
                 .append("        this.@closureMap['").append(methodName).append("'] (*args)\n    }\n");
     }
 
     private static void addOverridingMapCall(StringBuffer buffer, Method method) {
         Class[] parameterTypes = addMethodPrefix(buffer, method);
         addMethodBody(buffer, method, parameterTypes);
         addMethodSuffix(buffer);
     }
 
     private static void addMapOrDummyCall(Map map, StringBuffer buffer, Method method) {
         Class[] parameterTypes = addMethodPrefix(buffer, method);
         if (map.containsKey(method.getName())) {
             addMethodBody(buffer, method, parameterTypes);
         }
         addMethodSuffix(buffer);
     }
 
     private static Class[] addMethodPrefix(StringBuffer buffer, Method method) {
         buffer.append("    ").append(getSimpleName(method.getReturnType()))
                 .append(" ").append(method.getName()).append("(");
         Class[] parameterTypes = method.getParameterTypes();
         for (int parameterTypeIndex = 0; parameterTypeIndex < parameterTypes.length; parameterTypeIndex++) {
             Class parameter = parameterTypes[parameterTypeIndex];
             if (parameterTypeIndex != 0) {
                 buffer.append(", ");
             }
             buffer.append(getSimpleName(parameter)).append(" ")
                     .append("p").append(parameterTypeIndex);
         }
         buffer.append(") { ");
         return parameterTypes;
     }
 
     private static void addMethodBody(StringBuffer buffer, Method method, Class[] parameterTypes) {
         buffer.append("this.@closureMap['").append(method.getName()).append("'] (");
         for (int j = 0; j < parameterTypes.length; j++) {
             if (j != 0) {
                 buffer.append(", ");
             }
             buffer.append("p").append(j);
         }
         buffer.append(")");
     }
 
     private static void addWrappedMethodBody(StringBuffer buffer, Method method, Class[] parameterTypes) {
         buffer.append("\n        Object[] args = [");
         for (int j = 0; j < parameterTypes.length; j++) {
             if (j != 0) {
                 buffer.append(", ");
             }
             buffer.append("p").append(j);
         }
         buffer.append("]\n        ");
         buffer.append("InvokerHelper.invokeMethod(delegate, '").append(method.getName()).append("', args)\n");
     }
 
     private static void addMethodSuffix(StringBuffer buffer) {
         buffer.append("    }\n");
     }
 
     /**
      * TODO once we switch to Java 1.5 bt default, use Class#getSimpleName() directly
      *
      * @param c the class of which we want the readable simple name
      * @return the readable simple name
      */
     private static String getSimpleName(Class c) {
         if (c.isArray()) {
             int dimension = 0;
             Class componentClass = c;
             while (componentClass.isArray()) {
                 componentClass = componentClass.getComponentType();
                 dimension++;
             }
             return componentClass.getName().replaceAll("\\$", "\\.") +
                     DefaultGroovyMethods.multiply("[]", new Integer(dimension));
         } else {
             return c.getName().replaceAll("\\$", "\\.");
         }
     }
 
     public static String shortName(String name) {
         int index = name.lastIndexOf('.');
         if (index == -1) return name;
         return name.substring(index + 1, name.length());
     }
 
 }
