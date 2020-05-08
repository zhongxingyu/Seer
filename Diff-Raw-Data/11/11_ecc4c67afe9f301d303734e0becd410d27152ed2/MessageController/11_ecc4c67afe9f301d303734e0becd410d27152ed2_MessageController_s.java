 package com.scottbyrns;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.*;
 
 /**
  * Aspect messaging between disparate class methods through out an application.
  *
  * Messages that are handled have all execution errors mucked to prevent an unwanted message handler from crashing your software.
  *
  *
  * Copyright (C) 2012 by Scott Byrns
  * http://github.com/scottbyrns
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  * <p/>
  * Created 7/17/12 10:54 AM
  */
 public class MessageController
 {
     private static Map<String, List<ScopedMethod>> methods = new HashMap<String, List<ScopedMethod>>();
 
     private static Map<String, List<ScopedMethod>> getMethods()
     {
         return methods;
     }
 
     private static void setMethods(Map<String, List<ScopedMethod>> methods)
     {
         MessageController.methods = methods;
     }
 
     /**
      * Register a listener.
      *
      * @param registerAsCallback Annotated description of message registration.
      * @param callback The method to callback to when a message is sent.
      * @param context The execution context to execute a method callback in.
      */
     private static void registerListener(RegisterAsCallback registerAsCallback, Method callback, Object context) {
         List<ScopedMethod> methodList;
         ScopedMethod scopedMethod = new ScopedMethod(context, callback, registerAsCallback.channel());
 
         if (null != getMethods().get(registerAsCallback.group())) {
             methodList = getMethods().get(registerAsCallback.group());
             methodList.add(scopedMethod);
         }
         else {
             methodList = new ArrayList<ScopedMethod>();
             methodList.add(scopedMethod);
             getMethods().put(registerAsCallback.group(), methodList);
         }
 
     }
 
     /**
      * Scan a class for {@link RegisterAsCallback} annotations and register them as callbacks in the message controller.
      *
      * @param clazz The class to register callbacks for.
      * @param context The execution context to run class listeners in.
      */
     public static void registerListenersOfClass(Class clazz, Object context) {
         Method[] clazzMethods = clazz.getMethods();
         for (Method method: clazzMethods) {
             RegisterAsCallback registerAsCallback = method.getAnnotation(RegisterAsCallback.class);
             if (null == registerAsCallback) {
                 continue;
             }
 
 
             registerListener(registerAsCallback, method, context);
         }
     }
 
     public static void unregisterListenersOfClass(Class clazz) {
         Method[] clazzMethods = clazz.getMethods();
         for (Method method: clazzMethods) {
             RegisterAsCallback registerAsCallback = method.getAnnotation(RegisterAsCallback.class);
             if (null == registerAsCallback) {
                 continue;
             }
 
 
             unregisterListener(registerAsCallback, method);
         }
     }
 
     public static void unregisterListener(RegisterAsCallback registerAsCallback, Method method) {
         List<ScopedMethod> scopedMethods = getMethods().get(registerAsCallback.group());
         Iterator<ScopedMethod> iterator = scopedMethods.iterator();
 
         ScopedMethod scopedMethod;
         ScopedMethod methodToRemove = null;
         while (iterator.hasNext()) {
             scopedMethod = iterator.next();
             if (scopedMethod.getMethod().toGenericString().equals(method.toGenericString())) {
                 methodToRemove = scopedMethod;
             }
         }
         scopedMethods.remove(methodToRemove);
     }
 
     /**
      * Send a message to a message handler.
      *
      * @param key The handler set to invoke.
      */
     public static void sendMessage(String key, Message message) {
         List<ScopedMethod> methodList = getMethods().get(key);
         Iterator<ScopedMethod> methodIterator = methodList.iterator();
         ScopedMethod scopedMethod;
         while (methodIterator.hasNext()) {
             try {
                 scopedMethod = methodIterator.next();
                 scopedMethod.invoke(message);
             }
             catch (Throwable e) {
                 e.printStackTrace();
                 // Muck all exceptions.
             }
         }
     }
 
     /**
      * Send a message to a message handler.
      *
      * @param key The handler set to invoke.
      */
     public static void sendMessage(String key, Object message) {
         List<ScopedMethod> methodList = getMethods().get(key);
         if (null == methodList) {
             return;
         }
         Iterator<ScopedMethod> methodIterator = methodList.iterator();
         ScopedMethod scopedMethod;
         while (methodIterator.hasNext()) {
             try {
                 scopedMethod = methodIterator.next();
                 scopedMethod.invoke(Message.create(message));
             }
             catch (Throwable e) {
                 e.printStackTrace();
                 // Muck all exceptions.
             }
         }
     }
 }
