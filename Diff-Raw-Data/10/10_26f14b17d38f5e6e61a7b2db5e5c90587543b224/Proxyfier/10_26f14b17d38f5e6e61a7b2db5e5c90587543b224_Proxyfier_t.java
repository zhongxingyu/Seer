 /**
  * Copyright (C) 2008 Ovea <dev@testatoo.org>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.testatoo.selenium.server.proxy;
 
 import org.testatoo.selenium.server.util.SearchingClassLoader;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 public final class Proxyfier {
 
     public static <T> T proxify(final Object instance, final ClassLoader threadClassLoader, Class<T> proxyInterface, final MethodHandler... methodHandlers) {
         return Proxyfier.<T>proxify(instance, threadClassLoader, new Class<?>[]{proxyInterface}, methodHandlers);
     }
 
     public static <T> T proxify(final Object instance, final ClassLoader threadClassLoader, Class<?>[] interfaces, final MethodHandler... methodHandlers) {
         if (interfaces == null || interfaces.length == 0) throw new IllegalArgumentException("Missing interfaces");
         for (Class<?> aClass : interfaces) {
             if (!aClass.isInterface()) throw new IllegalArgumentException("Bad argument type: must be interfaces");
         }
         // No inspection unchecked
         return (T) Proxy.newProxyInstance(SearchingClassLoader.combineLoadersOf(interfaces), interfaces, new InvocationHandler() {
             public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                 for (MethodHandler methodHandler : methodHandlers) {
                    if (methodHandler.canHandle(method)) {
                        final ClassLoader backupCl = Thread.currentThread().getContextClassLoader();
                        Thread.currentThread().setContextClassLoader(threadClassLoader);
                        try {
                            return methodHandler.invoke(instance, method, args);
                        } finally {
                            Thread.currentThread().setContextClassLoader(backupCl);
                        }
                    }
                 }
                 final ClassLoader backupCl = Thread.currentThread().getContextClassLoader();
                 try {
                     Thread.currentThread().setContextClassLoader(threadClassLoader);
                     final Object ret = instance.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(instance, args);
                     return ret == null || Void.TYPE.isInstance(ret) ? proxy : ret;
                 } catch (InvocationTargetException e) {
                     if (e.getTargetException() instanceof RuntimeException)
                         throw (RuntimeException) e.getTargetException();
                     else throw new RuntimeException(e.getTargetException().getMessage(), e.getTargetException());
                 } catch (Exception e) {
                     throw new RuntimeException(e.getMessage(), e);
                 } finally {
                     Thread.currentThread().setContextClassLoader(backupCl);
                 }
             }
         });
     }
 
 }
